# Requisitos — Sistemas Legados, Práctica 2 (2026-27)

Fuente: `Practica_2_2026_2027.pdf` (2 páginas; el análisis se hace sobre una conversión local a markdown, revisada a mano contra el PDF, que no se sube).
Hash SHA-256 del guion: `7f72826e58007f45413819a5799d385ff8836aa479f8ced193d4ef7afc060129`

Tipos: **E** explícito · **I** implícito (se deriva necesariamente) · **A** ambiguo (más de una lectura razonable).
Estado: Pendiente salvo que se indique; nada se marca Cumplido sin evidencia (fichero y línea, ejecución o captura). Creado el 2026-09-27.

---

## 2.4 Estructura del guion (orden y nombres exactos)

1. Objetivos
2. Descripción de sistema
3. Terminales IBM 3270
4. Aplicación legada
5. Acceso 3270 displayless
6. Aplicación que hay que construir
7. Documentación
8. Referencias
   - Nota al pie *i* (MUY IMPORTANTE), llamada desde "Acceso 3270 displayless"

---

## 2.1 Requisitos

### Objetivos (p.1)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R1 | Objetivos | Programa con interfaz gráfica de ventanas que da acceso a la aplicación legada del mainframe a través de TN3270 | "Implementar un programa que permita el acceso mediante una interfaz gráfica de ventanas a una aplicación legada ejecutada sobre un mainframe accedido desde un terminal TN3270" | p.1, Objetivos, viñeta 1 | E | Pendiente | |
| R2 | Objetivos | Entregar como máximo el lunes 19-oct-2026 | "Fecha límite: lunes 19 octubre 2026" | p.1, Objetivos, viñeta 2 | E (hora y canal: A, ver AMB-1) | Pendiente | |
| R3 | Objetivos | El fichero de entrega se llama SL-P2-nn.zip o .rar, con nn = nº de grupo | "Fichero a enviar: SL-P2-nn.zip/rar (nn: número de grupo)" | p.1, Objetivos, viñeta 2 | E (nn: A, ver AMB-2) | Pendiente | |

### Descripción de sistema (p.1)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R4 | Descripción de sistema | La GUI trabaja contra el mainframe real (IBM ESA/390 con MUSIC/SP), no contra una copia local | "La aplicación a la que se desea dotar de GUI se ejecuta sobre un mainframe IBM ESA/390 [1] con el sistema operativo MUSIC/SP [2]" | p.1, Descripción de sistema | I | Pendiente | |

### Terminales IBM 3270 (p.1)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R5 | Terminales IBM 3270 | Se usa la familia de emuladores x3270 | "En esta práctica vamos a utilizar x3270 [4], software libre con licencia BSD que puede funcionar tanto en Windows como en GNU/Linux" | p.1, Terminales IBM 3270, párr. 2 | E | En curso | Parte manual hecha: wc3270 v4.5ga6 instalado y usado para el login (`ENTORNO.md`, `img/a1/`). Falta que la GUI use ws3270 (R14). |
| R6 | Terminales IBM 3270 | Conexión al mainframe en 155.210.152.51, puerto 3270 | "nuestro mainframe está en la dirección IP 155.210.152.51 y es accesible a través del puerto 3270" | p.1, Terminales IBM 3270, párr. 3 | E | Cumplido | Sesión `grupo02.wc3270` con `155.210.152.51:3270`; conexión real el 2026-09-28 que llega a MUSIC/SP (`img/a1/01-bienvenida.png`). |
| R7 | Terminales IBM 3270 | Login con la cuenta del grupo `grupo_XX` y clave inicial `secreto6` | "cada grupo de alumnos usará la cuenta "grupo_XX", donde XX es su número de grupo, y la clave inicial "secreto6" (se puede cambiar una vez conectado)" | p.1, Terminales IBM 3270, párr. 3 | E (cómo obtiene la GUI las credenciales: A, ver AMB-3) | En curso | Login manual con `grupo_02` / `secreto6` correcto, clave sin cambiar (`img/a1/02-login.png`, `img/a1/04-menu-fsi.png`). Falta el login desde la GUI (D1, B4). |

