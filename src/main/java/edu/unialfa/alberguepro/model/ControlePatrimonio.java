package edu.unialfa.alberguepro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ControlePatrimonio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 30, message = "O nome não pode ter mais de 30 caracteres")
    private String nome;

    @NotNull(message = "O número de patrimônio é obrigatório")
    @Max(value = 30000, message = "O patrimônio não pode ter mais de 30000  caracteres")
    private Integer patrimonio;

    @NotNull(message = "A data de aquisição é obrigatória")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data_aquisicao;

    @NotBlank(message = "A localização atual é obrigatória")
    @Size(max = 30, message = "A localização não pode ter mais de 30 caracteres")
    private String local_atual;

    @NotBlank(message = "A observação é obrigatória")
    @Size(max = 30, message = "A observação não pode ter mais de 30 caracteres")
    private String observacao;

    @NotBlank(message = "O status é obrigatório")
    private String status;
}