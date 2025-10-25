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

    @Autowired
    private QuartoRepository quartoRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false, defaultValue = "0") int pageEstoque,
                        @RequestParam(required = false, defaultValue = "0") int pageLeitos) {
        DashboardDTO dashboardDTO = new DashboardDTO();

        // Acolhidos Ativos (todos os acolhidos cadastrados no sistema)
        long totalAcolhidosAtivos = cadastroAcolhidoRepository.count();
        dashboardDTO.setTotalAcolhidos(totalAcolhidosAtivos);

        // Leitos
        long totalLeitos = leitoRepository.count();
        long leitosOcupados = vagaRepository.countByAcolhidoIsNotNullAndDataSaidaIsNull();
        long leitosLivres = totalLeitos - leitosOcupados;
        
        dashboardDTO.setTotalLeitos(totalLeitos);
        dashboardDTO.setLeitosOcupados(leitosOcupados);
        dashboardDTO.setLeitosLivres(leitosLivres);

        // Quartos
        long totalQuartos = quartoRepository.count();
        List<Object[]> occupiedBedsByRoom = vagaRepository.countOccupiedBedsByRoom();
        
        long quartosOcupados = occupiedBedsByRoom.size();
        long quartosLivres = totalQuartos - quartosOcupados;

        dashboardDTO.setTotalQuartos(totalQuartos);
        dashboardDTO.setQuartosOcupados(quartosOcupados);
        dashboardDTO.setQuartosLivres(quartosLivres);

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
        List<Vaga> leitos = vagaRepository.findAll();
        PagedListHolder<Vaga> pagedListLeitos = new PagedListHolder<>(leitos);
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