#### Observaciones del primer login (2026-09-28), para el mapa de pantallas

Comprobado con wc3270 y `x3270if`; texto de cada pantalla en `img/a1/*.txt`.

1. **Paginación de MUSIC:** la bienvenida y el mensaje tras el login terminan en `More...`; se avanza con Intro.
2. **Modelo de terminal:** con el modelo 4 (43x80), MUSIC usa las 43 filas (la línea de separación y el `More...` salen en las filas 42-43). El modelo que use la GUI tiene que ser el mismo que el del mapa de pantallas. **Decidido (2026-09-28): modelo 4, 43x80**, para el mapa y para la GUI.
3. **Pantalla de login:** `MUSIC Userid:` (fila 3, cursor en la columna 18) y `Password:` (fila 5, campo oculto); se pasa de uno a otro con Tab; F3 = Sign off.
4. **Sesión ya abierta:** si la sesión anterior no se cerró con `off`, al hacer login sale `Userid is in use. Type OK to cancel previous session` (`img/a1/03-sesion-en-uso.png`). Hay que responder `OK` e Intro enseguida: si se tarda, MUSIC vuelve a la bienvenida y hay que repetir el login.
5. **Tras el login:** mensajes de la cuenta (`Press ENTER to continue...`) y luego el menú FSI, con la línea `Command ===>` en la fila 3.
6. **Salida correcta:** `off` en la línea de comandos cierra la sesión de MUSIC y vuelve a la bienvenida; la conexión sigue abierta hasta que se desconecta el emulador. Salir sin `off` deja la sesión colgada (caso 4).
7. **`help`:** abre la ayuda del FSI (11 páginas, F7/F8; F3 vuelve).

### Aplicación legada (p.1-2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R8 | Aplicación legada | Antes de hacer la GUI, ejecutar la aplicación legada a mano y entender su funcionamiento (definir tareas y listarlas) | "Antes de realizar la GUI solicitada en el enunciado lo primero será ejecutar la aplicación legada y entender su funcionamiento […] Usa la aplicación para definir tareas y listarlas posteriormente" | p.1, Aplicación legada | E | En curso | Recorrido a mano el 2026-09-28: altas generales y específicas, listados, EXIT y errores; `docs/mapa-pantallas.md`, `img/a2/`. Casos límite probados en una segunda sesión (fechas, tildes, longitudes, listado largo): límite de 31 caracteres por tarea (§5 del mapa). Queda abierta la persistencia (AMB-8). |
| R9 | Aplicación legada | La aplicación legada se arranca escribiendo `tasks.c` en la línea de comandos de MUSIC/SP; la GUI debe hacer lo mismo | "debe escribirse directamente `tasks.c` en la línea de comandos" | p.1, Aplicación legada | E (arranque manual) / I (que lo haga la GUI) | En curso | **Errata:** `tasks.c` da `ERR04 File not accessible`; el programa real es `tareas.c` (`img/a2/00-tasks-c-no-existe.png`, `img/a2/01-tareas-arranque.png`). Arranque manual comprobado; falta que lo haga la GUI. |
| R10 | Aplicación legada | La GUI cubre las funciones de la aplicación legada: asignar (definir) tareas y verlas (listar) | "Usa la aplicación para definir tareas y listarlas" + captura "MAIN MENU · 1 - ASSIGN TASKS · 2 - VIEW TASKS · 0 - EXIT" | p.1, Aplicación legada y figura | I (alcance exacto: A, ver AMB-5) | En curso | Funciones reales: alta general (fecha, descripción), alta específica (fecha, nombre, descripción), listado general, listado específico (`docs/mapa-pantallas.md` §3-5). El menú real difiere de la figura (`3.EXIT`, no `0 - EXIT`). |
| R11 | Aplicación legada | Los datos se obtienen únicamente a través de `tasks.c` | "Para encapsularla deberemos obtener los datos a través de la aplicación legada `tasks.c`" | p.2, Aplicación legada (continuación) | E | Pendiente | |
| R12 | Aplicación legada | No se modifica `tasks.c` | "la cual no debemos alterar" | p.2, Aplicación legada | E (restricción) | Pendiente | |
| R13 | Aplicación legada | No se accede directamente al fichero de datos de `tasks.c` | "ni tampoco debemos acceder directamente al fichero de datos que maneja" | p.2, Aplicación legada | E (restricción) | Pendiente | |

