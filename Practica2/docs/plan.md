# Plan — calcado al guion (Práctica 2, Sistemas Legados 2026-27)

Mismos apartados, mismo orden y mismos nombres que `Practica_2_2026_2027.pdf`. No se agrupa, no se reordena, no se renombra.
Estados: Pendiente · En curso · Hecho (con evidencia en `docs/requisitos.md`). Decisiones `Dn` en `DECISIONES.md`; ambigüedades `AMB-n` y requisitos `Rn` en `docs/requisitos.md`.

---

## Objetivos
- **Qué pide:** un programa con interfaz gráfica de ventanas que dé acceso a la aplicación legada del mainframe por TN3270; entrega el lunes 19-oct-2026 en `SL-P2-nn.zip/rar`.
- **Requisitos:** R1, R2, R3.
- **Tareas:**
  - Montar `SL-P2-02.zip` desde el commit final (`git archive`): programa, runtime, ws3270, `lanzar.bat`, `LEEME.txt`, memoria PDF.
  - Probarlo descomprimiendo en una carpeta limpia de Windows (ruta con espacios) y doble clic.
  - Enviarlo por email antes del 19-oct 23:59.
- **Verificación:** nombre exacto `SL-P2-02.zip`; prueba en carpeta limpia; justificante del envío.
- **Estado:** Pendiente.

## Descripción de sistema
- **Qué pide:** conocer el entorno: mainframe IBM ESA/390 con MUSIC/SP que soporta terminales 3270. La GUI trabaja contra ese mainframe real.
- **Requisitos:** R4.
- **Tareas:**
  - Leer [1] y [2] lo justo para la memoria (qué es ESA/390 y MUSIC/SP).
- **Verificación:** la conexión real del apartado siguiente llega a la pantalla de MUSIC/SP.
- **Estado:** Pendiente.

## Terminales IBM 3270
- **Qué pide:** instalar x3270 (wc3270 en Windows) con el *Session Wizard*, configurar el acceso a `155.210.152.51:3270` y hacer login con `grupo_02` / `secreto6`; conocer el comando `help` de MUSIC/SP.
- **Requisitos:** R5, R6, R7.
- **Tareas:**
  - Imad instala wc3270 desde x3270.bgp.nu (sin administrador) y crea la sesión.
  - Comprobar desde dónde se alcanza el mainframe (casa, VPN, laboratorio).
  - Primer login manual, dejando la sesión abierta unos segundos antes de salir (R15). Capturas de la bienvenida y del login.
  - Anotar versión de wc3270 en `ENTORNO.md`.
- **Verificación:** captura de MUSIC/SP con sesión iniciada en `grupo_02`.
- **Estado:** Hecho (2026-09-28): wc3270 v4.5ga6, acceso desde eduroam y desde casa sin VPN, login y `help` con capturas en `img/a1/`; versión en `ENTORNO.md`. R6 cumplido; R5 y R7 quedan en curso hasta que la GUI use ws3270 y haga el login.

## Aplicación legada
- **Qué pide:** ejecutar `tasks.c` escribiéndolo en la línea de comandos, entender su funcionamiento definiendo y listando tareas; después encapsularla sin modificarla ni tocar su fichero de datos.
- **Requisitos:** R8, R9, R10, R11, R12, R13. Resuelve AMB-5.
- **Tareas:**
  - Ejecutar `tasks.c` a mano en wc3270 (Imad) y recorrer todas las opciones: 1 ASSIGN TASKS, 2 VIEW TASKS, 0 EXIT.
  - **Mapa de pantallas**: por cada pantalla, textos, posición de campos, longitudes máximas, mensajes de error, paginación "More...", listado vacío, tildes/ñ (EBCDIC), cómo se sale y a qué pantalla se vuelve; qué hay que teclear y en qué orden (Enter, PF3, Clear…).
  - Decidir con Imad el alcance de la GUI según lo encontrado (AMB-5).
- **Verificación:** mapa de pantallas completo con capturas; cada transición comprobada a mano.
- **Estado:** Hecho (2026-09-28), con AMB-8 pendiente de respuesta (decisión provisional en D2): `docs/mapa-pantallas.md` con menús, transiciones, formato del listado y fallos de `tareas.c` (el guion dice `tasks.c`: errata, AMB-9); capturas en `img/a2/`. Casos límite probados (segunda sesión, mismo día). Alcance de la GUI acordado (AMB-5, D2).

