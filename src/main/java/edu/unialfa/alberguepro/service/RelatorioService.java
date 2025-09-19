package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Produto;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUNAS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUNAS[col]);
            }

            int rowIdx = 1;
            for (Produto produto : produtos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(produto.getId());
                row.createCell(1).setCellValue(produto.getNome());
                row.createCell(2).setCellValue(produto.getTipo());
                row.createCell(3).setCellValue(produto.getQuantidade());
                row.createCell(4).setCellValue(produto.getUnidade().getNome());
                row.createCell(5).setCellValue(produto.getDataDeVencimento().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
