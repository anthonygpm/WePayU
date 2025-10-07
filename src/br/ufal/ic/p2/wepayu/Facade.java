package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;

import javax.naming.directory.InvalidAttributesException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class Facade {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Map<String,Empregado> empregados = new LinkedHashMap<>();
    private Map<String, Empregado> sindicatos = new LinkedHashMap<>();
    private Stack<EstadoSistema> historico = new Stack<>();
    private int contador = 0, proximoId = 0;

    public void zerarSistema() {
        salvarEstado();
        empregados.clear();
        sindicatos.clear();
        contador = 1;
        proximoId = 0;
    }

    // ---------------- Criar Empregado Horista ou Assalariado ----------------
    public String criarEmpregado (String nome, String endereco, String tipo, String salario) {
        salvarEstado();

        if (nome==null || nome.isEmpty())
            throw new NomeNuloException();

        if (endereco==null || endereco.isEmpty())
            throw new EnderecoNuloException();

        String tipoLower = tipo.toLowerCase();
        if (!tipoLower.equals("horista") && !tipoLower.equals("assalariado") && !tipoLower.equals("comissionado"))
            throw new TipoEmpregadoInvalidoException();

        if (tipoLower.equals("comissionado"))
            throw new TipoNaoAplicavelException();

        if (salario==null || salario.isEmpty())
            throw new SalarioNuloException();

        double salarioConvertido;
        try {
            salarioConvertido = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException();
        }

        if (salarioConvertido < 0)
            throw new SalarioNegativoException();

        String id = "emp" + contador++;
        if (tipoLower.equals("horista")) {
            empregados.put(id, new EmpregadoHorista(nome, endereco, salarioConvertido));
        } else { // assalariado
            empregados.put(id, new EmpregadoAssalariado(nome, endereco, salarioConvertido));
        }

        return id;
    }

    // ---------------- Criar Empregado Comissionado ----------------
    public String criarEmpregado (String nome, String endereco, String tipo, String salario, String comissao) {
        salvarEstado();

        if (nome==null || nome.isEmpty())
            throw new NomeNuloException();

        if (endereco==null || endereco.isEmpty())
            throw new EnderecoNuloException();

        String tipoLower = tipo.toLowerCase();
        if (!tipoLower.equals("horista") && !tipoLower.equals("assalariado") && !tipoLower.equals("comissionado"))
            throw new TipoEmpregadoInvalidoException();

        if (tipoLower.equals("horista") || tipoLower.equals("assalariado"))
            throw new TipoNaoAplicavelException();

        if (salario==null || salario.isEmpty())
            throw new SalarioNuloException();

        double salarioConvertido;
        try {
            salarioConvertido = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException();
        }

        if (salarioConvertido < 0)
            throw new SalarioNegativoException();

        if (comissao==null || comissao.isEmpty())
            throw new ComissaoNulaException();

        double comissaoConvertida;
        try {
            comissaoConvertida = Double.parseDouble(comissao.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ComissaoNaoNumericaException();
        }

        if (comissaoConvertida < 0)
            throw new ComissaoNegativaException();

        String id = "emp" + contador++;
        empregados.put(id, new EmpregadoComissionado(nome, endereco, tipo, salarioConvertido, comissaoConvertida));

        return id;
    }

    // ---------------- Remover Empregado ----------------
    public void removerEmpregado (String emp) {
        if (emp==null || emp.isEmpty())
            throw new IdentificacaoEmpregadoNulaException();

        if (!empregados.containsKey(emp))
            throw new EmpregadoNaoExisteException();

        salvarEstado();
        empregados.remove(emp);
    }

    // ---------------- Obter atributos do empregado ----------------
    public String getAtributoEmpregado(String emp, String atributo) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado e = empregados.get(emp);

        return switch (atributo) {
            case "nome" -> e.getNome();
            case "endereco" -> e.getEndereco();
            case "tipo" -> e.getTipo();
            case "salario" -> String.format("%.2f", e.getSalario());

            case "comissao" -> {
                if (!(e instanceof EmpregadoComissionado ec)) {
                    throw new EmpregadoNaoComissionadoException();
                }
                yield String.format("%.2f", e.getComissao()).replace(".", ",");
            }

            case "sindicalizado" -> Boolean.toString(e.isSindicalizado());

            case "metodoPagamento" -> e.getMetodoPagamento();

            case "banco", "agencia", "contaCorrente" -> {
                if (!e.recebeEmBanco()) {
                    throw new EmpregadoNaoRecebeBancoException();
                }
                if (atributo.equals("banco")) {
                    yield e.getBanco();
                }
                else if (atributo.equals("agencia")) {
                    yield e.getAgencia();
                }
                else if (atributo.equals("contaCorrente")) {
                    yield e.getContaCorrente();
                }
                else {
                    yield null;
                }
            }

            case "idSindicato" -> {
                if (!e.isSindicalizado()) {
                    throw new EmpregadoNaoSindicalizadoException();
                }

                yield e.getIdSindicato();
            }

            case "taxaSindical" -> {
                if (!e.isSindicalizado()) {
                    throw new EmpregadoNaoSindicalizadoException();
                }

                yield String.format("%.2f", e.getTaxaSindical());
            }

            default -> throw new AtributoNaoExisteException();
        };
    }

    public int getNumeroDeEmpregados() {
        return empregados.size();
    }

    // ---------------- Pilha de Comandos ----------------
    private void salvarEstado() {
        historico.push(new EstadoSistema(empregados, sindicatos, contador, proximoId));
    }

    public void undo() {
        if (historico.isEmpty()) {
            throw new HistoricoVazioException();
        }
        EstadoSistema estadoAnterior = historico.pop();
        this.empregados = estadoAnterior.empregadosBackup;
        this.sindicatos = estadoAnterior.sindicatosBackup;
        this.contador = estadoAnterior.contadorBackup;
        this.proximoId = estadoAnterior.proximoIdBackup;
    }


    // ---------------- Buscar empregado pelo nome ----------------
    public String getEmpregadoPorNome(String nome, int indice) {
        if (nome==null || nome.isEmpty())
            throw new NomeNuloException();

        int count = 0;
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            Empregado e = entry.getValue();
            if (e.getNome().contains(nome)) {
                count++;
                if (count == indice)
                    return entry.getKey();
            }
        }

        throw new EmpregadoNaoEncontradoException();
    }

    // ---------------- Horas trabalhadas (apenas para horista) ----------------
    public void lancaCartao(String emp, String data, String horas) {
        salvarEstado();

        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregados.get(emp);
        if (!(e instanceof EmpregadoHorista)) {
            throw new EmpregadoNaoHoristaException();
        }
        if (horas==null || horas.isEmpty()) {
            throw new HorasEmpregadoNulaException();
        }

        String dataNorm = normalizarData(data, " ");
        LocalDate d;
        try {
            d = LocalDate.parse(dataNorm, formatter);
        } catch (DateTimeParseException ex) {
            throw new DataInvalidaException();
        }

        double h;
        try {
            h = Double.parseDouble(horas.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new HorasNaoNumericasException();
        }
        if (h <= 0) {
            throw new HorasNaoPositivasException();
        }

        ((EmpregadoHorista) e).lancaCartao(d, h);
    }


    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregados.get(emp);
        if (!(e instanceof EmpregadoHorista)) {
            throw new EmpregadoNaoHoristaException();
        }

        String inicioNorm = normalizarData(dataInicial, " inicial ");
        String fimNorm = normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double horas = ((EmpregadoHorista) e).getHorasNormais(inicio, fim);
        return formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregados.get(emp);
        if (!(e instanceof EmpregadoHorista)) {
            return "0";
        }

        String inicioNorm = normalizarData(dataInicial, " inicial ");
        String fimNorm = normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double horas = ((EmpregadoHorista) e).getHorasExtras(inicio, fim);
        return formatarHoras(horas);
    }

    // ---------------- Lança Venda (apenas para comissionado) ----------------
    public void lancaVenda(String emp, String data, String valor) {
        salvarEstado();

        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregados.get(emp);
        if (!(e instanceof EmpregadoComissionado)) {
            throw new EmpregadoNaoComissionadoException();
        }

        String dataNorm = normalizarData(data, " ");
        LocalDate d = LocalDate.parse(dataNorm, formatter);

        double v;
        try {
            v = Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new ValorNaoNumericoException();
        }
        if (v <= 0) {
            throw new ValorNaoPositivoException();
        }

        ((EmpregadoComissionado) e).lancaVenda(d, v);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregados.get(emp);
        if (!(e instanceof EmpregadoComissionado)) {
            throw new EmpregadoNaoComissionadoException();
        }

        String inicioNorm = normalizarData(dataInicial, " inicial ");
        String fimNorm = normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double vendas = ((EmpregadoComissionado) e).getVendas(inicio, fim);

        return String.format("%.2f", vendas).replace(".", ",");
    }


    // ---------------- Atualiza Empregado ----------------
    public void alteraEmpregado(String emp, String atributo, String valor) {
        alteraEmpregado(emp, atributo, valor, null, null);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) {
        salvarEstado();

        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregados.get(emp);

        if (atributo.equals("sindicalizado")) {
            if (!valor.equals("true") &&  !valor.equals("false")) {
                throw new ValorNaoBooleanoException();
            }
            boolean sindicalizado = Boolean.parseBoolean(valor);

            if (sindicalizado) {
                if (idSindicato == null || idSindicato.isEmpty()) {
                    throw new IdentificacaoSindicatoNulaException();
                }
                if  (taxaSindical == null || taxaSindical.isEmpty()) {
                    throw new TaxaSindicalNulaException();
                }
                if (sindicatos.containsKey(idSindicato) && sindicatos.get(idSindicato) != e) {
                    throw new IdentificacaoSindicatoRepetidaException();
                }

                try {
                    double taxa = Double.parseDouble(taxaSindical.replace(",", "."));
                    if  (taxa <= 0) {
                        throw new TaxaSindicalNegativaException();
                    }

                    e.setSindicalizado(true, idSindicato, taxa);
                    sindicatos.put(idSindicato, e);
                }
                catch (NumberFormatException ex) {
                    throw new TaxaSindicalNaoNumericaException();
                }
            }
            else {
                if (e.isSindicalizado()) {
                    sindicatos.remove(e.getIdSindicato());
                }
                e.setSindicalizado(false, null, 0.0);
            }
        }
        else if (atributo.equals("nome")) {
            if (valor == null || valor.isEmpty()) {
                throw new NomeNuloException();
            }
            e.setNome(valor);
        }
        else if (atributo.equals("endereco")) {
            if (valor == null || valor.isEmpty()) {
                throw new EnderecoNuloException();
            }
            e.setEndereco(valor);
        }
        else if (atributo.equals("tipo")) {
            if (valor.equals("comissionado") && !(e instanceof EmpregadoComissionado)) {
                EmpregadoComissionado novo = new EmpregadoComissionado(
                        e.getNome(), e.getEndereco(), "comissionado", e.getSalario(), 0.0
                );
                empregados.put(emp, novo); // substitui o antigo
            }
            else if (valor.equals("horista") && !(e instanceof EmpregadoHorista)) {
                EmpregadoHorista novo = new EmpregadoHorista(
                        e.getNome(), e.getEndereco(), e.getSalario()
                );
                empregados.put(emp, novo);
            }
            else if (valor.equals("assalariado") && !(e instanceof EmpregadoAssalariado)){
                EmpregadoAssalariado novo = new EmpregadoAssalariado(
                        e.getNome(), e.getEndereco(), e.getSalario()
                );
                empregados.put(emp, novo);
            }
            else {
                throw new TipoEmpregadoInvalidoException();
            }
        }
        else if (atributo.equals("salario")) {
            if (valor == null || valor.isEmpty()) {
                throw new SalarioNuloException();
            }

            try {
                double salario = Double.parseDouble(valor);
                if (salario <= 0) {
                    throw new SalarioNegativoException();
                }

                e.setSalario(salario);
            }
            catch (NumberFormatException ex) {
                throw new SalarioNaoNumericoException();
            }
        }
        else if (atributo.equals("comissao")) {
            if (!(empregados.get(emp) instanceof EmpregadoComissionado)) {
                throw new EmpregadoNaoComissionadoException();
            }

            if (valor == null || valor.isEmpty()) {
                throw new ComissaoNulaException();
            }

            try {
                double taxa = Double.parseDouble(valor.replace(",", "."));
                if (taxa <= 0) {
                    throw new ComissaoNegativaException();
                }

                empregados.get(emp).setComissao(taxa);
            }
            catch (NumberFormatException ex) {
                throw new ComissaoNaoNumericaException();
            }

        }
        else if (atributo.equals("metodoPagamento")) {
            if (valor.equals("banco") || valor.equals("emMaos") || valor.equals("correios")) {
                e.setMetodoPagamento(valor);
            }
            else {
                throw new MetodoPagamentoInvalidoException();
            }
        }
        else {
            throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) {
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        salvarEstado();

        Empregado e = empregados.get(emp);

        if (!atributo.equals("metodoPagamento")) {
            throw new AtributoDesconhecidoException();
        }

        if (valor1.equals("banco")) {
            if (banco == null || banco.isEmpty()) {
                throw new BancoNuloException();
            }
            if (agencia == null || agencia.isEmpty()) {
                throw new AgenciaNulaException();
            }
            if (contaCorrente == null || contaCorrente.isEmpty()) {
                throw new ContaCorrenteNulaException();
            }

            e.setMetodoPagamentoBanco("banco", banco, agencia, contaCorrente);
        }
        else {
            throw new MetodoPagamentoDesconhecidoException();
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String comissao) {
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        salvarEstado();

        Empregado e = empregados.get(emp);

        if (!atributo.equals("tipo")) {
            throw new AtributoDesconhecidoException();
        }

        if (valor.equals("comissionado")) {
            double taxaComissao = Double.parseDouble(comissao.replace(",", "."));

            // cria um novo comissionado, mas preserva os dados básicos
            EmpregadoComissionado novo = new EmpregadoComissionado(
                    e.getNome(),
                    e.getEndereco(),
                    "comissionado",
                    e.getSalario(),
                    taxaComissao
            );

            // também preserva infos de sindicato e método de pagamento
            if (e.isSindicalizado()) {
                novo.setSindicalizado(true, e.getIdSindicato(), e.getTaxaSindical());
            }
            if (e.getMetodoPagamento().equals("banco")) {
                novo.setMetodoPagamentoBanco(e.getMetodoPagamento(), e.getBanco(), e.getAgencia(), e.getContaCorrente());
            } else {
                novo.setMetodoPagamento(e.getMetodoPagamento());
            }

            // substitui na coleção
            empregados.put(emp, novo);
        }
        else if (valor.equals("horista")) {
            double salario = Double.parseDouble(comissao.replace(",", "."));

            // cria um novo horista, mas preserva os dados básicos
            EmpregadoHorista novo = new EmpregadoHorista (
                    e.getNome(),
                    e.getEndereco(),
                    Double.parseDouble(comissao)
            );

            // também preserva infos de sindicato e método de pagamento
            if (e.isSindicalizado()) {
                novo.setSindicalizado(true, e.getIdSindicato(), e.getTaxaSindical());
            }
            if (e.getMetodoPagamento().equals("banco")) {
                novo.setMetodoPagamentoBanco(e.getMetodoPagamento(), e.getBanco(), e.getAgencia(), e.getContaCorrente());
            } else {
                novo.setMetodoPagamento(e.getMetodoPagamento());
            }

            // substitui na coleção
            empregados.put(emp, novo);
        }
        else {
            throw new TipoEmpregadoDesconhecidoException();
        }
    }

    // ---------------- Taxas de Serviço ----------------
    public String getTaxasServico(String emp, String dataInicial, String dataFinal) {
        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado e = empregados.get(emp);
        if (!e.isSindicalizado()) {
            throw new EmpregadoNaoSindicalizadoException();
        }

        String inicioNorm = normalizarData(dataInicial, " inicial ");
        String fimNorm = normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double total = e.getTaxasServico(inicio, fim);

        return String.format("%.2f", total).replace(".", ",");
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws MembroNaoExisteException {
        salvarEstado();

        if (membro.isEmpty()) {
            throw new IdentificacaoMembroNulaException();
        }
        if (!sindicatos.containsKey(membro)) {
            throw new MembroNaoExisteException();
        }

        Empregado e = sindicatos.get(membro);

        String dataNorm = normalizarData(data, " ");
        LocalDate dataLanc = LocalDate.parse(dataNorm, formatter);
        double v = Double.parseDouble(valor.replace(",", "."));

        if (v <= 0) {
            throw new ValorNaoPositivoException();
        }

        e.lancaTaxaServico(dataLanc, v);
    }

    // ---------------- Formatação ----------------
    private String formatarHoras(double valor) {
        double rounded = Math.round(valor * 100.0) / 100.0;
        if (Math.abs(rounded - Math.round(rounded)) < 1e-9) {
            return String.valueOf((int) Math.round(rounded));
        } else {
            String s = BigDecimal.valueOf(rounded).stripTrailingZeros().toPlainString();
            return s.replace(".", ",");
        }
    }

    private String normalizarData(String data, String momento) {
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

    private LocalDate parseData(String dataStr, String momento) {
        String dataNormalizada = normalizarData(dataStr, momento);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dataNormalizada, formatter);
    }

    // ---------------- Folha de Pagamento ----------------
    public String totalFolha(String dataStr) {
        LocalDate data = parseData(dataStr, " do totalFolha");

        double total = 0.0;
        for (Empregado e : empregados.values()) {
            total += e.calculaPagamento(data);
        }
        return String.format("%.2f",  total);
    }

    public void rodaFolha(String dataStr, String saida) {
        LocalDate data = parseData(dataStr, " da rodaFolha");
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
                List<EmpregadoHorista> listaHoristas = empregados.values().stream()
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
                List<EmpregadoAssalariado> listaAssalariados = empregados.values().stream()
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

            List<EmpregadoComissionado> listaComissionados = empregados.values().stream()
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


    public void encerrarSistema() {}
}