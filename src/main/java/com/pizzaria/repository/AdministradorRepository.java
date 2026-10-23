package com.pizzaria.repository;

import com.pizzaria.model.Administradores;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administradores, Long> {
    Optional<Administradores> findByUsuario(String usuario);
}
