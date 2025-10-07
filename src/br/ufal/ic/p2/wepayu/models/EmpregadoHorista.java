package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado implements Cloneable{
    private List<RegistroHoras> registros = new ArrayList<>();

    public EmpregadoHorista(String nome, String endereco, double salario) {
        super(nome, endereco, "horista", salario);
        this.dataContrato = null;
    }

    public void lancaCartao(LocalDate data, double horas) {
        if (dataContrato == null) {
            dataContrato = data; // Define data de contrato no primeiro registro
        }
        registros.add(new RegistroHoras(data, horas));
    }

    public double getHorasNormais(LocalDate inicio, LocalDate fimExclusive) {
        double soma = 0.0;
        for (RegistroHoras r : registros) {
            LocalDate d = r.getData();
            if (!d.isBefore(inicio) && d.isBefore(fimExclusive)) {
                soma += Math.min(8.0, r.getHoras());
            }
        }
        return soma;
    }

    public double getHorasExtras(LocalDate inicio, LocalDate fimExclusive) {
        double soma = 0.0;
        for (RegistroHoras r : registros) {
            LocalDate d = r.getData();
            if (!d.isBefore(inicio) && d.isBefore(fimExclusive)) {
                double h = r.getHoras();
                if (h > 8.0) soma += (h - 8.0);
            }
        }
        return soma;
    }

    public double calculaTaxa (LocalDate inicio, LocalDate fim) {
        if (!sindicalizado) return 0.0;

        return taxaSindical * 7 + getTaxasServico(inicio, fim);
    }

    @Override
    public double calculaPagamento(LocalDate dataPagamento) {
        if (dataContrato == null) {
            dataContrato = dataPagamento;
        }

        if (ultimoPagamento == null) {
            ultimoPagamento = dataContrato.minusDays(1);
        }

        LocalDate inicio = dataPagamento.minusDays(6);

        // Horistas recebem semanalmente na sexta-feira
        if (dataPagamento.getDayOfWeek() != java.time.DayOfWeek.FRIDAY) {
            return 0.0;
        }

        double horasNormais = getHorasNormais(inicio, dataPagamento.plusDays(1));
        double horasExtras = getHorasExtras(inicio, dataPagamento.plusDays(1));
        double pagamento = horasNormais * salario + horasExtras * (salario * 1.5);

        if (pagamento < 0) pagamento = 0;
        ultimoPagamento = dataPagamento;
        return pagamento;
    }

    @Override
    public EmpregadoHorista clone() {
        EmpregadoHorista copia = (EmpregadoHorista) super.clone();
        copia.registros = new ArrayList<>();
        for (RegistroHoras registro : this.registros) {
            copia.registros.add(registro.clone());
        }

        return copia;
    }
}
