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
            throw new IllegalStateException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        produto.setQuantidade(novaQuantidade);
        produtoRepository.save(produto);
    }

    public boolean isNomeAndTipoUnique(String nome, String tipo, Long id) {
        Optional<Produto> existingProduto;
        if (id == null) {
            // se o produto é novo, verifica se existe algum produto com o mesmo nome e tipo
            existingProduto = produtoRepository.findByNomeIgnoreCaseAndTipoIgnoreCase(nome, tipo);
        } else {
            // ao editar um produto já existente, verifica se existe outro produto com o mesmo nome e tipo
            existingProduto = produtoRepository.findByNomeIgnoreCaseAndTipoIgnoreCaseAndIdNot(nome, tipo, id);
        }
        return existingProduto.isEmpty();
    }
}