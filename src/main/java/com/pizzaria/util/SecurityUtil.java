package com.pizzaria.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtil {

    public static String gerarSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
