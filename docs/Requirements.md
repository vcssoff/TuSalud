# Requisitos funcionales

* **RF-01 — Registro de usuarios:** El sistema deberá permitir a los usuarios crear una cuenta proporcionando sus datos personales, incluyendo nombre, apellido, correo electrónico, contraseña, fecha de nacimiento y género.
* **RF-02 — Inicio de sesión:** El sistema deberá permitir a los usuarios autenticarse mediante su correo electrónico y contraseña.
* **RF-03 — Registro de mediciones:** El sistema deberá permitir al usuario autenticado registrar mediciones biométricas (peso, altura, frecuencia cardíaca y glucosa), restringiendo la entrada únicamente a las variables que influyan en su perfil configurado en el Cuestionario Médico Inicial.
* **RF-04 — Consulta de historial:** El sistema deberá permitir al usuario autenticado consultar el historial de sus mediciones biométricas registradas.
* **RF-05 — Modificación de mediciones:** El sistema deberá permitir al usuario autenticado modificar sus mediciones biométricas previamente registradas, garantizando que solo pueda cambiar los datos correspondientes a su perfil médico.
* **RF-06 — Eliminación de mediciones:** El sistema deberá permitir al usuario autenticado eliminar sus mediciones biométricas previamente registradas.
* **RF-07 — Cálculo de estadísticas:** El sistema deberá permitir calcular estadísticas a partir de las mediciones registradas, incluyendo promedios de frecuencia cardíaca, glucosa e índice de masa corporal (IMC).
* **RF-08 — Visualización gráfica:** El sistema deberá permitir visualizar mediante gráficos la evolución de las mediciones biométricas registradas a lo largo del tiempo, pudiendo seleccionar diferentes tipos de datos.
* **RF-09 — Comparación de mediciones:** El sistema deberá permitir seleccionar dos fechas y comparar las mediciones biométricas correspondientes, mostrando las diferencias entre ellas.
* **RF-10 — Generación de recomendaciones:** El sistema deberá generar recomendaciones generales a partir de los valores registrados de las mediciones biométricas y los resultados obtenidos del análisis.
* **RF-11 — Visualización del progreso:** El sistema deberá permitir al usuario visualizar la evolución general de sus datos biométricos mediante la información histórica registrada.
* **RF-12 — Cuestionario Médico Inicial y parametrización de variables:** El sistema deberá aplicar un cuestionario inicial al usuario para clasificar su perfil de salud (diabetes, hipertensión, preventivo dual o bienestar) y restringir automáticamente la captura y edición de datos biométricos únicamente a los que tengan impacto clínico en su objetivo de control.


# Requisitos no funcionales

* **RNF-01 — Rendimiento:** El sistema deberá mostrar el historial de mediciones en un tiempo máximo de 2 segundos para un usuario que posea hasta 1000 registros almacenados.
* **RNF-02 — Usabilidad:** Las operaciones principales del sistema, como registrar, consultar, modificar y eliminar una medición, deberán poder realizarse desde la interfaz gráfica sin necesidad de utilizar comandos de consola.
* **RNF-03 — Seguridad:** Las contraseñas de los usuarios no deberán almacenarse en texto plano y deberán almacenarse utilizando un mecanismo de hashing seguro.
* **RNF-04 — Privacidad:** El sistema deberá asociar cada medición al usuario autenticado y deberá impedir que un usuario consulte, modifique o elimine mediciones pertenecientes a otro usuario.
* **RNF-05 — Disponibilidad:** La aplicación deberá poder ejecutarse y utilizarse durante la demostración del proyecto con la configuración de base de datos definida para el entorno de entrega.
* **RNF-06 — Integridad de datos:** El sistema deberá validar los datos ingresados antes de almacenarlos y no deberá permitir guardar mediciones que incumplan las validaciones definidas por la aplicación.
* **RNF-07 — Mantenibilidad:** El proyecto deberá mantener una separación entre la interfaz gráfica, la lógica de análisis y el acceso a la base de datos, de acuerdo con la organización utilizada en la implementación.

