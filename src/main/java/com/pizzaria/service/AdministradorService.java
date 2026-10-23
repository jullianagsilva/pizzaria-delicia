package com.pizzaria.service;

import com.pizzaria.model.Administradores;
import com.pizzaria.repository.AdministradorDAO;
import com.pizzaria.util.HashUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdministradorService {

    private final AdministradorDAO administradorDAO;

    public AdministradorService(AdministradorDAO administradorDAO) {
        this.administradorDAO = administradorDAO;
    }

    public Optional<Administradores> autenticar(String usuario, String senha) {
        if (usuario == null || usuario.isBlank() || senha == null || senha.isBlank()) {
            return Optional.empty();
        }

        // Busca o administrador pelo usuário
        Optional<Administradores> adminOpt = administradorDAO.buscarPorUsuario(usuario);

        // Valida senha
        return adminOpt.filter(admin -> {
            String senhaHashCalculada = HashUtil.gerarHashSHA256(admin.getSalt() + senha);
            return admin.getSenhaHash().equals(senhaHashCalculada);
        });
    }
}
