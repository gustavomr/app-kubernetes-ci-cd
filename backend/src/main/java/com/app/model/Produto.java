package com.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    public Produto() {}

    public Produto(String nome, BigDecimal preco) {
        this.nome = nome;
        this.preco = preco;
        this.valor = preco;
    }

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

    public BigDecimal getPreco() {
        if (valor != null) return valor;
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
        this.valor = preco;
    }

    public BigDecimal getValor() {
        if (valor != null) return valor;
        return preco;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
        this.preco = valor;
    }
}
