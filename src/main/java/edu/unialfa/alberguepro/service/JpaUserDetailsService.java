package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.LockedException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca o usuário no repositório pelo nome de usuário
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        // Se não encontrar, lança uma exceção
        if (usuarioOpt.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }

        Usuario usuario = usuarioOpt.get();

        // Check if account is locked
        if (usuario.getAccountLockedUntil() != null && usuario.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("Sua conta está bloqueada até " + usuario.getAccountLockedUntil());
        }

        // Cria e retorna um objeto UserDetails que o Spring Security entende
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // A senha já está criptografada no banco
                .roles(usuario.getRole()) // Atuais: "ADMIN", "USER"
                .disabled(!usuario.isAtivo())
                .build();
    }
}