package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.QuartoRepository;
import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.repository.VagaRepository;
import edu.unialfa.alberguepro.service.QuartoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/quarto")
@Slf4j
public class QuartoController {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private QuartoService service;

    @GetMapping("/novo")
    public String iniciarCadastro(Model model) {
        model.addAttribute("quarto", new Quarto());
        return "Quarto/form"; // Sua página Thymeleaf
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Quarto quarto, BindingResult result, RedirectAttributes attributes, Model model) {

        // Validação de preenchimento obrigatório via Bean Validation
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return "Quarto/form";
        }

        try {
            service.salvar(quarto);
            attributes.addFlashAttribute("successMessage", "Quarto salvo com sucesso!");
            return "redirect:/quarto/listar";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "Quarto/form";
        }

    }

    private void adicionarContagemDeLeitosOcupados(Model model) {

        List<Object[]> contagemLeitos = vagaRepository.countOccupiedBedsByRoom();

        // 💡 DEBUG: Imprima o resultado da consulta SQL
        log.info("Resultado da contagem de leitos ocupados (Quarto / Qtd):");
        for (Object[] resultado : contagemLeitos) {
            log.info("Quarto: {} | Ocupados: {}", resultado[0], resultado[1]);
        }
        // FIM DEBUG

        Map<String, Long> leitosOcupadosPorQuarto = contagemLeitos.stream()
                .collect(Collectors.toMap(

                        array -> (String) array[0],

                        array -> (Long) array[1]
                ));

        model.addAttribute("leitosOcupadosPorQuarto", leitosOcupadosPorQuarto);
    }

    @GetMapping("/listar")
    public String listarquartos(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "15") int size,
                                @RequestParam(defaultValue = "numeroQuarto") String sort,
                                @RequestParam(defaultValue = "asc") String dir) {
        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        org.springframework.data.domain.Page<Quarto> pageResult = service.listarTodosPaginado(pageable);
        
        model.addAttribute("quartos", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        adicionarContagemDeLeitosOcupados(model);
        return "Quarto/index";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            service.deletarPorId(id);
            attributes.addFlashAttribute("successMessage", "Quarto removido com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao remover o quarto: " + e.getMessage());
        }
        return "redirect:/quarto/listar";
    }

    @GetMapping("/pesquisar")
    public String pesquisaForm(@RequestParam(value = "filtro", required = false) String filtro, Model model) {
        List<Quarto> quartos;
        if (filtro != null && !filtro.isEmpty()) {
            quartos = quartoRepository.findByNumeroQuartoContainingIgnoreCase(filtro);
        } else {
            quartos = quartoRepository.findAll();
        }
        model.addAttribute("quartos", quartos);
        model.addAttribute("filtro", filtro);
        adicionarContagemDeLeitosOcupados(model);
        return "Quarto/index";
    }

}