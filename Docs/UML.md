# Diagrama de clases

```mermaid
classDiagram
    class Usuario {
        -String nombre
        -String correo
        -String contraseña
        -Date fechaNacimiento
        -String genero
        -String perfilMedico
    }

    class Medicion {
        -Date fecha
        -double altura
        -double peso
        -int frecuenciaCardiaca
        -double glucosaSangre
    }

    class Recomendacion {
        -String mensaje
        -String tipo
    }

    class Promedio {
        <<interface>>
        +calcularCardiaco(List~Medicion~ mediciones) double
        +calcularGlucosa(List~Medicion~ mediciones) double
        +calcularIMC(List~Medicion~ mediciones) double
    }

    class AnalisisService {
        +calcularCardiaco(List~Medicion~ mediciones) double
        +calcularGlucosa(List~Medicion~ mediciones) double
        +calcularIMC(List~Medicion~ mediciones) double
        +generarRecomendaciones(List~Medicion~ mediciones) List~Recomendacion~
        +generarGraficaEvolucion(List~Medicion~ mediciones)
        +generarGraficaFiltrada(List~Medicion~ mediciones)
        +compararMediciones(Medicion medicion1, Medicion medicion2)
    }

    class PerfilMedicoService {
        +puedeEditarPeso(String perfil) boolean
        +puedeEditarAltura(String perfil) boolean
        +puedeEditarFrecuencia(String perfil) boolean
        +puedeEditarGlucosa(String perfil) boolean
        +sanitizarMedicion(Medicion medicion, String perfil) void
        +getNombreLegible(String perfil) String
    }

    class ComparacionBiometrica {
        -double diferenciaPeso
        -double diferenciaIMC
        -double diferenciaCardiaca
        -double diferenciaGlucosa
    }

    class UsuarioDAO {
        +registrar(Usuario usuario)
        +iniciarSesion(String correo, String contraseña)
        +actualizarPerfilMedico(int idUsuario, String perfil)
    }

    class MedicionDAO {
        +guardar(Medicion medicion)
        +obtenerPorUsuario(int idUsuario)
        +actualizar(Medicion medicion)
        +eliminar(int idMedicion, int idUsuario)
    }

    class RecomendacionDAO {
        +guardar(Recomendacion recomendacion)
        +obtenerPorUsuario(int idUsuario)
    }

    Usuario "1" --> "*" Medicion : registra
    Usuario "1" --> "*" Recomendacion : recibe

    UsuarioDAO ..> Usuario : gestiona
    MedicionDAO ..> Medicion : gestiona
    RecomendacionDAO ..> Recomendacion : gestiona

    AnalisisService ..|> Promedio : implementa
    AnalisisService ..> Medicion : analiza
    AnalisisService ..> Recomendacion : genera
    AnalisisService ..> ComparacionBiometrica : genera

    PerfilMedicoService ..> Usuario : valida perfil de
    PerfilMedicoService ..> Medicion : filtra y sanitiza
```
---

## Casos de Uso

### Actores

* **Usuario:** utiliza la aplicación para registrar, consultar y analizar sus datos biométricos.


### CU01 - Registrarse

**Actor:** Usuario

**Objetivo:** Crear una cuenta en el sistema.
#### Flujo principal
1. El usuario selecciona la opción de registrarse.
2. El sistema solicita los datos necesarios.
3. El usuario introduce sus datos.
4. El sistema valida la información.
5. El sistema crea la cuenta.
6. El sistema confirma el registro.

#### Flujo alternativo

* Si los datos introducidos no son válidos, el sistema informa al usuario y solicita corregirlos.



### CU02 - Iniciar sesión

**Actor:** Usuario

**Objetivo:** Acceder a su cuenta.

#### Flujo principal

1. El usuario introduce sus credenciales.
2. El sistema verifica los datos.
3. El sistema permite el acceso a la cuenta.

#### Flujo alternativo

* Si las credenciales son incorrectas, el sistema informa al usuario y solicita introducirlas nuevamente.



### CU03 - Registrar datos biométricos

**Actor:** Usuario

**Objetivo:** Guardar nuevas mediciones biométricas.

##### Flujo principal

1. El usuario selecciona la opción para registrar una medición.
2. El sistema consulta el perfil médico del usuario y habilita únicamente los campos relevantes (bloqueando métricas no contempladas).
3. El usuario introduce sus mediciones en los campos habilitados.
4. El sistema valida y sanitiza los datos según el perfil activo.
5. El sistema guarda las mediciones junto con la fecha.
6. El sistema confirma el registro.

