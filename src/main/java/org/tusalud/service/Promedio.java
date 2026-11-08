package org.tusalud.service;

import org.tusalud.model.Medicion;
import java.util.List;

public interface Promedio {
    double calcularCardiaco(List<Medicion> mediciones);
    double calcularGlucosa(List<Medicion> mediciones);
    double calcularIMC(List<Medicion> mediciones);
}
