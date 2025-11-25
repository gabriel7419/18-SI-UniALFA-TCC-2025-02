package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.service.RelatorioPatrimonioService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/relatorios/patrimonio")
public class RelatorioPatrimonioController {

    @Autowired
    private RelatorioPatrimonioService service;

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioPdf(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String localAtual) throws JRException {
        
        ByteArrayInputStream bis = service.gerarRelatorioPdf(nome, status, localAtual);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_patrimonios.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioExcel(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String localAtual) throws IOException {
        
        ByteArrayInputStream bis = service.gerarRelatorioExcel(nome, status, localAtual);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_patrimonios.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}