### Acceso 3270 displayless (p.2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R14 | Acceso 3270 displayless | El acceso se hace por *screen-scraping* con s3270 (ws3270 en Windows): comandos de script por la entrada estándar y captura de pantalla por la salida estándar | "una de ellas es s3270 (ws3270 en Windows) desarrollada para realizar aplicaciones screen-scraping […], que es lo que nosotros queremos. Su principio de funcionamiento es mediante comandos script por la entrada estándar […] y capturas de la pantalla por la salida estándar" | p.2, Acceso 3270 displayless | E (si admite otra vía: A, ver AMB-4) | En curso | `SL-P2-02/src/mainframe/MainframeClient.java`: lanza ws3270 como proceso hijo, escribe las órdenes en su entrada y lee `data:`/`ok`/`error` de su salida (`ejecutar()`). Ejecución real de `Main` el 2026-10-02 14:16: `Connect`, lectura de la bienvenida de MUSIC/SP (43 filas) y `Disconnect`. Por script (2026-09-28) se recorrió login → `tareas.c` → alta → listado → `3.EXIT` → `off`. Falta el login y `tareas.c` desde Java. |
| R15 | Acceso 3270 displayless (nota *i*) | Entre la conexión por script y la desconexión pasa **al menos 1 s**, también cuando el programa termina por un error | "hay que asegurarse que al menos pase 1 seg entre la conexión por script con el mainframe y la desconexión, ya sea por finalizar el programa o por haberse producido un error durante la ejecución" | p.2, nota al pie *i* | E (restricción crítica) | En curso | `MainframeClient.desconectar()` es el único punto de desconexión: espera hasta 1,5 s desde que `Connect` responde `ok`, también desde el cierre de emergencia (*shutdown hook*). Ejecución real del 2026-10-02: `DESCONECTADO tras 1501 ms`. Con un ws3270 simulado: 1,50 s en cierre normal, Hercules, Ctrl+C y error no capturado (`docs/correcciones-prueba-java.md`). Falta probar contra el mainframe el cierre por error y por Ctrl+C, y los cierres de la GUI. |
| R16 | Acceso 3270 displayless | El programa sincroniza con el mainframe: espera a que cada pantalla esté lista antes de leerla o de enviar la siguiente orden | "todo aquello que hayáis tenido que hacer más allá […] (problemas de sincronización, etc.)" | p.2, Documentación | I | En curso | `MainframeClient.esperarPantallaEstable()`: repite `Ascii()` hasta dos lecturas iguales, con texto y sin `Working`; cada orden tiene tiempo máximo de respuesta (40 s). Las esperas `Wait(InputField)` y `Wait(Output)` de ws3270 vuelven antes de que MUSIC dibuje (prueba por script del 2026-09-28). Ejecución real del 2026-10-02: bienvenida estable a la tercera lectura. `hayMas()` detecta `More...`. Falta usarlo en el login y en los listados. |

