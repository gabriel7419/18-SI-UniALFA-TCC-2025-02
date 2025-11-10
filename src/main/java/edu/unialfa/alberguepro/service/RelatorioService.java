package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class RelatorioService {

    public ByteArrayInputStream gerarRelatorioPdf(List<Produto> produtos) throws JRException {
        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_estoque.jrxml");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(produtos);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        // Calcular estatísticas
        int totalItens = produtos.size();
        long itensEsgotados = produtos.stream().filter(p -> p.getQuantidade() == 0).count();

        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("TOTAL_ITENS", totalItens);
        parameters.put("ITENS_ESGOTADOS", (int)itensEsgotados);

        // Obter data/hora atual no fuso horário GMT-3 (America/Sao_Paulo)
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        parameters.put("DATA_EMISSAO", java.util.Date.from(agora.toInstant()));

        // Configurar timezone do relatório
        parameters.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone(saoPauloZone));

        parameters.put("USUARIO_EMISSOR", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioMovimentacaoPdf(List<MovimentacaoEstoque> movimentacoes) throws JRException {
        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_movimentacao_estoque.jrxml");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(movimentacoes);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("TOTAL_MOVIMENTACOES", movimentacoes.size());

        // Obter data/hora atual no fuso horário GMT-3 (America/Sao_Paulo)
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        parameters.put("DATA_EMISSAO", java.util.Date.from(agora.toInstant()));

        // Configurar timezone do relatório
        parameters.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone(saoPauloZone));

        parameters.put("USUARIO_EMISSOR", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }
}