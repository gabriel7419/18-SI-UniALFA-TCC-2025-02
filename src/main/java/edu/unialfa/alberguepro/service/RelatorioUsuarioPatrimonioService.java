package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class RelatorioUsuarioPatrimonioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ControlePatrimonioRepository patrimonioRepository;

    public ByteArrayInputStream gerarRelatorioUsuarioPdf() throws JRException {
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_usuario.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(usuarios);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        
        // Calcular estatísticas
        int totalUsuarios = usuarios.size();
        long usuariosAtivos = usuarios.stream().filter(Usuario::isAtivo).count();
        
        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("TOTAL_USUARIOS", totalUsuarios);
        parameters.put("USUARIOS_ATIVOS", (int)usuariosAtivos);
        
        // Obter data/hora atual no fuso horário GMT-3 (America/Sao_Paulo)
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        parameters.put("DATA_EMISSAO", java.util.Date.from(agora.toInstant()));
        
        // Configurar timezone do relatório
        parameters.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone(saoPauloZone));
        
        parameters.put("USUARIO_EMISSOR", SecurityContextHolder.getContext().getAuthentication().getName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }
    
    public ByteArrayInputStream gerarRelatorioPatrimonioPdf() throws JRException {
        List<ControlePatrimonio> patrimonios = patrimonioRepository.findAll();
        
        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_patrimonio.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(patrimonios);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        
        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("TOTAL_PATRIMONIOS", patrimonios.size());
        
        // Obter data/hora atual no fuso horário GMT-3 (America/Sao_Paulo)
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        parameters.put("DATA_EMISSAO", java.util.Date.from(agora.toInstant()));
        
        // Configurar timezone do relatório
        parameters.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone(saoPauloZone));
        
        parameters.put("USUARIO_EMISSOR", SecurityContextHolder.getContext().getAuthentication().getName());
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }
}
