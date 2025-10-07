package br.ufal.ic.p2.wepayu.Exception;

public class TaxaSindicalNulaException extends RuntimeException {
    public TaxaSindicalNulaException() {
        super("Taxa sindical nao pode ser nula.");
    }
}
