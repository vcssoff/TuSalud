package org.tusalud.ui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.tusalud.model.ComparacionBiometrica;
import org.tusalud.model.Medicion;
import org.tusalud.model.Recomendacion;
import org.tusalud.model.Usuario;
import org.tusalud.repository.MedicionDAO;
import org.tusalud.repository.RecomendacionDAO;
import org.tusalud.service.AnalisisService;
import org.tusalud.service.PerfilMedicoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final Usuario usuario;
    private final MedicionDAO medicionDAO = new MedicionDAO();
    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();
    private final AnalisisService analisisService = new AnalisisService();

    private List<Medicion> medicionesCargadas = new ArrayList<>();

    // Componentes de Nueva Medición
    private JLabel lblBannerPerfil;
    private JLabel lblPeso;
    private JLabel lblAltura;
    private JLabel lblFrecuencia;
    private JLabel lblGlucosa;
    private JTextField txtPeso;
    private JTextField txtAltura;
    private JTextField txtFrecuencia;
    private JTextField txtGlucosa;

    // Componentes de Historial
    private JTable tblHistorial;
    private DefaultTableModel tableModel;

    // Componentes de Estadísticas
    private JLabel lblPromedioCardiaco;
    private JLabel lblPromedioGlucosa;
    private JLabel lblIMC;
    private JPanel panelGrafico;
    private String filtroGraficoActual = "TODAS";

    // Componentes de Comparación
    private JComboBox<String> cbMedicion1;
    private JComboBox<String> cbMedicion2;
    private JTextArea txtResultadoComparacion;

    // Componentes de Recomendaciones
    private JTextArea txtRecomendaciones;
    private List<Recomendacion> recomendacionesActuales = new ArrayList<>();

    private JLabel userLabel;

    public MainFrame(Usuario usuario) {
        this.usuario = usuario;
        setTitle("TuSalud - Monitoreo Biométrico | Usuario: " + usuario.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null);
        initComponents();
        aplicarRestriccionesPerfil();

        // Si el usuario no tiene cuestionario completado, abrir diálogo de onboarding
        if (usuario.getPerfilMedico() == null || usuario.getPerfilMedico().trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                CuestionarioDialog dialog = new CuestionarioDialog(this, usuario);
                dialog.setVisible(true);
                if (dialog.isCompletado()) {
                    actualizarEtiquetaUsuario();
                    recargarDatos();
                }
            });
        }

        recargarDatos();
    }

    private void actualizarEtiquetaUsuario() {
        String perfilStr = (usuario.getPerfilMedico() != null && !usuario.getPerfilMedico().isEmpty()) 
                ? " | Perfil: " + PerfilMedicoService.getNombreLegible(usuario.getPerfilMedico()) 
                : "";
        if (userLabel != null) {
            userLabel.setText("Sesión activa: " + usuario.getNombre() + " (" + usuario.getCorreo() + ")" + perfilStr);
        }
        aplicarRestriccionesPerfil();
    }

    private void aplicarRestriccionesPerfil() {
        String perfil = usuario.getPerfilMedico();
        boolean puedePeso = PerfilMedicoService.permitePeso(perfil);
        boolean puedeAltura = PerfilMedicoService.permiteAltura(perfil);
        boolean puedeFC = PerfilMedicoService.permiteFrecuencia(perfil);
        boolean puedeGlucosa = PerfilMedicoService.permiteGlucosa(perfil);

        if (lblBannerPerfil != null) {
            StringBuilder sb = new StringBuilder("<html><div style='padding:8px 12px; background-color:#ebf5fb; border:1px solid #aed6f1; border-radius:5px;'>");
            sb.append("<b style='color:#1b4f72;'>📋 Perfil Médico: ").append(PerfilMedicoService.getNombreLegible(perfil)).append("</b><br>");
            sb.append("<small><span style='color:#1e8449;'><b>✔ Habilitados para ingresar:</b> ");
            List<String> permitidas = PerfilMedicoService.getMetricasPermitidas(perfil);
            sb.append(String.join(", ", permitidas)).append("</span>");
            List<String> bloqueadas = PerfilMedicoService.getMetricasBloqueadas(perfil);
            if (!bloqueadas.isEmpty()) {
                sb.append("<br><span style='color:#922b21;'><b>🔒 Bloqueados (no influyen en tus opciones):</b> ");
                sb.append(String.join(", ", bloqueadas)).append("</span>");
            }
            sb.append("</small></div></html>");
            lblBannerPerfil.setText(sb.toString());
        }

        configurarCampoBiometrico(txtPeso, lblPeso, "Peso Corporal (kg)", puedePeso, PerfilMedicoService.getMotivoBloqueo("peso", perfil));
        configurarCampoBiometrico(txtAltura, lblAltura, "Altura (metros, ej. 1.75)", puedeAltura, PerfilMedicoService.getMotivoBloqueo("altura", perfil));
        configurarCampoBiometrico(txtFrecuencia, lblFrecuencia, "Frecuencia Cardíaca (bpm)", puedeFC, PerfilMedicoService.getMotivoBloqueo("fc", perfil));
        configurarCampoBiometrico(txtGlucosa, lblGlucosa, "Glucosa en Sangre (mg/dL)", puedeGlucosa, PerfilMedicoService.getMotivoBloqueo("glucosa", perfil));
    }

    private void configurarCampoBiometrico(JTextField field, JLabel label, String tituloBase, boolean habilitado, String motivoBloqueo) {
        if (field == null) return;
        if (label != null) {
            label.setText(tituloBase + (habilitado ? ":" : " [Bloqueado]:"));
            label.setForeground(habilitado ? new Color(44, 62, 80) : new Color(127, 140, 141));
        }
        if (!habilitado) {
            field.setText("");
            field.setEditable(false);
            field.setEnabled(false);
            field.setBackground(new Color(242, 244, 244));
            field.setToolTipText("🔒 " + motivoBloqueo);
        } else {
            field.setEditable(true);
            field.setEnabled(true);
            field.setBackground(Color.WHITE);
            field.setToolTipText("Ingresa valor para " + tituloBase);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Superior
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("TuSalud — Panel de Control Biométrico");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        String perfilStr = (usuario.getPerfilMedico() != null && !usuario.getPerfilMedico().isEmpty()) 
                ? " | Perfil: " + PerfilMedicoService.getNombreLegible(usuario.getPerfilMedico()) 
                : "";
        userLabel = new JLabel("Sesión activa: " + usuario.getNombre() + " (" + usuario.getCorreo() + ")" + perfilStr);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLabel.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Pestañas principales
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 14));

        tabbedPane.addTab("➕ Nueva Medición", crearPanelNuevaMedicion());
        tabbedPane.addTab("📋 Historial", crearPanelHistorial());
        tabbedPane.addTab("📊 Estadísticas & Gráficos", crearPanelEstadisticas());
        tabbedPane.addTab("⚖️ Comparación de Fechas", crearPanelComparacion());
        tabbedPane.addTab("💡 Recomendaciones", crearPanelRecomendaciones());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelNuevaMedicion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        JLabel desc = new JLabel("<html><b>Ingreso de Datos Biométricos</b><br><small>Ingresa tus mediciones personalizadas según tu perfil:</small></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(desc, gbc);

        lblBannerPerfil = new JLabel();
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblBannerPerfil, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        lblPeso = new JLabel("Peso Corporal (kg):");
        panel.add(lblPeso, gbc);
        gbc.gridx = 1;
        txtPeso = new JTextField(15);
        panel.add(txtPeso, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        lblAltura = new JLabel("Altura (metros, ej. 1.75):");
        panel.add(lblAltura, gbc);
        gbc.gridx = 1;
        txtAltura = new JTextField(15);
        panel.add(txtAltura, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        lblFrecuencia = new JLabel("Frecuencia Cardíaca (bpm):");
        panel.add(lblFrecuencia, gbc);
        gbc.gridx = 1;
        txtFrecuencia = new JTextField(15);
        panel.add(txtFrecuencia, gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        lblGlucosa = new JLabel("Glucosa en Sangre (mg/dL):");
        panel.add(lblGlucosa, gbc);
        gbc.gridx = 1;
        txtGlucosa = new JTextField(15);
        panel.add(txtGlucosa, gbc);

        JButton btnGuardar = new JButton("Guardar Medición");
        btnGuardar.setBackground(new Color(41, 128, 185));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnGuardar.addActionListener(e -> guardarMedicion());

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnGuardar, gbc);

        return panel;
    }

    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columnas = {"ID", "Fecha y Hora", "Peso (kg)", "Altura (m)", "FC (bpm)", "Glucosa (mg/dL)"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblHistorial = new JTable(tableModel);
        tblHistorial.setRowHeight(24);
        tblHistorial.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        panel.add(new JScrollPane(tblHistorial), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnRefrescar = new JButton("Actualizar");
        JButton btnEditar = new JButton("Editar Seleccionada");
        JButton btnEliminar = new JButton("Eliminar Seleccionada");

        btnRefrescar.addActionListener(e -> recargarDatos());
        btnEditar.addActionListener(e -> editarSeleccionada());
        btnEliminar.addActionListener(e -> eliminarSeleccionada());

        btnPanel.add(btnRefrescar);
        btnPanel.add(btnEditar);
        btnPanel.add(btnEliminar);

        JButton btnDemo = new JButton("🌱 Datos Demo");
        btnDemo.setToolTipText("Carga 5 mediciones de prueba para evaluar gráficos y comparaciones");
        btnDemo.addActionListener(e -> cargarDatosDemo());
        btnPanel.add(btnDemo);

        JButton btnTest = new JButton("🩺 Cuestionario");
        btnTest.setToolTipText("Abrir o actualizar tu cuestionario médico inicial");
        btnTest.addActionListener(e -> {
            CuestionarioDialog dialog = new CuestionarioDialog(this, usuario);
            dialog.setVisible(true);
            if (dialog.isCompletado()) {
                actualizarEtiquetaUsuario();
                recargarDatos();
            }
        });
        btnPanel.add(btnTest);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel metricas = new JPanel(new GridLayout(1, 3, 15, 0));
        metricas.setBorder(BorderFactory.createTitledBorder("Promedios y Métricas"));

        lblPromedioCardiaco = new JLabel("FC Promedio: -- bpm", SwingConstants.CENTER);
        lblPromedioCardiaco.setFont(new Font("SansSerif", Font.BOLD, 14));

        lblPromedioGlucosa = new JLabel("Glucosa Promedio: -- mg/dL", SwingConstants.CENTER);
        lblPromedioGlucosa.setFont(new Font("SansSerif", Font.BOLD, 14));

        lblIMC = new JLabel("IMC Actual: --", SwingConstants.CENTER);
        lblIMC.setFont(new Font("SansSerif", Font.BOLD, 14));

        metricas.add(lblPromedioCardiaco);
        metricas.add(lblPromedioGlucosa);
        metricas.add(lblIMC);

        JPanel topEstadisticas = new JPanel(new BorderLayout());
        topEstadisticas.add(metricas, BorderLayout.CENTER);

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelFiltro.add(new JLabel("Filtrar Gráfico por:"));
        JComboBox<String> cbFiltroGrafico = new JComboBox<>(new String[]{
                "Todas las variables",
                "Solo Peso (kg)",
                "Solo Frecuencia Cardíaca (bpm)",
                "Solo Glucosa (mg/dL)",
                "Solo Índice de Masa Corporal (IMC)"
        });
        cbFiltroGrafico.addActionListener(e -> {
            String sel = (String) cbFiltroGrafico.getSelectedItem();
            String tipo = "TODAS";
            if ("Solo Peso (kg)".equals(sel)) tipo = "PESO";
            else if ("Solo Frecuencia Cardíaca (bpm)".equals(sel)) tipo = "CARDIACO";
            else if ("Solo Glucosa (mg/dL)".equals(sel)) tipo = "GLUCOSA";
            else if ("Solo Índice de Masa Corporal (IMC)".equals(sel)) tipo = "IMC";
            actualizarGrafico(tipo);
        });
        panelFiltro.add(cbFiltroGrafico);
        topEstadisticas.add(panelFiltro, BorderLayout.SOUTH);

        panel.add(topEstadisticas, BorderLayout.NORTH);

        panelGrafico = new JPanel(new BorderLayout());
        panelGrafico.setBorder(BorderFactory.createTitledBorder("Gráfica de Evolución Temporal"));
        panel.add(panelGrafico, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelComparacion() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel selectores = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        selectores.setBorder(BorderFactory.createTitledBorder("Seleccionar Mediciones a Comparar"));

        cbMedicion1 = new JComboBox<>();
        cbMedicion2 = new JComboBox<>();
        JButton btnComparar = new JButton("Comparar Evolución");
        btnComparar.setBackground(new Color(41, 128, 185));
        btnComparar.setForeground(Color.WHITE);
        btnComparar.addActionListener(e -> ejecutarComparacion());

        selectores.add(new JLabel("Medición A:"));
        selectores.add(cbMedicion1);
        selectores.add(new JLabel("Medición B:"));
        selectores.add(cbMedicion2);
        selectores.add(btnComparar);

        panel.add(selectores, BorderLayout.NORTH);

        txtResultadoComparacion = new JTextArea();
        txtResultadoComparacion.setEditable(false);
        txtResultadoComparacion.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtResultadoComparacion.setMargin(new Insets(15, 15, 15, 15));
        panel.add(new JScrollPane(txtResultadoComparacion), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelRecomendaciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Recomendaciones de Salud Personalizadas");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton btnGuardarBD = new JButton("Guardar Recomendaciones en BD");
        btnGuardarBD.addActionListener(e -> guardarRecomendacionesEnBD());

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnGuardarBD, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        txtRecomendaciones = new JTextArea();
        txtRecomendaciones.setEditable(false);
        txtRecomendaciones.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtRecomendaciones.setLineWrap(true);
        txtRecomendaciones.setWrapStyleWord(true);
        txtRecomendaciones.setMargin(new Insets(10, 10, 10, 10));

        panel.add(new JScrollPane(txtRecomendaciones), BorderLayout.CENTER);

        return panel;
    }

    private void guardarMedicion() {
        String perfil = usuario.getPerfilMedico();
        boolean puedePeso = PerfilMedicoService.permitePeso(perfil);
        boolean puedeAltura = PerfilMedicoService.permiteAltura(perfil);
        boolean puedeFC = PerfilMedicoService.permiteFrecuencia(perfil);
        boolean puedeGlucosa = PerfilMedicoService.permiteGlucosa(perfil);

        try {
            Double peso = (puedePeso && !txtPeso.getText().trim().isEmpty()) ? Double.parseDouble(txtPeso.getText().trim()) : null;
            Double altura = (puedeAltura && !txtAltura.getText().trim().isEmpty()) ? Double.parseDouble(txtAltura.getText().trim()) : null;
            Integer fc = (puedeFC && !txtFrecuencia.getText().trim().isEmpty()) ? Integer.parseInt(txtFrecuencia.getText().trim()) : null;
            Double glucosa = (puedeGlucosa && !txtGlucosa.getText().trim().isEmpty()) ? Double.parseDouble(txtGlucosa.getText().trim()) : null;

            if (peso == null && altura == null && fc == null && glucosa == null) {
                JOptionPane.showMessageDialog(this,
                        "Debes ingresar al menos un dato biométrico correspondiente a tu perfil (" + PerfilMedicoService.getNombreLegible(perfil) + "):\n• "
                                + String.join("\n• ", PerfilMedicoService.getMetricasPermitidas(perfil)),
                        "Validación de Perfil", JOptionPane.WARNING_MESSAGE);
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

            Medicion m = new Medicion(
                    usuario.getIdUsuario(),
                    new Timestamp(System.currentTimeMillis()),
                    peso,
                    altura,
                    fc,
                    glucosa
            );

            PerfilMedicoService.sanitizarMedicion(m, perfil);

            if (medicionDAO.agregar(m)) {
                JOptionPane.showMessageDialog(this, "Medición registrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtPeso.setText("");
                txtAltura.setText("");
                txtFrecuencia.setText("");
                txtGlucosa.setText("");
                recargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar la medición.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa números válidos (ej: Altura: 1.75, Peso: 70.5).", "Formato numérico inválido", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarSeleccionada() {
        int row = tblHistorial.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idMedicion = (int) tableModel.getValueAt(row, 0);
        Medicion target = null;
        for (Medicion m : medicionesCargadas) {
            if (m.getIdMedicion() == idMedicion) {
                target = m;
                break;
            }
        }

        if (target != null) {
            EditarMedicionDialog dialog = new EditarMedicionDialog(this, target, usuario);
            dialog.setVisible(true);
            if (dialog.isActualizado()) {
                recargarDatos();
            }
        }
    }

    private void eliminarSeleccionada() {
        int row = tblHistorial.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una fila para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idMedicion = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de eliminar la medición con ID #" + idMedicion + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (medicionDAO.eliminar(idMedicion, usuario.getIdUsuario())) {
                    JOptionPane.showMessageDialog(this, "Medición eliminada.");
                    recargarDatos();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ejecutarComparacion() {
        int idx1 = cbMedicion1.getSelectedIndex();
        int idx2 = cbMedicion2.getSelectedIndex();

        if (idx1 < 0 || idx2 < 0 || idx1 >= medicionesCargadas.size() || idx2 >= medicionesCargadas.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona dos mediciones válidas para comparar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (idx1 == idx2) {
            JOptionPane.showMessageDialog(this, "Selecciona dos mediciones distintas para realizar una comparación temporal.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Medicion m1 = medicionesCargadas.get(idx1);
        Medicion m2 = medicionesCargadas.get(idx2);

        ComparacionBiometrica comp = analisisService.compararMediciones(m1, m2);
        if (comp == null) {
            txtResultadoComparacion.setText("No se pudo calcular la comparación.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("               INFORME COMPARATIVO DE EVOLUCIÓN BIOMÉTRICA              \n");
        sb.append("========================================================================\n\n");
        sb.append(String.format("Medición Anterior: #%d (%s)\n", comp.getMedicionAnterior().getIdMedicion(), sdf.format(comp.getMedicionAnterior().getFechaHora())));
        sb.append(String.format("Medición Posterior: #%d (%s)\n\n", comp.getMedicionPosterior().getIdMedicion(), sdf.format(comp.getMedicionPosterior().getFechaHora())));

        sb.append("DETALLE DE DIFERENCIAS:\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append(String.format("• Peso:        %s -> %s  (Δ: %s)\n",
                formatear(comp.getPesoAnterior(), "kg"), formatear(comp.getPesoPosterior(), "kg"), formatearDelta(comp.getDeltaPeso(), "kg")));
        sb.append(String.format("• IMC:         %s -> %s  (Δ: %s)\n",
                formatear(comp.getImcAnterior(), ""), formatear(comp.getImcPosterior(), ""), formatearDelta(comp.getDeltaIMC(), "")));
        sb.append(String.format("• Frec. Card.: %s -> %s  (Δ: %s)\n",
                formatearInt(comp.getFcAnterior(), "bpm"), formatearInt(comp.getFcPosterior(), "bpm"), formatearDeltaInt(comp.getDeltaFC(), "bpm")));
        sb.append(String.format("• Glucosa:     %s -> %s  (Δ: %s)\n",
                formatear(comp.getGlucosaAnterior(), "mg/dL"), formatear(comp.getGlucosaPosterior(), "mg/dL"), formatearDelta(comp.getDeltaGlucosa(), "mg/dL")));

        sb.append("\nINTERPRETACIÓN CLÍNICA / DIAGNÓSTICO:\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append(comp.getDiagnostico());

        txtResultadoComparacion.setText(sb.toString());
    }

    private String formatear(Double val, String unit) {
        return val != null ? String.format("%.2f %s", val, unit).trim() : "-";
    }

    private String formatearInt(Integer val, String unit) {
        return val != null ? String.format("%d %s", val, unit).trim() : "-";
    }

    private String formatearDelta(Double val, String unit) {
        return val != null ? String.format("%+.2f %s", val, unit).trim() : "-";
    }

    private String formatearDeltaInt(Integer val, String unit) {
        return val != null ? String.format("%+d %s", val, unit).trim() : "-";
    }

    private void guardarRecomendacionesEnBD() {
        if (recomendacionesActuales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay recomendaciones para guardar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int guardadas = 0;
            for (Recomendacion r : recomendacionesActuales) {
                if (recomendacionDAO.guardar(r)) {
                    guardadas++;
                }
            }
            JOptionPane.showMessageDialog(this, "Se guardaron " + guardadas + " recomendaciones en la base de datos.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar recomendaciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recargarDatos() {
        try {
            medicionesCargadas = medicionDAO.obtenerPorUsuario(usuario.getIdUsuario());

            // Actualizar tabla
            tableModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdfCorto = new SimpleDateFormat("dd/MM HH:mm");

            cbMedicion1.removeAllItems();
            cbMedicion2.removeAllItems();

            for (Medicion m : medicionesCargadas) {
                tableModel.addRow(new Object[]{
                        m.getIdMedicion(),
                        sdf.format(m.getFechaHora()),
                        m.getPeso() != null ? m.getPeso() : "-",
                        m.getAltura() != null ? m.getAltura() : "-",
                        m.getFrecuenciaCardiaca() != null ? m.getFrecuenciaCardiaca() : "-",
                        m.getGlucosaSangre() != null ? m.getGlucosaSangre() : "-"
                });

                String item = String.format("#%d (%s)", m.getIdMedicion(), sdfCorto.format(m.getFechaHora()));
                cbMedicion1.addItem(item);
                cbMedicion2.addItem(item);
            }

            if (medicionesCargadas.size() >= 2) {
                cbMedicion1.setSelectedIndex(0);
                cbMedicion2.setSelectedIndex(medicionesCargadas.size() - 1);
            }

            // Actualizar Métricas
            double fcProm = analisisService.calcularCardiaco(medicionesCargadas);
            double gluProm = analisisService.calcularGlucosa(medicionesCargadas);
            double imc = analisisService.calcularIMC(medicionesCargadas);

            lblPromedioCardiaco.setText(String.format("FC Promedio: %.0f bpm", fcProm));
            lblPromedioGlucosa.setText(String.format("Glucosa Promedio: %.1f mg/dL", gluProm));
            lblIMC.setText(String.format("IMC Actual: %.1f", imc));

            // Actualizar Gráfica con el filtro seleccionado
            actualizarGrafico(filtroGraficoActual);

            // Actualizar Recomendaciones
            recomendacionesActuales = analisisService.generarRecomendaciones(usuario.getIdUsuario(), medicionesCargadas);
            StringBuilder sb = new StringBuilder();
            for (Recomendacion r : recomendacionesActuales) {
                sb.append("• [").append(r.getTipo()).append("]: ").append(r.getMensaje()).append("\n\n");
            }
            txtRecomendaciones.setText(sb.toString());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al sincronizar datos:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarGrafico(String tipoFiltro) {
        this.filtroGraficoActual = tipoFiltro;
        panelGrafico.removeAll();
        if (!medicionesCargadas.isEmpty()) {
            JFreeChart chart = analisisService.generarGraficaFiltrada(medicionesCargadas, tipoFiltro);
            ChartPanel chartPanel = new ChartPanel(chart);
            panelGrafico.add(chartPanel, BorderLayout.CENTER);
        } else {
            panelGrafico.add(new JLabel("No hay mediciones suficientes para graficar.", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        panelGrafico.revalidate();
        panelGrafico.repaint();
    }

    private void cargarDatosDemo() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas cargar 5 mediciones biométricas de prueba para este usuario?\nEsto facilitará evaluar gráficos, comparaciones y recomendaciones.",
                "Cargar Datos de Demostración",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            int insertados = org.tusalud.config.DatabaseInitializer.sembrarDatosDemo(usuario.getIdUsuario(), usuario.getPerfilMedico());
            JOptionPane.showMessageDialog(this, "Se cargaron " + insertados + " registros de prueba adaptados a tu perfil.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            recargarDatos();
        }
    }
}
