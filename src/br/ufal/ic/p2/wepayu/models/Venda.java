package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class Venda implements Cloneable {
    private LocalDate data;
    private double valor;

    public Venda(LocalDate data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public double getValor() {
        return valor;
    }

    public Venda clone() {
        try {
            return (Venda) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
