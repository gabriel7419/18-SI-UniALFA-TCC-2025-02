package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByNomeContainingIgnoreCaseAndTipoContainingIgnoreCase(String nome, String tipo);
    Optional<Produto> findByNomeIgnoreCaseAndTipoIgnoreCase(String nome, String tipo);
    Optional<Produto> findByNomeIgnoreCaseAndTipoIgnoreCaseAndIdNot(String nome, String tipo, Long id);
}