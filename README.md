# Proyecto: TuSalud
Hecho por Manuel Pintaluba, Sofía Cedrés, Rodrigo Alonso y Fabian Guillermo.

### Descripción del problema y solución:

El problema que busca resolver TuSalud es la dificultad para registrar, organizar y analizar longitudinalmente datos biométricos personales de forma centralizada. Las personas que desean realizar un seguimiento de sus datos biométricos pueden registrar sus mediciones en diferentes lugares, como anotaciones manuales, planillas de cálculo o aplicaciones que no están específicamente adaptadas a sus necesidades.

Esto dificulta consultar el historial de mediciones, comparar registros realizados en diferentes fechas y visualizar la evolución de los datos de forma organizada.

Nuestro objetivo es desarrollar una aplicación que permita a los usuarios registrar y realizar un seguimiento de sus datos biométricos a lo largo del tiempo.

La aplicación permitirá ingresar diferentes datos personales y físicos, almacenarlos y analizarlos para obtener promedios, estadísticas y gráficos que faciliten la visualización del progreso. De esta manera, el usuario podrá comparar sus mediciones actuales con registros anteriores y observar su evolución de forma clara y sencilla.

Además, el sistema podrá proporcionar recomendaciones generales basadas en los datos registrados, con el objetivo de ayudar al usuario a comprender mejor su progreso y establecer metas personales.

En resumen, buscamos crear una herramienta simple, visual e intuitiva que permita registrar datos, analizar cambios y llevar un control organizado de la evolución del usuario.

## Alcance

El proyecto contempla:

- Registro e inicio de sesión de usuarios.
- Cuestionario de bienvenida (onboarding) para personalizar el perfil y definir variables clave de seguimiento.
- Registro de mediciones biométricas.
- Consulta y modificación del historial.
- Cálculo de estadísticas.
- Visualización mediante gráficos.
- Comparación entre mediciones.
- Generación de recomendaciones generales.

El proyecto no pretende realizar diagnósticos médicos ni sustituir la evaluación de profesionales de la salud.

## Cuestionario Médico Inicial (Flujo de Onboarding)

Para conocer las necesidades particulares de cada usuario y determinar automáticamente qué variables biométricas priorizar en su seguimiento, el sistema contempla el siguiente flujo condicional interactivo:

