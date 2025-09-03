package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.ControleEstoque;
import edu.unialfa.alberguepro.repository.ControleEstoqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ControleEstoqueService {

    @Autowired
    private ControleEstoqueRepository repository;

    @Transactional
    public void salvar(ControleEstoque controleEstoque) {
        repository.save(controleEstoque);
    }

    public List<ControleEstoque> listarTodos() {
        return repository.findAll();
    }

    public Optional<ControleEstoque> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<ControleEstoque> buscarPorNome(String nome) {
        return repository.findByNome(nome);
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

}
