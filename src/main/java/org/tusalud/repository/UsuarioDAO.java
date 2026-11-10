// src/main/java/org/tusalud/repository/UsuarioDAO.java
package org.tusalud.repository;

import org.mindrot.jbcrypt.BCrypt;
import org.tusalud.config.DatabaseConnection;
import org.tusalud.model.Medico;
import org.tusalud.model.Paciente;
import org.tusalud.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación JDBC de IUsuarioDAO (Cumple R2, R3, R4, R5, R6 y R9).
 * Todas las consultas utilizan PreparedStatement contra inyección SQL.
 */
public class UsuarioDAO implements IUsuarioDAO {

    private Usuario mapearUsuarioPolimorfico(ResultSet rs) throws SQLException {
        int idRol = 1;
        String rolNombre = "PACIENTE";
        try {
            idRol = rs.getInt("id_rol");
            if (rs.wasNull() || idRol == 0) idRol = 1;
        } catch (SQLException ignored) {}
        try {
            String rn = rs.getString("rol_nombre");
            if (rn != null && !rn.isEmpty()) rolNombre = rn;
            else if (idRol == 2) rolNombre = "MEDICO";
            else if (idRol == 3) rolNombre = "ADMIN";
        } catch (SQLException ignored) {
            if (idRol == 2) rolNombre = "MEDICO";
            else if (idRol == 3) rolNombre = "ADMIN";
        }

        Usuario u;
        if (idRol == 2 || idRol == 3 || "MEDICO".equalsIgnoreCase(rolNombre) || "ADMIN".equalsIgnoreCase(rolNombre)) {
            Medico med = new Medico();
            try { med.setEspecialidad(rs.getString("especialidad")); } catch (SQLException ignored) {}
            try { med.setMatricula(rs.getString("matricula")); } catch (SQLException ignored) {}
            u = med;
        } else {
            u = new Paciente();
        }

        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setIdRol(idRol);
        u.setRolNombre(rolNombre);
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        try { u.setContraseniaHash(rs.getString("contrasenia_hash")); } catch (SQLException ignored) {}
        u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
        u.setGenero(rs.getString("genero"));
        try { u.setFechaRegistro(rs.getTimestamp("fecha_registro")); } catch (SQLException ignored) {}
        try { u.setPerfilMedico(rs.getString("perfil_medico")); } catch (SQLException ignored) {}

        return u;
    }

