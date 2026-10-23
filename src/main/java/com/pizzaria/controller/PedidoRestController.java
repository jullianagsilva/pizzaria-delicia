package com.pizzaria.controller;

import com.pizzaria.model.Pedido;
import com.pizzaria.model.Clientes;
import com.pizzaria.service.PedidoService;
import com.pizzaria.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController // Indica que essa classe é um controlador REST (retorna JSON)
@RequestMapping("/api/pedidos") // URL base para todos os endpoints desse controller
public class PedidoRestController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;

    // Construtor com injeção de dependências
    public PedidoRestController(PedidoService pedidoService, ClienteService clienteService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
    }

    // ================== Criar pedido ==================
    @PostMapping("/novo") // Endpoint POST em /api/pedidos/novo
    public ResponseEntity<?> criarPedido(@RequestBody Pedido pedido, HttpSession session) {
        // Verifica se existe um usuário logado na sessão
        Object usuarioObj = session.getAttribute("usuarioLogado");
        if (usuarioObj == null) {
            // Se não estiver logado, retorna erro 401 (Unauthorized)
            return ResponseEntity.status(401).body("Usuário não logado");
        }

        // Converte o objeto da sessão para cliente
        Clientes cliente = (Clientes) usuarioObj;
        // Define o cliente como dono do pedido
        pedido.setCliente(cliente);

        try {
            // Salva o pedido usando o serviço
            pedidoService.salvarPedido(pedido);
            return ResponseEntity.ok(pedido); // Retorna 200 com o pedido salvo
        } catch (Exception e) {
            // Se der erro ao salvar, retorna 500 (Internal Server Error)
            return ResponseEntity.status(500).body("Erro ao salvar pedido: " + e.getMessage());
        }
    }

    // ================== Listar pedidos do cliente logado ==================
    @GetMapping("/meus") // Endpoint GET em /api/pedidos/meus
    public ResponseEntity<?> listarPedidosDoCliente(HttpSession session) {
        // Verifica se existe usuário logado
        Object usuarioObj = session.getAttribute("usuarioLogado");
        if (usuarioObj == null) {
            return ResponseEntity.status(401).body("Usuário não logado");
        }

        // Pega o cliente logado
        Clientes cliente = (Clientes) usuarioObj;
        // Busca pedidos do cliente pelo ID
        List<Pedido> pedidos = pedidoService.getPedidosPorCliente(cliente.getId());
        return ResponseEntity.ok(pedidos);
    }

    // ================== Listar todos pedidos (Admin) ==================
    @GetMapping("/todos") // Endpoint GET em /api/pedidos/todos
    public ResponseEntity<List<Pedido>> listarTodosPedidos(HttpSession session) {
        // Verifica o tipo de usuário na sessão
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ADMIN".equals(tipo)) {
            // Se não for admin → 403 Forbidden
            return ResponseEntity.status(403).build();
        }
        // Admin pode ver todos os pedidos
        return ResponseEntity.ok(pedidoService.buscarTodos());
    }

    // ================== Obter pedido por número ==================
    @GetMapping("/{numeroPedido}") // Exemplo: /api/pedidos/123
    public ResponseEntity<?> obterPedido(@PathVariable int numeroPedido, HttpSession session) {
        // Verifica login
        Object usuarioObj = session.getAttribute("usuarioLogado");
        if (usuarioObj == null) {
            return ResponseEntity.status(401).body("Usuário não logado");
        }

        // Busca pedido no banco
        Pedido pedido = pedidoService.buscarPedidoPorNumero(numeroPedido);
        if (pedido == null) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }

        // Se for cliente, só pode acessar os próprios pedidos
        if ("CLIENTE".equals(session.getAttribute("tipoUsuario"))) {
            Clientes cliente = (Clientes) usuarioObj;
            if (pedido.getCliente().getId() != cliente.getId()) {
                return ResponseEntity.status(403).body("Acesso negado");
            }
        }

        // Retorna o pedido
        return ResponseEntity.ok(pedido);
    }

    // ================== Obter usuário logado ==================
    @GetMapping("/usuario-logado") // /api/pedidos/usuario-logado
    public ResponseEntity<?> getUsuarioLogado(HttpSession session) {
        Object usuarioObj = session.getAttribute("usuarioLogado");
        String tipo = (String) session.getAttribute("tipoUsuario");

        if (usuarioObj == null || tipo == null) {
            return ResponseEntity.status(401).body("Nenhum usuário logado");
        }

        // Se for cliente, retorna informações básicas
        if ("CLIENTE".equals(tipo)) {
            Clientes cliente = (Clientes) usuarioObj;
            return ResponseEntity.ok(new Object() {
                public final String nome = cliente.getNome();
                public final String tipoUsuario = "CLIENTE";
                public final int id = cliente.getId();
            });
        }

        // Se for admin, retorna informações fixas
        if ("ADMIN".equals(tipo)) {
            return ResponseEntity.ok(new Object() {
                public final String nome = "Administrador";
                public final String tipoUsuario = "ADMIN";
            });
        }

        return ResponseEntity.status(401).body("Usuário inválido");
    }

    // ================== Histórico de pedidos ==================
    @GetMapping("/historico") // /api/pedidos/historico
    public ResponseEntity<?> historicoPedidos(HttpSession session) {
        Object usuarioObj = session.getAttribute("usuarioLogado");
        String tipo = (String) session.getAttribute("tipoUsuario");

        if (usuarioObj == null || tipo == null) {
            return ResponseEntity.status(401).body("Usuário não logado");
        }

        // Cliente → apenas pedidos dele
        if ("CLIENTE".equals(tipo)) {
            Clientes cliente = (Clientes) usuarioObj;
            return ResponseEntity.ok(pedidoService.getPedidosPorCliente(cliente.getId()));
        }

        // Admin → todos os pedidos
        if ("ADMIN".equals(tipo)) {
            return ResponseEntity.ok(pedidoService.buscarTodos());
        }

        return ResponseEntity.status(403).body("Tipo de usuário inválido");
    }
}
