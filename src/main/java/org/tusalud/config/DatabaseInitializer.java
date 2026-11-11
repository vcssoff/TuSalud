// src/main/java/org/tusalud/config/DatabaseInitializer.java
package org.tusalud.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Inicializador automático del esquema MySQL (Cumple R1).
 * Garantiza que las 4 tablas (ROLES, USUARIOS, MEDICIONES, RECOMENDACIONES) existan
 * con sus claves primarias, foráneas y roles base, sin insertar usuarios ni mediciones de prueba.
 */
public class DatabaseInitializer {

    public static boolean inicializarTablas() {
        String dbName = DatabaseConnection.getEnv("DB_NAME", "railway");
        if (dbName.matches("[A-Za-z0-9_]+")) {
            try (Connection rootConn = DatabaseConnection.getConnectionSinBD();
                 Statement stmtRoot = rootConn.createStatement()) {
                stmtRoot.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "`;");
            } catch (Exception e) {
                System.err.println("Aviso al verificar/crear base de datos: " + e.getMessage());
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             InputStream is = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql")) {

            if (is == null) {
                System.err.println("No se encontró /db/schema.sql en resources.");
                return false;
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            String[] sentencias = sql.split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sentencia : sentencias) {
                    String limpia = sentencia.trim();
                    if (!limpia.isEmpty()) {
                        stmt.execute(limpia);
                    }
                }

                // Asegurar únicamente los 3 roles del catálogo estructural (R1 y R3)
                stmt.execute("INSERT IGNORE INTO ROLES (id_rol, nombre, descripcion) VALUES " +
                        "(1, 'PACIENTE', 'Paciente que gestiona sus mediciones biométricas'), " +
                        "(2, 'MEDICO', 'Profesional médico con acceso a reportes globales y búsqueda de pacientes'), " +
                        "(3, 'ADMIN', 'Administrador del sistema');");

                // Migraciones seguras para bases de datos preexistentes
                try { stmt.execute("ALTER TABLE USUARIOS ADD COLUMN perfil_medico VARCHAR(100) NULL;"); } catch (Exception ignored) {}
                try { stmt.execute("ALTER TABLE USUARIOS ADD COLUMN id_rol INT NOT NULL DEFAULT 1;"); } catch (Exception ignored) {}
                try { stmt.execute("ALTER TABLE USUARIOS ADD COLUMN especialidad VARCHAR(100) NULL;"); } catch (Exception ignored) {}
                try { stmt.execute("ALTER TABLE USUARIOS ADD COLUMN matricula VARCHAR(50) NULL;"); } catch (Exception ignored) {}
                try {
                    stmt.execute("ALTER TABLE USUARIOS ADD CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES ROLES(id_rol);");
                } catch (Exception ignored) {}
                try { stmt.execute("ALTER TABLE MEDICIONES ADD COLUMN presion_sistolica INT NULL;"); } catch (Exception ignored) {}
                try { stmt.execute("ALTER TABLE MEDICIONES ADD COLUMN presion_diastolica INT NULL;"); } catch (Exception ignored) {}
            }

            return true;
        } catch (Exception e) {
            System.err.println("Aviso al inicializar tablas: " + e.getMessage());
            return false;
        }
    }
}
