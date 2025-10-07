package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoHoristaException extends RuntimeException {
    public EmpregadoNaoHoristaException() {
        super("Empregado nao eh horista.");
    }
}
