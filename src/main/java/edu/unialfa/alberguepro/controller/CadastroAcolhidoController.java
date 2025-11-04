package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Period;

import java.util.List;

@Controller
@RequestMapping("/cadastroAcolhido")
public class CadastroAcolhidoController {

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
    public String listar(@RequestParam(required = false) String filtro, Model model) {
        List<CadastroAcolhido> acolhidos;

        if (filtro != null && !filtro.trim().isEmpty()) {
            acolhidos = service.buscarPorNome(filtro);
        } else {
            acolhidos = service.listarTodos();
        }

        model.addAttribute("acolhidos", acolhidos);
        model.addAttribute("filtro", filtro);
        return "cadastroAcolhido/lista";
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
}
