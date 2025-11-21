package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CadastroAcolhidoRepository extends JpaRepository <CadastroAcolhido, Long> {
    long countByDataSaidaIsNull();

    boolean existsByCpf(String cpf);

    List<CadastroAcolhido> findByNomeContainingIgnoreCase(String nome);
    
    Page<CadastroAcolhido> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    List<CadastroAcolhido> findByDataIngressoBeforeAndDataSaidaIsNullOrderByDataIngressoAsc(LocalDate dataLimite);
    
    List<CadastroAcolhido> findByDataIngressoBetweenOrderByDataIngressoAsc(LocalDate dataInicio, LocalDate dataFim);
    
    long countByDataIngressoBetween(LocalDate dataInicio, LocalDate dataFim);
}