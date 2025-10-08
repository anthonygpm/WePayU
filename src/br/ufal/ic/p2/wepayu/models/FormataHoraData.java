package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.DataMomentoInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormataHoraData {

    public String formatarHoras(double valor) {
        double rounded = Math.round(valor * 100.0) / 100.0;
        if (Math.abs(rounded - Math.round(rounded)) < 1e-9) {
            return String.valueOf((int) Math.round(rounded));
        } else {
            String s = BigDecimal.valueOf(rounded).stripTrailingZeros().toPlainString();
            return s.replace(".", ",");
        }
    }

    public String normalizarData(String data, String momento) {
        if (data == null || data.trim().isEmpty()) {
            throw new DataMomentoInvalidaException("Data" + momento + "invalida.");
        }
        String[] partes = data.trim().split("/");
        if (partes.length != 3) {
            throw new DataMomentoInvalidaException("Data" + momento + "invalida.");
        }
        try {
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);
            if (dia < 1 || dia > 31 || mes < 1 || mes > 12) {
                throw new DataMomentoInvalidaException("Data" + momento + "invalida.");
            }
            if (dia > 29 && mes == 2) {
                throw new DataMomentoInvalidaException("Data" + momento + "invalida.");
            }
            return String.format("%02d/%02d/%04d", dia, mes, ano);
        } catch (NumberFormatException e) {
            throw new DataMomentoInvalidaException("Data" + momento + "invalida.");
        }
    }

    public LocalDate parseData(String dataStr, String momento) {
        String dataNormalizada = normalizarData(dataStr, momento);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dataNormalizada, formatter);
    }
}
