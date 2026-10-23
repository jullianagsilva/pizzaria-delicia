package com.pizzaria.service;

import com.pizzaria.model.Clientes;
import com.pizzaria.repository.ClienteDAO;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private static final String MORADA_DEFAULT = "N/A";
    private final ClienteDAO clienteDAO;

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    // Aceita qualquer telefone internacional (6 a 20 caracteres, números, +, -, espaço e parênteses)
    private boolean telefoneValido(String telefone) {
        return telefone != null && telefone.matches("[\\d+\\-\\s()]{6,20}");
    }

    private String normalizarNome(String nome) {
        return (nome != null) ? nome.trim() : "";
    }

    // Busca cliente existente ou cria um novo
    public Clientes buscarOuCriarCliente(String nome, String telefone) {
        nome = normalizarNome(nome);
        telefone = (telefone != null) ? telefone.trim() : "";

        if (nome.isEmpty() || !telefoneValido(telefone)) {
            throw new IllegalArgumentException("Nome ou telefone inválido");
        }

        // Tenta buscar no banco
        Clientes clienteExistente = clienteDAO.buscarPorNomeTelefone(nome, telefone);
        if (clienteExistente != null) {
            return clienteExistente; // retorna cliente existente
        }

        // Cria novo cliente se não existir
        Clientes novoCliente = new Clientes(nome, telefone, MORADA_DEFAULT);
        boolean sucesso = clienteDAO.salvar(novoCliente);
        if (!sucesso) {
            throw new IllegalArgumentException("Não foi possível salvar o cliente.");
        }
        return novoCliente;
    }
}
