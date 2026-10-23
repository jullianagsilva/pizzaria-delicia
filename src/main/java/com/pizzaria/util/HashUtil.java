package com.pizzaria.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class HashUtil {

    public static String gerarHashSHA256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", e);
        }
    }
}
