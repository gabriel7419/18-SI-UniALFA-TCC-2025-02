package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VagaService {

    @Autowired
    private VagaRepository repository;

    public void salvar(Vaga vaga) {
        repository.save(vaga);
    }

    public List<Vaga> listarTodos() {
        return repository.findAll();
    }

    public Vaga buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }
}
