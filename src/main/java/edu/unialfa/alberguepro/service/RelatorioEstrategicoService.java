package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.dto.AcolhidoDTO;
import edu.unialfa.alberguepro.dto.EvolucaoOcupacaoDTO;
import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import edu.unialfa.alberguepro.repository.CadastroAcolhidoRepository;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import edu.unialfa.alberguepro.repository.MovimentacaoEstoqueRepository;
import edu.unialfa.alberguepro.repository.VagaRepository;
import edu.unialfa.alberguepro.repository.LeitoRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RelatorioEstrategicoService {

    @Autowired
    private CadastroAcolhidoRepository cadastroAcolhidoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private ControlePatrimonioRepository controlePatrimonioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    // ==================== RELATÓRIO DE ACOLHIDOS POR PERÍODO ====================

    public ByteArrayInputStream gerarRelatorioAcolhidosPorPeriodoPdf(LocalDate dataInicio, LocalDate dataFim) throws JRException {
        List<CadastroAcolhido> acolhidos = cadastroAcolhidoRepository.findByDataIngressoBetweenOrderByDataIngressoAsc(dataInicio, dataFim);
        List<AcolhidoDTO> acolhidosDTO = acolhidos.stream().map(AcolhidoDTO::new).collect(Collectors.toList());

        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_acolhido.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(acolhidosDTO);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        Map<String, Object> parameters = criarParametrosComuns();
        parameters.put("TOTAL_REGISTROS", acolhidos.size());
        parameters.put("DATA_INICIO", java.sql.Date.valueOf(dataInicio));
        parameters.put("DATA_FIM", java.sql.Date.valueOf(dataFim));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioAcolhidosPorPeriodoExcel(LocalDate dataInicio, LocalDate dataFim) throws IOException {
        List<CadastroAcolhido> acolhidos = cadastroAcolhidoRepository.findByDataIngressoBetweenOrderByDataIngressoAsc(dataInicio, dataFim);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Acolhidos por Período");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        // Subtítulo
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório de Entradas de Acolhidos por Período");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 8));

        // Informações do relatório
        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Período: " + dataInicio.format(dateFormatter) + " a " + dataFim.format(dateFormatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        Cell infoCell2 = infoRow1.createCell(5);
        infoCell2.setCellValue("Emitido em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 5, 8));

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Total de Entradas: " + acolhidos.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        Cell userCell = infoRow2.createCell(5);
        userCell.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 5, 8));

        rowNum++;

        // Cabeçalho
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Nome", "Data Nasc.", "Idade", "Sexo", "Naturalidade", "RG", "CPF", "Data Ingresso"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dados
        for (CadastroAcolhido acolhido : acolhidos) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(acolhido.getId());
            row.getCell(0).setCellStyle(centerStyle);
            
            row.createCell(1).setCellValue(acolhido.getNome());
            row.getCell(1).setCellStyle(dataStyle);
            
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
            if (acolhido.getSexo() != null) {
                cell4.setCellValue(acolhido.getSexo().name());
            }
            cell4.setCellStyle(centerStyle);
            
            row.createCell(5).setCellValue(acolhido.getNaturalidade());
            row.getCell(5).setCellStyle(dataStyle);
            
            row.createCell(6).setCellValue(acolhido.getRg());
            row.getCell(6).setCellStyle(dataStyle);
            
            row.createCell(7).setCellValue(acolhido.getCpf());
            row.getCell(7).setCellStyle(dataStyle);
            
            Cell cell8 = row.createCell(8);
            if (acolhido.getDataIngresso() != null) {
                cell8.setCellValue(acolhido.getDataIngresso().format(dateFormatter));
            }
            cell8.setCellStyle(dateStyle);
        }

        // Ajustar largura das colunas
        sheet.setColumnWidth(0, 1500);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 2000);
        sheet.setColumnWidth(4, 2500);
        sheet.setColumnWidth(5, 5000);
        sheet.setColumnWidth(6, 3500);
        sheet.setColumnWidth(7, 4000);
        sheet.setColumnWidth(8, 3500);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==================== RELATÓRIO DE MOVIMENTAÇÃO DE ESTOQUE POR PERÍODO ====================

    public ByteArrayInputStream gerarRelatorioMovimentacaoEstoquePdf(LocalDate dataInicio, LocalDate dataFim, String tipo) throws JRException {
        LocalDateTime dataHoraInicio = dataInicio.atStartOfDay();
        LocalDateTime dataHoraFim = dataFim.atTime(LocalTime.MAX);

        List<MovimentacaoEstoque> movimentacoes;
        if (tipo != null && !tipo.isEmpty() && !tipo.equals("TODOS")) {
            MovimentacaoEstoque.TipoMovimentacao tipoEnum = MovimentacaoEstoque.TipoMovimentacao.valueOf(tipo);
            movimentacoes = movimentacaoEstoqueRepository.findByDataMovimentacaoBetweenAndTipo(dataHoraInicio, dataHoraFim, tipoEnum);
        } else {
            movimentacoes = movimentacaoEstoqueRepository.findByDataMovimentacaoBetween(dataHoraInicio, dataHoraFim);
        }

        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_movimentacao_estoque.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(movimentacoes);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        Map<String, Object> parameters = criarParametrosComuns();
        parameters.put("TOTAL_REGISTROS", movimentacoes.size());
        parameters.put("DATA_INICIO", java.sql.Date.valueOf(dataInicio));
        parameters.put("DATA_FIM", java.sql.Date.valueOf(dataFim));
        parameters.put("TIPO_FILTRO", tipo != null && !tipo.isEmpty() ? tipo : "TODOS");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioMovimentacaoEstoqueExcel(LocalDate dataInicio, LocalDate dataFim, String tipo) throws IOException {
        LocalDateTime dataHoraInicio = dataInicio.atStartOfDay();
        LocalDateTime dataHoraFim = dataFim.atTime(LocalTime.MAX);

        List<MovimentacaoEstoque> movimentacoes;
        if (tipo != null && !tipo.isEmpty() && !tipo.equals("TODOS")) {
            MovimentacaoEstoque.TipoMovimentacao tipoEnum = MovimentacaoEstoque.TipoMovimentacao.valueOf(tipo);
            movimentacoes = movimentacaoEstoqueRepository.findByDataMovimentacaoBetweenAndTipo(dataHoraInicio, dataHoraFim, tipoEnum);
        } else {
            movimentacoes = movimentacaoEstoqueRepository.findByDataMovimentacaoBetween(dataHoraInicio, dataHoraFim);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Movimentação Estoque");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Subtítulo
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        String tipoDesc = (tipo != null && !tipo.isEmpty() && !tipo.equals("TODOS")) ? " - Tipo: " + tipo : "";
        subtitleCell.setCellValue("Relatório de Movimentação de Estoque por Período" + tipoDesc);
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // Informações
        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Período: " + dataInicio.format(dateFormatter) + " a " + dataFim.format(dateFormatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Cell infoCell2 = infoRow1.createCell(4);
        infoCell2.setCellValue("Emitido em: " + LocalDateTime.now().format(dateTimeFormatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 4, 7));

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Total de Movimentações: " + movimentacoes.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        Cell userCell = infoRow2.createCell(4);
        userCell.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 4, 7));

        rowNum++;

        // Cabeçalho
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Data/Hora", "Produto", "Tipo", "Qtd. Movimentada", "Qtd. Anterior", "Qtd. Posterior", "Usuário", "Observação"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dados
        for (MovimentacaoEstoque mov : movimentacoes) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            if (mov.getDataMovimentacao() != null) {
                cell0.setCellValue(mov.getDataMovimentacao().format(dateTimeFormatter));
            }
            cell0.setCellStyle(dateStyle);
            
            row.createCell(1).setCellValue(mov.getProduto() != null ? mov.getProduto().getNome() : "");
            row.getCell(1).setCellStyle(dataStyle);
            
            row.createCell(2).setCellValue(mov.getTipo() != null ? mov.getTipo().name() : "");
            row.getCell(2).setCellStyle(centerStyle);
            
            row.createCell(3).setCellValue(mov.getQuantidadeMovimentada() != null ? mov.getQuantidadeMovimentada() : 0);
            row.getCell(3).setCellStyle(centerStyle);
            
            row.createCell(4).setCellValue(mov.getQuantidadeAnterior() != null ? mov.getQuantidadeAnterior() : 0);
            row.getCell(4).setCellStyle(centerStyle);
            
            row.createCell(5).setCellValue(mov.getQuantidadePosterior() != null ? mov.getQuantidadePosterior() : 0);
            row.getCell(5).setCellStyle(centerStyle);
            
            row.createCell(6).setCellValue(mov.getUsuario() != null ? mov.getUsuario().getUsername() : "");
            row.getCell(6).setCellStyle(dataStyle);
            
            row.createCell(7).setCellValue(mov.getObservacao() != null ? mov.getObservacao() : "");
            row.getCell(7).setCellStyle(dataStyle);
        }

        // Ajustar largura
        sheet.setColumnWidth(0, 4500);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 3500);
        sheet.setColumnWidth(3, 3500);
        sheet.setColumnWidth(4, 3000);
        sheet.setColumnWidth(5, 3000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 8000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==================== RELATÓRIO DE PATRIMÔNIO POR PERÍODO DE AQUISIÇÃO ====================

    public ByteArrayInputStream gerarRelatorioPatrimonioPorPeriodoPdf(LocalDate dataInicio, LocalDate dataFim, String status) throws JRException {
        List<ControlePatrimonio> patrimonios;
        if (status != null && !status.isEmpty()) {
            patrimonios = controlePatrimonioRepository.findByDataAquisicaoBetweenAndStatus(dataInicio, dataFim, status);
        } else {
            patrimonios = controlePatrimonioRepository.findByDataAquisicaoBetween(dataInicio, dataFim);
        }

        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_patrimonio.jrxml");
        if (inputStream == null) throw new RuntimeException("Arquivo JRXML não encontrado!");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(patrimonios);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        Map<String, Object> parameters = criarParametrosComuns();
        parameters.put("TOTAL_REGISTROS", patrimonios.size());
        parameters.put("DATA_INICIO", java.sql.Date.valueOf(dataInicio));
        parameters.put("DATA_FIM", java.sql.Date.valueOf(dataFim));
        parameters.put("STATUS_FILTRO", status != null && !status.isEmpty() ? status : "TODOS");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioPatrimonioPorPeriodoExcel(LocalDate dataInicio, LocalDate dataFim, String status) throws IOException {
        List<ControlePatrimonio> patrimonios;
        if (status != null && !status.isEmpty()) {
            patrimonios = controlePatrimonioRepository.findByDataAquisicaoBetweenAndStatus(dataInicio, dataFim, status);
        } else {
            patrimonios = controlePatrimonioRepository.findByDataAquisicaoBetween(dataInicio, dataFim);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Patrimônio por Período");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        // Subtítulo
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        String statusDesc = (status != null && !status.isEmpty()) ? " - Status: " + status : "";
        subtitleCell.setCellValue("Relatório de Patrimônio por Período de Aquisição" + statusDesc);
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        // Informações
        rowNum++;
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Período: " + dataInicio.format(dateFormatter) + " a " + dataFim.format(dateFormatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell infoCell2 = infoRow1.createCell(3);
        infoCell2.setCellValue("Emitido em: " + LocalDateTime.now().format(dateTimeFormatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Total de Patrimônios: " + patrimonios.size());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell userCell = infoRow2.createCell(3);
        userCell.setCellValue("Usuário: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));

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
            
            row.createCell(5).setCellValue(patrimonio.getObservacao());
            row.getCell(5).setCellStyle(dataStyle);
        }

        // Ajustar largura
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 3500);
        sheet.setColumnWidth(2, 3500);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 3000);
        sheet.setColumnWidth(5, 8000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Map<String, Object> criarParametrosComuns() {
        java.time.ZoneId saoPauloZone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.ZonedDateTime agora = java.time.ZonedDateTime.now(saoPauloZone);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("DATA_EMISSAO", java.util.Date.from(agora.toInstant()));
        parameters.put("REPORT_TIME_ZONE", java.util.TimeZone.getTimeZone(saoPauloZone));
        parameters.put("USUARIO_EMISSOR", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        
        return parameters;
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
        font.setFontHeightInPoints((short) 14);
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

    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.0%"));
        return style;
    }

    // ==================== RELATÓRIO DE EVOLUÇÃO DE OCUPAÇÃO ====================

    public ByteArrayInputStream gerarRelatorioEvolucaoOcupacaoPdf(String periodo) throws IOException {
        List<EvolucaoOcupacaoDTO> dados = obterDadosEvolucaoOcupacao(periodo);
        
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            com.itextpdf.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

            Paragraph title = new Paragraph("AlberguePro", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph subtitle = new Paragraph("Relatório Estratégico - Evolução de Ocupação", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            String periodoTexto = obterTextoPeriodo(periodo);
            Paragraph periodoP = new Paragraph("Período: " + periodoTexto, normalFont);
            periodoP.setAlignment(Element.ALIGN_CENTER);
            periodoP.setSpacingAfter(20);
            document.add(periodoP);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Paragraph info = new Paragraph(
                "Data de Emissão: " + LocalDateTime.now().format(formatter) + 
                " | Usuário: " + obterUsuarioLogado(), 
                normalFont
            );
            info.setAlignment(Element.ALIGN_LEFT);
            info.setSpacingAfter(15);
            document.add(info);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            String[] headers = {"Período", "Entradas", "Saídas", "Leitos Ocupados", "Taxa Ocupação", "Saldo"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new BaseColor(52, 152, 219));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(8);
                table.addCell(cell);
            }

            for (EvolucaoOcupacaoDTO dto : dados) {
                table.addCell(createPdfCell(dto.getPeriodo(), normalFont, Element.ALIGN_CENTER));
                table.addCell(createPdfCell(String.valueOf(dto.getEntradas()), normalFont, Element.ALIGN_CENTER));
                table.addCell(createPdfCell(String.valueOf(dto.getSaidas()), normalFont, Element.ALIGN_CENTER));
                table.addCell(createPdfCell(String.valueOf(dto.getLeitosOcupados()), normalFont, Element.ALIGN_CENTER));
                table.addCell(createPdfCell(String.format("%.1f%%", dto.getTaxaOcupacao()), normalFont, Element.ALIGN_CENTER));
                
                int saldo = dto.getEntradas() - dto.getSaidas();
                String saldoStr = (saldo >= 0 ? "+" : "") + saldo;
                table.addCell(createPdfCell(saldoStr, normalFont, Element.ALIGN_CENTER));
            }

            document.add(table);

            long totalEntradas = dados.stream().mapToLong(EvolucaoOcupacaoDTO::getEntradas).sum();
            long totalSaidas = dados.stream().mapToLong(EvolucaoOcupacaoDTO::getSaidas).sum();
            double mediaOcupacao = dados.stream().mapToDouble(EvolucaoOcupacaoDTO::getTaxaOcupacao).average().orElse(0);

            Paragraph resumo = new Paragraph("\n\nResumo do Período:", subtitleFont);
            resumo.setSpacingAfter(10);
            document.add(resumo);

            document.add(new Paragraph("Total de Entradas: " + totalEntradas, normalFont));
            document.add(new Paragraph("Total de Saídas: " + totalSaidas, normalFont));
            document.add(new Paragraph("Taxa Média de Ocupação: " + String.format("%.1f%%", mediaOcupacao), normalFont));
            document.add(new Paragraph("Saldo do Período: " + (totalEntradas - totalSaidas), normalFont));

            document.close();

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private PdfPCell createPdfCell(String content, com.itextpdf.text.Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }

    public ByteArrayInputStream gerarRelatorioEvolucaoOcupacaoExcel(String periodo) throws IOException {
        List<EvolucaoOcupacaoDTO> dados = obterDadosEvolucaoOcupacao(periodo);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Evolução de Ocupação");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle centerStyle = createCenterStyle(workbook);
        CellStyle percentStyle = createPercentStyle(workbook);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("AlberguePro");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Relatório Estratégico - Evolução de Ocupação");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        rowNum++;
        Row periodoRow = sheet.createRow(rowNum++);
        Cell periodoCell = periodoRow.createCell(0);
        periodoCell.setCellValue("Período: " + obterTextoPeriodo(periodo));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        Row infoRow = sheet.createRow(rowNum++);
        Cell infoCell1 = infoRow.createCell(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        infoCell1.setCellValue("Data de Emissão: " + LocalDateTime.now().format(formatter));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Cell infoCell2 = infoRow.createCell(3);
        infoCell2.setCellValue("Usuário: " + obterUsuarioLogado());
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 3, 5));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Período", "Entradas", "Saídas", "Leitos Ocupados", "Taxa Ocupação", "Saldo"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (EvolucaoOcupacaoDTO dto : dados) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(dto.getPeriodo());
            cell0.setCellStyle(centerStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(dto.getEntradas());
            cell1.setCellStyle(centerStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(dto.getSaidas());
            cell2.setCellStyle(centerStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(dto.getLeitosOcupados());
            cell3.setCellStyle(centerStyle);
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(dto.getTaxaOcupacao() / 100);
            cell4.setCellStyle(percentStyle);
            
            Cell cell5 = row.createCell(5);
            int saldo = dto.getEntradas() - dto.getSaidas();
            cell5.setCellValue(saldo);
            cell5.setCellStyle(centerStyle);
        }

        rowNum += 2;
        Row resumoHeaderRow = sheet.createRow(rowNum++);
        Cell resumoHeaderCell = resumoHeaderRow.createCell(0);
        resumoHeaderCell.setCellValue("Resumo do Período");
        resumoHeaderCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        long totalEntradas = dados.stream().mapToLong(EvolucaoOcupacaoDTO::getEntradas).sum();
        long totalSaidas = dados.stream().mapToLong(EvolucaoOcupacaoDTO::getSaidas).sum();
        double mediaOcupacao = dados.stream().mapToDouble(EvolucaoOcupacaoDTO::getTaxaOcupacao).average().orElse(0);

        Row resumoRow1 = sheet.createRow(rowNum++);
        Cell resumoCell1 = resumoRow1.createCell(0);
        resumoCell1.setCellValue("Total de Entradas: " + totalEntradas);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Row resumoRow2 = sheet.createRow(rowNum++);
        Cell resumoCell2 = resumoRow2.createCell(0);
        resumoCell2.setCellValue("Total de Saídas: " + totalSaidas);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Row resumoRow3 = sheet.createRow(rowNum++);
        Cell resumoCell3 = resumoRow3.createCell(0);
        resumoCell3.setCellValue("Taxa Média de Ocupação: " + String.format("%.1f%%", mediaOcupacao));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        Row resumoRow4 = sheet.createRow(rowNum++);
        Cell resumoCell4 = resumoRow4.createCell(0);
        resumoCell4.setCellValue("Saldo do Período: " + (totalEntradas - totalSaidas));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 2));

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 3000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private List<EvolucaoOcupacaoDTO> obterDadosEvolucaoOcupacao(String periodo) {
        List<EvolucaoOcupacaoDTO> dados = new ArrayList<>();
        long totalLeitos = leitoRepository.count();
        
        LocalDate hoje = LocalDate.now();

        switch (periodo.toLowerCase()) {
            case "semanal":
                for (int i = 11; i >= 0; i--) {
                    LocalDate inicioSemana = hoje.minusWeeks(i);
                    LocalDate fimSemana = inicioSemana.plusDays(6);
                    
                    long entradas = vagaRepository.countEntradasPorPeriodo(inicioSemana, fimSemana);
                    long saidas = vagaRepository.countSaidasPorPeriodo(inicioSemana, fimSemana);
                    long leitosOcupados = vagaRepository.countLeitosOcupadosNaData(fimSemana);
                    
                    double taxaOcupacao = totalLeitos > 0 ? (leitosOcupados * 100.0 / totalLeitos) : 0;
                    
                    WeekFields weekFields = WeekFields.of(Locale.getDefault());
                    int semana = inicioSemana.get(weekFields.weekOfWeekBasedYear());
                    String periodoStr = String.format("Sem %d/%d", semana, inicioSemana.getYear());
                    
                    dados.add(new EvolucaoOcupacaoDTO(periodoStr, (int)entradas, (int)saidas, 
                                                      (int)leitosOcupados, taxaOcupacao));
                }
                break;
                
            case "mensal":
                for (int i = 11; i >= 0; i--) {
                    YearMonth yearMonth = YearMonth.from(hoje.minusMonths(i));
                    LocalDate inicioMes = yearMonth.atDay(1);
                    LocalDate fimMes = yearMonth.atEndOfMonth();
                    
                    long entradas = vagaRepository.countEntradasPorPeriodo(inicioMes, fimMes);
                    long saidas = vagaRepository.countSaidasPorPeriodo(inicioMes, fimMes);
                    long leitosOcupados = vagaRepository.countLeitosOcupadosNaData(fimMes);
                    
                    double taxaOcupacao = totalLeitos > 0 ? (leitosOcupados * 100.0 / totalLeitos) : 0;
                    
                    String nomeMes = inicioMes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
                    String periodoStr = nomeMes + "/" + inicioMes.getYear();
                    
                    dados.add(new EvolucaoOcupacaoDTO(periodoStr, (int)entradas, (int)saidas, 
                                                      (int)leitosOcupados, taxaOcupacao));
                }
                break;
                
            case "trimestral":
                for (int i = 7; i >= 0; i--) {
                    LocalDate inicioTrimestre = hoje.minusMonths(i * 3).withDayOfMonth(1);
                    LocalDate fimTrimestre = inicioTrimestre.plusMonths(3).minusDays(1);
                    
                    if (fimTrimestre.isAfter(hoje)) {
                        fimTrimestre = hoje;
                    }
                    
                    long entradas = vagaRepository.countEntradasPorPeriodo(inicioTrimestre, fimTrimestre);
                    long saidas = vagaRepository.countSaidasPorPeriodo(inicioTrimestre, fimTrimestre);
                    long leitosOcupados = vagaRepository.countLeitosOcupadosNaData(fimTrimestre);
                    
                    double taxaOcupacao = totalLeitos > 0 ? (leitosOcupados * 100.0 / totalLeitos) : 0;
                    
                    int trimestre = (inicioTrimestre.getMonthValue() - 1) / 3 + 1;
                    String periodoStr = String.format("T%d/%d", trimestre, inicioTrimestre.getYear());
                    
                    dados.add(new EvolucaoOcupacaoDTO(periodoStr, (int)entradas, (int)saidas, 
                                                      (int)leitosOcupados, taxaOcupacao));
                }
                break;
                
            case "anual":
                for (int i = 4; i >= 0; i--) {
                    int ano = hoje.getYear() - i;
                    LocalDate inicioAno = LocalDate.of(ano, 1, 1);
                    LocalDate fimAno = LocalDate.of(ano, 12, 31);
                    
                    if (fimAno.isAfter(hoje)) {
                        fimAno = hoje;
                    }
                    
                    long entradas = vagaRepository.countEntradasPorPeriodo(inicioAno, fimAno);
                    long saidas = vagaRepository.countSaidasPorPeriodo(inicioAno, fimAno);
                    long leitosOcupados = vagaRepository.countLeitosOcupadosNaData(fimAno);
                    
                    double taxaOcupacao = totalLeitos > 0 ? (leitosOcupados * 100.0 / totalLeitos) : 0;
                    
                    dados.add(new EvolucaoOcupacaoDTO(String.valueOf(ano), (int)entradas, (int)saidas, 
                                                      (int)leitosOcupados, taxaOcupacao));
                }
                break;
        }

        return dados;
    }

    private String obterTextoPeriodo(String periodo) {
        switch (periodo.toLowerCase()) {
            case "semanal": return "Últimas 12 Semanas";
            case "mensal": return "Últimos 12 Meses";
            case "trimestral": return "Últimos 8 Trimestres";
            case "anual": return "Últimos 5 Anos";
            default: return periodo;
        }
    }

    private String obterUsuarioLogado() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "Sistema";
        }
    }
}
