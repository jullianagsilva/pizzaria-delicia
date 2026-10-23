package com.pizzaria.model;

public class Pizza {

    public enum Sabor {
        MARGUERITA("Marguerita"),
        CALABRESA("Calabresa"),
        PORTUGUESA("Portuguesa"),
        FRANGO_CATUPIRY("Frango com Catupiry"),
        QUATRO_QUEIJOS("Quatro Queijos"),
        VEGETARIANA("Vegetariana");

        private final String descricao;

        Sabor(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private String sabor;
    private String tamanho;
    private double preco;
    private int quantidade = 1;
    private String observacoes = ""; // NOVO CAMPO

    // Construtor vazio para Jackson
    public Pizza() {}

    public Pizza(Sabor sabor, String tamanho, int quantidade) {
        this.sabor = sabor.toString();
        this.tamanho = tamanho;
        this.preco = calcularPreco(tamanho);
        this.quantidade = quantidade;
    }

    public Pizza(String sabor, String tamanho, double preco) {
        this.sabor = sabor;
        this.tamanho = tamanho;
        this.preco = preco;
        this.quantidade = 1;
    }

    // Getters e setters
    public String getSabor() {
        return sabor;
    }

    public void setSabor(String sabor) {
        this.sabor = sabor;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
        this.preco = calcularPreco(tamanho);
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = (observacoes != null) ? observacoes.trim() : "";
    }

    private double calcularPreco(String tamanho) {
        if (tamanho == null) return 0.0;
        switch (tamanho.toLowerCase()) {
            case "pequena": return 15.0;
            case "média": return 20.0;
            case "grande": return 25.0;
            default: return 0.0;
        }
    }

    public String getDescricao() {
        return quantidade + "x " + sabor + " (" + tamanho + ") - R$ " + String.format("%.2f", preco * quantidade)
                + (observacoes.isEmpty() ? "" : " [Obs: " + observacoes + "]");
    }

    @Override
    public String toString() {
        return getDescricao();
    }
}
