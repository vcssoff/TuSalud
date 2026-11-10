// src/main/java/org/tusalud/repository/RecomendacionDAO.java
package org.tusalud.repository;

import org.tusalud.config.DatabaseConnection;
import org.tusalud.model.Recomendacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de IRecomendacionDAO con CRUD completo (Cumple R2, R4 y R9).
 */
public class RecomendacionDAO implements IRecomendacionDAO {

    @Override
    public boolean guardar(Recomendacion r) throws SQLException {
        String sql = "INSERT INTO RECOMENDACIONES (id_usuario, id_medicion, tipo, mensaje) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, r.getIdUsuario());
            if (r.getIdMedicion() != null) stmt.setInt(2, r.getIdMedicion());
            else stmt.setNull(2, Types.INTEGER);

            stmt.setString(3, r.getTipo());
            stmt.setString(4, r.getMensaje());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        r.setIdRecomendacion(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Recomendacion> obtenerPorUsuario(int idUsuario) throws SQLException {
        List<Recomendacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM RECOMENDACIONES WHERE id_usuario = ? ORDER BY fecha_generacion DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Recomendacion r = new Recomendacion();
                    r.setIdRecomendacion(rs.getInt("id_recomendacion"));
                    r.setIdUsuario(rs.getInt("id_usuario"));

                    int idMed = rs.getInt("id_medicion");
                    r.setIdMedicion(rs.wasNull() ? null : idMed);

                    r.setTipo(rs.getString("tipo"));
                    r.setMensaje(rs.getString("mensaje"));
                    r.setFechaGeneracion(rs.getTimestamp("fecha_generacion"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Recomendacion r) throws SQLException {
        String sql = "UPDATE RECOMENDACIONES SET tipo = ?, mensaje = ? WHERE id_recomendacion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getTipo());
            stmt.setString(2, r.getMensaje());
            stmt.setInt(3, r.getIdRecomendacion());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int idRecomendacion, int idUsuario) throws SQLException {
        String sql = "DELETE FROM RECOMENDACIONES WHERE id_recomendacion = ? AND id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRecomendacion);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminarPorMedico(int idRecomendacion) throws SQLException {
        String sql = "DELETE FROM RECOMENDACIONES WHERE id_recomendacion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRecomendacion);
            return stmt.executeUpdate() > 0;
        }
    }
}
