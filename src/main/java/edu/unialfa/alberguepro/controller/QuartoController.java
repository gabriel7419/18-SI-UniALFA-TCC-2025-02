package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.QuartoRepository;
import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.repository.VagaRepository;
import edu.unialfa.alberguepro.service.QuartoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/quarto")
@Slf4j
public class QuartoController {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private QuartoService service;

    @GetMapping("/novo")
    public String iniciarCadastro(Model model) {
        model.addAttribute("quarto", new Quarto());
        return "Quarto/form"; // Sua página Thymeleaf
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Quarto quarto, BindingResult result, RedirectAttributes attributes, Model model) {

        // Validação de preenchimento obrigatório via Bean Validation
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return "Quarto/form";
        }

        try {
            service.salvar(quarto);
            attributes.addFlashAttribute("successMessage", "Quarto salvo com sucesso!");
            return "redirect:/quarto/listar";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "Quarto/form";
        }

    }

    private void adicionarContagemDeLeitosOcupados(Model model) {

        List<Object[]> contagemLeitos = vagaRepository.countOccupiedBedsByRoom();

        // 💡 DEBUG: Imprima o resultado da consulta SQL
        log.info("Resultado da contagem de leitos ocupados (Quarto / Qtd):");
        for (Object[] resultado : contagemLeitos) {
            log.info("Quarto: {} | Ocupados: {}", resultado[0], resultado[1]);
        }
        // FIM DEBUG

        Map<String, Long> leitosOcupadosPorQuarto = contagemLeitos.stream()
                .collect(Collectors.toMap(

                        array -> (String) array[0],

                        array -> (Long) array[1]
                ));

        model.addAttribute("leitosOcupadosPorQuarto", leitosOcupadosPorQuarto);
    }

    @GetMapping("/listar")
    public String listarquartos(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "15") int size,
                                @RequestParam(defaultValue = "numeroQuarto") String sort,
                                @RequestParam(defaultValue = "asc") String dir) {
        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        org.springframework.data.domain.Page<Quarto> pageResult = service.listarTodosPaginado(pageable);
        
        model.addAttribute("quartos", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        adicionarContagemDeLeitosOcupados(model);
        return "Quarto/index";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            service.deletarPorId(id);
            attributes.addFlashAttribute("successMessage", "Quarto removido com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao remover o quarto: " + e.getMessage());
        }
        return "redirect:/quarto/listar";
    }

    @GetMapping("/pesquisar")
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro,
                              @RequestParam(value = "capacidadeMin", required = false) Integer capacidadeMin,
                              @RequestParam(value = "disponibilidade", required = false) String disponibilidade,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "15") int size,
                              @RequestParam(defaultValue = "numeroQuarto") String sort,
                              @RequestParam(defaultValue = "asc") String dir,
                              Model model) {
        
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        
        org.springframework.data.domain.Page<Quarto> pageResult = service.buscarComFiltros(filtro, capacidadeMin, disponibilidade, pageable);
        
        model.addAttribute("quartos", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("filtro", filtro);
        model.addAttribute("capacidadeMin", capacidadeMin);
        model.addAttribute("disponibilidade", disponibilidade);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        adicionarContagemDeLeitosOcupados(model);
        return "Quarto/index";
    }

    @GetMapping("/relatorio/ocupacao-pdf")
    public ResponseEntity<byte[]> relatorioOcupacaoPdf() {
        try {
            List<Quarto> quartos = quartoRepository.findAll();
            Map<String, Long> leitosOcupadosPorQuarto = vagaRepository.countOccupiedBedsByRoom()
                    .stream()
                    .collect(Collectors.toMap(
                            obj -> (String) obj[0],
                            obj -> ((Number) obj[1]).longValue()
                    ));

            long totalLeitos = quartos.stream().mapToLong(q -> q.getLeitos().size()).sum();
            long totalOcupados = leitosOcupadosPorQuarto.values().stream().mapToLong(Long::longValue).sum();
            long totalLivres = totalLeitos - totalOcupados;
            double taxaOcupacao = totalLeitos > 0 ? (totalOcupados * 100.0 / totalLeitos) : 0;

            // Preparar dados para o relatório
            List<Map<String, Object>> dados = new java.util.ArrayList<>();
            for (Quarto quarto : quartos) {
                Map<String, Object> item = new java.util.HashMap<>();
                long leitosQuarto = quarto.getLeitos().size();
                long ocupados = leitosOcupadosPorQuarto.getOrDefault(quarto.getNumeroQuarto(), 0L);
                long livres = leitosQuarto - ocupados;
                double taxa = leitosQuarto > 0 ? (ocupados * 100.0 / leitosQuarto) : 0;
                
                item.put("numeroQuarto", quarto.getNumeroQuarto());
                item.put("totalLeitos", (int)leitosQuarto);
                item.put("leitosOcupados", (int)ocupados);
                item.put("leitosLivres", (int)livres);
                item.put("taxaOcupacao", taxa);
                dados.add(item);
            }

            // Carregar template JRXML
            InputStream jrxmlStream = getClass().getResourceAsStream("/relatorios/relatorio_ocupacao.jrxml");
            if (jrxmlStream == null) {
                throw new RuntimeException("Template JRXML não encontrado: /relatorios/relatorio_ocupacao.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport = 
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(jrxmlStream);

            // Parâmetros
            Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("TOTAL_LEITOS", totalLeitos);
            parametros.put("TOTAL_OCUPADOS", totalOcupados);
            parametros.put("TOTAL_LIVRES", totalLivres);
            parametros.put("TAXA_OCUPACAO", taxaOcupacao);

            // DataSource
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource = 
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dados);

            // Preencher relatório
            net.sf.jasperreports.engine.JasperPrint jasperPrint = 
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            // Exportar para PDF
            byte[] pdf = net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(jasperPrint);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition.inline()
                    .filename("relatorio-ocupacao.pdf")
                    .build());

            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório PDF de ocupação", e);
            return ResponseEntity.status(500).body(("Erro: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/relatorio/ocupacao-excel")
    @ResponseBody
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> relatorioOcupacaoExcel() {
        try {
            List<Quarto> quartos = quartoRepository.findAll();
            Map<String, Long> leitosOcupadosPorQuarto = vagaRepository.countOccupiedBedsByRoom()
                    .stream()
                    .collect(Collectors.toMap(
                            obj -> (String) obj[0],
                            obj -> ((Number) obj[1]).longValue()
                    ));

            // Criar Excel
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Relatório de Ocupação");

            // Estilos
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Cabeçalho
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = {"Quarto", "Total Leitos", "Ocupados", "Livres", "Taxa Ocupação (%)"};
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dados
            int rowNum = 1;
            long totalLeitos = 0;
            long totalOcupados = 0;

            for (Quarto quarto : quartos) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                long leitosQuarto = quarto.getLeitos().size();
                long ocupados = leitosOcupadosPorQuarto.getOrDefault(quarto.getNumeroQuarto(), 0L);
                long livres = leitosQuarto - ocupados;
                double taxa = leitosQuarto > 0 ? (ocupados * 100.0 / leitosQuarto) : 0;

                totalLeitos += leitosQuarto;
                totalOcupados += ocupados;

                row.createCell(0).setCellValue(quarto.getNumeroQuarto());
                row.createCell(1).setCellValue(leitosQuarto);
                row.createCell(2).setCellValue(ocupados);
                row.createCell(3).setCellValue(livres);
                row.createCell(4).setCellValue(String.format("%.1f%%", taxa));
            }

            // Total
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowNum);
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            
            org.apache.poi.ss.usermodel.Cell totalCell = totalRow.createCell(0);
            totalCell.setCellValue("TOTAL");
            totalCell.setCellStyle(boldStyle);
            totalRow.createCell(1).setCellValue(totalLeitos);
            totalRow.createCell(2).setCellValue(totalOcupados);
            totalRow.createCell(3).setCellValue(totalLeitos - totalOcupados);
            double taxaTotal = totalLeitos > 0 ? (totalOcupados * 100.0 / totalLeitos) : 0;
            totalRow.createCell(4).setCellValue(String.format("%.1f%%", taxaTotal));

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(outputStream.toByteArray());

            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-ocupacao.xlsx")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("Erro ao gerar relatório Excel de ocupação", e);
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }
}