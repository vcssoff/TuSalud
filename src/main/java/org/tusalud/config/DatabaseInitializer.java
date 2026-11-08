package org.tusalud.config;

import org.tusalud.model.Medicion;
import org.tusalud.repository.MedicionDAO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static boolean inicializarTablas() {
        // Asegurar que la base de datos exista antes de intentar conectar a ella
        try (Connection rootConn = DatabaseConnection.getConnectionSinBD();
             Statement stmtRoot = rootConn.createStatement()) {
            stmtRoot.execute("CREATE DATABASE IF NOT EXISTS railway;");
        } catch (Exception e) {
            System.err.println("Aviso al verificar/crear base de datos: " + e.getMessage());
        }

        try (Connection conn = DatabaseConnection.getConnection();
             InputStream is = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql")) {

            if (is == null) {
                System.err.println("No se encontró /db/schema.sql en resources.");
                return false;
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String[] sentencias = sql.split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sentencia : sentencias) {
                    String limpia = sentencia.trim();
                    if (!limpia.isEmpty()) {
                        stmt.execute(limpia);
                    }
                }
                // Si la tabla USUARIOS ya existía sin perfil_medico, añadir la columna
                try {
                    stmt.execute("ALTER TABLE USUARIOS ADD COLUMN perfil_medico VARCHAR(100) NULL;");
                } catch (Exception ignored) {}
            }
            return true;
        } catch (Exception e) {
            System.err.println("Aviso al inicializar tablas: " + e.getMessage());
            return false;
        }
    }

    public static int sembrarDatosDemo(int idUsuario) {
        return sembrarDatosDemo(idUsuario, null);
    }

    public static int sembrarDatosDemo(int idUsuario, String perfilMedico) {
        MedicionDAO dao = new MedicionDAO();
        long ahora = System.currentTimeMillis();
        long unDia = 86400000L;

        Medicion[] demos = new Medicion[]{
                new Medicion(idUsuario, new Timestamp(ahora - (28 * unDia)), 82.5, 1.76, 88, 118.0),
                new Medicion(idUsuario, new Timestamp(ahora - (21 * unDia)), 81.7, 1.76, 83, 109.0),
                new Medicion(idUsuario, new Timestamp(ahora - (14 * unDia)), 80.9, 1.76, 79, 102.0),
                new Medicion(idUsuario, new Timestamp(ahora - (7 * unDia)), 80.1, 1.76, 75, 97.0),
                new Medicion(idUsuario, new Timestamp(ahora), 79.4, 1.76, 71, 93.0)
        };

        int insertados = 0;
        for (Medicion m : demos) {
            try {
                org.tusalud.service.PerfilMedicoService.sanitizarMedicion(m, perfilMedico);
                if (dao.agregar(m)) {
                    insertados++;
                }
            } catch (Exception ignored) {}
        }
        return insertados;
    }
}
