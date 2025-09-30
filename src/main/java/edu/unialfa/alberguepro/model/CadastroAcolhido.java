package edu.unialfa.alberguepro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CadastroAcolhido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    private String naturalidade;
    private Integer idade;
    private String profissao;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    private String cor;
    private String rg;
    private String cpf;
    private String certidaoNascimento;
    private String filiacao;

    @Enumerated(EnumType.STRING)
    private EstadoCivil estadoCivil;

    private String nomeConjuge;

    @Enumerated(EnumType.STRING)
    private Filho filho;
    private Integer quantidadeFilhos;
    private String endereco;
    private String telefoneFamiliar;

    @Enumerated(EnumType.STRING)
    private Escolaridade escolaridade;

    @Enumerated(EnumType.STRING)
    private Renda renda;

    @Enumerated(EnumType.STRING)
    private BeneficioSocial beneficioSocial;
    private String qualBeneficio;

    @Enumerated(EnumType.STRING)
    private EstadoSaude estadoSaude;

    @Enumerated(EnumType.STRING)
    private MedicamentoControlado medicamentoControlado;
    private String qualMedicamento;

    @Enumerated(EnumType.STRING)
    private DoencaSexualmentetransmissivel doencaSexualmentetransmissivel;

    @Enumerated(EnumType.STRING)
    private PossuiAlergia possuiAlergia;
    private String qualAlergia;

    @Enumerated(EnumType.STRING)
    private Fumante fumante;

    @Enumerated(EnumType.STRING)
    private BebidaAlcoolica bebidaAlcoolica;

    @Enumerated(EnumType.STRING)
    private UsaDrogas usaDrogas;
    private String qualDroga;

    @Enumerated(EnumType.STRING)
    private SituacaoRua situacaoRua;
    private String tempoRua;

    @Enumerated(EnumType.STRING)
    private VinculoFamiliar vinculoFamiliar;

    private String vinculoFamiliarQuem;
    private String cidadeVinculoFamiliar;

    @Enumerated(EnumType.STRING)
    private ServicoAcolhimento servicoAcolhimento;

    private String vezesAcolhido;
    private String ultimaCidadeQueEsteve;
    private String tempoUltimaCidade;
    private String objetivoAcolhimento;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataIngresso;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataSaida;


    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public EstadoCivil getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(EstadoCivil estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public Filho getFilho() {
        return filho;
    }

    public void setFilho(Filho filho) {
        this.filho = filho;
    }

    public Escolaridade getEscolariade() {
        return escolaridade;
    }

    public void setEscolariade(Escolaridade escolaridade) {
        this.escolaridade = escolaridade;
    }

    public Renda getRenda() {
        return renda;
    }

    public void setRenda(Renda renda) {
        this.renda = renda;
    }

    public BeneficioSocial getBeneficioSocial() {
        return beneficioSocial;
    }

    public void setBeneficioSocial(BeneficioSocial beneficioSocial) {
        this.beneficioSocial = beneficioSocial;
    }

    public EstadoSaude getEstadoSaude() {
        return estadoSaude;
    }

    public void setEstadoSaude(EstadoSaude estadoSaude) {
        this.estadoSaude = estadoSaude;
    }

    public MedicamentoControlado getMedicamentoControlado() {
        return medicamentoControlado;
    }

    public void setMedicamentoControlado(MedicamentoControlado medicamentoControlado) {
        this.medicamentoControlado = medicamentoControlado;
    }

    public DoencaSexualmentetransmissivel getDoencaSexualmentetransmissivel() {
        return doencaSexualmentetransmissivel;
    }

    public void setDoencaSexualmentetransmissivel(DoencaSexualmentetransmissivel doencaSexualmentetramissivel) {
        this.doencaSexualmentetransmissivel = doencaSexualmentetramissivel;
    }

    public PossuiAlergia getPossuiAlergia() {
        return possuiAlergia;
    }

    public void setPossuiAlergia(PossuiAlergia possuiAlergia) {
        this.possuiAlergia = possuiAlergia;
    }

    public Fumante getFumante() {
        return fumante;
    }

    public void setFumante(Fumante fumante) {
        this.fumante = fumante;
    }

    public BebidaAlcoolica getBebidaAlcoolica() {
        return bebidaAlcoolica;
    }

    public void setBebidaAlcoolica(BebidaAlcoolica bebidaAlcoolica) {
        this.bebidaAlcoolica = bebidaAlcoolica;
    }

    public UsaDrogas getUsaDrogas() {
        return usaDrogas;
    }

    public void setUsaDrogas(UsaDrogas usaDrogas) {
        this.usaDrogas = usaDrogas;
    }

    public SituacaoRua getSituacaoRua() {
        return situacaoRua;
    }

    public void setSituacaoRua(SituacaoRua situacaoRua) {
        this.situacaoRua = situacaoRua;
    }

    public VinculoFamiliar getVinculoFamiliar() {
        return vinculoFamiliar;
    }

    public void setVinculoFamiliar(VinculoFamiliar vinculoFamiliar) {
        this.vinculoFamiliar = vinculoFamiliar;
    }

    public ServicoAcolhimento getServicoAcolhimento() {
        return servicoAcolhimento;
    }

    public void setServicoAcolhimento(ServicoAcolhimento servicoAcolhimento) {
        this.servicoAcolhimento = servicoAcolhimento;
    }


    public enum Filho {
        Sim,
        Nao
    }

    public enum EstadoCivil {
        Solteiro,
        Casado,
        Divorciado,
        UniaoEstavel,
        Viuvo
    }

    public enum Sexo {
        Masculino,
        Feminino
    }

    public enum Escolaridade {
        Analfabeto,
        EnsinoFundamentalIncompleto,
        EnsinoFundamentalCompleto,
        EnsinoMedioIncompleto,
        EnsinoMedioCompleto,
        SuperiorIncompleto,
        SuperiorCompleto
    }

    public enum Renda {
        Sim,
        Nao
    }

    public enum BeneficioSocial {
        Sim,
        Nao
    }

    public enum EstadoSaude {
        Bom,
        Regular
    }

    public enum MedicamentoControlado {
        Sim,
        Nao
    }

    public enum DoencaSexualmentetransmissivel {
        Sim,
        Nao
    }

    public enum PossuiAlergia {
        Sim,
        Nao
    }

    public enum Fumante {
        Sim,
        Nao
    }

    public enum BebidaAlcoolica {
        Sim,
        Nao
    }

    public enum UsaDrogas {
        Sim,
        Nao
    }

    public enum SituacaoRua {
        Sim,
        Nao
    }

    public enum VinculoFamiliar {
        Sim,
        Nao
    }

    public enum ServicoAcolhimento {
        Sim,
        Nao
    }
}
