package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.repository.CadastroAcolhidoRepository;
import edu.unialfa.alberguepro.dto.AcolhidoDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.format.DateTimeFormatter;
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

    public ByteArrayInputStream gerarRelatorioExcel() throws IOException {
        List<AcolhidoDTO> acolhidosDTO = repository.findAll()
                .stream()
                .map(AcolhidoDTO::new)
                .collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Acolhidos");

        // Estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;

        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        // Subtítulo
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório de Acolhidos");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

        // Informações do relatório
        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        Cell infoCell1 = infoRow1.createCell(0);
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        infoCell1.setCellValue("Data de Emissão: " + agora.format(formatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Cell infoCell2 = infoRow1.createCell(5);
        infoCell2.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 5, 8));

        Row infoRow2 = sheet.createRow(rowNum++);
        Cell totalCell = infoRow2.createCell(0);
        totalCell.setCellValue("Total de Registros: " + acolhidosDTO.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        rowNum++;

        // Cabeçalho da tabela
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Nome", "Data Nasc.", "Idade", "Sexo", "Naturalidade", "RG", "CPF", "Data Ingresso"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dados
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (AcolhidoDTO acolhido : acolhidosDTO) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(acolhido.getId());
            cell0.setCellStyle(centerStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(acolhido.getNome());
            cell1.setCellStyle(dataStyle);
            
            Cell cell2 = row.createCell(2);
            if (acolhido.getDataNascimento() != null) {
                cell2.setCellValue(acolhido.getDataNascimento().format(dateFormatter));
            }
            cell2.setCellStyle(dateStyle);
            
            Cell cell3 = row.createCell(3);
            if (acolhido.getIdade() != null) {
                cell3.setCellValue(acolhido.getIdade());
            }
            cell3.setCellStyle(centerStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(acolhido.getSexo());
            cell4.setCellStyle(centerStyle);
            
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(acolhido.getNaturalidade());
            cell5.setCellStyle(dataStyle);
            
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(acolhido.getRg());
            cell6.setCellStyle(dataStyle);
            
            Cell cell7 = row.createCell(7);
            cell7.setCellValue(acolhido.getCpf());
            cell7.setCellStyle(dataStyle);
            
            Cell cell8 = row.createCell(8);
            if (acolhido.getDataIngresso() != null) {
                cell8.setCellValue(acolhido.getDataIngresso().format(dateFormatter));
            }
            cell8.setCellStyle(dateStyle);
        }

        // Ajustar largura das colunas
        sheet.setColumnWidth(0, 1500);  // ID
        sheet.setColumnWidth(1, 8000);  // Nome
        sheet.setColumnWidth(2, 3000);  // Data Nasc.
        sheet.setColumnWidth(3, 2000);  // Idade
        sheet.setColumnWidth(4, 2500);  // Sexo
        sheet.setColumnWidth(5, 5000);  // Naturalidade
        sheet.setColumnWidth(6, 3500);  // RG
        sheet.setColumnWidth(7, 4000);  // CPF
        sheet.setColumnWidth(8, 3500);  // Data Ingresso

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
