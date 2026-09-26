# Elección del compilador — Práctica 1 (Sistemas Legados)

**Elegido: GnuCOBOL 3.2 con Berkeley DB.** Desarrollo con `cobc` 3.2.0 (antes 3.1.2, mismo resultado) y ejecutable final para Windows con GnuCOBOL 3.2.0 para Windows (paquete GC32-BDB-SP1 de Arnold Trembley: MinGW 9.2.0 y Berkeley DB 18.1).
Este documento explica por qué, qué otros compiladores se probaron o se analizaron, y por qué se descartaron. La decisión resumida está en `DECISIONES.md` (D12).

Todo lo que aquí se afirma como "probado" se ejecutó el 2026-09-21 en copias de los fuentes, sin tocar los originales. Lo que no se pudo ejecutar está marcado como tal. Actualizado el 2026-09-25 con el convenio `AT` y las pruebas en Windows real (§7).

---

## 1. El problema
El guion pide compilar el programa con *"algún compilador de COBOL [2]"* (`escobol.com` en archive.org) y obtener un ejecutable para Windows. Avisa de que *"cada compilador acepta su propia versión y sintaxis de COBOL y desgraciadamente no sabemos qué compilador se usó"*.

El código original **no compila** con `cobc`. De los 250 errores que da GnuCOBOL 3.2.0 (251 en la 3.1.2), **243 (97%) son el mismo**: la sintaxis posicional antigua de pantalla, `DISPLAY (fila col) "texto"` y `ACCEPT (fila col) variable`. Hay **275 usos** (259 `DISPLAY` + 16 `ACCEPT`) repartidos en los 7 programas.

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
| GnuCOBOL 3.1.2 | Docker `ubuntu:22.04` y `24.04` | 251 | **6 de 7 sin errores** | **Usado al principio del desarrollo** |
| **GnuCOBOL 3.2.0** | Docker `debian:trixie` (paquete `gnucobol3`) | 250 | **6 de 7 sin errores** | **ELEGIDO** para el entregable |
| GnuCOBOL 4.0-early | Docker `ubuntu:24.04` (paquete `gnucobol4`) | 249 | Falla: *"compiler is not configured to support ORGANIZATION INDEXED"* | **Descartado.** Esta compilación no admite ficheros indexados, así que no puede leer los `.ubd` |

Además, en `cobc` 3.1.2 se probaron todos los dialectos que trae (`-std=` bs2000, acu, ibm, mf, mvs, realia, rm, cobol85, cobol2002, cobol2014, xopen y sus variantes estrictas): **ninguno acepta el código original** (entre 14 y 50 errores solo en `BANK1`). Repetido el 2026-09-26 con `cobc` 3.2.0 y sus 21 dialectos (los anteriores más gcos y las variantes estrictas que trae): ninguno lo acepta (entre 13 y 129 errores en `BANK1`).

### Solo analizados (sin poder ejecutarlos)

| Compilador | Qué es | Por qué no se ejecutó | Por qué se descarta |
|---|---|---|---|
| `cob650.zip` | Compilador ANSI de 1990 de dominio público (`COBOL.EXE`, `RUN650.EXE`, `COBOL650.LIB`) que genera `.OBJ` y necesita un enlazador externo (`LINK`) | Se intentó bajo DOSBox en un contenedor, pero escribe directo a pantalla y en modo desatendido no produjo nada | Es de **DOS de 16 bits**: no puede producir un ejecutable de Windows |
| `mscobol5.zip` | Microsoft COBOL 5.0 (1993), "para MS-DOS y Windows", en 6 discos de instalación con `SETUP.EXE` | El instalador es interactivo y de otra era | Es de la era **DOS y Windows 3.x**: no daría un ejecutable de Windows moderno |
| `pcobol3.zip` | PowerCOBOL 3: instalador InstallShield de 32 bits con **Fujitsu COBOL85**; según el `leeme` de escobol es "totalmente libre y gratuita tanto en desarrollo como en distribución" | Requiere Windows (Windows Sandbox no está activado en el equipo) | Compilador de **interfaz gráfica** de dialecto Fujitsu. Usa otro formato de ficheros indexados, así que habría que **migrar los datos**, y las pantallas del código habría que adaptarlas igualmente |
| Acucobol-GT, NetExpress (Micro Focus), Objective COBOL, RM/COBOL | Compiladores comerciales | No hay instalador en [2] (solo artículos) | **No obtenibles** |

