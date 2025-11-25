package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/cadastroAcolhido/relatorio")
public class RelatorioAcolhidoController {

    @Autowired
    private RelatorioAcolhidoService service;

    @Autowired
    private CadastroAcolhidoService cadastroAcolhidoService;

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

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> baixarExcel() throws IOException {
        ByteArrayInputStream bis = service.gerarRelatorioExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=acolhidos.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/permanencia/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioPermanenciaPdf(
            @RequestParam(defaultValue = "30") Integer dias) throws JRException {
        
        List<CadastroAcolhido> acolhidos = cadastroAcolhidoService.buscarAcolhidosPermanenciaProlongada(dias);
        ByteArrayInputStream bis = service.gerarRelatorioPermanenciaPdf(acolhidos, dias);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=acolhidos_permanencia.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/permanencia/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioPermanenciaExcel(
            @RequestParam(defaultValue = "30") Integer dias) throws IOException {
        
        List<CadastroAcolhido> acolhidos = cadastroAcolhidoService.buscarAcolhidosPermanenciaProlongada(dias);
        ByteArrayInputStream bis = service.gerarRelatorioPermanenciaExcel(acolhidos, dias);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=acolhidos_permanencia.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}
