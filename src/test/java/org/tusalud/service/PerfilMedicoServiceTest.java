package org.tusalud.service;

import org.junit.jupiter.api.Test;
import org.tusalud.model.Medicion;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PerfilMedicoServiceTest {

    @Test
    public void testPerfilDiabetes_PermisosCorrectos() {
        String perfil = PerfilMedicoService.PERFIL_DIABETES;

        assertTrue(PerfilMedicoService.permiteGlucosa(perfil), "Diabetes debe permitir registrar y modificar glucosa");
        assertTrue(PerfilMedicoService.permitePeso(perfil), "Diabetes debe permitir control de peso metabólico");
        assertTrue(PerfilMedicoService.permiteAltura(perfil), "Diabetes debe permitir altura para cálculo de IMC");
        assertFalse(PerfilMedicoService.permiteFrecuencia(perfil), "Diabetes debe bloquear frecuencia cardíaca ya que no influye en la diabetes");

        List<String> permitidas = PerfilMedicoService.getMetricasPermitidas(perfil);
        assertTrue(permitidas.contains("Glucosa en Sangre (mg/dL)"));
        assertTrue(permitidas.contains("Peso corporal (kg)"));
        assertFalse(permitidas.contains("Frecuencia Cardíaca (bpm)"));
    }

    @Test
    public void testPerfilDiabetesSoloGlucosa_PermiteUnicamenteGlucosa() {
        String perfil = PerfilMedicoService.PERFIL_DIABETES_SOLO_GLUCOSA;

        assertTrue(PerfilMedicoService.permiteGlucosa(perfil), "Debe permitir registrar glucosa");
        assertFalse(PerfilMedicoService.permitePeso(perfil), "Debe bloquear peso");
        assertFalse(PerfilMedicoService.permiteAltura(perfil), "Debe bloquear altura");
        assertFalse(PerfilMedicoService.permiteFrecuencia(perfil), "Debe bloquear frecuencia cardíaca");

        List<String> bloqueadas = PerfilMedicoService.getMetricasBloqueadas(perfil);
        assertEquals(3, bloqueadas.size());
    }

    @Test
    public void testPerfilHipertension_PermisosCorrectos() {
        String perfil = PerfilMedicoService.PERFIL_HIPERTENSION;

        assertTrue(PerfilMedicoService.permiteFrecuencia(perfil), "Hipertensión debe permitir frecuencia cardíaca");
        assertTrue(PerfilMedicoService.permitePeso(perfil), "Hipertensión debe permitir control de peso");
        assertTrue(PerfilMedicoService.permiteAltura(perfil), "Hipertensión debe permitir altura para IMC");
        assertFalse(PerfilMedicoService.permiteGlucosa(perfil), "Hipertensión debe bloquear glucosa en sangre");
    }

    @Test
    public void testPerfilHipertensionSoloFC_PermiteUnicamentePulsaciones() {
        String perfil = PerfilMedicoService.PERFIL_HIPERTENSION_SOLO_FC;

        assertTrue(PerfilMedicoService.permiteFrecuencia(perfil), "Debe permitir registrar frecuencia cardíaca");
        assertFalse(PerfilMedicoService.permitePeso(perfil), "Debe bloquear peso");
        assertFalse(PerfilMedicoService.permiteAltura(perfil), "Debe bloquear altura");
        assertFalse(PerfilMedicoService.permiteGlucosa(perfil), "Debe bloquear glucosa");
    }

    @Test
    public void testPerfilDualYPreventivoDual_PermitenTodasLasVariables() {
        for (String perfil : new String[]{PerfilMedicoService.PERFIL_DUAL, PerfilMedicoService.PERFIL_PREVENTIVO_DUAL}) {
            assertTrue(PerfilMedicoService.permiteGlucosa(perfil));
            assertTrue(PerfilMedicoService.permiteFrecuencia(perfil));
            assertTrue(PerfilMedicoService.permitePeso(perfil));
            assertTrue(PerfilMedicoService.permiteAltura(perfil));
            assertEquals(4, PerfilMedicoService.getMetricasPermitidas(perfil).size());
            assertTrue(PerfilMedicoService.getMetricasBloqueadas(perfil).isEmpty());
        }
    }

    @Test
    public void testPerfilBienestarPeso_SoloPermitePesoEIMC() {
        String perfil = PerfilMedicoService.PERFIL_BIENESTAR_PESO;

        assertTrue(PerfilMedicoService.permitePeso(perfil));
        assertTrue(PerfilMedicoService.permiteAltura(perfil));
        assertFalse(PerfilMedicoService.permiteFrecuencia(perfil));
        assertFalse(PerfilMedicoService.permiteGlucosa(perfil));
    }

    @Test
    public void testSanitizarMedicion_EliminaCamposNoPermitidos() {
        Medicion m = new Medicion(1, new Timestamp(System.currentTimeMillis()), 75.0, 1.75, 80, 110.0);

        // Si el usuario es de perfil DIABETES (sin FC)
        PerfilMedicoService.sanitizarMedicion(m, PerfilMedicoService.PERFIL_DIABETES);
        assertEquals(75.0, m.getPeso(), "Debe conservar peso");
        assertEquals(1.75, m.getAltura(), "Debe conservar altura");
        assertEquals(110.0, m.getGlucosaSangre(), "Debe conservar glucosa");
        assertNull(m.getFrecuenciaCardiaca(), "Frecuencia cardíaca debe haber sido sanitizada a null");

        // Si el usuario es de perfil DIABETES_SOLO_GLUCOSA
        Medicion m2 = new Medicion(1, new Timestamp(System.currentTimeMillis()), 75.0, 1.75, 80, 110.0);
        PerfilMedicoService.sanitizarMedicion(m2, PerfilMedicoService.PERFIL_DIABETES_SOLO_GLUCOSA);
        assertNull(m2.getPeso(), "Debe sanitizar peso");
        assertNull(m2.getAltura(), "Debe sanitizar altura");
        assertNull(m2.getFrecuenciaCardiaca(), "Debe sanitizar frecuencia");
        assertEquals(110.0, m2.getGlucosaSangre(), "Debe conservar glucosa");
    }

    @Test
    public void testMotivoBloqueo_InformaRazonClinica() {
        String motivoFC = PerfilMedicoService.getMotivoBloqueo("fc", PerfilMedicoService.PERFIL_DIABETES);
        assertTrue(motivoFC.contains("Diabetes"), "Debe explicar que no influye en diabetes");

        String motivoGlucosa = PerfilMedicoService.getMotivoBloqueo("glucosa", PerfilMedicoService.PERFIL_HIPERTENSION);
        assertTrue(motivoGlucosa.contains("Hipertensión"), "Debe explicar que no influye en hipertensión");
    }
}
