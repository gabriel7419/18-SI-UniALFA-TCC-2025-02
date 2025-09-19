package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cadastroAcolhido")
public class CadastroAcolhidoController {

    @Autowired
    private CadastroAcolhidoService service;

    @GetMapping()
    public String iniciar(CadastroAcolhido cadastroAcolhido, Model model) {
        return "cadastroAcolhido/form";
    }

    @GetMapping("/formulario")
    public String formulario(Model model) {
        try {
            model.addAttribute("cadastroAcolhido", new CadastroAcolhido());
            return "formulario";
        } catch (Exception e) {
            e.printStackTrace(); // log no console
            model.addAttribute("erro", e.getMessage());
            return "error";
        }
    }

    @PostMapping("salvar")
    public String salvar(CadastroAcolhido cadastroAcolhido, Model model) {
        try {
            service.salvar(cadastroAcolhido);
            return  "redirect:/cadastroAcolhido/listar";
        } catch (Exception e) {
            model.addAttribute("message", "");
            return iniciar(cadastroAcolhido,model);
        }
    }

    @GetMapping("listar")
    public String listar(Model model) {
        model.addAttribute("cadastroAcolhido", service.listarTodos());
        return "cadastroAcolhido/lista";
    }

    @GetMapping("editar/{id}")
    public String alterar(@PathVariable Long id, Model model) {
        model.addAttribute("cadastroAcolhido", service.buscarPorId(id));
        return "cadastroAcolhido/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id, Model model) {
        service.deletarPorId(id);
        return "redirect:/cadastroAcolhido/listar";
    }
}



