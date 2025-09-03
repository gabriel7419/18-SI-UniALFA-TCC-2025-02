package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}