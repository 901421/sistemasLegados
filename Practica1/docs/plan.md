# Plan — calcado al guion (Práctica 1, Sistemas Legados 2026-27)

Mismos apartados, mismo orden y mismos nombres que `Practica_1_2026_2027.pdf`. No se agrupa, no se reordena, no se renombra.
Estados: Pendiente · En curso · Hecho (con evidencia en `docs/requisitos.md`). Decisiones `Dn` en `DECISIONES.md`; ambigüedades `AMBn` y requisitos `Rn` en `docs/requisitos.md`.

---

## Resumen
- **Qué pide:** modificar y añadir nuevas funcionalidades a un programa COBOL; fecha límite lunes 28-sep-2026; fichero a enviar `SL-P1-nn.zip/rar`.
- **Requisitos:** R1, R2, R3.
- **Tareas:**
  - Montar la estructura del zip `SL-P1-02.zip` (fuentes, ejecutable, runtime, datos, lanzador, memoria PDF).
  - Enviarlo por email el 28-sep antes de las 23:59.
- **Verificación:** nombre exacto `SL-P1-02.zip`; descomprimir en carpeta limpia y ejecutar con doble clic; justificante del envío.
- **Estado:** Cumplido (zip `SL-P1-02.zip` montado y probado el 2026-09-26; enviado por email el 2026-09-27 mediante un enlace de Google Drive, porque el correo bloquea los adjuntos con ejecutables).

## Descripción del sistema legado
- **Qué pide:** que la aplicación funcione como describe el guion: bienvenida, Enter, tarjeta y clave (`000n`), menú y cambio de clave con máximo 3 intentos y bloqueo. Hay que conseguir compilarla y reconstruir lo que falta (`BANK8`, `movimientos.ubd`).
- **Requisitos:** R4 a R15.
- **Tareas:**
  - Reconstruir `BANK8` (cambio de clave; D1, D2, B5 en `requisitos.md`).
  - Crear `movimientos.ubd` con saldo inicial; `tarjetas.ubd` e `intentos.ubd` se entregan tal cual del backup (AMB16).
  - Comprobar bienvenida, Enter, tarjeta, clave y menú según pantallazo.
  - Comprobar error de clave con intentos restantes y bloqueo tras 3 fallos, con cierre de sesión (D1).
- **Verificación:** ejecución con capturas de cada pantalla; tarjeta 1 con clave 0001 entra; clave errónea 3 veces bloquea y el siguiente login lo rechaza.
- **Estado:** Cumplido (2026-09-25; R4-R15 con evidencia en `docs/requisitos.md`, auditados el 2026-09-26).

## Objetivos

### 1. Descarga los ficheros fuente COBOL [1] y consigue compilarlos utilizando algún compilador de COBOL [2] para obtener un ejecutable para Windows
- **Qué pide:** obtener los fuentes de [1], compilarlos con un compilador de [2] y producir un ejecutable de Windows; usar la aplicación y comprobar que **todas** las opciones del menú funcionan.
- **Requisitos:** R16, R17, R18, R19 (y R14, R15, R43 que se ejecutan aquí).
- **Tareas:**
  - Reescritura mecánica de `DISPLAY/ACCEPT (l c)` a `AT llcc` (275 usos) en formato fijo de los 7 `BANK` (fuentes en `src/`); arreglar `BANK6.cbl:114` (D12).
  - Scripts `build.sh`, `build.bat` y `lanzar.bat`.
  - Programa que crea `movimientos.ubd` (saldos iniciales de 100, 200, 300, 400 y 500 € para las tarjetas 1-5) y reinicio de `intentos.ubd`.
  - Programa que crea `espectaculos.ubd` de prueba con fechas futuras (los del backup son de 2015-2016 y la opción 6 los oculta por pasados).
  - Verificar en Windows real que GnuCOBOL 3.2 lee los `.ubd`.
  - Prueba de las 7 opciones base con capturas (línea base: cambios mínimos y trazables, con antes/después).
  - Comprobar las hipótesis H1-H9 de `guia-cobol.pdf` contra el código.
- **Verificación:** `cobc` compila los 7 + `BANK8` con 0 errores; el ejecutable arranca con doble clic en Windows; captura de cada una de las 7 opciones funcionando.
- **Estado:** Cumplido (2026-09-25; capturas en `img/obj1-menu-7-opciones/`).

