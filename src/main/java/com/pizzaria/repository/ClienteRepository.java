package com.pizzaria.repository;

import com.pizzaria.model.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Clientes, Long> {
    Optional<Clientes> findByNomeAndTelefone(String nome, String telefone);
}

