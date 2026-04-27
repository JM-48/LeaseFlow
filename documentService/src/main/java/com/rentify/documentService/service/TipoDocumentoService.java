package com.rentify.documentService.service;

import com.rentify.documentService.dto.TipoDocumentoDTO;
import com.rentify.documentService.exception.ResourceNotFoundException;
import com.rentify.documentService.model.TipoDocumento;
import com.rentify.documentService.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.rentify.documentService.constants.DocumentConstants.Mensajes;

/**
 * Servicio para gestión de tipos de documentos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<TipoDocumentoDTO> listarTodos() {
        log.debug("Listando todos los tipos de documentos");
        return tipoDocumentoRepository.findAll().stream()
                .map(t -> modelMapper.map(t, TipoDocumentoDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoDocumentoDTO obtenerPorId(Long id) {
        log.debug("Obteniendo tipo de documento con ID: {}", id);
        TipoDocumento tipoDoc = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.TIPO_DOC_NO_ENCONTRADO, id)));
        return modelMapper.map(tipoDoc, TipoDocumentoDTO.class);
    }

    @Transactional
    public TipoDocumentoDTO crear(TipoDocumentoDTO tipoDocDTO) {
        log.info("Creando nuevo tipo de documento: {}", tipoDocDTO.getNombre());
        TipoDocumento tipoDoc = modelMapper.map(tipoDocDTO, TipoDocumento.class);
        TipoDocumento saved = tipoDocumentoRepository.save(tipoDoc);
        return modelMapper.map(saved, TipoDocumentoDTO.class);
    }

    @Transactional
    public TipoDocumentoDTO actualizar(Long id, TipoDocumentoDTO tipoDocDTO) {
        log.info("Actualizando tipo de documento con ID: {}", id);
        TipoDocumento tipoDoc = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.TIPO_DOC_NO_ENCONTRADO, id)));

        tipoDoc.setNombre(tipoDocDTO.getNombre());
        TipoDocumento updated = tipoDocumentoRepository.save(tipoDoc);
        return modelMapper.map(updated, TipoDocumentoDTO.class);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando tipo de documento con ID: {}", id);
        if (!tipoDocumentoRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format(Mensajes.TIPO_DOC_NO_ENCONTRADO, id));
        }
        tipoDocumentoRepository.deleteById(id);
    }
}