```mermaid
flowchart TD

A["¿Cuál es tu situación de salud actual?"]

A --> B["Hipertensión diagnosticada"]
A --> C["Diabetes diagnosticada"]
A --> D["Hipertensión y Diabetes diagnosticadas"]
A --> E["Control preventivo de ambas (Hipertensión y Diabetes)"]
A --> P["Control preventivo general (Bienestar y condición física)"]

%% RAMA HIPERTENSIÓN
B --> B1["¿Qué tipo de hipertensión tienes?"]
B1 --> B2["Hipertensión primaria o esencial"]
B1 --> B3["Hipertensión secundaria"]
B1 --> B4["No lo sé"]

B2 --> B5["¿Hace cuánto te diagnosticaron?"]
B3 --> B5
B4 --> B5

B5 --> B6["Menos de 1 año"]
B5 --> B7["Entre 1 y 5 años"]
B5 --> B8["Más de 5 años"]
B5 --> B9["No recuerdo"]

B6 --> B10["¿Cómo controlas tu hipertensión?"]
B7 --> B10
B8 --> B10
B9 --> B10

B10 --> B11["Medicamentos antihipertensivos"]
B10 --> B12["Cambios en alimentación o estilo de vida"]
B10 --> B13["Actividad física indicada"]
B10 --> B14["Tratamiento combinado (medicación y hábitos)"]
B10 --> B15["Ningún tratamiento actual"]
B10 --> B16["No lo sé"]

B11 --> B17["¿Tomas medicamentos todos los días?"]
B17 --> B18["Sí"]
B17 --> B19["No"]
B17 --> B20["A veces"]

B12 --> B21["¿Con qué frecuencia controlas tus valores?"]
B13 --> B21
B14 --> B21
B15 --> B21
B16 --> B21
B18 --> B21
B19 --> B21
B20 --> B21

B21 --> B22["Todos los días"]
B21 --> B23["Varias veces por semana"]
B21 --> B24["Una vez por semana"]
B21 --> B25["Algunas veces al mes"]
B21 --> B26["Solo cuando tengo síntomas"]
B21 --> B27["Casi nunca"]

B22 --> B28["¿Registras tus valores habitualmente?"]
B23 --> B28
B24 --> B28
B25 --> B28
B26 --> B28
B27 --> B28

B28 --> B29["Sí, diariamente"]
B28 --> B30["Sí, algunas veces"]
B28 --> B31["No"]

B29 --> PERFIL_HTA["Perfil configurado: Foco en Salud Cardiovascular"]
B30 --> PERFIL_HTA
B31 --> PERFIL_HTA

%% RAMA DIABETES
C --> C1["¿Qué tipo de diabetes tienes?"]
C1 --> C2["Tipo 1"]
C1 --> C3["Tipo 2"]
C1 --> C4["Diabetes gestacional"]
C1 --> C5["Otro tipo"]
C1 --> C6["No lo sé"]

C2 --> C7["¿Hace cuánto te diagnosticaron?"]
C3 --> C7
C4 --> C7
C5 --> C7
C6 --> C7

C7 --> C8["Menos de 1 año"]
C7 --> C9["Entre 1 y 5 años"]
C7 --> C10["Más de 5 años"]
C7 --> C11["No recuerdo"]

C8 --> C12["¿Cómo controlas actualmente tu diabetes?"]
C9 --> C12
C10 --> C12
C11 --> C12

C12 --> C13["Alimentación indicada"]
C12 --> C14["Insulina"]
C12 --> C15["Medicamentos orales"]
C12 --> C16["Actividad física indicada"]
C12 --> C17["Tratamiento combinado"]
C12 --> C18["Ningún tratamiento actual"]
C12 --> C19["No lo sé"]

C14 --> C20["¿Cómo administras la insulina?"]
C20 --> C21["Inyecciones (lapicera o jeringa)"]
C20 --> C22["Bomba de infusión continua"]
C20 --> C23["Otro método"]
C20 --> C24["No lo sé"]

C13 --> C25["¿Sigues una pauta nutricional específica?"]
C15 --> C25
C16 --> C25
C17 --> C25
C18 --> C25
C19 --> C25
C21 --> C25
C22 --> C25
C23 --> C25
C24 --> C25

C25 --> C26["Sí, estrictamente"]
C25 --> C27["No"]
C25 --> C28["A veces"]
C25 --> C29["No tengo pauta"]

C26 --> C30["¿Con qué frecuencia mides tu glucosa?"]
C27 --> C30
C28 --> C30
C29 --> C30

C30 --> C31["Varias veces al día"]
C30 --> C32["Una vez al día"]
C30 --> C33["Varias veces por semana"]
C30 --> C34["Algunas veces al mes"]
C30 --> C35["Solo cuando me siento mal"]
C30 --> C36["Casi nunca"]

C31 --> C37["¿Registras tus valores de glucosa?"]
C32 --> C37
C33 --> C37
C34 --> C37
C35 --> C37
C36 --> C37

C37 --> C38["Sí, diariamente"]
C37 --> C39["Sí, algunas veces"]
C37 --> C40["No"]

C38 --> PERFIL_DBT["Perfil configurado: Foco en Control Metabólico"]
C39 --> PERFIL_DBT
C40 --> PERFIL_DBT

%% RAMA AMBAS ENFERMEDADES DIAGNOSTICADAS
D --> D1["Iniciar preguntas sobre Diabetes"]
D1 --> C1
PERFIL_DBT -.-> D2["Continuar con preguntas sobre Hipertensión"]
D2 --> B1
PERFIL_HTA -.-> PERFIL_AMBAS["Perfil configurado: Monitoreo Integral Cardio-Metabólico"]

%% RAMA CONTROL PREVENTIVO DE AMBAS (HIPERTENSIÓN Y DIABETES)
E --> E1["¿Por qué deseas realizar control preventivo de ambas?"]
E1 --> E2["Antecedentes familiares de diabetes o hipertensión"]
E1 --> E3["Indicación médica de rutina o chequeo preventivo"]
E1 --> E4["Sobrepeso, sedentarismo o cambios en estilo de vida"]
E1 --> E5["Interés personal en prevención y cuidado"]

E2 --> E6["¿Te has realizado análisis clínicos en el último año?"]
E3 --> E6
E4 --> E6
E5 --> E6

E6 --> E7["Sí, con valores normales"]
E6 --> E8["Sí, con valores limítrofes o prediabetes/prehipertensión"]
E6 --> E9["No en el último año"]
E6 --> E10["No recuerdo"]

E7 --> E11["¿Con qué frecuencia deseas registrar tus mediciones preventivas?"]
E8 --> E11
E9 --> E11
E10 --> E11

E11 --> E12["Semanalmente"]
E11 --> E13["Quincenal o mensualmente"]
E11 --> E14["Ocasionalmente / Chequeo periódico"]

E12 --> PERFIL_PREV_AMBAS["Perfil configurado: Monitoreo Preventivo Dual"]
E13 --> PERFIL_PREV_AMBAS
E14 --> PERFIL_PREV_AMBAS

%% RAMA CONTROL PREVENTIVO GENERAL / BIENESTAR
P --> P1["¿Cuál es tu principal objetivo de salud?"]
P1 --> P2["Monitoreo de peso corporal y composición (IMC)"]
P1 --> P3["Seguimiento de actividad física y frecuencia cardíaca"]
P1 --> P4["Registro general de hábitos y bienestar"]

P2 --> P5["¿Tienes una meta de peso o acondicionamiento?"]
P3 --> P5
P4 --> P5

P5 --> P6["Bajar de peso saludablemente"]
P5 --> P7["Aumentar masa muscular o mantener peso"]
P5 --> P8["Control de rutina sin meta específica"]

P6 --> PERFIL_BIENESTAR["Perfil configurado: Foco en Peso y Condición Física"]
P7 --> PERFIL_BIENESTAR
P8 --> PERFIL_BIENESTAR

%% ASIGNACIÓN INTELIGENTE DE MÉTRICAS BIOMÉTRICAS
PERFIL_HTA --> REC_HTA["Métricas prioritarias asignadas: Frecuencia Cardíaca (bpm), Peso e IMC"]
PERFIL_DBT --> REC_DBT["Métricas prioritarias asignadas: Glucosa en Sangre (mg/dL), Peso e IMC"]
PERFIL_AMBAS --> REC_AMBAS["Métricas prioritarias asignadas: Glucosa, Frecuencia Cardíaca, Peso e IMC"]
PERFIL_PREV_AMBAS --> REC_PREV["Métricas prioritarias preventivas: Glucosa, Frecuencia Cardíaca, Peso e IMC"]
PERFIL_BIENESTAR --> REC_GEN["Métricas prioritarias de bienestar: Peso corporal, IMC y Frecuencia Cardíaca"]

REC_HTA --> DASHBOARD["Panel de Control Personalizado TuSalud"]
REC_DBT --> DASHBOARD
REC_AMBAS --> DASHBOARD
REC_PREV --> DASHBOARD
REC_GEN --> DASHBOARD
```

