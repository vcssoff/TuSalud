package org.tusalud.ui;

import org.tusalud.model.Medicion;
import org.tusalud.model.Usuario;
import org.tusalud.repository.MedicionDAO;
import org.tusalud.service.PerfilMedicoService;

import javax.swing.*;
import java.awt.*;

public class EditarMedicionDialog extends JDialog {
    private final Medicion medicion;
    private final Usuario usuario;
    private final MedicionDAO medicionDAO = new MedicionDAO();
    private boolean actualizado = false;

    private JTextField txtPeso;
    private JTextField txtAltura;
    private JTextField txtFrecuencia;
    private JTextField txtGlucosa;

    public EditarMedicionDialog(Dialog parent, Medicion medicion, Usuario usuario) {
        super(parent, "Editar Medición #" + medicion.getIdMedicion(), true);
        this.medicion = medicion;
        this.usuario = usuario;
        initComponents();
    }

    public EditarMedicionDialog(Frame parent, Medicion medicion, Usuario usuario) {
        super(parent, "Editar Medición #" + medicion.getIdMedicion(), true);
        this.medicion = medicion;
        this.usuario = usuario;
        initComponents();
    }

    public EditarMedicionDialog(Dialog parent, Medicion medicion) {
        this(parent, medicion, null);
    }

    public EditarMedicionDialog(Frame parent, Medicion medicion) {
        this(parent, medicion, null);
    }

