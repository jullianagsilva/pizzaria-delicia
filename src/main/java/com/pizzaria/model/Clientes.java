package com.pizzaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Clientes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nome;

    @Column(unique = true)
    private String telefone;

    private String morada;

    public Clientes() {}

    public Clientes(String nome, String telefone, String morada) {
        this.nome = nome;
        this.telefone = telefone;
        this.morada = morada;
    }

    public Clientes(int id, String nome, String telefone, String morada) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.morada = morada;
    }

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    @Override
    public String toString() {
        return "Cliente{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", telefone='" + telefone + '\'' +
               ", morada='" + morada + '\'' +
               '}';
    }

    public String apresentar() {
        return "Nome: " + nome + "\nTelefone: " + telefone + "\nMorada: " + morada;
    }
}
