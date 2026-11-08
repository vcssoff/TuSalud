package org.tusalud.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.tusalud.model.Medicion;
import org.tusalud.model.Recomendacion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AnalisisService implements Promedio {

    @Override
    public double calcularCardiaco(List<Medicion> mediciones) {
        if (mediciones == null || mediciones.isEmpty()) return 0.0;
        int count = 0;
        double sum = 0;
        for (Medicion m : mediciones) {
            if (m.getFrecuenciaCardiaca() != null) {
                sum += m.getFrecuenciaCardiaca();
                count++;
            }
        }
        return count > 0 ? (sum / count) : 0.0;
    }

    @Override
    public double calcularGlucosa(List<Medicion> mediciones) {
        if (mediciones == null || mediciones.isEmpty()) return 0.0;
        int count = 0;
        double sum = 0;
        for (Medicion m : mediciones) {
            if (m.getGlucosaSangre() != null) {
                sum += m.getGlucosaSangre();
                count++;
            }
        }
        return count > 0 ? (sum / count) : 0.0;
    }

    @Override
    public double calcularIMC(List<Medicion> mediciones) {
        if (mediciones == null || mediciones.isEmpty()) return 0.0;
        Double ultimoPeso = null;
        Double ultimaAltura = null;
        for (int i = mediciones.size() - 1; i >= 0; i--) {
            Medicion m = mediciones.get(i);
            if (ultimoPeso == null && m.getPeso() != null && m.getPeso() > 0) {
                ultimoPeso = m.getPeso();
            }
            if (ultimaAltura == null && m.getAltura() != null && m.getAltura() > 0) {
                ultimaAltura = m.getAltura();
            }
            if (ultimoPeso != null && ultimaAltura != null) break;
        }
        if (ultimoPeso != null && ultimaAltura != null && ultimaAltura > 0) {
            return ultimoPeso / (ultimaAltura * ultimaAltura);
        }
        return 0.0;
    }

    public List<Recomendacion> generarRecomendaciones(int idUsuario, List<Medicion> mediciones) {
        List<Recomendacion> recs = new ArrayList<>();
        if (mediciones == null || mediciones.isEmpty()) {
            recs.add(new Recomendacion(idUsuario, null, "GENERAL", "Comienza registrando tus mediciones biométricas para recibir un análisis personalizado."));
            return recs;
        }

        double imc = calcularIMC(mediciones);
        if (imc > 0) {
            if (imc < 18.5) {
                recs.add(new Recomendacion(idUsuario, null, "IMC", String.format("Tu IMC es %.1f (Bajo peso). Considera una consulta nutricional para un plan calórico adecuado.", imc)));
            } else if (imc < 24.9) {
                recs.add(new Recomendacion(idUsuario, null, "IMC", String.format("Tu IMC es %.1f (Peso normal). ¡Excelente! Mantén tus hábitos saludables.", imc)));
            } else if (imc < 29.9) {
                recs.add(new Recomendacion(idUsuario, null, "IMC", String.format("Tu IMC es %.1f (Sobrepeso). Se recomienda actividad física regular y cuidar el balance calórico.", imc)));
            } else {
                recs.add(new Recomendacion(idUsuario, null, "IMC", String.format("Tu IMC es %.1f (Obesidad). Consulta a un profesional de la salud para seguimiento.", imc)));
            }
        }

        double fcPromedio = calcularCardiaco(mediciones);
        if (fcPromedio > 0) {
            if (fcPromedio < 60) {
                recs.add(new Recomendacion(idUsuario, null, "CARDIACO", String.format("Frecuencia cardíaca promedio baja (%.0f bpm). Si no eres atleta de alto rendimiento, consulta a tu médico.", fcPromedio)));
            } else if (fcPromedio <= 100) {
                recs.add(new Recomendacion(idUsuario, null, "CARDIACO", String.format("Frecuencia cardíaca promedio normal (%.0f bpm en reposo). Ritmo adecuado.", fcPromedio)));
            } else {
                recs.add(new Recomendacion(idUsuario, null, "CARDIACO", String.format("Frecuencia cardíaca promedio elevada (%.0f bpm). Intenta reducir fuentes de estrés o consulta al cardiólogo.", fcPromedio)));
            }
        }

        double glucosaPromedio = calcularGlucosa(mediciones);
        if (glucosaPromedio > 0) {
            if (glucosaPromedio < 70) {
                recs.add(new Recomendacion(idUsuario, null, "GLUCOSA", String.format("Glucosa promedio baja (%.1f mg/dL). Monitorea posibles síntomas de hipoglucemia.", glucosaPromedio)));
            } else if (glucosaPromedio <= 100) {
                recs.add(new Recomendacion(idUsuario, null, "GLUCOSA", String.format("Glucosa promedio normal en ayunas (%.1f mg/dL). Niveles saludables.", glucosaPromedio)));
            } else if (glucosaPromedio <= 125) {
                recs.add(new Recomendacion(idUsuario, null, "GLUCOSA", String.format("Glucosa promedio en rango de prediabetes (%.1f mg/dL). Cuida la ingesta de azúcares refinados.", glucosaPromedio)));
            } else {
                recs.add(new Recomendacion(idUsuario, null, "GLUCOSA", String.format("Glucosa promedio elevada (%.1f mg/dL). Es importante consultar a tu médico para una evaluación.", glucosaPromedio)));
            }
        }

        return recs;
    }

    public JFreeChart generarGraficaEvolucion(List<Medicion> mediciones) {
        return generarGraficaFiltrada(mediciones, "TODAS");
    }

    public JFreeChart generarGraficaFiltrada(List<Medicion> mediciones, String tipoFiltro) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");

        if (mediciones != null) {
            for (Medicion m : mediciones) {
                if (m.getFechaHora() == null) continue;
                String fecha = sdf.format(m.getFechaHora()) + " (#" + m.getIdMedicion() + ")";

                if ("TODAS".equalsIgnoreCase(tipoFiltro) || "PESO".equalsIgnoreCase(tipoFiltro)) {
                    if (m.getPeso() != null) {
                        dataset.addValue(m.getPeso(), "Peso (kg)", fecha);
                    }
                }
                if ("TODAS".equalsIgnoreCase(tipoFiltro) || "CARDIACO".equalsIgnoreCase(tipoFiltro)) {
                    if (m.getFrecuenciaCardiaca() != null) {
                        dataset.addValue(m.getFrecuenciaCardiaca(), "Pulsaciones (bpm)", fecha);
                    }
                }
                if ("TODAS".equalsIgnoreCase(tipoFiltro) || "GLUCOSA".equalsIgnoreCase(tipoFiltro)) {
                    if (m.getGlucosaSangre() != null) {
                        dataset.addValue(m.getGlucosaSangre(), "Glucosa (mg/dL)", fecha);
                    }
                }
                if ("IMC".equalsIgnoreCase(tipoFiltro)) {
                    if (m.getPeso() != null && m.getAltura() != null && m.getAltura() > 0) {
                        double imc = m.getPeso() / (m.getAltura() * m.getAltura());
                        dataset.addValue(Math.round(imc * 10.0) / 10.0, "IMC", fecha);
                    }
                }
            }
        }

        String titulo = "Evolución de Mediciones Biométricas";
        String etiquetaY = "Valor";

        if ("PESO".equalsIgnoreCase(tipoFiltro)) {
            titulo = "Evolución del Peso Corporal";
            etiquetaY = "Kilogramos (kg)";
        } else if ("CARDIACO".equalsIgnoreCase(tipoFiltro)) {
            titulo = "Evolución de Frecuencia Cardíaca";
            etiquetaY = "Pulsaciones por minuto (bpm)";
        } else if ("GLUCOSA".equalsIgnoreCase(tipoFiltro)) {
            titulo = "Evolución de Glucosa en Sangre";
            etiquetaY = "Miligramos por decilitro (mg/dL)";
        } else if ("IMC".equalsIgnoreCase(tipoFiltro)) {
            titulo = "Evolución del Índice de Masa Corporal (IMC)";
            etiquetaY = "Índice (kg/m²)";
        }

        return ChartFactory.createLineChart(
                titulo,
                "Fecha y Hora",
                etiquetaY,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    public org.tusalud.model.ComparacionBiometrica compararMediciones(Medicion m1, Medicion m2) {
        if (m1 == null || m2 == null) return null;

        Medicion ant = m1;
        Medicion post = m2;
        if (m1.getFechaHora() != null && m2.getFechaHora() != null && m1.getFechaHora().after(m2.getFechaHora())) {
            ant = m2;
            post = m1;
        }

        org.tusalud.model.ComparacionBiometrica comp = new org.tusalud.model.ComparacionBiometrica();
        comp.setMedicionAnterior(ant);
        comp.setMedicionPosterior(post);

        comp.setPesoAnterior(ant.getPeso());
        comp.setPesoPosterior(post.getPeso());
        if (ant.getPeso() != null && post.getPeso() != null) {
            comp.setDeltaPeso(post.getPeso() - ant.getPeso());
        }

        Double imcAnt = (ant.getPeso() != null && ant.getAltura() != null && ant.getAltura() > 0)
                ? (ant.getPeso() / (ant.getAltura() * ant.getAltura())) : null;
        Double imcPost = (post.getPeso() != null && post.getAltura() != null && post.getAltura() > 0)
                ? (post.getPeso() / (post.getAltura() * post.getAltura())) : null;
        comp.setImcAnterior(imcAnt);
        comp.setImcPosterior(imcPost);
        if (imcAnt != null && imcPost != null) {
            comp.setDeltaIMC(imcPost - imcAnt);
        }

        comp.setFcAnterior(ant.getFrecuenciaCardiaca());
        comp.setFcPosterior(post.getFrecuenciaCardiaca());
        if (ant.getFrecuenciaCardiaca() != null && post.getFrecuenciaCardiaca() != null) {
            comp.setDeltaFC(post.getFrecuenciaCardiaca() - ant.getFrecuenciaCardiaca());
        }

        comp.setGlucosaAnterior(ant.getGlucosaSangre());
        comp.setGlucosaPosterior(post.getGlucosaSangre());
        if (ant.getGlucosaSangre() != null && post.getGlucosaSangre() != null) {
            comp.setDeltaGlucosa(post.getGlucosaSangre() - ant.getGlucosaSangre());
        }

        StringBuilder diag = new StringBuilder();
        if (comp.getDeltaPeso() != null) {
            if (comp.getDeltaPeso() > 0) diag.append(String.format("• Peso: Aumento de +%.2f kg.\n", comp.getDeltaPeso()));
            else if (comp.getDeltaPeso() < 0) diag.append(String.format("• Peso: Reducción de %.2f kg.\n", comp.getDeltaPeso()));
            else diag.append("• Peso: Sin variación.\n");
        }
        if (comp.getDeltaIMC() != null) {
            diag.append(String.format("• IMC: Varió de %.1f a %.1f (Δ: %+.1f).\n", imcAnt, imcPost, comp.getDeltaIMC()));
        }
        if (comp.getDeltaFC() != null) {
            diag.append(String.format("• Frecuencia Cardíaca: Variación de %+d bpm.\n", comp.getDeltaFC()));
        }
        if (comp.getDeltaGlucosa() != null) {
            diag.append(String.format("• Glucosa: Variación de %+.1f mg/dL.\n", comp.getDeltaGlucosa()));
        }
        if (diag.length() == 0) {
            diag.append("No hay suficientes variables coincidentes para calcular diferencias.");
        }
        comp.setDiagnostico(diag.toString());

        return comp;
    }
}
