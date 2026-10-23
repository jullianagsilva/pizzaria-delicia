package com.pizzaria.util;

public class Validador {

    // Valida se o telefone tem exatamente 9 dígitos numéricos
    public static boolean telefoneValido(String telefone) {
        return telefone != null && telefone.matches("\\d{9}");
    }

    // Verifica se um texto foi preenchido (não é nulo, vazio ou apenas espaços)
    public static boolean textoPreenchido(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
