package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.repository.CadastroAcolhidoRepository;
import edu.unialfa.alberguepro.dto.AcolhidoDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RelatorioAcolhidoService {

    @Autowired
    private CadastroAcolhidoRepository repository;

    public ByteArrayInputStream gerarRelatorioPdf() throws JRException {
        List<AcolhidoDTO> acolhidosDTO = repository.findAll()
                .stream()
                .map(AcolhidoDTO::new)
                .collect(Collectors.toList());

        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_acolhido.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(acolhidosDTO);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("TOTAL_REGISTROS", acolhidosDTO.size());
        
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