## Acceso 3270 displayless
- **Qué pide:** acceder al mainframe con s3270 (ws3270 en Windows) por *screen-scraping*: comandos de script por la entrada estándar y capturas de pantalla por la salida estándar; y (nota *i*) garantizar al menos 1 s entre conexión y desconexión, también ante errores.
- **Requisitos:** R14, R15, R16.
- **Tareas:**
  - Conseguir s3270 (WSL) y ws3270 (Windows) y anotar versiones en `ENTORNO.md`.
  - Probar a mano el guion de comandos (`Connect`, `Wait`, `String`, `Enter`, `Ascii`, `Disconnect`) respetando la regla de 1 s.
  - Capa de acceso en Java: proceso hijo ws3270/s3270, envío de comandos y lectura de respuestas, sincronización (esperar a que la pantalla esté lista), **punto único de desconexión** con espera ≥ 1,5 s en todos los caminos, detección de "Hercules".
  - Capturar pantallas reales (fixtures) y tests offline del análisis de pantallas.
- **Verificación:** tests offline en verde; sesión real login → `tasks.c` → salida → logoff sin errores y con ≥ 1 s medido en el registro; prueba de cierre por error y por Ctrl+C.
- **Estado:** En curso (2026-10-02). Hecho: ws3270 v4.5ga6 identificado (`ENTORNO.md`); guion de órdenes probado a mano por script (2026-09-28); clase de conexión `mainframe/MainframeClient.java` con punto único de desconexión, espera de pantalla estable, tiempo máximo por orden y detección de Hercules, ejecutada contra el mainframe (conectar, leer la bienvenida, desconectar a los 1501 ms). Falta: login, arranque de `tareas.c` y salida desde Java; pantallas guardadas y tests offline; prueba real de cierre por error y por Ctrl+C.

## Aplicación que hay que construir
- **Qué pide:** programa con interfaz gráfica (lenguaje libre) que accede al mainframe lanzando el emulador como proceso; diseño de interfaz propio, tomando la maqueta como ejemplo (tabla de tareas, alta con "Nueva", "Salir").
- **Requisitos:** R17, R18, R19, R20, R21.
- **Tareas:**
  - Ventana de conexión precargada y editable (D1), con errores de login recuperables.
  - Ventana principal "Tareas": formulario de alta (campos según el mapa), tabla con desplazamiento, refresco tras alta, "Salir".
  - Operaciones en segundo plano con indicador; la ventana nunca se congela.
  - Casos límite acordados: errores de conexión, validación de entrada, listados vacíos/largos.
  - Cierre ordenado con "Salir" y con la X (sale de `tasks.c`, logoff, R15).
  - `build.bat`/`build.sh`, runtime jlink para Windows y `lanzar.bat`.
- **Verificación:** prueba en Windows con doble clic: conectar, listar, crear tarea, comprobar que aparece también en wc3270 a mano, casos límite y salida; capturas automáticas.
- **Estado:** Pendiente.

## Documentación
- **Qué pide:** memoria de 4-5 páginas: dificultades, todo lo hecho más allá del enunciado (sincronización, etc.), pantallazos, distribución de tareas, cronograma, alcance, y todo lo necesario para ejecutar desde Windows con doble clic.
- **Requisitos:** R22, R23, R24, R25, R26, R27, R28, R29.
- **Tareas:**
  - Reparto de tareas del grupo: `REPARTO.md` (paquetes A, B y C).
  - Generador de la memoria (python-docx) con portada UZ/EINA; PDF exportado con Word.
  - Pantallazos de la GUI y del mainframe; cronograma a partir de git.
  - `LEEME.txt` con el doble clic y las credenciales.
- **Verificación:** memoria revisada contra R22-R29; zip probado en limpio.
- **Estado:** Pendiente.

## Referencias
- **Qué pide:** nada que implementar; fuentes [1]-[5] (ESA/390, MUSIC/SP, IBM 3270, x3270, manual de scripts).
- **Requisitos:** —
- **Tareas:** citar en la memoria las que se usen; [5] es la referencia técnica de s3270.
- **Estado:** Pendiente.
