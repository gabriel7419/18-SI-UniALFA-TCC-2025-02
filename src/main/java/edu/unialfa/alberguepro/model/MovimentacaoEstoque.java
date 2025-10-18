package edu.unialfa.alberguepro.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MovimentacaoEstoque {

    public enum TipoMovimentacao {
        ENTRADA, // Adição de novos itens ou aumento de quantidade
        SAIDA,   // Baixa de itens para uso
        AJUSTE_POSITIVO, // Ajuste manual de contagem para mais
        AJUSTE_NEGATIVO, // Ajuste manual de contagem para menos
        EXCLUSAO // Registro de exclusão de produto
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidadeMovimentada;

    @Column(nullable = false)
    private Integer quantidadeAnterior;
    
    @Column(nullable = false)
    private Integer quantidadePosterior;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataMovimentacao;

    private String observacao;

    @PrePersist
    protected void onCreate() {
        dataMovimentacao = LocalDateTime.now();
    }

    // Construtores, Getters e Setters

    public MovimentacaoEstoque() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidadeMovimentada() {
        return quantidadeMovimentada;
    }

    public void setQuantidadeMovimentada(Integer quantidadeMovimentada) {
        this.quantidadeMovimentada = quantidadeMovimentada;
    }

    public Integer getQuantidadeAnterior() {
        return quantidadeAnterior;
    }

    public void setQuantidadeAnterior(Integer quantidadeAnterior) {
        this.quantidadeAnterior = quantidadeAnterior;
    }

    public Integer getQuantidadePosterior() {
        return quantidadePosterior;
    }

    public void setQuantidadePosterior(Integer quantidadePosterior) {
        this.quantidadePosterior = quantidadePosterior;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
