package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Leito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeitoRepository extends JpaRepository<Leito, Long> {

    List<Leito> findByQuartoId(Long quartoId);
    
    @Query("SELECT l FROM Leito l WHERE l.quarto.id = :quartoId AND l.id NOT IN " +
           "(SELECT v.leito.id FROM Vaga v WHERE v.leito.id IS NOT NULL AND " +
           "(v.dataSaida IS NULL OR v.dataSaida >= CURRENT_DATE))")
    List<Leito> findLeitosDisponiveisByQuartoId(@Param("quartoId") Long quartoId);
}