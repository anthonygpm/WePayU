package br.ufal.ic.p2.wepayu.Exception;

public class EnderecoNuloException extends RuntimeException {
    public EnderecoNuloException() {
        super("Endereco nao pode ser nulo.");
    }
}
