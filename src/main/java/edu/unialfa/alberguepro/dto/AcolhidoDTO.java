package edu.unialfa.alberguepro.dto;

import edu.unialfa.alberguepro.model.CadastroAcolhido;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class AcolhidoDTO {
    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private Integer idade;
    private String sexo;
    private String naturalidade;
    private String rg;
    private String cpf;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataIngresso;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataSaida;

    public AcolhidoDTO(CadastroAcolhido acolhido) {
        this.id = acolhido.getId();
        this.nome = acolhido.getNome();
        this.dataNascimento = acolhido.getDataNascimento();
        this.idade = acolhido.getIdade();
        this.sexo = acolhido.getSexo() != null ? acolhido.getSexo().toString() : null;
        this.naturalidade = acolhido.getNaturalidade();
        this.rg = acolhido.getRg();
        this.cpf = acolhido.getCpf();
        this.dataIngresso = acolhido.getDataIngresso();
        this.dataSaida = acolhido.getDataSaida();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public Integer getIdade() { return idade; }
    public String getSexo() { return sexo; }
    public String getNaturalidade() { return naturalidade; }
    public String getRg() { return rg; }
    public String getCpf() { return cpf; }
    public LocalDate getDataIngresso() { return dataIngresso; }
    public LocalDate getDataSaida() { return dataSaida; }

}
