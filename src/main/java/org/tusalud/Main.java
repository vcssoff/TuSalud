package org.tusalud;

import com.formdev.flatlaf.FlatLightLaf;
import org.tusalud.model.Usuario;
import org.tusalud.ui.LoginDialog;
import org.tusalud.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Look & Feel moderno y plano
        try {
            FlatLightLaf.setup();
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        // Auto-creación de tablas si no existen en la BD
        org.tusalud.config.DatabaseInitializer.inicializarTablas();

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog();
            login.setVisible(true);
            login.toFront();
            login.requestFocus();
        });
    }
}
