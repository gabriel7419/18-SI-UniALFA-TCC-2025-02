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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("/vaga")
public class VagaController {

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
        model.addAttribute("acolhidos", acolhidoService.listarTodos());
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
        addCommonAttributes(model);

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
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro, Model model) {
        List<Vaga> vagas;
        if (filtro != null && !filtro.isEmpty()) {
            vagas = service.buscarPorNomeAcolhido(filtro);
        } else {
            vagas = service.listarTodos();
        }
        model.addAttribute("vagas", vagas);
        model.addAttribute("filtro", filtro);
        return "vaga/lista";
    }

}
