package br.ufal.ic.p2.wepayu.Exception;

public class HorasEmpregadoNulaException extends RuntimeException {
    public HorasEmpregadoNulaException() {
        super("Horas do empregado nao pode ser nula.");
    }
}