### 2. Implementa una nueva funcionalidad "Listado de transferencias" (opción 6 del menú; opción 5 con transferencia puntual y mensual)
- **Qué pide:** nueva opción 6 "Listado de transferencias"; las opciones 6 y 7 pasan a 7 y 8; la opción 5 admite ejecución puntual en una fecha o mensual en un día; el listado muestra las ejecutadas o pendientes entre dos fechas indicando puntual o periódica.
- **Requisitos:** R20, R21, R22, R23, R24, R25, R26.
- **Tareas** (revisadas el 2026-09-25 tras el correo del docente: el cajero solo registra y consulta las programadas; D3 y D9 anuladas):
  - Fichero `transferencias.ubd` con una fila por transferencia y fecha de alta, creado por `CREAR-TRANSFERENCIAS` (D13).
  - `BANK6`: ordenar inmediata, puntual o mensual con reglas D4-D8; la inmediata mueve el dinero y todas se anotan (D10).
  - `BANK1`: menú de 8 opciones.
  - `BANK9`: listado con formulario de fechas, ocurrencias de las mensuales, estado por fecha (D14) y paginación; enviadas en todos los estados y recibidas ya ejecutadas, con columna de sentido (D10, revisada el 2026-09-26).
- **Verificación:** casos de prueba con capturas en `img/obj2-transferencias/` (inmediata, puntual pasada/inexistente/válida, mensual día 31, cuenta propia, listado en dos páginas con una transferencia recibida, inicio > fin, rango vacío) y regresión de las 8 opciones.
- **Estado:** Cumplido (2026-09-25; listado ampliado con las recibidas y capturas rehechas el 2026-09-26).

### 3. Cambios en la aplicación

#### a. Mejorar el interfaz de usuario de la opción "4 - Ingresar efectivo"
- **Qué pide:** pedir cuántos billetes de 10, 20 y 50 €; al pulsar Enter, pantalla que informe de recibido correctamente, euros ingresados y saldo tras la operación.
- **Requisitos:** R27, R28, R29, R30.
- **Tareas:** reescribir la entrada de `BANK5` a tres campos; una operación por visita, un único movimiento con el total, ESC cancela (D11).
- **Verificación:** ingreso de 2×10 + 1×20 + 1×50 = 90 € y saldo correcto; total 0 da error; ESC no ingresa.
- **Estado:** Cumplido (2026-09-25; capturas en `img/obj3-cambios/`).

#### b. A la vista de los pantallazos recuperados, asegurarse que los mensajes sean fáciles de leer
- **Qué pide:** texto blanco en vez de negro sobre fondo rojo (errores) y un azul más claro cuando se use el azul.
- **Requisitos:** R31, R32.
- **Tareas:** cambiar `BLACK` sobre `RED` a `WHITE` sobre `RED` en todos los `BANK` (`BANK1.cbl:222-276`, etc.); probar `BLUE`+`HIGHLIGHT` y cian en la consola de Windows y elegir el más legible (decisión diferida).
- **Verificación:** captura de una pantalla de error y del título antes y después.
- **Estado:** Cumplido (2026-09-25; capturas en `img/obj3-cambios/`).

#### c. En algunas pantallas aparecen en la esquina inferior derecha unos ceros que no tienen ninguna utilidad, se deberán eliminar
- **Qué pide:** eliminar los ceros de la esquina inferior derecha.
- **Requisitos:** R33.
- **Tareas:** localizar en qué pantallas aparecen al ejecutar (sospecha: los 16 `ACCEPT (24 80)`); ocultarlos.
- **Verificación:** captura de cada pantalla afectada antes y después.
- **Estado:** Cumplido (2026-09-25; capturas en `img/obj3-cambios/`).

#### d. La ventana de ejecución debería ser siempre de 80x25 caracteres
- **Qué pide:** ventana de 80x25 siempre.
- **Requisitos:** R34.
- **Tareas:** `lanzar.bat` con `mode con cols=80 lines=25` y todas las pantallas diseñadas dentro de 80x25.
- **Verificación:** doble clic en Windows abre la consola a 80x25; ninguna pantalla se sale.
- **Estado:** Cumplido (2026-09-25; capturas en `img/obj3-cambios/`).

## Documentación
- **Qué pide:** unas 4-5 páginas con el proceso de cada uno de los tres pasos (problemas e intentos de solución), información gráfica, herramientas usadas (con énfasis en los compiladores), tarea de cada miembro, cronograma, otra información relevante y todo lo necesario para ejecutar en Windows con doble clic.
- **Requisitos:** R35 a R42.
- **Tareas:**
  - Redactar la memoria en PDF (Obj. 1, 2 y 3), cada uno la parte de sus tareas.
  - Incluir la comparativa de compiladores (D12) y por qué se descartan los de [2].
  - Reparto y cronograma a partir del historial de git.
  - Instrucciones de doble clic.
- **Verificación:** 4-5 páginas; contiene los 8 elementos pedidos; PDF dentro del zip.
- **Estado:** Cumplido (2026-09-26; `docs/memoria-SL-P1-02.pdf`, §8 con la estructura del zip `SL-P1-02.zip`).

## Referencias
- **Qué pide:** [1] `http://webdiis.unizar.es/asignaturas/SL/` y [2] `escobol.com (en archive.org)`.
- **Requisitos:** R43.
- **Tareas:** citar ambas en la memoria; documentar qué se encontró en [2] (D12).
- **Verificación:** referencias presentes y coherentes con la memoria.
- **Estado:** Cumplido (R43; [1]-[3] citadas en la memoria con enlace).
