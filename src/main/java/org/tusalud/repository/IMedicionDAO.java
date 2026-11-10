// src/main/java/org/tusalud/repository/IMedicionDAO.java
package org.tusalud.repository;

import org.tusalud.model.Medicion;
import org.tusalud.model.Recomendacion;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz de acceso a datos para la entidad Medicion (Cumple R4: Arquitectura en capas con interfaces).
 */
public interface IMedicionDAO {
    boolean agregar(Medicion m) throws SQLException;
    boolean registrarConRecomendaciones(Medicion m, List<Recomendacion> recomendaciones) throws SQLException;
    List<Medicion> obtenerPorUsuario(int idUsuario) throws SQLException;
    Medicion obtenerPorId(int idMedicion, int idUsuario) throws SQLException;
    boolean actualizar(Medicion m) throws SQLException;
    boolean eliminar(int idMedicion, int idUsuario) throws SQLException;
    List<Map<String, Object>> obtenerEvolucionMensualGlobal() throws SQLException;
}
