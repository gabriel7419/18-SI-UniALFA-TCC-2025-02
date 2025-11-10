package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.service.RelatorioUsuarioPatrimonioService;
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

@Controller
@RequestMapping("/relatorios")
public class RelatorioUsuarioPatrimonioController {

    @Autowired
    private RelatorioUsuarioPatrimonioService service;

    @GetMapping("/usuarios/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioUsuariosPdf() throws JRException {
        ByteArrayInputStream bis = service.gerarRelatorioUsuarioPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=usuarios.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
    
    @GetMapping("/patrimonio/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioPatrimonioPdf() throws JRException {
        ByteArrayInputStream bis = service.gerarRelatorioPatrimonioPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=patrimonio.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
