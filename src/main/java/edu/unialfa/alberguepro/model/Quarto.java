package edu.unialfa.alberguepro.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O número do quarto é obrigatório.")
    private String numeroQuarto;

    @ToString.Exclude
    @JsonIgnoreProperties("quarto")
    @OneToMany(mappedBy = "quarto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)

    private List<Leito> leitos = new ArrayList<>();

    public void addLeito(Leito leito) {
        this.leitos.add(leito);
        leito.setQuarto(this);
    }
}
