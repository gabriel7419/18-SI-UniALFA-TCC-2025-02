package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.repository.CadastroAcolhidoRepository;
import edu.unialfa.alberguepro.dto.AcolhidoDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream gerarRelatorioXlsx() throws IOException {
        List<AcolhidoDTO> acolhidosDTO = repository.findAll()
                .stream()
                .map(AcolhidoDTO::new)
                .collect(Collectors.toList());

        String[] COLUNAS = {"ID", "Nome", "Data Nascimento", "Idade", "Sexo", "Naturalidade", "RG", "CPF", "Entrada", "Saída"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Acolhidos");

            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUNAS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUNAS[col]);
            }

            int rowIdx = 1;
            for (AcolhidoDTO acolhido : acolhidosDTO) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(acolhido.getId());
                row.createCell(1).setCellValue(acolhido.getNome());
                row.createCell(2).setCellValue(acolhido.getDataNascimento() != null ? acolhido.getDataNascimento().toString() : "");
                row.createCell(3).setCellValue(acolhido.getIdade() != null ? acolhido.getIdade() : 0);
                row.createCell(4).setCellValue(acolhido.getSexo());
                row.createCell(5).setCellValue(acolhido.getNaturalidade());
                row.createCell(6).setCellValue(acolhido.getRg());
                row.createCell(7).setCellValue(acolhido.getCpf());

                Cell cellEntrada = row.createCell(8);
                if (acolhido.getDataIngresso() != null) {
                    cellEntrada.setCellValue(acolhido.getDataIngresso());
                    cellEntrada.setCellStyle(dateCellStyle);
                }

                Cell cellSaida = row.createCell(9);
                if (acolhido.getDataSaida() != null) {
                    cellSaida.setCellValue(acolhido.getDataSaida());
                    cellSaida.setCellStyle(dateCellStyle);
                }
            }

            for (int i = 0; i < COLUNAS.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

}
