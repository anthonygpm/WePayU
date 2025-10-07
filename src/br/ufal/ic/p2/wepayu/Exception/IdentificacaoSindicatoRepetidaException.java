package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoSindicatoRepetidaException extends RuntimeException {
    public IdentificacaoSindicatoRepetidaException() {
        super("Ha outro empregado com esta identificacao de sindicato");
    }
}
