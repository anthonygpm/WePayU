package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoEmpregadoNulaException extends RuntimeException {
    public IdentificacaoEmpregadoNulaException() {
        super("Identificacao do empregado nao pode ser nula.");
    }
}
