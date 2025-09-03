package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControleEstoque;
import edu.unialfa.alberguepro.service.ControleEstoqueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class ControleEstoqueController {

    private ControleEstoqueService service;

    @GetMapping("/novo")
    public String iniciar(Model model) {
        model.addAttribute("controleEstoque", new ControleEstoque());
        return "controleEstoque/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ControleEstoque controleEstoque, Model model) {

        if (controleEstoque.getNome() == null || controleEstoque.getNome().trim().isEmpty()) {
            model.addAttribute("erro", "O campo Nome é obrigatório.");
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }

        if (controleEstoque.getQuantidade() == null || controleEstoque.getQuantidade() <= 0) {
            model.addAttribute("erro", "O campo Quantidade deve ser um número maior que zero.");
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }

        if (controleEstoque.getData_vencimento() == null || controleEstoque.getData_vencimento().isBefore(LocalDate.now())) {
            model.addAttribute("erro", "A data de vencimento não pode ser no passado.");
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }

        if (controleEstoque.getTipo() == null) {
            model.addAttribute("erro", "Selecione um desses tipo:(Alimento, Higiene ou Limpeza)");
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }

        if (controleEstoque.getUnidade() == null) {
            model.addAttribute("erro", "Selecione uma dessas unidades:(Kilo, Litro ou Unidade)");
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }

        Optional<ControleEstoque> existente = service.buscarPorNome(controleEstoque.getNome());
        if (existente.isPresent() && !existente.get().getId().equals(controleEstoque.getId())) {
            model.addAttribute("erro", "Já existe um produto com esse nome.");
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }

        try {
            service.salvar(controleEstoque);
            return "redirect:/controleEstoque/listar";
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao salvar a Produto: " + e.getMessage());
            model.addAttribute("controleEstoque", controleEstoque);
            return "controleEstoque/formulario";
        }
    }

    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("controleEstoque", service.listarTodos());
        return "controleEstoque/lista";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<ControleEstoque> controleEstoqueOpt = service.buscarPorId(id);
        if (controleEstoqueOpt.isPresent()) {
            model.addAttribute("controleEstoque", controleEstoqueOpt.get());
            return "controleEstoque/formulario";
        } else {
            model.addAttribute("erro", "controleEstoque não encontrada.");
            return "redirect:/controleEstoque/listar";
        }
    }

    @GetMapping("/remover/{id}")
    public String remover(@PathVariable Long id, Model model) {
        try {
            service.deletarPorId(id);
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao remover: " + e.getMessage());
        }
        return "redirect:/controleEstoque/listar";
    }
}
