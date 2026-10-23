package com.pizzaria.repository;

import com.pizzaria.config.ConexaoBD;
import com.pizzaria.model.Pizza;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PizzaDAO {

    // Inserir pizza vinculada a um pedido
    public void inserirPizza(Pizza pizza, int pedidoId) {
        String sql = "INSERT INTO pedidopizza (pedido_id, sabor, tamanho, quantidade, preco) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedidoId);
            stmt.setString(2, pizza.getSabor());
            stmt.setString(3, pizza.getTamanho());
            stmt.setInt(4, pizza.getQuantidade());
            stmt.setDouble(5, pizza.getPreco());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pizza: " + e.getMessage(), e);
        }
    }

    // Buscar pizzas de um pedido
    public List<Pizza> getPizzasPorPedido(int pedidoId) {
        List<Pizza> pizzas = new ArrayList<>(); // sempre inicializada

        String sql = "SELECT sabor, tamanho, quantidade, preco FROM pedidopizza WHERE pedido_id = ?";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedidoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pizza pizza = new Pizza();
                    pizza.setSabor(rs.getString("sabor"));
                    pizza.setTamanho(rs.getString("tamanho"));
                    pizza.setQuantidade(rs.getInt("quantidade"));
                    pizza.setPreco(rs.getDouble("preco"));
                    pizzas.add(pizza);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pizzas do pedido: " + e.getMessage(), e);
        }

        return pizzas; // nunca retorna null
    }
}
