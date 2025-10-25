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

    @ManyToOne
    @JoinColumn(name = "leito_id")
    private Leito leito;

    @ManyToOne
    @JoinColumn(name = "acolhido_id")
    private CadastroAcolhido acolhido;
}
