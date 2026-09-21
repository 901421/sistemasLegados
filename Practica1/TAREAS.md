# Tareas del equipo — Práctica 1 (Sistemas Legados)

Grupo 02 · Imad, Paula y Ainhoa · Entrega: **lunes 28 de septiembre de 2026, 23:59, por email** · Fichero: `SL-P1-02.zip`

Este documento está en el **orden lógico de trabajo**, no en el orden del enunciado. Cada tarea tiene un **dueño** (la hace) y un **revisor** (la lee y la prueba). Como habrá defensa oral, los tres tenemos que saber explicar todo el código: por eso siempre revisa alguien que no la ha escrito.

Léete primero `DECISIONES.md`: explica por qué se ha decidido cada cosa.

---

## Cómo trabajamos (leer una vez)
1. **Antes de empezar cada día:** `git pull`. Antes de subir: `git pull` otra vez y luego `git push`.
2. **Cada fichero `BANK` tiene un dueño.** Si necesitas tocar uno ajeno, avisa antes en el grupo.
3. **Commits pequeños, uno por tarea**, en `main`. Mensaje tipo `Obj1: compilar BANK1`.
4. **Una tarea está hecha** cuando: (1) funciona ejecutándola, (2) hay captura o salida que lo demuestra, (3) su revisor la ha probado y (4) se ha anotado en `docs/bitacora.md`.
5. Si algo de una tarea anterior falla, **se avisa al grupo**; no se arregla en silencio.
6. **Sin cambios pendientes en los `BANK` durante la tarea T3.2** (ver el calendario): toca todos los ficheros.
7. Cada pantalla nueva nace ya con **texto blanco sobre fondo rojo** en los errores.

## Estado general
| Objetivo | Estado |
|---|---|
| Análisis, spike de compilación y elección de compilador | Hecho (ver `DECISIONES.md`, D12) |
| 1. Compilar y probar las 7 opciones | Pendiente |
| 2. Listado de transferencias | Pendiente |
| 3. Cambios en la aplicación | Pendiente |
| Memoria y entrega | Pendiente |

---

## Objetivo 1 — Compilar y probar (21-22 sep) · es lo primero: hasta que compile no se prueba nada
| ID | Tarea | Dueño | Revisor | Depende de | Hecha cuando |
|---|---|---|---|---|---|
| T1.1 | Script que reescribe las 275 sentencias `DISPLAY/ACCEPT (l c)` a `LINE/COL`; compilar los 7 `BANK`; arreglar `BANK6.cbl:114`; `build.sh` | Imad | Ainhoa | — | `cobc` compila los 7 sin errores |
| T1.2 | Programa que crea `movimientos.ubd` (1.000 € por tarjeta); reiniciar `intentos.ubd` (la tarjeta 5 tiene 2 intentos); **comprobar en Windows que GnuCOBOL 3.2 lee los `.ubd`** | Ainhoa | Paula | T1.1 (parcial) | `movimientos.ubd` existe y las 5 tarjetas tienen saldo; lectura en Windows probada |
| T1.3 | Reconstruir `BANK8` (cambiar clave): pide actual, nueva y repetición; errores con intentos restantes; bloqueo a los 3 fallos y cierre de sesión | Paula | Imad | T1.1 | Las pantallas del guion funcionan y el bloqueo persiste al volver a entrar |
| T1.4 | Prueba de las 7 opciones con capturas: **Imad** opciones 1-3, **Paula** 4-6, **Ainhoa** la 7 y el login/bloqueo | Los tres | Cruzada | T1.1-T1.3 | Cada opción probada con captura y anotada en la bitácora |

## Objetivo 2 — Transferencias (23-24 sep)
| ID | Tarea | Dueño | Revisor | Depende de | Hecha cuando |
|---|---|---|---|---|---|
| T2.1 | Registros compartidos de `ordenes.ubd` y `ejecuciones.ubd` en un único fichero `COPY` que usan todos | Paula | Imad | T1.4 | Los tres programas lo incluyen y compilan |
| T2.2 | `BANK6`: ordenar transferencia **inmediata, puntual o mensual** (D4-D8) y anotar también la inmediata | Paula | Imad | T2.1 | Se crean las tres clases de orden y se ven en los ficheros |
| T2.3 | `BANK1`: menú de 8 opciones (las opciones 6 y 7 pasan a 7 y 8) y módulo que **procesa las vencidas al iniciar sesión** (D3, D9) | Imad | Paula | T2.1 | Una puntual de hoy y una mensual se ejecutan y mueven saldo |
| T2.4 | `BANK9`: listado con formulario de fechas, pendientes calculadas, mensuales y paginación (D10) | Ainhoa | Paula | T2.1 | El listado del ejemplo de `DECISIONES.md` (D10) sale igual |
| T2.5 | Pruebas cruzadas del Objetivo 2 con la matriz de casos límite | Los tres | Cruzada | T2.2-T2.4 | Matriz rellenada con capturas |

## Objetivo 3 — Cambios en la aplicación (23-25 sep)
| ID | Tarea | Dueño | Revisor | Depende de | Hecha cuando |
|---|---|---|---|---|---|
| T3.1 | `BANK5`: ingresar efectivo por billetes de 10, 20 y 50 (D11) | Paula | Ainhoa | T1.4 | 2×10+1×20+1×50 = 90 € y saldo correcto; total 0 da error |
| T3.2 | Colores (blanco sobre rojo; azul más claro, **probando en pantalla**) y quitar los ceros de la esquina, con un script que toca todos los `BANK` | Imad | Ainhoa | T2.3 y T2.2 subidos | Capturas antes y después de cada tipo de pantalla |
| T3.3 | `lanzar.bat` (consola a 80x25), `build.bat` y empaquetado del zip | Ainhoa | Imad | T1.2 | Doble clic en Windows abre la aplicación a 80x25 |

## Cierre (25-28 sep)
| ID | Tarea | Dueño | Revisor | Cuándo |
|---|---|---|---|---|
| T4.1 | Integración y prueba de **las 8 opciones** | Los tres | Cruzada | 25 sep |
| T4.2 | Validación en **Windows real**: zip limpio, doble clic, 80x25 | Ainhoa | Imad | 26 sep |
| T4.3 | **Memoria** (PDF de 4-5 páginas): cada uno redacta lo de sus tareas; Ainhoa ensambla | Los tres | Cruzada | 26 sep |
| T4.4 | Guion de demostración para la defensa oral | Los tres | — | 27 sep |
| T4.5 | Envío por email de `SL-P1-02.zip` | Imad | Paula y Ainhoa | 28 sep antes de las 23:59 |

## Calendario
| Día | Qué |
|---|---|
| 21-22 sep | Objetivo 1 |
| 23-24 sep | Objetivos 2 y 3 en paralelo. **T3.2 el 24 por la tarde**, con todos sin cambios pendientes |
| 25 sep | Integración y prueba de las 8 opciones |
| 26 sep | Windows real y memoria |
| 27 sep | Margen y guion de demostración |
| 28 sep | Envío por email antes de las 23:59 |

## Preguntas pendientes para el profesor
1. ¿Es aceptable usar GnuCOBOL en lugar de un compilador de escobol.com?
2. ¿"Todas las transferencias" del listado incluye las recibidas?
3. ¿Qué hacer si no hay saldo el día en que toca ejecutar una transferencia?
4. ¿Cómo es la defensa oral o demostración?
