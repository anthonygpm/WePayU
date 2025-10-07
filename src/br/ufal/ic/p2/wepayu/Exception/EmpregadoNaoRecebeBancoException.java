package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoRecebeBancoException extends RuntimeException {
    public EmpregadoNaoRecebeBancoException() {
        super("Empregado nao recebe em banco.");
    }
}
