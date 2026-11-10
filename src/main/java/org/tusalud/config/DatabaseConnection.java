// src/main/java/org/tusalud/config/DatabaseConnection.java
package org.tusalud.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestión centralizada de conexiones JDBC hacia MySQL (Railway / Docker).
 * Cumple con R10: Lectura exclusiva desde variables de entorno (.env / System.getenv)
 * sin credenciales hardcodeadas en el código fuente y con soporte SSL activo (useSSL=true).
 */
public class DatabaseConnection {

    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            dotenv = null;
        }
    }

    public static String getEnv(String key, String defaultValue) {
        String sysVal = System.getenv(key);
        if (sysVal != null && !sysVal.trim().isEmpty()) {
            return sysVal.trim();
        }
        if (dotenv != null) {
            String val = dotenv.get(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return defaultValue;
    }

    private static void validarCredenciales(String host, String user, String pass) throws SQLException {
        if (host == null || host.isEmpty() || user == null || user.isEmpty() || pass == null) {
            throw new SQLException(
                "Configuración de base de datos incompleta. Defina DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD en las variables de entorno o archivo .env."
            );
        }
    }

    public static Connection getConnectionSinBD() throws SQLException {
        String host = getEnv("DB_HOST", "");
        String port = getEnv("DB_PORT", "3306");
        String user = getEnv("DB_USER", "");
        String pass = getEnv("DB_PASSWORD", null);
        String ssl = getEnv("DB_SSL", "true");

        validarCredenciales(host, user, pass);

        String url = String.format(
            "jdbc:mysql://%s:%s/?useSSL=%s&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000",
            host, port, ssl
        );
        return DriverManager.getConnection(url, user, pass);
    }

    public static Connection getConnection() throws SQLException {
        String host = getEnv("DB_HOST", "");
        String port = getEnv("DB_PORT", "3306");
        String name = getEnv("DB_NAME", "railway");
        String user = getEnv("DB_USER", "");
        String pass = getEnv("DB_PASSWORD", null);
        String ssl = getEnv("DB_SSL", "true");

        validarCredenciales(host, user, pass);

        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=%s&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000",
            host, port, name, ssl
        );
        return DriverManager.getConnection(url, user, pass);
    }
}