### Configuración resultante del perfil y restricción de variables biométricas:

El sistema garantiza que **cada usuario únicamente pueda registrar y modificar las variables biométricas que influyen en las opciones que seleccionó en su Cuestionario Médico Inicial**, bloqueando automáticamente los campos no pertinentes tanto al ingresar nuevas mediciones como al editarlas en el historial:

* **Diabetes diagnosticada:** Solo permite registrar y modificar **Glucosa en sangre (mg/dL)** y control metabólico de **Peso corporal e IMC**. Si el usuario selecciona monitoreo exclusivo de glucemia, los campos de peso y altura también se bloquean. La **Frecuencia Cardíaca (bpm) queda deshabilitada** por no influir en el control directo de la diabetes.
* **Hipertensión diagnosticada:** Solo permite registrar y modificar **Frecuencia Cardíaca (bpm)** y control de sobrepeso (**Peso corporal e IMC**). La **Glucosa en sangre queda deshabilitada** por no influir en la salud cardiovascular primaria.
* **Hipertensión y Diabetes / Preventivo Dual:** Permite el registro integral de las cuatro variables biométricas para un seguimiento clínico cruzado cardio-metabólico.
* **Control preventivo general:** Permite peso, altura y pulsaciones según el objetivo físico elegido (descenso de peso, rendimiento deportivo o salud general), bloqueando la glucosa al tratarse de un usuario sin afección metabólica.

