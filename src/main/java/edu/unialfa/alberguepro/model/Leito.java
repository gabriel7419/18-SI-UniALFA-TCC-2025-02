package edu.unialfa.alberguepro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Leito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroLeito;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;
}