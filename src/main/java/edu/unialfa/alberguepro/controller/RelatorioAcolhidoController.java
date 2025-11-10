package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.service.RelatorioAcolhidoService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/cadastroAcolhido/relatorio")
public class RelatorioAcolhidoController {

    @Autowired
    private RelatorioAcolhidoService service;

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> baixarPdf() throws JRException {
        ByteArrayInputStream bis = service.gerarRelatorioPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=acolhidos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
