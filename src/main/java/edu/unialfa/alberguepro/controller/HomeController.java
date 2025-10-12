package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.dto.DashboardDTO;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Vaga;
import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private CadastroAcolhidoRepository cadastroAcolhidoRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ControlePatrimonioRepository controlePatrimonioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false, defaultValue = "0") int pageEstoque,
                        @RequestParam(required = false, defaultValue = "0") int pageVagas) {
        DashboardDTO dashboardDTO = new DashboardDTO();

        // Acolhidos
        dashboardDTO.setTotalAcolhidos(cadastroAcolhidoRepository.countByDataSaidaIsNull());

        // Vagas
        long vagasOcupadas = vagaRepository.countByAcolhidoIsNotNull();
        long totalVagas = vagaRepository.count();
        dashboardDTO.setVagasOcupadas(vagasOcupadas);
        dashboardDTO.setVagasLivres(totalVagas - vagasOcupadas);
        dashboardDTO.setTotalVagas(totalVagas);

        // Quartos
        long totalQuartos = Vaga.Quarto.values().length;
        long camasPorQuarto = Vaga.NumeroLeito.values().length;
        List<Object[]> occupiedBedsByRoom = vagaRepository.countOccupiedBedsByRoom();

        long quartosCheios = occupiedBedsByRoom.stream()
            .filter(result -> (Long) result[1] >= camasPorQuarto)
            .count();

        dashboardDTO.setQuartosOcupados(quartosCheios);
        dashboardDTO.setQuartosLivres(totalQuartos - quartosCheios);
        dashboardDTO.setTotalQuartos(totalQuartos);

        // Usuarios
        dashboardDTO.setTotalUsuarios(usuarioRepository.count());

        // Patrimonio
        List<ControlePatrimonio> patrimonios = controlePatrimonioRepository.findAll();
        Map<String, Long> patrimonioPorStatus = patrimonios.stream()
                .collect(Collectors.groupingBy(ControlePatrimonio::getStatus, Collectors.counting()));
        dashboardDTO.setPatrimonioPorStatus(patrimonioPorStatus);

        // Estoque
        List<Produto> produtosBaixoEstoque = produtoRepository.findTop5ByOrderByQuantidadeAsc();
        Map<String, Integer> estoqueBaixo = produtosBaixoEstoque.stream()
                .collect(Collectors.toMap(Produto::getNome, Produto::getQuantidade));
        dashboardDTO.setEstoqueBaixo(estoqueBaixo);


        model.addAttribute("dashboard", dashboardDTO);

        // Paginação para Estoque Baixo
        PagedListHolder<Produto> pagedListEstoque = new PagedListHolder<>(produtoRepository.findAll());
        pagedListEstoque.setPageSize(5);
        pagedListEstoque.setPage(pageEstoque);
        model.addAttribute("produtosBaixoEstoque", pagedListEstoque);

        // Paginação para Vagas
        List<Vaga> vagas = vagaRepository.findAll();
        PagedListHolder<Vaga> pagedListVagas = new PagedListHolder<>(vagas);
        pagedListVagas.setPageSize(5);
        pagedListVagas.setPage(pageVagas);
        model.addAttribute("vagas", pagedListVagas);


        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
