package com.pizzaria.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // URLs públicas
        String uri = request.getRequestURI();
        if (uri.startsWith("/login") || uri.equals("/") || uri.startsWith("/css") || uri.startsWith("/js")) {
            return true; // permite acesso
        }

        // Verifica se há usuário logado
        if (session != null && session.getAttribute("usuarioLogado") != null) {
            return true; // usuário logado, permite
        }

        // Caso não esteja logado, redireciona para login
        response.sendRedirect("/index");
        return false;
    }
}
