package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNaoNumericoException extends RuntimeException {
    public SalarioNaoNumericoException() {
        super("Salario deve ser numerico.");
    }
}
