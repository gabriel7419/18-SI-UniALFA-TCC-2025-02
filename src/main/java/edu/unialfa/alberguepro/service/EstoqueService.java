package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EstoqueService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public void darBaixa(Long produtoId, Integer quantidadeParaBaixa) {
        if (quantidadeParaBaixa == null || quantidadeParaBaixa <= 0) {
            return; // Não faz nada se a quantidade for nula, zero ou negativa
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto inválido:" + produtoId));

        int novaQuantidade = produto.getQuantidade() - quantidadeParaBaixa;
        if (novaQuantidade < 0) {
            // Em um sistema real, poderíamos lançar uma exceção mais específica aqui
            // para dar um feedback melhor ao usuário.
            throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        produto.setQuantidade(novaQuantidade);
        produtoRepository.save(produto);
    }

    public boolean isNomeAndTipoUnique(String nome, String tipo, Long id) {
        Optional<Produto> existingProduto;
        if (id == null) {
            // New product, check if any product with same name and type exists
            existingProduto = produtoRepository.findByNomeIgnoreCaseAndTipoIgnoreCase(nome, tipo);
        } else {
            // Editing existing product, check if any *other* product with same name and type exists
            existingProduto = produtoRepository.findByNomeIgnoreCaseAndTipoIgnoreCaseAndIdNot(nome, tipo, id);
        }
        return existingProduto.isEmpty();
    }
}