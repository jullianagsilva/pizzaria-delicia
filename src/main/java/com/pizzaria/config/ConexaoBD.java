
package com.pizzaria.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private static final String url = System.getenv("DB_URL");
    private static final String user = System.getenv("DB_USERNAME");
    private static final String password = System.getenv("DB_PASSWORD");

    public static Connection ligar() throws SQLException {
        if (url == null || url.isBlank()
                || user == null || user.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                "Faltam variáveis de ambiente da base de dados. " +
                "Verifica DB_URL, DB_USERNAME e DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(url, user, password);
    }

    // Teste manual da conexão
    public static void main(String[] args) {
        try (Connection conn = ligar()) {
            System.out.println(
                "Conexão com o banco de dados estabelecida com sucesso!"
            );
        } catch (SQLException | IllegalStateException e) {
            System.err.println(
                "Erro ao conectar: " + e.getMessage()
            );
        }
    }
}
