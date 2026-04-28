package com.rentify.applicationService.dto.external;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String rol;
    private Boolean activo;
}