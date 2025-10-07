package br.ufal.ic.p2.wepayu.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends Empregado implements Cloneable {
    private List<Venda> vendas;

    public EmpregadoComissionado(String nome, String endereco, String tipo, double salario, double comissao) {
        super(nome, endereco, tipo, salario);
        this.comissao = comissao;
        this.dataContrato = LocalDate.of(2005, 1, 1);
        this.vendas = new ArrayList<>();
    }

    public void lancaVenda(LocalDate data, double valor) {
        vendas.add(new Venda(data, valor));
    }

    public double getVendas(LocalDate inicio, LocalDate fimExclusive) {
        return vendas.stream()
                .filter(v -> !v.getData().isBefore(inicio) && v.getData().isBefore(fimExclusive))
                .mapToDouble(Venda::getValor)
                .sum();
    }

    public double calculaTaxa(LocalDate inicio, LocalDate fim) {
        if (!sindicalizado) return 0.0;

        return 14 * taxaSindical + getTaxasServico(inicio, fim);
    }

    public double getVendasPeriodo(LocalDate data) {
        LocalDate inicio;
        LocalDate fim;

        int dia = data.getDayOfMonth();
        if (dia <= 14) {
            inicio = data.withDayOfMonth(1);
            fim = data.withDayOfMonth(14).plusDays(1);
        } else {
            inicio = data.withDayOfMonth(15);
            fim = data.withDayOfMonth(data.lengthOfMonth()).plusDays(1);
        }

        return getVendas(inicio, fim);
    }

    public LocalDate getPrimeiroPagamento() {
        if (dataContrato == null) return null;
        LocalDate segundaSemanaInicio = dataContrato.plusDays(7);
        return segundaSemanaInicio.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    }

    // Retorna true se 'data' é um dia de pagamento do comissionado
    public boolean ehDiaDePagamento(LocalDate data) {
        LocalDate primeiro = getPrimeiroPagamento();
        if (primeiro == null) return false;
        if (data.isBefore(primeiro)) return false;
        long dias = ChronoUnit.DAYS.between(primeiro, data);
        // pagamento a cada 14 dias a partir do primeiro pagamento
        return dias % 14 == 0 && data.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    // Retorna o inicio do periodo de vendas para o pagamento de 'data'
    public LocalDate inicioPeriodoParaPagamento(LocalDate dataPagamento) {
        if (ultimoPagamento == null) {
            return dataContrato;
        } else {
            return ultimoPagamento.plusDays(1);
        }
    }

    @Override
    public double calculaPagamento(LocalDate dataPagamento) {
        if (ultimoPagamento == null) {
            ultimoPagamento = dataContrato.minusDays(1);
        }

        LocalDate inicio = ultimoPagamento.plusDays(1);

        if (!ehDiaDePagamento(dataPagamento)) {
            return 0.0;
        }

        double salarioBase = salario * 12 / 52;
        double vendasPeriodo = getVendas(inicio, dataPagamento.plusDays(1));
        double comissaoTotal = vendasPeriodo * comissao;

        double pagamento = salarioBase + comissaoTotal;

        if (sindicalizado) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, dataPagamento.plusDays(1));
            pagamento -= dias * taxaSindical;
            pagamento -= getTaxasServico(inicio, dataPagamento.plusDays(1));
        }

        if (pagamento < 0) pagamento = 0;
        ultimoPagamento = dataPagamento;
        return pagamento;
    }

    @Override
    public EmpregadoComissionado clone() {
        EmpregadoComissionado copia = (EmpregadoComissionado) super.clone();
        copia.vendas = new ArrayList<>();
        for (Venda venda : this.vendas) {
            copia.vendas.add(venda.clone());
        }
        return copia;
    }

}
