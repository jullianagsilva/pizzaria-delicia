package com.pizzaria.dto;

public class PedidoForm {
    private String sabor;
    private String tamanho;
    private int quantidade;

    // getters e setters
    public String getSabor() { return sabor; }
    public void setSabor(String sabor) { this.sabor = sabor; }

    public String getTamanho() { return tamanho; }
    public void setTamanho(String tamanho) { this.tamanho = tamanho; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
