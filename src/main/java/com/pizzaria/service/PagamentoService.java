package com.pizzaria.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class PagamentoService {

    /**
     * Simula o envio de um pagamento para uma API externa.
     * Este método é opcional e pode ser adaptado para uso real.
     */
    public String processarPagamento(double valor, String metodo) {
        try {
            URI uri = URI.create("https://sandbox.exemplo.com/api/pagamento"); // URL fictícia
            URL url = uri.toURL();
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setDoOutput(true);

            String payload = String.format("{\"valor\": %.2f, \"metodo\": \"%s\"}", valor, metodo);

            try (OutputStream os = conexao.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            return response.toString(); // Simulado: normalmente se faria parsing do JSON
        } catch (Exception e) {
            return "erro: " + e.getMessage();
        }
    }

    /**
     * Simula localmente o resultado de um pagamento.
     * Retorna "sucesso" se valor > 0, senão "falha".
     */
    public String simularPagamento(double valor) {
        return valor > 0 ? "sucesso" : "falha";
    }
}
