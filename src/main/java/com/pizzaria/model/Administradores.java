package com.pizzaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administradores") // opcional, garante que o nome da tabela seja correto
public class Administradores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;      // campo "nome" que seu DAO tenta setar
    private String usuario;   // campo de login
    private String senhaHash; // hash da senha
    private String salt;      // salt para gerar o hash

    // Getters e setters
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getNome() { 
        return nome; 
    }
    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getUsuario() { 
        return usuario; 
    }
    public void setUsuario(String usuario) { 
        this.usuario = usuario; 
    }

    public String getSenhaHash() { 
        return senhaHash; 
    }
    public void setSenhaHash(String senhaHash) { 
        this.senhaHash = senhaHash; 
    }

    public String getSalt() { 
        return salt; 
    }
    public void setSalt(String salt) { 
        this.salt = salt; 
    }
}
