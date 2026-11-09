package com.alphabike.backend.proveedor.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String nombre;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String ruc;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String email;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String direccion;

    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String contactoPrincipal;
}
