package com.pizzaria.dto;

public class ClienteFrequenteDTO {
    private int id;
    private String nome;
    private int totalPedidos;
    private double totalGasto;

    public ClienteFrequenteDTO(int id, String nome, int totalPedidos, double totalGasto) {
        this.id = id;
        this.nome = nome;
        this.totalPedidos = totalPedidos;
        this.totalGasto = totalGasto;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public double getTotalGasto() {
        return totalGasto;
    }
}
