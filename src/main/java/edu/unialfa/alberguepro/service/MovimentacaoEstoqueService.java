package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.MovimentacaoEstoque;
import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.MovimentacaoEstoqueRepository;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoEstoqueService {

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarMovimentacao(Produto produto, MovimentacaoEstoque.TipoMovimentacao tipo,
        Integer quantidadeMovimentada, Integer quantidadeAnterior,
        Integer quantidadePosterior, String observacao) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para registrar a movimentação."));

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setUsuario(usuario);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidadeMovimentada(quantidadeMovimentada);
        movimentacao.setQuantidadeAnterior(quantidadeAnterior);
        movimentacao.setQuantidadePosterior(quantidadePosterior);
        movimentacao.setObservacao(observacao);

        movimentacaoEstoqueRepository.save(movimentacao);
    }
}
