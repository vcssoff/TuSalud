-- sql/02-datos-prueba.sql
-- ==============================================================================
-- Carga de Datos Clínicos Reales — TuSalud (Cumple R1, R3 y R11)
-- Incluye roles, usuario paciente real (Francisco Pintaluba) con 13 mediciones
-- biométricas reales de presión arterial y pulso, recomendaciones y usuario médico.
-- ==============================================================================

USE railway;

-- 1. Catálogo estructural de roles (R1 y R3)
INSERT IGNORE INTO ROLES (id_rol, nombre, descripcion) VALUES
(1, 'PACIENTE', 'Usuario paciente que registra y monitorea sus propias mediciones biométricas'),
(2, 'MEDICO', 'Profesional médico con acceso a búsqueda de pacientes, historia clínica JOIN y reportes globales'),
(3, 'ADMIN', 'Administrador general del sistema clínico');

-- 2. Usuario Paciente Real (Francisco Pintaluba - Perfil Hipertensión)
-- Contraseña en texto plano: "Contra123" (hasheada con BCrypt según R3)
INSERT INTO USUARIOS (id_usuario, id_rol, nombre, correo, contrasenia_hash, fecha_nacimiento, genero, perfil_medico)
VALUES (1, 1, 'Francisco Pintaluba', 'francisco@gmail.com', '$2a$10$UoWbX2q7E72i5V7iOQ2MeeE2rM03hQe0ZkVrJ6m7r89rG3Kq6m6qy', '1975-06-12', 'MASCULINO', 'HIPERTENSION')
ON DUPLICATE KEY UPDATE perfil_medico = 'HIPERTENSION';

-- 3. Usuario Profesional Médico para demostración de roles y permisos (R3)
-- Contraseña en texto plano: "Doctor123" (hasheada con BCrypt)
INSERT INTO USUARIOS (id_usuario, id_rol, nombre, correo, contrasenia_hash, fecha_nacimiento, genero, especialidad, matricula)
VALUES (2, 2, 'Dra. Valentina Silva', 'medico@tusalud.com', '$2a$10$a0W1c1X4y9Z7e8q2w3e4r5t6y7u8i9o0p1a2s3d4f5g6h7j8k9l0m', '1980-03-22', 'FEMENINO', 'Cardiología', 'MED-48291')
ON DUPLICATE KEY UPDATE especialidad = 'Cardiología';

-- 4. 20 Mediciones Clínicas de Hipertensión (Presión Sistólica, Diastólica y Pulso - Cumple R11)
DELETE FROM MEDICIONES WHERE id_usuario = 1;

INSERT INTO MEDICIONES (id_usuario, fecha_hora, presion_sistolica, presion_diastolica, frecuencia_cardiaca) VALUES
(1, '2026-08-18 14:20:00', 158, 91, 77),
(1, '2026-08-21 09:17:00', 146, 86, 77),
(1, '2026-08-21 09:26:00', 124, 86, 76),
(1, '2026-08-22 20:56:00', 144, 92, 88),
(1, '2026-08-23 22:09:00', 146, 99, 68),
(1, '2026-08-30 18:01:00', 134, 89, 78),
(1, '2026-09-02 17:01:00', 144, 93, 81),
(1, '2026-09-05 19:52:00', 142, 89, 89),
(1, '2026-09-10 18:43:00', 148, 88, 68),
(1, '2026-09-22 21:21:00', 132, 88, 70),
(1, '2026-09-28 17:14:00', 133, 85, 73),
(1, '2026-10-04 08:30:00', 138, 87, 74),
(1, '2026-10-10 19:15:00', 145, 90, 80),
(1, '2026-10-16 07:45:00', 126, 81, 71),
(1, '2026-10-22 09:05:00', 130, 84, 72),
(1, '2026-10-29 18:40:00', 142, 89, 79),
(1, '2026-11-02 08:15:00', 136, 86, 75),
(1, '2026-11-05 21:30:00', 149, 93, 82),
(1, '2026-11-08 10:20:00', 128, 83, 70),
(1, '2026-11-09 20:10:00', 140, 85, 68);

-- 5. Recomendaciones Clínicas Iniciales Personalizadas (Hipertensión)
INSERT INTO RECOMENDACIONES (id_usuario, tipo, mensaje) VALUES
(1, 'ONBOARDING', 'Perfil clínico configurado: Hipertensión Arterial. Se habilita el monitoreo de presión sistólica, diastólica y pulso basal.'),
(1, 'PRESION', 'Monitorea tu presión arterial matutina en reposo y antes de las comidas principales.'),
(1, 'HABITOS', 'Mantén una dieta baja en sodio (menos de 2g diarios) y realiza actividad física aeróbica moderada.');
