// src/main/java/org/tusalud/Main.java
package org.tusalud;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import org.tusalud.config.DatabaseConnection;
import org.tusalud.config.DatabaseInitializer;
import org.tusalud.exception.TuSaludException;
import org.tusalud.model.*;
import org.tusalud.repository.*;
import org.tusalud.service.AnalisisService;
import org.tusalud.service.PerfilMedicoService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

/**
 * Servidor Web Embebido Javalin 6 y Controlador REST principal de TuSalud.
 * Cumple con R2 (CRUD 3 entidades), R3 (Roles PACIENTE/MEDICO), R4 (Capas con Interfaces),
 * R5 (Polimorfismo), R6 (JOIN y GROUP BY), R7 (Transacciones), R8 (TuSaludException) y R12 (/health).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("  Iniciando TuSalud Web — Servidor Clínico Javalin  ");
        System.out.println("====================================================");

        DatabaseInitializer.inicializarTablas();

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Javalin app = crearApp();
        app.start(port);
        System.out.println("Servidor TuSalud activo en: http://localhost:" + port);
    }

    public static Javalin crearApp() {
        return crearApp(new UsuarioDAO(), new MedicionDAO(), new RecomendacionDAO(), new AnalisisService());
    }

    public static Javalin crearApp(IUsuarioDAO usuarioDAO, IMedicionDAO medicionDAO,
                                   IRecomendacionDAO recomendacionDAO, AnalisisService analisisService) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.http.defaultContentType = "application/json; charset=utf-8";
        });

        // =========================================================================
        // MANEJO GLOBAL DE EXCEPCIONES (Cumple R8: Sin errores internos al usuario)
        // =========================================================================
        app.exception(TuSaludException.class, (e, ctx) -> {
            ctx.status(e.getStatusCode()).json(Map.of("error", e.getMessage()));
        });

        app.exception(NumberFormatException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", "Formato numérico inválido en los datos enviados."));
        });

        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("[ERROR INTERNO] " + e.getMessage());
            ctx.status(500).json(Map.of(
                "error", "No se pudo completar la operación en este momento. Verifique los datos ingresados."
            ));
        });

        // =========================================================================
        // ENDPOINT DE ESTADO DEL SERVICIO (Cumple R12: GET /health HTTP 200)
        // =========================================================================
        app.get("/health", ctx -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                ctx.status(200).json(Map.of(
                    "status", "UP",
                    "database", "CONNECTED",
                    "service", "TuSalud Web API"
                ));
            } catch (Exception ex) {
                ctx.status(503).json(Map.of(
                    "status", "DOWN",
                    "database", "DISCONNECTED",
                    "detail", "No se pudo establecer conexión con la base de datos"
                ));
            }
        });

        // =========================================================================
        // AUTENTICACIÓN Y GESTIÓN DE SESIONES (Cumple R3 y R5)
        // =========================================================================
        app.post("/api/register", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String nombre = str(body.get("nombre"));
            String correo = str(body.get("correo")).toLowerCase();
            String password = str(body.get("password"));
            String fechaNacStr = str(body.get("fechaNacimiento"));
            String genero = str(body.get("genero"));

            if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || fechaNacStr.isEmpty() || genero.isEmpty()) {
                throw new TuSaludException("Todos los campos obligatorios deben completarse.");
            }
            if (!correo.contains("@") || !correo.contains(".")) {
                throw new TuSaludException("El formato del correo electrónico no es válido.");
            }
            if (password.length() < 6) {
                throw new TuSaludException("La contraseña debe tener al menos 6 caracteres.");
            }
            if (usuarioDAO.existeCorreo(correo)) {
                throw new TuSaludException("Ya existe una cuenta registrada con el correo " + correo + ".");
            }

            Date fechaNac;
            try {
                fechaNac = Date.valueOf(fechaNacStr);
            } catch (IllegalArgumentException ex) {
                throw new TuSaludException("La fecha de nacimiento debe tener formato AAAA-MM-DD.");
            }

            // El registro público siempre crea un Paciente; las cuentas médicas se gestionan vía SQL/admin.
            Usuario nuevoUsuario = new Paciente(0, nombre, correo, null, fechaNac, genero, null);

            if (!usuarioDAO.registrar(nuevoUsuario, password)) {
                throw new TuSaludException("No se pudo registrar el usuario.");
            }

            Usuario guardado = usuarioDAO.obtenerPorId(nuevoUsuario.getIdUsuario());
            ctx.sessionAttribute("usuario", guardado);
            ctx.status(201).json(construirRespuestaUsuario(guardado));
        });

        app.post("/api/login", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String correo = str(body.get("correo")).toLowerCase();
            String password = str(body.get("password"));

            if (correo.isEmpty() || password.isEmpty()) {
                throw new TuSaludException("Debes ingresar tu correo electrónico y contraseña.");
            }

            Usuario u = usuarioDAO.iniciarSesion(correo, password);
            if (u == null) {
                ctx.status(401).json(Map.of("error", "Credenciales incorrectas. Verifica tu correo y contraseña."));
                return;
            }

            ctx.sessionAttribute("usuario", u);
            ctx.json(construirRespuestaUsuario(u));
        });

        app.post("/api/logout", ctx -> {
            if (ctx.req().getSession(false) != null) {
                ctx.req().getSession().invalidate();
            }
            ctx.json(Map.of("mensaje", "Sesión finalizada correctamente."));
        });

        app.get("/api/me", ctx -> {
            Usuario sesion = obtenerUsuarioAutenticado(ctx);
            Usuario actualizado = usuarioDAO.obtenerPorId(sesion.getIdUsuario());
            if (actualizado != null) {
                ctx.sessionAttribute("usuario", actualizado);
                sesion = actualizado;
            }
            ctx.json(construirRespuestaUsuario(sesion));
        });

        // =========================================================================
        // CUESTIONARIO MÉDICO Y CRUD DE USUARIO (Cumple RF-03, RF-12 y R2)
        // =========================================================================
        app.post("/api/cuestionario", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String situacion = str(body.get("situacion"));
            String alcanceHipertension = str(body.get("alcanceHipertension"));
            String controlHipertension = str(body.get("controlHipertension"));
            String alcanceDiabetes = str(body.get("alcanceDiabetes"));
            String controlDiabetes = str(body.get("controlDiabetes"));
            String objetivoGeneral = str(body.get("objetivoGeneral"));
            String perfilDirecto = str(body.get("perfilMedico"));

            String perfilNombre;
            List<String> consejos = new ArrayList<>();

            if (!perfilDirecto.isEmpty()) {
                perfilNombre = perfilDirecto.toUpperCase();
                consejos.add("Perfil clínico actualizado a: " + PerfilMedicoService.getNombreLegible(perfilNombre) + ".");
            } else if ("HIPERTENSION".equalsIgnoreCase(situacion)) {
                boolean soloFC = "SOLO_FC".equalsIgnoreCase(alcanceHipertension);
                perfilNombre = soloFC ? PerfilMedicoService.PERFIL_HIPERTENSION_SOLO_FC : PerfilMedicoService.PERFIL_HIPERTENSION;
                consejos.add("Monitorea tu frecuencia cardíaca matutina en reposo y mantén un registro constante.");
                if ("MEDICAMENTOS".equalsIgnoreCase(controlHipertension) || "COMBINADO".equalsIgnoreCase(controlHipertension)) {
                    consejos.add("Recuerda la toma puntual de tu medicación antihipertensiva según indicación médica.");
                }
            } else if ("DIABETES".equalsIgnoreCase(situacion)) {
                boolean soloGlucosa = "SOLO_GLUCOSA".equalsIgnoreCase(alcanceDiabetes);
                perfilNombre = soloGlucosa ? PerfilMedicoService.PERFIL_DIABETES_SOLO_GLUCOSA : PerfilMedicoService.PERFIL_DIABETES;
                consejos.add("Registra tus niveles de glucosa en ayunas y postprandial para un mejor control metabólico.");
                if ("INSULINA".equalsIgnoreCase(controlDiabetes)) {
                    consejos.add("Lleva control estricto de las dosis de insulina y horarios de ingesta.");
                }
            } else if ("AMBAS".equalsIgnoreCase(situacion)) {
                perfilNombre = PerfilMedicoService.PERFIL_DUAL;
                consejos.add("Tu perfil integral requiere un seguimiento coordinado de glucosa en sangre y salud cardiovascular.");
                consejos.add("Evita sal refinada y azúcares simples; realiza actividad física aeróbica moderada.");
            } else if ("PREVENTIVO_DUAL".equalsIgnoreCase(situacion)) {
                perfilNombre = PerfilMedicoService.PERFIL_PREVENTIVO_DUAL;
                consejos.add("Monitoreo preventivo activo por antecedentes o factores de riesgo cardiometabólico.");
                consejos.add("Un chequeo periódico de glucosa y frecuencia cardíaca ayuda a la detección temprana.");
            } else {
                if ("PESO".equalsIgnoreCase(objetivoGeneral)) {
                    perfilNombre = PerfilMedicoService.PERFIL_BIENESTAR_PESO;
                    consejos.add("Registra semanalmente tu peso corporal para observar la tendencia de tu IMC.");
                } else {
                    perfilNombre = PerfilMedicoService.PERFIL_BIENESTAR_GENERAL;
                    consejos.add("Mantén el registro periódico de tu peso, altura y pulsaciones en reposo.");
                }
            }

            usuarioDAO.actualizarPerfilMedico(u.getIdUsuario(), perfilNombre);
            u.setPerfilMedico(perfilNombre);
            ctx.sessionAttribute("usuario", u);

            for (String c : consejos) {
                recomendacionDAO.guardar(new Recomendacion(u.getIdUsuario(), null, "ONBOARDING", c));
            }

            ctx.json(construirRespuestaUsuario(u));
        });

        app.put("/api/usuarios/perfil", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String nombre = str(body.get("nombre"));
            String fechaNacStr = str(body.get("fechaNacimiento"));
            String genero = str(body.get("genero"));
            String perfilMedico = str(body.get("perfilMedico"));

            if (nombre.isEmpty()) {
                throw new TuSaludException("El nombre del usuario no puede estar vacío.");
            }
            u.setNombre(nombre);
            if (!fechaNacStr.isEmpty()) {
                try {
                    u.setFechaNacimiento(Date.valueOf(fechaNacStr));
                } catch (IllegalArgumentException ex) {
                    throw new TuSaludException("Formato de fecha inválido (use AAAA-MM-DD).");
                }
            }
            if (!genero.isEmpty()) u.setGenero(genero);
            if (!perfilMedico.isEmpty()) u.setPerfilMedico(perfilMedico);

            usuarioDAO.actualizarDatosUsuario(u);
            ctx.sessionAttribute("usuario", u);
            ctx.json(construirRespuestaUsuario(u));
        });

        app.delete("/api/usuarios/me", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            usuarioDAO.eliminar(u.getIdUsuario());
            if (ctx.req().getSession(false) != null) {
                ctx.req().getSession().invalidate();
            }
            ctx.json(Map.of("mensaje", "Cuenta de usuario eliminada correctamente."));
        });

        // =========================================================================
        // CRUD DE MEDICIONES BIOMÉTRICAS + TRANSACCIÓN ACID (Cumple R2, R7 y R8)
        // =========================================================================
        app.get("/api/mediciones", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            List<Medicion> mediciones = medicionDAO.obtenerPorUsuario(u.getIdUsuario());

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("mediciones", mediciones);
            resp.put("imc", Math.round(analisisService.calcularIMC(mediciones) * 10.0) / 10.0);
            resp.put("promedioCardiaco", Math.round(analisisService.calcularCardiaco(mediciones) * 10.0) / 10.0);
            resp.put("promedioGlucosa", Math.round(analisisService.calcularGlucosa(mediciones) * 10.0) / 10.0);
            resp.put("totalMediciones", mediciones.size());
            ctx.json(resp);
        });

        app.post("/api/mediciones", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            Medicion nueva = new Medicion();
            nueva.setIdUsuario(u.getIdUsuario());
            nueva.setFechaHora(new Timestamp(System.currentTimeMillis()));
            nueva.setPeso(parseDoubleNullable(body.get("peso")));
            nueva.setAltura(parseDoubleNullable(body.get("altura")));
            nueva.setFrecuenciaCardiaca(parseIntNullable(body.get("frecuenciaCardiaca")));
            nueva.setGlucosaSangre(parseDoubleNullable(body.get("glucosaSangre")));
            nueva.setPresionSistolica(parseIntNullable(body.get("presionSistolica")));
            nueva.setPresionDiastolica(parseIntNullable(body.get("presionDiastolica")));

            // 1. Sanitizar según las reglas clínicas del perfil del usuario (RF-03 y RF-12)
            PerfilMedicoService.sanitizarMedicion(nueva, u.getPerfilMedico());

            // 2. Validar rangos biológicos posibles lanzando TuSaludException si falla (R8)
            analisisService.validarMedicion(nueva);

            // 3. Calcular recomendaciones clínicas asociadas a la nueva medición
            List<Medicion> historial = medicionDAO.obtenerPorUsuario(u.getIdUsuario());
            historial.add(nueva);
            List<Recomendacion> nuevasRecomendaciones = analisisService.generarRecomendaciones(u.getIdUsuario(), historial);

            // 4. Persistir Medición + Recomendaciones en una ÚNICA TRANSACCIÓN con commit/rollback (R7)
            boolean ok = medicionDAO.registrarConRecomendaciones(nueva, nuevasRecomendaciones);
            if (!ok) {
                throw new TuSaludException("No se pudo registrar la medición en la base de datos.");
            }

            ctx.status(201).json(Map.of(
                "mensaje", "Medición y recomendaciones clínicas registradas exitosamente.",
                "medicion", nueva,
                "recomendacionesGeneradas", nuevasRecomendaciones.size()
            ));
        });

        app.put("/api/mediciones/{id}", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            int idMedicion = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            Medicion existente = medicionDAO.obtenerPorId(idMedicion, u.getIdUsuario());
            if (existente == null) {
                throw new TuSaludException("No se encontró la medición solicitada o no tienes permisos para editarla.");
            }

            existente.setPeso(parseDoubleNullable(body.get("peso")));
            existente.setAltura(parseDoubleNullable(body.get("altura")));
            existente.setFrecuenciaCardiaca(parseIntNullable(body.get("frecuenciaCardiaca")));
            existente.setGlucosaSangre(parseDoubleNullable(body.get("glucosaSangre")));
            existente.setPresionSistolica(parseIntNullable(body.get("presionSistolica")));
            existente.setPresionDiastolica(parseIntNullable(body.get("presionDiastolica")));

            PerfilMedicoService.sanitizarMedicion(existente, u.getPerfilMedico());
            analisisService.validarMedicion(existente);

            if (!medicionDAO.actualizar(existente)) {
                throw new TuSaludException("No se pudo actualizar la medición.");
            }
            ctx.json(Map.of("mensaje", "Medición actualizada correctamente.", "medicion", existente));
        });

        app.delete("/api/mediciones/{id}", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            int idMedicion = Integer.parseInt(ctx.pathParam("id"));
            if (!medicionDAO.eliminar(idMedicion, u.getIdUsuario())) {
                throw new TuSaludException("No se encontró la medición a eliminar.");
            }
            ctx.json(Map.of("mensaje", "Medición eliminada correctamente."));
        });

        app.get("/api/mediciones/comparar", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            int id1 = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("id1")));
            int id2 = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("id2")));

            Medicion m1 = medicionDAO.obtenerPorId(id1, u.getIdUsuario());
            Medicion m2 = medicionDAO.obtenerPorId(id2, u.getIdUsuario());
            if (m1 == null || m2 == null) {
                throw new TuSaludException("Debes seleccionar dos mediciones válidas para comparar.");
            }

            ComparacionBiometrica comp = analisisService.compararMediciones(m1, m2);
            ctx.json(comp);
        });

        // =========================================================================
        // CRUD DE RECOMENDACIONES (Cumple R2)
        // =========================================================================
        app.get("/api/recomendaciones", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            List<Recomendacion> lista = recomendacionDAO.obtenerPorUsuario(u.getIdUsuario());
            ctx.json(lista);
        });

        app.post("/api/recomendaciones", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String tipo = str(body.get("tipo"));
            String mensaje = str(body.get("mensaje"));
            if (mensaje.isEmpty()) {
                throw new TuSaludException("El mensaje de la nota o recomendación no puede estar vacío.");
            }
            Recomendacion rec = new Recomendacion(u.getIdUsuario(), null, tipo.isEmpty() ? "PERSONAL" : tipo, mensaje);
            recomendacionDAO.guardar(rec);
            ctx.status(201).json(rec);
        });

        app.put("/api/recomendaciones/{id}", ctx -> {
            obtenerUsuarioAutenticado(ctx);
            int idRec = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String tipo = str(body.get("tipo"));
            String mensaje = str(body.get("mensaje"));
            if (mensaje.isEmpty()) {
                throw new TuSaludException("El texto de la recomendación no puede estar vacío.");
            }
            Recomendacion r = new Recomendacion();
            r.setIdRecomendacion(idRec);
            r.setTipo(tipo.isEmpty() ? "CLINICA" : tipo);
            r.setMensaje(mensaje);
            recomendacionDAO.actualizar(r);
            ctx.json(Map.of("mensaje", "Recomendación actualizada correctamente."));
        });

        app.delete("/api/recomendaciones/{id}", ctx -> {
            Usuario u = obtenerUsuarioAutenticado(ctx);
            int idRec = Integer.parseInt(ctx.pathParam("id"));
            if (u.tieneAccesoPanelMedico()) {
                recomendacionDAO.eliminarPorMedico(idRec);
            } else {
                recomendacionDAO.eliminar(idRec, u.getIdUsuario());
            }
            ctx.json(Map.of("mensaje", "Recomendación eliminada correctamente."));
        });

        // =========================================================================
        // PANEL EXCLUSIVO DEL MÉDICO / ADMIN (Cumple R3 y R6: JOIN + GROUP BY)
        // =========================================================================
        app.get("/api/medico/pacientes", ctx -> {
            verificarPermisoMedico(ctx);
            String filtro = ctx.queryParam("q");
            List<Usuario> pacientes = usuarioDAO.buscarPacientes(filtro);
            ctx.json(pacientes);
        });

        app.get("/api/medico/ficha/{id}", ctx -> {
            verificarPermisoMedico(ctx);
            int idPaciente = Integer.parseInt(ctx.pathParam("id"));

            Map<String, Object> fichaJoin = usuarioDAO.obtenerHistoriaClinicaConsolidada(idPaciente);
            if (fichaJoin.isEmpty()) {
                throw new TuSaludException("El paciente solicitado no existe.");
            }
            List<Medicion> mediciones = medicionDAO.obtenerPorUsuario(idPaciente);
            List<Recomendacion> recomendaciones = recomendacionDAO.obtenerPorUsuario(idPaciente);

            fichaJoin.put("mediciones", mediciones);
            fichaJoin.put("recomendaciones", recomendaciones);
            fichaJoin.put("imc", Math.round(analisisService.calcularIMC(mediciones) * 10.0) / 10.0);
            fichaJoin.put("promedioCardiaco", Math.round(analisisService.calcularCardiaco(mediciones) * 10.0) / 10.0);
            fichaJoin.put("promedioGlucosa", Math.round(analisisService.calcularGlucosa(mediciones) * 10.0) / 10.0);
            fichaJoin.put("nombreLegiblePerfil", PerfilMedicoService.getNombreLegible((String) fichaJoin.get("perfilMedico")));
            ctx.json(fichaJoin);
        });

        app.post("/api/medico/recomendaciones", ctx -> {
            Usuario medico = verificarPermisoMedico(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Integer idPaciente = parseIntNullable(body.get("idUsuario"));
            Integer idMedicion = parseIntNullable(body.get("idMedicion"));
            String tipo = str(body.get("tipo"));
            String mensaje = str(body.get("mensaje"));

            if (idPaciente == null || mensaje.isEmpty()) {
                throw new TuSaludException("Debes indicar el paciente y el mensaje de la indicación médica.");
            }
            String prefijo = "[Dr/a. " + medico.getNombre() + "] " + mensaje;
            Recomendacion rec = new Recomendacion(idPaciente, idMedicion, tipo.isEmpty() ? "INDICACION_MEDICA" : tipo, prefijo);
            recomendacionDAO.guardar(rec);
            ctx.status(201).json(rec);
        });

        app.delete("/api/medico/usuarios/{id}", ctx -> {
            verificarPermisoMedico(ctx);
            int idPaciente = Integer.parseInt(ctx.pathParam("id"));
            if (!usuarioDAO.eliminar(idPaciente)) {
                throw new TuSaludException("No se pudo eliminar el paciente.");
            }
            ctx.json(Map.of("mensaje", "Paciente eliminado correctamente."));
        });

        app.get("/api/medico/reportes", ctx -> {
            verificarPermisoMedico(ctx);
            List<Map<String, Object>> porPerfil = usuarioDAO.obtenerReporteGlobalPorPerfil();
            List<Map<String, Object>> porMes = medicionDAO.obtenerEvolucionMensualGlobal();
            ctx.json(Map.of(
                "reportePorPerfil", porPerfil,
                "evolucionMensual", porMes
            ));
        });

        return app;
    }

    private static Usuario obtenerUsuarioAutenticado(Context ctx) throws TuSaludException {
        Usuario u = ctx.sessionAttribute("usuario");
        if (u == null) {
            throw new TuSaludException("Sesión no iniciada o expirada. Por favor, inicia sesión.", 401);
        }
        return u;
    }

    private static Usuario verificarPermisoMedico(Context ctx) throws TuSaludException {
        Usuario u = obtenerUsuarioAutenticado(ctx);
        if (!u.tieneAccesoPanelMedico()) {
            throw new TuSaludException("Acceso denegado: Se requiere rol MEDICO o ADMIN para acceder a este módulo.", 403);
        }
        return u;
    }

    private static Map<String, Object> construirRespuestaUsuario(Usuario u) {
        String perfil = u.getPerfilMedico();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idUsuario", u.getIdUsuario());
        map.put("idRol", u.getIdRol());
        map.put("rolNombre", u.getRolNombre());
        map.put("descripcionRol", u.getDescripcionRol());
        map.put("tieneAccesoMedico", u.tieneAccesoPanelMedico());
        map.put("nombre", u.getNombre());
        map.put("correo", u.getCorreo());
        map.put("fechaNacimiento", u.getFechaNacimiento() != null ? u.getFechaNacimiento().toString() : null);
        map.put("genero", u.getGenero());
        map.put("perfilMedico", perfil);
        map.put("nombreLegiblePerfil", PerfilMedicoService.getNombreLegible(perfil));
        map.put("permitePeso", PerfilMedicoService.permitePeso(perfil));
        map.put("permiteAltura", PerfilMedicoService.permiteAltura(perfil));
        map.put("permiteFrecuencia", PerfilMedicoService.permiteFrecuencia(perfil));
        map.put("permiteGlucosa", PerfilMedicoService.permiteGlucosa(perfil));
        map.put("metricasPermitidas", PerfilMedicoService.getMetricasPermitidas(perfil));
        map.put("metricasBloqueadas", PerfilMedicoService.getMetricasBloqueadas(perfil));
        map.put("motivoPeso", PerfilMedicoService.getMotivoBloqueo("peso", perfil));
        map.put("motivoAltura", PerfilMedicoService.getMotivoBloqueo("altura", perfil));
        map.put("motivoFrecuencia", PerfilMedicoService.getMotivoBloqueo("frecuenciaCardiaca", perfil));
        map.put("motivoGlucosa", PerfilMedicoService.getMotivoBloqueo("glucosaSangre", perfil));
        return map;
    }

    private static String str(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

    private static Double parseDoubleNullable(Object obj) {
        String s = str(obj).replace(',', '.');
        if (s.isEmpty()) return null;
        return Double.parseDouble(s);
    }

    private static Integer parseIntNullable(Object obj) {
        String s = str(obj);
        if (s.isEmpty()) return null;
        if (s.contains(".")) {
            return (int) Math.round(Double.parseDouble(s));
        }
        return Integer.parseInt(s);
    }
}
