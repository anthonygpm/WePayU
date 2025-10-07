package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Cloneable {
    protected String nome;
    protected String endereco;
    protected String tipo;
    protected double salario;
    protected double comissao;

    protected String metodoPagamento;
    protected String banco;
    protected String agencia;
    protected String contaCorrente;

    protected boolean sindicalizado = false;
    private String idSindicato;
    protected double taxaSindical; // valor fixo periódico
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public LocalDate dataContrato;
    public LocalDate ultimoPagamento;

    public Empregado(String nome, String endereco, String tipo, double salario) {
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.metodoPagamento = "emMaos";
    }

    public void setSindicalizado(boolean sindicalizado, String idSindicato, double taxaSindical) {
        this.sindicalizado = sindicalizado;
        if (sindicalizado) {
            this.idSindicato = idSindicato;
            this.taxaSindical = taxaSindical;
        } else {
            this.idSindicato = null;
            this.taxaSindical = 0.0;
            this.taxasServico.clear();
        }
    }

    public void lancaTaxaServico(LocalDate data, double valor) {
        if (!sindicalizado) {
            throw new IllegalArgumentException("Empregado nao eh sindicalizado.");
        }
        taxasServico.add(new TaxaServico(data, valor));
    }

    public double getTaxasServico(LocalDate inicio, LocalDate fimExclusive) {
        if (!sindicalizado) return 0.0;

        return taxasServico.stream()
                .filter(t -> !t.getData().isBefore(inicio) && t.getData().isBefore(fimExclusive))
                .mapToDouble(TaxaServico::getValor)
                .sum();
    }

    public boolean recebeEmBanco() {
        if (metodoPagamento.equals("banco")) {
            return true;
        }
        return false;
    }

    public abstract double calculaPagamento(LocalDate dataPagamento);

    protected long diasDesdeUltimoPagamento(LocalDate dataAtual) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(ultimoPagamento, dataAtual);
        ultimoPagamento = dataAtual; // atualiza para o próximo pagamento
        return dias;
    }

    @Override
    public Empregado clone() {
        try {
            Empregado copia = (Empregado) super.clone();
            copia.taxasServico = new ArrayList<>();
            for (TaxaServico taxa : this.taxasServico) {
                copia.taxasServico.add(taxa.clone());
            }
            return copia;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public void setMetodoPagamentoBanco(String metodoPagamento, String banco, String agencia, String contaCorrente) {
        this.metodoPagamento = metodoPagamento;
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }

    public double getComissao() { return comissao; }
    public void setComissao(double comissao) { this.comissao = comissao; }

    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindicato() { return idSindicato; }

    public double getTaxaSindical() { return taxaSindical; }
}
