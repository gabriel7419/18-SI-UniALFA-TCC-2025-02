package edu.unialfa.alberguepro.dto;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import java.util.Date;

public class AcolhidoDTO {
    private Long id;
    private String nome;
    private Date dataNascimento;
    private Integer idade;
    private String sexo;
    private String naturalidade;
    private String rg;
    private String cpf;
    private Date dataIngresso;
    private Date dataSaida;

    public AcolhidoDTO(CadastroAcolhido acolhido) {
        this.id = acolhido.getId();
        this.nome = acolhido.getNome();
        this.dataNascimento = acolhido.getDataNascimento() != null
                ? java.sql.Date.valueOf(acolhido.getDataNascimento())
                : null;
        this.idade = acolhido.getIdade();
        this.sexo = acolhido.getSexo() != null ? acolhido.getSexo().toString() : null;
        this.naturalidade = acolhido.getNaturalidade();
        this.rg = acolhido.getRg();
        this.cpf = acolhido.getCpf();
        this.dataIngresso = acolhido.getDataIngresso() != null
                ? java.sql.Date.valueOf(acolhido.getDataIngresso())
                : null;
        this.dataSaida = acolhido.getDataSaida() != null
                ? java.sql.Date.valueOf(acolhido.getDataSaida())
                : null;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Date getDataNascimento() { return dataNascimento; }
    public Integer getIdade() { return idade; }
    public String getSexo() { return sexo; }
    public String getNaturalidade() { return naturalidade; }
    public String getRg() { return rg; }
    public String getCpf() { return cpf; }
    public Date getDataIngresso() { return dataIngresso; }
    public Date getDataSaida() { return dataSaida; }

}
