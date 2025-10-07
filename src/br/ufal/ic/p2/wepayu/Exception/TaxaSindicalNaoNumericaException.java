package br.ufal.ic.p2.wepayu.Exception;

public class TaxaSindicalNaoNumericaException extends RuntimeException {
    public TaxaSindicalNaoNumericaException() {
        super("Taxa sindical deve ser numerica.");
    }
}
