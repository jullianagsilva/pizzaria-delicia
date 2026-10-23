package com.pizzaria.service;

import com.pizzaria.model.Pedido;
import com.pizzaria.model.ExportadorPedido;
import com.pizzaria.repository.PedidoDAO;
import com.pizzaria.dto.ClienteFrequenteDTO;
import com.pizzaria.dto.PizzaMaisVendidaDTO;
import com.pizzaria.dto.FaturacaoDiariaDTO;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class PedidoService {

    private final PedidoDAO pedidoDAO;

    public PedidoService(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    // =========================
    // Salvar pedido e exportar
    // =========================
    public void salvarPedido(Pedido pedido) {
        executarComTratamentoVoid(() -> {
            pedidoDAO.salvarPedido(pedido);

            // Exportação não bloqueia o salvamento
            try {
                ExportadorPedido.exportar(pedido);
            } catch (Exception e) {
                System.err.println("❌ Erro ao exportar pedido: " + e.getMessage());
            }

        }, "Erro ao salvar pedido no banco de dados");
    }

    // =========================
    // Buscar pedido por ID
    // =========================
    public Pedido buscarPorId(int id) {
        return executarComTratamento(() -> pedidoDAO.buscarPorId(id),
                "Erro ao buscar pedido por ID");
    }

    // =========================
    // Métodos genéricos para lidar com exceções
    // =========================
    private <T> T executarComTratamento(Supplier<T> acao, String mensagemErro) {
        try {
            return acao.get();
        } catch (Exception e) {
            throw new RuntimeException(mensagemErro, e);
        }
    }

    private void executarComTratamentoVoid(Runnable acao, String mensagemErro) {
        try {
            acao.run();
        } catch (Exception e) {
            throw new RuntimeException(mensagemErro, e);
        }
    }

    // =========================
    // Consultas e relatórios
    // =========================
    public List<Pedido> buscarTodos() {
        return executarComTratamento(pedidoDAO::buscarTodos, "Erro ao buscar pedidos");
    }

    public List<Pedido> getHistoricoPedidos() {
        return buscarTodos();
    }

    public Pedido buscarPedidoPorNumero(int numeroPedido) {
        return executarComTratamento(() -> pedidoDAO.buscarPedidoPorNumero(numeroPedido),
                "Erro ao buscar pedido por número");
    }

    public List<Pedido> getPedidosFiltrados(String nome, String telefone, String dataInicio, String dataFim) {
        return executarComTratamento(() -> pedidoDAO.getPedidosFiltrados(nome, telefone, dataInicio, dataFim),
                "Erro ao filtrar pedidos");
    }

    public List<Pedido> getPedidosPorCliente(int clienteId) {
        return executarComTratamento(() -> pedidoDAO.getPedidosPorCliente(clienteId),
                "Erro ao buscar pedidos do cliente");
    }

    public List<Pedido> getHistoricoPedidosPorCliente(int clienteId) {
        return getPedidosPorCliente(clienteId);
    }

    // =========================
    // Relatórios (ADMIN) com DTOs
    // =========================

    // Clientes mais frequentes
    public List<ClienteFrequenteDTO> buscarClientesMaisFrequentes() {
        return executarComTratamento(pedidoDAO::buscarClientesMaisFrequentes,
                "Erro ao buscar clientes mais frequentes");
    }

    // Pizzas mais vendidas
    public List<PizzaMaisVendidaDTO> buscarPizzasMaisVendidas() {
        List<PizzaMaisVendidaDTO> pizzas = executarComTratamento(
                pedidoDAO::buscarPizzasMaisVendidas,
                "Erro ao buscar pizzas mais vendidas"
        );

        // Calcula totalVendas em euros (€) multiplicando quantidade pelo preço
        return pizzas.stream()
                .map(p -> new PizzaMaisVendidaDTO(
                        p.getSabor(),
                        p.getTamanho(),
                        p.getQuantidadeVendida(),
                        calcularPreco(p.getTamanho()) * p.getQuantidadeVendida()
                ))
                .toList();
    }

    // Faturação dos últimos 30 dias
    public List<FaturacaoDiariaDTO> buscarFaturacaoUltimos30Dias() {
        return executarComTratamento(pedidoDAO::buscarFaturacaoUltimos30Dias,
                "Erro ao buscar faturação dos últimos 30 dias");
    }

    // =========================
    // Método auxiliar para calcular preço da pizza
    // =========================
    private double calcularPreco(String tamanho) {
        return switch (tamanho) {
            case "Pequena" -> 15.0;
            case "Média"   -> 20.0;
            case "Grande"  -> 25.0;
            default -> 0.0;
        };
    }
}
