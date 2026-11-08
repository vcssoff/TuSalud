package org.tusalud.service;

import org.tusalud.model.Medicion;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que administra las reglas de negocio y restricciones biométricas
 * derivadas de las respuestas del Cuestionario Médico Inicial.
 * 
 * Garantiza que cada usuario únicamente pueda registrar y modificar los datos
 * que influyen en las opciones o enfermedades que eligió controlar.
 */
public class PerfilMedicoService {

    // Identificadores de Perfiles Médicos
    public static final String PERFIL_DIABETES = "DIABETES";
    public static final String PERFIL_DIABETES_SOLO_GLUCOSA = "DIABETES_SOLO_GLUCOSA";
    public static final String PERFIL_HIPERTENSION = "HIPERTENSION";
    public static final String PERFIL_HIPERTENSION_SOLO_FC = "HIPERTENSION_SOLO_FC";
    public static final String PERFIL_DUAL = "DUAL_DIAGNOSTICADO";
    public static final String PERFIL_PREVENTIVO_DUAL = "PREVENTIVO_DUAL";
    public static final String PERFIL_BIENESTAR_PESO = "BIENESTAR_PESO";
    public static final String PERFIL_BIENESTAR_CARDIO = "BIENESTAR_CARDIO";
    public static final String PERFIL_BIENESTAR_GENERAL = "BIENESTAR_GENERAL";

    /**
     * Determina si el perfil permite ingresar o modificar el Peso corporal.
     */
    public static boolean permitePeso(String perfil) {
        if (perfil == null || perfil.trim().isEmpty()) return true;
        String p = perfil.trim().toUpperCase();
        if (p.equals(PERFIL_DIABETES_SOLO_GLUCOSA) || p.equals(PERFIL_HIPERTENSION_SOLO_FC)) {
            return false;
        }
        return true;
    }

    /**
     * Determina si el perfil permite ingresar o modificar la Altura (para cálculo de IMC).
     */
    public static boolean permiteAltura(String perfil) {
        return permitePeso(perfil);
    }

    /**
     * Determina si el perfil permite ingresar o modificar la Frecuencia Cardíaca (pulsaciones bpm).
     */
    public static boolean permiteFrecuencia(String perfil) {
        if (perfil == null || perfil.trim().isEmpty()) return true;
        String p = perfil.trim().toUpperCase();
        if (p.startsWith("DIABETES") || p.equals(PERFIL_BIENESTAR_PESO)) {
            return false;
        }
        return true;
    }

    /**
     * Determina si el perfil permite ingresar o modificar el nivel de Glucosa en sangre.
     */
    public static boolean permiteGlucosa(String perfil) {
        if (perfil == null || perfil.trim().isEmpty()) return true;
        String p = perfil.trim().toUpperCase();
        if (p.startsWith("HIPERTENSION") || p.startsWith("BIENESTAR")) {
            return false;
        }
        return true;
    }

    /**
     * Retorna el nombre legible y profesional del perfil para la interfaz gráfica.
     */
    public static String getNombreLegible(String perfil) {
        if (perfil == null || perfil.trim().isEmpty()) return "General (Sin cuestionario)";
        switch (perfil.trim().toUpperCase()) {
            case PERFIL_DIABETES:
                return "Diabetes (Glucosa + Control Metabólico e IMC)";
            case PERFIL_DIABETES_SOLO_GLUCOSA:
                return "Diabetes (Monitoreo Exclusivo de Glucosa)";
            case PERFIL_HIPERTENSION:
                return "Hipertensión (Frecuencia Cardíaca + Peso e IMC)";
            case PERFIL_HIPERTENSION_SOLO_FC:
                return "Hipertensión (Monitoreo Exclusivo de Pulsaciones)";
            case PERFIL_DUAL:
                return "Monitoreo Integral (Hipertensión y Diabetes)";
            case PERFIL_PREVENTIVO_DUAL:
                return "Control Preventivo Dual (Cardio-Metabólico)";
            case PERFIL_BIENESTAR_PESO:
                return "Bienestar y Control de Peso corporal (IMC)";
            case PERFIL_BIENESTAR_CARDIO:
                return "Bienestar y Rendimiento Cardiovascular (FC)";
            case PERFIL_BIENESTAR_GENERAL:
                return "Bienestar y Condición Física Integral";
            default:
                return perfil.replace('_', ' ');
        }
    }

