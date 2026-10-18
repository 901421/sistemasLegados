# Reparto de tareas — Práctica 2 (grupo 02)

Reparto de lo que queda por hacer a 2026-10-02, por persona. Cada uno tiene una parte de cada zona del programa (conexión, pantallas, ventana, entrega y pruebas), y cada tarea la revisa otra persona.
Las horas son una estimación aproximada para comparar cargas, no una medida.
Referencias: `Rn` = requisito y `AMB-n` = ambigüedad de `docs/requisitos.md`; `Dn` = decisión de `DECISIONES.md`. Qué fichero lleva cada uno y cómo se trabaja: `docs/convenios.md`.

| Zona | Paula (empieza la primera) | Imad | Ainhoa |
|---|---|---|---|
| **Conexión** | Login, arranque de `tareas.c`, salida con `off` y "Userid is in use" (2,5) | Guardar pantallas reales para los tests (1,5) · prueba real de cierre por error y Ctrl+C (1) | Ventana de conexión D1 (2,5) |
| **Pantallas** | Leer los listados con paginación "More..." y sus tests (3) | Alta de una tarea: navegar los menús y leer el resultado (2) | Validaciones de D2 y sus tests (dentro del formulario) |
| **Ventana** | Tabla de tareas con desplazamiento y refresco (3,5) | Trabajo en segundo plano, indicador y cierre con la X (3) | Formulario de alta con el botón "Nueva" (3) |
| **Entrega** | Montar el zip y probarlo en limpio (1,5) | Java reducido para Windows (jlink), ws3270 dentro del zip y `ENTORNO.md` (3) | `build.bat`/`build.sh` y `lanzar.bat` (2) · capturas automáticas (2) |
| **Pruebas finales** | Listado vacío y largo (0,5) | Casos de conexión y cierre (1) · leer [1] y [2] para la memoria (0,5) | Datos no válidos y tildes (1,5) |
| **Total** | **≈11 h** | **≈12 h** | **≈11 h** |
| **Memoria** (≈3 h cada uno) | Aplicación legada, mapa de pantallas y lectura de listados | Conexión, sincronización, regla de 1 s y entorno | Interfaz, diseño frente a la maqueta y cómo ejecutarlo |

**Revisión cruzada:** Imad revisa lo de Paula, Paula lo de Ainhoa y Ainhoa lo de Imad.

## Por qué este reparto

- **Paula empieza y lleva el login**, que es lo que necesitan los demás para probar contra el mainframe: así no depende de nadie.
- **Lo que se lee y lo que se muestra van juntos:** Paula lee los listados y hace la tabla; Ainhoa valida los datos y hace el formulario.
- **Imad sigue con la conexión**, porque la clase `MainframeClient` ya está hecha por él, y con lo que depende de ella (cierre seguro, segundo plano).
- **Todos tienen algo difícil:** Paula, la paginación de los listados; Imad, que la ventana no se congele y cierre siempre bien; Ainhoa, las validaciones y los errores de conexión.

## Orden y fechas orientativas

| Fecha | Qué tiene que estar |
|---|---|
| 5-oct | Convenios revisados por los tres (T1) · login desde Java (P1) · pantallas guardadas (I1) |
| 9-oct | Lectura de listados (P2) y alta (I3) con tests · validaciones (A2) · ventana de conexión (A1) |
| 13-oct | Ventana "Tareas" completa funcionando contra el mainframe (P3, I4, A2) |
| 16-oct | Zip probado en un Windows limpio (I5, A3, P4) · capturas (A4) · memoria en borrador |
| 19-oct | Revisión final y envío por email antes de las 23:59 |

## Dependencias y riesgos

- **El login (P1) va por delante de casi todo:** hasta que no esté, no se pueden guardar pantallas (I1) ni probar la ventana de conexión contra el mainframe (A1). Mientras tanto, lo que solo analiza texto se hace con las pantallas ya guardadas en `img/a1` e `img/a2`, y la ventana con datos inventados.
- **Regla de 1 s (R15):** si se incumple, el mainframe deja de aceptar conexiones **para todos los grupos** hasta que el profesor lo resetee. Solo `MainframeClient` conecta y desconecta; nadie hace sondeos de puerto ni conexiones de prueba (`docs/convenios.md`, apartado 3).
- **Una sola cuenta para los tres (`grupo_02`):** avisar por el grupo antes de cada conexión real.
- **AMB-8 (persistencia) sigue abierta:** las tareas no sobreviven a reiniciar `tareas.c`. Si el profesor aclara algo distinto, se revisa D2.
- **Entrega:** lunes 19-oct-2026, 23:59, por email, `SL-P2-02.zip`.

