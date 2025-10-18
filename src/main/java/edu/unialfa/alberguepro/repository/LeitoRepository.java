package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Leito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeitoRepository extends JpaRepository<Leito, Long> {

    List<Leito> findByQuartoIdOrderByNumeroLeitoAsc(Long quartoId);
}