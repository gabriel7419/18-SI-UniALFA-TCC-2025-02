package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cadastroAcolhido")
public class CadastroAcolhidoController {

    private static final Logger log = LoggerFactory.getLogger(CadastroAcolhidoController.class);

    @Autowired
    private CadastroAcolhidoService service;

    @GetMapping
    public String iniciar(Model model) {
        model.addAttribute("acolhido", new CadastroAcolhido());
        carregarListas(model);
        model.addAttribute("stepWithError", 1);
        return "cadastroAcolhido/form";
    }

    @PostMapping("salvar")
    public String salvar(@ModelAttribute("acolhido") CadastroAcolhido acolhido, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {

        if (acolhido.getNome() == null || acolhido.getNome().trim().isEmpty()) {
            result.rejectValue("nome", "campo.obrigatorio", "O nome é obrigatório.");
        } else if (!acolhido.getNome().matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s]{2,}$")) {
            result.rejectValue("nome", "nome.invalido", "O nome deve ter pelo menos 2 letras e conter apenas caracteres alfabéticos.");
        }

        if (acolhido.getDataNascimento() == null) {
            result.rejectValue("dataNascimento", "campo.obrigatorio", "A data de nascimento é obrigatória.");
        } else if (acolhido.getDataNascimento().isAfter(LocalDate.now())) {
            result.rejectValue("dataNascimento", "data.invalida", "A data de nascimento não pode ser futura.");
        } else {
            int idade = Period.between(acolhido.getDataNascimento(), LocalDate.now()).getYears();
            if (idade < 18 || idade > 130 ) {
                result.rejectValue("dataNascimento", "idade.invalida", "O acolhido deve ter no mínimo 18 anos e no maximo 130 anos.");
            } else {
                acolhido.setIdade(idade);
            }
        }

        if (acolhido.getNaturalidade() == null || acolhido.getNaturalidade().trim().isEmpty()) {
            result.rejectValue("naturalidade", "campo.obrigatorio", "A naturalidade é obrigatória.");
        }

        if (acolhido.getSexo() == null) {
            result.rejectValue("sexo", "campo.obrigatorio", "O sexo é obrigatório.");
        }

        if (acolhido.getCor() == null || acolhido.getCor().trim().isEmpty()) {
            result.rejectValue("cor", "campo.obrigatorio", "A cor é obrigatória.");
        }

        if (acolhido.getRg() == null || acolhido.getRg().trim().isEmpty()) {
            result.rejectValue("rg", "campo.obrigatorio", "O RG é obrigatório.");
        } else {
            String rgLimpo = acolhido.getRg().replaceAll("[^0-9A-Za-z]", "");

            if (!rgLimpo.matches("\\d{5,9}[A-Za-z]?")) {
                result.rejectValue("rg", "rg.invalido", "O RG informado é inválido. Deve ter entre 5 e 9 números, podendo ter uma letra no final.");
            }
        }

        String cpfLimpo = null;
        if (acolhido.getCpf() == null || acolhido.getCpf().trim().isEmpty()) {
            result.rejectValue("cpf", "campo.obrigatorio", "O CPF é obrigatório.");
        } else {
            cpfLimpo = acolhido.getCpf().replaceAll("\\D", "");

            if (!cpfLimpo.matches("\\d{11}")) {
                result.rejectValue("cpf", "cpf.invalido", "O CPF informado é inválido. Deve ter exatamente 11 números.");
            } else if (!isCpfValido(cpfLimpo)) {
                result.rejectValue("cpf", "cpf.invalido", "O CPF informado é inválido.");
            } else if (acolhido.getId() == null && service.cpfJaExiste(cpfLimpo)) {
                result.rejectValue("cpf", "cpf.duplicado", "CPF já existente no sistema.");
            }
        }

        if (acolhido.getCertidaoNascimento() == null || acolhido.getCertidaoNascimento().trim().isEmpty()) {
            result.rejectValue("certidaoNascimento", "campo.obrigatorio", "A certidão de nascimento é obrigatória.");
        }

        if (acolhido.getFiliacao() == null || acolhido.getFiliacao().trim().isEmpty()) {
            result.rejectValue("filiacao", "campo.obrigatorio", "A filiação é obrigatória.");
        }

        if (acolhido.getEstadoCivil() == null) {
            result.rejectValue("estadoCivil", "campo.obrigatorio", "O estado civil é obrigatório.");
        } else if (acolhido.getEstadoCivil() == CadastroAcolhido.EstadoCivil.Casado
                || acolhido.getEstadoCivil() == CadastroAcolhido.EstadoCivil.UniaoEstavel) {

            if (acolhido.getNomeConjuge() == null || acolhido.getNomeConjuge().trim().isEmpty()) {
                result.rejectValue("nomeConjuge", "campo.obrigatorio", "O nome do cônjuge é obrigatório.");
            } else if (!acolhido.getNomeConjuge().matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s]{2,}$")) {
                result.rejectValue("nomeConjuge", "nome.invalido", "O nome do cônjuge deve ter pelo menos 2 letras e conter apenas caracteres alfabéticos.");
            }
        } else {
            acolhido.setNomeConjuge(null);
        }

