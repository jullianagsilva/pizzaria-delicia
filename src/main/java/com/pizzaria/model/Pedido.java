package com.pizzaria.model;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Pedido implements Comparable<Pedido>, Serializable {
    private static final long serialVersionUID = 1L;

    private int id; // ID do banco de dados
    private Clientes cliente;
    private Carrinho carrinho;
    private String observacoes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataPedido;

    private int numeroPedido;
    private Pagamento pagamento;
    private String moradaEntrega;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ================== CONSTRUTORES ==================
    public Pedido(Clientes cliente, Carrinho carrinho, String observacoes, LocalDateTime dataPedido) {
        this.cliente = cliente;
        this.carrinho = (carrinho != null) ? carrinho : new Carrinho();
        this.observacoes = (observacoes != null) ? observacoes.trim() : "";
        this.dataPedido = (dataPedido != null) ? dataPedido : LocalDateTime.now();
        this.numeroPedido = gerarNumeroPedidoAleatorio();
    }

    public Pedido(Clientes cliente, Carrinho carrinho, String observacoes) {
        this(cliente, carrinho, observacoes, LocalDateTime.now());
    }

    public Pedido() {
        this.carrinho = new Carrinho();
        this.dataPedido = LocalDateTime.now();
    }

    // ================== MÉTODOS AUXILIARES ==================
    private int gerarNumeroPedidoAleatorio() {
        Random rand = new Random();
        return rand.nextInt(9000) + 1000; // 1000 a 9999
    }

    // ================== GETTERS E SETTERS ==================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Clientes getCliente() {
        return cliente;
    }

    public void setCliente(Clientes cliente) {
        this.cliente = cliente;
    }

    public Carrinho getCarrinho() {
        if (carrinho == null) {
            carrinho = new Carrinho();
        }
        return carrinho;
    }

    public void setCarrinho(Carrinho carrinho) {
        this.carrinho = (carrinho != null) ? carrinho : new Carrinho();
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = (observacoes != null) ? observacoes.trim() : "";
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        if (dataPedido != null) {
            this.dataPedido = dataPedido;
        }
    }

    public int getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(int numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public String getMoradaEntrega() {
        return moradaEntrega;
    }

    public void setMoradaEntrega(String moradaEntrega) {
        this.moradaEntrega = moradaEntrega;
    }

    // ================== MÉTODOS DE PIZZAS ==================
    public List<Pizza> getPizzas() {
        if (carrinho == null || carrinho.getPizzas() == null) {
            return new ArrayList<>();
        }
        return carrinho.getPizzas();
    }

    public void setPizzas(List<Pizza> pizzas) {
        if (carrinho == null) {
            carrinho = new Carrinho();
        }
        carrinho.setPizzas((pizzas != null) ? pizzas : new ArrayList<>());
    }

    public double getTotal() {
        double soma = (carrinho != null) ? carrinho.calcularTotal() : 0.0;
        return soma + Carrinho.TAXA_ENTREGA;
    }

    // ================== MÉTODOS DE RESUMO ==================
    public String mostrarResumo() {
        StringBuilder sb = new StringBuilder();

        sb.append("Número do Pedido: ").append(numeroPedido).append("\n");
        sb.append("Data: ").append(dataPedido.format(formatter)).append("\n");
        sb.append("Cliente: ").append(cliente.getNome()).append("\n");
        sb.append("Telefone: ").append(cliente.getTelefone()).append("\n");
        sb.append("Morada do Cliente: ").append(cliente.getMorada()).append("\n");
        sb.append("Morada de Entrega: ").append(moradaEntrega != null ? moradaEntrega : "").append("\n");
        sb.append("Observações: ").append(observacoes != null ? observacoes : "").append("\n\n");

        sb.append("Itens do Pedido:\n");

        double subtotal = 0.0;
        if (carrinho != null && carrinho.getPizzas() != null) {
            for (Pizza pizza : carrinho.getPizzas()) {
                double totalPizza = pizza.getPreco() * pizza.getQuantidade();
                sb.append(String.format("%dx %s %s - R$ %.2f cada | Subtotal: R$ %.2f\n",
                        pizza.getQuantidade(),
                        pizza.getTamanho(),
                        pizza.getSabor(),
                        pizza.getPreco(),
                        totalPizza));
                subtotal += totalPizza;
            }
        }

        sb.append(String.format("\nSubtotal das Pizzas: R$ %.2f\n", subtotal));
        sb.append(String.format("Taxa de Entrega: R$ %.2f\n", Carrinho.TAXA_ENTREGA));

        double totalGeral = subtotal + Carrinho.TAXA_ENTREGA;
        sb.append(String.format("Total Geral: R$ %.2f\n", totalGeral));

        if (pagamento != null) {
            sb.append("\nPagamento: ").append(pagamento.getStatus());
            sb.append(" | Valor: R$ ").append(String.format("%.2f", pagamento.getValor()));
        }

        return sb.toString();
    }

    // ================== EXPORTAÇÃO ==================
    public void exportarParaTxt(String pastaDestino, String nomeCliente) {
        File pasta = new File(pastaDestino);
        if (!pasta.exists()) pasta.mkdirs();

        String dataFormatada = dataPedido.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivoCliente = nomeCliente.trim().replaceAll("[^a-zA-Z0-9]", "_");
        String nomeArquivo = "pedido_" + nomeArquivoCliente + "_" + dataFormatada + ".txt";

        File arquivo = new File(pasta, nomeArquivo);

        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivo))) {
            writer.println(mostrarResumo());
            System.out.println("Resumo exportado para: " + arquivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao exportar pedido: " + e.getMessage());
        }
    }

    public static void exportarPedidos(List<Pedido> pedidos, String pastaDestino) {
        for (Pedido pedido : pedidos) {
            pedido.exportarParaTxt(pastaDestino, pedido.getCliente().getNome());
        }
    }

    @Override
    public int compareTo(Pedido outro) {
        return this.dataPedido.compareTo(outro.getDataPedido());
    }

    // ================== MÉTODO AUXILIAR PARA RELATÓRIOS ==================
    public LocalDate getData() {
        return dataPedido.toLocalDate();
    }
}
