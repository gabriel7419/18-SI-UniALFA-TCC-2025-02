package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.repository.ProdutoRepository;
import edu.unialfa.alberguepro.service.EstoqueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueService estoqueService; // Service para a lógica de baixa

    @GetMapping
    public String listarProdutos(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "estoque/index";
    }

    @GetMapping("/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        return "estoque/form";
    }

    @PostMapping("/salvar")
    public String salvarProduto(Produto produto) {
        produtoRepository.save(produto);
        // Redireciona para a lista de produtos
        return "redirect:/estoque";
    }

    @GetMapping("/editar/{id}")
    public String editarProdutoForm(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produto = produtoRepository.findById(id);
        if (produto.isPresent()) {
            model.addAttribute("produto", produto.get());
            return "estoque/form";
        } else {
            return "redirect:/estoque";
        }
    }

    @GetMapping("/baixa")
    public String darBaixaForm(@RequestParam(value = "filtro", required = false) String filtro, Model model) {
        List<Produto> produtos;
        if (filtro != null && !filtro.isEmpty()) {
            // Se houver um filtro, busca no repositório
            produtos = produtoRepository.findByNomeContainingIgnoreCase(filtro);
        } else {
            // Senão, busca todos
            produtos = produtoRepository.findAll();
        }
        model.addAttribute("produtos", produtos);
        model.addAttribute("filtro", filtro); // Envia o filtro de volta para a view
        return "estoque/baixa";
    }

    @PostMapping("/dar-baixa")
    public String processarBaixaIndividual(@RequestParam("produtoId") Long produtoId, @RequestParam("quantidade") Integer quantidade) {
        estoqueService.darBaixa(produtoId, quantidade);
        // Redireciona de volta para a página de baixa para continuar o trabalho
        return "redirect:/estoque/baixa";
    }

    @PostMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable("id") Long id) {
        produtoRepository.deleteById(id);
        return "redirect:/estoque";
    }
}