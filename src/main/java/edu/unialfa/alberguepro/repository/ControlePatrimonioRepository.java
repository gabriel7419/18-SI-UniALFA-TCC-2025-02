package edu.unialfa.alberguepro.repository;


import edu.unialfa.alberguepro.model.ControlePatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ControlePatrimonioRepository extends JpaRepository <ControlePatrimonio, Long>, JpaSpecificationExecutor<ControlePatrimonio> {
    List<ControlePatrimonio> findByNomeContainingIgnoreCase(String nome);
    List<ControlePatrimonio> findByStatus(String status);
    List<ControlePatrimonio> findByNomeContainingIgnoreCaseAndStatus(String nome, String status);
    ControlePatrimonio findByPatrimonio(Integer patrimonio);
    ControlePatrimonio findByPatrimonioAndIdNot(Integer patrimonio, Long id);
}
