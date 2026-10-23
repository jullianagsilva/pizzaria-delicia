package com.pizzaria.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Relatorio {
    private List<Clientes> clientes;
    private List<Pedido> pedidos;

    public Relatorio(List<Clientes> clientes, List<Pedido> pedidos) {
        this.clientes = clientes;
        this.pedidos = pedidos;
    }

    // 1. Clientes com mais pedidos (top N)
    public List<String> clientesMaisFrequentes(int topN) {
        return pedidos.stream()
                .collect(Collectors.groupingBy(p -> p.getCliente().getNome(), Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(e -> e.getKey() + " - " + e.getValue() + " pedidos")
                .collect(Collectors.toList());
    }

    // 2. Pizzas mais vendidas
    public List<String> pizzasMaisVendidas() {
        return pedidos.stream()
                .flatMap(p -> p.getPizzas().stream())
                .collect(Collectors.groupingBy(p -> p.getSabor() + " - " + p.getTamanho(), Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> e.getKey() + ": " + e.getValue() + " unidades")
                .collect(Collectors.toList());
    }

    // 3. Faturação por dia (últimos X dias)
    public List<String> faturacaoPorDia(int dias) {
        LocalDate limite = LocalDate.now().minusDays(dias);
        return pedidos.stream()
                .filter(p -> p.getData().isAfter(limite))
                .collect(Collectors.groupingBy(Pedido::getData, Collectors.summingDouble(Pedido::getTotal)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ": R$ " + String.format("%.2f", e.getValue()))
                .collect(Collectors.toList());
    }

    // 4. Pedidos recentes (últimos X dias)
    public List<String> pedidosRecentes(int dias) {
        LocalDate limite = LocalDate.now().minusDays(dias);
        return pedidos.stream()
                .filter(p -> p.getData().isAfter(limite))
                .sorted(Comparator.comparing(Pedido::getData).reversed())
                .map(p -> "Pedido #" + p.getNumeroPedido() + " - " + p.getCliente().getNome() +
                        " - " + p.getData() + " - Total: R$ " + String.format("%.2f", p.getTotal()))
                .collect(Collectors.toList());
    }

    // 5. Pizzas de um pedido específico
    public List<String> pizzasDePedido(int numeroPedido) {
        return pedidos.stream()
                .filter(p -> p.getNumeroPedido() == numeroPedido)
                .findFirst()
                .map(Pedido::getPizzas)
                .orElse(Collections.emptyList())
                .stream()
                .map(Pizza::getDescricao)
                .collect(Collectors.toList());
    }

    // 6. Clientes sem pedidos
    public List<String> clientesSemPedidos() {
        Set<Integer> clientesComPedido = pedidos.stream()
                .map(p -> p.getCliente().getId())
                .collect(Collectors.toSet());

        return clientes.stream()
                .filter(c -> !clientesComPedido.contains(c.getId()))
                .map(Clientes::getNome)
                .collect(Collectors.toList());
    }

    // 7. Média de gasto por cliente
    public List<String> mediaGastoPorCliente() {
        Map<Clientes, DoubleSummaryStatistics> stats = pedidos.stream()
                .collect(Collectors.groupingBy(Pedido::getCliente,
                        Collectors.summarizingDouble(Pedido::getTotal)));

        return stats.entrySet().stream()
                .map(e -> e.getKey().getNome() + ": R$ " + String.format("%.2f", e.getValue().getAverage()))
                .collect(Collectors.toList());
    }
}
