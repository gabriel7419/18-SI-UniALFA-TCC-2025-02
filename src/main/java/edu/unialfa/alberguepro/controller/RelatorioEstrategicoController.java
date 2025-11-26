package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.service.RelatorioEstrategicoService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/relatorios/estrategicos")
public class RelatorioEstrategicoController {

    @Autowired
    private RelatorioEstrategicoService service;

    @GetMapping
    public String index() {
        return "relatorios/estrategicos/index";
    }

    @GetMapping("/acolhidos")
    public String acolhidos(Model model) {
        model.addAttribute("dataInicio", LocalDate.now().minusMonths(1));
        model.addAttribute("dataFim", LocalDate.now());
        return "relatorios/estrategicos/acolhidos";
    }

    @GetMapping("/acolhidos/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioAcolhidosPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) throws JRException {
        
        ByteArrayInputStream bis = service.gerarRelatorioAcolhidosPorPeriodoPdf(dataInicio, dataFim);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_acolhidos_periodo.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/acolhidos/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioAcolhidosExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) throws IOException {
        
        ByteArrayInputStream bis = service.gerarRelatorioAcolhidosPorPeriodoExcel(dataInicio, dataFim);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_acolhidos_periodo.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/movimentacao-estoque")
    public String movimentacaoEstoque(Model model) {
        model.addAttribute("dataInicio", LocalDate.now().minusMonths(1));
        model.addAttribute("dataFim", LocalDate.now());
        return "relatorios/estrategicos/movimentacao-estoque";
    }

    @GetMapping("/movimentacao-estoque/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioMovimentacaoEstoquePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipo) throws JRException {
        
        ByteArrayInputStream bis = service.gerarRelatorioMovimentacaoEstoquePdf(dataInicio, dataFim, tipo);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_movimentacao_estoque_periodo.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/movimentacao-estoque/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioMovimentacaoEstoqueExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipo) throws IOException {
        
        ByteArrayInputStream bis = service.gerarRelatorioMovimentacaoEstoqueExcel(dataInicio, dataFim, tipo);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_movimentacao_estoque_periodo.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/vagas")
    public String vagas(Model model) {
        return "relatorios/estrategicos/evolucao-ocupacao";
    }

    @GetMapping("/evolucao-ocupacao/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioEvolucaoOcupacaoPdf(
            @RequestParam String periodo) throws IOException {
        
        ByteArrayInputStream bis = service.gerarRelatorioEvolucaoOcupacaoPdf(periodo);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=evolucao_ocupacao_" + periodo + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/evolucao-ocupacao/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioEvolucaoOcupacaoExcel(
            @RequestParam String periodo) throws IOException {
        
        ByteArrayInputStream bis = service.gerarRelatorioEvolucaoOcupacaoExcel(periodo);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=evolucao_ocupacao_" + periodo + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}
