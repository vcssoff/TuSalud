package org.tusalud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tusalud.model.Medico;
import org.tusalud.model.Paciente;
import org.tusalud.model.Usuario;
import org.tusalud.repository.IUsuarioDAO;
import org.tusalud.service.AnalisisService;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MainRegistroSeguridadTest {

    private Javalin app;
    private int port;
    private InMemoryUsuarioDAO usuarioDAO;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        usuarioDAO = new InMemoryUsuarioDAO();
        app = Main.crearApp(usuarioDAO, null, null, new AnalisisService());
        app.start(0);
        port = app.port();
    }

    @AfterEach
    void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpClient nuevoClienteConCookies() {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    private HttpResponse<String> postJson(HttpClient client, String path, Map<String, Object> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(HttpClient client, String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .DELETE()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void registroConRolMedicoForzado_creaPacienteYRecibe403EnEndpointsMedico() throws Exception {
        HttpClient client = nuevoClienteConCookies();

        HttpResponse<String> regResp = postJson(client, "/api/register", Map.of(
                "nombre", "Usuario Prueba",
                "correo", "paciente.prueba@tusalud.test",
                "password", "ClaveSegura123",
                "fechaNacimiento", "1995-05-10",
                "genero", "Masculino",
                "rol", "MEDICO",
                "especialidad", "Cardiología",
                "matricula", "MSP-9999"
        ));

        assertEquals(201, regResp.statusCode(), "El registro debe responder 201");
        Map<String, Object> body = mapper.readValue(regResp.body(), new TypeReference<>() {});
        assertEquals("PACIENTE", body.get("rolNombre"));
        assertEquals(1, body.get("idRol"));
        assertEquals(Boolean.FALSE, body.get("tieneAccesoMedico"));

        Usuario guardado = usuarioDAO.obtenerPorId(((Number) body.get("idUsuario")).intValue());
        assertNotNull(guardado);
        assertInstanceOf(Paciente.class, guardado, "El registro público siempre debe instanciar Paciente");
        assertFalse(guardado.tieneAccesoPanelMedico());

        assertEquals(403, get(client, "/api/medico/pacientes").statusCode());
        assertEquals(403, get(client, "/api/medico/reportes").statusCode());
        assertEquals(403, get(client, "/api/medico/ficha/1").statusCode());
        assertEquals(403, postJson(client, "/api/medico/recomendaciones",
                Map.of("idUsuario", 1, "mensaje", "Intento no autorizado")).statusCode());
        assertEquals(403, delete(client, "/api/medico/usuarios/1").statusCode());
    }

    @Test
    void accesoEndpointsMedicoSinSesion_retorna401() throws Exception {
        HttpClient client = nuevoClienteConCookies();
        assertEquals(401, get(client, "/api/medico/pacientes").statusCode());
    }

    @Test
    void medicoPreexistente_mantieneAccesoPanelMedico() throws Exception {
        Medico medico = new Medico(0, "Dra. Demo", "medico.demo@tusalud.test", null,
                Date.valueOf("1985-03-15"), "Femenino", "Cardiología", "MSP-1001");
        usuarioDAO.registrar(medico, "ClaveMedico123");

        HttpClient client = nuevoClienteConCookies();
        HttpResponse<String> loginResp = postJson(client, "/api/login", Map.of(
                "correo", "medico.demo@tusalud.test",
                "password", "ClaveMedico123"
        ));

        assertEquals(200, loginResp.statusCode());
        Map<String, Object> body = mapper.readValue(loginResp.body(), new TypeReference<>() {});
        assertEquals(Boolean.TRUE, body.get("tieneAccesoMedico"));
        assertEquals(200, get(client, "/api/medico/pacientes").statusCode());
    }

    private static class InMemoryUsuarioDAO implements IUsuarioDAO {
        private final AtomicInteger seq = new AtomicInteger(1);
        private final Map<Integer, Usuario> usuarios = new HashMap<>();
        private final Map<String, String> passwords = new HashMap<>();

        @Override
        public boolean registrar(Usuario usuario, String contraseniaPlana) {
            int id = seq.getAndIncrement();
            usuario.setIdUsuario(id);
            usuarios.put(id, usuario);
            passwords.put(usuario.getCorreo(), contraseniaPlana);
            return true;
        }

        @Override
        public Usuario iniciarSesion(String correo, String contraseniaPlana) {
            String esperada = passwords.get(correo);
            if (esperada == null || !esperada.equals(contraseniaPlana)) {
                return null;
            }
            return usuarios.values().stream()
                    .filter(u -> correo.equals(u.getCorreo()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Usuario obtenerPorId(int idUsuario) {
            return usuarios.get(idUsuario);
        }

        @Override
        public boolean actualizarPerfilMedico(int idUsuario, String perfil) {
            Usuario u = usuarios.get(idUsuario);
            if (u == null) return false;
            u.setPerfilMedico(perfil);
            return true;
        }

        @Override
        public boolean actualizarDatosUsuario(Usuario usuario) {
            usuarios.put(usuario.getIdUsuario(), usuario);
            return true;
        }

        @Override
        public boolean eliminar(int idUsuario) {
            return usuarios.remove(idUsuario) != null;
        }

        @Override
        public boolean existeCorreo(String correo) {
            return usuarios.values().stream().anyMatch(u -> correo.equals(u.getCorreo()));
        }

        @Override
        public List<Usuario> buscarPacientes(String filtro) {
            return new ArrayList<>(usuarios.values());
        }

        @Override
        public Map<String, Object> obtenerHistoriaClinicaConsolidada(int idUsuario) {
            return Map.of();
        }

        @Override
        public List<Map<String, Object>> obtenerReporteGlobalPorPerfil() {
            return List.of();
        }
    }
}
