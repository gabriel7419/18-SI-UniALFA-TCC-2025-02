package edu.unialfa.alberguepro.config;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se o usuário 'admin' já existe
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            System.out.println("Criando usuário administrador padrão...");

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            // Criptografa a senha usando o PasswordEncoder da aplicação
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setAtivo(true);

            usuarioRepository.save(admin);
            
            System.out.println("Usuário administrador criado com sucesso!");
        } else {
            System.out.println("Usuário administrador já existe. Nenhuma ação necessária.");
        }
    }
}