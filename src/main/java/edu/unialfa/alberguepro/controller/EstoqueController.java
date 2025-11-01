package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.model.Unidade;
import edu.unialfa.alberguepro.repository.ProdutoRepository;
import edu.unialfa.alberguepro.repository.UnidadeRepository;
import edu.unialfa.alberguepro.repository.ProdutoSpecification;
import edu.unialfa.alberguepro.service.EstoqueService;
import edu.unialfa.alberguepro.service.RelatorioService;
import edu.unialfa.alberguepro.repository.MovimentacaoEstoqueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private RelatorioService relatorioService;

    private void carregarUnidades(Model model) {
        List<Unidade> unidades = unidadeRepository.findAll();
        model.addAttribute("unidades", unidades);
    }

    @GetMapping({"/", ""}) 
    public String listarProdutos(Model model,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) Long unidadeId) {
        Specification<Produto> spec = Specification.where(null);

        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comNome(nome));
        }

        if (tipo != null && !tipo.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comTipo(tipo));
        }

        Unidade unidade = null;
        if (unidadeId != null && unidadeId > 0) {
            unidade = unidadeRepository.findById(unidadeId).orElse(null);
            if (unidade != null) {
                spec = spec.and(ProdutoSpecification.comUnidade(unidade));
            }
        }

        model.addAttribute("produtos", produtoRepository.findAll(spec));
        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        model.addAttribute("unidade", unidade);
        carregarUnidades(model);

        return "estoque/index";
    }

    @GetMapping("/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        carregarUnidades(model);
        return "estoque/form";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@Valid Produto produto, BindingResult result, Model model) {
        // Validação de unicidade que requer acesso ao banco
        if (produto.getNome() != null && produto.getTipo() != null) {
            if (!estoqueService.isNomeAndTipoUnique(produto.getNome(), produto.getTipo(), produto.getId())) {
                result.rejectValue("nome", "error.produto", "Já existe um produto com este nome e tipo.");
            }
        }

        // Verifica todos os erros de validação
        if (result.hasErrors()) {
            carregarUnidades(model);
            return "estoque/form";
        }

        // Se a validação passou, o 'unidadeId' existe. 
        Optional<Unidade> unidadeOptional = unidadeRepository.findById(produto.getUnidadeId());
        if (unidadeOptional.isEmpty()) {    
            result.rejectValue("unidadeId", "error.produto", "Unidade selecionada é inválida.");
            carregarUnidades(model);
            return "estoque/form";
        }
        produto.setUnidade(unidadeOptional.get());
        
        estoqueService.salvar(produto); // Alterado para usar o serviço

        return "redirect:/estoque";
    }

    @GetMapping("/editar/{id}")
    public String editarProdutoForm(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produto = produtoRepository.findById(id);
        if (produto.isPresent()) {
            model.addAttribute("produto", produto.get());
            carregarUnidades(model);
            return "estoque/form";
        } else {
            return "redirect:/estoque";
        }
    }

    @GetMapping("/baixa")
    public String darBaixaForm(@RequestParam(value = "filtro", required = false) String filtro,
        @RequestParam(value = "tipo", required = false) String tipo,
        Model model) {
        List<Produto> produtos;
        if ((filtro != null && !filtro.isEmpty()) || (tipo != null && !tipo.isEmpty())) {
            produtos = produtoRepository.findByNomeContainingIgnoreCaseAndTipoContainingIgnoreCase(filtro, tipo);
        } else {
            produtos = produtoRepository.findAll();
        }
        model.addAttribute("produtos", produtos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("tipo", tipo);
        return "estoque/baixa";
    }

    @PostMapping("/dar-baixa")
    public String processarBaixaIndividual(@RequestParam("produtoId") Long produtoId, @RequestParam("quantidade") Integer quantidade) {
        estoqueService.darBaixa(produtoId, quantidade);
        return "redirect:/estoque/baixa";
    }

    @PostMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable("id") Long id) {
        estoqueService.excluir(id); // Alterado para usar o serviço
        return "redirect:/estoque";
    }

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioPdf(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long unidadeId) throws JRException {

        Specification<Produto> spec = Specification.where(null);
        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comNome(nome));
        }
        if (tipo != null && !tipo.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comTipo(tipo));
        }
        Unidade unidade = null;
        if (unidadeId != null && unidadeId > 0) { 
            unidade = unidadeRepository.findById(unidadeId).orElse(null);
            if (unidade != null) {
                spec = spec.and(ProdutoSpecification.comUnidade(unidade));
            }
        }

        List<Produto> produtos = produtoRepository.findAll(spec);
        ByteArrayInputStream bis = relatorioService.gerarRelatorioPdf(produtos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=estoque.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @GetMapping("/historico")
    public String verHistorico(Model model) {
        model.addAttribute("movimentacoes", movimentacaoEstoqueRepository.findAllByOrderByDataMovimentacaoDesc());
        return "estoque/historico";
    }

    @GetMapping("/historico/relatorio/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioMovimentacaoPdf() throws JRException {
        List<edu.unialfa.alberguepro.model.MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAllByOrderByDataMovimentacaoDesc();
        ByteArrayInputStream bis = relatorioService.gerarRelatorioMovimentacaoPdf(movimentacoes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=movimentacao_estoque.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/historico/relatorio/xlsx")
    public ResponseEntity<InputStreamResource> gerarRelatorioMovimentacaoXlsx() throws IOException {
        List<edu.unialfa.alberguepro.model.MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAllByOrderByDataMovimentacaoDesc();
        ByteArrayInputStream bis = relatorioService.gerarRelatorioMovimentacaoXlsx(movimentacoes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=movimentacao_estoque.xlsx");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/relatorio/xlsx")
    public ResponseEntity<InputStreamResource> gerarRelatorioXlsx(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long unidadeId) throws IOException {

        Specification<Produto> spec = Specification.where(null);
        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comNome(nome));
        }
        if (tipo != null && !tipo.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comTipo(tipo));
        }
        Unidade unidade = null;
        if (unidadeId != null && unidadeId > 0) {
            unidade = unidadeRepository.findById(unidadeId).orElse(null);
            if (unidade != null) {
                spec = spec.and(ProdutoSpecification.comUnidade(unidade));
            }
        }

        List<Produto> produtos = produtoRepository.findAll(spec);
        ByteArrayInputStream bis = relatorioService.gerarRelatorioXlsx(produtos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=estoque.xlsx");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }
}