package edu.unialfa.alberguepro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList; // Importe este
import java.util.List;

@Getter
@Setter
@Entity
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroQuarto;

    @OneToMany(mappedBy = "quarto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)

    private List<Leito> leitos = new ArrayList<>();

    public void addLeito(Leito leito) {
        this.leitos.add(leito);
        leito.setQuarto(this);
    }
}
