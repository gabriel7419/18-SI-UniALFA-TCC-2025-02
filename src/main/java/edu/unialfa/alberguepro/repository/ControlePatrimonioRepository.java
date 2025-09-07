package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.ControleEstoque;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ControlePatrimonioRepository extends JpaRepository <ControlePatrimonio, Long> {
    List<ControlePatrimonio> findByNomeContainingIgnoreCase(String nome);
}
