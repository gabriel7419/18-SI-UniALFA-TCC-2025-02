package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    List<Produto> findByNomeContainingIgnoreCaseAndTipoContainingIgnoreCase(String nome, String tipo);
    Optional<Produto> findByNomeIgnoreCaseAndTipoIgnoreCase(String nome, String tipo);
    Optional<Produto> findByNomeIgnoreCaseAndTipoIgnoreCaseAndIdNot(String nome, String tipo, Long id);
    List<Produto> findTop5ByOrderByQuantidadeAsc();
    List<Produto> findTop10ByOrderByQuantidadeAsc();
    List<Produto> findByNaoPerecivelFalseAndDataDeVencimentoBetweenOrderByDataDeVencimentoAsc(LocalDate dataInicio, LocalDate dataFim);
}