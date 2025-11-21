package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>, JpaSpecificationExecutor<MovimentacaoEstoque> {
    List<MovimentacaoEstoque> findAllByOrderByDataMovimentacaoDesc();
    
    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.dataMovimentacao BETWEEN :dataInicio AND :dataFim ORDER BY m.dataMovimentacao DESC")
    List<MovimentacaoEstoque> findByDataMovimentacaoBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);
    
    @Query("SELECT m FROM MovimentacaoEstoque m WHERE m.dataMovimentacao BETWEEN :dataInicio AND :dataFim AND m.tipo = :tipo ORDER BY m.dataMovimentacao DESC")
    List<MovimentacaoEstoque> findByDataMovimentacaoBetweenAndTipo(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim, @Param("tipo") MovimentacaoEstoque.TipoMovimentacao tipo);
}