    /**
     * Devuelve una lista con los nombres de las métricas que el usuario tiene habilitadas.
     */
    public static List<String> getMetricasPermitidas(String perfil) {
        List<String> list = new ArrayList<>();
        if (permitePeso(perfil)) list.add("Peso corporal (kg)");
        if (permiteAltura(perfil)) list.add("Altura (m)");
        if (permiteFrecuencia(perfil)) list.add("Frecuencia Cardíaca (bpm)");
        if (permiteGlucosa(perfil)) list.add("Glucosa en Sangre (mg/dL)");
        return list;
    }

    /**
     * Devuelve una lista con los nombres de las métricas deshabilitadas por no influir
     * en el motivo de seguimiento o enfermedad elegida.
     */
    public static List<String> getMetricasBloqueadas(String perfil) {
        List<String> list = new ArrayList<>();
        if (!permitePeso(perfil)) list.add("Peso corporal (kg)");
        if (!permiteAltura(perfil)) list.add("Altura (m)");
        if (!permiteFrecuencia(perfil)) list.add("Frecuencia Cardíaca (bpm)");
        if (!permiteGlucosa(perfil)) list.add("Glucosa en Sangre (mg/dL)");
        return list;
    }

    /**
     * Explica clínicamente por qué un campo específico no puede modificarse.
     */
    public static String getMotivoBloqueo(String campo, String perfil) {
        String p = perfil != null ? perfil.toUpperCase() : "";
        if (campo.equalsIgnoreCase("frecuenciaCardiaca") || campo.equalsIgnoreCase("fc")) {
            if (p.startsWith("DIABETES")) {
                return "La frecuencia cardíaca está deshabilitada porque tu objetivo es controlar Diabetes (no influye en el control glucémico).";
            }
            if (p.equals(PERFIL_BIENESTAR_PESO)) {
                return "La frecuencia cardíaca está deshabilitada porque tu objetivo es únicamente el control de peso e IMC.";
            }
        }
        if (campo.equalsIgnoreCase("glucosa") || campo.equalsIgnoreCase("glucosaSangre")) {
            if (p.startsWith("HIPERTENSION")) {
                return "La glucosa en sangre está deshabilitada porque tu objetivo es controlar Hipertensión (no influye en la salud cardiovascular primaria).";
            }
            if (p.startsWith("BIENESTAR")) {
                return "La glucosa en sangre está deshabilitada porque tu objetivo es bienestar y control físico general (sin diagnóstico de diabetes).";
            }
        }
        if (campo.equalsIgnoreCase("peso") || campo.equalsIgnoreCase("altura")) {
            if (p.equals(PERFIL_DIABETES_SOLO_GLUCOSA)) {
                return "El registro de peso y altura está deshabilitado porque configuraste el seguimiento exclusivo de glucosa.";
            }
            if (p.equals(PERFIL_HIPERTENSION_SOLO_FC)) {
                return "El registro de peso y altura está deshabilitado porque configuraste el seguimiento exclusivo de pulsaciones.";
            }
        }
        return "Este campo no está habilitado para tu perfil médico actual.";
    }

    /**
     * Sanitiza una medición antes de persistirla en base de datos, garantizando
     * que ningún dato que no influya en las opciones del usuario sea registrado.
     */
    public static void sanitizarMedicion(Medicion m, String perfil) {
        if (m == null) return;
        if (!permitePeso(perfil)) m.setPeso(null);
        if (!permiteAltura(perfil)) m.setAltura(null);
        if (!permiteFrecuencia(perfil)) m.setFrecuenciaCardiaca(null);
        if (!permiteGlucosa(perfil)) m.setGlucosaSangre(null);
    }
}
