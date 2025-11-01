package edu.unialfa.alberguepro.config;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_TIME_MINUTES = 10;

    public CustomAuthenticationFailureHandler() {
        setDefaultFailureUrl("/login?error=true");
    }

    @Override
    @Transactional
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        String errorMessage = "Usuário ou senha inválidos.";

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.isAtivo()) {
                if (usuario.getFailedLoginAttempts() < MAX_FAILED_ATTEMPTS - 1) {
                    usuario.setFailedLoginAttempts(usuario.getFailedLoginAttempts() + 1);
                    usuarioRepository.updateFailedLoginAttempts(usuario.getFailedLoginAttempts(), username);
                } else {
                    usuario.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
                    usuario.setFailedLoginAttempts(0);
                    usuarioRepository.updateAccountLockedUntil(usuario.getAccountLockedUntil(), username);
                    usuarioRepository.updateFailedLoginAttempts(usuario.getFailedLoginAttempts(), username);
                    errorMessage = "Sua conta foi bloqueada devido a muitas tentativas de login falhas. Tente novamente em " + LOCK_TIME_MINUTES + " minutos.";
                }
            } else {
                errorMessage = "Sua conta está inativa.";
            }
        }

        // Salva a mensagem de erro na sessão
        request.getSession().setAttribute("errorMessage", errorMessage);

        // Cria uma nova exceção com a mensagem personalizada
        AuthenticationException newException = new LockedException(errorMessage);

        super.onAuthenticationFailure(request, response, newException);
    }
}
