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
        // Verificar se é uma edição
        if (usuario.getId() != null) {
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
            
            // Obter o usuário logado
            String usernameLogado = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isMaster = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));
            boolean isSelf = usuarioExistente.getUsername().equals(usernameLogado);
            
            // Impedir que um admin edite outro admin ou master (exceto se for ele mesmo)
            if (isAdmin && !isMaster && !isSelf && 
                ("ADMIN".equals(usuarioExistente.getRole()) || "MASTER".equals(usuarioExistente.getRole()))) {
                throw new IllegalArgumentException("Não é permitido editar outro administrador ou master.");
            }
            
            // APENAS MASTER pode promover alguém para MASTER
            if ("MASTER".equals(usuario.getRole()) && !"MASTER".equals(usuarioExistente.getRole())) {
                if (!isMaster) {
                    throw new IllegalArgumentException("Apenas um Master pode promover usuários para Master.");
                }
            }
            
            // Impedir que alguém edite um Master (exceto outro Master ou ele mesmo)
            if ("MASTER".equals(usuarioExistente.getRole()) && !isMaster && !isSelf) {
                throw new IllegalArgumentException("Apenas um Master pode editar outro Master.");
            }
            
            // se uma nova senha foi fornecida (não está em branco), criptografa e atualiza
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else {
                // se não, mantém a senha atual
                usuario.setPassword(usuarioExistente.getPassword());
            }
            
            // Manter outros campos que não devem ser alterados
            usuario.setDataCriacao(usuarioExistente.getDataCriacao());
            usuario.setFailedLoginAttempts(usuarioExistente.getFailedLoginAttempts());
            usuario.setAccountLockedUntil(usuarioExistente.getAccountLockedUntil());
        } else {
            // Validação para criação de novo usuário Master
            boolean isMaster = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));
            
            if ("MASTER".equals(usuario.getRole()) && !isMaster) {
                throw new IllegalArgumentException("Apenas um Master pode criar outro Master.");
            }
            
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
        boolean isMaster = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));

        Usuario usuarioParaExcluir = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + id));

        if (usuarioParaExcluir.getUsername().equals(usernameLogado)) {
            throw new IllegalStateException("Não é possível excluir o usuário logado.");
        }

        // Impedir exclusão de usuários administradores e masters
        if ("ADMIN".equals(usuarioParaExcluir.getRole())) {
            throw new IllegalStateException("Não é possível excluir usuários administradores.");
        }
        
        if ("MASTER".equals(usuarioParaExcluir.getRole())) {
            throw new IllegalStateException("Não é possível excluir usuários master.");
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

    public org.springframework.data.domain.Page<UsuarioDTO> findAllDTOPaginado(org.springframework.data.domain.Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioDTO::new);
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
        boolean isMaster = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));
        boolean isSelf = usuario.getUsername().equals(usernameLogado);
        
        // Impedir que um admin altere a senha de outro admin ou master
        if (isAdmin && !isMaster && !isSelf && 
            ("ADMIN".equals(usuario.getRole()) || "MASTER".equals(usuario.getRole()))) {
            throw new IllegalArgumentException("Não é permitido alterar a senha de outro administrador ou master.");
        }
        
        // Impedir que alguém (exceto Master) altere a senha de um Master
        if ("MASTER".equals(usuario.getRole()) && !isMaster && !isSelf) {
            throw new IllegalArgumentException("Apenas um Master pode alterar a senha de outro Master.");
        }

        // Exige senha atual se o próprio usuário estiver alterando ou se não for master
        boolean mustValidateCurrent = isSelf || (!isMaster);
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