### Aplicación que hay que construir (p.2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R17 | Aplicación que hay que construir | Lenguaje y plataforma libres (limitados por R26: ejecución en Windows) | "podrá utilizarse cualquier lenguaje y plataforma" | p.2, Aplicación que hay que construir, párr. 1 | E | Pendiente | |
| R18 | Aplicación que hay que construir | El emulador se puede lanzar como proceso hijo de la propia aplicación, capturando su entrada y salida estándar | "El emulador 3270 puede ejecutarse como un proceso dentro de la propia aplicación que se desarrolle" + ejemplo `Runtime.getRuntime().exec(...)` | p.2, Aplicación que hay que construir, párr. 1 | E (opcional: "puede") | Pendiente | |
| R19 | Aplicación que hay que construir | Interfaz de ventanas diseñada por el grupo, la que considere más adecuada; la maqueta es solo un ejemplo | "Un ejemplo de la interfaz gráfica de ventanas requerida puede ser el siguiente, pero se espera de cada grupo que diseñe el interfaz que considere más adecuado para esta aplicación" | p.2, Aplicación que hay que construir, párr. 2 | E | Pendiente | |
| R20 | Aplicación que hay que construir | Como mínimo, lo que sugiere la maqueta: ventana "Tareas" con campos para una tarea nueva y botón "Nueva", tabla de tareas con desplazamiento y botón "Salir" | Maqueta: 3 campos + "Nueva"; tabla de 3 columnas con barra de desplazamiento; "Salir" | p.2, figura | I | Pendiente | |
| R21 | Aplicación que hay que construir | "Salir" termina la sesión con el mainframe de forma ordenada (salir de `tasks.c`, cerrar sesión, respetar R15), también al cerrar la ventana | Deriva de R15 y del botón "Salir" de la maqueta | p.2 | I | Pendiente | |

### Documentación (p.2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R22 | Documentación | Memoria de 4-5 páginas aproximadamente | "Redactar 4-5 páginas aprox." | p.2, Documentación | E | Pendiente | |
| R23 | Documentación | Principales dificultades encontradas | "describiendo las principales dificultades encontradas" | p.2, Documentación | E | Pendiente | |
| R24 | Documentación | Todo lo hecho más allá de lo descrito (problemas de sincronización, etc.) | "todo aquello que hayáis tenido que hacer más allá de lo descrito en la memoria (problemas de sincronización, etc.)" | p.2, Documentación | E (redacción: A, ver AMB-6) | Pendiente | |
| R25 | Documentación | Pantallazos de interés | "pantallazos que se consideren de interés" | p.2, Documentación | E | Pendiente | |
| R26 | Documentación | Distribución de tareas entre los miembros del grupo | "distribución de tareas entre los miembros del grupo" | p.2, Documentación | E | Pendiente | |
| R27 | Documentación | Cronograma de las tareas | "cronograma de las mismas" | p.2, Documentación | E | Pendiente | |
| R28 | Documentación | Cualquier otra información para entender el alcance del trabajo | "cualquier otra información que se crea de importancia para entender el alcance de la tarea realizada" | p.2, Documentación | E | Pendiente | |
| R29 | Documentación | Todo lo necesario para ejecutar la práctica desde Windows con doble clic en un fichero | "incluyendo todo lo necesario para poder ejecutar las practicas desde Windows haciendo doble click en un fichero" | p.2, Documentación | E (si debe ser autocontenido: A, ver AMB-7) | Pendiente | |

---

## 2.2 Restricciones (lo prohibido o limitado)

- **No modificar `tasks.c`** (R12).
- **No acceder directamente a su fichero de datos**: todo pasa por la aplicación legada (R11, R13).
- **Mínimo 1 s entre conexión y desconexión**, incluidos los errores (R15). Si no se cumple, el mainframe deja de aceptar conexiones **para todos los grupos** hasta que el profesor lo resetee. Afecta también a las pruebas de desarrollo (no hacer sondeos rápidos de puerto ni conexiones de prueba que se corten al instante).
- Emulador de la familia **x3270**, acceso por **s3270 / ws3270** (R5, R14).
- Ejecución **en Windows con doble clic** en un fichero (R29).
- Entrega **`SL-P2-nn.zip`/`.rar`** antes del **lunes 19-oct-2026** (R2, R3). Memoria de **4-5 páginas** aprox. (R22).

