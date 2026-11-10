// src/main/java/org/tusalud/repository/MedicionDAO.java
package org.tusalud.repository;

import org.tusalud.config.DatabaseConnection;
import org.tusalud.model.Medicion;
import org.tusalud.model.Recomendacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación JDBC de IMedicionDAO (Cumple R2, R4, R6, R7 y R9).
 * Incluye soporte transaccional explícito (commit/rollback) entre MEDICIONES y RECOMENDACIONES,
 * y soporte para presión arterial sistólica y diastólica.
 */
public class MedicionDAO implements IMedicionDAO {

    private void parametrizarMedicion(PreparedStatement stmt, Medicion m) throws SQLException {
        stmt.setInt(1, m.getIdUsuario());
        stmt.setTimestamp(2, m.getFechaHora() != null ? m.getFechaHora() : new Timestamp(System.currentTimeMillis()));

        if (m.getPeso() != null) stmt.setDouble(3, m.getPeso());
        else stmt.setNull(3, Types.DECIMAL);

        if (m.getAltura() != null) stmt.setDouble(4, m.getAltura());
        else stmt.setNull(4, Types.DECIMAL);

        if (m.getFrecuenciaCardiaca() != null) stmt.setInt(5, m.getFrecuenciaCardiaca());
        else stmt.setNull(5, Types.INTEGER);

        if (m.getGlucosaSangre() != null) stmt.setDouble(6, m.getGlucosaSangre());
        else stmt.setNull(6, Types.DECIMAL);

        if (m.getPresionSistolica() != null) stmt.setInt(7, m.getPresionSistolica());
        else stmt.setNull(7, Types.INTEGER);

        if (m.getPresionDiastolica() != null) stmt.setInt(8, m.getPresionDiastolica());
        else stmt.setNull(8, Types.INTEGER);
    }