        if (acolhido.getFilho() == null) {
            result.rejectValue("filho", "campo.obrigatorio", "Informe se possui filhos.");
        } else if (acolhido.getFilho() == CadastroAcolhido.Filho.Sim) {
            if (acolhido.getQuantidadeFilhos() == null || acolhido.getQuantidadeFilhos() <= 0) {
                result.rejectValue("quantidadeFilhos", "campo.obrigatorio", "Informe a quantidade de filhos.");
            } else if (acolhido.getQuantidadeFilhos() > 30) {
                result.rejectValue("quantidadeFilhos", "quantidade.excedida", "A quantidade de filhos não pode ser maior que 30.");
            }
        }


        if (acolhido.getEndereco() == null || acolhido.getEndereco().trim().isEmpty()) {
            result.rejectValue("endereco", "campo.obrigatorio", "O endereço é obrigatório.");
        }

        if (acolhido.getEscolaridade() == null) {
            result.rejectValue("escolaridade", "campo.obrigatorio", "A escolaridade é obrigatória.");
        }

        if (acolhido.getRenda() == null) {
            result.rejectValue("renda", "campo.obrigatorio", "Informe se possui renda.");
        }

        if (acolhido.getBeneficioSocial() == null) {
            result.rejectValue("beneficioSocial", "campo.obrigatorio", "Informe se recebe benefício social.");
        } else if (acolhido.getBeneficioSocial() == CadastroAcolhido.BeneficioSocial.Sim
                && (acolhido.getQualBeneficio() == null || acolhido.getQualBeneficio().trim().isEmpty())) {
            result.rejectValue("qualBeneficio", "campo.obrigatorio", "Informe qual benefício recebe.");
        }

        if (acolhido.getEstadoSaude() == null) {
            result.rejectValue("estadoSaude", "campo.obrigatorio", "O estado de saúde é obrigatório.");
        }

        if (acolhido.getPossuiAlergia() == null) {
            result.rejectValue("possuiAlergia", "campo.obrigatorio", "Informe se possui alergia.");
        } else if (acolhido.getPossuiAlergia() == CadastroAcolhido.PossuiAlergia.Sim
                && (acolhido.getQualAlergia() == null || acolhido.getQualAlergia().trim().isEmpty())) {
            result.rejectValue("qualAlergia", "campo.obrigatorio", "Informe a alergia.");
        }

        if (acolhido.getFumante() == null) {
            result.rejectValue("fumante", "campo.obrigatorio", "Informe se é fumante.");
        }

        if (acolhido.getBebidaAlcoolica() == null) {
            result.rejectValue("bebidaAlcoolica", "campo.obrigatorio", "Informe se consome bebida alcoólica.");
        }

        if (acolhido.getUsaDrogas() == null) {
            result.rejectValue("usaDrogas", "campo.obrigatorio", "Informe se utiliza drogas.");
        } else if (acolhido.getUsaDrogas() == CadastroAcolhido.UsaDrogas.Sim
                && (acolhido.getQualDroga() == null || acolhido.getQualDroga().trim().isEmpty())) {
            result.rejectValue("qualDroga", "campo.obrigatorio", "Informe qual droga utiliza.");
        }

