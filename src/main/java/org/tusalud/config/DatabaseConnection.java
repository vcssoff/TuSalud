/**
 * Gestión centralizada de conexiones JDBC hacia MySQL (Railway / Docker).
 * Cumple con R10: Lectura exclusiva desde variables de entorno (.env / System.getenv)
 * sin credenciales hardcodeadas y con soporte sslMode configurable (REQUIRED por defecto).
 */
package org.tusalud.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public class DatabaseConnection {

    public static final String SSL_DISABLED = "DISABLED";
    public static final String SSL_REQUIRED = "REQUIRED";
    public static final String SSL_VERIFY_CA = "VERIFY_CA";
    public static final String SSL_VERIFY_IDENTITY = "VERIFY_IDENTITY";

    private static final Set<String> MODOS_SSL_VALIDOS = Set.of(
            SSL_DISABLED, SSL_REQUIRED, SSL_VERIFY_CA, SSL_VERIFY_IDENTITY
    );

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

    private static String envOrDefault(Function<String, String> envReader, String key, String defaultValue) {
        String val = envReader.apply(key);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    private static void validarCredenciales(String host, String user, String pass) throws SQLException {
        if (host == null || host.isEmpty() || user == null || user.isEmpty() || pass == null) {
            throw new SQLException(
                "Configuración de base de datos incompleta. Defina DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD en las variables de entorno o archivo .env."
            );
        }
    }

    /**
     * Resuelve el modo SSL a partir de DB_SSL_MODE, manteniendo compatibilidad con DB_SSL=true/false.
     * Por defecto retorna REQUIRED para cifrar conexiones remotas.
     */
    public static String resolverSslMode(Function<String, String> envReader) {
        String rawMode = envOrDefault(envReader, "DB_SSL_MODE", "");
        if (!rawMode.isEmpty()) {
            String normalized = rawMode.toUpperCase(Locale.ROOT);
            if (!MODOS_SSL_VALIDOS.contains(normalized)) {
                throw new IllegalArgumentException(
                    "Valor inválido para DB_SSL_MODE: '" + rawMode + "'. Use DISABLED, REQUIRED, VERIFY_CA o VERIFY_IDENTITY."
                );
            }
            return normalized;
        }

        String legacySsl = envOrDefault(envReader, "DB_SSL", "");
        if (!legacySsl.isEmpty()) {
            if ("false".equalsIgnoreCase(legacySsl)) {
                return SSL_DISABLED;
            }
            if ("true".equalsIgnoreCase(legacySsl)) {
                return SSL_REQUIRED;
            }
            throw new IllegalArgumentException(
                "Valor inválido para DB_SSL: '" + legacySsl + "'. Use true/false o configure DB_SSL_MODE."
            );
        }

        return SSL_REQUIRED;
    }

    static String construirUrlJdbc(String databaseName, Function<String, String> envReader) throws SQLException {
        String host = envOrDefault(envReader, "DB_HOST", "");
        String port = envOrDefault(envReader, "DB_PORT", "3306");
        String user = envOrDefault(envReader, "DB_USER", "");
        String pass = envOrDefault(envReader, "DB_PASSWORD", null);

        validarCredenciales(host, user, pass);

        String sslMode = resolverSslMode(envReader);
        String dbSegment = (databaseName != null && !databaseName.isEmpty()) ? databaseName : "";

        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://").append(host).append(':').append(port).append('/').append(dbSegment)
           .append("?sslMode=").append(sslMode);

        // Solo necesario en MySQL 8 local sin TLS (caching_sha2_password)
        if (SSL_DISABLED.equals(sslMode)) {
            url.append("&allowPublicKeyRetrieval=true");
        }

        url.append("&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000");
        return url.toString();
    }

    static Properties construirPropiedadesConexion(String user, String pass, String sslMode,
                                                   Function<String, String> envReader) {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);

        if (SSL_VERIFY_CA.equals(sslMode) || SSL_VERIFY_IDENTITY.equals(sslMode)) {
            String truststoreUrl = envOrDefault(envReader, "DB_TRUSTSTORE_URL", "");
            if (!truststoreUrl.isEmpty()) {
                props.setProperty("trustCertificateKeyStoreUrl", truststoreUrl);
                String truststoreType = envOrDefault(envReader, "DB_TRUSTSTORE_TYPE", "PKCS12");
                props.setProperty("trustCertificateKeyStoreType", truststoreType);
                String truststorePass = envOrDefault(envReader, "DB_TRUSTSTORE_PASSWORD", "");
                if (!truststorePass.isEmpty()) {
                    props.setProperty("trustCertificateKeyStorePassword", truststorePass);
                }
            }
        }
        return props;
    }

    private static Connection openConnection(String databaseName) throws SQLException {
        Function<String, String> envReader = key -> getEnv(key, null);
        String url = construirUrlJdbc(databaseName, envReader);
        String user = getEnv("DB_USER", "");
        String pass = getEnv("DB_PASSWORD", null);
        String sslMode = resolverSslMode(envReader);
        Properties props = construirPropiedadesConexion(user, pass, sslMode, envReader);
        return DriverManager.getConnection(url, props);
    }

    public static Connection getConnectionSinBD() throws SQLException {
        return openConnection("");
    }

    public static Connection getConnection() throws SQLException {
        String name = getEnv("DB_NAME", "railway");
        return openConnection(name);
    }
}