## 5. Por qué GnuCOBOL 3.2
1. **Es el que menos cambia nuestro código de todos los que se pudieron probar.** El cambio necesario es el mismo en toda la familia GnuCOBOL, porque todas las versiones rechazan la misma sintaxis: una versión más antigua **no ahorra ninguna modificación**.
2. **El cambio es mínimo y mecánico.** Se sustituyen las 275 sentencias por la forma `AT fila-columna` (4 cifras), que GnuCOBOL acepta en el formato fijo de siempre:

   ```
   antes:   DISPLAY (11 18) "Por su seguridad se ha bloqueado la tarjeta"
   después: DISPLAY "Por su seguridad se ha bloqueado la tarjeta" AT 1118
   ```

   El primer ensayo (2026-09-21) usó `LINE 11 COL 18` y formato libre (`-free`); el equipo adoptó después `AT`, que no obliga a cambiar de formato (fuentes de trabajo en `src/`). En ambos casos **6 de los 7 programas compilan con 0 errores** en la 3.1.2 y en la 3.2.0. El séptimo, `BANK6`, falla solo en `BANK6.cbl:114` (`SIGN IS LEADING SEPARATE` sobre un `PIC 9(7)` sin `S`); se corrige quitando esa línea. La estructura del código no se toca.

   **Trampa del formato fijo:** `cobc` ignora sin avisar lo que pasa de la columna 72, así que un `AT` o un punto final más allá de esa columna se pierde y el programa compila igual. Comprobación: `awk 'length($0)>72' src/*.cbl` (debe salir vacío); las líneas largas se parten antes de `AT`.
3. **No hay que migrar datos.** Los `.ubd` originales son Berkeley DB y `cobc` los lee sin conversión (5 tarjetas con PIN `000n`, como dice el guion). Es un indicio fuerte de que el original se ejecutó con la familia OpenCOBOL/GnuCOBOL, aunque no una prueba. Fujitsu y Microsoft COBOL usan formatos indexados propios.
4. **Ejecutable de Windows:** GnuCOBOL 3.2.0 para Windows con Berkeley DB, paquete **GC32-BDB-SP1** de la página de Arnold Trembley (`https://www.arnoldtrembley.com/GnuCOBOL.htm`), instalado en `C:\GnuCOBOL`. Probado en Windows 11: compila, lee los `.ubd` y el cajero funciona con doble clic (§7).
5. **Libre, disponible y reproducible.** Cualquiera del grupo puede repetir las pruebas con los comandos de §7.

## 6. Limitaciones (no ocultar en la memoria)
- **Microsoft COBOL 5.0 y PowerCOBOL 3 (Fujitsu) no se han ejecutado.** Puede que alguno acepte `DISPLAY (fila col)` y necesite menos cambios de sintaxis. Aun así, MS COBOL 5 no daría un ejecutable de Windows y Fujitsu obligaría a migrar los datos.
- **El compilador elegido no procede de [2]**, que es lo que el guion pide literalmente. Es la caída prevista cuando [2] no ofrece un compilador utilizable. **Confirmado el 2026-09-26: GnuCOBOL es válido para la práctica.**
- Los binarios de Windows de GnuCOBOL son de terceros (Arnold Trembley), no de la FSF.
- El ejecutable **no está firmado**: en Windows 11 con el Control inteligente de aplicaciones activo puede bloquearse (§7.4).

## 7. Ejecución en Windows (probado el 2026-09-25 en Windows 11)

### 7.1 Compilar y ejecutar
- `build.bat` (con GnuCOBOL en `C:\GnuCOBOL`, o la ruta en `COB_DIR`) genera la carpeta `ejecutable` autocontenida: `BANK1.exe`, las DLL de GnuCOBOL (`libcob-4.dll`, `libdb-18.1.dll`, `pdcurses.dll`…), los `.ubd` y `lanzar.bat`.
- Para usar el cajero: **doble clic en `ejecutable\lanzar.bat`**. En Linux, `build.sh` genera `ejecutable-linux/` y se ejecuta con `./BANK1`.

