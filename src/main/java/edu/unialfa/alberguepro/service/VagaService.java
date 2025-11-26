package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VagaService {

    @Autowired
    private VagaRepository repository;

    public void salvar(Vaga vaga) {

        Long quartoId = null;
        String numeroQuarto = "Desconhecido";

        if (vaga.getLeito() != null && vaga.getLeito().getQuarto() != null) {
            quartoId = vaga.getLeito().getQuarto().getId();
            numeroQuarto = vaga.getLeito().getQuarto().getNumeroQuarto();
        } else {
            throw new IllegalArgumentException("Erro de sistema: A vaga deve estar associada a um Quarto válido.");
        }

        final int MAX_VAGAS = 4;

        if (quartoId != null) {

            // Conta as vagas ativas
            Long vagasAtivas = repository.countActiveVagasByQuartoId(quartoId);

            // Valida o limite
            if (vagasAtivas >= MAX_VAGAS) {
                throw new IllegalArgumentException("Limite de vagas atingido! O Quarto "
                        + numeroQuarto
                        + " já possui o máximo de " + MAX_VAGAS + " acolhidos ativos.");
            }
        }

        repository.save(vaga);
    }

    public List<Vaga> listarTodos() {
        return repository.findAll();
    }

    public Page<Vaga> listarTodosPaginado(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Vaga buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

    public List<Vaga> buscarPorNomeAcolhido(String nome) {
        return repository.findByAcolhidoNomeContainingIgnoreCase(nome);
    }

    public Page<Vaga> buscarPorNomeAcolhidoPaginado(String nome, Pageable pageable) {
        return repository.findByAcolhidoNomeContainingIgnoreCase(nome, pageable);
    }

    public Page<Vaga> buscarComFiltros(String nomeAcolhido, String numeroQuarto, String numeroLeito, Pageable pageable) {
        if ((nomeAcolhido == null || nomeAcolhido.trim().isEmpty()) &&
            (numeroQuarto == null || numeroQuarto.trim().isEmpty()) &&
            (numeroLeito == null || numeroLeito.trim().isEmpty())) {
            return repository.findAll(pageable);
        }
        return repository.findByFiltros(nomeAcolhido, numeroQuarto, numeroLeito, pageable);
    }
}
