package org.tusalud.repository;

import org.mindrot.jbcrypt.BCrypt;
import org.tusalud.config.DatabaseConnection;
import org.tusalud.model.Usuario;

import java.sql.*;

public class UsuarioDAO {

    public boolean registrar(Usuario usuario, String contraseniaPlana) throws SQLException {
        String sql = "INSERT INTO USUARIOS (nombre, correo, contrasenia_hash, fecha_nacimiento, genero) VALUES (?, ?, ?, ?, ?)";
        String hash = BCrypt.hashpw(contraseniaPlana, BCrypt.gensalt());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, hash);
            stmt.setDate(4, usuario.getFechaNacimiento());
            stmt.setString(5, usuario.getGenero());

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

    public Usuario iniciarSesion(String correo, String contraseniaPlana) throws SQLException {
        String sql = "SELECT * FROM USUARIOS WHERE correo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("contrasenia_hash");
                    if (BCrypt.checkpw(contraseniaPlana, hash)) {
                        Usuario u = new Usuario();
                        u.setIdUsuario(rs.getInt("id_usuario"));
                        u.setNombre(rs.getString("nombre"));
                        u.setCorreo(rs.getString("correo"));
                        u.setContraseniaHash(hash);
                        u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                        u.setGenero(rs.getString("genero"));
                        u.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                        try {
                            u.setPerfilMedico(rs.getString("perfil_medico"));
                        } catch (Exception ignored) {}
                        return u;
                    }
                }
            }
        }
        return null;
    }

    public boolean actualizarPerfilMedico(int idUsuario, String perfil) throws SQLException {
        String sql = "UPDATE USUARIOS SET perfil_medico = ? WHERE id_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, perfil);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

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
}
