window.addEventListener("DOMContentLoaded", async () => {
  const listaHistorico = document.getElementById("lista-historico");
  const prevPageBtn = document.getElementById("prevPage");
  const nextPageBtn = document.getElementById("nextPage");
  const paginaAtualSpan = document.getElementById("pagina-atual");

  const itensPorPagina = 10;
  let paginaAtual = 1;

  let historico = [];
  let usuarioLogado = null;

  const formatEuro = v => v.toLocaleString('pt-PT', { style: 'currency', currency: 'EUR' });

  // ==========================
  // Carregar usuário logado
  // ==========================
  async function carregarUsuario() {
    try {
      const res = await fetch("/api/pedidos/usuario-logado");
      if (!res.ok) throw new Error("Não logado");
      usuarioLogado = await res.json();
      if (!usuarioLogado.nome) throw new Error("Não logado");
    } catch (err) {
      alert("Você precisa fazer login!");
      window.location.href = "/logincliente";
    }
  }

  // ==========================
  // Carregar histórico
  // ==========================
  async function carregarHistorico() {
    try {
      let url = usuarioLogado.tipoUsuario === "ADMIN" ? "/api/pedidos/todos" : "/api/pedidos/meus";
      const res = await fetch(url);
      historico = res.ok ? await res.json() : [];
    } catch {
      historico = [];
    }
  }

  // ==========================
  // Filtrar pedidos
  // ==========================
  function filtrarPedidos() {
    if (usuarioLogado.tipoUsuario === "CLIENTE") {
      return historico.filter(p => p.cliente?.id === usuarioLogado.id);
    }
    return historico; // admin vê todos
  }

  // ==========================
  // Renderizar histórico
  // ==========================
  function mostrarHistorico() {
    const pedidos = filtrarPedidos();
    listaHistorico.innerHTML = "";

    const totalPaginas = Math.ceil(pedidos.length / itensPorPagina) || 1;
    if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;
    paginaAtualSpan.textContent = paginaAtual;

    const inicio = (paginaAtual - 1) * itensPorPagina;
    const fim = inicio + itensPorPagina;
    const pedidosPagina = pedidos.slice(inicio, fim);

    pedidosPagina.forEach(pedido => {
      const pizzas = pedido.pizzas || [];
      const numPizzas = pizzas.length || 1;

      // Caso não haja pizzas
      if (pizzas.length === 0) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${pedido.id}</td>
          ${usuarioLogado.tipoUsuario === "ADMIN" ? `<td>${pedido.cliente?.nome || "-"}</td><td>${pedido.cliente?.telefone || "-"}</td>` : ""}
          <td>${pedido.dataPedido ? new Date(pedido.dataPedido).toLocaleString("pt-BR") : "-"}</td>
          <td colspan="4">Sem pizzas</td>
          <td>${formatEuro(pedido.taxaEntrega || 0)}</td>
          <td>${formatEuro(pedido.total || 0)}</td>
          <td>${pedido.formaPagamento || "-"}</td>
          <td>${pedido.status || "Pendente"}</td>
          <td>${pedido.observacoes || "-"}</td>
          <td><button onclick="exportarPedido(${pedido.id})">Exportar</button></td>
        `;
        listaHistorico.appendChild(tr);
      } else {
        pizzas.forEach((pizza, index) => {
          const tr = document.createElement("tr");
          const subtotalItem = (pizza.preco || 0) * (pizza.quantidade || 1);

          tr.innerHTML = `
            ${index === 0 ? `<td rowspan="${numPizzas}">${pedido.id}</td>` : ""}
            ${usuarioLogado.tipoUsuario === "ADMIN" && index === 0 ? `<td rowspan="${numPizzas}">${pedido.cliente?.nome || "-"}</td><td rowspan="${numPizzas}">${pedido.cliente?.telefone || "-"}</td>` : ""}
            ${index === 0 ? `<td rowspan="${numPizzas}">${pedido.dataPedido ? new Date(pedido.dataPedido).toLocaleString("pt-BR") : "-"}</td>` : ""}
            <td>${pizza.sabor}</td>
            <td>${pizza.tamanho}</td>
            <td>${pizza.quantidade}</td>
            <td>${formatEuro(subtotalItem)}</td>
            ${index === 0 ? `<td rowspan="${numPizzas}">${formatEuro(pedido.taxaEntrega || 0)}</td>` : ""}
            ${index === 0 ? `<td rowspan="${numPizzas}">${formatEuro(pedido.total || 0)}</td>` : ""}
            ${index === 0 ? `<td rowspan="${numPizzas}">${pedido.formaPagamento || "-"}</td>` : ""}
            ${index === 0 ? `<td rowspan="${numPizzas}">${pedido.status || "Pendente"}</td>` : ""}
            ${index === 0 ? `<td rowspan="${numPizzas}">${pedido.observacoes || "-"}</td>` : ""}
            ${index === 0 ? `<td rowspan="${numPizzas}"><button onclick="exportarPedido(${pedido.id})">Exportar</button></td>` : ""}
          `;
          listaHistorico.appendChild(tr);
        });
      }
    });

    prevPageBtn.disabled = paginaAtual === 1;
    nextPageBtn.disabled = paginaAtual === totalPaginas;
  }

  // ==========================
  // Exportar pedido
  // ==========================
  async function exportarPedido(id) {
    try {
      const res = await fetch(`/api/pedidos/${id}`);
      if (!res.ok) return;
      const pedido = await res.json();

      let texto = `Pedido Nº: ${pedido.id}\nCliente: ${pedido.cliente?.nome || "-"}\n\nItens:\n`;
      (pedido.pizzas || []).forEach(p => {
        texto += `- ${p.sabor} (${p.tamanho}) x${p.quantidade} - ${formatEuro(p.preco * p.quantidade)}\n`;
        if (p.observacoes) texto += `  Obs: ${p.observacoes}\n`;
      });
      texto += `\nTotal: ${formatEuro(pedido.total || 0)}\nPagamento: ${pedido.formaPagamento || "-"}\nStatus: ${pedido.status || "Pendente"}\nData: ${pedido.dataPedido ? new Date(pedido.dataPedido).toLocaleString("pt-BR") : "-"}`;

      const blob = new Blob([texto], { type: "text/plain" });
      const a = document.createElement("a");
      a.href = URL.createObjectURL(blob);
      a.download = `pedido_${pedido.id}.txt`;
      a.click();
      URL.revokeObjectURL(a.href);
    } catch (err) {
      console.error("Erro ao exportar pedido", err);
    }
  }

  // ==========================
  // Paginação
  // ==========================
  prevPageBtn.addEventListener("click", () => { 
    if (paginaAtual > 1) { paginaAtual--; mostrarHistorico(); } 
  });
  nextPageBtn.addEventListener("click", () => { 
    const totalPaginas = Math.ceil(filtrarPedidos().length / itensPorPagina);
    if (paginaAtual < totalPaginas) { paginaAtual++; mostrarHistorico(); }
  });

  window.exportarPedido = exportarPedido;

  // ==========================
  // Inicialização
  // ==========================
  await carregarUsuario();
  await carregarHistorico();
  mostrarHistorico();
});
