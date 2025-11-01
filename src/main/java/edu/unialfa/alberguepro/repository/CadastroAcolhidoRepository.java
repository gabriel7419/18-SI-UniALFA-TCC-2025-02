package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CadastroAcolhidoRepository extends JpaRepository <CadastroAcolhido, Long> {
    long countByDataSaidaIsNull();

    boolean existsByCpf(String cpf);

    List<CadastroAcolhido> findByNomeContainingIgnoreCase(String nome);
}