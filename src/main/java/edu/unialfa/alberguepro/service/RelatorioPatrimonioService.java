package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import edu.unialfa.alberguepro.repository.PatrimonioSpecification;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioPatrimonioService {

    @Autowired
    private ControlePatrimonioRepository repository;

    public ByteArrayInputStream gerarRelatorioPdf(String nome, String status, String localAtual) throws JRException {
        List<ControlePatrimonio> patrimonios = buscarPatrimoniosComFiltros(nome, status, localAtual);

        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_patrimonio.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML de patrimônio não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(patrimonios);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("TOTAL_PATRIMONIOS", patrimonios.size());
        
        ZoneId saoPauloZone = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime agora = ZonedDateTime.now(saoPauloZone);
        parameters.put("DATA_EMISSAO", java.util.Date.from(agora.toInstant()));
        parameters.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone(saoPauloZone));
        parameters.put("USUARIO_EMISSOR", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioExcel(String nome, String status, String localAtual) throws IOException {
        List<ControlePatrimonio> patrimonios = buscarPatrimoniosComFiltros(nome, status, localAtual);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Patrimônios");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        // Subtítulo
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório de Patrimônios");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        // Informações
        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        ZoneId saoPauloZone = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime agora = ZonedDateTime.now(saoPauloZone);
        infoRow1.createCell(0).setCellValue("Data de Emissão: " + agora.format(dateTimeFormatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Cell userCell = infoRow1.createCell(4);
        userCell.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 4, 6));

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Total de Patrimônios: " + patrimonios.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));

        rowNum++;

        // Cabeçalho
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Nome", "Nº Patrimônio", "Data Aquisição", "Local Atual", "Status", "Observação"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dados
        for (ControlePatrimonio patrimonio : patrimonios) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(patrimonio.getNome());
            row.getCell(0).setCellStyle(dataStyle);
            
            row.createCell(1).setCellValue(patrimonio.getPatrimonio());
            row.getCell(1).setCellStyle(centerStyle);
            
            Cell cell2 = row.createCell(2);
            if (patrimonio.getData_aquisicao() != null) {
                cell2.setCellValue(patrimonio.getData_aquisicao().format(dateFormatter));
            }
            cell2.setCellStyle(dateStyle);
            
            row.createCell(3).setCellValue(patrimonio.getLocal_atual());
            row.getCell(3).setCellStyle(dataStyle);
            
            row.createCell(4).setCellValue(patrimonio.getStatus());
            row.getCell(4).setCellStyle(centerStyle);
            
            row.createCell(5).setCellValue(patrimonio.getObservacao() != null ? patrimonio.getObservacao() : "");
            row.getCell(5).setCellStyle(dataStyle);
        }

        // Ajustar largura das colunas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private List<ControlePatrimonio> buscarPatrimoniosComFiltros(String nome, String status, String localAtual) {
        Specification<ControlePatrimonio> spec = Specification.where(null);

        if (nome != null && !nome.trim().isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comNome(nome));
        }

        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comStatus(status));
        }

        if (localAtual != null && !localAtual.trim().isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comLocalAtual(localAtual));
        }

        return repository.findAll(spec);
    }

    // Métodos auxiliares para estilos Excel
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
        style.setFont(font);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
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
