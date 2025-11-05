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
    public String index(Model model) {
        DashboardDTO dashboardDTO = new DashboardDTO();

        // Acolhidos Ativos (todos os acolhidos cadastrados no sistema)
        long totalAcolhidosAtivos = cadastroAcolhidoRepository.count();
        dashboardDTO.setTotalAcolhidos(totalAcolhidosAtivos);

        // Leitos
        long totalLeitos = leitoRepository.count();
        long leitosOcupados = vagaRepository.countLeitosOcupados();
        long leitosLivres = totalLeitos - leitosOcupados;
        
        dashboardDTO.setTotalLeitos(totalLeitos);
        dashboardDTO.setLeitosOcupados(leitosOcupados);
        dashboardDTO.setLeitosLivres(leitosLivres);

        // Quartos
        long totalQuartos = quartoRepository.count();
        long quartosLivres = quartoRepository.countQuartosComLeitosLivres();
        long quartosOcupados = quartoRepository.countQuartosTotalmenteOcupados();

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

        // Estoque - Top 10 produtos com menor quantidade
        List<Produto> produtosBaixoEstoque = produtoRepository.findTop10ByOrderByQuantidadeAsc();
        Map<String, Integer> estoqueBaixo = produtosBaixoEstoque.stream()
                .collect(Collectors.toMap(Produto::getNome, Produto::getQuantidade));
        dashboardDTO.setEstoqueBaixo(estoqueBaixo);

        // Evolução de Acolhimentos - Últimos 6 meses
        List<Object[]> entradas = vagaRepository.countEntradasUltimos6Meses();
        List<Object[]> saidas = vagaRepository.countSaidasUltimos6Meses();
        
        List<String> meses = new java.util.ArrayList<>();
        List<Long> entradasList = new java.util.ArrayList<>();
        List<Long> saidasList = new java.util.ArrayList<>();
        
        // Preencher últimos 6 meses
        java.time.LocalDate hoje = java.time.LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate data = hoje.minusMonths(i);
            int mes = data.getMonthValue();
            int ano = data.getYear();
            
            String nomeMes = data.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, new java.util.Locale("pt", "BR"));
            meses.add(nomeMes + "/" + String.valueOf(ano).substring(2));
            
            // Buscar entradas do mês
            long qtdEntradas = entradas.stream()
                .filter(e -> ((Number)e[0]).intValue() == mes && ((Number)e[1]).intValue() == ano)
                .mapToLong(e -> ((Number)e[2]).longValue())
                .sum();
            entradasList.add(qtdEntradas);
            
            // Buscar saídas do mês
            long qtdSaidas = saidas.stream()
                .filter(s -> ((Number)s[0]).intValue() == mes && ((Number)s[1]).intValue() == ano)
                .mapToLong(s -> ((Number)s[2]).longValue())
                .sum();
            saidasList.add(qtdSaidas);
        }
        
        dashboardDTO.setMesesEvolucao(meses);
        dashboardDTO.setEntradasEvolucao(entradasList);
        dashboardDTO.setSaidasEvolucao(saidasList);

        model.addAttribute("dashboard", dashboardDTO);
        model.addAttribute("produtosBaixoEstoque", new PagedListHolder<>(produtosBaixoEstoque));

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
