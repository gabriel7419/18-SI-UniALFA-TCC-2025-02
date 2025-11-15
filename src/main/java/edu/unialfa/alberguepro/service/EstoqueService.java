package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EstoqueService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Transactional
    public Produto salvar(Produto produto) {
        Integer quantidadeAnterior = 0;
        MovimentacaoEstoque.TipoMovimentacao tipoMovimentacao;
        Integer quantidadeMovimentada;
        String observacao;

        if (produto.getId() == null) {
            // Novo produto
            tipoMovimentacao = MovimentacaoEstoque.TipoMovimentacao.ENTRADA;
            quantidadeMovimentada = produto.getQuantidade();
            observacao = "Criação de novo produto no estoque.";
        } else {
            // Produto existente
            Produto produtoExistente = produtoRepository.findById(produto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto inválido para atualização: " + produto.getId()));
            quantidadeAnterior = produtoExistente.getQuantidade();

            if (produto.getQuantidade() > quantidadeAnterior) {
                tipoMovimentacao = MovimentacaoEstoque.TipoMovimentacao.AJUSTE_POSITIVO;
                quantidadeMovimentada = produto.getQuantidade() - quantidadeAnterior;
                observacao = "Ajuste manual de estoque (aumento).";
            } else if (produto.getQuantidade() < quantidadeAnterior) {
                tipoMovimentacao = MovimentacaoEstoque.TipoMovimentacao.AJUSTE_NEGATIVO;
                quantidadeMovimentada = quantidadeAnterior - produto.getQuantidade();
                observacao = "Ajuste manual de estoque (redução).";
            } else {
                // A quantidade não mudou, apenas salva outras possíveis alterações (nome, tipo, etc.)
                return produtoRepository.save(produto);
            }
        }

        Produto produtoSalvo = produtoRepository.save(produto);

        movimentacaoEstoqueService.registrarMovimentacao(produtoSalvo, tipoMovimentacao,
                quantidadeMovimentada, quantidadeAnterior, produtoSalvo.getQuantidade(), observacao);

        return produtoSalvo;
    }

    @Transactional
    public void darBaixa(Long produtoId, Integer quantidadeParaBaixa) {
        if (quantidadeParaBaixa == null || quantidadeParaBaixa <= 0) {
            return; // Não faz nada se a quantidade for nula, zero ou negativa
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto inválido:" + produtoId));

        int quantidadeAnterior = produto.getQuantidade();
        int novaQuantidade = quantidadeAnterior - quantidadeParaBaixa;
        if (novaQuantidade < 0) {
            throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        produto.setQuantidade(novaQuantidade);

        movimentacaoEstoqueService.registrarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.SAIDA, 
            quantidadeParaBaixa, quantidadeAnterior, novaQuantidade, "Baixa de produto");

        produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto inválido para exclusão: " + id));

        Integer quantidadeAnterior = produto.getQuantidade();

        movimentacaoEstoqueService.registrarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.EXCLUSAO,
                quantidadeAnterior, quantidadeAnterior, 0, "Exclusão de produto do sistema.");

        produtoRepository.delete(produto);
    }

    public boolean isNomeAndTipoUnique(String nome, String tipo, Long id) {
        Optional<Produto> existingProduto;
        if (id == null) {
            existingProduto = produtoRepository.findByNomeIgnoreCaseAndTipoIgnoreCase(nome, tipo);
        } else {
            existingProduto = produtoRepository.findByNomeIgnoreCaseAndTipoIgnoreCaseAndIdNot(nome, tipo, id);
        }
        return existingProduto.isEmpty();
    }

    public List<Produto> buscarProdutosProximosVencimento(Integer dias) {
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return produtoRepository.findByNaoPerecivelFalseAndDataDeVencimentoBetweenOrderByDataDeVencimentoAsc(
            LocalDate.now(), dataLimite);
    }
}