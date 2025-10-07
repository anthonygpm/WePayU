package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class EmpregadoAssalariado extends Empregado implements Cloneable {
    public EmpregadoAssalariado(String nome, String endereco, double salario) {
        super(nome, endereco, "assalariado", salario);
        this.dataContrato = LocalDate.of(2005, 1, 1);
    }

    public double calculaTaxa(int ultimoDiaDoMes, LocalDate inicio, LocalDate fim) {
        if (!sindicalizado) return 0.0;

        return ultimoDiaDoMes * taxaSindical + getTaxasServico(inicio, fim);
    }

    @Override
    public double calculaPagamento(LocalDate dataPagamento) {
        if (ultimoPagamento == null) {
            ultimoPagamento = dataContrato.minusDays(1);
        }

        LocalDate inicio = ultimoPagamento.plusDays(1);

        // Pagamento mensal: último dia do mês
        if (dataPagamento.getDayOfMonth() != dataPagamento.lengthOfMonth()) {
            return 0.0;
        }

        double pagamento = salario;

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
    public EmpregadoAssalariado clone() {
        return (EmpregadoAssalariado) super.clone();
    }

}