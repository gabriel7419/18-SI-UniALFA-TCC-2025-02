package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Leito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LeitoRepository  extends JpaRepository<Leito, Long>  {
    long countByAcolhidoIsNotNull();
    long countByAcolhidoIsNull();

    @Query("SELECT COUNT(DISTINCT l.quarto) FROM Leito l WHERE l.acolhido IS NOT NULL")
    long countDistinctQuartoByAcolhidoIsNotNull();

    @Query("SELECT l.quarto, COUNT(l) FROM Leito l WHERE l.acolhido IS NOT NULL GROUP BY l.quarto")
    List<Object[]> countOccupiedBedsByRoom();
}
