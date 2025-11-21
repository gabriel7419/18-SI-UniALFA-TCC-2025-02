package edu.unialfa.alberguepro.repository;


import edu.unialfa.alberguepro.model.ControlePatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ControlePatrimonioRepository extends JpaRepository <ControlePatrimonio, Long>, JpaSpecificationExecutor<ControlePatrimonio> {
    List<ControlePatrimonio> findByNomeContainingIgnoreCase(String nome);
    List<ControlePatrimonio> findByStatus(String status);
    List<ControlePatrimonio> findByNomeContainingIgnoreCaseAndStatus(String nome, String status);
    ControlePatrimonio findByPatrimonio(String patrimonio);
    ControlePatrimonio findByPatrimonioAndIdNot(String patrimonio, Long id);
    
    @Query("SELECT c FROM ControlePatrimonio c WHERE c.data_aquisicao BETWEEN :dataInicio AND :dataFim ORDER BY c.data_aquisicao DESC")
    List<ControlePatrimonio> findByDataAquisicaoBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
    
    @Query("SELECT c FROM ControlePatrimonio c WHERE c.data_aquisicao BETWEEN :dataInicio AND :dataFim AND c.status = :status ORDER BY c.data_aquisicao DESC")
    List<ControlePatrimonio> findByDataAquisicaoBetweenAndStatus(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim, @Param("status") String status);
}