        if (acolhido.getSituacaoRua() == null) {
            result.rejectValue("situacaoRua", "campo.obrigatorio", "Informe se está em situação de rua.");
        } else if (acolhido.getSituacaoRua() == CadastroAcolhido.SituacaoRua.Sim
                && (acolhido.getTempoRua() == null || acolhido.getTempoRua().trim().isEmpty())) {
            result.rejectValue("tempoRua", "campo.obrigatorio", "Informe o tempo na rua.");
        }

        if (acolhido.getVinculoFamiliar() == CadastroAcolhido.VinculoFamiliar.Sim) {
            if (acolhido.getCidadeVinculoFamiliar() == null || acolhido.getCidadeVinculoFamiliar().trim().isEmpty()) {
                result.rejectValue("cidadeVinculoFamiliar", "campo.obrigatorio", "Informe a cidade do vínculo familiar.");
            }
            if (acolhido.getVinculoFamiliarQuem() == null || acolhido.getVinculoFamiliarQuem().trim().isEmpty()) {
                result.rejectValue("vinculoFamiliarQuem", "campo.obrigatorio", "Informe com quem é o vínculo familiar.");
            }
        }

        if (acolhido.getMedicamentoControlado() == null) {
            result.rejectValue("medicamentoControlado", "campo.obrigatorio", "Informe se usa medicamento controlado.");
        } else if (acolhido.getMedicamentoControlado() == CadastroAcolhido.MedicamentoControlado.Sim
                && (acolhido.getQualMedicamento() == null || acolhido.getQualMedicamento().trim().isEmpty())) {
            result.rejectValue("qualMedicamento", "campo.obrigatorio", "Informe qual medicamento.");
        }

        if (acolhido.getDoencaSexualmentetransmissivel() == null) {
            result.rejectValue("doencaSexualmentetransmissivel", "campo.obrigatorio", "Informe se possui DST.");
        }

        if (acolhido.getServicoAcolhimento() == null) {
            result.rejectValue("servicoAcolhimento", "campo.obrigatorio", "Informe se já foi acolhido antes.");
        } else if (acolhido.getServicoAcolhimento() == CadastroAcolhido.ServicoAcolhimento.Sim
                && (acolhido.getVezesAcolhido() == null || acolhido.getVezesAcolhido().trim().isEmpty())) {
            result.rejectValue("vezesAcolhido", "campo.obrigatorio", "Informe quantas vezes já foi acolhido.");
        }

        if (acolhido.getUltimaCidadeQueEsteve() == null || acolhido.getUltimaCidadeQueEsteve().trim().isEmpty()) {
            result.rejectValue("ultimaCidadeQueEsteve", "campo.obrigatorio", "Informe a última cidade que esteve.");
        }

        if (acolhido.getTempoUltimaCidade() == null || acolhido.getTempoUltimaCidade().trim().isEmpty()) {
            result.rejectValue("tempoUltimaCidade", "campo.obrigatorio", "Informe o tempo na última cidade.");
        }

        if (acolhido.getObjetivoAcolhimento() == null || acolhido.getObjetivoAcolhimento().trim().isEmpty()) {
            result.rejectValue("objetivoAcolhimento", "campo.obrigatorio", "Informe o objetivo do acolhimento.");
        }

        if (acolhido.getDataIngresso() == null) {
            acolhido.setDataIngresso(LocalDate.now());
        }

        if (acolhido.getDataSaida() != null) {
            if (acolhido.getDataSaida().isBefore(acolhido.getDataIngresso())) {
                result.rejectValue("dataSaida", "data.invalida", "A data de saída não pode ser anterior à data de ingresso.");
            }

            else if (acolhido.getDataSaida().isAfter(acolhido.getDataIngresso().plusMonths(3))) {
                result.rejectValue("dataSaida", "data.invalida", "A permanência não pode ultrapassar 3 meses da data de ingresso.");
            }
        }

