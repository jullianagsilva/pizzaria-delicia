package com.pizzaria.repository;

import com.pizzaria.config.ConexaoBD;
import com.pizzaria.model.Pagamento;
import java.sql.*;
import org.springframework.stereotype.Repository;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class PagamentoDAO {

    private static final Logger LOGGER = Logger.getLogger(PagamentoDAO.class.getName());

    public boolean salvar(Pagamento pagamento) {
        String sql = "INSERT INTO pagamento (id_pedido, valor, status, data_pagamento) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pagamento.getIdPedido());
            stmt.setDouble(2, pagamento.getValor());
            stmt.setString(3, pagamento.getStatus());

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;

        } catch (SQLException e) {
            // Usar Logger para registrar erro ao invés de printStackTrace
            LOGGER.log(Level.SEVERE, "Erro ao salvar pagamento", e);
            return false;
        }
    }
}
