package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
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

    @GetMapping
    public String iniciar(Model model) {
        model.addAttribute("vaga", new Vaga());
        model.addAttribute("acolhidos", acolhidoService.listarTodos());
        model.addAttribute("quartos", Vaga.Quarto.values());
        model.addAttribute("numeroLeitos", Vaga.NumeroLeito.values());
        return "vaga/form";
    }

    @PostMapping("salvar")
    public String salvar(@ModelAttribute("vaga") Vaga vaga, BindingResult result, Model model) {

        if (vaga.getAcolhido() == null || vaga.getAcolhido().getId() == null) {
            result.rejectValue("acolhido.id", "campo.obrigatorio", "O acolhido é obrigatório.");
        }

        if (vaga.getDataEntrada() == null) {
            result.rejectValue("dataEntrada", "campo.obrigatorio", "A data de entrada é obrigatória.");
        } else if (vaga.getDataEntrada().isBefore(LocalDate.now())) {
            result.rejectValue("dataEntrada", "data.invalida", "A data de entrada deve ser hoje ou futura.");
        }

        if (vaga.getDataSaida() == null) {
            result.rejectValue("dataSaida", "campo.obrigatorio", "A data de saída é obrigatória.");
        } else if (vaga.getDataEntrada() != null && !vaga.getDataSaida().isAfter(vaga.getDataEntrada())) {
            result.rejectValue("dataSaida", "data.invalida", "A data de saída deve ser posterior à data de entrada.");
        }

        if (vaga.getDataSaida() == null && vaga.getDataEntrada() != null) {
            vaga.setDataSaida(vaga.getDataEntrada().plusMonths(3));
        }

        if (vaga.getNumeroLeito() == null) {
            result.rejectValue("numeroLeito", "campo.obrigatorio", "O número do leito é obrigatório.");
        }

        if (vaga.getQuarto() == null) {
            result.rejectValue("quarto", "campo.obrigatorio", "O quarto é obrigatório.");
        }


        if (result.hasErrors()) {
            model.addAttribute("acolhidos", acolhidoService.listarTodos());
            model.addAttribute("quartos", Vaga.Quarto.values());
            model.addAttribute("numeroLeitos", Vaga.NumeroLeito.values());
            return "vaga/form";
        }

        if (vaga.getAcolhido() != null && vaga.getAcolhido().getId() != null) {
            CadastroAcolhido full = acolhidoService.buscarPorId(vaga.getAcolhido().getId());
            vaga.setAcolhido(full);
        }

        service.salvar(vaga);
        return "redirect:/vaga/listar";
    }

    @GetMapping("listar")
    public String listar(Model model) {
        List<Vaga> vaga = service.listarTodos();
        model.addAttribute("vagas", vaga);
        return "vaga/lista";
    }

    @GetMapping("editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("vaga", service.buscarPorId(id));
        model.addAttribute("acolhidos", acolhidoService.listarTodos());
        model.addAttribute("quartos", Vaga.Quarto.values());
        model.addAttribute("numeroLeitos", Vaga.NumeroLeito.values());
        return "vaga/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
        return "redirect:/vaga/listar";
    }
}
