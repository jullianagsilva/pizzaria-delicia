package com.pizzaria.repository;

import com.pizzaria.config.ConexaoBD;
import com.pizzaria.dto.ClienteFrequenteDTO;
import com.pizzaria.dto.PizzaMaisVendidaDTO;
import com.pizzaria.dto.FaturacaoDiariaDTO;
import com.pizzaria.model.Pedido;
import com.pizzaria.model.Pizza;
import com.pizzaria.model.Carrinho;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PedidoDAO {

    private final PizzaDAO pizzaDAO;
    private final ClienteDAO clienteDAO;

    @Autowired
    public PedidoDAO(PizzaDAO pizzaDAO, ClienteDAO clienteDAO) {
        this.pizzaDAO = pizzaDAO;
        this.clienteDAO = clienteDAO;
    }

    // ===================== SALVAR PEDIDO =====================
    public void salvarPedido(Pedido pedido) {
        String sql = "INSERT INTO Pedido (cliente_id, data_pedido, observacoes, numero_pedido, morada) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pedido.getCliente().getId());
            stmt.setTimestamp(2, Timestamp.valueOf(pedido.getDataPedido()));
            stmt.setString(3, pedido.getObservacoes());
            stmt.setInt(4, pedido.getNumeroPedido());
            stmt.setString(5, pedido.getMoradaEntrega());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int pedidoId = rs.getInt(1);
                    for (var pizza : pedido.getPizzas()) {
                        pizzaDAO.inserirPizza(pizza, pedidoId);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pedido: " + e.getMessage(), e);
        }
    }

    // ===================== BUSCAR TODOS =====================
    public List<Pedido> buscarTodos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM Pedido";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pedidos.add(mapearPedido(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedidos: " + e.getMessage(), e);
        }

        return pedidos;
    }

    // ===================== BUSCAR POR ID =====================
    public Pedido buscarPorId(int id) {
        String sql = "SELECT * FROM Pedido WHERE id = ?";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido por ID: " + e.getMessage(), e);
        }

        return null;
    }

    // ===================== BUSCAR POR NÚMERO =====================
    public Pedido buscarPedidoPorNumero(int numeroPedido) {
        String sql = "SELECT * FROM Pedido WHERE numero_pedido = ?";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numeroPedido);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido por número: " + e.getMessage(), e);
        }

        return null;
    }

    // ===================== PEDIDOS POR CLIENTE =====================
    public List<Pedido> getPedidosPorCliente(int clienteId) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM Pedido WHERE cliente_id = ? ORDER BY data_pedido DESC";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapearPedido(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedidos do cliente: " + e.getMessage(), e);
        }

        return pedidos;
    }

    // ===================== FILTRAR PEDIDOS =====================
    public List<Pedido> getPedidosFiltrados(String nome, String telefone, String dataInicio, String dataFim) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.* FROM Pedido p " +
                     "JOIN Clientes c ON p.cliente_id = c.id " +
                     "WHERE c.nome LIKE ? AND c.telefone LIKE ?";

        if (dataInicio != null && !dataInicio.isEmpty()) {
            sql += " AND p.data_pedido >= ?";
        }
        if (dataFim != null && !dataFim.isEmpty()) {
            sql += " AND p.data_pedido <= ?";
        }

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            stmt.setString(2, "%" + telefone + "%");

            int index = 3;
            if (dataInicio != null && !dataInicio.isEmpty()) {
                stmt.setDate(index++, Date.valueOf(LocalDate.parse(dataInicio)));
            }
            if (dataFim != null && !dataFim.isEmpty()) {
                stmt.setDate(index, Date.valueOf(LocalDate.parse(dataFim)));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapearPedido(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao filtrar pedidos: " + e.getMessage(), e);
        }

        return pedidos;
    }

    // ===================== RELATÓRIOS =====================

    // Clientes mais frequentes
    public List<ClienteFrequenteDTO> buscarClientesMaisFrequentes() {
        List<ClienteFrequenteDTO> lista = new ArrayList<>();
        String sql = """
            SELECT c.id, c.nome, COUNT(p.id) AS totalPedidos, 
                   SUM(pp.quantidade * CASE pp.tamanho
                        WHEN 'Pequena' THEN 15
                        WHEN 'Média' THEN 20
                        WHEN 'Grande' THEN 25
                        ELSE 0 END) AS totalGasto
            FROM Pedido p
            JOIN Clientes c ON p.cliente_id = c.id
            JOIN pedidopizza pp ON pp.pedido_id = p.id
            GROUP BY c.id, c.nome
            ORDER BY totalPedidos DESC
            LIMIT 10
        """;

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new ClienteFrequenteDTO(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("totalPedidos"),
                        rs.getDouble("totalGasto")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar clientes mais frequentes", e);
        }

        return lista;
    }

    // Pizzas mais vendidas
    public List<PizzaMaisVendidaDTO> buscarPizzasMaisVendidas() {
        List<PizzaMaisVendidaDTO> lista = new ArrayList<>();
        String sql = """
            SELECT sabor, tamanho, COUNT(*) AS totalVendidas
            FROM pedidopizza
            GROUP BY sabor, tamanho
            ORDER BY totalVendidas DESC
            LIMIT 10
        """;

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String sabor = rs.getString("sabor");
                String tamanho = rs.getString("tamanho");
                int quantidade = rs.getInt("totalVendidas");

                double totalVendas = calcularPreco(tamanho) * quantidade;

                lista.add(new PizzaMaisVendidaDTO(
                        sabor,
                        tamanho,
                        quantidade,
                        totalVendas
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pizzas mais vendidas", e);
        }

        return lista;
    }

    // Faturação últimos 30 dias
    public List<FaturacaoDiariaDTO> buscarFaturacaoUltimos30Dias() {
        List<FaturacaoDiariaDTO> lista = new ArrayList<>();
        String sql = """
            SELECT DATE(p.data_pedido) AS dia, 
                   SUM(pp.quantidade * CASE pp.tamanho
                        WHEN 'Pequena' THEN 15
                        WHEN 'Média' THEN 20
                        WHEN 'Grande' THEN 25
                        ELSE 0 END) AS faturacao
            FROM Pedido p
            JOIN pedidopizza pp ON pp.pedido_id = p.id
            WHERE p.data_pedido >= CURDATE() - INTERVAL 30 DAY
            GROUP BY dia
            ORDER BY dia ASC
        """;

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new FaturacaoDiariaDTO(
                        rs.getDate("dia").toLocalDate(),
                        rs.getDouble("faturacao")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar faturação dos últimos 30 dias", e);
        }

        return lista;
    }

    // ===================== MÉTODO AUXILIAR PARA MAPEAR PEDIDO =====================
    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setNumeroPedido(rs.getInt("numero_pedido"));
        pedido.setDataPedido(rs.getTimestamp("data_pedido").toLocalDateTime());
        pedido.setObservacoes(rs.getString("observacoes"));
        pedido.setMoradaEntrega(rs.getString("morada"));

        int clienteId = rs.getInt("cliente_id");
        pedido.setCliente(clienteDAO.buscarPorId(clienteId));

        if (pedido.getCarrinho() == null) {
            pedido.setCarrinho(new Carrinho());
        }

        List<Pizza> pizzas = pizzaDAO.getPizzasPorPedido(pedido.getId());
        pedido.setPizzas((pizzas != null) ? pizzas : new ArrayList<>());

        return pedido;
    }

    // ===================== AUXILIAR CALCULAR PREÇO =====================
    private double calcularPreco(String tamanho) {
        return switch (tamanho) {
            case "Pequena" -> 15.0;
            case "Média"   -> 20.0;
            case "Grande"  -> 25.0;
            default -> 0.0;
        };
    }
}