        if (acolhido.getFilho() == CadastroAcolhido.Filho.Nao) {
            acolhido.setQuantidadeFilhos(null);
        }
        if (acolhido.getMedicamentoControlado() == CadastroAcolhido.MedicamentoControlado.Nao) {
            acolhido.setQualMedicamento(null);
        }
        if (acolhido.getUsaDrogas() == CadastroAcolhido.UsaDrogas.Nao) {
            acolhido.setQualDroga(null);
        }
        if (acolhido.getPossuiAlergia() == CadastroAcolhido.PossuiAlergia.Nao) {
            acolhido.setQualAlergia(null);
        }
        if (acolhido.getBeneficioSocial() == CadastroAcolhido.BeneficioSocial.Nao) {
            acolhido.setQualBeneficio(null);
        }
        if (acolhido.getSituacaoRua() == CadastroAcolhido.SituacaoRua.Nao) {
            acolhido.setTempoRua(null);
        }
        if (acolhido.getVinculoFamiliar() == CadastroAcolhido.VinculoFamiliar.Nao) {
            acolhido.setCidadeVinculoFamiliar(null);
            acolhido.setVinculoFamiliarQuem(null);
        }

        if (result.hasErrors()) {
            int stepWithError = determinarStepComErro(result);

            model.addAttribute("stepWithError", stepWithError);
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            carregarListas(model);
            return "cadastroAcolhido/form";
        }