## Tareas realizadas (para la memoria)

Registro de lo hecho, con quién lo hizo y dónde está la evidencia. Sirve para el reparto y el cronograma de la memoria (R26, R27).

| Fecha | Tarea | Realizada por | Estado | Evidencia |
|---|---|---|---|---|
| 2026-09-28 | A1 — Instalar wc3270 y primer login | Imad, Paula y Ainhoa | Hecha | `ENTORNO.md`, `img/a1/` |
| 2026-09-28 | A2 — Mapa de pantallas de la aplicación legada (`tareas.c`) | Imad y Paula | Hecha | `docs/mapa-pantallas.md`, `img/a2/` |
| 2026-09-28 | A2 — Pantalla de mainframe bloqueado (Hercules) y cómo debe reaccionar la GUI | Imad, Paula y Ainhoa | Hecha | `docs/mapa-pantallas.md` §9, `img/a2/21-hercules-bloqueado` |
| 2026-09-28 | A2 — Casos límite de la aplicación legada: fechas, tildes, longitudes (límite de 31 caracteres por tarea) y listado largo | Imad | Hecha | `docs/mapa-pantallas.md` §2, §5 y §6, `img/a2/22` a `32` |
| 2026-09-28 | A2 — Alcance de la GUI acordado a partir del mapa (AMB-5) | Imad | Hecha | `DECISIONES.md` D2, `docs/requisitos.md` AMB-5 |
| 2026-09-28 | B2 — Revisión y corrección de la prueba Java con ws3270: desconexión segura (1,5 s), UTF-8 y espera de pantalla | Imad | Hecha | `docs/correcciones-prueba-java.md`, `SL-P2-02/src/Main.java` |
| 2026-10-02 | B2/B3 — Clase de conexión `MainframeClient`: órdenes a ws3270 con tiempo máximo, espera de pantalla estable, punto único de desconexión (1,5 s, también con Ctrl+C) y detección de Hercules; probada contra el mainframe sin login | Imad | En curso | `SL-P2-02/src/mainframe/MainframeClient.java`, `docs/correcciones-prueba-java.md` |
| 2026-10-02 | T1 — Estructura del programa (esqueletos de las clases con sus firmas) y convenios de trabajo | Imad | Hecha, pendiente de que la revisen Paula y Ainhoa | `docs/convenios.md`, `SL-P2-02/src/` |
| 2026-10-07 | P3 — Tabla de tareas (`ModeloTablaTareas`): columnas, fecha "12 oct", orden y contadores | Imad | Hecha | `gui/ModeloTablaTareas.java`, `PruebasOffline` |
| 2026-10-07 | A2 — Formulario de alta (`PanelAlta`): tipo con botones, fecha con desplegables, contador y errores en rojo | Imad | Hecha | `gui/PanelAlta.java`, D3 |
| 2026-10-07 | I4 — Ventana de tareas: trabajo en segundo plano, indicador, Salir/X en orden, pestañas y buscador | Imad | Hecha, falta prueba real | `gui/VentanaTareas.java` |
| 2026-10-07 | A1 — Ventana de conexión (D1): datos precargados, errores y aviso de Hercules | Imad | Hecha, falta prueba real | `gui/VentanaConexion.java` |
| 2026-10-07 | Diseño de la interfaz (D3) y aspecto común (`Estilo`) | Imad | Hecha | `DECISIONES.md` D3, `gui/Estilo.java` |
| 2026-10-09 | A2 — `Validador`: fecha DDMM real, sin espacios y longitudes (D2) | Paula; corregido por Imad (espacios en la descripción) | Hecha | `tareas/Validador.java`, `docs/correcciones-revision-octubre.md` D |
| 2026-10-09 | P2 — Leer los listados (`LectorListado`): tareas generales y específicas | Paula; corregido por Imad (sin filtro, acordado con el grupo) | Hecha | `tareas/LectorListado.java`, `docs/correcciones-revision-octubre.md` C |
| 2026-10-14 | P2 — Recorrer el listado con `More...` (`GestorTareas.listar`) | Paula; rehecho por Imad según el mapa de pantallas | Hecha, falta prueba real | `tareas/GestorTareas.java`, `docs/correcciones-revision-octubre.md` A |
| 2026-10-14 | I3 — Alta de una tarea (`GestorTareas.nueva`) | Primera versión de Paula; rehecho por Imad | Hecha, falta prueba real | `tareas/GestorTareas.java`, `docs/correcciones-revision-octubre.md` B |
| 2026-10-13 | P1 — Login, arranque de `tareas.c` y cierre con `off` (`SesionMusic`), espera de texto en `MainframeClient` y pruebas contra el mainframe | Ainhoa; ampliado por Imad (salida con un alta a medias) | Hecha, falta la salida de sus pruebas reales | `mainframe/SesionMusic.java`, `pruebas/Prueba*.java`, `docs/correcciones-revision-octubre.md` E |
| 2026-10-16 | Reconocimiento de pantallas (`Pantalla.identificar`) | Paula; ampliado por Imad (estado `FORMULARIO`) y pruebas con las capturas reales | Hecha | `mainframe/Pantalla.java`, `PruebasOffline` |
| 2026-10-17 | Revisión de los commits del 9 al 16 de octubre y correcciones A-E | Imad | Hecha | `docs/correcciones-revision-octubre.md` |

