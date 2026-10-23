package com.pizzaria.controller;

import com.pizzaria.dto.ClienteFrequenteDTO;
import com.pizzaria.dto.FaturacaoDiariaDTO;
import com.pizzaria.dto.PizzaMaisVendidaDTO;
import com.pizzaria.model.*;
import com.pizzaria.service.LoginService;
import com.pizzaria.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final LoginService loginService;
    private final PedidoService pedidoService;

    // Construtor: injeta os serviços necessários
    public WebController(LoginService loginService, PedidoService pedidoService) {
        this.loginService = loginService;
        this.pedidoService = pedidoService;
    }

    // ================== HOME ==================
    @GetMapping("/")
    public String index() {
        return "index"; // Página inicial
    }

    // ================== LOGIN CLIENTE ==================
    @GetMapping("/logincliente")
    public String mostrarLoginCliente() {
        return "logincliente"; // Exibe formulário de login do cliente
    }

    @PostMapping("/logincliente")
    public String processarLoginCliente(@RequestParam String nome,
                                        @RequestParam String telefone,
                                        HttpSession session,
                                        Model model) {
        // Remove espaços extras
        nome = (nome != null) ? nome.trim() : "";
        telefone = (telefone != null) ? telefone.trim() : "";

        // Validação
        if (nome.isEmpty() || telefone.isEmpty()) {
            model.addAttribute("erro", "Nome e telefone são obrigatórios!");
            return "logincliente";
        }

        // Autentica ou cadastra cliente
        Optional<Clientes> clienteOpt = loginService.autenticarOuCadastrarCliente(nome, telefone);

        if (clienteOpt.isPresent()) {
            Clientes cliente = clienteOpt.get();
            session.setAttribute("usuarioLogado", cliente);
            session.setAttribute("tipoUsuario", "CLIENTE");
            return "redirect:/pedido"; // Redireciona para a tela de pedido
        } else {
            model.addAttribute("erro", "Não foi possível cadastrar/entrar. Verifique nome e telefone.");
            return "logincliente";
        }
    }

    // ================== LOGIN ADMIN ==================
    @GetMapping("/loginadmin")
    public String mostrarLoginAdmin() {
        return "loginadmin"; // Exibe formulário de login do admin
    }

    @PostMapping("/loginadmin")
    public String processarLoginAdmin(@RequestParam String usuario,
                                      @RequestParam String senha,
                                      HttpSession session,
                                      Model model) {
        // Autentica administrador
        Optional<Administradores> adminOpt = loginService.autenticarAdmin(usuario, senha);

        if (adminOpt.isPresent()) {
            Administradores admin = adminOpt.get();
            session.setAttribute("usuarioLogado", admin);
            session.setAttribute("tipoUsuario", "ADMIN");
            return "redirect:/admin"; // Redireciona para painel administrativo
        } else {
            model.addAttribute("erro", "Usuário ou senha inválidos!");
            return "loginadmin";
        }
    }

    // ================== PAINEL ADMIN ==================
    @GetMapping("/admin")
    public String painelAdmin(HttpSession session) {
        if (!usuarioEhAdmin(session)) return "redirect:/loginadmin";
        return "admin"; // Página do painel administrativo
    }

    // ================== RELATÓRIOS ==================
    @GetMapping("/relatorios")
    public String mostrarRelatorios(HttpSession session, Model model) {
        if (!usuarioEhAdmin(session)) return "redirect:/loginadmin";

        // Busca dados de relatórios
        List<ClienteFrequenteDTO> clientesMaisFrequentes = pedidoService.buscarClientesMaisFrequentes();
        List<PizzaMaisVendidaDTO> pizzasMaisVendidas = pedidoService.buscarPizzasMaisVendidas();
        List<FaturacaoDiariaDTO> faturacaoPorDia = pedidoService.buscarFaturacaoUltimos30Dias();

        // Envia para a view
        model.addAttribute("clientesMaisFrequentes", clientesMaisFrequentes);
        model.addAttribute("pizzasMaisVendidas", pizzasMaisVendidas);
        model.addAttribute("faturacaoPorDia", faturacaoPorDia);

        return "relatorios";
    }

    // ================== PÁGINA DE PEDIDOS ==================
    @GetMapping("/pedido")
    public String paginaPedidos(HttpSession session, Model model) {
        if (!usuarioEhCliente(session)) return "redirect:/logincliente";

        // Pega cliente logado
        Clientes cliente = (Clientes) session.getAttribute("usuarioLogado");
        model.addAttribute("cliente", cliente);

        // Recupera ou cria carrinho
        Carrinho carrinho = (Carrinho) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new Carrinho();
            session.setAttribute("carrinho", carrinho);
        }
        model.addAttribute("carrinho", carrinho);

        // Calcula total com taxa de entrega
        double total = carrinho.getPizzas().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                .sum() + 3.50;

        model.addAttribute("total", total);
        model.addAttribute("taxaEntrega", 3.50);

        return "pedido";
    }

    // ================== ADICIONAR PIZZA / IR PARA PAGAMENTO ==================
    @PostMapping("/pedido")
    public String processarPedido(HttpSession session,
                                  @RequestParam String sabor,
                                  @RequestParam String tamanho,
                                  @RequestParam int quantidade,
                                  @RequestParam(required = false) String observacoes,
                                  @RequestParam String morada,
                                  @RequestParam String acao,
                                  Model model) {
        if (!usuarioEhCliente(session)) return "redirect:/logincliente";

        Clientes cliente = (Clientes) session.getAttribute("usuarioLogado");

        // Recupera ou cria carrinho
        Carrinho carrinho = (Carrinho) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new Carrinho();
            session.setAttribute("carrinho", carrinho);
        }

        // Se ação for "adicionar", adiciona pizza no carrinho
        if ("adicionar".equals(acao)) {
            Pizza pizza = new Pizza(sabor, tamanho, quantidade);
            pizza.setPreco(calcularPreco(tamanho)); // Define preço pela lógica de tamanho
            carrinho.adicionarPizza(pizza);
            session.setAttribute("carrinho", carrinho);

            // Recalcula valores para mostrar na tela
            model.addAttribute("carrinho", carrinho);
            model.addAttribute("cliente", cliente);

            double total = carrinho.getPizzas().stream()
                    .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                    .sum() + 3.50;
            model.addAttribute("total", total);
            model.addAttribute("taxaEntrega", 3.50);

            return "pedido"; // Volta para a tela de pedidos
        }

        // Se não for adicionar, considera que é para finalizar
        Pedido pedido = new Pedido(cliente, carrinho, observacoes != null ? observacoes : "");
        pedido.setMoradaEntrega(morada);
        session.setAttribute("pedidoAtual", pedido);

        return "redirect:/pagamento";
    }

    // ================== PÁGINA DE PAGAMENTO ==================
    @GetMapping("/pagamento")
    public String paginaPagamento(HttpSession session, Model model) {
        if (!usuarioEhCliente(session)) return "redirect:/logincliente";

        Pedido pedido = (Pedido) session.getAttribute("pedidoAtual");
        if (pedido == null) return "redirect:/pedido";

        // Envia dados do pedido para a tela
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", pedido.getCliente());
        model.addAttribute("taxaEntrega", 3.50);

        double total = pedido.getPizzas().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                .sum() + 3.50;
        model.addAttribute("total", total);

        return "pagamento";
    }

    @PostMapping("/processar-pagamento")
    public String processarPagamento(HttpSession session,
                                     @RequestParam String formaPagamento) {
        Pedido pedido = (Pedido) session.getAttribute("pedidoAtual");
        if (pedido == null) return "redirect:/pedido";

        // Cria objeto de pagamento
        Pagamento pagamento = new Pagamento();
        pagamento.setMetodo(formaPagamento);
        pagamento.setValor(pedido.getTotal());
        pagamento.setStatus("Pago");
        pedido.setPagamento(pagamento);

        // Salva no banco
        pedidoService.salvarPedido(pedido);

        // Limpa carrinho e pedido atual
        session.setAttribute("carrinho", new Carrinho());
        session.removeAttribute("pedidoAtual");

        // Armazena pedido finalizado para exibir na tela final
        session.setAttribute("pedidoFinalizado", pedido);

        return "redirect:/pedido-finalizado";
    }

    // ================== PEDIDO FINALIZADO ==================
    @GetMapping("/pedido-finalizado")
    public String pedidoFinalizado(HttpSession session, Model model) {
        Pedido pedido = (Pedido) session.getAttribute("pedidoFinalizado");
        if (pedido == null) return "redirect:/pedido";

        // Prepara dados para exibir o resumo do pedido
        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", session.getAttribute("usuarioLogado"));
        model.addAttribute("taxaEntrega", 3.50);

        double totalItens = pedido.getPizzas().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                .sum();
        model.addAttribute("totalItens", totalItens);
        model.addAttribute("totalPedido", totalItens + 3.50);

        return "pedido-finalizado";
    }

    // ================== EXPORTAR PEDIDO ==================
    @GetMapping("/exportar-pedido")
    public ResponseEntity<Resource> exportarPedido(HttpSession session) {
        Pedido pedido = (Pedido) session.getAttribute("pedidoFinalizado");
        if (pedido == null) return ResponseEntity.notFound().build();

        // Gera conteúdo do arquivo
        String conteudo = ExportadorPedido.gerarConteudo(pedido);
        byte[] bytes = conteudo.getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        // Define nome do arquivo com cliente + data/hora
        String nomeCliente = pedido.getCliente().getNome().trim().replaceAll("[^a-zA-Z0-9]", "_");
        String dataHora = pedido.getDataPedido().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivo = String.format("pedido_%s_%d_%s.txt", nomeCliente, pedido.getNumeroPedido(), dataHora);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo)
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length)
                .body(resource);
    }

    // ================== REMOVER PIZZA DO CARRINHO ==================
    @PostMapping("/remover-pizza")
    public String removerPizza(HttpSession session, @RequestParam int index, Model model) {
        if (!usuarioEhCliente(session)) return "redirect:/logincliente";

        Carrinho carrinho = (Carrinho) session.getAttribute("carrinho");
        if (carrinho != null && index >= 0 && index < carrinho.getPizzas().size()) {
            carrinho.getPizzas().remove(index);
        }

        session.setAttribute("carrinho", carrinho);

        // Atualiza dados da tela
        Clientes cliente = (Clientes) session.getAttribute("usuarioLogado");
        model.addAttribute("cliente", cliente);
        model.addAttribute("carrinho", carrinho);

        double total = carrinho.getPizzas().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                .sum() + 3.50;
        model.addAttribute("total", total);
        model.addAttribute("taxaEntrega", 3.50);

        return "pedido";
    }

    // ================== HISTÓRICO DE PEDIDOS ==================
    @GetMapping("/historico")
    public String mostrarHistorico(HttpSession session,
                                   Model model,
                                   @RequestParam(defaultValue = "1") int pagina,
                                   @RequestParam(defaultValue = "5") int tamanho) {

        Object usuario = session.getAttribute("usuarioLogado");
        String tipoUsuario = (String) session.getAttribute("tipoUsuario");

        if (usuario == null || tipoUsuario == null) {
            return "redirect:/logincliente";
        }

        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("tipoUsuario", tipoUsuario);

        // Busca pedidos (todos se admin, apenas do cliente se cliente comum)
        List<Pedido> todosPedidos;
        if ("ADMIN".equals(tipoUsuario)) {
            todosPedidos = pedidoService.buscarTodos();
        } else {
            todosPedidos = pedidoService.getPedidosPorCliente(((Clientes) usuario).getId());
        }

        // Garante que lista não terá null
        List<Pedido> pedidosValidos = todosPedidos.stream()
                .filter(Objects::nonNull)
                .peek(p -> {
                    if (p.getPizzas() == null) p.setPizzas(new ArrayList<>());
                })
                .collect(Collectors.toList());

        // Paginação
        int totalPedidos = pedidosValidos.size();
        int totalPaginas = (int) Math.ceil((double) totalPedidos / tamanho);
        pagina = Math.max(1, Math.min(pagina, totalPaginas));

        int inicio = (pagina - 1) * tamanho;
        int fim = Math.min(inicio + tamanho, totalPedidos);

        List<Pedido> historicoPedidos = pedidosValidos.subList(inicio, fim);

        model.addAttribute("historicoPedidos", historicoPedidos);
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", totalPaginas);

        return "historico";
    }

    // ================== DETALHES DE UM PEDIDO ==================
    @GetMapping("/pedido/detalhes/{id}")
    public String detalhesPedido(@PathVariable int id, HttpSession session, Model model) {
        Object usuario = session.getAttribute("usuarioLogado");
        String tipoUsuario = (String) session.getAttribute("tipoUsuario");

        if (usuario == null || tipoUsuario == null) {
            return "redirect:/logincliente";
        }

        Pedido pedido = pedidoService.buscarPorId(id);
        if (pedido == null) {
            model.addAttribute("erro", "Pedido não encontrado.");
            return "redirect:/historico";
        }

        // Se cliente tentar acessar pedido que não é dele → bloqueia
        if ("CLIENTE".equals(tipoUsuario)) {
            Clientes cliente = (Clientes) usuario;
            if (pedido.getCliente() == null || pedido.getCliente().getId() != cliente.getId()) {
                model.addAttribute("erro", "Você não tem permissão para ver este pedido.");
                return "redirect:/historico";
            }
        }

        // Evita null em pizzas
        if (pedido.getPizzas() == null) {
            pedido.setPizzas(new ArrayList<>());
        }

        // Envia dados para view
        model.addAttribute("pedido", pedido);
        model.addAttribute("tipoUsuario", tipoUsuario);
        model.addAttribute("taxaEntrega", 3.50);

        double totalItens = pedido.getPizzas().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                .sum();
        model.addAttribute("totalItens", totalItens);
        model.addAttribute("totalPedido", totalItens + 3.50);

        return "pedido-detalhes";
    }

    // ================== LOGOUT ==================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Encerra sessão
        return "redirect:/";
    }

    // ================== MÉTODOS AUXILIARES ==================
    private boolean usuarioEhCliente(HttpSession session) {
        return session.getAttribute("usuarioLogado") != null &&
                "CLIENTE".equals(session.getAttribute("tipoUsuario"));
    }

    private boolean usuarioEhAdmin(HttpSession session) {
        return session.getAttribute("usuarioLogado") != null &&
                "ADMIN".equals(session.getAttribute("tipoUsuario"));
    }

    // Define preços fixos baseados no tamanho da pizza
    private double calcularPreco(String tamanho) {
        return switch (tamanho) {
            case "Pequena" -> 15.0;
            case "Média" -> 20.0;
            case "Grande" -> 25.0;
            default -> 0.0;
        };
    }
}
