package com.rentify.userservice.controller;

import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.dto.UsuarioUpdateDTO;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.LoginResponseDTO;
import com.rentify.userservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestion de usuarios
 * Endpoints: POST /api/usuarios, GET /api/usuarios, GET /api/usuarios/{id}, etc.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestion de usuarios del sistema Rentify")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario
     * POST /api/usuarios
     */
    @PostMapping
    @Operation(summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario en el sistema. Solo mayores de 18 anos. " +
                    "Los usuarios con email @duoc.cl obtienen beneficio VIP (20% descuento)")
    public ResponseEntity<UsuarioDTO> registrarUsuario(
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO creado = usuarioService.registrarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Login de usuario
     * POST /api/usuarios/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login de usuario",
            description = "Autentica un usuario con email y contrasena")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDTO) {
        UsuarioDTO usuario = usuarioService.login(loginDTO);
        LoginResponseDTO response = LoginResponseDTO.builder()
                .mensaje("Login exitoso")
                .usuario(usuario)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los usuarios
     * GET /api/usuarios
     */
    @GetMapping
    @Operation(summary = "Listar todos los usuarios",
            description = "Obtiene la lista completa de usuarios registrados")
    public ResponseEntity<List<UsuarioDTO>> obtenerTodos(
            @Parameter(description = "Incluir detalles de rol y estado")
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(usuarioService.obtenerTodos(includeDetails));
    }

    /**
     * Obtiene un usuario por su ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID",
            description = "Obtiene un usuario especifico por su ID")
    public ResponseEntity<UsuarioDTO> obtenerPorId(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id, includeDetails));
    }

    /**
     * Obtiene un usuario por su email
     * GET /api/usuarios/email/{email}
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por email",
            description = "Obtiene un usuario especifico por su correo electronico")
    public ResponseEntity<UsuarioDTO> obtenerPorEmail(
            @Parameter(description = "Email del usuario", example = "juan.perez@email.com")
            @PathVariable String email,
            @RequestParam(defaultValue = "true") boolean includeDetails) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(email, includeDetails));
    }

    /**
     * Obtiene usuarios por rol
     * GET /api/usuarios/rol/{rolId}
     */
    @GetMapping("/rol/{rolId}")
    @Operation(summary = "Obtener usuarios por rol",
            description = "Obtiene todos los usuarios con un rol especifico")
    public ResponseEntity<List<UsuarioDTO>> obtenerPorRol(
            @Parameter(description = "ID del rol (1=ADMIN, 2=PROPIETARIO, 3=ARRIENDATARIO)", example = "3")
            @PathVariable Long rolId,
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(usuarioService.obtenerPorRol(rolId, includeDetails));
    }

    /**
     * Obtiene usuarios VIP de DUOC
     * GET /api/usuarios/vip
     */
    @GetMapping("/vip")
    @Operation(summary = "Obtener usuarios DUOC VIP",
            description = "Obtiene todos los usuarios con beneficio DUOC (20% descuento)")
    public ResponseEntity<List<UsuarioDTO>> obtenerUsuariosVIP(
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosVIP(includeDetails));
    }

    /**
     * Actualiza los datos de un usuario (version admin - campos parciales)
     * PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario",
            description = "Actualiza los datos de un usuario. Permite cambiar nombre, email, telefono, rol y estado.")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO updateDTO) {
        return ResponseEntity.ok(usuarioService.actualizarUsuarioAdmin(id, updateDTO));
    }

    /**
     * Cambia el rol de un usuario
     * PATCH /api/usuarios/{id}/rol
     */
    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar rol de usuario",
            description = "Cambia el rol asignado a un usuario")
    public ResponseEntity<UsuarioDTO> cambiarRol(
            @PathVariable Long id,
            @Parameter(description = "ID del nuevo rol", example = "2")
            @RequestParam Long rolId) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, rolId));
    }

    /**
     * Cambia el estado de un usuario
     * PATCH /api/usuarios/{id}/estado
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de usuario",
            description = "Cambia el estado de un usuario (1=ACTIVO, 2=INACTIVO, 3=SUSPENDIDO)")
    public ResponseEntity<UsuarioDTO> cambiarEstado(
            @PathVariable Long id,
            @Parameter(description = "ID del nuevo estado", example = "1")
            @RequestParam Long estadoId) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, estadoId));
    }

    /**
     * Agrega puntos RentifyPoints a un usuario
     * PATCH /api/usuarios/{id}/puntos
     */
    @PatchMapping("/{id}/puntos")
    @Operation(summary = "Agregar puntos RentifyPoints",
            description = "Agrega puntos al programa de fidelizacion del usuario")
    public ResponseEntity<UsuarioDTO> agregarPuntos(
            @PathVariable Long id,
            @Parameter(description = "Cantidad de puntos a agregar", example = "100")
            @RequestParam Integer puntos) {
        return ResponseEntity.ok(usuarioService.agregarPuntos(id, puntos));
    }

    /**
     * Verifica si un usuario existe
     * GET /api/usuarios/{id}/exists
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar si usuario existe",
            description = "Verifica si existe un usuario con el ID especificado")
    public ResponseEntity<Boolean> existeUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.existeUsuario(id));
    }
}