-- sql/02-datos-prueba.sql
-- ==============================================================================
-- Carga de Datos de Demostración — TuSalud (Cumple R1, R3 y R11)
-- Incluye roles, usuario paciente ficticio (Paciente Demo) con 21 mediciones
-- sintéticas de presión arterial y pulso, recomendaciones y usuario médico demo.
-- ==============================================================================

USE railway;

-- 1. Catálogo estructural de roles (R1 y R3)
INSERT IGNORE INTO ROLES (id_rol, nombre, descripcion) VALUES
(1, 'PACIENTE', 'Usuario paciente que registra y monitorea sus propias mediciones biométricas'),
(2, 'MEDICO', 'Profesional médico con acceso a búsqueda de pacientes, historia clínica JOIN y reportes globales'),
(3, 'ADMIN', 'Administrador general del sistema clínico');

-- 2. Usuario Paciente de Demostración (datos ficticios - Perfil Hipertensión)
-- Credenciales de demo (sin relación con contraseñas reales):
--   Correo: paciente.demo@tusalud.com
--   Contraseña: DemoPaciente2026! (hasheada con BCrypt según R3)
INSERT INTO USUARIOS (id_usuario, id_rol, nombre, correo, contrasenia_hash, fecha_nacimiento, genero, perfil_medico)
VALUES (1, 1, 'Paciente Demo', 'paciente.demo@tusalud.com', '$2a$10$G7pYKnAEiltBZYcEe3aZg.ov6.MrCyCecZqTwM7PLCHrxNxeb8.4S', '1990-01-15', 'MASCULINO', 'HIPERTENSION')
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    correo = VALUES(correo),
    contrasenia_hash = VALUES(contrasenia_hash),
    fecha_nacimiento = VALUES(fecha_nacimiento),
    perfil_medico = VALUES(perfil_medico);

-- 3. Usuario Profesional Médico de Demostración (R3)
-- Credenciales de demo (sin relación con contraseñas reales):
--   Correo: medico@tusalud.com
--   Contraseña: DemoMedico2026! (hasheada con BCrypt)
INSERT INTO USUARIOS (id_usuario, id_rol, nombre, correo, contrasenia_hash, fecha_nacimiento, genero, especialidad, matricula)
VALUES (2, 2, 'Dra. Valentina Silva', 'medico@tusalud.com', '$2a$10$4pIrhFrCXZZzgtmBXf5S/e1FdqDyi2f3L1huG/fFel93PUsyiamRi', '1980-03-22', 'FEMENINO', 'Cardiología', 'MED-48291')
ON DUPLICATE KEY UPDATE
    contrasenia_hash = VALUES(contrasenia_hash),
    especialidad = VALUES(especialidad);

-- 4. 21 Mediciones Sintéticas de Hipertensión (Presión Sistólica, Diastólica y Pulso - Cumple R11)
DELETE FROM MEDICIONES WHERE id_usuario = 1;

INSERT INTO MEDICIONES (id_usuario, fecha_hora, presion_sistolica, presion_diastolica, frecuencia_cardiaca) VALUES
(1, '2026-08-18 09:10:00', 152, 94, 80),
(1, '2026-08-21 09:20:00', 148, 90, 78),
(1, '2026-08-24 18:15:00', 136, 86, 75),
(1, '2026-08-27 20:30:00', 144, 91, 82),
(1, '2026-08-30 21:00:00', 140, 88, 74),
(1, '2026-09-02 08:45:00', 134, 85, 76),
(1, '2026-09-05 19:10:00', 146, 92, 81),
(1, '2026-09-08 18:25:00', 139, 87, 77),
(1, '2026-09-12 08:50:00', 143, 89, 73),
(1, '2026-09-16 20:40:00', 132, 84, 71),
(1, '2026-09-21 17:30:00', 135, 86, 72),
(1, '2026-09-26 09:00:00', 138, 87, 74),
(1, '2026-10-01 19:20:00', 141, 89, 79),
(1, '2026-10-06 08:15:00', 129, 82, 70),
(1, '2026-10-11 09:30:00', 131, 83, 72),
(1, '2026-10-16 18:55:00', 137, 86, 76),
(1, '2026-10-21 08:40:00', 133, 84, 73),
(1, '2026-10-26 21:10:00', 142, 88, 78),
(1, '2026-10-31 10:05:00', 127, 81, 69),
(1, '2026-11-05 19:45:00', 135, 85, 71),
(1, '2026-11-09 09:15:00', 128, 82, 68);

-- 5. Recomendaciones Clínicas Iniciales Personalizadas (Hipertensión)
INSERT INTO RECOMENDACIONES (id_usuario, tipo, mensaje) VALUES
(1, 'ONBOARDING', 'Perfil clínico configurado: Hipertensión Arterial. Se habilita el monitoreo de presión sistólica, diastólica y pulso basal.'),
(1, 'PRESION', 'Monitorea tu presión arterial matutina en reposo y antes de las comidas principales.'),
(1, 'HABITOS', 'Mantén una dieta baja en sodio (menos de 2g diarios) y realiza actividad física aeróbica moderada.');
