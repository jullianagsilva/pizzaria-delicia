package com.pizzaria.service;

import com.pizzaria.model.*;
import com.pizzaria.repository.ClienteDAO;
import com.pizzaria.repository.PagamentoDAO;
import com.pizzaria.repository.PedidoDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PizzariaService {

    private final ClienteDAO clienteDAO;
    private final PedidoDAO pedidoDAO;
    private final PagamentoDAO pagamentoDAO;
    private final PagamentoService pagamentoService;

    @Autowired
    public PizzariaService(
        ClienteDAO clienteDAO,
        PedidoDAO pedidoDAO,
        PagamentoDAO pagamentoDAO,
        PagamentoService pagamentoService
    ) {
        this.clienteDAO = clienteDAO;
        this.pedidoDAO = pedidoDAO;
        this.pagamentoDAO = pagamentoDAO;
        this.pagamentoService = pagamentoService;
    }

    public Clientes registrarCliente(Clientes cliente) throws Exception {
        clienteDAO.salvar(cliente);
        if (cliente.getId() == 0) {
            throw new Exception("Erro ao salvar cliente no banco.");
        }
        return cliente;
    }

    public Clientes atualizarCliente(Clientes cliente) throws Exception {
        boolean sucesso = clienteDAO.atualizar(cliente);
        if (!sucesso) {
            throw new Exception("Erro ao atualizar cliente.");
        }
        return cliente;
    }

    public Pedido registrarPedido(Clientes cliente, Carrinho carrinho, String observacoes) throws Exception {
        Pedido pedido = new Pedido(cliente, carrinho, observacoes);
        System.out.println("Carrinho no momento do salvarPedido:");
        System.out.println(pedido.getCarrinho().mostrarCarrinho());

        pedidoDAO.salvarPedido(pedido);
        if (pedido.getNumeroPedido() == 0) {
            throw new Exception("Erro ao salvar o pedido.");
        }
        return pedido;
    }

    public Pagamento processarPagamento(Pedido pedido) {
        String status = pagamentoService.simularPagamento(pedido.getTotal());

        Pagamento pagamento = new Pagamento();
        pagamento.setIdPedido(pedido.getNumeroPedido());
        pagamento.setValor(pedido.getTotal());
        pagamento.setStatus(status);

        pagamentoDAO.salvar(pagamento);
        return pagamento;
    }

    public Pedido realizarPedido(String nome, String telefone, String morada, Carrinho carrinho, String observacoes) throws Exception {
        Clientes cliente = new Clientes(nome, telefone, morada);
        registrarCliente(cliente);
        Pedido pedido = registrarPedido(cliente, carrinho, observacoes);
        Pagamento pagamento = processarPagamento(pedido);

        if (!"sucesso".equalsIgnoreCase(pagamento.getStatus())) {
            throw new Exception("Pagamento recusado.");
        }

        pedido.setPagamento(pagamento);
        return pedido;
    }

    public Pedido finalizarPedido(Clientes cliente, Carrinho carrinho, String observacoes) throws Exception {
        Pedido pedido = registrarPedido(cliente, carrinho, observacoes);
        return finalizarPedido(pedido); // reutiliza lógica
    }

    public Pedido finalizarPedido(Pedido pedido) throws Exception {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não pode ser nulo.");
        }

        if (pedido.getNumeroPedido() == 0) {
            pedidoDAO.salvarPedido(pedido);
            if (pedido.getNumeroPedido() == 0) {
                throw new Exception("Erro ao salvar o pedido.");
            }
        }

        Pagamento pagamento = processarPagamento(pedido);
        if (!"sucesso".equalsIgnoreCase(pagamento.getStatus())) {
            throw new Exception("Pagamento recusado.");
        }

        pedido.setPagamento(pagamento);
        return pedido;
    }
}
