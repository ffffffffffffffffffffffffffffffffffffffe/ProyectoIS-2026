package com.example.demo.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CrearUsuarioForm {

    @NotBlank(message = "Debes ingresar un nombre.")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String nombre;

    @NotBlank(message = "Debes ingresar un correo.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 150, message = "El correo admite hasta 150 caracteres.")
    private String correo;

    @NotBlank(message = "Debes ingresar una contraseña.")
    @Size(min = 8, max = 72,
            message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String password;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
