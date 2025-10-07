package br.ufal.ic.p2.wepayu.Exception;

public class AtributoNaoExisteException extends RuntimeException {
    public AtributoNaoExisteException() {
        super("Atributo nao existe.");
    }
}