        acolhido.setCpf(cpfLimpo);
        service.salvar(acolhido);
        return "redirect:/cadastroAcolhido/listar";
    }

    @GetMapping("listar")
    public String listar(@RequestParam(required = false) String filtro,
                        @RequestParam(required = false) Integer diasPermanencia,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size,
                        @RequestParam(defaultValue = "nome") String sort,
                        @RequestParam(defaultValue = "asc") String dir,
                        Model model) {
        org.springframework.data.domain.Page<CadastroAcolhido> pageResult;
        
        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);

        if (diasPermanencia != null && diasPermanencia > 0) {
            List<CadastroAcolhido> acolhidos = service.buscarAcolhidosPermanenciaProlongada(diasPermanencia);
            
            if (filtro != null && !filtro.trim().isEmpty()) {
                acolhidos = acolhidos.stream()
                    .filter(a -> a.getNome().toLowerCase().contains(filtro.toLowerCase()))
                    .toList();
            }
            
            // Ordenar manualmente a lista
            acolhidos = ordenarLista(acolhidos, sort, direction);
            
            int start = Math.min(page * size, acolhidos.size());
            int end = Math.min(start + size, acolhidos.size());
            List<CadastroAcolhido> pageContent = acolhidos.subList(start, end);
            pageResult = new org.springframework.data.domain.PageImpl<>(pageContent, 
                org.springframework.data.domain.PageRequest.of(page, size, sortObj), acolhidos.size());
        } else {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
            if (filtro != null && !filtro.trim().isEmpty()) {
                pageResult = service.buscarPorNomePaginado(filtro, pageable);
            } else {
                pageResult = service.listarTodosPaginado(pageable);
            }
        }

        model.addAttribute("acolhidos", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("filtro", filtro);
        model.addAttribute("diasPermanencia", diasPermanencia);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "cadastroAcolhido/lista";
    }
    
    private List<CadastroAcolhido> ordenarLista(List<CadastroAcolhido> lista, String campo, org.springframework.data.domain.Sort.Direction direction) {
        java.util.Comparator<CadastroAcolhido> comparator = null;
        
        switch (campo) {
            case "nome":
                comparator = java.util.Comparator.comparing(CadastroAcolhido::getNome, java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "idade":
                comparator = java.util.Comparator.comparing(CadastroAcolhido::getIdade, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
            case "sexo":
                comparator = java.util.Comparator.comparing(CadastroAcolhido::getSexo, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
            case "dataIngresso":
                comparator = java.util.Comparator.comparing(CadastroAcolhido::getDataIngresso, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
            default:
                comparator = java.util.Comparator.comparing(CadastroAcolhido::getNome, java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }
        
        if (direction == org.springframework.data.domain.Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        
        return lista.stream().sorted(comparator).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        CadastroAcolhido acolhido = service.buscarPorId(id);
        model.addAttribute("acolhido", acolhido);
        carregarListas(model);
        return "cadastroAcolhido/form";
    }

    @GetMapping("remover/{id}")
    public String remover(@PathVariable Long id) {
        service.deletarPorId(id);
        return "redirect:/cadastroAcolhido/listar";
    }

    private void carregarListas(Model model) {
        model.addAttribute("sexos", CadastroAcolhido.Sexo.values());
        model.addAttribute("estadosCivil", CadastroAcolhido.EstadoCivil.values());
        model.addAttribute("escolaridade", CadastroAcolhido.Escolaridade.values());
        model.addAttribute("renda", CadastroAcolhido.Renda.values());
        model.addAttribute("beneficioSocial", CadastroAcolhido.BeneficioSocial.values());
        model.addAttribute("estadoSaude", CadastroAcolhido.EstadoSaude.values());
        model.addAttribute("medicamentoControlado", CadastroAcolhido.MedicamentoControlado.values());
        model.addAttribute("doencaSexualmentetransmissivel", CadastroAcolhido.DoencaSexualmentetransmissivel.values());
        model.addAttribute("possuiAlergia", CadastroAcolhido.PossuiAlergia.values());
        model.addAttribute("fumante", CadastroAcolhido.Fumante.values());
        model.addAttribute("bebidaAlcoolica", CadastroAcolhido.BebidaAlcoolica.values());
        model.addAttribute("usaDrogas", CadastroAcolhido.UsaDrogas.values());
        model.addAttribute("situacaoRua", CadastroAcolhido.SituacaoRua.values());
        model.addAttribute("vinculoFamiliar", CadastroAcolhido.VinculoFamiliar.values());
        model.addAttribute("servicoAcolhimento", CadastroAcolhido.ServicoAcolhimento.values());
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito > 9) primeiroDigito = 0;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito > 9) segundoDigito = 0;

            return (cpf.charAt(9) - '0' == primeiroDigito) && (cpf.charAt(10) - '0' == segundoDigito);
        } catch (Exception e) {
            return false;
        }
    }

    private int determinarStepComErro(BindingResult result) {
        if (result.hasFieldErrors("nome") || result.hasFieldErrors("dataNascimento")
                || result.hasFieldErrors("idade") || result.hasFieldErrors("naturalidade")
                || result.hasFieldErrors("sexo") || result.hasFieldErrors("cor")
                || result.hasFieldErrors("rg") || result.hasFieldErrors("cpf")
                || result.hasFieldErrors("certidaoNascimento") || result.hasFieldErrors("filiacao")) {
            return 1;
        }

        if (result.hasFieldErrors("estadoCivil") || result.hasFieldErrors("nomeConjuge")
                || result.hasFieldErrors("filho") || result.hasFieldErrors("quantidadeFilhos")) {
            return 2;
        }

        if (result.hasFieldErrors("endereco")) {
            return 3;
        }

        if (result.hasFieldErrors("escolaridade") || result.hasFieldErrors("renda")
                || result.hasFieldErrors("beneficioSocial") || result.hasFieldErrors("qualBeneficio")) {
            return 4;
        }

        if (result.hasFieldErrors("estadoSaude") || result.hasFieldErrors("medicamentoControlado")
                || result.hasFieldErrors("qualMedicamento") || result.hasFieldErrors("doencaSexualmentetransmissivel")
                || result.hasFieldErrors("possuiAlergia") || result.hasFieldErrors("qualAlergia")
                || result.hasFieldErrors("fumante") || result.hasFieldErrors("bebidaAlcoolica")
                || result.hasFieldErrors("usaDrogas") || result.hasFieldErrors("qualDroga")) {
            return 5;
        }

        if (result.hasFieldErrors("situacaoRua") || result.hasFieldErrors("tempoRua")
                || result.hasFieldErrors("vinculoFamiliar") || result.hasFieldErrors("vinculoFamiliarQuem")
                || result.hasFieldErrors("cidadeVinculoFamiliar") || result.hasFieldErrors("servicoAcolhimento")
                || result.hasFieldErrors("vezesAcolhido") || result.hasFieldErrors("ultimaCidadeQueEsteve")
                || result.hasFieldErrors("tempoUltimaCidade") || result.hasFieldErrors("objetivoAcolhimento")
                || result.hasFieldErrors("dataIngresso") || result.hasFieldErrors("dataSaida")) {
            return 6;
        }
        return 1;
    }

    @GetMapping("/relatorio/estrategico-pdf")
    public ResponseEntity<byte[]> relatorioEstrategicoPdf() {
        try {
            List<CadastroAcolhido> todosAcolhidos = service.listarTodos();
            
            // RESUMO EXECUTIVO
            long totalAcolhidos = todosAcolhidos.size();
            long acolhidosAtivos = todosAcolhidos.stream()
                    .filter(a -> a.getDataSaida() == null || a.getDataSaida().isAfter(LocalDate.now()))
                    .count();
            long acolhidosInativos = totalAcolhidos - acolhidosAtivos;
            
            double mediaIdade = todosAcolhidos.stream()
                    .filter(a -> a.getIdade() != null)
                    .mapToInt(CadastroAcolhido::getIdade)
                    .average()
                    .orElse(0.0);
            
            // DOCUMENTAÇÃO
            long comRg = todosAcolhidos.stream().filter(a -> a.getRg() != null && !a.getRg().trim().isEmpty()).count();
            long comCpf = todosAcolhidos.stream().filter(a -> a.getCpf() != null && !a.getCpf().trim().isEmpty()).count();
            long comCertidao = todosAcolhidos.stream().filter(a -> a.getCertidaoNascimento() != null && !a.getCertidaoNascimento().trim().isEmpty()).count();
            long semDocumentos = todosAcolhidos.stream()
                    .filter(a -> (a.getRg() == null || a.getRg().trim().isEmpty()) &&
                                (a.getCpf() == null || a.getCpf().trim().isEmpty()) &&
                                (a.getCertidaoNascimento() == null || a.getCertidaoNascimento().trim().isEmpty()))
                    .count();
            
            // GÊNERO
            long masculino = todosAcolhidos.stream().filter(a -> a.getSexo() == CadastroAcolhido.Sexo.Masculino).count();
            long feminino = todosAcolhidos.stream().filter(a -> a.getSexo() == CadastroAcolhido.Sexo.Feminino).count();
            
            // RETORNOS
            long primeiraVez = todosAcolhidos.stream()
                    .filter(a -> a.getVezesAcolhido() == null || a.getVezesAcolhido().equals("1") || a.getVezesAcolhido().toLowerCase().contains("primeira"))
                    .count();
            long retornos = totalAcolhidos - primeiraVez;
            
            // Preparar dados por seção
            List<Map<String, Object>> dados = new ArrayList<>();
            
            // SEÇÃO: DOCUMENTAÇÃO
            adicionarSecao(dados, "DOCUMENTAÇÃO", totalAcolhidos,
                    Map.of(
                        "Possuem RG", comRg,
                        "Possuem CPF", comCpf,
                        "Possuem Certidão de Nascimento", comCertidao,
                        "Sem Documentação", semDocumentos
                    ));
            
            // SEÇÃO: GÊNERO
            adicionarSecao(dados, "DISTRIBUIÇÃO POR GÊNERO", totalAcolhidos,
                    Map.of(
                        "Masculino", masculino,
                        "Feminino", feminino
                    ));
            
            // SEÇÃO: FAIXA ETÁRIA
            Map<String, Long> faixasEtarias = new java.util.LinkedHashMap<>();
            faixasEtarias.put("18 a 25 anos", todosAcolhidos.stream().filter(a -> a.getIdade() != null && a.getIdade() >= 18 && a.getIdade() <= 25).count());
            faixasEtarias.put("26 a 35 anos", todosAcolhidos.stream().filter(a -> a.getIdade() != null && a.getIdade() >= 26 && a.getIdade() <= 35).count());
            faixasEtarias.put("36 a 45 anos", todosAcolhidos.stream().filter(a -> a.getIdade() != null && a.getIdade() >= 36 && a.getIdade() <= 45).count());
            faixasEtarias.put("46 a 60 anos", todosAcolhidos.stream().filter(a -> a.getIdade() != null && a.getIdade() >= 46 && a.getIdade() <= 60).count());
            faixasEtarias.put("Acima de 60 anos", todosAcolhidos.stream().filter(a -> a.getIdade() != null && a.getIdade() > 60).count());
            adicionarSecao(dados, "DISTRIBUIÇÃO POR FAIXA ETÁRIA", totalAcolhidos, faixasEtarias);
            
            // SEÇÃO: ESTADO CIVIL
            Map<String, Long> estadoCivil = todosAcolhidos.stream()
                    .filter(a -> a.getEstadoCivil() != null)
                    .collect(Collectors.groupingBy(a -> a.getEstadoCivil().name(), Collectors.counting()));
            adicionarSecao(dados, "ESTADO CIVIL", totalAcolhidos, estadoCivil);
            
            // SEÇÃO: ESCOLARIDADE
            Map<String, Long> escolaridade = todosAcolhidos.stream()
                    .filter(a -> a.getEscolariade() != null)
                    .collect(Collectors.groupingBy(a -> a.getEscolariade().name(), Collectors.counting()));
            adicionarSecao(dados, "ESCOLARIDADE", totalAcolhidos, escolaridade);
            
            // SEÇÃO: ACOLHIMENTOS ANTERIORES
            adicionarSecao(dados, "ACOLHIMENTOS ANTERIORES", totalAcolhidos,
                    Map.of(
                        "Primeira vez no serviço", primeiraVez,
                        "Retornos (já acolhidos antes)", retornos
                    ));
            
            // SEÇÃO: TOP 10 CIDADES DE ORIGEM
            Map<String, Long> cidades = todosAcolhidos.stream()
                    .filter(a -> a.getNaturalidade() != null && !a.getNaturalidade().trim().isEmpty())
                    .collect(Collectors.groupingBy(CadastroAcolhido::getNaturalidade, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, java.util.LinkedHashMap::new));
            adicionarSecao(dados, "TOP 10 CIDADES DE NATURALIDADE", totalAcolhidos, cidades);
            
            // Carregar template
            InputStream jrxmlStream = getClass().getResourceAsStream("/relatorios/relatorio_perfil_acolhidos.jrxml");
            if (jrxmlStream == null) {
                throw new RuntimeException("Template JRXML não encontrado: /relatorios/relatorio_perfil_acolhidos.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport = 
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(jrxmlStream);
            
            // Parâmetros
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("TOTAL_ACOLHIDOS", totalAcolhidos);
            parametros.put("ACOLHIDOS_ATIVOS", acolhidosAtivos);
            parametros.put("ACOLHIDOS_INATIVOS", acolhidosInativos);
            parametros.put("MEDIA_IDADE", mediaIdade);
            parametros.put("COM_RG", comRg);
            parametros.put("COM_CPF", comCpf);
            parametros.put("COM_CERTIDAO", comCertidao);
            parametros.put("SEM_DOCUMENTOS", semDocumentos);
            parametros.put("MASCULINO", masculino);
            parametros.put("FEMININO", feminino);
            parametros.put("PRIMEIRA_VEZ", primeiraVez);
            parametros.put("RETORNOS", retornos);
            
            // DataSource
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource = 
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dados);
            
            // Preencher
            net.sf.jasperreports.engine.JasperPrint jasperPrint = 
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            
            // Exportar
            byte[] pdf = net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(jasperPrint);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition.inline()
                    .filename("relatorio-perfil-acolhidos.pdf")
                    .build());
            
            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Erro ao gerar relatório estratégico de acolhidos", e);
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Erro: " + e.getMessage()).getBytes());
        }
    }
    
    private void adicionarSecao(List<Map<String, Object>> dados, String categoria, long total, Map<String, Long> itens) {
        for (Map.Entry<String, Long> entry : itens.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoria", categoria);
            item.put("descricao", entry.getKey());
            item.put("quantidade", entry.getValue());
            item.put("percentual", total > 0 ? (entry.getValue() * 100.0 / total) : 0.0);
            dados.add(item);
        }
    }
}
