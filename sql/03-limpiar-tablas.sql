-- sql/03-limpiar-tablas.sql
-- ==============================================================================
-- Script de Limpieza Total — TuSalud (MySQL 8.0+)
-- Deja la base de datos 100% vacía de usuarios, mediciones y recomendaciones
-- ==============================================================================

USE railway;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Vaciar completamente tablas de datos clínicos y transaccionales
TRUNCATE TABLE RECOMENDACIONES;
TRUNCATE TABLE MEDICIONES;

-- 2. Vaciar completamente la tabla de usuarios y reiniciar contador
TRUNCATE TABLE USUARIOS;

-- 3. Catálogo estructural de roles (R1 / R3)
INSERT IGNORE INTO ROLES (id_rol, nombre, descripcion) VALUES
(1, 'PACIENTE', 'Usuario paciente que registra y monitorea sus propias mediciones biométricas'),
(2, 'MEDICO', 'Profesional médico con acceso a búsqueda de pacientes, historia clínica JOIN y reportes globales'),
(3, 'ADMIN', 'Administrador general del sistema clínico');

SET FOREIGN_KEY_CHECKS = 1;

-- Verificación: Las tablas USUARIOS, MEDICIONES y RECOMENDACIONES quedan con 0 filas.
SELECT 'USUARIOS' AS tabla, COUNT(*) AS total_registros FROM USUARIOS
UNION ALL
SELECT 'MEDICIONES', COUNT(*) FROM MEDICIONES
UNION ALL
SELECT 'RECOMENDACIONES', COUNT(*) FROM RECOMENDACIONES;
