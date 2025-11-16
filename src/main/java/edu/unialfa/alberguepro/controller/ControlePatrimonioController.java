package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import edu.unialfa.alberguepro.repository.PatrimonioSpecification;
import edu.unialfa.alberguepro.service.ControlePatrimonioService;

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

    @Autowired
    private ControlePatrimonioService controlePatrimonioService;

    @GetMapping
    public String listarPatrimonios(Model model,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String localAtual,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size,
        @RequestParam(defaultValue = "nome") String sort,
        @RequestParam(defaultValue = "asc") String dir) {

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

        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        org.springframework.data.domain.Page<ControlePatrimonio> pageResult = controlePatrimonioRepository.findAll(spec, pageable);

        model.addAttribute("patrimonios", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("nome", nome);
        model.addAttribute("status", status);
        model.addAttribute("localAtual", localAtual);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

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
            controlePatrimonioService.salvar(controlePatrimonio);
            attributes.addFlashAttribute("successMessage", "Patrimônio salvo com sucesso!");
        } catch (IllegalArgumentException e) {
            ModelAndView mv = new ModelAndView("patrimonio/form");
            mv.addObject("errorMessage", e.getMessage());
            return mv;
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao salvar patrimônio: " + e.getMessage());
        }

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