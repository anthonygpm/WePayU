package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class TaxaServico implements Cloneable {
    private LocalDate data;
    private double valor;

    public TaxaServico(LocalDate data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public TaxaServico clone() {
        try {
            return (TaxaServico) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
