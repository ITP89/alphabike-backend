package com.alphabike.backend.usuario.dto;

import com.alphabike.backend.shared.validation.ValidEnum;
import com.alphabike.backend.usuario.Usuario;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email
    private String email;

    private String password;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    @ValidEnum(enumClass = Usuario.Rol.class, message = "El rol no es valido")
    private String rol;
}
