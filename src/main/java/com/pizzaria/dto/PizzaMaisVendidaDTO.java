package com.pizzaria.dto;

public class PizzaMaisVendidaDTO {
    private String sabor;
    private String tamanho;
    private int quantidadeVendida;
    private double totalVendas; // 💰 valor total em euros

    public PizzaMaisVendidaDTO(String sabor, String tamanho, int quantidadeVendida, double totalVendas) {
        this.sabor = sabor;
        this.tamanho = tamanho;
        this.quantidadeVendida = quantidadeVendida;
        this.totalVendas = totalVendas;
    }

    // Getters
    public String getSabor() {
        return sabor;
    }

    public String getTamanho() {
        return tamanho;
    }

    public int getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public double getTotalVendas() {
        return totalVendas;
    }
}
