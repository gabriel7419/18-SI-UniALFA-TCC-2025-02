package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import edu.unialfa.alberguepro.repository.PatrimonioSpecification;
import edu.unialfa.alberguepro.service.RelatorioUsuarioPatrimonioService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.jpa.domain.Specification;
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
@RequestMapping("/relatorios")
public class RelatorioUsuarioPatrimonioController {

    @Autowired
    private RelatorioUsuarioPatrimonioService service;

    @Autowired
    private ControlePatrimonioRepository patrimonioRepository;

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

    @GetMapping("/usuarios/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioUsuariosExcel() throws IOException {
        ByteArrayInputStream bis = service.gerarRelatorioUsuarioExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=usuarios.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
    
    @GetMapping("/patrimonio/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioPatrimonioPdf(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String localAtual) throws JRException {
        
        Specification<ControlePatrimonio> spec = Specification.where(null);

        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comNome(nome));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comStatus(status));
        }

        if (localAtual != null && !localAtual.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comLocalAtual(localAtual));
        }

        List<ControlePatrimonio> patrimonios = patrimonioRepository.findAll(spec);
        ByteArrayInputStream bis = service.gerarRelatorioPatrimonioPdf(patrimonios);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=patrimonio.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/patrimonio/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioPatrimonioExcel(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String localAtual) throws IOException {
        
        Specification<ControlePatrimonio> spec = Specification.where(null);

        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comNome(nome));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comStatus(status));
        }

        if (localAtual != null && !localAtual.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comLocalAtual(localAtual));
        }

        List<ControlePatrimonio> patrimonios = patrimonioRepository.findAll(spec);
        ByteArrayInputStream bis = service.gerarRelatorioPatrimonioExcel(patrimonios);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=patrimonio.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}
