package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
// import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
// import edu.unialfa.alberguepro.repository.ProdutoRepository;
// import edu.unialfa.alberguepro.service.ControlePatrimonioService;
// import edu.unialfa.alberguepro.service.EstoqueService;

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
@RequestMapping("/patrimonio")
public class ControlePatrimonioController {


    @Autowired
    private ControlePatrimonioRepository controlePatrimonioRepository;

    //@Autowired
    //private ControlePatrimonioService controlePatrimonioService; // Service para a lógica de baixa

    @GetMapping
    public String listarPatrimonios(Model model) {
        model.addAttribute("patrimonios", controlePatrimonioRepository.findAll());
        return "patrimonio/index";
    }

    @GetMapping("/novo")
    public String novoPatrimonioForm(Model model) {
        model.addAttribute("patrimonio", new ControlePatrimonio());
        return "patrimonio/form";
    }

    @PostMapping("/salvar")
    public String salvarPatrimonio(ControlePatrimonio controlePatrimonio) {
        controlePatrimonioRepository.save(controlePatrimonio);
        // Redireciona para a lista de patrimonios
        return "redirect:/patrimonio";
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
            // Se houver um filtro, busca no repositório
            controlePatrimonios = controlePatrimonioRepository.findByNomeContainingIgnoreCase(filtro);
        } else {
            // Senão, busca todos
            controlePatrimonios = controlePatrimonioRepository.findAll();
        }
        model.addAttribute("patrimonios", controlePatrimonios);
        model.addAttribute("filtro", filtro); // Envia o filtro de volta para a view
        return "/patrimonio/index";
    }

    /*@PostMapping("/dar-baixa")
    public String processarBaixaIndividual(@RequestParam("patrimonioId") Long patrimonioId, @RequestParam("quantidade") Integer quantidade) {
        estoqueService.darBaixa(produtoId, quantidade);
        // Redireciona de volta para a página de baixa para continuar o trabalho
        return "redirect:/estoque/baixa";
    }

    @PostMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable("id") Long id) {
        produtoRepository.deleteById(id);
        return "redirect:/estoque";
    }*/
}