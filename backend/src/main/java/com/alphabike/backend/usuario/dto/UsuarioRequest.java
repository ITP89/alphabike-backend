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
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String email;

    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    @ValidEnum(enumClass = Usuario.Rol.class, message = "El rol no es valido")
    private String rol;
}
