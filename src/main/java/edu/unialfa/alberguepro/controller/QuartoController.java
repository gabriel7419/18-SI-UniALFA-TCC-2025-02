package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;

import edu.unialfa.alberguepro.repository.QuartoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/quarto")
public class QuartoController {

    @Autowired
    private QuartoRepository quartoRepository;

    @GetMapping
    public String listarQuartos(Model model) {
        model.addAttribute("quartos", quartoRepository.findAll());
        return "quarto/index";
    }

    @GetMapping("/novo")
    public ModelAndView novoQuartoForm() {
        ModelAndView mv = new ModelAndView("quarto/form");
        mv.addObject("quartoForm", new Quarto());
        return mv;
    }

    @PostMapping("/salvar")
    public ModelAndView salvarQuarto(
            @Valid @ModelAttribute("quartoForm") Quarto quarto,
            BindingResult result) {

        if (result.hasErrors()) {
            // Retorna à mesma página com os erros de validação
            return new ModelAndView("quarto/form");
        }

        // Salva o patrimônio no banco se não houver erros
        quartoRepository.save(quarto);
        return new ModelAndView("redirect:/quarto");
    }


    @GetMapping("/editar/{id}")
    public String quartoForm(@PathVariable("id") Long id, Model model) {
        Optional<Quarto> quarto = quartoRepository.findById(id);
        if (quarto.isPresent()) {
            model.addAttribute("quartoForm", quarto.get());
            return "quarto/form";
        } else {
            return "redirect:/quarto";
        }
    }

    @GetMapping("/pesquisar")
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro, Model model) {
        List<Quarto> quartos;
        if (filtro != null && !filtro.isEmpty()) {
            quartos = quartoRepository.findByQuartoContainingIgnoreCase(filtro);
        } else {
            quartos = quartoRepository.findAll();
        }
        model.addAttribute("quartos", quartos);
        model.addAttribute("filtro", filtro);
        return "/quarto/index";
    }
}