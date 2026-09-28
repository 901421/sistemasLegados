# Reparto de tareas — Práctica 2 (grupo 02)

Tres paquetes de trabajo con la misma carga. Los nombres se asignan cuando lo decida el grupo.
Las horas son una estimación aproximada para comparar cargas, no una medida.
Referencias: `Rn` = requisito y `AMB-n` = ambigüedad de `docs/requisitos.md`; `Dn` = decisión de `DECISIONES.md`.

| Paquete | Responsable | Tareas (horas estimadas) | Total |
|---|---|---|---|
| **A — Aplicación legada y pantallas** | *(por asignar)* | Instalar wc3270 y primer login (1) · mapa de pantallas de `tasks.c` (4) · guardar pantallas reales con s3270 (1,5) · código que lee las pantallas (listado, paginación "More...", errores) y sus tests (5) · pruebas completas contra el mainframe con los casos límite (3) | **≈14,5 h** |
| **B — Conexión y entorno** | *(por asignar)* | Lanzar ws3270 desde Java y enviarle comandos (4,5) · sincronización, regla de 1 s, detección de "Hercules" y cierre ante errores (4) · ventana de conexión D1 (2,5) · Java reducido para Windows (jlink), ws3270 dentro del zip y `ENTORNO.md` (3) | **≈14 h** |
| **C — Ventana principal y entrega** | *(por asignar)* | Tabla de tareas con desplazamiento y refresco (3,5) · formulario de alta con validaciones (3) · trabajo en segundo plano, indicador y cierre con la X (3) · `build.bat`/`build.sh` y `lanzar.bat` (2) · montar el zip y probarlo en limpio (1,5) · capturas automáticas (2) | **≈15 h** |
| **Memoria** | Todos | Cada uno escribe la parte de su paquete (≈3 h por persona) y los tres revisan el conjunto. Reparto y cronograma a partir de git | igual para los tres |

## Por qué este reparto

- **Cada paquete es una parte separada del programa:** lo que se ve en pantalla (A), cómo se habla con el mainframe (B) y la ventana y la entrega (C). Así hay pocos cambios pisándose entre personas. La regla de 1 s va en el mismo paquete que el código que habla con ws3270, porque tienen que ir juntos.
- **Todos pueden empezar el primer día:** A hace el mapa; B prueba ws3270 a mano y monta la conexión; C hace la ventana con datos inventados hasta tener el mapa.
- **Todos tienen algo difícil:** A, leer las pantallas con paginación; B, la regla de 1 s y la sincronización; C, que la ventana no se congele mientras espera al mainframe.

## Dependencias y riesgos

- **A va por delante de los demás.** El mapa de pantallas (A2) y las pantallas guardadas (A3) los necesitan B (qué esperar tras cada orden) y C (qué campos tiene una tarea). Objetivo: tenerlos en los **3-4 primeros días**.
- **Regla de 1 s (R15):** si se incumple, el mainframe deja de aceptar conexiones **para todos los grupos** hasta que el profesor lo resetee. Nadie hace sondeos de puerto (`nc`, `telnet`…) ni conexiones de prueba que se corten al instante. Mientras B no tenga terminado el cierre seguro (B3), las conexiones por script se hacen a mano y esperando varios segundos antes de desconectar.
- **Puntos de unión:** A usa la conexión de B; C usa la lectura de pantallas de A y la conexión de B. Las firmas de estas clases se acuerdan entre los tres antes de programarlas (tarea T1).
- **Entrega:** lunes 19-oct-2026, 23:59, por email, `SL-P2-02.zip`.

---

## Lista detallada de tareas

Formato: `[ ]` pendiente · `[x]` hecha. Cada tarea indica los requisitos que cubre, de qué depende y cuándo se considera hecha.

### Tareas comunes

- [ ] **T1 — Acordar las piezas del programa y cómo se llaman entre sí** *(todos, al empezar)*
  - Nombres de paquetes y clases; qué ofrece la conexión (B) a los demás (conectar, enviar orden, leer pantalla, desconectar) y qué devuelve la lectura de pantallas (A) (lista de tareas, resultado de un alta, error).
  - Hecha cuando: las firmas están escritas en un fichero del repo y los tres las han revisado.
- [ ] **T2 — Rama de trabajo** *(todos)*
  - Se trabaja en la rama `practica2`; commits pequeños y con mensaje claro; `git pull` antes de empezar cada día.
- [ ] **T3 — Memoria** *(todos, al final)* — R22-R29. Ver tareas M1-M4.

### Paquete A — Aplicación legada y pantallas (≈14,5 h)