### 7.2 Un único ejecutable
Los 9 programas (`BANK1` a `BANK9`) se compilan juntos en un solo `BANK1.exe` (`cobc -x` con los 9 fuentes). Los `CALL "BANKn"` del menú encuentran los programas dentro del mismo ejecutable; no hacen falta `BANK2.dll`…`BANK9.dll` ni `COB_LIBRARY_PATH`. El comportamiento es el mismo que con módulos separados (cada programa queda cargado tras la primera llamada). Motivo: con DLL separadas, un bloqueo de Windows (§7.4) puede aparecer a mitad de sesión, al elegir una opción; con un único ejecutable, si lo hay, es al arrancar. Verificado en Linux y en Windows con las 8 opciones del menú final.

### 7.3 Ventana de 80x25
En Windows 11 la consola por defecto es **Windows Terminal, que ignora `mode con cols=80 lines=25`**. `lanzar.bat` se vuelve a abrir en la consola clásica (`conhost.exe`), donde `mode con` sí fija 80x25; si `conhost.exe` no existiera, sigue en la ventana actual. Funciona aunque la carpeta tenga espacios o paréntesis (por ejemplo `SL-P1-02 (1)`, que es como Windows guarda un zip descargado dos veces): el `.bat` no usa bloques `( ... )` con rutas y se relanza desde su carpeta solo con su nombre de fichero.

### 7.4 Bloqueos de Windows a ejecutables sin firmar
- **Control inteligente de aplicaciones (Smart App Control).** En instalaciones limpias de Windows 11 puede estar activo; entonces Windows decide caso a caso si deja cargar programas recién compilados y sin firmar. Se vio dos veces: al elegir la opción 7 salió un diálogo "BANK1.exe…" y no se abrió la pantalla (cuando `BANK8` era una DLL aparte), y `build.bat` falló con *"bloqueado por la directiva de Device Guard"* en `CREAR-ESPECTACULOS.exe`. Queda registrado en el Visor de eventos, `Microsoft-Windows-CodeIntegrity/Operational`, eventos 3033/3077 (*"did not meet the Enterprise signing level"*). No es un fallo del programa COBOL. Se comprueba en *Seguridad de Windows → Control de aplicaciones y del explorador → Control inteligente de aplicaciones*. Windows recuerda la decisión para cada fichero, así que reintentar el mismo `.exe` no sirve: `build.bat` ejecuta los programas `CREAR-*` y, si Windows bloquea alguno, lo **vuelve a compilar** (fichero nuevo, nueva evaluación) y lo reintenta, hasta 5 veces. Probado: 6 de 6 compilaciones correctas, 3 de ellas con un bloqueo resuelto al primer reintento.
- **SmartScreen.** Al descargar el zip de Internet o del correo, Windows puede mostrar *"Windows protegió su PC"* al abrir `lanzar.bat` o `BANK1.exe`: *Más información → Ejecutar de todas formas*.

### 7.5 Ficheros de datos y versiones de Berkeley DB
La Berkeley DB de GnuCOBOL para Windows (18.1) crea los ficheros indexados en formato btree v10, que el `cobc` de Linux (libdb 5.3, v9) no lee; en sentido contrario sí funciona. Por eso `tarjetas.ubd` e `intentos.ubd` se copian de `original/` y `movimientos.ubd`, `espectaculos.ubd` y `transferencias.ubd` los crean `build.bat`/`build.sh` en la propia máquina (programas `CREAR-MOVIMIENTOS`, `CREAR-ESPECTACULOS` y `CREAR-TRANSFERENCIAS`).

## 8. Cómo reproducirlo
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

## 9. Referencias
- [1] `http://webdiis.unizar.es/asignaturas/SL/` (fuentes de la práctica).
- [2] `escobol.com` en archive.org (captura del 17-ene-2021) y sus ficheros en `descargas.cabasc.com/ftp/`.
- GnuCOBOL para Windows: `https://www.arnoldtrembley.com/GnuCOBOL.htm`.