## 2.3 Entregables y fechas

- **Qué:** fichero `SL-P2-02.zip` con el programa, lo necesario para ejecutarlo en Windows con doble clic y la memoria (4-5 págs.).
- **Cuándo:** lunes 19-oct-2026 23:59 (AMB-1).
- **Dónde:** por email (AMB-1).

## Ambigüedades

| ID | Cita | Lecturas posibles | Resolución |
|---|---|---|---|
| AMB-1 | "Fecha límite: lunes 19 octubre 2026" | Hora y canal no indicados | **Resuelta (2026-09-27):** por email antes de las 23:59, como en la P1 |
| AMB-2 | "SL-P2-nn.zip/rar (nn: número de grupo)" | Número de grupo y formato | **Resuelta:** grupo 02 (mismo equipo que la P1), `SL-P2-02.zip` |
| AMB-3 | "usará la cuenta "grupo_XX" […] clave inicial "secreto6" (se puede cambiar una vez conectado)" | Credenciales fijas, ventana de conexión o fichero; cambiar o no la clave | **Resuelta (D1):** ventana de conexión precargada y editable; la clave no se cambia |
| AMB-4 | "s3270 (ws3270 en Windows) […] que es lo que nosotros queremos" | Obligatorio s3270/ws3270 u otra vía | **Resuelta:** s3270/ws3270 obligatorio |
| AMB-5 | "Usa la aplicación para definir tareas y listarlas" + menú de 3 opciones | Alcance exacto (campos, validaciones, paginación, otras opciones) | **Resuelta (2026-09-28, D2), a partir de `docs/mapa-pantallas.md`:** (1) formulario de 3 campos como la maqueta (fecha, nombre, descripción) y botón Nueva: nombre vacío = tarea general, con nombre = específica; tabla con Nº, tipo, fecha, nombre y descripción, y botón Salir. (2) Espacios en nombre o descripción: se rechazan con aviso. (3) Fecha: `DDMM` con día y mes reales. (4) Longitudes: general, descripción ≤ 12; específica, nombre + descripción ≤ 16 (límite de 31 caracteres por tarea). (5) Mientras AMB-8 siga abierta, `tareas.c` se arranca una vez al conectar y se sale con `3.EXIT` solo al cerrar. |
| AMB-6 | "más allá de lo descrito en la memoria" | Errata por "en el enunciado" o lectura literal | **Resuelta (interpretación):** errata por "enunciado"; la memoria cuenta lo hecho más allá de lo que pide el guion |
| AMB-7 | "todo lo necesario para poder ejecutar las practicas desde Windows haciendo doble click en un fichero" | Zip autocontenido o instrucciones de instalación | **Resuelta:** autocontenido (runtime Java, ws3270 y `lanzar.bat` dentro; no se instala nada) |
| AMB-8 | "Usa la aplicación para definir tareas y listarlas posteriormente (los datos se almacenan en disco)" | Las tareas deberían sobrevivir a un reinicio de `tareas.c`, pero en la prueba del 2026-09-28 no sobrevivió ninguna (ni saliendo con `3.EXIT`) | **Abierta, preguntar:** primero al grupo y a otros grupos; si nadie lo sabe, al profesor. Provisional: la GUI arranca `tareas.c` una vez y lo mantiene abierto toda la sesión (`docs/mapa-pantallas.md` §7) |
| AMB-9 | "debe escribirse directamente `tasks.c` en la línea de comandos" | El fichero `tasks.c` no existe en `grupo_02` | **Resuelta (2026-09-28):** errata del guion, el programa es `tareas.c` |

Pendientes de preguntar al profesor: AMB-8 (persistencia de las tareas), si el grupo no lo resuelve antes.
