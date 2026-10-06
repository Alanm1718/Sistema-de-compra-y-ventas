package com.tomorrow.Negocio.LoginCliente;


import jakarta.validation.constraints.NotBlank;

public class ClienteLoginRequest {

    @NotBlank (message = "El usuario es obligatori")
    private String username;

    @NotBlank (message = "La contraseña es obligatoria")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


