package edu.unialfa.alberguepro.dto;

import java.util.Map;

public class DashboardDTO {

    private long totalAcolhidos;
    private long leitosLivres;
    private long leitosOcupados;
    private long totalLeitos;
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

    public long getLeitosLivres() {
        return leitosLivres;
    }

    public void setLeitosLivres(long leitosLivres) {
        this.leitosLivres = leitosLivres;
    }

    public long getLeitosOcupados() {
        return leitosOcupados;
    }

    public void setLeitosOcupados(long leitosOcupados) {
        this.leitosOcupados = leitosOcupados;
    }

    public long getTotalLeitos() {
        return totalLeitos;
    }

    public void setTotalLeitos(long totalLeitos) {
        this.totalLeitos = totalLeitos;
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
