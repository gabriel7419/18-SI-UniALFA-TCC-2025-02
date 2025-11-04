package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.dto.AcolhidoDTO;
import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.model.Leito;
import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import edu.unialfa.alberguepro.service.LeitoService;
import edu.unialfa.alberguepro.service.QuartoService;
import edu.unialfa.alberguepro.service.VagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/vaga")
public class VagaController {

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
    public String salvar(@ModelAttribute("vaga") Vaga vaga, BindingResult result, Model model) {

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

        service.salvar(vaga);
        return "redirect:/vaga/listar";
    }

    @GetMapping("/leitos/{quartoId}")
    @ResponseBody
    public List<Leito> buscarLeitosPorQuarto(@PathVariable Long quartoId) {
        return leitoService.buscarPorQuartoId(quartoId);
    }

    @GetMapping("listar")
    public String listar(Model model) {
        List<Vaga> vaga = service.listarTodos();
        model.addAttribute("vagas", vaga);
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
            model.addAttribute("leitosDoQuarto", leitoService.buscarPorQuartoId(quartoId));
        }

        return "vaga/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
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
}
