package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Leito;
import edu.unialfa.alberguepro.repository.LeitoRepository;
import edu.unialfa.alberguepro.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeitoService {

    @Autowired
    private LeitoRepository repository;

    @Autowired
    private VagaRepository vagaRepository;

    public List<Leito> buscarLeitosLivresPorQuartoId(Long quartoId) {

        List<Long> leitosOcupadosIds = vagaRepository.findOccupiedLeitoIds();

        List<Leito> todosLeitosDoQuarto = repository.findByQuartoId(quartoId);

        return todosLeitosDoQuarto.stream()
                .filter(leito -> !leitosOcupadosIds.contains(leito.getId()))
                .collect(Collectors.toList());
    }


    public List<Leito> listarTodos() {
        return repository.findAll();
    }

    public Leito buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Leito> buscarPorQuartoId(Long quartoId) {
        return repository.findByQuartoId(quartoId);
    }
    
    public List<Leito> buscarLeitosDisponiveisPorQuartoId(Long quartoId) {
        return repository.findLeitosDisponiveisByQuartoId(quartoId);
    }

}