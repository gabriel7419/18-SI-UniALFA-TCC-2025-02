package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.model.Leito; // Novo Import
import edu.unialfa.alberguepro.repository.QuartoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartoService {

    @Autowired
    private QuartoRepository quartoRepository;

    public void salvar(Quarto quarto) throws IllegalArgumentException {

        String inputNumeroQuarto = quarto.getNumeroQuarto();

        try {
            int numero = Integer.parseInt(inputNumeroQuarto);
            if (numero <= 0) {
                throw new IllegalArgumentException("O número do quarto deve ser positivo.");
            }

            if (numero < 10 && !inputNumeroQuarto.startsWith("0")) {
                throw new IllegalArgumentException("Para números de 1 a 9, use sempre dois dígitos: 01, 02, ..., 09.");
            }

            quarto.setNumeroQuarto(String.format("%02d", numero));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O número do quarto é inválido. Use apenas números.");
        }

        Quarto quartoExistente = null;

        if (quarto.getId() == null) {

            quartoExistente = quartoRepository.findByNumeroQuarto(quarto.getNumeroQuarto());
        } else {

            quartoExistente = quartoRepository.findByNumeroQuartoAndIdNot(
                    quarto.getNumeroQuarto(),
                    quarto.getId()
            );
        }

        if (quartoExistente != null) {
            throw new IllegalArgumentException("O número do quarto '" + quarto.getNumeroQuarto() + "' já está cadastrado.");
        }

        boolean isNew = quarto.getId() == null;

        if (isNew) {

            quarto.getLeitos().clear();

            final int CAPACIDADE_MAXIMA = 4;

            for (int i = 1; i <= CAPACIDADE_MAXIMA; i++) {
                Leito leito = new Leito();

                String numeroFormatado = String.format("%02d", i);

                leito.setNumeroLeito(numeroFormatado);

                quarto.addLeito(leito);
            }
        }

        quartoRepository.save(quarto);
    }

    public void deletarPorId(Long id) {
        quartoRepository.deleteById(id);
    }
}