---

## Lista detallada de tareas

Formato: `[ ]` pendiente · `[x]` hecha. Cada tarea indica los requisitos que cubre, de qué depende y cuándo se considera hecha.

### Ya hechas

- [x] **Instalar wc3270 y primer login** — R5, R6, R7. `ENTORNO.md`, `img/a1/`.
- [x] **Mapa de pantallas de `tareas.c`** y alcance de la GUI (D2) — R8, R9, R10, AMB-5. `docs/mapa-pantallas.md`, `img/a2/`.
- [x] **ws3270 localizado y versión anotada** — R5, R14. Es el `ws3270.exe` que viene con wc3270; no se usa s3270 de Linux (`ENTORNO.md`).
- [x] **Clase de conexión `MainframeClient`** — R14, R15, R16, R18. Órdenes con tiempo máximo, espera de pantalla estable, punto único de desconexión (1,5 s, también con Ctrl+C) y detección de Hercules. Probada contra el mainframe sin login (`docs/correcciones-prueba-java.md`).

### Tareas comunes

- [ ] **T1 — Estructura del programa y convenios** *(todos)*
  - Esqueletos de las clases con sus firmas en `SL-P2-02/src/` y reglas de trabajo en `docs/convenios.md`: escritos.
  - Hecha cuando: Paula y Ainhoa los han revisado y están de acuerdo (o se han cambiado).
- [ ] **T2 — Forma de trabajo en git** *(todos)*
  - Se trabaja en `main`; `git pull --rebase` antes de empezar y antes de subir; commits pequeños y que compilen.
- [ ] **T3 — Memoria** *(todos, al final)* — R22-R29. Ver tareas M1-M4.

### Paula (≈11 h)

- [x] **P1 — Login, arranque de `tareas.c` y cierre de sesión** (2,5 h) — R7, R9, R14, R21 — *Hecha por Ainhoa (`SesionMusic`) y Paula (`Pantalla`); prueba real completa el 2026-10-17. Con "Userid is in use" se da un error claro en vez de contestar `OK` (decisión del 2026-10-17); login con Intro en vez de Tab, comprobado en real.*
  - `mainframe/Pantalla.java`: reconocer cada pantalla por su texto, con pruebas sin conexión.
  - `mainframe/SesionMusic.java`: `iniciar` (pasar "More...", usuario, Tab, clave; contestar `OK` si sale "Userid is in use"; error claro si la clave no vale), `arrancarTareas` y `cerrar` (opción `3`, Intro, `off`).
  - Depende de: `MainframeClient` (hecha).
  - Hecha cuando: una prueba real conecta, entra, abre `tareas.c`, lee el menú, sale con `off` y desconecta sin errores.
- [x] **P2 — Leer los listados** (3 h) — R10, R11, R16 — *`LectorListado` y primera versión de `listar` por Paula; `listar` rehecho por Imad. Pruebas sin conexión y prueba real del 2026-10-17.*
  - `tareas/LectorListado.java`: sacar las tareas de las líneas `TASK n: ...` y detectar `TOTAL TASK`.
  - `GestorTareas.listar()`: pedir el listado general y el específico, guardar cada página y pulsar Intro mientras haya "More...".
  - Pruebas sin conexión con `img/a2/`: listado vacío, con tildes, de varias páginas (`31-listado-largo-p1` y `p2`).
  - Depende de: P1.
  - Hecha cuando: las pruebas pasan y el listado real coincide con lo que se ve en la ventana durante la prueba real (no se puede comparar después en wc3270: al salir, `tareas.c` pierde las tareas, AMB-8).
