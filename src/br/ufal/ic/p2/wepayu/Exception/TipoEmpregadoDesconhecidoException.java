package br.ufal.ic.p2.wepayu.Exception;

public class TipoEmpregadoDesconhecidoException extends RuntimeException {
    public TipoEmpregadoDesconhecidoException() {
        super("Tipo de empregado desconhecido.");
    }
}
