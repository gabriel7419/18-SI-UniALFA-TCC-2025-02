package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import edu.unialfa.alberguepro.service.CadastroAcolhidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return "cadastroAcolhido/form";
    }

    @PostMapping("salvar")
    public String salvar(@ModelAttribute("acolhido") CadastroAcolhido acolhido, BindingResult result, Model model) {

        // Validações
        if (acolhido.getNome() == null || acolhido.getNome().trim().isEmpty()) {
            result.rejectValue("nome", "campo.obrigatorio", "O nome é obrigatório.");
        }

        if (acolhido.getDataNascimento() == null) {
            result.rejectValue("dataNascimento", "campo.obrigatorio", "A data de nascimento é obrigatória.");
        } else if (acolhido.getDataNascimento().isAfter(LocalDate.now())) {
            result.rejectValue("dataNascimento", "data.invalida", "A data de nascimento não pode ser futura.");
        }

        if (acolhido.getSexo() == null) {
            result.rejectValue("sexo", "campo.obrigatorio", "O sexo é obrigatório.");
        }

        if (acolhido.getEstadoCivil() == null) {
            result.rejectValue("estadoCivil", "campo.obrigatorio", "O estado civil é obrigatório.");
        }

        if (acolhido.getCpf() == null || acolhido.getCpf().trim().isEmpty()) {
            result.rejectValue("cpf", "campo.obrigatorio", "O CPF é obrigatório.");
        }

        if (acolhido.getRg() == null || acolhido.getRg().trim().isEmpty()) {
            result.rejectValue("rg", "campo.obrigatorio", "O RG é obrigatório.");
        }

        if (acolhido.getEndereco() == null || acolhido.getEndereco().trim().isEmpty()) {
            result.rejectValue("endereco", "campo.obrigatorio", "O endereço é obrigatório.");
        }

        if (acolhido.getTelefoneFamiliar() == null || acolhido.getTelefoneFamiliar().trim().isEmpty()) {
            result.rejectValue("telefoneFamiliar", "campo.obrigatorio", "O telefone do familiar é obrigatório.");
        }

        if (acolhido.getProfissao() == null || acolhido.getProfissao().trim().isEmpty()) {
            result.rejectValue("profissao", "campo.obrigatorio", "A profissão é obrigatória.");
        }

        if (acolhido.getFilho() == CadastroAcolhido.Filho.Sim && (acolhido.getQuantidadeFilhos() == null || acolhido.getQuantidadeFilhos() <= 0)) {
            result.rejectValue("quantidadeFilhos", "campo.obrigatorio", "Informe a quantidade de filhos.");
        }

        if (acolhido.getBeneficioSocial() == CadastroAcolhido.BeneficioSocial.Sim && (acolhido.getQualBeneficio() == null || acolhido.getQualBeneficio().trim().isEmpty())) {
            result.rejectValue("qualBeneficio", "campo.obrigatorio", "Informe qual benefício recebe.");
        }

        if (acolhido.getMedicamentoControlado() == CadastroAcolhido.MedicamentoControlado.Sim && (acolhido.getQualMedicamento() == null || acolhido.getQualMedicamento().trim().isEmpty())) {
            result.rejectValue("qualMedicamento", "campo.obrigatorio", "Informe qual medicamento controlado utiliza.");
        }

        if (acolhido.getPossuiAlergia() == CadastroAcolhido.PossuiAlergia.Sim && (acolhido.getQualAlergia() == null || acolhido.getQualAlergia().trim().isEmpty())) {
            result.rejectValue("qualAlergia", "campo.obrigatorio", "Informe a alergia.");
        }

        if (acolhido.getUsaDrogas() == CadastroAcolhido.UsaDrogas.Sim && (acolhido.getQualDroga() == null || acolhido.getQualDroga().trim().isEmpty())) {
            result.rejectValue("qualDroga", "campo.obrigatorio", "Informe qual droga utiliza.");
        }

        if (acolhido.getDataIngresso() == null) {
            acolhido.setDataIngresso(LocalDateTime.now());
        }

        if (acolhido.getDataSaida() != null && acolhido.getDataSaida().isBefore(acolhido.getDataIngresso())) {
            result.rejectValue("dataSaida", "data.invalida", "A data de saída não pode ser anterior à data de ingresso.");
        }

        // Se houver erro, recarregar listas e voltar para o formulário
        if (result.hasErrors()) {
            carregarListas(model);
            return "cadastroAcolhido/form";
        }

        // Salvar
        service.salvar(acolhido);
        return "redirect:/cadastroAcolhido/listar";
    }

    @GetMapping("listar")
    public String listar(Model model) {
        List<CadastroAcolhido> acolhidos = service.listarTodos();
        model.addAttribute("acolhidos", acolhidos);
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
        model.addAttribute("filhos", CadastroAcolhido.Filho.values());
        model.addAttribute("beneficios", CadastroAcolhido.BeneficioSocial.values());
        model.addAttribute("medicamentos", CadastroAcolhido.MedicamentoControlado.values());
        model.addAttribute("alergias", CadastroAcolhido.PossuiAlergia.values());
        model.addAttribute("drogas", CadastroAcolhido.UsaDrogas.values());
    }
}