    private void initComponents() {
        setSize(480, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(15, 15));

        String perfil = usuario != null ? usuario.getPerfilMedico() : null;

        // Header Superior con información del perfil
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Modificar Registro Biométrico #" + medicion.getIdMedicion());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Perfil activo: " + PerfilMedicoService.getNombreLegible(perfil));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(236, 240, 241));

        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Verificación de permisos según cuestionario médico
        boolean puedePeso = PerfilMedicoService.permitePeso(perfil);
        boolean puedeAltura = PerfilMedicoService.permiteAltura(perfil);
        boolean puedeFC = PerfilMedicoService.permiteFrecuencia(perfil);
        boolean puedeGlucosa = PerfilMedicoService.permiteGlucosa(perfil);

        // Peso Corporal
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Peso Corporal (kg):" + (!puedePeso ? " [Bloqueado]" : "")), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPeso = new JTextField(medicion.getPeso() != null ? String.valueOf(medicion.getPeso()) : "");
        aplicarEstiloCampo(txtPeso, puedePeso, PerfilMedicoService.getMotivoBloqueo("peso", perfil));
        formPanel.add(txtPeso, gbc);

        // Altura
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Altura (m):" + (!puedeAltura ? " [Bloqueado]" : "")), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtAltura = new JTextField(medicion.getAltura() != null ? String.valueOf(medicion.getAltura()) : "");
        aplicarEstiloCampo(txtAltura, puedeAltura, PerfilMedicoService.getMotivoBloqueo("altura", perfil));
        formPanel.add(txtAltura, gbc);

        // Frecuencia Cardíaca
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("FC (bpm):" + (!puedeFC ? " [Bloqueado]" : "")), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFrecuencia = new JTextField(medicion.getFrecuenciaCardiaca() != null ? String.valueOf(medicion.getFrecuenciaCardiaca()) : "");
        aplicarEstiloCampo(txtFrecuencia, puedeFC, PerfilMedicoService.getMotivoBloqueo("fc", perfil));
        formPanel.add(txtFrecuencia, gbc);

        // Glucosa
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Glucosa (mg/dL):" + (!puedeGlucosa ? " [Bloqueado]" : "")), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtGlucosa = new JTextField(medicion.getGlucosaSangre() != null ? String.valueOf(medicion.getGlucosaSangre()) : "");
        aplicarEstiloCampo(txtGlucosa, puedeGlucosa, PerfilMedicoService.getMotivoBloqueo("glucosa", perfil));
        formPanel.add(txtGlucosa, gbc);

        // Nota aclaratoria
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JLabel lblAviso = new JLabel("<html><small><i>Nota: Solo puedes modificar variables que influyen en las opciones elegidas en tu cuestionario médico inicial.</i></small></html>");
        lblAviso.setForeground(new Color(127, 140, 141));
        formPanel.add(lblAviso, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Actualizar");

        btnGuardar.setBackground(new Color(41, 128, 185));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarCambios());

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void aplicarEstiloCampo(JTextField field, boolean habilitado, String motivoBloqueo) {
        if (!habilitado) {
            field.setEditable(false);
            field.setEnabled(false);
            field.setBackground(new Color(242, 244, 244));
            field.setForeground(new Color(127, 140, 141));
            field.setToolTipText("🔒 " + motivoBloqueo);
        } else {
            field.setEditable(true);
            field.setEnabled(true);
            field.setBackground(Color.WHITE);
            field.setForeground(Color.BLACK);
            field.setToolTipText("Modifica este dato para actualizar tu registro.");
        }
    }

    private void guardarCambios() {
        String perfil = usuario != null ? usuario.getPerfilMedico() : null;
        boolean puedePeso = PerfilMedicoService.permitePeso(perfil);
        boolean puedeAltura = PerfilMedicoService.permiteAltura(perfil);
        boolean puedeFC = PerfilMedicoService.permiteFrecuencia(perfil);
        boolean puedeGlucosa = PerfilMedicoService.permiteGlucosa(perfil);

        try {
            Double peso = puedePeso
                    ? (txtPeso.getText().trim().isEmpty() ? null : Double.parseDouble(txtPeso.getText().trim()))
                    : medicion.getPeso();

            Double altura = puedeAltura
                    ? (txtAltura.getText().trim().isEmpty() ? null : Double.parseDouble(txtAltura.getText().trim()))
                    : medicion.getAltura();

            Integer fc = puedeFC
                    ? (txtFrecuencia.getText().trim().isEmpty() ? null : Integer.parseInt(txtFrecuencia.getText().trim()))
                    : medicion.getFrecuenciaCardiaca();

            Double glucosa = puedeGlucosa
                    ? (txtGlucosa.getText().trim().isEmpty() ? null : Double.parseDouble(txtGlucosa.getText().trim()))
                    : medicion.getGlucosaSangre();

            if (peso == null && altura == null && fc == null && glucosa == null) {
                JOptionPane.showMessageDialog(this, "Debe existir al menos un dato biométrico válido.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (puedeAltura && altura != null) {
                if (altura > 3.0 && altura <= 300.0) {
                    altura = Math.round((altura / 100.0) * 100.0) / 100.0;
                }
                if (altura < 0.4 || altura > 2.8) {
                    JOptionPane.showMessageDialog(this, "La altura debe ser un valor válido en metros (ej: 1.75).", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (puedePeso && peso != null && (peso < 10.0 || peso > 500.0)) {
                JOptionPane.showMessageDialog(this, "El peso debe estar entre 10 kg y 500 kg.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (puedeFC && fc != null && (fc < 25 || fc > 260)) {
                JOptionPane.showMessageDialog(this, "La frecuencia cardíaca debe estar entre 25 y 260 bpm.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (puedeGlucosa && glucosa != null && (glucosa < 20.0 || glucosa > 1000.0)) {
                JOptionPane.showMessageDialog(this, "El nivel de glucosa debe estar entre 20 y 1000 mg/dL.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            medicion.setPeso(peso);
            medicion.setAltura(altura);
            medicion.setFrecuenciaCardiaca(fc);
            medicion.setGlucosaSangre(glucosa);

            if (medicionDAO.actualizar(medicion)) {
                JOptionPane.showMessageDialog(this, "Medición actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                this.actualizado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el registro.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa formatos numéricos válidos en los campos habilitados.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isActualizado() {
        return actualizado;
    }
}
