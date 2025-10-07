package br.ufal.ic.p2.wepayu.Exception;

public class AtributoDesconhecidoException extends RuntimeException {
    public AtributoDesconhecidoException() {
        super("Atributo desconhecido.");
    }
}
