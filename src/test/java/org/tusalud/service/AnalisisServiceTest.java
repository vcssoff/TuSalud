package org.tusalud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tusalud.model.ComparacionBiometrica;
import org.tusalud.model.Medicion;
import org.tusalud.model.Recomendacion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AnalisisServiceTest {

    private AnalisisService service = new AnalisisService();

    @BeforeEach
    public void setUp() {
        service = new AnalisisService();
    }

    @Test
    public void testCalcularCardiaco_ConValoresValidosYNulos() {
        List<Medicion> mediciones = new ArrayList<>();
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), 70.0, 1.75, 60, 90.0));
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), 70.0, 1.75, null, 95.0)); // nulo
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), 70.0, 1.75, 80, 100.0));

        double promedio = service.calcularCardiaco(mediciones);
        assertEquals(70.0, promedio, 0.001, "El promedio de 60 y 80 debe ser 70.0");
    }

    @Test
    public void testCalcularGlucosa() {
        List<Medicion> mediciones = new ArrayList<>();
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), null, null, null, 90.0));
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), null, null, null, 110.0));

        double promedio = service.calcularGlucosa(mediciones);
        assertEquals(100.0, promedio, 0.001, "El promedio de glucosa debe ser 100.0");
    }

    @Test
    public void testCalcularIMC_UltimaMedicionConDatos() {
        List<Medicion> mediciones = new ArrayList<>();
        // Primera medición: 80kg, 1.75m
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis() - 10000), 80.0, 1.75, null, null));
        // Segunda medición: 70kg, 1.75m (más reciente)
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), 70.0, 1.75, 75, 90.0));

        double imc = service.calcularIMC(mediciones);
        double imcEsperado = 70.0 / (1.75 * 1.75);
        assertEquals(imcEsperado, imc, 0.01, "Debe calcular el IMC basado en la medición más reciente");
    }

    @Test
    public void testGenerarRecomendaciones() {
        List<Medicion> mediciones = new ArrayList<>();
        // Glucosa elevada (140 mg/dL) y frecuencia cardíaca elevada (110 bpm)
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), 95.0, 1.70, 110, 140.0));

        List<Recomendacion> recomendaciones = service.generarRecomendaciones(1, mediciones);
        assertNotNull(recomendaciones);
        assertFalse(recomendaciones.isEmpty());

        boolean tieneAlertaGlucosa = recomendaciones.stream().anyMatch(r -> "GLUCOSA".equals(r.getTipo()));
        boolean tieneAlertaCardiaca = recomendaciones.stream().anyMatch(r -> "CARDIACO".equals(r.getTipo()));
        boolean tieneAlertaIMC = recomendaciones.stream().anyMatch(r -> "IMC".equals(r.getTipo()));

        assertTrue(tieneAlertaGlucosa, "Debe emitir recomendación sobre glucosa");
        assertTrue(tieneAlertaCardiaca, "Debe emitir recomendación sobre frecuencia cardíaca");
        assertTrue(tieneAlertaIMC, "Debe emitir recomendación sobre IMC");
    }

    @Test
    public void testCompararMediciones() {
        Medicion m1 = new Medicion(1, new Timestamp(1000000000L), 80.0, 1.75, 80, 110.0);
        Medicion m2 = new Medicion(1, new Timestamp(2000000000L), 77.0, 1.75, 72, 95.0);

        ComparacionBiometrica comp = service.compararMediciones(m1, m2);
        assertNotNull(comp);
        assertEquals(-3.0, comp.getDeltaPeso(), 0.001, "El peso debe haber bajado 3.0 kg");
        assertEquals(-8, comp.getDeltaFC(), "La frecuencia cardíaca debe haber bajado 8 bpm");
        assertEquals(-15.0, comp.getDeltaGlucosa(), 0.001, "La glucosa debe haber bajado 15.0 mg/dL");
        assertNotNull(comp.getDiagnostico());
    }

    @Test
    public void testCalcularIMC_AlturaYPesoEnMedicionesSeparadas() {
        List<Medicion> mediciones = new ArrayList<>();
        // Medición inicial donde solo registró su altura
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis() - 20000), null, 1.80, null, null));
        // Medición posterior donde solo registró su peso
        mediciones.add(new Medicion(1, new Timestamp(System.currentTimeMillis()), 75.0, null, 70, 90.0));

        double imc = service.calcularIMC(mediciones);
        double imcEsperado = 75.0 / (1.80 * 1.80);
        assertEquals(imcEsperado, imc, 0.01, "Debe calcular el IMC usando la última altura conocida y el último peso");
    }

    @Test
    public void testGenerarGraficaFiltrada_ManejoDeNulos() {
        assertDoesNotThrow(() -> {
            service.generarGraficaFiltrada(null, "TODAS");
            service.generarGraficaFiltrada(new ArrayList<>(), "PESO");
        }, "No debe lanzar excepciones cuando la lista de mediciones es nula o vacía");
    }
}
