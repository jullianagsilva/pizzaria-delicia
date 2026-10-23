package com.pizzaria.service;

import com.pizzaria.model.Clientes;
import com.pizzaria.model.Administradores;
import com.pizzaria.repository.AdministradorDAO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final ClienteService clienteService;
    private final AdministradorDAO adminDAO;

    public LoginService(ClienteService clienteService, AdministradorDAO adminDAO) {
        this.clienteService = clienteService;
        this.adminDAO = adminDAO;
    }

    // Autentica administrador
    public Optional<Administradores> autenticarAdmin(String usuario, String senhaDigitada) {
        if (usuario == null || usuario.isBlank() || senhaDigitada == null || senhaDigitada.isBlank())
            return Optional.empty();

        Optional<Administradores> adminOpt = adminDAO.buscarPorUsuario(usuario.trim());
        if (adminOpt.isEmpty()) return Optional.empty();

        Administradores admin = adminOpt.get();
        String hashDigitada = com.pizzaria.util.HashUtil.gerarHashSHA256(admin.getSalt() + senhaDigitada.trim());

        return hashDigitada.equals(admin.getSenhaHash()) ? Optional.of(admin) : Optional.empty();
    }

    // Autentica cliente ou cria se não existir
    public Optional<Clientes> autenticarOuCadastrarCliente(String nome, String telefone) {
        if (nome == null || nome.isBlank() || telefone == null || telefone.isBlank()) {
            return Optional.empty();
        }

        try {
            Clientes cliente = clienteService.buscarOuCriarCliente(nome, telefone);
            return Optional.of(cliente);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
