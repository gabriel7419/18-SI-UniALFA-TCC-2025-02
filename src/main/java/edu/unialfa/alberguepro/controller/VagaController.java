package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.dto.AcolhidoDTO;
import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Leito;
import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.repository.VagaRepository;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import edu.unialfa.alberguepro.service.LeitoService;
import edu.unialfa.alberguepro.service.QuartoService;
import edu.unialfa.alberguepro.service.VagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vaga")
public class VagaController {

    private static final Logger log = LoggerFactory.getLogger(VagaController.class);

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private VagaService service;

    @Autowired
    private CadastroAcolhidoService acolhidoService;

    @Autowired
    private LeitoService leitoService;

    @Autowired
    private QuartoService quartoService;

    private void addCommonAttributes(Model model) {
        model.addAttribute("acolhidos", acolhidoService.listarAcolhidosSemLeitoAtivo());
        model.addAttribute("quartos", quartoService.listarTodos());
    }

    @GetMapping
    public String iniciar(Model model) {
        Vaga vaga = new Vaga();

        if (vaga.getLeito() == null) {
            vaga.setLeito(new Leito());
        }

        model.addAttribute("vaga", vaga);
        addCommonAttributes(model);
        return "vaga/form";
    }

    @PostMapping("salvar")
    public String salvar(@ModelAttribute("vaga") Vaga vaga, BindingResult result, Model model,
    org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {

        if (vaga.getAcolhido() == null || vaga.getAcolhido().getId() == null) {
            result.rejectValue("acolhido.id", "campo.obrigatorio", "O acolhido é obrigatório.");
        }

        if (vaga.getLeito() == null || vaga.getLeito().getId() == null) {
            result.rejectValue("leito", "campo.obrigatorio", "O leito é obrigatório.");
        }

        if (result.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return "vaga/form";
        }

            if (vaga.getAcolhido() != null && vaga.getAcolhido().getId() != null) {
                CadastroAcolhido full = acolhidoService.buscarPorId(vaga.getAcolhido().getId());
                vaga.setAcolhido(full);
            }

            if (vaga.getLeito() != null && vaga.getLeito().getId() != null) {
                Leito fullLeito = leitoService.buscarPorId(vaga.getLeito().getId());
                vaga.setLeito(fullLeito);
            }

        if (vaga.getLeito() != null && vaga.getLeito().getId() != null) {
            Leito full = leitoService.buscarPorId(vaga.getLeito().getId());
            vaga.setLeito(full);
        }

        try {
            service.salvar(vaga);
            attributes.addFlashAttribute("successMessage", "Vaga salva com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao salvar vaga: " + e.getMessage());
        }
        return "redirect:/vaga/listar";
    }

    @GetMapping("/leitos/{quartoId}")
    @ResponseBody
    public List<Leito> buscarLeitosPorQuarto(@PathVariable Long quartoId,
    @RequestParam(required = false) Long vagaId) {

        if (vagaId != null) {
            // Modo edição: incluir o leito atual da vaga
            Vaga vagaAtual = service.buscarPorId(vagaId);
            List<Leito> leitosDisponiveis = leitoService.buscarLeitosDisponiveisPorQuartoId(quartoId);

            // Adicionar o leito atual da vaga se não estiver na lista
            if (vagaAtual != null && vagaAtual.getLeito() != null) {
                boolean leitoAtualJaEstaLista = leitosDisponiveis.stream()
                    .anyMatch(l -> l.getId().equals(vagaAtual.getLeito().getId()));

                if (!leitoAtualJaEstaLista) {
                    leitosDisponiveis.add(vagaAtual.getLeito());
                }
            }

            return leitosDisponiveis;
        } else {
            // Modo cadastro: apenas leitos disponíveis
            return leitoService.buscarLeitosDisponiveisPorQuartoId(quartoId);
        }
    }

    @GetMapping("listar")
    public String listar(Model model, 
                        @RequestParam(required = false) String filtro,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size,
                        @RequestParam(defaultValue = "acolhido.nome") String sort,
                        @RequestParam(defaultValue = "asc") String dir) {
        org.springframework.data.domain.Page<Vaga> pageResult;
        
        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        
        if (filtro != null && !filtro.trim().isEmpty()) {
            pageResult = service.buscarPorNomeAcolhidoPaginado(filtro, pageable);
        } else {
            pageResult = service.listarTodosPaginado(pageable);
        }
        
        model.addAttribute("vagas", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("filtro", filtro);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "vaga/lista";
    }

    @GetMapping("editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Vaga vaga = service.buscarPorId(id);
        model.addAttribute("vaga", vaga);
        
        // Buscar acolhidos sem leito ativo
        List<CadastroAcolhido> acolhidosDisponiveis = acolhidoService.listarAcolhidosSemLeitoAtivo();
        
        // Adicionar o acolhido atual da vaga se não estiver na lista
        if (vaga != null && vaga.getAcolhido() != null) {
            boolean acolhidoAtualNaLista = acolhidosDisponiveis.stream()
                .anyMatch(a -> a.getId().equals(vaga.getAcolhido().getId()));
            
            if (!acolhidoAtualNaLista) {
                acolhidosDisponiveis.add(vaga.getAcolhido());
            }
        }
        
        model.addAttribute("acolhidos", acolhidosDisponiveis);
        model.addAttribute("quartos", quartoService.listarTodos());

        if (vaga != null && vaga.getLeito() != null) {
            Long quartoId = vaga.getLeito().getQuarto().getId();
            model.addAttribute("quartoSelecionadoId", quartoId);

            List<Leito> leitosLivres = leitoService.buscarLeitosLivresPorQuartoId(quartoId);

            if (vaga.getLeito() != null && !leitosLivres.contains(vaga.getLeito())) {
                leitosLivres.add(vaga.getLeito());
            }

            model.addAttribute("leitosDoQuarto", leitoService.buscarPorQuartoId(quartoId));
        }

        return "vaga/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {
        try {
            service.deletarPorId(id);
            attributes.addFlashAttribute("successMessage", "Vaga removida com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao remover vaga: " + e.getMessage());
        }
        return "redirect:/vaga/listar";
    }

    @GetMapping("/acolhido/datas/{acolhidoId}")
    @ResponseBody
    public AcolhidoDTO buscarDatasAcolhido(@PathVariable Long acolhidoId) {

        CadastroAcolhido acolhido = acolhidoService.buscarPorId(acolhidoId);

        if (acolhido != null) {

            return new AcolhidoDTO(acolhido);
        }

        return null;
    }

    @GetMapping("/pesquisar")
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro,
                              @RequestParam(value = "numeroQuarto", required = false) String numeroQuarto,
                              @RequestParam(value = "numeroLeito", required = false) String numeroLeito,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "15") int size,
                              @RequestParam(defaultValue = "acolhido.nome") String sort,
                              @RequestParam(defaultValue = "asc") String dir,
                              Model model) {
        
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        
        org.springframework.data.domain.Page<Vaga> pageResult = service.buscarComFiltros(filtro, numeroQuarto, numeroLeito, pageable);
        
        model.addAttribute("vagas", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("filtro", filtro);
        model.addAttribute("numeroQuarto", numeroQuarto);
        model.addAttribute("numeroLeito", numeroLeito);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "vaga/lista";
    }

    @GetMapping("/relatorio/estrategico-pdf")
    public ResponseEntity<byte[]> relatorioEstrategicoPdf() {
        try {
            List<Vaga> todasVagas = service.listarTodos();
            
            // Totais gerais
            long totalVagas = todasVagas.size();
            long vagasAtivas = todasVagas.stream()
                    .filter(v -> v.getDataSaida() == null || v.getDataSaida().isAfter(LocalDate.now()))
                    .count();
            long vagasEncerradas = totalVagas - vagasAtivas;
            
            // Média de permanência
            double mediaPermanencia = todasVagas.stream()
                    .filter(v -> v.getDataSaida() != null)
                    .mapToLong(v -> java.time.temporal.ChronoUnit.DAYS.between(v.getDataEntrada(), v.getDataSaida()))
                    .average()
                    .orElse(0.0);
            
            // Acolhimentos por mês (últimos 12 meses)
            LocalDate dataInicio = LocalDate.now().minusMonths(12);
            Map<String, Long> acolhimentosPorMes = todasVagas.stream()
                    .filter(v -> v.getDataEntrada().isAfter(dataInicio))
                    .collect(Collectors.groupingBy(
                            v -> v.getDataEntrada().format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy")),
                            Collectors.counting()
                    ));
            
            // Preparar dados ordenados por mês
            List<Map<String, Object>> dados = new ArrayList<>();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM/yyyy", 
                    new java.util.Locale("pt", "BR"));
            
            for (int i = 11; i >= 0; i--) {
                LocalDate mes = LocalDate.now().minusMonths(i);
                String mesAnoChave = mes.format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy"));
                String mesAnoExibicao = mes.format(formatter);
                
                long totalMes = acolhimentosPorMes.getOrDefault(mesAnoChave, 0L);
                long ativasMes = todasVagas.stream()
                        .filter(v -> v.getDataEntrada().getMonth() == mes.getMonth() && 
                                    v.getDataEntrada().getYear() == mes.getYear() &&
                                    (v.getDataSaida() == null || v.getDataSaida().isAfter(LocalDate.now())))
                        .count();
                long encerradasMes = totalMes - ativasMes;
                
                Map<String, Object> item = new HashMap<>();
                item.put("mesAno", mesAnoExibicao.substring(0, 1).toUpperCase() + mesAnoExibicao.substring(1));
                item.put("totalAcolhimentos", totalMes);
                item.put("vagasAtivas", ativasMes);
                item.put("vagasEncerradas", encerradasMes);
                dados.add(item);
            }
            
            // Carregar template
            InputStream jrxmlStream = getClass().getResourceAsStream("/relatorios/relatorio_vagas_estrategico.jrxml");
            if (jrxmlStream == null) {
                throw new RuntimeException("Template JRXML não encontrado: /relatorios/relatorio_vagas_estrategico.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport = 
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(jrxmlStream);
            
            // Parâmetros
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("TOTAL_VAGAS", totalVagas);
            parametros.put("VAGAS_ATIVAS", vagasAtivas);
            parametros.put("VAGAS_ENCERRADAS", vagasEncerradas);
            parametros.put("MEDIA_PERMANENCIA", mediaPermanencia);
            
            // DataSource
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource = 
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dados);
            
            // Preencher
            net.sf.jasperreports.engine.JasperPrint jasperPrint = 
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            
            // Exportar
            byte[] pdf = net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(jasperPrint);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition.inline()
                    .filename("relatorio-vagas-estrategico.pdf")
                    .build());
            
            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Erro ao gerar relatório estratégico de vagas", e);
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Erro: " + e.getMessage()).getBytes());
        }
    }

}
