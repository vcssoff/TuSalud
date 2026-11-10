// src/main/java/org/tusalud/repository/IRecomendacionDAO.java
package org.tusalud.repository;

import org.tusalud.model.Recomendacion;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Recomendacion (Cumple R4: Arquitectura en capas con interfaces).
 */
public interface IRecomendacionDAO {
    boolean guardar(Recomendacion r) throws SQLException;
    List<Recomendacion> obtenerPorUsuario(int idUsuario) throws SQLException;
    boolean actualizar(Recomendacion r) throws SQLException;
    boolean eliminar(int idRecomendacion, int idUsuario) throws SQLException;
    boolean eliminarPorMedico(int idRecomendacion) throws SQLException;
}
