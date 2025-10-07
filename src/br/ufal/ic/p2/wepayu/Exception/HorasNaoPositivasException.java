package br.ufal.ic.p2.wepayu.Exception;

public class HorasNaoPositivasException extends RuntimeException {
    public HorasNaoPositivasException() {
        super("Horas devem ser positivas.");
    }
}
