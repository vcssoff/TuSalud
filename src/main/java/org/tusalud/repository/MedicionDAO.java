package org.tusalud.repository;

import org.tusalud.config.DatabaseConnection;
import org.tusalud.model.Medicion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicionDAO {

    public boolean agregar(Medicion m) throws SQLException {
        String sql = "INSERT INTO MEDICIONES (id_usuario, fecha_hora, peso, altura, frecuencia_cardiaca, glucosa_sangre) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, m.getIdUsuario());
            stmt.setTimestamp(2, m.getFechaHora());

            if (m.getPeso() != null) stmt.setDouble(3, m.getPeso());
            else stmt.setNull(3, Types.DECIMAL);

            if (m.getAltura() != null) stmt.setDouble(4, m.getAltura());
            else stmt.setNull(4, Types.DECIMAL);

            if (m.getFrecuenciaCardiaca() != null) stmt.setInt(5, m.getFrecuenciaCardiaca());
            else stmt.setNull(5, Types.INTEGER);

            if (m.getGlucosaSangre() != null) stmt.setDouble(6, m.getGlucosaSangre());
            else stmt.setNull(6, Types.DECIMAL);

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

    public List<Medicion> obtenerPorUsuario(int idUsuario) throws SQLException {
        List<Medicion> lista = new ArrayList<>();
        String sql = "SELECT * FROM MEDICIONES WHERE id_usuario = ? ORDER BY fecha_hora ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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

                    lista.add(m);
                }
            }
        }
        return lista;
    }

    public boolean actualizar(Medicion m) throws SQLException {
        String sql = "UPDATE MEDICIONES SET peso = ?, altura = ?, frecuencia_cardiaca = ?, glucosa_sangre = ? WHERE id_medicion = ? AND id_usuario = ?";
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

            stmt.setInt(5, m.getIdMedicion());
            stmt.setInt(6, m.getIdUsuario());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idMedicion, int idUsuario) throws SQLException {
        String sql = "DELETE FROM MEDICIONES WHERE id_medicion = ? AND id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMedicion);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }
}
