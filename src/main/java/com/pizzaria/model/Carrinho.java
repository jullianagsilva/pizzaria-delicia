package com.pizzaria.model;

import java.util.ArrayList;
import java.util.List;

public class Carrinho {
    private List<Pizza> pizzas;
    public static final double TAXA_ENTREGA = 3.50; // taxa fixa de entrega

    // ================== CONSTRUTOR ==================
    public Carrinho() {
        this.pizzas = new ArrayList<>(); // garante lista não nula
    }

    // ================== MÉTODOS PRINCIPAIS ==================
    public void adicionarPizza(Pizza pizza) {
        if (pizza != null) {
            pizzas.add(pizza);
        }
    }

    public void removerPizza(int index) {
        if (index >= 0 && index < pizzas.size()) {
            pizzas.remove(index);
        }
    }

    public void limpar() {
        pizzas.clear();
    }

    public double calcularTotal() {
        return pizzas.stream()
                     .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                     .sum();
    }

    public List<Pizza> getPizzas() {
        return pizzas;
    }

    public void setPizzas(List<Pizza> pizzas) {
        this.pizzas = (pizzas != null) ? pizzas : new ArrayList<>();
    }

    public String mostrarCarrinho() {
        StringBuilder sb = new StringBuilder();
        if (pizzas.isEmpty()) {
            sb.append("Carrinho vazio.\n");
        } else {
            for (Pizza p : pizzas) {
                sb.append(p.getQuantidade())
                  .append("x ")
                  .append(p.getSabor())
                  .append(" (").append(p.getTamanho()).append(")")
                  .append(" - R$ ")
                  .append(String.format("%.2f", p.getPreco() * p.getQuantidade()))
                  .append("\n");
            }
        }
        sb.append("\nTaxa de entrega: R$ ")
          .append(String.format("%.2f", TAXA_ENTREGA));
        return sb.toString();
    }
}