- [x] **A1 — Instalar wc3270 y primer login** (1 h) — R5, R6, R7
  - Descargar wc3270 de x3270.bgp.nu e instalarlo con el *Session Wizard* (no hace falta administrador).
  - Sesión con host `155.210.152.51`, puerto `3270`.
  - Comprobar desde dónde se llega al mainframe: casa, VPN de Unizar o laboratorio.
  - Login con `grupo_02` / `secreto6`. **No cambiar la clave** (D1). Probar el comando `help`.
  - Dejar la sesión abierta unos segundos antes de salir (R15).
  - Hecha cuando: capturas de la bienvenida de MUSIC/SP y de la sesión iniciada; versión de wc3270 anotada en `ENTORNO.md`.
- [ ] **A2 — Mapa de pantallas de `tasks.c`** (4 h) — R8, R9, R10; resuelve AMB-5
  - Arrancar `tasks.c` escribiéndolo en la línea de comandos.
  - Recorrer todas las opciones: `1 - ASSIGN TASKS`, `2 - VIEW TASKS`, `0 - EXIT`.
  - Para cada pantalla, anotar: textos fijos, posición (fila y columna) de cada campo, longitud máxima, teclas que acepta (Enter, PF3, Clear…), mensajes de error y a qué pantalla lleva cada acción.
  - Casos a probar: listado vacío; muchas tareas (paginación "More..."); campos vacíos; campos más largos de lo que admite; tildes y ñ (el mainframe usa EBCDIC); cómo se sale de `tasks.c` y de la sesión.
  - **No modificar `tasks.c` ni buscar su fichero de datos** (R12, R13).
  - Hecha cuando: documento con una sección por pantalla, capturas y las transiciones comprobadas a mano; alcance de la GUI acordado con el grupo.
- [ ] **A3 — Guardar pantallas reales para los tests** (1,5 h) — R16
  - Con s3270 (o ws3270), guardar el texto de cada pantalla del mapa (orden `Ascii`) en ficheros del repo: menú, alta, alta correcta, errores de alta, listado vacío, listado con una página, listado con varias páginas, pantalla de login incorrecto.
  - Respetar la regla de 1 s en cada conexión.
  - Hecha cuando: ficheros de pantallas en el repo, con nombre descriptivo y una línea de qué muestra cada uno.
- [ ] **A4 — Código que lee las pantallas y sus tests** (5 h) — R10, R11, R16
  - Clases que, a partir del texto de una pantalla, sacan: en qué pantalla estamos, la lista de tareas, si hay más páginas, y si un alta fue bien o dio error (con el mensaje).
  - Tests con las pantallas de A3, sin conectarse al mainframe.
  - Depende de: A2, A3, T1.
  - Hecha cuando: todos los tests pasan y cubren cada pantalla guardada.
- [ ] **A5 — Pruebas completas contra el mainframe** (3 h) — R1, R10, R15, R21
  - Con el programa ya montado (B y C): conectar, listar, crear tareas, comprobar en wc3270 a mano que aparecen, salir.
  - Casos límite acordados: login incorrecto, mainframe no disponible, listado vacío y largo, datos no válidos, cierre con la X, cierre por error.
  - Comprobar en el registro que siempre pasa ≥ 1 s entre conexión y desconexión.
  - Depende de: A4, B4, C3.
  - Hecha cuando: lista de casos con resultado y captura de cada uno.

### Paquete B — Conexión y entorno (≈14 h)

- [ ] **B1 — Conseguir s3270 y ws3270** (incluida en B5) — R5, R14
  - s3270 para Linux/WSL y ws3270 para Windows (viene con wc3270).
  - Anotar versiones en `ENTORNO.md`.
- [ ] **B2 — Lanzar ws3270 desde Java y enviarle comandos** (4,5 h) — R14, R18
  - Arrancar ws3270/s3270 como proceso hijo (`ProcessBuilder`), escribir órdenes por su entrada y leer la respuesta por su salida (fin de respuesta: `ok` / `error`).
  - Órdenes básicas: `Connect`, `Wait`, `String`, `Enter`, las teclas PF que use `tasks.c`, `Ascii`, `Disconnect`.
  - Login automático y arranque de `tasks.c`; salida de `tasks.c` y logoff.
  - Probar primero a mano el guion de órdenes, esperando varios segundos antes de desconectar.
  - Hecha cuando: una prueba conecta, entra, abre `tasks.c`, lee el menú y sale sin errores.
- [ ] **B3 — Sincronización, regla de 1 s y cierre seguro** (4 h) — R15, R16, R21
  - Tras cada orden, esperar a que la pantalla esté lista (`Wait(InputField)`, `Wait(Output)` o comprobar el texto esperado) antes de leer o enviar la siguiente; tiempo máximo con error claro si no llega.
  - **Un único punto de desconexión**: guarda la hora de conexión y espera hasta **≥ 1,5 s** antes de cerrar, en todos los casos: Salir, cierre con la X, excepción, Ctrl+C (shutdown hook).
  - Detectar la pantalla "Hercules" (mainframe bloqueado) y avisar sin reintentar en bucle.
  - Registro con la hora de conexión y desconexión para poder demostrar el segundo.
  - Hecha cuando: pruebas de cierre normal, por error y por Ctrl+C con ≥ 1 s medido en el registro.
