package br.ufal.ic.p2.wepayu.Exception;

public class ValorNaoPositivoException extends RuntimeException {
    public ValorNaoPositivoException() {
        super("Valor deve ser positivo.");
    }
}
