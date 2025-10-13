package edu.unialfa.alberguepro.config;

import edu.unialfa.alberguepro.model.Unidade;
import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UnidadeRepository;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UnidadeRepository unidadeRepository;

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

        // Verifica se as unidades de medida já existem
        if (unidadeRepository.count() == 0) {
            System.out.println("Criando unidades de medida padrão...");

            Unidade un1 = new Unidade(); un1.setNome("Unidade");
            Unidade un2 = new Unidade(); un2.setNome("Caixa");
            Unidade un3 = new Unidade(); un3.setNome("Pacote");
            Unidade un4 = new Unidade(); un4.setNome("Litro");
            Unidade un5 = new Unidade(); un5.setNome("Kg");

            unidadeRepository.saveAll(Arrays.asList(un1, un2, un3, un4, un5));
            System.out.println("Unidades de medida criadas com sucesso!");
        }
    }
}