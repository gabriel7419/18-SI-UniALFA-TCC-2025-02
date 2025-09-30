package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.model.Leito;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import edu.unialfa.alberguepro.service.LeitoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/leito")
public class LeitoController {

    @Autowired
    private LeitoService service;

    @Autowired
    private CadastroAcolhidoService acolhidoService;

    @GetMapping
    public String iniciar(Model model) {
        model.addAttribute("leito", new Leito());
        model.addAttribute("acolhidos", acolhidoService.listarTodos());
        model.addAttribute("quartos", Leito.Quarto.values());
        model.addAttribute("numeroLeitos", Leito.NumeroLeito.values());
        return "leito/form";
    }

    @PostMapping("salvar")
    public String salvar(@ModelAttribute("leito") Leito leito, BindingResult result, Model model) {

        if (leito.getAcolhido() == null || leito.getAcolhido().getId() == null) {
            result.rejectValue("acolhido.id", "campo.obrigatorio", "O acolhido é obrigatório.");
        }

        if (leito.getDataEntrada() == null) {
            result.rejectValue("dataEntrada", "campo.obrigatorio", "A data de entrada é obrigatória.");
        } else if (leito.getDataEntrada().isBefore(LocalDate.now())) {
            result.rejectValue("dataEntrada", "data.invalida", "A data de entrada deve ser hoje ou futura.");
        }

        if (leito.getDataSaida() == null) {
            result.rejectValue("dataSaida", "campo.obrigatorio", "A data de saída é obrigatória.");
        } else if (leito.getDataEntrada() != null && !leito.getDataSaida().isAfter(leito.getDataEntrada())) {
            result.rejectValue("dataSaida", "data.invalida", "A data de saída deve ser posterior à data de entrada.");
        }

        if (leito.getDataSaida() == null && leito.getDataEntrada() != null) {
            leito.setDataSaida(leito.getDataEntrada().plusMonths(3));
        }

        if (leito.getNumeroLeito() == null) {
            result.rejectValue("numeroLeito", "campo.obrigatorio", "O número do leito é obrigatório.");
        }

        if (leito.getQuarto() == null) {
            result.rejectValue("quarto", "campo.obrigatorio", "O quarto é obrigatório.");
        }


        if (result.hasErrors()) {
            model.addAttribute("acolhidos", acolhidoService.listarTodos());
            model.addAttribute("quartos", Leito.Quarto.values());
            model.addAttribute("numeroLeitos", Leito.NumeroLeito.values());
            return "leito/form";
        }

        if (leito.getAcolhido() != null && leito.getAcolhido().getId() != null) {
            CadastroAcolhido full = acolhidoService.buscarPorId(leito.getAcolhido().getId());
            leito.setAcolhido(full);
        }

        service.salvar(leito);
        return "redirect:/leito/listar";
    }

    @GetMapping("listar")
    public String listar(Model model) {
        List<Leito> leitos = service.listarTodos();
        model.addAttribute("leitos", leitos);
        return "leito/lista";
    }

    @GetMapping("editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("leito", service.buscarPorId(id));
        model.addAttribute("acolhidos", acolhidoService.listarTodos());
        model.addAttribute("quartos", Leito.Quarto.values());
        model.addAttribute("numeroLeitos", Leito.NumeroLeito.values());
        return "leito/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
        return "redirect:/leito/listar";
    }
}
