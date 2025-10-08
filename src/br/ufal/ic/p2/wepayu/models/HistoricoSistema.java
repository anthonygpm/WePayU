package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.HistoricoVazioException;

import java.util.Map;
import java.util.Stack;

public class HistoricoSistema {
    private Stack<EstadoSistema> historico = new Stack<>();

    public void salvarEstado(Map<String, Empregado> empregados, Map<String, Empregado> sindicatos, int contador, int proximoId) {
        historico.push(new EstadoSistema(empregados, sindicatos, contador, proximoId));
    }

    public EstadoSistema desfazer() {
        if (historico.isEmpty()) {
            throw new HistoricoVazioException();
        }
        return historico.pop();
    }

    public boolean estaVazio() {
        return historico.isEmpty();
    }
}
