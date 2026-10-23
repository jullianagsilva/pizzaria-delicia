
package com.pizzaria;

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
                "Configuração da base de dados em falta. " +
                "Verifica as variáveis DB_URL, DB_USERNAME e DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        try (Connection conn = ligar()) {
            System.out.println(
                "Ligação estabelecida com sucesso ao MySQL!"
            );
        } catch (SQLException | IllegalStateException e) {
            System.err.println("Não foi possível ligar ao MySQL: "
                    + e.getMessage());
        }
    }
}
