package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;

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
@RequestMapping("/patrimonio")
public class ControlePatrimonioController {

    @Autowired
    private ControlePatrimonioRepository controlePatrimonioRepository;

    @GetMapping
    public String listarPatrimonios(Model model) {
        model.addAttribute("patrimonios", controlePatrimonioRepository.findAll());
        return "patrimonio/index";
    }

    @GetMapping("/novo")
    public ModelAndView novoPatrimonioForm() {
        ModelAndView mv = new ModelAndView("patrimonio/form");
        mv.addObject("patrimonio", new ControlePatrimonio());
        return mv;
    }

    @PostMapping("/salvar")
    public ModelAndView salvarPatrimonio(
            @Valid @ModelAttribute("patrimonio") ControlePatrimonio controlePatrimonio,
            BindingResult result) {

        if (result.hasErrors()) {
            // Retorna à mesma página com os erros de validação
            return new ModelAndView("patrimonio/form");
        }

        // Salva o patrimônio no banco se não houver erros
        controlePatrimonioRepository.save(controlePatrimonio);
        return new ModelAndView("redirect:/patrimonio");
    }


    @GetMapping("/editar/{id}")
    public String editarPatrimonioForm(@PathVariable("id") Long id, Model model) {
        Optional<ControlePatrimonio> controlePatrimonio = controlePatrimonioRepository.findById(id);
        if (controlePatrimonio.isPresent()) {
            model.addAttribute("patrimonio", controlePatrimonio.get());
            return "patrimonio/form";
        } else {
            return "redirect:/patrimonio";
        }
    }

    @GetMapping("/pesquisar")
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro, Model model) {
        List<ControlePatrimonio> controlePatrimonios;
        if (filtro != null && !filtro.isEmpty()) {
            controlePatrimonios = controlePatrimonioRepository.findByNomeContainingIgnoreCase(filtro);
        } else {
            controlePatrimonios = controlePatrimonioRepository.findAll();
        }
        model.addAttribute("patrimonios", controlePatrimonios);
        model.addAttribute("filtro", filtro);
        return "patrimonio/index";
    }
}