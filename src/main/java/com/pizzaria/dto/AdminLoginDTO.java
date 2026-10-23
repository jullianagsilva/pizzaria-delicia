
package com.pizzaria.dto;

public class AdminLoginDTO {
    private String usuario;
    private String senha;

    public AdminLoginDTO() {}

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
