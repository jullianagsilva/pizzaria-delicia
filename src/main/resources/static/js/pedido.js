const TAXA_ENTREGA = 3.50;

const saborEl = document.getElementById("sabor");
const tamanhoEl = document.getElementById("tamanho");
const observacoesEl = document.getElementById("observacoes");
const moradaEl = document.getElementById("morada");

const btnAdicionar = document.getElementById("adicionar");
const resumoCarrinhoEl = document.getElementById("resumo-carrinho");
const totalEl = document.getElementById("total");

const btnAbrirModal = document.getElementById("abrir-modal");
const modalCarrinho = document.getElementById("modal-carrinho");
const btnFecharModal = document.getElementById("fechar-modal");
const tabelaCorpo = document.querySelector("#tabela-carrinho tbody");
const totalModalEl = document.getElementById("total-modal");

const btnPagamento = document.getElementById("btnPagamento");
const btnHistorico = document.getElementById("btnHistorico");

let carrinho = []; // mantido no frontend

// ==========================
// Atualizar carrinho na tela
// ==========================
function atualizarCarrinho() {
  resumoCarrinhoEl.innerHTML = "";
  tabelaCorpo.innerHTML = "";
  let subtotalGeral = 0;

  carrinho.forEach((item, index) => {
    const subtotal = item.preco * item.quantidade;
    subtotalGeral += subtotal;

    // Lista resumida
    const li = document.createElement("li");
    li.innerHTML = `${item.sabor} (${item.tamanho}) x${item.quantidade} - € ${subtotal.toFixed(2)}`;
    resumoCarrinhoEl.appendChild(li);

    // Tabela detalhada
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${item.sabor}</td>
      <td>${item.tamanho}</td>
      <td>€ ${item.preco.toFixed(2)}</td>
      <td><input type="number" min="1" value="${item.quantidade}" class="quantidade-input" data-index="${index}"></td>
      <td>${item.observacoes || "-"}</td>
      <td>€ ${subtotal.toFixed(2)}</td>
      <td><button class="remover-btn" data-index="${index}">Remover</button></td>
    `;
    tabelaCorpo.appendChild(tr);
  });

  const totalComTaxa = subtotalGeral + TAXA_ENTREGA;
  totalEl.textContent = totalComTaxa.toFixed(2);
  totalModalEl.textContent = totalComTaxa.toFixed(2);
}

// ==========================
// Adicionar pizza
// ==========================
btnAdicionar.addEventListener("click", () => {
  if (!moradaEl.value.trim()) {
    alert("Preencha sua morada!");
    return;
  }

  const sabor = saborEl.value;
  const tamanho = tamanhoEl.value;
  const preco = parseFloat(tamanhoEl.selectedOptions[0].dataset.preco);
  const observacoes = observacoesEl.value.trim();

  const indexExistente = carrinho.findIndex(i => i.sabor === sabor && i.tamanho === tamanho);
  if (indexExistente !== -1) {
    carrinho[indexExistente].quantidade++;
    if (observacoes) {
      carrinho[indexExistente].observacoes += ` | ${observacoes}`;
    }
  } else {
    carrinho.push({ sabor, tamanho, preco, quantidade: 1, observacoes });
  }

  atualizarCarrinho();
});

// ==========================
// Alterar quantidade / remover
// ==========================
tabelaCorpo.addEventListener("input", e => {
  if (e.target.classList.contains("quantidade-input")) {
    const index = parseInt(e.target.dataset.index);
    let valor = parseInt(e.target.value);
    if (isNaN(valor) || valor < 1) valor = 1;
    carrinho[index].quantidade = valor;
    atualizarCarrinho();
  }
});

tabelaCorpo.addEventListener("click", e => {
  if (e.target.classList.contains("remover-btn")) {
    const index = parseInt(e.target.dataset.index);
    carrinho.splice(index, 1);
    atualizarCarrinho();
  }
});

// ==========================
// Modal Carrinho
// ==========================
btnAbrirModal.addEventListener("click", () => {
  modalCarrinho.style.display = "flex";
  modalCarrinho.setAttribute("aria-hidden", "false");
});

btnFecharModal.addEventListener("click", () => {
  modalCarrinho.style.display = "none";
  modalCarrinho.setAttribute("aria-hidden", "true");
});

// ==========================
// Pagamento (via formulário hidden)
// ==========================
btnPagamento.addEventListener("click", () => {
  if (carrinho.length === 0) {
    alert("O carrinho está vazio.");
    return;
  }

  // Criar um form escondido
  const form = document.createElement("form");
  form.method = "POST";
  form.action = "/pagamento";

  // Morada
  const inputMorada = document.createElement("input");
  inputMorada.type = "hidden";
  inputMorada.name = "morada";
  inputMorada.value = moradaEl.value;
  form.appendChild(inputMorada);

  // Carrinho (JSON)
  const inputCarrinho = document.createElement("input");
  inputCarrinho.type = "hidden";
  inputCarrinho.name = "carrinho";
  inputCarrinho.value = JSON.stringify(carrinho);
  form.appendChild(inputCarrinho);

  document.body.appendChild(form);
  form.submit();
});

// ==========================
// Histórico
// ==========================
btnHistorico.addEventListener("click", () => {
  window.location.href = "/historico";
});
