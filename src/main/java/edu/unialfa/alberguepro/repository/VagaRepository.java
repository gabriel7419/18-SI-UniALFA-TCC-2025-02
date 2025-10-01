package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VagaRepository extends JpaRepository<Vaga, Long>  {
    long countByAcolhidoIsNotNull();
    long countByAcolhidoIsNull();

    @Query("SELECT COUNT(DISTINCT l.quarto) FROM Vaga l WHERE l.acolhido IS NOT NULL")
    long countDistinctQuartoByAcolhidoIsNotNull();

    @Query("SELECT l.quarto, COUNT(l) FROM Vaga l WHERE l.acolhido IS NOT NULL GROUP BY l.quarto")
    List<Object[]> countOccupiedBedsByRoom();
}
