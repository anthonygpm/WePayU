package br.ufal.ic.p2.wepayu.Exception;

public class ContaCorrenteNulaException extends RuntimeException {
    public ContaCorrenteNulaException() {
        super("Conta corrente nao pode ser nulo.");
    }
}
