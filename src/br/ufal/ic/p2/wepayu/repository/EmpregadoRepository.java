package br.ufal.ic.p2.wepayu.repository;

import br.ufal.ic.p2.wepayu.models.Empregado;

import java.util.LinkedHashMap;
import java.util.Map;

public class EmpregadoRepository {
    private Map<String, Empregado> empregados = new LinkedHashMap<>();
    private Map<String, Empregado> sindicatos = new LinkedHashMap<>();
    private int contador = 1;
    private int proximoId = 0;

    public Map<String, Empregado> getEmpregados() {
        return empregados;
    }

    public Map<String, Empregado> getSindicatos() {
        return sindicatos;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }

    public int getProximoId() {
        return proximoId;
    }

    public void setProximoId(int proximoId) {
        this.proximoId = proximoId;
    }

    public Empregado getEmpregado(String id) {
        return empregados.get(id);
    }

    public void addEmpregado(String id, Empregado empregado) {
        empregados.put(id, empregado);
    }

    public void removeEmpregado(String id) {
        empregados.remove(id);
    }

    public boolean containsEmpregado(String id) {
        return empregados.containsKey(id);
    }

    public Empregado getSindicato(String idSindicato) {
        return sindicatos.get(idSindicato);
    }

    public void addSindicato(String idSindicato, Empregado empregado) {
        sindicatos.put(idSindicato, empregado);
    }

    public void removeSindicato(String idSindicato) {
        sindicatos.remove(idSindicato);
    }

    public EmpregadoRepository(Map<String, Empregado> empregados, Map<String, Empregado> sindicatos, int contador, int proximoId) {
        this.empregados = empregados;
        this.sindicatos = sindicatos;
        this.contador = contador;
        this.proximoId = proximoId;
    }

    public EmpregadoRepository() {
    }
}