    @Override
    public boolean registrar(Usuario usuario, String contraseniaPlana) throws SQLException {
        String sql = "INSERT INTO USUARIOS (id_rol, nombre, correo, contrasenia_hash, fecha_nacimiento, genero, perfil_medico, especialidad, matricula) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String hash = BCrypt.hashpw(contraseniaPlana, BCrypt.gensalt());

        int idRol = usuario.getIdRol() > 0 ? usuario.getIdRol() : 1;
        String especialidad = (usuario instanceof Medico) ? ((Medico) usuario).getEspecialidad() : null;
        String matricula = (usuario instanceof Medico) ? ((Medico) usuario).getMatricula() : null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, idRol);
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, hash);
            stmt.setDate(5, usuario.getFechaNacimiento());
            stmt.setString(6, usuario.getGenero());
            stmt.setString(7, usuario.getPerfilMedico());
            stmt.setString(8, especialidad);
            stmt.setString(9, matricula);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setIdUsuario(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Usuario iniciarSesion(String correo, String contraseniaPlana) throws SQLException {
        String sql = "SELECT u.*, r.nombre AS rol_nombre " +
                     "FROM USUARIOS u " +
                     "LEFT JOIN ROLES r ON u.id_rol = r.id_rol " +
                     "WHERE u.correo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("contrasenia_hash");
                    if (hash != null && BCrypt.checkpw(contraseniaPlana, hash)) {
                        return mapearUsuarioPolimorfico(rs);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Usuario obtenerPorId(int idUsuario) throws SQLException {
        String sql = "SELECT u.*, r.nombre AS rol_nombre " +
                     "FROM USUARIOS u " +
                     "LEFT JOIN ROLES r ON u.id_rol = r.id_rol " +
                     "WHERE u.id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuarioPolimorfico(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean actualizarPerfilMedico(int idUsuario, String perfil) throws SQLException {
        String sql = "UPDATE USUARIOS SET perfil_medico = ? WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, perfil);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizarDatosUsuario(Usuario usuario) throws SQLException {
        String sql = "UPDATE USUARIOS SET nombre = ?, fecha_nacimiento = ?, genero = ?, perfil_medico = ? WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setDate(2, usuario.getFechaNacimiento());
            stmt.setString(3, usuario.getGenero());
            stmt.setString(4, usuario.getPerfilMedico());
            stmt.setInt(5, usuario.getIdUsuario());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int idUsuario) throws SQLException {
        String sql = "DELETE FROM USUARIOS WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existeCorreo(String correo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USUARIOS WHERE correo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<Usuario> buscarPacientes(String filtro) throws SQLException {
        List<Usuario> pacientes = new ArrayList<>();
        String termino = (filtro == null) ? "" : "%" + filtro.trim() + "%";
        String sql = "SELECT u.*, r.nombre AS rol_nombre " +
                     "FROM USUARIOS u " +
                     "LEFT JOIN ROLES r ON u.id_rol = r.id_rol " +
                     "WHERE (u.id_rol = 1 OR u.id_rol IS NULL) " +
                     "AND (u.nombre LIKE ? OR u.correo LIKE ? OR IFNULL(u.perfil_medico, '') LIKE ?) " +
                     "ORDER BY u.fecha_registro DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, termino);
            stmt.setString(2, termino);
            stmt.setString(3, termino);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario u = mapearUsuarioPolimorfico(rs);
                    u.setContraseniaHash(null);
                    pacientes.add(u);
                }
            }
        }
        return pacientes;
    }

    /**
     * Consulta avanzada con JOIN entre USUARIOS, ROLES, MEDICIONES y RECOMENDACIONES (Cumple R6).
     * Retorna la ficha médica consolidada de un paciente junto con su historial clínico.
     */
    @Override
    public Map<String, Object> obtenerHistoriaClinicaConsolidada(int idUsuario) throws SQLException {
        Map<String, Object> ficha = new LinkedHashMap<>();
        List<Map<String, Object>> registrosJoin = new ArrayList<>();

        String sql = "SELECT u.id_usuario, u.nombre, u.correo, u.fecha_nacimiento, u.genero, u.perfil_medico, " +
                     "       r.nombre AS rol_nombre, " +
                     "       m.id_medicion, m.fecha_hora, m.peso, m.altura, m.frecuencia_cardiaca, m.glucosa_sangre, " +
                     "       rec.id_recomendacion, rec.tipo AS rec_tipo, rec.mensaje AS rec_mensaje, rec.fecha_generacion " +
                     "FROM USUARIOS u " +
                     "INNER JOIN ROLES r ON u.id_rol = r.id_rol " +
                     "LEFT JOIN MEDICIONES m ON u.id_usuario = m.id_usuario " +
                     "LEFT JOIN RECOMENDACIONES rec ON m.id_medicion = rec.id_medicion " +
                     "WHERE u.id_usuario = ? " +
                     "ORDER BY m.fecha_hora DESC, rec.fecha_generacion DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean headerLoaded = false;
                while (rs.next()) {
                    if (!headerLoaded) {
                        ficha.put("idUsuario", rs.getInt("id_usuario"));
                        ficha.put("nombre", rs.getString("nombre"));
                        ficha.put("correo", rs.getString("correo"));
                        ficha.put("fechaNacimiento", rs.getDate("fecha_nacimiento"));
                        ficha.put("genero", rs.getString("genero"));
                        ficha.put("perfilMedico", rs.getString("perfil_medico"));
                        ficha.put("rolNombre", rs.getString("rol_nombre"));
                        headerLoaded = true;
                    }
                    int idMed = rs.getInt("id_medicion");
                    if (!rs.wasNull()) {
                        Map<String, Object> fila = new LinkedHashMap<>();
                        fila.put("idMedicion", idMed);
                        fila.put("fechaHora", rs.getTimestamp("fecha_hora"));
                        double peso = rs.getDouble("peso");
                        fila.put("peso", rs.wasNull() ? null : peso);
                        double altura = rs.getDouble("altura");
                        fila.put("altura", rs.wasNull() ? null : altura);
                        int fc = rs.getInt("frecuencia_cardiaca");
                        fila.put("frecuenciaCardiaca", rs.wasNull() ? null : fc);
                        double gluc = rs.getDouble("glucosa_sangre");
                        fila.put("glucosaSangre", rs.wasNull() ? null : gluc);

                        int idRec = rs.getInt("id_recomendacion");
                        fila.put("idRecomendacion", rs.wasNull() ? null : idRec);
                        fila.put("recomendacionTipo", rs.getString("rec_tipo"));
                        fila.put("recomendacionMensaje", rs.getString("rec_mensaje"));
                        registrosJoin.add(fila);
                    }
                }
            }
        }
        ficha.put("historialJoin", registrosJoin);
        return ficha;
    }

    /**
     * Consulta avanzada de reporte estadístico con COUNT, AVG y GROUP BY (Cumple R6).
     * Agrupa pacientes y mediciones por perfil médico para el panel del médico.
     */
    @Override
    public List<Map<String, Object>> obtenerReporteGlobalPorPerfil() throws SQLException {
        List<Map<String, Object>> reporte = new ArrayList<>();
        String sql = "SELECT IFNULL(u.perfil_medico, 'SIN_CUESTIONARIO') AS perfil_medico, " +
                     "       COUNT(DISTINCT u.id_usuario) AS total_pacientes, " +
                     "       COUNT(m.id_medicion) AS total_mediciones, " +
                     "       ROUND(AVG(m.peso), 2) AS promedio_peso, " +
                     "       ROUND(AVG(m.frecuencia_cardiaca), 1) AS promedio_fc, " +
                     "       ROUND(AVG(m.glucosa_sangre), 1) AS promedio_glucosa " +
                     "FROM USUARIOS u " +
                     "LEFT JOIN MEDICIONES m ON u.id_usuario = m.id_usuario " +
                     "WHERE u.id_rol = 1 OR u.id_rol IS NULL " +
                     "GROUP BY IFNULL(u.perfil_medico, 'SIN_CUESTIONARIO') " +
                     "ORDER BY total_mediciones DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("perfilMedico", rs.getString("perfil_medico"));
                fila.put("totalPacientes", rs.getInt("total_pacientes"));
                fila.put("totalMediciones", rs.getInt("total_mediciones"));

                double avgPeso = rs.getDouble("promedio_peso");
                fila.put("promedioPeso", rs.wasNull() ? 0.0 : avgPeso);

                double avgFc = rs.getDouble("promedio_fc");
                fila.put("promedioFc", rs.wasNull() ? 0.0 : avgFc);

                double avgGlucosa = rs.getDouble("promedio_glucosa");
                fila.put("promedioGlucosa", rs.wasNull() ? 0.0 : avgGlucosa);

                reporte.add(fila);
            }
        }
        return reporte;
    }
}
