package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.QuartoRepository;
import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.repository.VagaRepository;
import edu.unialfa.alberguepro.service.QuartoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String salvar(@ModelAttribute Quarto quarto, RedirectAttributes attributes) {

        try {
            service.salvar(quarto);
            attributes.addFlashAttribute("successMessage", "Quarto salvo com sucesso!");
            return "redirect:/quarto/listar";
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("errorMessage", e.getMessage());
            // Se for novo, volta para a tela de cadastro, se for edição, volta para a lista
            return "redirect:/quarto/novo";
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
    public String listarquartos(Model model) {
        model.addAttribute("quartos", quartoRepository.findAll());
        adicionarContagemDeLeitosOcupados(model);
        return "Quarto/index";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
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