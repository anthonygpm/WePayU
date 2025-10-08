package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.repository.EmpregadoRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class EmpregadoService {
    private final EmpregadoRepository empregadoRepository;
    private final FormataHoraData fmt;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmpregadoService(EmpregadoRepository empregadoRepository) {
        this.empregadoRepository = empregadoRepository;
        this.fmt = new FormataHoraData();
    }

    public void identificacaoEmpregadoNula(String emp) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
    }

    public void contemEmpregado(String emp) {
        if (!empregadoRepository.containsEmpregado(emp)) {
            throw new EmpregadoNaoExisteException();
        }
    }

    // ---------------- Criar Empregado Horista ou Assalariado ----------------
    public String criarEmpregado (String nome, String endereco, String tipo, String salario) {
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

        int idNum = empregadoRepository.getContador();
        String id = "emp" + idNum;
        empregadoRepository.setContador(idNum + 1);
        if (tipoLower.equals("horista")) {
            empregadoRepository.getEmpregados().put(id, new EmpregadoHorista(nome, endereco, salarioConvertido));
        } else { // assalariado
            empregadoRepository.getEmpregados().put(id, new EmpregadoAssalariado(nome, endereco, salarioConvertido));
        }

        return id;
    }

    // ---------------- Criar Empregado Comissionado ----------------
    public String criarEmpregado (String nome, String endereco, String tipo, String salario, String comissao) {
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

        int idNum = empregadoRepository.getContador();
        String id = "emp" + idNum;
        empregadoRepository.setContador(idNum + 1);
        empregadoRepository.getEmpregados().put(id, new EmpregadoComissionado(nome, endereco, tipo, salarioConvertido, comissaoConvertida));

        return id;
    }

    // ---------------- Remover Empregado ----------------
    public void removerEmpregado(String emp) {
        empregadoRepository.getEmpregados().remove(emp);
    }

    // ---------------- Obter atributos do empregado ----------------
    public String getAtributoEmpregado(String emp, String atributo) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregadoRepository.getEmpregados().containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregados().get(emp);
        return switch (atributo) {
            case "nome" -> e.getNome();
            case "endereco" -> e.getEndereco();
            case "tipo" -> e.getTipo();
            case "salario" -> String.format("%.2f", e.getSalario()).replace(".", ",");
            case "comissao" -> {
                if (!(e instanceof EmpregadoComissionado)) {
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
                } else if (atributo.equals("agencia")) {
                    yield e.getAgencia();
                } else if (atributo.equals("contaCorrente")) {
                    yield e.getContaCorrente();
                } else {
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
                yield String.format("%.2f", e.getTaxaSindical()).replace(".", ",");
            }
            default -> throw new AtributoNaoExisteException();
        };
    }

    public int getNumeroDeEmpregados() {
        return empregadoRepository.getEmpregados().size();
    }

    // ---------------- Buscar empregado pelo nome ----------------
    public String getEmpregadoPorNome(String nome, int indice) {
        if (nome == null || nome.isEmpty())
            throw new NomeNuloException();
        int count = 0;
        for (Map.Entry<String, Empregado> entry : empregadoRepository.getEmpregados().entrySet()) {
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
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregadoRepository.getEmpregados().containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregados().get(emp);
        if (!(e instanceof EmpregadoHorista)) {
            throw new EmpregadoNaoHoristaException();
        }
        if (horas == null || horas.isEmpty()) {
            throw new HorasEmpregadoNulaException();
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
        String dataNorm = fmt.normalizarData(data, " ");
        java.time.LocalDate d;
        try {
            d = java.time.LocalDate.parse(dataNorm, formatter);
        } catch (java.time.format.DateTimeParseException ex) {
            throw new DataInvalidaException();
        }
        ((EmpregadoHorista) e).lancaCartao(d, h);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregadoRepository.getEmpregados().containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregados().get(emp);
        if (!(e instanceof EmpregadoHorista)) {
            throw new EmpregadoNaoHoristaException();
        }

        String inicioNorm = fmt.normalizarData(dataInicial, " inicial ");
        String fimNorm = fmt.normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double horas = ((EmpregadoHorista) e).getHorasNormais(inicio, fim);
        return fmt.formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregadoRepository.getEmpregados().containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregados().get(emp);
        if (!(e instanceof EmpregadoHorista)) {
            return "0";
        }

        String inicioNorm = fmt.normalizarData(dataInicial, " inicial ");
        String fimNorm = fmt.normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double horas = ((EmpregadoHorista) e).getHorasExtras(inicio, fim);
        return fmt.formatarHoras(horas);
    }

    // ---------------- Lança Venda (apenas para comissionado) ----------------
    public void lancaVenda(String emp, String data, String valor) {
        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregadoRepository.containsEmpregado(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregado(emp);
        if (!(e instanceof EmpregadoComissionado)) {
            throw new EmpregadoNaoComissionadoException();
        }

        String dataNorm = fmt.normalizarData(data, " ");
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
        if (!empregadoRepository.getEmpregados().containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregados().get(emp);
        if (!(e instanceof EmpregadoComissionado)) {
            throw new EmpregadoNaoComissionadoException();
        }

        String inicioNorm = fmt.normalizarData(dataInicial, " inicial ");
        String fimNorm = fmt.normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double vendas = ((EmpregadoComissionado) e).getVendas(inicio, fim);

        return String.format("%.2f", vendas).replace(".", ",");
    }

    public void alteraEmpregado(String emp, String atributo, String valor) {
        alteraEmpregado(emp, atributo, valor, null, null);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) {

        if (emp == null || emp.isEmpty()) {
            throw new IdentificacaoEmpregadoNulaException();
        }
        if (!empregadoRepository.containsEmpregado(emp)) {
            throw new EmpregadoNaoExisteException();
        }
        Empregado e = empregadoRepository.getEmpregado(emp);

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
                if (empregadoRepository.getSindicatos().containsKey(idSindicato) && empregadoRepository.getSindicatos().get(idSindicato) != e) {
                    throw new IdentificacaoSindicatoRepetidaException();
                }

                try {
                    double taxa = Double.parseDouble(taxaSindical.replace(",", "."));
                    if  (taxa <= 0) {
                        throw new TaxaSindicalNegativaException();
                    }

                    e.setSindicalizado(true, idSindicato, taxa);
                    empregadoRepository.getSindicatos().put(idSindicato, e);
                }
                catch (NumberFormatException ex) {
                    throw new TaxaSindicalNaoNumericaException();
                }
            }
            else {
                if (e.isSindicalizado()) {
                    empregadoRepository.getSindicatos().remove(e.getIdSindicato());
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
                empregadoRepository.getEmpregados().put(emp, novo); // substitui o antigo
            }
            else if (valor.equals("horista") && !(e instanceof EmpregadoHorista)) {
                EmpregadoHorista novo = new EmpregadoHorista(
                        e.getNome(), e.getEndereco(), e.getSalario()
                );
                empregadoRepository.getEmpregados().put(emp, novo);
            }
            else if (valor.equals("assalariado") && !(e instanceof EmpregadoAssalariado)){
                EmpregadoAssalariado novo = new EmpregadoAssalariado(
                        e.getNome(), e.getEndereco(), e.getSalario()
                );
                empregadoRepository.getEmpregados().put(emp, novo);
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
            if (!(empregadoRepository.getEmpregados().get(emp) instanceof EmpregadoComissionado)) {
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

                empregadoRepository.getEmpregados().get(emp).setComissao(taxa);
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
        Empregado e = empregadoRepository.getEmpregados().get(emp);

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
        Empregado e = empregadoRepository.getEmpregados().get(emp);

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
            empregadoRepository.getEmpregados().put(emp, novo);
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
            empregadoRepository.getEmpregados().put(emp, novo);
        }
        else {
            throw new TipoEmpregadoDesconhecidoException();
        }
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) {
        Empregado e = empregadoRepository.getEmpregados().get(emp);
        if (!e.isSindicalizado()) {
            throw new EmpregadoNaoSindicalizadoException();
        }

        String inicioNorm = fmt.normalizarData(dataInicial, " inicial ");
        String fimNorm = fmt.normalizarData(dataFinal, " final ");
        LocalDate inicio = LocalDate.parse(inicioNorm, formatter);
        LocalDate fim = LocalDate.parse(fimNorm, formatter);

        if (fim.isBefore(inicio)) {
            throw new DataInicialPosteriorException();
        }

        double total = e.getTaxasServico(inicio, fim);

        return String.format("%.2f", total).replace(".", ",");
    }

    public void lancaTaxaServico(String membro, String data, String valor) {
        if (membro.isEmpty()) {
            throw new IdentificacaoMembroNulaException();
        }
        if (!empregadoRepository.getSindicatos().containsKey(membro)) {
            throw new MembroNaoExisteException();
        }

        Empregado e = empregadoRepository.getSindicato(membro);

        String dataNorm = fmt.normalizarData(data, " ");
        LocalDate dataLanc = LocalDate.parse(dataNorm, formatter);
        double v = Double.parseDouble(valor.replace(",", "."));

        if (v <= 0) {
            throw new ValorNaoPositivoException();
        }

        e.lancaTaxaServico(dataLanc, v);
    }

    // Métodos de lógica de negócio serão implementados aqui
}
