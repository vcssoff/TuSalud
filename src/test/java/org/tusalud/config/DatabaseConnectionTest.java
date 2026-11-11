package org.tusalud.config;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    void resolverSslMode_porDefectoEsRequired() {
        Map<String, String> env = Map.of();
        assertEquals("REQUIRED", DatabaseConnection.resolverSslMode(env::get));
    }

    @Test
    void resolverSslMode_mantieneCompatibilidadConDbSsl() {
        assertEquals("DISABLED", DatabaseConnection.resolverSslMode(Map.of("DB_SSL", "false")::get));
        assertEquals("REQUIRED", DatabaseConnection.resolverSslMode(Map.of("DB_SSL", "true")::get));
    }

    @Test
    void resolverSslMode_dbSslModeTienePrioridadYNormaliza() {
        Map<String, String> env = Map.of(
                "DB_SSL", "false",
                "DB_SSL_MODE", "verify_ca"
        );
        assertEquals("VERIFY_CA", DatabaseConnection.resolverSslMode(env::get));
    }

    @Test
    void resolverSslMode_rechazaValorInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseConnection.resolverSslMode(Map.of("DB_SSL_MODE", "INVALIDO")::get));
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseConnection.resolverSslMode(Map.of("DB_SSL", "talvez")::get));
    }

    @Test
    void construirUrlJdbc_porDefectoNoDesactivaVerificacionNiHabilitaPublicKeyRetrieval() throws SQLException {
        Map<String, String> env = Map.of(
                "DB_HOST", "db.ejemplo.local",
                "DB_PORT", "3306",
                "DB_USER", "usuario",
                "DB_PASSWORD", "clave"
        );
        String url = DatabaseConnection.construirUrlJdbc("railway", env::get);
        assertTrue(url.startsWith("jdbc:mysql://db.ejemplo.local:3306/railway?"));
        assertTrue(url.contains("sslMode=REQUIRED"));
        assertFalse(url.contains("verifyServerCertificate=false"));
        assertFalse(url.contains("allowPublicKeyRetrieval=true"));
    }

    @Test
    void construirUrlJdbc_disabledHabilitaPublicKeyRetrievalParaDockerLocal() throws SQLException {
        Map<String, String> env = Map.of(
                "DB_HOST", "mysql-db",
                "DB_PORT", "3306",
                "DB_USER", "root",
                "DB_PASSWORD", "local",
                "DB_SSL_MODE", "DISABLED"
        );
        String url = DatabaseConnection.construirUrlJdbc("", env::get);
        assertTrue(url.startsWith("jdbc:mysql://mysql-db:3306/?"));
        assertTrue(url.contains("sslMode=DISABLED"));
        assertTrue(url.contains("allowPublicKeyRetrieval=true"));
    }

    @Test
    void construirPropiedadesConexion_incluyeTruststoreSoloEnVerify() {
        Map<String, String> env = Map.of(
                "DB_TRUSTSTORE_URL", "file:///app/truststore.p12",
                "DB_TRUSTSTORE_TYPE", "PKCS12",
                "DB_TRUSTSTORE_PASSWORD", "changeit"
        );

        Properties propsVerify = DatabaseConnection.construirPropiedadesConexion(
                "user", "pass", DatabaseConnection.SSL_VERIFY_CA, env::get);
        assertEquals("file:///app/truststore.p12", propsVerify.getProperty("trustCertificateKeyStoreUrl"));
        assertEquals("PKCS12", propsVerify.getProperty("trustCertificateKeyStoreType"));
        assertEquals("changeit", propsVerify.getProperty("trustCertificateKeyStorePassword"));

        Properties propsReq = DatabaseConnection.construirPropiedadesConexion(
                "user", "pass", DatabaseConnection.SSL_REQUIRED, env::get);
        assertNull(propsReq.getProperty("trustCertificateKeyStoreUrl"));
    }

    @Test
    void construirUrlJdbc_fallaSiFaltanCredenciales() {
        Map<String, String> env = Map.of("DB_HOST", "localhost");
        assertThrows(SQLException.class,
                () -> DatabaseConnection.construirUrlJdbc("railway", env::get));
    }
}
