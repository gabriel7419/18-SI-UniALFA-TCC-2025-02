package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Leito;
import edu.unialfa.alberguepro.repository.LeitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeitoService {

    @Autowired
    private LeitoRepository repository;

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