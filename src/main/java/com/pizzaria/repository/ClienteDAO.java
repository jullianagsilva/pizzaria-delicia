
package com.pizzaria.repository;

import com.pizzaria.model.Clientes;
import com.pizzaria.model.Pedido;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class ClienteDAO {

    private final String url = System.getenv("DB_URL");
    private final String user = System.getenv("DB_USERNAME");
    private final String password = System.getenv("DB_PASSWORD");

    public ClienteDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "O driver JDBC do MySQL não está disponível.", e
            );
        }
    }

    // Mantém aqui os restantes métodos existentes da classe.
    // ================== CRUD ==================
    public boolean salvar(Clientes cliente) {
        String sql = "INSERT INTO clientes (nome, telefone, morada) VALUES (?, ?, ?)";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, cliente.getNome());
            pst.setString(2, cliente.getTelefone());
            pst.setString(3, cliente.getMorada());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) return false;

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt(1));
                } else return false;
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizar(Clientes cliente) {
        String sql = "UPDATE clientes SET nome = ?, telefone = ?, morada = ? WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, cliente.getNome());
            pst.setString(2, cliente.getTelefone());
            pst.setString(3, cliente.getMorada());
            pst.setInt(4, cliente.getId());

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizarMorada(int clienteId, String novaMorada) {
        String sql = "UPDATE clientes SET morada = ? WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, novaMorada);
            pst.setInt(2, clienteId);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Clientes> buscarTodos() {
        List<Clientes> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Clientes cliente = new Clientes();
                cliente.setId(rs.getInt("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setMorada(rs.getString("morada"));
                lista.add(cliente);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Clientes buscarPorNomeTelefone(String nome, String telefone) {
        String sql = "SELECT * FROM clientes WHERE nome = ? AND telefone = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, nome);
            pst.setString(2, telefone);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Clientes cliente = new Clientes();
                    cliente.setId(rs.getInt("id"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setMorada(rs.getString("morada"));
                    return cliente;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Clientes buscarPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Clientes cliente = new Clientes();
                    cliente.setId(rs.getInt("id"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setMorada(rs.getString("morada"));
                    return cliente;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================== BUSCAS COMPLEMENTARES ==================
    public List<Clientes> buscarClientesPorNomeTelefoneParcial(String nome, String telefone) {
        List<Clientes> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE nome LIKE ? OR telefone LIKE ?";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + nome + "%");
            pst.setString(2, "%" + telefone + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Clientes cliente = new Clientes();
                    cliente.setId(rs.getInt("id"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setMorada(rs.getString("morada"));
                    lista.add(cliente);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Clientes> buscarClientesPorPedidos(List<Pedido> pedidos) {
        Set<Clientes> clientesSet = new HashSet<>();
        for (Pedido pedido : pedidos) {
            if (pedido.getCliente() != null) {
                clientesSet.add(pedido.getCliente());
            }
        }
        return new ArrayList<>(clientesSet);
    }

    // ================== RELATÓRIOS ==================
    public List<String> clientesMaisFrequentes(int topN) {
        String sql = "SELECT c.nome, COUNT(p.id) AS total_pedidos " +
                     "FROM clientes c " +
                     "LEFT JOIN pedido p ON c.id = p.cliente_id " +
                     "GROUP BY c.id, c.nome " +
                     "ORDER BY total_pedidos DESC " +
                     "LIMIT ?";

        List<String> resultado = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, topN);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String clienteInfo = rs.getString("nome") + " - Pedidos: " + rs.getInt("total_pedidos");
                resultado.add(clienteInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public List<String> clientesSemPedidos() {
        String sql = "SELECT c.nome, c.telefone FROM clientes c " +
                     "LEFT JOIN pedido p ON c.id = p.cliente_id " +
                     "WHERE p.id IS NULL";

        List<String> resultado = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                resultado.add(rs.getString("nome") + " - Telefone: " + rs.getString("telefone"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    public List<String> mediaGastoPorCliente() {
        String sql = "SELECT c.nome, AVG(p.total) AS media_gasto " +
                     "FROM clientes c " +
                     "JOIN pedido p ON c.id = p.cliente_id " +
                     "GROUP BY c.id, c.nome " +
                     "ORDER BY media_gasto DESC";

        List<String> resultado = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String linha = String.format("%s - Média Gasto: € %.2f", rs.getString("nome"), rs.getDouble("media_gasto"));
                resultado.add(linha);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
