# Elección del compilador — Práctica 1 (Sistemas Legados)

**Elegido: GnuCOBOL 3.2 con Berkeley DB.** Desarrollo en WSL con `cobc` 3.1.2 (mismo resultado que la 3.2.0) y ejecutable final para Windows con los binarios de la 3.2.
Este documento explica por qué, qué otros compiladores se probaron o se analizaron, y por qué se descartaron. La decisión resumida está en `DECISIONES.md` (D12).

Todo lo que aquí se afirma como "probado" se ejecutó el 2026-09-21 en copias de los fuentes, sin tocar los originales. Lo que no se pudo ejecutar está marcado como tal.

---

## 1. El problema
El guion pide compilar el programa con *"algún compilador de COBOL [2]"* (`escobol.com` en archive.org) y obtener un ejecutable para Windows. Avisa de que *"cada compilador acepta su propia versión y sintaxis de COBOL y desgraciadamente no sabemos qué compilador se usó"*.

El código original **no compila** con `cobc`. De los 251 errores que da GnuCOBOL 3.1.2, **243 (97%) son el mismo**: la sintaxis posicional antigua de pantalla, `DISPLAY (fila col) "texto"` y `ACCEPT (fila col) variable`. Hay **275 usos** (259 `DISPLAY` + 16 `ACCEPT`) repartidos en los 7 programas.

Criterio de elección, tal como lo planteamos: **el compilador que menos obligue a cambiar nuestro código**, no el más moderno.

## 2. Criterios usados
1. **Cambios necesarios en el código** hasta que compile.
2. **Datos:** que lea los `.ubd` originales sin convertirlos.
3. **Ejecutable de Windows** (lo pide el guion).
4. **Disponibilidad y licencia:** que se pueda obtener y redistribuir en el zip.
5. **Reproducibilidad:** que cualquiera del grupo pueda repetir las pruebas.

## 3. Qué ofrece [2] (escobol.com en archive.org)
- El sitio es un portal en castellano sobre COBOL (PHP-Nuke, captura del 17-ene-2021): manuales, cursos de PowerCOBOL y RM/COBOL, y un módulo de descargas.
- Las **114 descargas** apuntan a `descargas.cabasc.com/ftp/`. Hay **106 ficheros archivados** y ninguno de los destinos se ha perdido: el inventario es completo.
- Casi todo son **ejemplos, juegos, utilidades, librerías y manuales** (Micro Focus, RM/COBOL). No son compiladores.
- Solo hay **3 toolchains**: `cob650.zip`, `mscobol5.zip` y `pcobol3.zip`.
- Acucobol-GT, NetExpress (Micro Focus), Objective COBOL, PowerCOBOL y RM/COBOL aparecen únicamente como artículos descriptivos, sin instalador.

## 4. Candidatos y resultados

### Compilados y probados

| Compilador | Origen de la prueba | Errores con el código original | Con la reescritura mecánica (ver §5) | Veredicto |
|---|---|---|---|---|
| OpenCOBOL 1.1 | Docker `ubuntu:16.04` y `18.04` (paquete `open-cobol`) | 62 (solo informa del primero de cada párrafo; rechaza `(`) | No probado | **Descartado.** Rechaza la misma sintaxis: no ahorra ningún cambio |
| GnuCOBOL 2.2 | Docker `ubuntu:20.04` | 249 | No probado | **Descartado.** Mismo problema, sin ventaja |
| GnuCOBOL 3.1.2 | Docker `ubuntu:22.04` y `24.04`; también el `cobc` de WSL | 251 | **6 de 7 sin errores** | **Usado para desarrollar** en WSL |
| **GnuCOBOL 3.2.0** | Docker `debian:trixie` (paquete `gnucobol3`) | 255 | **6 de 7 sin errores** | **ELEGIDO** para el entregable |
| GnuCOBOL 4.0-early | Docker `ubuntu:24.04` (paquete `gnucobol4`) | 249 | Falla: *"compiler is not configured to support ORGANIZATION INDEXED"* | **Descartado.** Esta compilación no admite ficheros indexados, así que no puede leer los `.ubd` |

Además, en `cobc` 3.1.2 se probaron todos los dialectos que trae (`-std=` bs2000, acu, ibm, mf, mvs, realia, rm, cobol85, cobol2002, cobol2014, xopen y sus variantes estrictas): **ninguno acepta el código original** (entre 14 y 50 errores solo en `BANK1`).

### Solo analizados (sin poder ejecutarlos)

