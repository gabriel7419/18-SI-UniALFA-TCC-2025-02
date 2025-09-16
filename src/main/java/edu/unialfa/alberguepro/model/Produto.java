package edu.unialfa.alberguepro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O tipo é obrigatório")
    private String tipo; // Alimento, Higiene, Limpeza

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 128, message = "O nome não pode ter mais de 128 caracteres")
    private String nome;

    @NotNull(message = "A quantidade é obrigatória")
    @Max(value = 5000, message = "A quantidade não pode ser maior que 5000")
    private Integer quantidade;

    @NotBlank(message = "A unidade é obrigatória")
    private String unidade; // Ex: kg, pacote, litro

    @NotNull(message = "A data de vencimento é obrigatória")
    @Future(message = "A data de vencimento deve ser uma data futura.")
    private LocalDate dataDeVencimento;

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
    public String getUnidade() {
        return unidade;
    }
    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
    public LocalDate getDataDeVencimento() {
        return dataDeVencimento;
    }
    public void setDataDeVencimento(LocalDate dataDeVencimento) {
        this.dataDeVencimento = dataDeVencimento;
    }
}