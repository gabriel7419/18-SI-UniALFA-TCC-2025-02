package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CadastroAcolhidoRepository extends JpaRepository <CadastroAcolhido, Long> {
    long countByDataSaidaIsNull();
}