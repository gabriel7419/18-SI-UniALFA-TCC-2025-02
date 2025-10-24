package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Produto;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class RelatorioService {

    public ByteArrayInputStream gerarRelatorioPdf(List<Produto> produtos) throws JRException {
        InputStream inputStream = getClass().getResourceAsStream("/relatorios/relatorio_estoque.jrxml");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(produtos);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioXlsx(List<Produto> produtos) throws IOException {
        String[] COLUNAS = {"ID", "Nome", "Tipo", "Quantidade", "Unidade", "Data de Vencimento"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet("Produtos");

            // Estilo para o cabeçalho
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            // Criar linha de cabeçalho
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUNAS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUNAS[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Estilo para a data
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            // Preencher dados
            int rowIdx = 1;
            for (Produto produto : produtos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(produto.getId());
                row.createCell(1).setCellValue(produto.getNome());
                row.createCell(2).setCellValue(produto.getTipo());
                row.createCell(3).setCellValue(produto.getQuantidade());
                if (produto.getUnidade() != null) {
                    row.createCell(4).setCellValue(produto.getUnidade().getNome());
                } else {
                    row.createCell(4).setCellValue("");
                }
                
                if (produto.getDataDeVencimento() != null) {
                    Cell dateCell = row.createCell(5);
                    Date date = Date.from(produto.getDataDeVencimento().atStartOfDay(ZoneId.systemDefault()).toInstant());
                    dateCell.setCellValue(date);
                    dateCell.setCellStyle(dateCellStyle);
                } else {
                    row.createCell(5).setCellValue("");
                }
            }

            // Auto-ajuste das colunas
            for (int i = 0; i < COLUNAS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
