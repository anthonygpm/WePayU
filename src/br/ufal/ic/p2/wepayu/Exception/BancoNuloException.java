package br.ufal.ic.p2.wepayu.Exception;

public class BancoNuloException extends RuntimeException {
    public BancoNuloException() { super("Banco nao pode ser nulo."); }
}
