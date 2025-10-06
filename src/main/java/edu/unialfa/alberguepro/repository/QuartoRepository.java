package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {
    List<Quarto> findByQuartoContainingIgnoreCase(String room);
}
