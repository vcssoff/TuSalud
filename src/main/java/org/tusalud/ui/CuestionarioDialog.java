package org.tusalud.ui;

import org.tusalud.model.Recomendacion;
import org.tusalud.model.Usuario;
import org.tusalud.repository.RecomendacionDAO;
import org.tusalud.repository.UsuarioDAO;
import org.tusalud.service.PerfilMedicoService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CuestionarioDialog extends JDialog {

    private final Usuario usuario;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RecomendacionDAO recomendacionDAO = new RecomendacionDAO();
    private boolean completado = false;

    // Pregunta 1: Situación de salud actual
    private JRadioButton rbHipertension;
    private JRadioButton rbDiabetes;
    private JRadioButton rbAmbas;
    private JRadioButton rbPrevAmbas;
    private JRadioButton rbPrevGeneral;

    // Preguntas Hipertensión
    private JComboBox<String> cbTipoHipertension;
    private JComboBox<String> cbTiempoHipertension;
    private JComboBox<String> cbControlHipertension;
    private JComboBox<String> cbMedicaHipertension;
    private JComboBox<String> cbFrecuenciaHipertension;
    private JComboBox<String> cbRegistraHipertension;
    private JComboBox<String> cbAlcanceHipertension;

    // Preguntas Diabetes
    private JComboBox<String> cbTipoDiabetes;
    private JComboBox<String> cbTiempoDiabetes;
    private JComboBox<String> cbControlDiabetes;
    private JComboBox<String> cbInsulinaDiabetes;
    private JComboBox<String> cbPautaDiabetes;
    private JComboBox<String> cbFrecuenciaDiabetes;
    private JComboBox<String> cbRegistraDiabetes;
    private JComboBox<String> cbAlcanceDiabetes;

    // Preguntas Preventivo Dual
    private JComboBox<String> cbMotivoPrev;
    private JComboBox<String> cbAnalisisPrev;
    private JComboBox<String> cbFrecuenciaPrev;

    // Preguntas Preventivo General / Bienestar
    private JComboBox<String> cbObjetivoGeneral;
    private JComboBox<String> cbMetaGeneral;

    // Paneles condicionales
    private JPanel panelHipertension;
    private JPanel panelDiabetes;
    private JPanel panelPrevDual;
    private JPanel panelPrevGeneral;

    public CuestionarioDialog(Frame parent, Usuario usuario) {
        super(parent, "TuSalud - Cuestionario Inicial de Salud", true);
        this.usuario = usuario;
        initComponents();
    }

    private void initComponents() {
        setSize(780, 680);
        setLocationRelativeTo(getParent());
        setResizable(true);
        setLayout(new BorderLayout());

        // Header Superior
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Bienvenido a TuSalud, " + usuario.getNombre());
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Por favor completa este breve cuestionario para personalizar tus mediciones y recomendaciones.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(236, 240, 241));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Panel de Contenido con Scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Pregunta Principal
        JPanel panelPregunta1 = new JPanel();
        panelPregunta1.setLayout(new BoxLayout(panelPregunta1, BoxLayout.Y_AXIS));
        panelPregunta1.setBorder(BorderFactory.createTitledBorder("1. ¿Cuál es tu situación de salud actual?"));

        rbHipertension = new JRadioButton("Hipertensión diagnosticada");
        rbDiabetes = new JRadioButton("Diabetes diagnosticada");
        rbAmbas = new JRadioButton("Hipertensión y Diabetes diagnosticadas");
        rbPrevAmbas = new JRadioButton("Control preventivo de ambas (Hipertensión y Diabetes - antecedentes o chequeo)");
        rbPrevGeneral = new JRadioButton("Control preventivo general (Bienestar, peso y condición física)", true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbHipertension);
        bg.add(rbDiabetes);
        bg.add(rbAmbas);
        bg.add(rbPrevAmbas);
        bg.add(rbPrevGeneral);

        panelPregunta1.add(rbHipertension);
        panelPregunta1.add(rbDiabetes);
        panelPregunta1.add(rbAmbas);
        panelPregunta1.add(rbPrevAmbas);
        panelPregunta1.add(rbPrevGeneral);

        contentPanel.add(panelPregunta1);
        contentPanel.add(Box.createVerticalStrut(10));

        // 2. Paneles Dinámicos
        panelHipertension = crearPanelHipertension();
        panelDiabetes = crearPanelDiabetes();
        panelPrevDual = crearPanelPrevDual();
        panelPrevGeneral = crearPanelPrevGeneral();

        contentPanel.add(panelHipertension);
        contentPanel.add(panelDiabetes);
        contentPanel.add(panelPrevDual);
        contentPanel.add(panelPrevGeneral);

        // Listener para cambiar visibilidad según la selección
        java.awt.event.ActionListener toggleListener = e -> actualizarVisibilidadPaneles();
        rbHipertension.addActionListener(toggleListener);
        rbDiabetes.addActionListener(toggleListener);
        rbAmbas.addActionListener(toggleListener);
        rbPrevAmbas.addActionListener(toggleListener);
        rbPrevGeneral.addActionListener(toggleListener);

        actualizarVisibilidadPaneles();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Botonera Inferior
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        JButton btnOmitir = new JButton("Completar más tarde");
        JButton btnGuardar = new JButton("Finalizar y Configurar Perfil");

        btnGuardar.setBackground(new Color(39, 174, 96));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 13));

        btnOmitir.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> procesarCuestionario());

        footerPanel.add(btnOmitir);
        footerPanel.add(btnGuardar);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel crearPanelHipertension() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Módulo de Hipertensión"));

        cbTipoHipertension = new JComboBox<>(new String[]{"Hipertensión primaria o esencial", "Hipertensión secundaria", "No lo sé"});
        cbTiempoHipertension = new JComboBox<>(new String[]{"Menos de 1 año", "Entre 1 y 5 años", "Más de 5 años", "No recuerdo"});
        cbControlHipertension = new JComboBox<>(new String[]{"Medicamentos antihipertensivos", "Cambios en alimentación", "Actividad física", "Tratamiento combinado", "Ningún tratamiento", "No lo sé"});
        cbMedicaHipertension = new JComboBox<>(new String[]{"Sí", "No", "A veces"});
        cbFrecuenciaHipertension = new JComboBox<>(new String[]{"Todos los días", "Varias veces por semana", "Una vez por semana", "Algunas veces al mes", "Solo con síntomas", "Casi nunca"});
        cbRegistraHipertension = new JComboBox<>(new String[]{"Sí, diariamente", "Sí, algunas veces", "No"});
        cbAlcanceHipertension = new JComboBox<>(new String[]{"Frecuencia Cardíaca y Control de Peso / IMC (Recomendado)", "Únicamente Frecuencia Cardíaca (Pulsaciones)"});

        panel.add(new JLabel("Tipo de hipertensión:"));
        panel.add(cbTipoHipertension);
        panel.add(new JLabel("Tiempo de diagnóstico:"));
        panel.add(cbTiempoHipertension);
        panel.add(new JLabel("Forma de control actual:"));
        panel.add(cbControlHipertension);
        panel.add(new JLabel("¿Tomas medicación a diario?:"));
        panel.add(cbMedicaHipertension);
        panel.add(new JLabel("Frecuencia de control:"));
        panel.add(cbFrecuenciaHipertension);
        panel.add(new JLabel("¿Registras tus valores?:"));
        panel.add(cbRegistraHipertension);
        panel.add(new JLabel("Variables que deseas registrar:"));
        panel.add(cbAlcanceHipertension);

        return panel;
    }

    private JPanel crearPanelDiabetes() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Módulo de Diabetes"));

        cbTipoDiabetes = new JComboBox<>(new String[]{"Tipo 1", "Tipo 2", "Gestacional", "Otro", "No lo sé"});
        cbTiempoDiabetes = new JComboBox<>(new String[]{"Menos de 1 año", "Entre 1 y 5 años", "Más de 5 años", "No recuerdo"});
        cbControlDiabetes = new JComboBox<>(new String[]{"Alimentación", "Insulina", "Medicamentos orales", "Tratamiento combinado", "Ninguno", "No lo sé"});
        cbInsulinaDiabetes = new JComboBox<>(new String[]{"No utilizo insulina", "Inyecciones (lapicera/jeringa)", "Bomba de infusión continua", "Otro"});
        cbPautaDiabetes = new JComboBox<>(new String[]{"Sí, estrictamente", "No", "A veces", "No tengo pauta"});
        cbFrecuenciaDiabetes = new JComboBox<>(new String[]{"Varias veces al día", "Una vez al día", "Varias veces por semana", "Algunas veces al mes", "Solo si me siento mal", "Casi nunca"});
        cbRegistraDiabetes = new JComboBox<>(new String[]{"Sí, diariamente", "Sí, algunas veces", "No"});
        cbAlcanceDiabetes = new JComboBox<>(new String[]{"Glucosa en Sangre y Control de Peso / IMC (Recomendado)", "Únicamente Glucosa en Sangre"});

        panel.add(new JLabel("Tipo de diabetes:"));
        panel.add(cbTipoDiabetes);
        panel.add(new JLabel("Tiempo de diagnóstico:"));
        panel.add(cbTiempoDiabetes);
        panel.add(new JLabel("Tratamiento principal:"));
        panel.add(cbControlDiabetes);
        panel.add(new JLabel("Administración de insulina:"));
        panel.add(cbInsulinaDiabetes);
        panel.add(new JLabel("¿Sigues pauta nutricional?:"));
        panel.add(cbPautaDiabetes);
        panel.add(new JLabel("Frecuencia de medición glucosa:"));
        panel.add(cbFrecuenciaDiabetes);
        panel.add(new JLabel("¿Registras tus valores?:"));
        panel.add(cbRegistraDiabetes);
        panel.add(new JLabel("Variables que deseas registrar:"));
        panel.add(cbAlcanceDiabetes);

        return panel;
    }

    private JPanel crearPanelPrevDual() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Módulo de Control Preventivo (Hipertensión y Diabetes)"));

        cbMotivoPrev = new JComboBox<>(new String[]{"Antecedentes familiares directos", "Indicación médica preventiva", "Sobrepeso o sedentarismo", "Cuidado general y prevención"});
        cbAnalisisPrev = new JComboBox<>(new String[]{"Sí, con valores normales", "Sí, con valores limítrofes / prediabetes", "No en el último año", "No recuerdo"});
        cbFrecuenciaPrev = new JComboBox<>(new String[]{"Semanalmente", "Quincenal o mensualmente", "Ocasionalmente"});

        panel.add(new JLabel("Motivo del seguimiento preventivo:"));
        panel.add(cbMotivoPrev);
        panel.add(new JLabel("Análisis clínicos recientes:"));
        panel.add(cbAnalisisPrev);
        panel.add(new JLabel("Frecuencia sugerida de control:"));
        panel.add(cbFrecuenciaPrev);

        return panel;
    }

    private JPanel crearPanelPrevGeneral() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Módulo de Bienestar y Control General"));

        cbObjetivoGeneral = new JComboBox<>(new String[]{"Control de peso corporal y evolución de IMC", "Monitoreo de actividad física y frecuencia cardíaca", "Registro de hábitos y salud general"});
        cbMetaGeneral = new JComboBox<>(new String[]{"Descenso de peso saludable", "Mantenimiento y aumento de masa muscular", "Control periódico de rutina"});

        panel.add(new JLabel("Objetivo principal de salud:"));
        panel.add(cbObjetivoGeneral);
        panel.add(new JLabel("Meta personal:"));
        panel.add(cbMetaGeneral);

        return panel;
    }

    private void actualizarVisibilidadPaneles() {
        boolean esHTA = rbHipertension.isSelected();
        boolean esDBT = rbDiabetes.isSelected();
        boolean esAmbas = rbAmbas.isSelected();
        boolean esPrevAmbas = rbPrevAmbas.isSelected();
        boolean esPrevGen = rbPrevGeneral.isSelected();

        panelHipertension.setVisible(esHTA || esAmbas);
        panelDiabetes.setVisible(esDBT || esAmbas);
        panelPrevDual.setVisible(esPrevAmbas);
        panelPrevGeneral.setVisible(esPrevGen);

        revalidate();
        repaint();
    }

    private void procesarCuestionario() {
        String perfilNombre = "";
        String metricas = "";
        List<String> consejos = new ArrayList<>();

        if (rbHipertension.isSelected()) {
            boolean soloFC = "Únicamente Frecuencia Cardíaca (Pulsaciones)".equals(cbAlcanceHipertension.getSelectedItem());
            perfilNombre = soloFC ? PerfilMedicoService.PERFIL_HIPERTENSION_SOLO_FC : PerfilMedicoService.PERFIL_HIPERTENSION;
            metricas = soloFC ? "• Frecuencia Cardíaca (bpm)" : "• Frecuencia Cardíaca (bpm)\n• Peso Corporal (kg) e IMC";
            consejos.add("Monitorea tu frecuencia cardíaca matutina en reposo y mantén un registro constante.");
            if ("Medicamentos antihipertensivos".equals(cbControlHipertension.getSelectedItem()) || "Tratamiento combinado".equals(cbControlHipertension.getSelectedItem())) {
                consejos.add("Recuerda la toma puntual de tu medicación según la indicación de tu médico.");
            }
        } else if (rbDiabetes.isSelected()) {
            boolean soloGlucosa = "Únicamente Glucosa en Sangre".equals(cbAlcanceDiabetes.getSelectedItem());
            perfilNombre = soloGlucosa ? PerfilMedicoService.PERFIL_DIABETES_SOLO_GLUCOSA : PerfilMedicoService.PERFIL_DIABETES;
            metricas = soloGlucosa ? "• Glucosa en Sangre (mg/dL)" : "• Glucosa en Sangre (mg/dL)\n• Peso Corporal (kg) e IMC";
            consejos.add("Registra tus niveles de glucosa en ayunas y postprandial para un mejor control metabólico.");
            if ("Insulina".equals(cbControlDiabetes.getSelectedItem())) {
                consejos.add("Lleva control estricto de las dosis de insulina y horarios de ingesta.");
            }
        } else if (rbAmbas.isSelected()) {
            perfilNombre = PerfilMedicoService.PERFIL_DUAL;
            metricas = "• Glucosa en Sangre (mg/dL)\n• Frecuencia Cardíaca (bpm)\n• Peso Corporal (kg) e IMC";
            consejos.add("Tu perfil integral requiere un seguimiento coordinado de glucosa en sangre y salud cardiovascular.");
            consejos.add("Evita sal refinada y azúcares simples; realiza actividad física aeróbica moderada.");
        } else if (rbPrevAmbas.isSelected()) {
            perfilNombre = PerfilMedicoService.PERFIL_PREVENTIVO_DUAL;
            metricas = "• Glucosa en Sangre (mg/dL)\n• Frecuencia Cardíaca (bpm)\n• Peso e IMC";
            consejos.add("Monitoreo preventivo activo por antecedentes o factores de riesgo.");
            consejos.add("Un chequeo quincenal o mensual de glucosa y frecuencia cardíaca ayuda a la detección temprana.");
        } else {
            String obj = (String) cbObjetivoGeneral.getSelectedItem();
            if ("Control de peso corporal y evolución de IMC".equals(obj)) {
                perfilNombre = PerfilMedicoService.PERFIL_BIENESTAR_PESO;
                metricas = "• Peso Corporal (kg)\n• Altura (m) e IMC";
                consejos.add("Registra semanalmente tu peso corporal para observar la tendencia sin obsesionarte con fluctuaciones diarias.");
            } else if ("Monitoreo de actividad física y frecuencia cardíaca".equals(obj)) {
                perfilNombre = PerfilMedicoService.PERFIL_BIENESTAR_CARDIO;
                metricas = "• Frecuencia Cardíaca (bpm)\n• Peso y Condición Física";
                consejos.add("Monitorea tu pulso basal al despertar para medir tu adaptación cardiovascular al entrenamiento.");
            } else {
                perfilNombre = PerfilMedicoService.PERFIL_BIENESTAR_GENERAL;
                metricas = "• Peso Corporal (kg) e IMC\n• Frecuencia Cardíaca (bpm)";
                consejos.add("Mantén el registro periódico de tu peso y altura para evaluar la evolución del IMC.");
                consejos.add("Monitorea tus pulsaciones antes y después de hacer ejercicio.");
            }
        }

        try {
            // Guardar perfil en la BD para el usuario
            usuario.setPerfilMedico(perfilNombre);
            usuarioDAO.actualizarPerfilMedico(usuario.getIdUsuario(), perfilNombre);

            // Generar recomendaciones iniciales personalizadas en la base de datos
            for (String c : consejos) {
                recomendacionDAO.guardar(new Recomendacion(usuario.getIdUsuario(), null, "ONBOARDING", c));
            }

            StringBuilder msg = new StringBuilder();
            msg.append("¡Perfil de Salud configurado exitosamente!\n\n");
            msg.append("📌 Categoría asignada: ").append(PerfilMedicoService.getNombreLegible(perfilNombre)).append("\n\n");
            msg.append("✅ Variables biométricas habilitadas para registrar:\n");
            for (String m : PerfilMedicoService.getMetricasPermitidas(perfilNombre)) {
                msg.append("  • ").append(m).append("\n");
            }
            List<String> bloqueadas = PerfilMedicoService.getMetricasBloqueadas(perfilNombre);
            if (!bloqueadas.isEmpty()) {
                msg.append("\n🔒 Variables bloqueadas (no influyen en las opciones elegidas):\n");
                for (String b : bloqueadas) {
                    msg.append("  • ").append(b).append("\n");
                }
            }
            msg.append("\nTu panel de nueva medición y formulario de edición han sido personalizados.");

            JOptionPane.showMessageDialog(this, msg.toString(), "Bienvenido a TuSalud", JOptionPane.INFORMATION_MESSAGE);
            this.completado = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el perfil médico:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompletado() {
        return completado;
    }
}