| Compilador | Qué es | Por qué no se ejecutó | Por qué se descarta |
|---|---|---|---|
| `cob650.zip` | Compilador ANSI de 1990 de dominio público (`COBOL.EXE`, `RUN650.EXE`, `COBOL650.LIB`) que genera `.OBJ` y necesita un enlazador externo (`LINK`) | Se intentó bajo DOSBox en un contenedor, pero escribe directo a pantalla y en modo desatendido no produjo nada | Es de **DOS de 16 bits**: no puede producir un ejecutable de Windows |
| `mscobol5.zip` | Microsoft COBOL 5.0 (1993), "para MS-DOS y Windows", en 6 discos de instalación con `SETUP.EXE` | El instalador es interactivo y de otra era | Es de la era **DOS y Windows 3.x**: no daría un ejecutable de Windows moderno |
| `pcobol3.zip` | PowerCOBOL 3: instalador InstallShield de 32 bits con **Fujitsu COBOL85**; según el `leeme` de escobol es "totalmente libre y gratuita tanto en desarrollo como en distribución" | Requiere Windows (Windows Sandbox no está activado en el equipo) | Compilador de **interfaz gráfica** de dialecto Fujitsu. Usa otro formato de ficheros indexados, así que habría que **migrar los datos**, y las pantallas del código habría que adaptarlas igualmente |
| Acucobol-GT, NetExpress (Micro Focus), Objective COBOL, RM/COBOL | Compiladores comerciales | No hay instalador en [2] (solo artículos) | **No obtenibles** |

## 5. Por qué GnuCOBOL 3.2
1. **Es el que menos cambia nuestro código de todos los que se pudieron probar.** El cambio necesario es el mismo en toda la familia GnuCOBOL, porque todas las versiones rechazan la misma sintaxis: una versión más antigua **no ahorra ninguna modificación**.
2. **El cambio es mínimo y mecánico.** Un script sustituye las 275 sentencias:

   ```
   antes:   DISPLAY (11 18) "Por su seguridad se ha bloqueado la tarjeta"
   después: DISPLAY "Por su seguridad se ha bloqueado la tarjeta" LINE 11 COL 18
   ```

   y se compila en formato libre (`cobc -m -free`). Con eso **6 de los 7 programas compilan con 0 errores** en la 3.1.2 y en la 3.2.0. El séptimo, `BANK6`, falla solo en `BANK6.cbl:114` (`SIGN IS LEADING SEPARATE` sobre un `PIC 9(7)` sin `S`), que se corrige a mano en una línea. La estructura del código no se toca.
3. **No hay que migrar datos.** Los `.ubd` originales son Berkeley DB y `cobc` los lee sin conversión (5 tarjetas con PIN `000n`, como dice el guion). Es un indicio fuerte de que el original se ejecutó con la familia OpenCOBOL/GnuCOBOL, aunque no una prueba. Fujitsu y Microsoft COBOL usan formatos indexados propios.
4. **Ejecutable de Windows:** existen compilaciones de GnuCOBOL 3.2 para Windows con Berkeley DB (página de Arnold Trembley, `https://www.arnoldtrembley.com/GnuCOBOL.htm`, p. ej. `GC32M-BDB-x64.7z`).
5. **Libre, disponible y reproducible.** Cualquiera del grupo puede repetir las pruebas con los comandos de §7.

## 6. Limitaciones (no ocultar en la memoria)
- **Microsoft COBOL 5.0 y PowerCOBOL 3 (Fujitsu) no se han ejecutado.** Puede que alguno acepte `DISPLAY (fila col)` y necesite menos cambios de sintaxis. Aun así, MS COBOL 5 no daría un ejecutable de Windows y Fujitsu obligaría a migrar los datos.
- **No se ha comprobado en Windows real** que el binario de GnuCOBOL 3.2 lea los `.ubd` (la versión de Berkeley DB puede variar) ni que el ejecutable arranque con doble clic. Es la tarea T1.2 de `TAREAS.md`.
- **El compilador elegido no procede de [2]**, que es lo que el guion pide literalmente. Es la caída prevista cuando [2] no ofrece un compilador utilizable. **Hay que preguntar al profesor** si es aceptable.
- Los binarios de Windows de GnuCOBOL son de terceros y no los hemos descargado ni probado todavía.

## 7. Cómo reproducirlo
Compilar un `BANK` original (falla con `DISPLAY (l c)`):
```
cobc -m original/BANK1.cbl -o /tmp/BANK1.so
```
Probar una versión de GnuCOBOL en un contenedor (Docker Desktop arrancado; los fuentes montados en solo lectura):
```
docker run --rm -v "<ruta a original>:/src:ro" debian:trixie sh -c \
  "apt-get update -qq && apt-get install -y -qq gnucobol3 && cobc --version && \
   mkdir /w && cp /src/BANK*.cbl /w && cd /w && for f in BANK*.cbl; do cobc -m \$f -o /tmp/x.so; done"
```
Imágenes usadas: `ubuntu:16.04` y `18.04` (OpenCOBOL 1.1), `ubuntu:20.04` (2.2), `ubuntu:22.04` y `24.04` (3.1.2 y 4.0-early), `debian:trixie` (3.2.0).

Contar los usos de la sintaxis antigua:
```
grep -c -E 'DISPLAY *\(' original/BANK*.cbl     # 259 en total
grep -c -E 'ACCEPT *\(' original/BANK*.cbl      # 16 en total
```

## 8. Referencias
- [1] `http://webdiis.unizar.es/asignaturas/SL/` (fuentes de la práctica).
- [2] `escobol.com` en archive.org (captura del 17-ene-2021) y sus ficheros en `descargas.cabasc.com/ftp/`.
- GnuCOBOL para Windows: `https://www.arnoldtrembley.com/GnuCOBOL.htm`.
