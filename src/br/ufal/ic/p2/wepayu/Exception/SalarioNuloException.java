package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNuloException extends RuntimeException {
    public SalarioNuloException() {
        super("Salario nao pode ser nulo.");
    }
}
