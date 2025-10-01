package edu.unialfa.alberguepro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataEntrada;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataSaida;

    @Enumerated(EnumType.STRING)
    private NumeroLeito numeroLeito;

    @Enumerated(EnumType.STRING)
    private Quarto quarto;

    @ManyToOne
    @JoinColumn(name = "acolhido_id")
    private CadastroAcolhido acolhido;

    public enum Quarto {
        Quarto1,
        Quarto2,
        Quarto3,
        Quarto4
    }

    public enum NumeroLeito {
        Leito1,
        Leito2,
        Leito3,
        Leito4
    }
}
