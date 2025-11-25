package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.repository.ControlePatrimonioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ControlePatrimonioService {

    @Autowired
    private ControlePatrimonioRepository repository;

    public void salvar(ControlePatrimonio patrimonio) throws IllegalArgumentException {
        ControlePatrimonio patrimonioExistente = null;

        if (patrimonio.getId() == null) {
            patrimonioExistente = repository.findByPatrimonio(patrimonio.getPatrimonio());
        } else {
            patrimonioExistente = repository.findByPatrimonioAndIdNot(
                patrimonio.getPatrimonio(), 
                patrimonio.getId()
            );
        }

        if (patrimonioExistente != null) {
            throw new IllegalArgumentException("O número de patrimônio " + patrimonio.getPatrimonio() + " já está cadastrado.");
        }

        repository.save(patrimonio);
    }
}
