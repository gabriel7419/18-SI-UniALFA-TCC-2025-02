package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Quarto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    List<Quarto> findByNumeroQuartoContainingIgnoreCase(String numeroQuarto);

    Quarto findByNumeroQuarto(String numeroQuarto);

    Quarto findByNumeroQuartoAndIdNot(String numeroQuarto, Long id);

    @Query("SELECT COUNT(DISTINCT q.id) FROM Quarto q " +
           "WHERE EXISTS (" +
           "  SELECT 1 FROM Leito l " +
           "  WHERE l.quarto = q) " +
           "AND EXISTS (" +
           "  SELECT 1 FROM Leito l2 " +
           "  WHERE l2.quarto = q " +
           "  AND NOT EXISTS (" +
           "    SELECT 1 FROM Vaga v " +
           "    WHERE v.leito = l2 " +
           "    AND v.acolhido IS NOT NULL " +
           "    AND (v.dataSaida IS NULL OR v.dataSaida >= CURRENT_DATE)))")
    long countQuartosComLeitosLivres();

    @Query("SELECT COUNT(DISTINCT q.id) FROM Quarto q " +
           "WHERE NOT EXISTS (" +
           "  SELECT 1 FROM Leito l " +
           "  WHERE l.quarto = q " +
           "  AND NOT EXISTS (" +
           "    SELECT 1 FROM Vaga v " +
           "    WHERE v.leito = l " +
           "    AND v.acolhido IS NOT NULL " +
           "    AND (v.dataSaida IS NULL OR v.dataSaida >= CURRENT_DATE)))")
    long countQuartosTotalmenteOcupados();
}
