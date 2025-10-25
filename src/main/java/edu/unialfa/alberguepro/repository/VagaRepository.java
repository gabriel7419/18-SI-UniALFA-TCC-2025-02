package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VagaRepository extends JpaRepository<Vaga, Long>  {
    long countByAcolhidoIsNotNullAndDataSaidaIsNull();
    long countByAcolhidoIsNull();

    @Query("SELECT v.leito.quarto.numeroQuarto, COUNT(v) " +
            "FROM Vaga v " +
            "WHERE v.acolhido IS NOT NULL AND v.dataSaida IS NULL " +
            "GROUP BY v.leito.quarto.numeroQuarto")
    List<Object[]> countOccupiedBedsByRoom();

    @Query("SELECT COUNT(DISTINCT v.acolhido.id) FROM Vaga v " +
            "WHERE v.acolhido IS NOT NULL AND v.dataSaida IS NULL")
    long countDistinctAcolhidosAtivos();
}
