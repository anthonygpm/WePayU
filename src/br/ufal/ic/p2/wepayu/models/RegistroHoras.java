package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class RegistroHoras implements Cloneable{
    private LocalDate data;
    private double horas;

    public RegistroHoras(LocalDate data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    @Override
    public RegistroHoras clone() {
        try {
            return (RegistroHoras) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public LocalDate getData() { return data; }
    public double getHoras() { return horas; }
}