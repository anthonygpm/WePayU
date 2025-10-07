package br.ufal.ic.p2.wepayu.Exception;

public class DataInicialPosteriorException extends RuntimeException {
    public DataInicialPosteriorException() {
        super("Data inicial nao pode ser posterior aa data final.");
    }
}