- [ ] **P3 — Tabla de tareas** (3,5 h) — R10, R19, R20
  - `gui/ModeloTablaTareas.java` y la tabla dentro de `VentanaTareas`: columnas Nº, tipo, fecha, nombre y descripción, barra de desplazamiento, refresco con botón y tras cada alta.
  - Mientras no esté P2, con datos inventados.
  - Hecha cuando: muestra el listado real, vacío y largo.
- [x] **P4 — Montar el zip y probarlo en limpio** (1,5 h) — R3, R29 — *Hecha por Imad el 2026-10-18: paso 4 de `build.sh` (`LEEME.txt`, memoria, `fuente/` con `git archive` sin ficheros del IDE, zip de 33 MB); descomprimido con Windows en una ruta con espacios y paréntesis, doble clic y prueba real (`docs/pruebas-reales/2026-10-18-zip-limpio.log`). Rehacer el zip con la memoria antes de enviar.*
  - `SL-P2-02.zip` desde el commit final (`git archive`) más lo compilado: programa, Java reducido, ws3270, `lanzar.bat`, `LEEME.txt` y memoria PDF.
  - Descomprimir en una carpeta nueva de Windows (ruta con espacios) y probar con doble clic.
  - No meter en el zip ficheros del IDE (`Practica2/.idea/`, `Practica2.iml`, `SL-P2-02/*.iml`) ni compilados sueltos.
  - Depende de: I5, A3.
  - Hecha cuando: la prueba en limpio funciona de principio a fin.
- [ ] **P5 — Pruebas finales: listados** (0,5 h) — R10
  - Listado vacío y listado largo desde la ventana, con captura.

### Imad (≈12 h)

- [ ] **I1 — Guardar pantallas reales para los tests** (1,5 h) — R16
  - Las que faltan: login incorrecto, fecha imposible, fecha vacía. La de Hercules tiene 24 filas, no 43.
  - Depende de: P1.
  - Hecha cuando: ficheros en `img/` con nombre descriptivo y usados en `PruebasOffline`.
- [ ] **I2 — Prueba real del cierre por error y por Ctrl+C** (1 h) — R15
  - Con el mainframe: comprobar en la traza que pasa ≥ 1 s entre conexión y desconexión en los dos casos.
  - Hecha cuando: las dos trazas están apuntadas como evidencia en `docs/requisitos.md`.
- [x] **I3 — Alta de una tarea** (2 h) — R10, R11, R16 — *Primera versión de Paula; rehecha por Imad. Prueba real del 2026-10-17: general `Camión` y específica `Peña`/`repaso` en el listado (`img/gui/r05-tabla.png`).*
  - `GestorTareas.nueva()`: navegar `ASSIGN TASKS`, enviar fecha, nombre y descripción, y confirmar `GENERAL TASK ASSIGNED` / `SPECIFIC TASK ASSIGNED`.
  - Depende de: P1.
  - Hecha cuando: un alta general y una específica aparecen en el listado, también con tildes.
- [x] **I4 — Trabajo en segundo plano, indicador y cierre** (3 h) — R15, R21 — *Hecha por Imad; prueba real con Salir el 2026-10-17 (`DESCONECTADO tras 81497 ms`). La X va por el mismo camino; falta probarla en real.*
  - `gui/VentanaTareas.java`: todas las operaciones con el mainframe en segundo plano (`SwingWorker`), con indicador de "trabajando" y botones desactivados.
  - "Salir" y la X: cierran la sesión (`SesionMusic.cerrar`), desconectan y esperan a que termine.
  - Hecha cuando: la ventana nunca se congela y el cierre siempre pasa por `MainframeClient.desconectar()`.
- [x] **I5 — Java reducido para Windows, ws3270 dentro del zip y `ENTORNO.md`** (3 h) — R29, AMB-7 — *Hecha por Imad el 2026-10-18: `build.sh` con `jlink` (`java.base`, `java.desktop`, 47 MB) y `ws3270.exe`; probado contra el mainframe desde una carpeta de Windows sin Java en el `PATH` (`docs/pruebas-reales/2026-10-18-entrega-automatica.log`).*
  - Generar con `jlink` un Java mínimo **para Windows** (necesita un JDK de Windows) con solo los módulos que use el programa.
  - Copiar `ws3270.exe` (y lo que necesite) a la carpeta de la entrega; la ruta se pasa con `-Dws3270.ruta`.
  - Hecha cuando: el programa arranca en un Windows sin Java instalado usando solo lo de la carpeta.
