# Decisiones de la práctica (para consultar con el grupo)

Cada decisión lleva: el problema, lo que se decide y por qué es lógico. Fecha de cada entrada entre paréntesis.
Referencias: `Rn` = requisito, `AMB-n` = ambigüedad de `docs/requisitos.md`.

---

## D1 — Credenciales del mainframe: ventana de conexión precargada y clave sin cambiar (2026-09-27)

**Problema.** El guion da una cuenta por grupo (`grupo_XX`) con la clave inicial `secreto6` y añade que "se puede cambiar una vez conectado" (R7, AMB-3). No dice cómo obtiene la GUI esos datos, y el profesor tiene que poder ejecutar el programa sin instrucciones aparte.

**Decisión.**
- Al arrancar, la GUI muestra una **ventana de conexión** con host (`155.210.152.51`), puerto (`3270`), usuario (`grupo_02`) y clave (`secreto6`) **ya rellenados y editables**. Basta con pulsar "Conectar".
- Si el login falla, se muestra el error y se pueden corregir los datos sin cerrar el programa.
- **La clave `secreto6` no se cambia.**

**Por qué es lógico.**
- El profesor conoce la clave inicial y el programa funciona sin tocar nada.
- Si la clave cambia (la cambia el grupo o el profesor resetea las cuentas), basta con escribir la nueva en la ventana: no hay que recompilar ni editar ficheros.
- No cambiar la clave evita tener que comunicarla y que los tres integrantes la conozcan.

**Descartado.** Credenciales fijas en el código (si la clave cambia, el programa deja de funcionar sin remedio). Fichero de configuración (obliga a editarlo a mano).

**Si se cambia la clave.** Escribir la nueva en la ventana de conexión al arrancar. Si se quiere que salga precargada, cambiar el valor por defecto en el código de la ventana de conexión, recompilar y regenerar el zip. Indicarla en la memoria y en el `LEEME.txt` para el profesor.

## D2 — Alcance de la GUI según el mapa de pantallas de `tareas.c` (2026-09-28)

**Problema.** El guion pide "definir tareas y listarlas" y deja el diseño de la interfaz a cada grupo (R10, AMB-5). El recorrido a mano (`docs/mapa-pantallas.md`) muestra que `tareas.c` tiene tareas generales (fecha y descripción) y específicas (fecha, nombre y descripción), que solo acepta una palabra por campo, que no valida las fechas, que cada tarea cabe en una línea de 31 caracteres y que, en nuestras pruebas, las tareas no sobreviven a reiniciar el programa (AMB-8).

**Decisión.**
- **Formulario de 3 campos, como la maqueta del guion:** fecha, nombre y descripción, con el botón "Nueva". Con el nombre vacío se crea una tarea **general**; con nombre, una **específica**.
- **Tabla** con Nº, tipo, fecha, nombre y descripción, con barra de desplazamiento, y botón "Salir".
- **Validaciones antes de enviar nada al mainframe:**
  - Fecha `DDMM` real: 4 cifras, mes de 01 a 12 y día válido para ese mes.
  - Sin espacios en el nombre ni en la descripción; si los hay, se avisa y no se envía.
  - Longitud: tarea general, descripción de 12 caracteres como máximo; tarea específica, nombre y descripción de 16 como máximo entre los dos.
- **`tareas.c` se arranca una vez al conectar** y se sale con `3.EXIT` solo al cerrar la GUI.

**Por qué es lógico.**
- Sigue la maqueta del guion y cubre las dos funciones reales de `tareas.c` sin pantallas de más.
- Las validaciones evitan los fallos de `tareas.c` que hemos visto: un espacio hace que salte preguntas o que entre en bucle, un texto largo estropea la tarea siguiente en el listado y una fecha imposible se guarda tal cual.
- Rechazar en lugar de corregir: lo que ve el usuario es exactamente lo que se guarda.
- Mantener `tareas.c` abierto evita perder las tareas de la sesión y el coste de recompilar (unos 3 s) en cada operación.

**Descartado.** Selector de tipo General/Específica (se aleja de la maqueta sin aportar nada). Solo tareas generales (deja fuera una función que `tareas.c` tiene). Cambiar los espacios por `_` (modifica el texto sin que el usuario lo pida). Relanzar `tareas.c` en cada operación (pierde las tareas y es lento).

**Pendiente.** AMB-8: confirmar si las tareas deberían sobrevivir a un reinicio de `tareas.c` ("los datos se almacenan en disco"). Si el profesor aclara que sí y hay que hacer algo distinto, se revisa esta decisión antes de tocar el código.
