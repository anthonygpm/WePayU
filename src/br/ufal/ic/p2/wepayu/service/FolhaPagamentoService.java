package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.ErroGeracaoArquivoException;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.repository.EmpregadoRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

public class FolhaPagamentoService {
    private final EmpregadoRepository empregadoRepository = new EmpregadoRepository();
    private final FormataHoraData fmt = new FormataHoraData();

    public String totalFolha(String dataStr) {
        LocalDate data = fmt.parseData(dataStr, " do totalFolha");

        double total = 0.0;
        for (Empregado e : empregadoRepository.getEmpregados().values()) {
            total += e.calculaPagamento(data);
        }
        return String.format("%.2f",  total);
    }

    public void rodaFolha(String dataStr, String saida) {
        LocalDate data = fmt.parseData(dataStr, " da rodaFolha");
        LocalDate inicio = data.minusDays(6);
        LocalDate fim = data.plusDays(1);
        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.PRIMARY);

        LocalDate primeiroDiaDoMes = data.withDayOfMonth(1);
        LocalDate ultimoDiaDoMes = data.with(TemporalAdjusters.lastDayOfMonth());

        try (PrintWriter out = new PrintWriter(new FileWriter(saida))) {
            out.printf("FOLHA DE PAGAMENTO DO DIA %s%n", data);
            out.println("====================================");
            out.println();

            out.println("===============================================================================================================================");
            out.println("===================== HORISTAS ================================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
            out.println("==================================== ===== ===== ============= ========= =============== ======================================");

            int totalHoras = 0, totalExtras = 0;
            double totalBruto = 0, totalDesc = 0, totalLiq = 0;

            if (!data.isBefore(primeiroDiaDoMes) && !data.isAfter(primeiroDiaDoMes.plusDays(7))) {
                List<EmpregadoHorista> listaHoristas = empregadoRepository.getEmpregados().values().stream()
                        .filter(e -> e instanceof EmpregadoHorista)
                        .map(e -> (EmpregadoHorista) e)
                        .sorted((h1, h2) -> collator.compare(h1.getNome().trim(), h2.getNome().trim()))
                        .toList();

                for (EmpregadoHorista e : listaHoristas) {
                    if (e != null) {
                        double pagamento = e.calculaPagamento(data);

                        int horas = (int) e.getHorasNormais(inicio, fim);
                        int extras = (int) e.getHorasExtras(inicio, fim);

                        double bruto = pagamento;
                        double descontos = (bruto > 0) ? e.calculaTaxa(inicio, fim) : 0;
                        double liquido = pagamento - descontos;

                        totalHoras += horas;
                        totalExtras += extras;
                        totalBruto += bruto;
                        totalDesc += descontos;
                        totalLiq += liquido;

                        out.printf("%-36s %5d %5d %13.2f %9.2f %15.2f ",
                                e.getNome(),
                                horas,
                                extras,
                                bruto,
                                descontos,
                                liquido
                        );
                        if (e.recebeEmBanco()) {
                            out.printf("%s%n", String.format("%s, Ag. %s CC %s", e.getBanco(), e.getAgencia(), e.getContaCorrente()));
                        }
                        else if (e.getMetodoPagamento().equals("correios")) {
                            out.printf("%s%n", String.format("Correios, %s", e.getEndereco()));
                        }
                        else {
                            out.printf("Em maos%n");
                        }
                    }
                }
            }

            out.printf("%nTOTAL HORISTAS%28d %5d %13.2f %9.2f %15.2f%n%n",
                    totalHoras, totalExtras, totalBruto, totalDesc, totalLiq);

            out.println("===============================================================================================================================");
            out.println("===================== ASSALARIADOS ============================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
            out.println("================================================ ============= ========= =============== ======================================");


            double totalBrutoA = 0, totalDescA = 0, totalLiqA = 0;

            if (!data.isBefore(ultimoDiaDoMes.minusDays(7)) && !data.isAfter(ultimoDiaDoMes)){
                List<EmpregadoAssalariado> listaAssalariados = empregadoRepository.getEmpregados().values().stream()
                        .filter(e -> e instanceof EmpregadoAssalariado)
                        .map(e -> (EmpregadoAssalariado) e)
                        .sorted((a1, a2) -> collator.compare(a1.getNome().trim(), a2.getNome().trim()))
                        .toList();

                for (EmpregadoAssalariado e : listaAssalariados) {
                    if (e != null) {
                        double bruto = e.getSalario();
                        double descontos = e.calculaTaxa(ultimoDiaDoMes.getDayOfMonth(), primeiroDiaDoMes, ultimoDiaDoMes);
                        double liquido = bruto - descontos;

                        totalBrutoA += bruto;
                        totalDescA += descontos;
                        totalLiqA += liquido;

                        out.printf("%-48s %13.2f %9.2f %15.2f ",
                                e.getNome(),
                                bruto,
                                descontos,
                                liquido
                        );
                        if (e.recebeEmBanco()) {
                            out.printf("%s%n", String.format("%s, Ag. %s CC %s", e.getBanco(), e.getAgencia(), e.getContaCorrente()));
                        }
                        else if (e.getMetodoPagamento().equals("correios")) {
                            out.printf("%s%n", String.format("Correios, %s", e.getEndereco()));
                        }
                        else {
                            out.printf("Em maos%n");
                        }
                    }
                }
            }

            out.printf("%nTOTAL ASSALARIADOS%44.2f %9.2f %15.2f%n%n",
                    totalBrutoA, totalDescA, totalLiqA);

            out.println("===============================================================================================================================");
            out.println("===================== COMISSIONADOS ===========================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
            out.println("===================== ======== ======== ======== ============= ========= =============== ======================================");

            double totalFixo = 0, totalVendas = 0, totalComissao = 0, totalBrutoC = 0, totalDescC = 0, totalLiqC = 0;

            List<EmpregadoComissionado> listaComissionados = empregadoRepository.getEmpregados().values().stream()
                    .filter(e -> e instanceof EmpregadoComissionado)
                    .map(e -> (EmpregadoComissionado) e)
                    .sorted((c1, c2) -> collator.compare(c1.getNome().trim(), c2.getNome().trim()))
                    .toList();

            for (EmpregadoComissionado e : listaComissionados) {
                if (!e.ehDiaDePagamento(data)) {
                    continue; // pula quem não deve receber nesta data
                }

                LocalDate inicio1 = e.inicioPeriodoParaPagamento(data);
                LocalDate fimExclusive = data.plusDays(1);

                double vendas = e.getVendasPeriodo(data);
                double comissao = vendas * e.getComissao();
                double fixo = (e.getSalario() * 12 / 52) * 2;
                double bruto = fixo + comissao;
                double descontos = e.calculaTaxa(inicio, fimExclusive);
                double liquido = bruto - descontos;

                totalFixo += fixo;
                totalVendas += vendas;
                totalComissao += comissao;
                totalBrutoC += bruto;
                totalDescC += descontos;
                totalLiqC += liquido;

                out.printf("%-21s %8.2f %8.2f %8.2f %13.2f %9.2f %15.2f ",
                        e.getNome(),
                        fixo,
                        vendas,
                        comissao,
                        bruto,
                        descontos,
                        liquido
                );
                if (e.recebeEmBanco()) {
                    out.printf("%s%n", String.format("%s, Ag. %s CC %s", e.getBanco(), e.getAgencia(), e.getContaCorrente()));
                } else if (e.getMetodoPagamento().equals("correios")) {
                    out.printf("%s%n", String.format("Correios, %s", e.getEndereco()));
                } else {
                    out.printf("Em maos%n");
                }
                e.ultimoPagamento = data;
            }

            out.printf("%nTOTAL COMISSIONADOS %10.2f %8.2f %8.2f %13.2f %9.2f %15.2f%n%n",
                    totalFixo, totalVendas, totalComissao, totalBrutoC, totalDescC, totalLiqC);

            double totalFolha = totalBruto + totalBrutoA + totalBrutoC;
            out.printf("TOTAL FOLHA: %.2f%n", totalFolha);
        } catch (IOException e) {
            throw new ErroGeracaoArquivoException("Erro ao gerar arquivo de folha: " + saida);
        }
    }
}
