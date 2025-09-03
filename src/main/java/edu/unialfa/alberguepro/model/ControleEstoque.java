package edu.unialfa.alberguepro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControleEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Integer quantidade;
    private LocalDate data_vencimento;

    public enum Unidade {
        Kilo,
        Litro,
        Unidade
    }

    private Unidade unidade;

    public enum TipoProduto {
        Alimento,
        Higiene,
        Limpeza
    }

    private TipoProduto tipo;

}
