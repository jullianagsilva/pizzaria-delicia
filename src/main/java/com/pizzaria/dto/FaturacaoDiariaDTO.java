package com.pizzaria.dto;

import java.time.LocalDate;

public class FaturacaoDiariaDTO {
    private LocalDate dia;   // nome correto usado no Thymeleaf
    private double valor;    // nome correto usado no Thymeleaf

    public FaturacaoDiariaDTO(LocalDate dia, double valor) {
        this.dia = dia;
        this.valor = valor;
    }

    // Getters
    public LocalDate getDia() {
        return dia;
    }

    public double getValor() {
        return valor;
    }
}
