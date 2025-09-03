package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        // Cria e retorna um objeto UserDetails que o Spring Security entende
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // A senha já está criptografada no banco
                .roles(usuario.getRole()) // Ex: "ADMIN", "USER"
                .build();
    }
}