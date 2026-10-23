package com.pizzaria.util;

import java.awt.Color;

public interface MensagemService {
    void mostrarMensagemCliente(String mensagem); // existente
    void mostrarMensagemCliente(String mensagem, Color cor); // novo
    void mostrarMensagemAdmin(String mensagem, Color cor);
}

