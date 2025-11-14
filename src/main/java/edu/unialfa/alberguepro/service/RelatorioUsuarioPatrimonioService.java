package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
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
    
    public ByteArrayInputStream gerarRelatorioPatrimonioPdf(List<ControlePatrimonio> patrimonios) throws JRException {
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

    public ByteArrayInputStream gerarRelatorioUsuarioExcel() throws IOException {
        List<Usuario> usuarios = usuarioRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Usuários");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório de Usuários");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        Cell infoCell1 = infoRow1.createCell(0);
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        infoCell1.setCellValue("Data de Emissão: " + agora.format(formatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell infoCell2 = infoRow1.createCell(3);
        infoCell2.setCellValue("Usuário: " + SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 4));

        Row infoRow2 = sheet.createRow(rowNum++);
        Cell totalCell = infoRow2.createCell(0);
        totalCell.setCellValue("Total de Usuários: " + usuarios.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell ativosCell = infoRow2.createCell(3);
        long usuariosAtivos = usuarios.stream().filter(Usuario::isAtivo).count();
        ativosCell.setCellValue("Usuários Ativos: " + usuariosAtivos);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 4));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Username", "Role", "Ativo", "Data Criação"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Usuario usuario : usuarios) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(usuario.getId());
            cell0.setCellStyle(centerStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(usuario.getUsername());
            cell1.setCellStyle(dataStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(usuario.getRole());
            cell2.setCellStyle(centerStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(usuario.isAtivo() ? "Sim" : "Não");
            cell3.setCellStyle(centerStyle);
            
            Cell cell4 = row.createCell(4);
            if (usuario.getDataCriacao() != null) {
                cell4.setCellValue(usuario.getDataCriacao().format(dateTimeFormatter));
            }
            cell4.setCellStyle(dateStyle);
        }

        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 2500);
        sheet.setColumnWidth(4, 4500);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
    
    public ByteArrayInputStream gerarRelatorioPatrimonioExcel(List<ControlePatrimonio> patrimonios) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Patrimônio");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório de Patrimônio");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        Cell infoCell1 = infoRow1.createCell(0);
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        infoCell1.setCellValue("Data de Emissão: " + agora.format(formatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell infoCell2 = infoRow1.createCell(3);
        infoCell2.setCellValue("Usuário: " + SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));

        Row infoRow2 = sheet.createRow(rowNum++);
        Cell totalCell = infoRow2.createCell(0);
        totalCell.setCellValue("Total de Patrimônios: " + patrimonios.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Nome", "Nº Patrimônio", "Data Aquisição", "Local Atual", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (ControlePatrimonio patrimonio : patrimonios) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(patrimonio.getId());
            cell0.setCellStyle(centerStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(patrimonio.getNome());
            cell1.setCellStyle(dataStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(patrimonio.getPatrimonio());
            cell2.setCellStyle(centerStyle);
            
            Cell cell3 = row.createCell(3);
            if (patrimonio.getData_aquisicao() != null) {
                cell3.setCellValue(patrimonio.getData_aquisicao().format(dateFormatter));
            }
            cell3.setCellStyle(dateStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(patrimonio.getLocal_atual());
            cell4.setCellStyle(dataStyle);
            
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(patrimonio.getStatus());
            cell5.setCellStyle(centerStyle);
        }

        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 7000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 3500);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 20);
        style.setFont(font);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