- [ ] **I6 — Pruebas finales: conexión y cierre** (1 h) — R1, R15, R21
  - Login incorrecto, mainframe no disponible, cierre con la X, cierre por error; con captura.
- [ ] **I7 — Leer [1] y [2]** (0,5 h) — R4
  - Lo justo para explicar en la memoria qué son ESA/390 y MUSIC/SP.

### Ainhoa (≈11 h)

- [ ] **A1 — Ventana de conexión (D1)** (2,5 h) — R7
  - `gui/VentanaConexion.java`: host, puerto, usuario y clave precargados (`155.210.152.51`, `3270`, `grupo_02`, `secreto6`) y editables; botón "Conectar".
  - Si el login falla, no hay red o sale Hercules: mensaje claro y se puede corregir sin cerrar el programa.
  - La conexión se hace en segundo plano; la ventana no se congela.
  - Depende de: P1 para probar contra el mainframe.
  - Hecha cuando: probado con datos correctos, clave errónea y sin red.
- [x] **A2 — Formulario de alta con validaciones** (3 h) — R10, R19, R20 — *`PanelAlta` hecho por Imad (D3); `Validador` por Paula, corregido por Imad (espacios). Pruebas sin conexión de datos válidos, vacíos, largos, con espacios y con tildes.*
  - `tareas/Validador.java` (D2): fecha `DDMM` real, sin espacios en nombre ni descripción, descripción ≤ 12 en general, nombre + descripción ≤ 16 en específica. Con pruebas sin conexión.
  - `gui/PanelAlta.java`: los tres campos y el botón "Nueva"; muestra el error de validación o el resultado del alta.
  - Hecha cuando: probado con datos válidos, vacíos, demasiado largos, con espacios y con tildes.
- [x] **A3 — `build.bat`, `build.sh` y `lanzar.bat`** (2 h) — R29 — *Hecha por Imad el 2026-10-18: `lanzar.bat` con `start "" javaw` (sin consola), probado con doble clic en una ruta con espacios y paréntesis, a mano (conectar, alta y salir) y con `pruebas.PruebaEntrega` (cierre con la X, `DESCONECTADO tras 38913 ms`); `build.bat` y `build.sh` generan la misma carpeta.*
  - `build.bat` / `build.sh`: compilan el programa y dejan la carpeta de ejecución lista.
  - `lanzar.bat`: arranca el programa con el Java de I5 con doble clic y funciona en rutas con espacios y paréntesis.
  - **Obligatorio sin consola:** `start "" "...\javaw.exe" ...`, nunca `java.exe` en una consola. Un Ctrl+C o cerrar la consola llega también a ws3270, que corta la conexión al instante sin esperar el mínimo (R15).
  - Hecha cuando: doble clic en `lanzar.bat` en Windows abre la ventana de conexión.
- [ ] **A4 — Capturas automáticas** (2 h) — R25
  - Script PowerShell que abre el programa y saca las capturas para la memoria (conexión, listado, alta, errores). Avisar antes de lanzarlo porque toma el teclado y el ratón.
  - Hecha cuando: las capturas están en `img/` de la práctica y se pueden repetir.
- [ ] **A5 — Pruebas finales: datos** (1,5 h) — R10
  - Datos no válidos y tildes desde la ventana, comprobando en wc3270 a mano que lo guardado coincide; con captura.

### Memoria (todos)

- [ ] **M1 — Parte de cada uno** (≈3 h por persona) — R23, R24, R25, R28
  - Paula: la aplicación legada, el mapa de pantallas y la lectura de listados.
  - Imad: la conexión, la sincronización, la regla de 1 s y el entorno.
  - Ainhoa: la interfaz, el diseño elegido frente a la maqueta y la entrega.
  - Cada uno incluye las dificultades que encontró y lo que hizo más allá del enunciado.
- [ ] **M2 — Reparto y cronograma** — R26, R27
  - Tabla de reparto (este fichero) y cronograma sacado de las fechas de los commits.
- [ ] **M3 — Cómo ejecutarlo en Windows** — R29
  - Doble clic en `lanzar.bat`; credenciales precargadas (D1); qué hacer si la clave cambia.
- [ ] **M4 — Revisión conjunta y PDF** — R22
  - 4-5 páginas aprox. más portada; revisión de los tres; exportar a PDF y meterlo en el zip (P4).
