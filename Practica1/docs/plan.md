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
- **Estado:** Pendiente.

## Descripción del sistema legado
- **Qué pide:** que la aplicación funcione como describe el guion: bienvenida, Enter, tarjeta y clave (`000n`), menú y cambio de clave con máximo 3 intentos y bloqueo. Hay que conseguir compilarla y reconstruir lo que falta (`BANK8`, `movimientos.ubd`).
- **Requisitos:** R4 a R15.
- **Tareas:**
  - Reconstruir `BANK8` (cambio de clave; D1, D2, B5 en `requisitos.md`).
  - Crear `movimientos.ubd` con saldo inicial y reiniciar `intentos.ubd` (AMB16).
  - Comprobar bienvenida, Enter, tarjeta, clave y menú según pantallazo.
  - Comprobar error de clave con intentos restantes y bloqueo tras 3 fallos, con cierre de sesión (D1).
- **Verificación:** ejecución con capturas de cada pantalla; tarjeta 1 con clave 0001 entra; clave errónea 3 veces bloquea y el siguiente login lo rechaza.
- **Estado:** Pendiente.

## Objetivos

### 1. Descarga los ficheros fuente COBOL [1] y consigue compilarlos utilizando algún compilador de COBOL [2] para obtener un ejecutable para Windows
- **Qué pide:** obtener los fuentes de [1], compilarlos con un compilador de [2] y producir un ejecutable de Windows; usar la aplicación y comprobar que **todas** las opciones del menú funcionan.
- **Requisitos:** R16, R17, R18, R19 (y R14, R15, R43 que se ejecutan aquí).
- **Tareas:**
  - Script de reescritura mecánica de `DISPLAY/ACCEPT (l c)` a `LINE/COL` (275 usos) y compilación en formato libre de los 7 `BANK`; arreglar `BANK6.cbl:114` (D12).
  - Scripts `build.sh`, `build.bat` y `lanzar.bat`.
  - Programa que crea `movimientos.ubd` (1.000 € por tarjeta) y reinicio de `intentos.ubd`.
  - Verificar en Windows real que GnuCOBOL 3.2 lee los `.ubd`.
  - Prueba de las 7 opciones base con capturas (línea base: cambios mínimos y trazables, con antes/después).
  - Comprobar las hipótesis H1-H9 de `guia-cobol.pdf` contra el código.
- **Verificación:** `cobc` compila los 7 + `BANK8` con 0 errores; el ejecutable arranca con doble clic en Windows; captura de cada una de las 7 opciones funcionando.
- **Estado:** En curso (análisis, spike de compilación y comparativa de compiladores hechos; falta implementar).

### 2. Implementa una nueva funcionalidad "Listado de transferencias" (opción 6 del menú; opción 5 con transferencia puntual y mensual)
- **Qué pide:** nueva opción 6 "Listado de transferencias"; las opciones 6 y 7 pasan a 7 y 8; la opción 5 admite ejecución puntual en una fecha o mensual en un día; el listado muestra las ejecutadas o pendientes entre dos fechas indicando puntual o periódica.
- **Requisitos:** R20, R21, R22, R23, R24, R25, R26.
- **Tareas:**
  - Ficheros `ordenes.ubd` y `ejecuciones.ubd` y copybook compartido (D13).
  - `BANK6`: ordenar inmediata, puntual o mensual con reglas D4-D8, anotar la inmediata (D10).
  - `BANK1`: menú de 8 opciones y módulo que procesa las vencidas al iniciar sesión (D3), con "Fallida por saldo" (D9).
  - `BANK9`: listado con formulario de fechas, cálculo de pendientes y mensuales, y paginación estilo `BANK3` (D10).
- **Verificación:** casos de prueba con capturas (inmediata, puntual futura, puntual hoy, mensual día 3, día 31, sin saldo, rango vacío, inicio > fin).
- **Estado:** Pendiente.

### 3. Cambios en la aplicación

#### a. Mejorar el interfaz de usuario de la opción "4 - Ingresar efectivo"
- **Qué pide:** pedir cuántos billetes de 10, 20 y 50 €; al pulsar Enter, pantalla que informe de recibido correctamente, euros ingresados y saldo tras la operación.
- **Requisitos:** R27, R28, R29, R30.
- **Tareas:** reescribir la entrada de `BANK5` a tres campos; una operación por visita, un único movimiento con el total, ESC cancela (D11).
- **Verificación:** ingreso de 2×10 + 1×20 + 1×50 = 90 € y saldo correcto; total 0 da error; ESC no ingresa.
- **Estado:** Pendiente.

#### b. A la vista de los pantallazos recuperados, asegurarse que los mensajes sean fáciles de leer
- **Qué pide:** texto blanco en vez de negro sobre fondo rojo (errores) y un azul más claro cuando se use el azul.
- **Requisitos:** R31, R32.
- **Tareas:** cambiar `BLACK` sobre `RED` a `WHITE` sobre `RED` en todos los `BANK` (`BANK1.cbl:222-276`, etc.); probar `BLUE`+`HIGHLIGHT` y cian en la consola de Windows y elegir el más legible (decisión diferida).
- **Verificación:** captura de una pantalla de error y del título antes y después.
- **Estado:** Pendiente.

#### c. En algunas pantallas aparecen en la esquina inferior derecha unos ceros que no tienen ninguna utilidad, se deberán eliminar
- **Qué pide:** eliminar los ceros de la esquina inferior derecha.
- **Requisitos:** R33.
- **Tareas:** localizar en qué pantallas aparecen al ejecutar (sospecha: los 16 `ACCEPT (24 80)`); ocultarlos.
- **Verificación:** captura de cada pantalla afectada antes y después.
- **Estado:** Pendiente.

#### d. La ventana de ejecución debería ser siempre de 80x25 caracteres
- **Qué pide:** ventana de 80x25 siempre.
- **Requisitos:** R34.
- **Tareas:** `lanzar.bat` con `mode con cols=80 lines=25` y todas las pantallas diseñadas dentro de 80x25.
- **Verificación:** doble clic en Windows abre la consola a 80x25; ninguna pantalla se sale.
- **Estado:** Pendiente.

## Documentación
- **Qué pide:** unas 4-5 páginas con el proceso de cada uno de los tres pasos (problemas e intentos de solución), información gráfica, herramientas usadas (con énfasis en los compiladores), tarea de cada miembro, cronograma, otra información relevante y todo lo necesario para ejecutar en Windows con doble clic.
- **Requisitos:** R35 a R42.
- **Tareas:**
  - Redactar la memoria en PDF (Obj. 1, 2 y 3), cada uno la parte de sus tareas.
  - Incluir la comparativa de compiladores (D12) y la limitación de los no ejecutados.
  - Reparto y cronograma a partir del historial de git.
  - Instrucciones de doble clic.
- **Verificación:** 4-5 páginas; contiene los 8 elementos pedidos; PDF dentro del zip.
- **Estado:** Pendiente.

## Referencias
- **Qué pide:** [1] `http://webdiis.unizar.es/asignaturas/SL/` y [2] `escobol.com (en archive.org)`.
- **Requisitos:** R43.
- **Tareas:** citar ambas en la memoria; documentar qué se encontró en [2] (D12).
- **Verificación:** referencias presentes y coherentes con la memoria.
- **Estado:** Pendiente.