---

## Alternativas consideradas

Una alternativa considerada fue utilizar una planilla de cálculo para que cada usuario registrara manualmente sus mediciones.

Esta alternativa permite almacenar datos y realizar cálculos básicos, pero requiere que el usuario organice manualmente la información y no proporciona una aplicación integrada con autenticación, historial, gráficos, comparaciones y recomendaciones.

Por este motivo se eligió desarrollar una aplicación propia que centralice estas funcionalidades.

# Marco teórico
### Datos biométricos registrados

La aplicación permitirá registrar los siguientes datos biométricos:

* **Peso:** peso corporal del usuario.
* **Altura:** altura del usuario.
* **Frecuencia cardíaca:** cantidad de latidos por minuto.
* **Glucosa en sangre:** nivel de glucosa registrado en sangre.

Estos serán los únicos datos biométricos contemplados en la primera versión del sistema. No se incluirán otros tipos de mediciones hasta que sean definidos explícitamente en los requisitos del proyecto.

Cada registro podrá contener los datos disponibles en el momento de la medición. No será obligatorio que el usuario proporcione todos los datos en cada registro, siempre que se proporcione al menos un dato biométrico válido.

El sistema almacenará la fecha y hora en la que se realizó cada registro.

---
### Definición de una medición

Una instancia de `Medicion` representa un registro realizado por un usuario en una fecha y hora determinadas.

Una medición puede contener uno o varios datos biométricos disponibles en ese momento:

* Peso.
* Altura.
* Frecuencia cardíaca.
* Glucosa en sangre.

Los datos pertenecientes a una misma medición se consideran parte del mismo registro histórico.

Cada `Medicion` pertenece exclusivamente a un `Usuario` y no puede ser consultada ni modificada por otros usuarios.

La fecha y hora forman parte del registro para permitir ordenar el historial y comparar mediciones realizadas en diferentes momentos.

La edad no se almacenará como un dato independiente de `Medicion`. Se calculará a partir de la `fechaNacimiento` almacenada en `Usuario` cuando sea necesaria.

---
### Unidades de los datos biométricos

Todos los datos biométricos utilizarán las siguientes unidades:

| Dato                | Unidad                           |
| :------------------ | :------------------------------- |
| Peso                | kilogramos (kg)                  |
| Altura              | metros (m)                       |
| Frecuencia cardíaca | latidos por minuto (bpm)         |
| Glucosa en sangre   | miligramos por decilitro (mg/dL) |

El sistema utilizará estas unidades internamente y no permitirá registrar valores utilizando otras unidades.

La altura se almacenará en metros utilizando valores decimales. Por ejemplo, una altura de 1 metro y 75 centímetros se registrará como `1.75 m`.

El peso se almacenará en kilogramos utilizando valores decimales. Por ejemplo, un peso de 72 kilogramos y 500 gramos se registrará como `72.5 kg`.