##### Regla de Negocio (Restricción por Perfil)
* Los campos de entrada se ajustan a la condición seleccionada en el Cuestionario Inicial (p. ej., control exclusivo de Diabetes habilita solo Glucosa; control de Hipertensión habilita Frecuencia Cardíaca).
* Los campos bloqueados permanecen deshabilitados con mensaje explicativo y no se persisten en la base de datos.

##### Datos que se pueden registrar

* Peso.
* Altura.
* Frecuencia cardíaca.
* Glucosa en sangre.



### CU04 - Actualizar datos biométricos

**Actor:** Usuario

**Objetivo:** Modificar una medición registrada anteriormente.

#### Flujo principal

1. El usuario selecciona un registro anterior.
2. El sistema muestra los datos registrados, bloqueando los campos que no correspondan a su perfil médico actual.
3. El usuario modifica la información permitida.
4. El sistema valida y sanitiza los nuevos datos.
5. El sistema guarda los cambios.
6. El sistema confirma la actualización.

#### Flujo alternativo

* Si los nuevos datos no son válidos, el sistema informa al usuario y solicita corregirlos.

#### Regla de Negocio
* Al editar, solo se permite alterar las métricas autorizadas por el perfil médico vigente del usuario; los demás valores se conservan o visualizan como solo lectura.



### CU05 - Consultar historial

**Actor:** Usuario

**Objetivo:** Consultar las mediciones realizadas anteriormente.

#### Flujo principal

1. El usuario selecciona la opción de historial.
2. El sistema obtiene los registros del usuario.
3. El sistema muestra las mediciones ordenadas por fecha.
4. El usuario puede consultar los diferentes registros.



### CU06 - Calcular promedios

**Actor:** Usuario

**Objetivo:** Obtener el promedio de sus mediciones.

#### Flujo principal

1. El usuario selecciona el tipo de medición que desea analizar.
2. El sistema obtiene los registros correspondientes.
3. El sistema calcula el promedio.
4. El sistema muestra el resultado.


### CU07 - Visualizar gráficos

**Actor:** Usuario

**Objetivo:** Observar visualmente la evolución de sus mediciones.

#### Flujo principal

1. El usuario selecciona el tipo de dato que desea consultar.
2. El sistema obtiene los registros históricos.
3. El sistema genera un gráfico con los datos.
4. El sistema muestra el gráfico al usuario.



### CU08 - Comparar mediciones

**Actor:** Usuario

**Objetivo:** Comparar mediciones realizadas en diferentes fechas.

#### Flujo principal

1. El usuario selecciona el tipo de medición que desea comparar.
2. El usuario selecciona las fechas que desea comparar.
3. El sistema obtiene los datos correspondientes.
4. El sistema muestra las diferencias entre las mediciones.


### CU09 - Obtener recomendaciones

**Actor:** Usuario

**Objetivo:** Recibir recomendaciones generales basadas en los datos registrados.

#### Flujo principal

1. El usuario solicita recomendaciones.
2. El sistema analiza los datos registrados.
3. El sistema genera recomendaciones generales.
4. El sistema muestra las recomendaciones al usuario.

#### Consideración

Las recomendaciones proporcionadas por el sistema son de carácter general y no sustituyen una evaluación o recomendación profesional.

### CU10 - Visualizar progreso

**Actor:** Usuario

**Objetivo:** Obtener un resumen de la evolución de sus datos.

#### Flujo principal

1. El usuario accede a la sección de progreso.
2. El sistema obtiene los registros del usuario.
3. El sistema analiza la evolución de los datos.
4. El sistema genera un resumen.
5. El sistema muestra estadísticas y gráficos relacionados con el progreso.

### CU11 - Completar Cuestionario Médico Inicial

**Actor:** Usuario

**Objetivo:** Determinar la condición de salud y el alcance de las métricas biométricas a monitorear.

#### Precondición
* El usuario ha iniciado sesión y no posee un perfil médico configurado, o solicita modificar su condición de seguimiento.

#### Flujo principal
1. El sistema presenta el cuestionario médico inicial con las opciones de control:
   - Control general / Preventivo.
   - Diabetes (opción de solo glucosa o glucosa + peso/IMC).
   - Hipertensión / Salud Cardiovascular (solo frecuencia cardíaca o integral con peso).
   - Monitoreo Integral Combinado.
2. El usuario selecciona la condición y el alcance específico de seguimiento.
3. El sistema valida la selección y asigna el identificador de perfil correspondiente.
4. El sistema persiste el perfil médico asociado al usuario en la base de datos.
5. El sistema actualiza la interfaz adaptando los formularios y habilitando únicamente las métricas pertinentes.

