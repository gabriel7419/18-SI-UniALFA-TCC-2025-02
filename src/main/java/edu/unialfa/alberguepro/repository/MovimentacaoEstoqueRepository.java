package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>, JpaSpecificationExecutor<MovimentacaoEstoque> {
    List<MovimentacaoEstoque> findAllByOrderByDataMovimentacaoDesc();
}
