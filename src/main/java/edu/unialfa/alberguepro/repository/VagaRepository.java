package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Vaga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VagaRepository extends JpaRepository<Vaga, Long>  {
    long countByAcolhidoIsNotNullAndDataSaidaIsNull();
    long countByAcolhidoIsNull();
    
    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.acolhido IS NOT NULL AND " +
           "(v.dataSaida IS NULL OR v.dataSaida >= CURRENT_DATE)")
    long countLeitosOcupados();

    @Query("SELECT COUNT(v.id) FROM Vaga v " +
            "WHERE v.leito.quarto.id = :quartoId")
    long countVagasByQuartoId(@Param("quartoId") Long quartoId);

    @Query("SELECT v.leito.id " +
            "FROM Vaga v " +
            "WHERE v.acolhido IS NOT NULL " +
            "  AND CURRENT_DATE BETWEEN v.dataEntrada AND v.dataSaida")
    List<Long> findOccupiedLeitoIds();

    @Query("SELECT COUNT(v) " +
            "FROM Vaga v " +
            "WHERE v.leito.quarto.id = :quartoId " +
            "  AND v.acolhido IS NOT NULL " +
            "  AND (v.dataSaida IS NULL OR v.dataSaida >= CURRENT_DATE)")
    Long countActiveVagasByQuartoId(@Param("quartoId") Long quartoId);

    @Query("SELECT v.leito.quarto.numeroQuarto, COUNT(DISTINCT v.leito.id) " +
            "FROM Vaga v " +
            "WHERE v.acolhido IS NOT NULL " +
            "  AND (v.dataSaida IS NULL OR v.dataSaida >= CURRENT_DATE) " +
            "GROUP BY v.leito.quarto.numeroQuarto")
    List<Object[]> countOccupiedBedsByRoom();

    @Query("SELECT COUNT(DISTINCT v.acolhido.id) FROM Vaga v " +
            "WHERE v.acolhido IS NOT NULL AND v.dataSaida IS NULL")
    long countDistinctAcolhidosAtivos();

    @Query(value = "SELECT MONTH(data_entrada) as mes, YEAR(data_entrada) as ano, COUNT(*) as qtd " +
            "FROM vaga " +
            "WHERE data_entrada >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
            "GROUP BY YEAR(data_entrada), MONTH(data_entrada) " +
            "ORDER BY ano, mes", nativeQuery = true)
    List<Object[]> countEntradasUltimos6Meses();

    @Query(value = "SELECT MONTH(data_saida) as mes, YEAR(data_saida) as ano, COUNT(*) as qtd " +
            "FROM vaga " +
            "WHERE data_saida IS NOT NULL AND data_saida >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
            "GROUP BY YEAR(data_saida), MONTH(data_saida) " +
            "ORDER BY ano, mes", nativeQuery = true)
    List<Object[]> countSaidasUltimos6Meses();

    List<Vaga> findByAcolhidoNomeContainingIgnoreCase(String nome);
    
    Page<Vaga> findByAcolhidoNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.dataEntrada BETWEEN :dataInicio AND :dataFim")
    long countEntradasPorPeriodo(@Param("dataInicio") java.time.LocalDate dataInicio, 
                                  @Param("dataFim") java.time.LocalDate dataFim);

    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.dataSaida BETWEEN :dataInicio AND :dataFim AND v.dataSaida IS NOT NULL")
    long countSaidasPorPeriodo(@Param("dataInicio") java.time.LocalDate dataInicio, 
                                @Param("dataFim") java.time.LocalDate dataFim);

    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.dataEntrada <= :data AND (v.dataSaida IS NULL OR v.dataSaida > :data)")
    long countLeitosOcupadosNaData(@Param("data") java.time.LocalDate data);
}
