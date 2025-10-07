package br.ufal.ic.p2.wepayu.Exception;

public class MetodoPagamentoDesconhecidoException extends RuntimeException {
    public MetodoPagamentoDesconhecidoException() {
        super("Metodo de pagamento desconhecido.");
    }
}
