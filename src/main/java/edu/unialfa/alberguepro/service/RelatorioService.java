package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
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

    public ByteArrayInputStream gerarRelatorioExcel(List<Produto> produtos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Estoque");

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
        subtitleCell.setCellValue("Relatório de Estoque");
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
        infoCell2.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));

        Row infoRow2 = sheet.createRow(rowNum++);
        Cell totalCell = infoRow2.createCell(0);
        totalCell.setCellValue("Total de Itens: " + produtos.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell esgotadosCell = infoRow2.createCell(3);
        long itensEsgotados = produtos.stream().filter(p -> p.getQuantidade() == 0).count();
        esgotadosCell.setCellValue("Itens Esgotados: " + itensEsgotados);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Tipo", "Nome", "Quantidade", "Unidade", "Data Vencimento"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Produto produto : produtos) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(produto.getId());
            cell0.setCellStyle(centerStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(produto.getTipo());
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(produto.getNome());
            cell2.setCellStyle(dataStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(produto.getQuantidade());
            cell3.setCellStyle(centerStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(produto.getUnidade() != null ? produto.getUnidade().getNome() : "");
            cell4.setCellStyle(dataStyle);

            Cell cell5 = row.createCell(5);
            if (produto.getDataDeVencimento() != null) {
                cell5.setCellValue(produto.getDataDeVencimento().format(dateFormatter));
            }
            cell5.setCellStyle(dateStyle);
        }

        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 8000);
        sheet.setColumnWidth(3, 3000);
        sheet.setColumnWidth(4, 3000);
        sheet.setColumnWidth(5, 4000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioMovimentacaoExcel(List<MovimentacaoEstoque> movimentacoes) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Movimentações");

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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório de Movimentações de Estoque");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        Cell infoCell1 = infoRow1.createCell(0);
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        infoCell1.setCellValue("Data de Emissão: " + agora.format(formatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Cell infoCell2 = infoRow1.createCell(4);
        infoCell2.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 4, 6));

        Row infoRow2 = sheet.createRow(rowNum++);
        Cell totalCell = infoRow2.createCell(0);
        totalCell.setCellValue("Total de Movimentações: " + movimentacoes.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Produto", "Tipo", "Qtd. Movimentada", "Qtd. Anterior", "Qtd. Posterior", "Data"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (MovimentacaoEstoque mov : movimentacoes) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(mov.getId());
            cell0.setCellStyle(centerStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(mov.getProduto() != null ? mov.getProduto().getNome() : "");
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(mov.getTipo() != null ? mov.getTipo().toString() : "");
            cell2.setCellStyle(dataStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(mov.getQuantidadeMovimentada());
            cell3.setCellStyle(centerStyle);

            Cell cell4 = row.createCell(4);
            cell4.setCellValue(mov.getQuantidadeAnterior());
            cell4.setCellStyle(centerStyle);

            Cell cell5 = row.createCell(5);
            cell5.setCellValue(mov.getQuantidadePosterior());
            cell5.setCellStyle(centerStyle);

            Cell cell6 = row.createCell(6);
            if (mov.getDataMovimentacao() != null) {
                cell6.setCellValue(mov.getDataMovimentacao().format(dateTimeFormatter));
            }
            cell6.setCellStyle(dateStyle);
        }

        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 3500);
        sheet.setColumnWidth(5, 3500);
        sheet.setColumnWidth(6, 4500);

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