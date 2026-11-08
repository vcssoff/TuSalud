-- Esquema Relacional de TuSalud (MySQL 8.0+)
CREATE DATABASE IF NOT EXISTS railway;
USE railway;

CREATE TABLE IF NOT EXISTS USUARIOS (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasenia_hash VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(20) NOT NULL,
    perfil_medico VARCHAR(100) NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS MEDICIONES (
    id_medicion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    peso DECIMAL(5,2) NULL,
    altura DECIMAL(3,2) NULL,
    frecuencia_cardiaca INT NULL,
    glucosa_sangre DECIMAL(5,2) NULL,
    CONSTRAINT fk_mediciones_usuario FOREIGN KEY (id_usuario)
        REFERENCES USUARIOS (id_usuario) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_al_menos_un_dato CHECK (
        peso IS NOT NULL OR altura IS NOT NULL OR frecuencia_cardiaca IS NOT NULL OR glucosa_sangre IS NOT NULL
    )
);

CREATE TABLE IF NOT EXISTS RECOMENDACIONES (
    id_recomendacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_medicion INT NULL,
    tipo VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recomendaciones_usuario FOREIGN KEY (id_usuario)
        REFERENCES USUARIOS (id_usuario) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_recomendaciones_medicion FOREIGN KEY (id_medicion)
        REFERENCES MEDICIONES (id_medicion) ON DELETE SET NULL ON UPDATE CASCADE
);
