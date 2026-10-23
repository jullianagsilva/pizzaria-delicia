package com.pizzaria.security;

import com.pizzaria.model.Administradores;
import com.pizzaria.model.Clientes;

public class Sessao {

    private static Clientes clienteLogado = null;
    private static Administradores adminLogado = null;

    // Construtor privado para impedir instanciação
    private Sessao() {}

    // Métodos para Cliente
    public static void logarCliente(Clientes cliente) {
        clienteLogado = cliente;
        adminLogado = null;
    }

    public static Clientes getClienteLogado() {
        return clienteLogado;
    }

    public static boolean isClienteLogado() {
        return clienteLogado != null;
    }

    // Métodos para Administrador
    public static void logarAdmin(Administradores admin) {
        adminLogado = admin;
        clienteLogado = null;
    }

    public static Administradores getAdminLogado() {
        return adminLogado;
    }

    public static boolean isAdminLogado() {
        return adminLogado != null;
    }

    // Logout geral
    public static void logout() {
        clienteLogado = null;
        adminLogado = null;
    }

    // Verifica se algum usuário está logado
    public static boolean isLogado() {
        return isClienteLogado() || isAdminLogado();
    }

    // Retorna o tipo do usuário logado
    public static String getTipoUsuario() {
        if (isClienteLogado()) return "cliente";
        if (isAdminLogado()) return "administrador";
        return "nenhum";
    }

    public static void encerrarSessao() {
        logout();
    }
}
