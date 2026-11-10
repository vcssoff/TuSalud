// src/main/java/org/tusalud/repository/IUsuarioDAO.java
package org.tusalud.repository;

import org.tusalud.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz de acceso a datos para la entidad Usuario (Cumple R4: Arquitectura en capas con interfaces).
 */
public interface IUsuarioDAO {
    boolean registrar(Usuario usuario, String contraseniaPlana) throws SQLException;
    Usuario iniciarSesion(String correo, String contraseniaPlana) throws SQLException;
    Usuario obtenerPorId(int idUsuario) throws SQLException;
    boolean actualizarPerfilMedico(int idUsuario, String perfil) throws SQLException;
    boolean actualizarDatosUsuario(Usuario usuario) throws SQLException;
    boolean eliminar(int idUsuario) throws SQLException;
    boolean existeCorreo(String correo) throws SQLException;
    List<Usuario> buscarPacientes(String filtro) throws SQLException;
    Map<String, Object> obtenerHistoriaClinicaConsolidada(int idUsuario) throws SQLException;
    List<Map<String, Object>> obtenerReporteGlobalPorPerfil() throws SQLException;
}
