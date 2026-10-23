package com.pizzaria.model;

public class Pagamento {
    private int id;
    private int idPedido;
    private double valor;
    private String status;
    private String dataPagamento;

    // 🔹 Novo campo para armazenar a forma de pagamento escolhida
    private String metodo;

    // Getters
    public int getId() {
        return id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public double getValor() {
        return valor;
    }

    public String getStatus() {
        return status;
    }

    public String getDataPagamento() {
        return dataPagamento;
    }

    public String getMetodo() {
        return metodo;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDataPagamento(String dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }
}
