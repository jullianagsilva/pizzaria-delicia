package com.pizzaria.service;

import com.pizzaria.model.Administradores;
import com.pizzaria.repository.AdministradorRepository;
import com.pizzaria.util.HashUtil;
import com.pizzaria.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AdministradorRepository administradorRepository;

    public AuthService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    // Cadastrar novo administrador
    public boolean cadastrar(String usuario, String senha) {
        if (administradorRepository.findByUsuario(usuario).isPresent()) {
            return false; // usuário já existe
        }

        String salt = SecurityUtil.gerarSalt();
        String hash = HashUtil.gerarHashSHA256(salt + senha);

        Administradores admin = new Administradores();
        admin.setUsuario(usuario);
        admin.setSalt(salt);
        admin.setSenhaHash(hash);

        administradorRepository.save(admin);
        return true;
    }

    // Autenticar usuário
    public boolean autenticar(String usuario, String senha) {
        Optional<Administradores> adminOpt = administradorRepository.findByUsuario(usuario);

        if (adminOpt.isPresent()) {
            Administradores admin = adminOpt.get();
            String hashCalculado = HashUtil.gerarHashSHA256(admin.getSalt() + senha);
            return hashCalculado.equals(admin.getSenhaHash());
        }

        return false;
    }
}
