package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoExisteException extends RuntimeException{
    public EmpregadoNaoExisteException(){
        super("Empregado nao existe.");
    }
}
