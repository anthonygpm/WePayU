package br.ufal.ic.p2.wepayu.Exception;

public class NomeNuloException extends RuntimeException {
    public NomeNuloException() {
        super("Nome nao pode ser nulo.");
    }
}
