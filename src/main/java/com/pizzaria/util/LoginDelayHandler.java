package com.pizzaria.util;

import javax.swing.*;
import java.awt.event.ActionListener;

public class LoginDelayHandler {

    private final int delayMillis;

    // Construtor que define o tempo de atraso em milissegundos
    public LoginDelayHandler(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    // Executa uma ação após o tempo de atraso configurado
    public void executarComAtraso(ActionListener acao) {
        Timer timer = new Timer(delayMillis, acao);
        timer.setRepeats(false); // Executa apenas uma vez
        timer.start();
    }
}
