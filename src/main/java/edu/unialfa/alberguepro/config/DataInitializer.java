package edu.unialfa.alberguepro.config;

import edu.unialfa.alberguepro.model.Unidade;
import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.repository.UnidadeRepository;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import edu.unialfa.alberguepro.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaRepository vagaRepository;

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

        // Verifica se as vagas já existem
        if (vagaRepository.count() == 0) {
            System.out.println("Criando vagas padrão...");
            List<Vaga> vagas = new ArrayList<>();
            for (Vaga.Quarto quarto : Vaga.Quarto.values()) {
                for (Vaga.NumeroLeito leito : Vaga.NumeroLeito.values()) {
                    Vaga vaga = new Vaga();
                    vaga.setQuarto(quarto);
                    vaga.setNumeroLeito(leito);
                    vagas.add(vaga);
                }
            }
            vagaRepository.saveAll(vagas);
            System.out.println(vagas.size() + " vagas criadas com sucesso!");
        }
    }
}