package edu.unialfa.alberguepro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O tipo do produto é obrigatório.")
    private String tipo; // Alimento, Higiene, Limpeza

    @NotBlank(message = "O nome do produto é obrigatório.")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    private String nome;

    @NotNull(message = "A quantidade é obrigatória.")
    @PositiveOrZero(message = "A quantidade não pode ser um número negativo.")
    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "unidade_id")
    private Unidade unidade;

    @Column(name = "nao_perecivel", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean naoPerecivel = false;

    @Column(name = "data_de_vencimento", nullable = true)
    @Future(message = "A data de vencimento deve ser uma data futura.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataDeVencimento;

    @Transient
    @NotNull(message = "A unidade de medida é obrigatória.")
    private Long unidadeId;

    @PostLoad
    private void onLoad() {
        if (this.unidade != null) {
            this.unidadeId = this.unidade.getId();
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public Integer getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    public Unidade getUnidade() {
        return unidade;
    }
    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }
    public LocalDate getDataDeVencimento() {
        return dataDeVencimento;
    }
    public void setDataDeVencimento(LocalDate dataDeVencimento) {
        this.dataDeVencimento = dataDeVencimento;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public Boolean getNaoPerecivel() {
        return naoPerecivel;
    }

    public void setNaoPerecivel(Boolean naoPerecivel) {
        this.naoPerecivel = naoPerecivel;
    }
}
