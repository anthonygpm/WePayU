package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoEncontradoException extends RuntimeException {
    public EmpregadoNaoEncontradoException() {
        super("Nao ha empregado com esse nome.");
    }
}
