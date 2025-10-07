package br.ufal.ic.p2.wepayu.Exception;

public class HorasNaoNumericasException extends RuntimeException {
    public HorasNaoNumericasException() {
        super("Horas devem ser numericas.");
    }
}
