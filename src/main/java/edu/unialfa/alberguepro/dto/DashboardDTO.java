package edu.unialfa.alberguepro.dto;

import java.util.Map;

public class DashboardDTO {

    private long totalAcolhidos;
    private long vagasLivres;
    private long vagasOcupadas;
    private long totalVagas;
    private long quartosLivres;
    private long quartosOcupados;
    private long totalQuartos;
    private long totalUsuarios;
    private Map<String, Long> patrimonioPorStatus;
    private Map<String, Integer> estoqueBaixo;

    // Getters and Setters

    public long getTotalAcolhidos() {
        return totalAcolhidos;
    }

    public void setTotalAcolhidos(long totalAcolhidos) {
        this.totalAcolhidos = totalAcolhidos;
    }

    public long getVagasLivres() {
        return vagasLivres;
    }

    public void setVagasLivres(long vagasLivres) {
        this.vagasLivres = vagasLivres;
    }

    public long getVagasOcupadas() {
        return vagasOcupadas;
    }

    public void setVagasOcupadas(long vagasOcupadas) {
        this.vagasOcupadas = vagasOcupadas;
    }

    public long getTotalVagas() {
        return totalVagas;
    }

    public void setTotalVagas(long totalVagas) {
        this.totalVagas = totalVagas;
    }

    public long getQuartosLivres() {
        return quartosLivres;
    }

    public void setQuartosLivres(long quartosLivres) {
        this.quartosLivres = quartosLivres;
    }

    public long getQuartosOcupados() {
        return quartosOcupados;
    }

    public void setQuartosOcupados(long quartosOcupados) {
        this.quartosOcupados = quartosOcupados;
    }

    public long getTotalQuartos() {
        return totalQuartos;
    }

    public void setTotalQuartos(long totalQuartos) {
        this.totalQuartos = totalQuartos;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public Map<String, Long> getPatrimonioPorStatus() {
        return patrimonioPorStatus;
    }

    public void setPatrimonioPorStatus(Map<String, Long> patrimonioPorStatus) {
        this.patrimonioPorStatus = patrimonioPorStatus;
    }

    public Map<String, Integer> getEstoqueBaixo() {
        return estoqueBaixo;
    }

    public void setEstoqueBaixo(Map<String, Integer> estoqueBaixo) {
        this.estoqueBaixo = estoqueBaixo;
    }
}
