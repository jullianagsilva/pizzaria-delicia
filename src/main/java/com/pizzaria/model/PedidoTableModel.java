package com.pizzaria.model;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PedidoTableModel extends AbstractTableModel {
    private final String[] colunas = {"Nº Pedido", "Nome Cliente", "Telefone", "Total", "Data", "Observações"};
    private List<Pedido> pedidos;

    public PedidoTableModel(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public int getRowCount() {
        return pedidos.size();
    }

    @Override
    public int getColumnCount() {
        return colunas.length;
    }

    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pedido pedido = pedidos.get(rowIndex);
        switch (columnIndex) {
            case 0: return pedido.getNumeroPedido();
            case 1: return pedido.getCliente().getNome();
            case 2: return pedido.getCliente().getTelefone();
            case 3: return String.format("€ %.2f", pedido.getTotal());
            case 4: return pedido.getDataPedido();
            case 5: return pedido.getObservacoes();
            default: return null;
        }
    }

    public Pedido getPedidoAt(int rowIndex) {
        return pedidos.get(rowIndex);
    }

    public void setPedidos(List<Pedido> novosPedidos) {
        this.pedidos = novosPedidos;
        fireTableDataChanged();
    }
}
