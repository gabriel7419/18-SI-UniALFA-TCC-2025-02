package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.dto.DashboardDTO;
import edu.unialfa.alberguepro.model.ControlePatrimonio;
import edu.unialfa.alberguepro.model.Leito;
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
    private LeitoRepository leitoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ControlePatrimonioRepository controlePatrimonioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false, defaultValue = "0") int pageEstoque,
                        @RequestParam(required = false, defaultValue = "0") int pageLeitos) {
        DashboardDTO dashboardDTO = new DashboardDTO();

        // Acolhidos
        dashboardDTO.setTotalAcolhidos(cadastroAcolhidoRepository.countByDataSaidaIsNull());

        // Leitos
        long leitosOcupados = leitoRepository.countByAcolhidoIsNotNull();
        long totalLeitos = (long) Leito.Quarto.values().length * Leito.NumeroLeito.values().length;
        dashboardDTO.setLeitosOcupados(leitosOcupados);
        dashboardDTO.setLeitosLivres(totalLeitos - leitosOcupados);
        dashboardDTO.setTotalLeitos(totalLeitos);

        // Quartos
        long totalQuartos = Leito.Quarto.values().length;
        long camasPorQuarto = Leito.NumeroLeito.values().length;
        List<Object[]> occupiedBedsByRoom = leitoRepository.countOccupiedBedsByRoom();

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

        // Paginação para Leitos
        List<Leito> leitos = leitoRepository.findAll();
        PagedListHolder<Leito> pagedListLeitos = new PagedListHolder<>(leitos);
        pagedListLeitos.setPageSize(5);
        pagedListLeitos.setPage(pageLeitos);
        model.addAttribute("leitos", pagedListLeitos);


        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