    private Medicion mapearMedicion(ResultSet rs) throws SQLException {
        Medicion m = new Medicion();
        m.setIdMedicion(rs.getInt("id_medicion"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setFechaHora(rs.getTimestamp("fecha_hora"));

        double peso = rs.getDouble("peso");
        m.setPeso(rs.wasNull() ? null : peso);

        double altura = rs.getDouble("altura");
        m.setAltura(rs.wasNull() ? null : altura);

        int fc = rs.getInt("frecuencia_cardiaca");
        m.setFrecuenciaCardiaca(rs.wasNull() ? null : fc);

        double glucosa = rs.getDouble("glucosa_sangre");
        m.setGlucosaSangre(rs.wasNull() ? null : glucosa);

        int sistole = rs.getInt("presion_sistolica");
        m.setPresionSistolica(rs.wasNull() ? null : sistole);

        int diastole = rs.getInt("presion_diastolica");
        m.setPresionDiastolica(rs.wasNull() ? null : diastole);

        return m;
    }

    @Override
    public boolean agregar(Medicion m) throws SQLException {
        String sql = "INSERT INTO MEDICIONES (id_usuario, fecha_hora, peso, altura, frecuencia_cardiaca, glucosa_sangre, presion_sistolica, presion_diastolica) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            parametrizarMedicion(stmt, m);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        m.setIdMedicion(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Operación transaccional multi-tabla (Cumple R7).
     * Inserta la medición en MEDICIONES y sus recomendaciones asociadas en RECOMENDACIONES
     * dentro de una única transacción ACID con setAutoCommit(false), commit() y rollback().
     */
    @Override
    public boolean registrarConRecomendaciones(Medicion m, List<Recomendacion> recomendaciones) throws SQLException {
        String sqlMedicion = "INSERT INTO MEDICIONES (id_usuario, fecha_hora, peso, altura, frecuencia_cardiaca, glucosa_sangre, presion_sistolica, presion_diastolica) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlRecomendacion = "INSERT INTO RECOMENDACIONES (id_usuario, id_medicion, tipo, mensaje) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Inicio de transacción explícita (R7)

            try (PreparedStatement stmtMed = conn.prepareStatement(sqlMedicion, Statement.RETURN_GENERATED_KEYS)) {
                parametrizarMedicion(stmtMed, m);
                int filasMed = stmtMed.executeUpdate();
                if (filasMed == 0) {
                    conn.rollback();
                    return false;
                }
                try (ResultSet rsKeys = stmtMed.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        m.setIdMedicion(rsKeys.getInt(1));
                    }
                }
            }

            if (recomendaciones != null && !recomendaciones.isEmpty()) {
                try (PreparedStatement stmtRec = conn.prepareStatement(sqlRecomendacion, Statement.RETURN_GENERATED_KEYS)) {
                    for (Recomendacion r : recomendaciones) {
                        stmtRec.setInt(1, m.getIdUsuario());
                        stmtRec.setInt(2, m.getIdMedicion());
                        stmtRec.setString(3, r.getTipo());
                        stmtRec.setString(4, r.getMensaje());
                        stmtRec.executeUpdate();
                    }
                }
            }

            conn.commit(); // Confirmación atómica de ambas tablas (R7)
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // Reversión completa ante cualquier fallo (R7)
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<Medicion> obtenerPorUsuario(int idUsuario) throws SQLException {
        List<Medicion> lista = new ArrayList<>();
        String sql = "SELECT * FROM MEDICIONES WHERE id_usuario = ? ORDER BY fecha_hora ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMedicion(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public Medicion obtenerPorId(int idMedicion, int idUsuario) throws SQLException {
        String sql = "SELECT * FROM MEDICIONES WHERE id_medicion = ? AND id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMedicion);
            stmt.setInt(2, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMedicion(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean actualizar(Medicion m) throws SQLException {
        String sql = "UPDATE MEDICIONES SET peso = ?, altura = ?, frecuencia_cardiaca = ?, glucosa_sangre = ?, presion_sistolica = ?, presion_diastolica = ? WHERE id_medicion = ? AND id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (m.getPeso() != null) stmt.setDouble(1, m.getPeso());
            else stmt.setNull(1, Types.DECIMAL);

            if (m.getAltura() != null) stmt.setDouble(2, m.getAltura());
            else stmt.setNull(2, Types.DECIMAL);

            if (m.getFrecuenciaCardiaca() != null) stmt.setInt(3, m.getFrecuenciaCardiaca());
            else stmt.setNull(3, Types.INTEGER);

            if (m.getGlucosaSangre() != null) stmt.setDouble(4, m.getGlucosaSangre());
            else stmt.setNull(4, Types.DECIMAL);

            if (m.getPresionSistolica() != null) stmt.setInt(5, m.getPresionSistolica());
            else stmt.setNull(5, Types.INTEGER);

            if (m.getPresionDiastolica() != null) stmt.setInt(6, m.getPresionDiastolica());
            else stmt.setNull(6, Types.INTEGER);

            stmt.setInt(7, m.getIdMedicion());
            stmt.setInt(8, m.getIdUsuario());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int idMedicion, int idUsuario) throws SQLException {
        String sql = "DELETE FROM MEDICIONES WHERE id_medicion = ? AND id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMedicion);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Reporte estadístico mensual con COUNT, AVG y GROUP BY (Cumple R6).
     */
    @Override
    public List<Map<String, Object>> obtenerEvolucionMensualGlobal() throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(fecha_hora, '%Y-%m') AS periodo, " +
                     "       COUNT(id_medicion) AS cantidad_mediciones, " +
                     "       ROUND(AVG(peso), 2) AS promedio_peso, " +
                     "       ROUND(AVG(frecuencia_cardiaca), 1) AS promedio_fc, " +
                     "       ROUND(AVG(glucosa_sangre), 1) AS promedio_glucosa, " +
                     "       ROUND(AVG(presion_sistolica), 0) AS promedio_sistole, " +
                     "       ROUND(AVG(presion_diastolica), 0) AS promedio_diastole " +
                     "FROM MEDICIONES " +
                     "GROUP BY DATE_FORMAT(fecha_hora, '%Y-%m') " +
                     "ORDER BY periodo ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("periodo", rs.getString("periodo"));
                fila.put("cantidadMediciones", rs.getInt("cantidad_mediciones"));

                double pPeso = rs.getDouble("promedio_peso");
                fila.put("promedioPeso", rs.wasNull() ? 0.0 : pPeso);

                double pFc = rs.getDouble("promedio_fc");
                fila.put("promedioFc", rs.wasNull() ? 0.0 : pFc);

                double pGluc = rs.getDouble("promedio_glucosa");
                fila.put("promedioGlucosa", rs.wasNull() ? 0.0 : pGluc);

                double pSis = rs.getDouble("promedio_sistole");
                fila.put("promedioSistole", rs.wasNull() ? 0.0 : pSis);

                double pDia = rs.getDouble("promedio_diastole");
                fila.put("promedioDiastole", rs.wasNull() ? 0.0 : pDia);

                lista.add(fila);
            }
        }
        return lista;
    }
}
