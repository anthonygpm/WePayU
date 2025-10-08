package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.service.EmpregadoService;
import br.ufal.ic.p2.wepayu.service.FolhaPagamentoService;

import java.util.*;

public class Facade {
    private final EmpregadoService empregadoService;
    private final FolhaPagamentoService folhaPagamentoService;
    private final HistoricoSistema historico;

    private Map<String,Empregado> empregados = new LinkedHashMap<>();
    private Map<String, Empregado> sindicatos = new LinkedHashMap<>();
    private int contador = 0, proximoId = 0;

    public Facade(EmpregadoService empregadoService,FolhaPagamentoService folhaPagamentoService, HistoricoSistema historico) {
        this.empregadoService = empregadoService;
        this.folhaPagamentoService = folhaPagamentoService;
        this.historico = historico;
    }

    public Facade() {
        this.empregados = new LinkedHashMap<>();
        this.sindicatos = new LinkedHashMap<>();
        this.contador = 1;
        this.proximoId = 0;
        var empregadoRepository = new br.ufal.ic.p2.wepayu.repository.EmpregadoRepository(empregados, sindicatos, contador, proximoId);
        this.empregadoService = new EmpregadoService(empregadoRepository);
        this.folhaPagamentoService = new FolhaPagamentoService();
        this.historico = new HistoricoSistema();
    }

    public void zerarSistema() {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregados.clear();
        sindicatos.clear();
        contador = 1;
        proximoId = 0;
    }

    // ---------------- Criar Empregado Horista ou Assalariado ----------------
    public String criarEmpregado (String nome, String endereco, String tipo, String salario) {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);;
        return empregadoService.criarEmpregado(nome, endereco, tipo, salario);
    }

    // ---------------- Criar Empregado Comissionado ----------------
    public String criarEmpregado (String nome, String endereco, String tipo, String salario, String comissao) {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);;
        return empregadoService.criarEmpregado(nome, endereco, tipo, salario, comissao);
    }

    // ---------------- Remover Empregado ----------------
    public void removerEmpregado (String emp) {
        empregadoService.identificacaoEmpregadoNula(emp);
        empregadoService.contemEmpregado(emp);

        historico.salvarEstado(empregados, sindicatos, contador, proximoId);;
        empregadoService.removerEmpregado(emp);
    }

    // ---------------- Obter atributos do empregado ----------------
    public String getAtributoEmpregado(String emp, String atributo) {
        return empregadoService.getAtributoEmpregado(emp, atributo);
    }

    public int getNumeroDeEmpregados() {
        return empregadoService.getNumeroDeEmpregados();
    }

    // ---------------- Pilha de Comandos ----------------
    private void salvarEstado() {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
    }

    public void undo() {
        EstadoSistema estadoAnterior = historico.desfazer();
        this.empregados = estadoAnterior.empregadosBackup;
        this.sindicatos = estadoAnterior.sindicatosBackup;
        this.contador = estadoAnterior.contadorBackup;
        this.proximoId = estadoAnterior.proximoIdBackup;
    }


    // ---------------- Buscar empregado pelo nome ----------------
    public String getEmpregadoPorNome(String nome, int indice) {
        return empregadoService.getEmpregadoPorNome(nome, indice);
    }

    // ---------------- Horas trabalhadas (apenas para horista) ----------------
    public void lancaCartao(String emp, String data, String horas) {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregadoService.lancaCartao(emp, data, horas);
    }


    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) {
        return empregadoService.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) {
        return empregadoService.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    // ---------------- Lança Venda (apenas para comissionado) ----------------
    public void lancaVenda(String emp, String data, String valor) {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregadoService.lancaVenda(emp, data, valor);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) {
        return empregadoService.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    // ---------------- Atualiza Empregado ----------------
    public void alteraEmpregado(String emp, String atributo, String valor) {
        empregadoService.alteraEmpregado(emp, atributo, valor);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregadoService.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) {
        empregadoService.contemEmpregado(emp);
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregadoService.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String comissao) {
        empregadoService.contemEmpregado(emp);
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregadoService.alteraEmpregado(emp, atributo, valor, comissao);
    }

    // ---------------- Taxas de Serviço ----------------
    public String getTaxasServico(String emp, String dataInicial, String dataFinal) {
        empregadoService.contemEmpregado(emp);
        return empregadoService.getTaxasServico(emp, dataInicial, dataFinal);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws MembroNaoExisteException {
        historico.salvarEstado(empregados, sindicatos, contador, proximoId);
        empregadoService.lancaTaxaServico(membro, data, valor);
    }

    // ---------------- Folha de Pagamento ----------------
    public String totalFolha(String dataStr) {
        return folhaPagamentoService.totalFolha(dataStr);
    }

    public void rodaFolha(String dataStr, String saida) {
        folhaPagamentoService.rodaFolha(dataStr, saida);
    }


    public void encerrarSistema() {}
}