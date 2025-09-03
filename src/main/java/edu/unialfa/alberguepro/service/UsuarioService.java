package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void salvar(Usuario usuario) {
        // Se o usuário já existe
        if (usuario.getId() != null) {
            // Se uma nova senha foi fornecida (não está em branco), criptografa e atualiza
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            } else {
                // Senão, busca a senha atual no banco e a mantém
                usuarioRepository.findById(usuario.getId()).ifPresent(usuarioExistente -> {
                    usuario.setPassword(usuarioExistente.getPassword());
                });
            }
        } else {
            // Se é um usuário novo, apenas criptografa a senha
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        usuarioRepository.save(usuario);
    }

        public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }

     @Transactional
    public void toggleAtivo(Long id) {
        // Busca o usuário no banco ou lança uma exceção se não encontrar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + id));
        
        // Inverte o status atual (se era true, vira false; se era false, vira true)
        usuario.setAtivo(!usuario.isAtivo());
        
        // Salva a alteração no banco de dados
        usuarioRepository.save(usuario);
    }
}