- [ ] **B4 — Ventana de conexión (D1)** (2,5 h) — R7
  - Campos host, puerto, usuario y clave precargados (`155.210.152.51`, `3270`, `grupo_02`, `secreto6`) y editables; botón "Conectar".
  - Si el login falla o no hay red, mensaje claro y se puede corregir sin cerrar el programa.
  - La conexión se hace en segundo plano; la ventana no se congela.
  - Hecha cuando: probado con datos correctos, clave errónea y sin red.
- [ ] **B5 — Java reducido para Windows, ws3270 dentro del zip y `ENTORNO.md`** (3 h) — R29, AMB-7
  - Generar con `jlink` un Java mínimo **para Windows** (necesita un JDK de Windows) con solo los módulos que use el programa.
  - Copiar `ws3270.exe` (y lo que necesite) a la carpeta de la entrega.
  - `ENTORNO.md`: versiones de Java, jlink, wc3270/ws3270 y s3270; cómo se obtiene cada cosa.
  - Hecha cuando: el programa arranca en un Windows sin Java instalado usando solo lo de la carpeta.

### Paquete C — Ventana principal y entrega (≈15 h)

- [ ] **C1 — Tabla de tareas** (3,5 h) — R10, R19, R20
  - Ventana "Tareas" con una tabla de columnas según el mapa (A2), barra de desplazamiento y refresco (botón y automático tras un alta).
  - Mientras no esté el mapa, trabajar con datos inventados.
  - Hecha cuando: muestra el listado real, vacío y largo.
- [ ] **C2 — Formulario de alta con validaciones** (3 h) — R10, R19, R20
  - Campos de una tarea nueva y botón "Nueva".
  - Validar antes de enviar: campos obligatorios, longitud máxima, caracteres que el mainframe no admite (según A2).
  - Mostrar el resultado del alta (correcto o el error del mainframe).
  - Hecha cuando: probado con datos válidos, vacíos, demasiado largos y con tildes.
- [ ] **C3 — Trabajo en segundo plano, indicador y cierre** (3 h) — R15, R21
  - Todas las operaciones con el mainframe en segundo plano (`SwingWorker`), con indicador de "trabajando" y botones desactivados mientras tanto.
  - Botón "Salir" y cierre con la X: llaman al cierre seguro de B3 y esperan a que termine.
  - Hecha cuando: la ventana nunca se congela y el cierre siempre pasa por B3.
- [ ] **C4 — `build.bat`, `build.sh` y `lanzar.bat`** (2 h) — R29
  - `build.bat` / `build.sh`: compilan el programa y dejan la carpeta de ejecución lista.
  - `lanzar.bat`: arranca el programa con el Java de B5 con doble clic, sin ventana de consola si es posible, y funciona en rutas con espacios y paréntesis.
  - Hecha cuando: doble clic en `lanzar.bat` en Windows abre la ventana de conexión.
- [ ] **C5 — Montar el zip y probarlo en limpio** (1,5 h) — R3, R29
  - `SL-P2-02.zip` generado desde el commit final (`git archive`) más lo compilado: programa, Java de B5, ws3270, `lanzar.bat`, `LEEME.txt` y memoria PDF.
  - Descomprimir en una carpeta nueva de Windows (ruta con espacios) y probar con doble clic.
  - Hecha cuando: la prueba en limpio funciona de principio a fin.
- [ ] **C6 — Capturas automáticas** (2 h) — R25
  - Script PowerShell que abre el programa y saca las capturas para la memoria (conexión, listado, alta, errores). Avisar antes de lanzarlo porque toma el teclado y el ratón.
  - Hecha cuando: las capturas están en `img/` de la práctica y se pueden repetir.

### Memoria (todos)

- [ ] **M1 — Parte de cada paquete** (≈3 h por persona) — R23, R24, R25, R28
  - A: la aplicación legada, el mapa de pantallas y la lectura de pantallas.
  - B: la conexión, la sincronización, la regla de 1 s y el entorno.
  - C: la interfaz, el diseño elegido frente a la maqueta y la entrega.
  - Cada uno incluye las dificultades que encontró y lo que hizo más allá del enunciado.
- [ ] **M2 — Reparto y cronograma** — R26, R27
  - Tabla de reparto (este fichero) y cronograma sacado de las fechas de los commits.
- [ ] **M3 — Cómo ejecutarlo en Windows** — R29
  - Doble clic en `lanzar.bat`; credenciales precargadas (D1); qué hacer si la clave cambia.
- [ ] **M4 — Revisión conjunta y PDF** — R22
  - 4-5 páginas aprox. más portada; revisión de los tres; exportar a PDF y meterlo en el zip (C5).
