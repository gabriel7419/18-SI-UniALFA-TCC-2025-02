package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.ControleEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControleEstoqueRepository extends JpaRepository <ControleEstoque, Long> {
    Optional<ControleEstoque> findByNome(String nome);
}
