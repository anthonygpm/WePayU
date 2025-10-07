package br.ufal.ic.p2.wepayu.Exception;

public class MetodoPagamentoInvalidoException extends RuntimeException {
    public MetodoPagamentoInvalidoException() {
        super("Metodo de pagamento invalido.");
    }
}
