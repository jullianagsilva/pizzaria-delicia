window.addEventListener("DOMContentLoaded", async () => {
  const TAXA_ENTREGA = 3.50;

  // Elementos do DOM
  const nomeClienteEl = document.getElementById("nomeCliente");
  const telefoneClienteEl = document.getElementById("telefoneCliente");
  const moradaClienteEl = document.getElementById("moradaCliente");
  const resumoCarrinhoPagamento = document.getElementById("resumo-carrinho-pagamento");
  const totalPagamentoEl = document.getElementById("total-pagamento");
  const pagamentoSelect = document.getElementById("pagamento");
  const btnFinalizarPagamento = document.getElementById("finalizarPagamento");
  const btnVoltarPedido = document.getElementById("voltarPedido");
  const btnExportarPedido = document.getElementById("exportarPedido");

  let usuarioLogado = null;
  let carrinho = [];

  // ==========================
  // Carregar usuário logado
  // ==========================
  async function carregarUsuario() {
    try {
      const res = await fetch("/api/usuario-logado");
      if (!res.ok) throw new Error("Não logado");
      usuarioLogado = await res.json();

      nomeClienteEl.textContent = usuarioLogado.nome || "-";
      telefoneClienteEl.textContent = usuarioLogado.telefone || "-";
      moradaClienteEl.textContent = usuarioLogado.morada || "-";

    } catch (err) {
      alert("Você precisa fazer login!");
      window.location.href = "/logincliente";
    }
  }

  // ==========================
  // Carregar carrinho do backend
  // ==========================
  async function carregarCarrinho() {
    try {
      const res = await fetch("/api/carrinho");
      if (!res.ok) {
        carrinho = [];
        return;
      }
      carrinho = await res.json();
    } catch (err) {
      carrinho = [];
    }
  }

  // ==========================
  // Renderizar resumo do carrinho
  // ==========================
  function atualizarResumoCarrinho() {
    resumoCarrinhoPagamento.innerHTML = "";
    let totalItens = 0;

    carrinho.forEach(item => {
      const preco = Number(item.preco) || 0;
      const quantidade = Number(item.quantidade) || 1;
      const subtotal = preco * quantidade;
      totalItens += subtotal;

      const li = document.createElement("li");
      li.textContent = `${item.sabor || "Sem sabor"} (${item.tamanho || "Sem tamanho"}) x${quantidade} - € ${subtotal.toFixed(2)}`;
      if (item.observacoes?.trim()) li.textContent += ` | Observações: ${item.observacoes}`;
      resumoCarrinhoPagamento.appendChild(li);
    });

    // Taxa de entrega
    const liTaxa = document.createElement("li");
    liTaxa.textContent = `Taxa de Entrega - € ${TAXA_ENTREGA.toFixed(2)}`;
    resumoCarrinhoPagamento.appendChild(liTaxa);

    totalPagamentoEl.textContent = (totalItens + TAXA_ENTREGA).toFixed(2);
  }

  // ==========================
  // Finalizar pagamento
  // ==========================
  async function finalizarPagamento() {
    const formaPagamento = pagamentoSelect.value;
    if (!formaPagamento) {
      alert("Selecione a forma de pagamento.");
      return;
    }
    if (carrinho.length === 0) {
      alert("O carrinho está vazio.");
      return;
    }

    const payload = {
      itens: carrinho,
      taxaEntrega: TAXA_ENTREGA,
      formaPagamento
    };

    try {
      const res = await fetch("/api/pedido", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!res.ok) throw new Error("Erro ao processar pagamento");

      window.location.href = "/pedido-finalizado";

    } catch (err) {
      alert("Não foi possível finalizar o pedido. Tente novamente.");
      console.error(err);
    }
  }

  // ==========================
  // Exportar último pedido
  // ==========================
  async function exportarUltimoPedido() {
    try {
      const res = await fetch("/api/pedido/ultimo");
      if (!res.ok) return;

      const pedido = await res.json();
      if (!pedido) return;

      let texto = `Pedido do Cliente\n\n`;
      texto += `Nome: ${pedido.cliente.nome || "-"}\n`;
      texto += `Telefone: ${pedido.cliente.telefone || "-"}\n`;
      texto += `Morada: ${pedido.cliente.morada || "-"}\n\n`;
      texto += `Itens:\n`;

      pedido.itens.forEach(item => {
        texto += `- ${item.sabor || "Sem sabor"} (${item.tamanho || "Sem tamanho"}) x${item.quantidade} - € ${(item.preco * item.quantidade).toFixed(2)}`;
        if (item.observacoes?.trim()) texto += ` | Observações: ${item.observacoes}`;
        texto += "\n";
      });

      texto += `\nTotal dos Itens: € ${(pedido.total - (pedido.taxaEntrega || 0)).toFixed(2)}\n`;
      texto += `Taxa de Entrega: € ${(pedido.taxaEntrega || 0).toFixed(2)}\n`;
      texto += `Total do Pedido: € ${pedido.total.toFixed(2)}\n`;
      texto += `Forma de Pagamento: ${pedido.formaPagamento || "-"}\n`;
      texto += `Status: ${pedido.status || "Pendente"}\n`;
      texto += `Data/Hora: ${new Date(pedido.data).toLocaleString()}\n`;

      const blob = new Blob([texto], { type: "text/plain" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `pedido_${pedido.cliente.nome || "cliente"}.txt`;
      a.click();
      URL.revokeObjectURL(url);

    } catch (err) {
      console.error("Erro ao exportar pedido", err);
    }
  }

  // ==========================
  // Eventos
  // ==========================
  btnFinalizarPagamento.addEventListener("click", finalizarPagamento);
  btnVoltarPedido.addEventListener("click", () => window.location.href = "/pedido");
  btnExportarPedido.addEventListener("click", exportarUltimoPedido);

  // ==========================
  // Inicialização
  // ==========================
  await carregarUsuario();
  await carregarCarrinho();
  atualizarResumoCarrinho();
});
