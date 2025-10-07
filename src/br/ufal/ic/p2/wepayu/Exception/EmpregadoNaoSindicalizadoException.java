package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoSindicalizadoException extends RuntimeException {
    public EmpregadoNaoSindicalizadoException() {
        super("Empregado nao eh sindicalizado.");
    }
}
