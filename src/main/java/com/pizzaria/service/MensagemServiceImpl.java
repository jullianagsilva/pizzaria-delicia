package com.pizzaria.service;

import com.pizzaria.util.MensagemService;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;

@Service
public class MensagemServiceImpl implements MensagemService {

    private JFrame parentFrame;

    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    @Override
    public void mostrarMensagemCliente(String mensagem) {
        mostrarDialogo("[CLIENTE] " + mensagem, "Mensagem do Cliente", null);
    }

    @Override
    public void mostrarMensagemCliente(String mensagem, Color cor) {
        mostrarDialogo(mensagem, "Mensagem do Cliente", cor);
    }

    @Override
    public void mostrarMensagemAdmin(String mensagem, Color cor) {
        mostrarDialogo(mensagem, "Mensagem do Administrador", cor);
    }

    private void mostrarDialogo(String mensagem, String titulo, Color cor) {
        final String msg = mensagem;
        final String tit = titulo;
        final Color c = cor;

        SwingUtilities.invokeLater(() -> {
            if (c == null) {
                JOptionPane.showMessageDialog(parentFrame, msg, tit, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JLabel label = new JLabel(msg, SwingConstants.CENTER);
                label.setForeground(c);
                JOptionPane.showMessageDialog(parentFrame, label, tit, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
