package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.Leito;
import edu.unialfa.alberguepro.service.LeitoService;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/leito")
public class LeitoController {

    @Autowired
    private LeitoService service;

    @Autowired
    private CadastroAcolhidoService acolhidoService;

    @GetMapping()
    public String iniciar(Leito leito, Model model) {
        model.addAttribute("acolhidos", acolhidoService.listarTodos());
        return "leito/form";
    }

    @PostMapping("salvar")
    public String salvar(@ModelAttribute Leito leito) {
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
        return "leito/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
        return "redirect:/leito/listar";
    }
}
