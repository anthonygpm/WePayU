package br.ufal.ic.p2.wepayu.models;

import java.util.HashMap;
import java.util.Map;

public class EstadoSistema {
    public Map<String, Empregado> empregadosBackup;
    public Map<String, Empregado> sindicatosBackup;
    public int contadorBackup;
    public int proximoIdBackup;

    public EstadoSistema(Map<String, Empregado> empregados, Map<String, Empregado> sindicatos, int contador, int proximoId) {
        this.empregadosBackup = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            this.empregadosBackup.put(entry.getKey(), entry.getValue().clone());
        }

        this.sindicatosBackup = new HashMap<>();
        for (Map.Entry<String, Empregado> entry : sindicatos.entrySet()) {
            this.sindicatosBackup.put(entry.getKey(), entry.getValue().clone());
        }

        this.contadorBackup = contador;
        this.proximoIdBackup = proximoId;
    }
}
