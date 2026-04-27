package com.rentify.reviewService.service;

import com.rentify.reviewService.client.PropertyServiceClient;
import com.rentify.reviewService.client.UserServiceClient;
import com.rentify.reviewService.constants.ReviewConstants.*;
import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.dto.external.PropiedadDTO;
import com.rentify.reviewService.dto.external.UsuarioDTO;
import com.rentify.reviewService.exception.BusinessValidationException;
import com.rentify.reviewService.exception.ResourceNotFoundException;
import com.rentify.reviewService.model.Review;
import com.rentify.reviewService.model.TipoResena;
import com.rentify.reviewService.repository.ReviewRepository;
import com.rentify.reviewService.repository.TipoResenaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.rentify.reviewService.constants.ReviewConstants.Mensajes;

/**
 * Implementación del servicio de reseñas.
 * Contiene toda la lógica de negocio para gestión de reseñas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TipoResenaRepository tipoResenaRepository;
    private final UserServiceClient userServiceClient;
    private final PropertyServiceClient propertyServiceClient;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ReviewDTO crearResena(ReviewDTO reviewDTO) {
        log.info("Creando nueva reseña para usuario {}", reviewDTO.getUsuarioId());

        // 1. Validar que el usuario existe
        UsuarioDTO usuario = userServiceClient.getUserById(reviewDTO.getUsuarioId());
        if (usuario == null) {
            throw new BusinessValidationException(
                    String.format(Mensajes.USUARIO_NO_EXISTE, reviewDTO.getUsuarioId())
            );
        }

        // 2. Validar rol del usuario
        if (!Roles.puedeCrearResena(usuario.getRol())) {
            throw new BusinessValidationException(Mensajes.ROL_INVALIDO_RESENA);
        }

        // 3. Validar puntaje
        if (!Limites.esPuntajeValido(reviewDTO.getPuntaje())) {
            throw new BusinessValidationException(
                    String.format(Mensajes.PUNTAJE_INVALIDO, Limites.PUNTAJE_MINIMO, Limites.PUNTAJE_MAXIMO)
            );
        }

        // 4. Validar tipo de reseña
        TipoResena tipoResena = tipoResenaRepository.findById(reviewDTO.getTipoResenaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.TIPO_RESENA_NO_ENCONTRADO, reviewDTO.getTipoResenaId())
                ));

        // 5. Validar según el tipo de reseña
        if (reviewDTO.getPropiedadId() != null) {
            validarResenaPropiedad(reviewDTO, usuario);
        } else if (reviewDTO.getUsuarioResenadoId() != null) {
            validarResenaUsuario(reviewDTO);
        } else {
            throw new BusinessValidationException(
                    "La reseña debe tener una propiedad o un usuario reseñado"
            );
        }

        // 6. Validar longitud de comentario si existe
        if (reviewDTO.getComentario() != null) {
            if (reviewDTO.getComentario().length() < Limites.MIN_LONGITUD_COMENTARIO) {
                throw new BusinessValidationException(
                        String.format(Mensajes.COMENTARIO_MUY_CORTO, Limites.MIN_LONGITUD_COMENTARIO)
                );
            }
            if (reviewDTO.getComentario().length() > Limites.MAX_LONGITUD_COMENTARIO) {
                throw new BusinessValidationException(
                        String.format(Mensajes.COMENTARIO_MUY_LARGO, Limites.MAX_LONGITUD_COMENTARIO)
                );
            }
        }

        // 7. Crear y guardar
        Review review = modelMapper.map(reviewDTO, Review.class);
        review.setFechaResena(new Date());
        review.setEstado(Estados.ACTIVA);
        review.setTipoResenaId(tipoResena.getId());

        Review saved = reviewRepository.save(review);
        log.info("Reseña creada exitosamente con ID: {}", saved.getId());

        return convertToDTO(saved, true);
    }

    /**
     * Validaciones específicas para reseñas de propiedades.
     */
    private void validarResenaPropiedad(ReviewDTO reviewDTO, UsuarioDTO usuario) {
        // Validar que la propiedad existe
        if (!propertyServiceClient.existsProperty(reviewDTO.getPropiedadId())) {
            throw new BusinessValidationException(
                    String.format(Mensajes.PROPIEDAD_NO_EXISTE, reviewDTO.getPropiedadId())
            );
        }

        // Validar que no sea reseña duplicada
        if (reviewRepository.existsByUsuarioIdAndPropiedadId(
                reviewDTO.getUsuarioId(), reviewDTO.getPropiedadId())) {
            throw new BusinessValidationException(Mensajes.RESENA_DUPLICADA);
        }

        // Validar que el propietario no reseñe su propia propiedad
        if (propertyServiceClient.isPropertyOwner(reviewDTO.getPropiedadId(), reviewDTO.getUsuarioId())) {
            throw new BusinessValidationException(Mensajes.USUARIO_NO_PUEDE_RESENAR_PROPIA_PROPIEDAD);
        }
    }

    /**
     * Validaciones específicas para reseñas de usuarios.
     */
    private void validarResenaUsuario(ReviewDTO reviewDTO) {
        // Validar que el usuario reseñado existe
        if (!userServiceClient.existsUser(reviewDTO.getUsuarioResenadoId())) {
            throw new BusinessValidationException(
                    String.format(Mensajes.USUARIO_NO_EXISTE, reviewDTO.getUsuarioResenadoId())
            );
        }

        // Validar que no sea reseña duplicada
        if (reviewRepository.existsByUsuarioIdAndUsuarioResenadoId(
                reviewDTO.getUsuarioId(), reviewDTO.getUsuarioResenadoId())) {
            throw new BusinessValidationException(Mensajes.RESENA_DUPLICADA);
        }

        // Validar que no se reseñe a sí mismo
        if (reviewDTO.getUsuarioId().equals(reviewDTO.getUsuarioResenadoId())) {
            throw new BusinessValidationException("Un usuario no puede reseñarse a sí mismo");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> listarTodas(boolean includeDetails) {
        log.debug("Listando todas las reseñas (includeDetails: {})", Boolean.valueOf(includeDetails));
        return reviewRepository.findAll().stream()
                .map(r -> convertToDTO(r, includeDetails))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO obtenerPorId(Long id, boolean includeDetails) {
        log.debug("Obteniendo reseña con ID: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.RESENA_NO_ENCONTRADA, id)
                ));
        return convertToDTO(review, includeDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> obtenerPorUsuario(Long usuarioId, boolean includeDetails) {
        log.debug("Obteniendo reseñas del usuario {}", usuarioId);
        return reviewRepository.findByUsuarioId(usuarioId).stream()
                .map(r -> convertToDTO(r, includeDetails))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> obtenerPorPropiedad(Long propiedadId, boolean includeDetails) {
        log.debug("Obteniendo reseñas de la propiedad {}", propiedadId);
        return reviewRepository.findByPropiedadId(propiedadId).stream()
                .map(r -> convertToDTO(r, includeDetails))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> obtenerPorUsuarioResenado(Long usuarioResenadoId, boolean includeDetails) {
        log.debug("Obteniendo reseñas sobre el usuario {}", usuarioResenadoId);
        return reviewRepository.findByUsuarioResenadoId(usuarioResenadoId).stream()
                .map(r -> convertToDTO(r, includeDetails))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioPorPropiedad(Long propiedadId) {
        log.debug("Calculando promedio de reseñas para propiedad {}", propiedadId);
        Double promedio = reviewRepository.calcularPromedioPuntajePorPropiedad(propiedadId);
        return promedio != null ? promedio : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularPromedioPorUsuario(Long usuarioResenadoId) {
        log.debug("Calculando promedio de reseñas para usuario {}", usuarioResenadoId);
        Double promedio = reviewRepository.calcularPromedioPuntajePorUsuario(usuarioResenadoId);
        return promedio != null ? promedio : 0.0;
    }

    @Override
    @Transactional
    public ReviewDTO actualizarEstado(Long id, String nuevoEstado) {
        log.info("Actualizando estado de reseña {} a {}", id, nuevoEstado);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.RESENA_NO_ENCONTRADA, id)
                ));

        String estadoUpper = nuevoEstado.toUpperCase();
        if (!Estados.esValido(estadoUpper)) {
            throw new BusinessValidationException(
                    "Estado inválido. Los estados válidos son: ACTIVA, BANEADA, OCULTA"
            );
        }

        review.setEstado(estadoUpper);
        if (Estados.BANEADA.equals(estadoUpper) && review.getFechaBaneo() == null) {
            review.setFechaBaneo(new Date());
        }

        Review updated = reviewRepository.save(review);
        return convertToDTO(updated, true);
    }

    @Override
    @Transactional
    public void eliminarResena(Long id) {
        log.info("Eliminando reseña con ID: {}", id);

        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format(Mensajes.RESENA_NO_ENCONTRADA, id)
            );
        }

        reviewRepository.deleteById(id);
        log.info("Reseña {} eliminada exitosamente", id);
    }

    /**
     * Convierte una entidad Review a ReviewDTO con o sin detalles.
     */
    private ReviewDTO convertToDTO(Review review, boolean includeDetails) {
        ReviewDTO dto = modelMapper.map(review, ReviewDTO.class);

        // Obtener nombre del tipo de reseña
        try {
            tipoResenaRepository.findById(review.getTipoResenaId())
                    .ifPresent(tipo -> dto.setTipoResenaNombre(tipo.getNombre()));
        } catch (Exception e) {
            log.warn("No se pudo obtener tipo de reseña {}: {}", review.getTipoResenaId(), e.getMessage());
        }

        if (includeDetails) {
            // Obtener información del usuario que creó la reseña
            try {
                UsuarioDTO usuario = userServiceClient.getUserById(review.getUsuarioId());
                dto.setUsuario(usuario);
            } catch (Exception e) {
                log.warn("No se pudo obtener información del usuario {}: {}",
                        review.getUsuarioId(), e.getMessage());
            }

            // Obtener información de la propiedad si existe
            if (review.getPropiedadId() != null) {
                try {
                    PropiedadDTO propiedad = propertyServiceClient.getPropertyById(review.getPropiedadId());
                    dto.setPropiedad(propiedad);
                } catch (Exception e) {
                    log.warn("No se pudo obtener información de la propiedad {}: {}",
                            review.getPropiedadId(), e.getMessage());
                }
            }

            // Obtener información del usuario reseñado si existe
            if (review.getUsuarioResenadoId() != null) {
                try {
                    UsuarioDTO usuarioResenado = userServiceClient.getUserById(review.getUsuarioResenadoId());
                    dto.setUsuarioResenado(usuarioResenado);
                } catch (Exception e) {
                    log.warn("No se pudo obtener información del usuario reseñado {}: {}",
                            review.getUsuarioResenadoId(), e.getMessage());
                }
            }
        }

        return dto;
    }
}