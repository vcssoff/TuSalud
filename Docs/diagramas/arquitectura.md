<!-- Docs/diagramas/arquitectura.md -->
# Diagramas de Arquitectura y Modelo de Datos — TuSalud

## 1. Arquitectura de Despliegue en Nube y Contenedores

```mermaid
flowchart LR
    subgraph Cliente["Dispositivos Cliente (Multidispositivo)"]
        Browser["Navegador Web\n(HTML5 + Bootstrap 5 + Chart.js)"]
    end

    subgraph Nube["Render Cloud / Kubernetes (Minikube)"]
        Container["Contenedor Docker (No-Root)\nOpenJDK 21 + Servidor Javalin 6"]
        Capas["Arquitectura en Capas:\nMain -> Service -> DAO (Interfaces) -> Model"]
        Container --> Capas
    end

    subgraph Railway["Railway Cloud (DBaaS)"]
        MySQL[("MySQL 8.0\nROLES | USUARIOS\nMEDICIONES | RECOMENDACIONES")]
    end

    Browser -- "HTTPS (Fetch JSON REST)\nPuerto 443 / 8080" --> Container
    Capas -- "JDBC + PreparedStatement\nSSL (useSSL=true)" --> MySQL
```

---

## 2. Diagrama Entidad-Relación (Cumple R1: 4 Tablas con PK y FK)

```mermaid
erDiagram
    ROLES ||--o{ USUARIOS : "1:N (fk_usuarios_rol)"
    USUARIOS ||--o{ MEDICIONES : "1:N (fk_mediciones_usuario)"
    USUARIOS ||--o{ RECOMENDACIONES : "1:N (fk_recomendaciones_usuario)"
    MEDICIONES ||--o{ RECOMENDACIONES : "1:N (fk_recomendaciones_medicion)"

    ROLES {
        int id_rol PK
        varchar nombre UK
        varchar descripcion
    }

    USUARIOS {
        int id_usuario PK
        int id_rol FK
        varchar nombre
        varchar correo UK
        varchar contrasenia_hash
        date fecha_nacimiento
        varchar genero
        varchar perfil_medico
        varchar especialidad
        varchar matricula
        timestamp fecha_registro
    }

    MEDICIONES {
        int id_medicion PK
        int id_usuario FK
        datetime fecha_hora
        decimal peso
        decimal altura
        int frecuencia_cardiaca
        decimal glucosa_sangre
    }

    RECOMENDACIONES {
        int id_recomendacion PK
        int id_usuario FK
        int id_medicion FK
        varchar tipo
        text mensaje
        datetime fecha_generacion
    }
```

---

## 3. Jerarquía de Herencia y Polimorfismo (Cumple R4 y R5)

```mermaid
classDiagram
    class Usuario {
        -int idUsuario
        -int idRol
        -String rolNombre
        -String nombre
        -String correo
        -String perfilMedico
        +getDescripcionRol() String
        +tieneAccesoPanelMedico() boolean
    }

    class Paciente {
        -List~Medicion~ mediciones
        -List~Recomendacion~ recomendaciones
        +getDescripcionRol() String
        +tieneAccesoPanelMedico() boolean
    }

    class Medico {
        -String especialidad
        -String matricula
        +getDescripcionRol() String
        +tieneAccesoPanelMedico() boolean
    }

    Usuario <|-- Paciente
    Usuario <|-- Medico
```
