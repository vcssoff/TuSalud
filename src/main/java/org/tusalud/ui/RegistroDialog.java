package org.tusalud.ui;

import org.tusalud.model.Usuario;
import org.tusalud.repository.UsuarioDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class RegistroDialog extends JDialog {
    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JTextField txtFechaNacimiento;
    private JComboBox<String> cbGenero;
    private boolean registrado = false;
    private String correoRegistrado = "";
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public RegistroDialog(Frame parent) {
        super(parent, "TuSalud - Crear Cuenta", true);
        initComponents();
    }

    public RegistroDialog(Dialog parent) {
        super(parent, "TuSalud - Crear Cuenta", true);
        initComponents();
    }

    private void initComponents() {
        setSize(440, 360);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(15, 15));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 174, 96));
        JLabel title = new JLabel("Registro de Usuario");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nombre completo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombre = new JTextField();
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Correo electrónico:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtCorreo = new JTextField();
        formPanel.add(txtCorreo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPassword = new JPasswordField();
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Fecha Nac. (AAAA-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFechaNacimiento = new JTextField("2000-01-01");
        formPanel.add(txtFechaNacimiento, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(new JLabel("Género:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbGenero = new JComboBox<>(new String[]{"Masculino", "Femenino", "Otro"});
        formPanel.add(cbGenero, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Registrarse");

        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarUsuario());

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void guardarUsuario() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String fechaStr = txtFechaNacimiento.getText().trim();
        String genero = (String) cbGenero.getSelectedItem();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || fechaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos requeridos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date fechaNac;
        try {
            fechaNac = Date.valueOf(fechaStr);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Utiliza el formato AAAA-MM-DD (ej: 1995-08-20).", "Fecha inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (usuarioDAO.existeCorreo(correo)) {
                JOptionPane.showMessageDialog(this, "El correo electrónico ya está registrado.", "Correo duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setFechaNacimiento(fechaNac);
            u.setGenero(genero);

            if (usuarioDAO.registrar(u, pass)) {
                JOptionPane.showMessageDialog(this, "¡Cuenta creada exitosamente! Ahora puedes iniciar sesión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                this.registrado = true;
                this.correoRegistrado = correo;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistrado() { return registrado; }
    public String getCorreoRegistrado() { return correoRegistrado; }
}
