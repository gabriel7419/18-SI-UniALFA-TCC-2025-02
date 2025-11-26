package edu.unialfa.alberguepro.service;

import edu.unialfa.alberguepro.model.Quarto;
import edu.unialfa.alberguepro.model.Leito; // Novo Import
import edu.unialfa.alberguepro.repository.LeitoRepository;
import edu.unialfa.alberguepro.repository.QuartoRepository;
import edu.unialfa.alberguepro.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartoService {

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private VagaRepository vagaRepository;

    public List<Quarto> listarTodos() {
        return quartoRepository.findAll();
    }

    public Page<Quarto> listarTodosPaginado(Pageable pageable) {
        return quartoRepository.findAll(pageable);
    }

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

        Quarto quarto = quartoRepository.findById(id).orElse(null);

        if (quarto == null) {

            return;
        }

        long vagasExistentes = vagaRepository.countVagasByQuartoId(id);

        if (vagasExistentes > 0) {
            throw new IllegalArgumentException(
                    "Não é possível excluir o Quarto " + quarto.getNumeroQuarto() +
                            " pois seus leitos estão sendo utilizados por " + vagasExistentes +
                            " acolhidos. Em Vagas, remova os acolhidos dos leitos antes de deletar o quarto."
            );
        }

        try {
            quartoRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Falha crítica ao tentar deletar o quarto. Houve um erro de dependência inesperado.", e);
        }
    }

    public Page<Quarto> buscarComFiltros(String numeroQuarto, Integer capacidadeMin, String disponibilidade, Pageable pageable) {
        List<Quarto> todosQuartos = quartoRepository.findAll();
        
        // Filtrar por número do quarto
        if (numeroQuarto != null && !numeroQuarto.trim().isEmpty()) {
            todosQuartos = todosQuartos.stream()
                    .filter(q -> q.getNumeroQuarto().contains(numeroQuarto))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Filtrar por capacidade mínima
        if (capacidadeMin != null && capacidadeMin > 0) {
            todosQuartos = todosQuartos.stream()
                    .filter(q -> q.getLeitos().size() >= capacidadeMin)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Filtrar por disponibilidade
        if (disponibilidade != null && !disponibilidade.trim().isEmpty()) {
            todosQuartos = todosQuartos.stream()
                    .filter(q -> {
                        long leitosOcupados = vagaRepository.countOccupiedBedsByRoom().stream()
                                .filter(obj -> obj[0].equals(q.getNumeroQuarto()))
                                .mapToLong(obj -> ((Number) obj[1]).longValue())
                                .sum();
                        long totalLeitos = q.getLeitos().size();
                        
                        if ("disponivel".equals(disponibilidade)) {
                            return leitosOcupados < totalLeitos;
                        } else if ("ocupado".equals(disponibilidade)) {
                            return leitosOcupados >= totalLeitos;
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Criar página manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todosQuartos.size());
        List<Quarto> paginatedList = todosQuartos.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(paginatedList, pageable, todosQuartos.size());
    }
}