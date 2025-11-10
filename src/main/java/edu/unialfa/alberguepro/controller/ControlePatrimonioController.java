package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import edu.unialfa.alberguepro.repository.PatrimonioSpecification;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
    public String listarPatrimonios(Model model,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String localAtual) {
        
        Specification<ControlePatrimonio> spec = Specification.where(null);

        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comNome(nome));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comStatus(status));
        }

        if (localAtual != null && !localAtual.isEmpty()) {
            spec = spec.and(PatrimonioSpecification.comLocalAtual(localAtual));
        }

        model.addAttribute("patrimonios", controlePatrimonioRepository.findAll(spec));
        model.addAttribute("nome", nome);
        model.addAttribute("status", status);
        model.addAttribute("localAtual", localAtual);
        
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
            BindingResult result,
            org.springframework.web.servlet.mvc.support.RedirectAttributes attributes) {

        if (result.hasErrors()) {
            ModelAndView mv = new ModelAndView("patrimonio/form");
            mv.addObject("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return mv;
        }

        try {
            controlePatrimonioRepository.save(controlePatrimonio);
            attributes.addFlashAttribute("successMessage", "Patrimônio salvo com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao salvar patrimônio: " + e.getMessage());
        }
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
}