package com.pizzaria.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ExportadorPedido {

    private static final DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter nomeArquivoFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Gera o conteúdo do pedido em formato de texto.
     */
    public static String gerarConteudo(Pedido pedido) {
        if (pedido == null) return "";

        StringBuilder conteudo = new StringBuilder();

        conteudo.append("==== PEDIDO DO CLIENTE ====\n");
        conteudo.append("Número do Pedido: ").append(pedido.getNumeroPedido()).append("\n");
        conteudo.append("Data: ").append(pedido.getDataPedido().format(dataFormatter)).append("\n");

        conteudo.append("\n--- Cliente ---\n");
        conteudo.append("Nome: ").append(pedido.getCliente().getNome()).append("\n");
        conteudo.append("Telefone: ").append(pedido.getCliente().getTelefone()).append("\n");
        conteudo.append("Morada: ").append(pedido.getCliente().getMorada()).append("\n");

        conteudo.append("\n--- Pizzas ---\n");

        double subtotal = 0.0;
        for (Pizza pizza : pedido.getPizzas()) {
            double totalPizza = pizza.getPreco() * pizza.getQuantidade();
            conteudo.append(String.format("%dx %s %s - %.2f € cada | Subtotal: %.2f €\n",
                    pizza.getQuantidade(),
                    pizza.getTamanho(),
                    pizza.getSabor(),
                    pizza.getPreco(),
                    totalPizza));
            subtotal += totalPizza;
        }

        conteudo.append("\nObservações: ").append(pedido.getObservacoes() != null ? pedido.getObservacoes() : "").append("\n");

        // Taxa de entrega fixa
        double taxaEntrega = Carrinho.TAXA_ENTREGA;
        conteudo.append(String.format("Taxa de Entrega: %.2f €\n", taxaEntrega));

        double totalGeral = subtotal + taxaEntrega;
        conteudo.append(String.format("Total: %.2f €\n", totalGeral));

        if (pedido.getPagamento() != null) {
            conteudo.append("\n--- Pagamento ---\n");
            conteudo.append("Status: ").append(pedido.getPagamento().getStatus()).append("\n");
            conteudo.append("Valor Pago: ").append(String.format("%.2f", pedido.getPagamento().getValor())).append(" €\n");
        }

        return conteudo.toString();
    }

    /**
     * Exporta o pedido para um arquivo TXT com nome seguro
     */
    public static void exportarParaArquivo(Pedido pedido) {
        if (pedido == null) return;

        String pastaDestino = "C:\\Users\\Julliana\\Projeto_Java_VSCODE\\PizzariaOnline\\exportados";
        File pasta = new File(pastaDestino);
        if (!pasta.exists()) pasta.mkdirs();

        String nomeCliente = pedido.getCliente().getNome().trim().replaceAll("[^a-zA-Z0-9]", "_");
        String dataHora = pedido.getDataPedido().format(nomeArquivoFormatter);
        String nomeArquivo = String.format("pedido_%s_%d_%s.txt", nomeCliente, pedido.getNumeroPedido(), dataHora);

        File arquivo = new File(pasta, nomeArquivo);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            writer.write(gerarConteudo(pedido));
            System.out.println("📦 Pedido exportado com sucesso para: " + arquivo.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("❌ Erro ao exportar o pedido: " + e.getMessage());
        }
    }

    /**
     * Método compatível com o PedidoService
     */
    public static void exportar(Pedido pedido) {
        exportarParaArquivo(pedido);
    }
}
