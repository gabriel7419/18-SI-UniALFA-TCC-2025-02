package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.QuartoRepository;
import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.service.QuartoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/quarto")
public class QuartoController {


    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private QuartoService service;

    @GetMapping("/novo")
    public String iniciarCadastro(Model model) {
        model.addAttribute("quarto", new Quarto());
        return "quarto/form"; // Sua página Thymeleaf
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Quarto quarto, RedirectAttributes attributes) {

        try {
            service.salvar(quarto);
            attributes.addFlashAttribute("mensagemSucesso", "Quarto salvo com sucesso!");
            return "redirect:/quarto/listar";
        } catch (IllegalArgumentException e) {
            // Adiciona a mensagem de erro e redireciona para o formulário
            attributes.addFlashAttribute("mensagemErro", e.getMessage());
            // Se for novo, volta para a tela de cadastro, se for edição, volta para a lista
            return "redirect:/quarto/novo";
        }

    }

    @GetMapping("/listar")
    public String listarquartos(Model model) {
        model.addAttribute("quartos", quartoRepository.findAll());
        return "quarto/index";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
        return "redirect:/quarto/listar";
    }

    @GetMapping("/pesquisar")
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro, Model model) {
        List<Quarto> quarto;
        if (filtro != null && !filtro.isEmpty()) {
            quarto = quartoRepository.findByNumeroQuartoContainingIgnoreCase(filtro);
        } else {
            quarto = quartoRepository.findAll();
        }
        model.addAttribute("quartos", quarto);
        model.addAttribute("filtro", filtro);
        return "/quarto/index";
    }

}