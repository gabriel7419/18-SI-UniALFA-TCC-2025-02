package edu.unialfa.alberguepro.dto;

public class EvolucaoOcupacaoDTO {
    private String periodo;
    private int entradas;
    private int saidas;
    private int leitosOcupados;
    private double taxaOcupacao;

    public EvolucaoOcupacaoDTO() {
    }

    public EvolucaoOcupacaoDTO(String periodo, int entradas, int saidas, int leitosOcupados, double taxaOcupacao) {
        this.periodo = periodo;
        this.entradas = entradas;
        this.saidas = saidas;
        this.leitosOcupados = leitosOcupados;
        this.taxaOcupacao = taxaOcupacao;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public int getEntradas() {
        return entradas;
    }

    public void setEntradas(int entradas) {
        this.entradas = entradas;
    }

    public int getSaidas() {
        return saidas;
    }

    public void setSaidas(int saidas) {
        this.saidas = saidas;
    }

    public int getLeitosOcupados() {
        return leitosOcupados;
    }

    public void setLeitosOcupados(int leitosOcupados) {
        this.leitosOcupados = leitosOcupados;
    }

    public double getTaxaOcupacao() {
        return taxaOcupacao;
    }

    public void setTaxaOcupacao(double taxaOcupacao) {
        this.taxaOcupacao = taxaOcupacao;
    }
}
