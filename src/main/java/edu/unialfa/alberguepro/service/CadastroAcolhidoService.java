package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.repository.CadastroAcolhidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CadastroAcolhidoService {

    @Autowired
    private CadastroAcolhidoRepository repository;

    public void salvar(CadastroAcolhido cadastroAcolhido) {
        repository.save(cadastroAcolhido);
    }

    public List<CadastroAcolhido> listarTodos() {
        return repository.findAll();
    }

    public CadastroAcolhido buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Acolhido não encontrado: " + id));
    }

    public List<CadastroAcolhido> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }

    public void deletarPorId(Long id) {
        repository.deleteById(id);
    }

    public boolean cpfJaExiste(String cpf) {
        return repository.existsByCpf(cpf);
    }
}
