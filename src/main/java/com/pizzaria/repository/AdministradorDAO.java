package com.pizzaria.repository;

import com.pizzaria.model.Administradores;
import com.pizzaria.config.ConexaoBD;
import com.pizzaria.util.HashUtil;
import com.pizzaria.util.SecurityUtil;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class AdministradorDAO {

    // Busca administrador pelo nome de usuário
    public Optional<Administradores> buscarPorUsuario(String usuario) {
        String sql = "SELECT * FROM administradores WHERE usuario = ?";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Administradores admin = mapearAdministrador(rs);
                return Optional.of(admin);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar administrador por usuário: " + e.getMessage());
        }

        return Optional.empty();
    }

    // Busca administrador por ID
    public Optional<Administradores> buscarPorId(Long id) {
        String sql = "SELECT * FROM administradores WHERE id = ?";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Administradores admin = mapearAdministrador(rs);
                return Optional.of(admin);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar administrador por ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    // Mapeia o ResultSet para Administrador
    private Administradores mapearAdministrador(ResultSet rs) throws SQLException {
        Administradores admin = new Administradores();
        admin.setId(rs.getLong("id")); // ✅ getLong compatível com Long
        admin.setUsuario(rs.getString("usuario"));
        admin.setSalt(rs.getString("salt"));
        admin.setSenhaHash(rs.getString("senha_hash"));
        return admin;
    }

    // Atualiza senha do administrador com nova senha EM TEXTO PURO (gera hash e salt internamente)
    public boolean atualizarSenhaComTextoPuro(Long adminId, String novaSenhaTextoPuro) {
        String novoSalt = SecurityUtil.gerarSalt();
        String novaSenhaHash = HashUtil.gerarHashSHA256(novoSalt + novaSenhaTextoPuro);
        return atualizarSenhaHashESalt(adminId, novaSenhaHash, novoSalt);
    }

    // Atualiza senha do administrador recebendo hash e salt PRONTOS
    public boolean atualizarSenhaHashESalt(Long adminId, String novoHash, String novoSalt) {
        String sql = "UPDATE administradores SET salt = ?, senha_hash = ? WHERE id = ?";

        try (Connection conn = ConexaoBD.ligar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoSalt);
            stmt.setString(2, novoHash);
            stmt.setLong(3, adminId);

            int linhasAtualizadas = stmt.executeUpdate();
            return linhasAtualizadas > 0;

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar senha do admin (hash/salt): " + e.getMessage());
            return false;
        }
    }
}
