package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import edu.unialfa.alberguepro.dto.UsuarioDTO;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void salvar(Usuario usuario) {
        // validacao se o usuario já existe
        if (usuario.getId() != null) {
            // se uma nova senha foi fornecida (não está em branco), criptografa e atualiza
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else {
                // se não, busca a senha atual no banco e a mantém
                usuarioRepository.findById(usuario.getId()).ifPresent(usuarioExistente -> {
                    usuario.setPassword(usuarioExistente.getPassword());
                });
            }
        } else {
            // Se é um usuário novo, valida e criptografa a senha
            if (usuario.getPassword() == null || usuario.getPassword().length() < 8) {
                throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres.");
            }
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        usuarioRepository.save(usuario);
    }

    public boolean isUsernameUnique(String username, Long id) {
        Optional<Usuario> existingUser;
        if (id == null) {
            // Usuário novo: verifica se o username já existe
            existingUser = usuarioRepository.findByUsernameIgnoreCase(username);
        } else {
            // Usuário existente: verifica se o username pertence a outro usuário
            existingUser = usuarioRepository.findByUsernameIgnoreCaseAndIdNot(username, id);
        }
        return existingUser.isEmpty(); // Retorna true se estiver vazio (único)
    }

    public void excluir(Long id) {
        String usernameLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuarioParaExcluir = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + id));

        if (usuarioParaExcluir.getUsername().equals(usernameLogado)) {
            throw new IllegalStateException("Não é possível excluir o usuário logado.");
        }

        usuarioRepository.deleteById(id);
    }

    @Transactional
    public void toggleAtivo(Long id) {
        String usernameLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + id));

        // Check if the user is trying to deactivate themselves
        if (usuario.getUsername().equals(usernameLogado) && usuario.isAtivo()) {
            throw new IllegalStateException("Não é possível desativar o próprio usuário.");
        }

        // Inverte o status atual (se era true, vira false; se era false, vira true)
        usuario.setAtivo(!usuario.isAtivo());

        // Salva a alteração no banco de dados
        usuarioRepository.save(usuario);
    }

    public List<UsuarioDTO> findAllDTO() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<UsuarioDTO> findByIdDTO(Long id) {
        return usuarioRepository.findById(id).map(UsuarioDTO::new);
    }

    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + id));

        String usernameLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSelf = usuario.getUsername().equals(usernameLogado);

        // Exige senha atual se o próprio usuário estiver alterando ou se não for admin
        boolean mustValidateCurrent = isSelf || !isAdmin;
        if (mustValidateCurrent) {
            if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
                throw new IllegalArgumentException("Senha atual incorreta.");
            }
        }

        if (novaSenha == null || novaSenha.length() < 8) {
            throw new IllegalArgumentException("A nova senha deve ter no mínimo 8 caracteres.");
        }
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}