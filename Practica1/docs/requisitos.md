# Requisitos — Sistemas Legados, Práctica 1 (2026-27)

Fuente: `Practica_1_2026_2027.pdf` (2 páginas; el análisis se hizo sobre una conversión local a markdown que no se sube).
Hash SHA-256 del guion: `6179fada70f989adc22577f726c884931ceacd24059c83d82d8ad319dcb146cb`
Zip de partida: `ATM Cobol_backup.zip` (SHA-256 `ce361871f0ee587609d615dbe8c59fa09ce76572a1a59e2e5afc301c934d8171`), confirmado por el usuario como el original.

Tipos: **E** explícito · **I** implícito (se deriva necesariamente) · **A** ambiguo (más de una lectura razonable).
Estado: Pendiente salvo que se indique; nada se marca Cumplido sin evidencia (fichero y línea, ejecución o captura). Actualizado el 2026-09-25 (Obj. 1, 2 y 3).

> `guia-cobol.pdf` es material de apoyo elaborado por el grupo, NO es fuente oficial. Sus hipótesis (H1-H9, A3-A8) se tratan como sospechas a verificar contra el código.

---

## 2.4 Estructura del guion (orden y nombres exactos)

1. Resumen
2. Descripción del sistema legado
3. Objetivos
   1. Descarga los ficheros fuente… (compilar y probar todo)
   2. Implementa una nueva funcionalidad "Listado de transferencias"…
   3. Cambios en la aplicación:
      - a. Mejorar el interfaz de usuario de la opción "4 - Ingresar efectivo"
      - b. Colores (texto blanco sobre fondo rojo; azul más claro)
      - c. Eliminar ceros de la esquina inferior derecha
      - d. Ventana de ejecución de 80x25
4. Documentación
5. Referencias

---

## 2.1 Requisitos

### Resumen (p.1)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R1 | Resumen | Modificar y ampliar un programa COBOL existente | "Modificar y añadir nuevas funcionalidades a un programa COBOL" | p.1, Resumen, viñeta 1 | E | Pendiente | |
| R2 | Resumen | Entregar como máximo el lunes 28-sep-2026 | "Fecha límite: lunes 28 septiembre 2026" | p.1, Resumen, viñeta 2 | E (hora: A, ver AMB-1) | Pendiente | |
| R3 | Resumen | El fichero de entrega se llama SL-P1-nn.zip o .rar, con nn = nº de grupo | "Fichero a enviar: SL-P1-nn.zip/rar (nn: número de grupo)" | p.1, Resumen, viñeta 2 | E (formato de nn: A, ver AMB-2) | Pendiente | |

### Descripción del sistema legado (p.1-2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R4 | Descripción | Hay que conseguir que el programa compile y se ejecute, modificando el fuente lo necesario | "Habrá que conseguir ejecutar el programa, modificando el programa fuente adecuadamente para poder compilarlo y que pueda utilizarse la aplicación dotada de la nueva funcionalidad" | p.1, Descripción, párr. 1 | E | Parcial | Compila y se ejecuta: 8 programas en `BANK1.exe` con GnuCOBOL 3.2 (`build.bat`/`build.sh`, commit `a8ab4cb`). Falta "dotada de la nueva funcionalidad" (Obj. 2) |
| R5 | Descripción | Al lanzar la aplicación se muestra una pantalla de bienvenida | "nada más lanzar la aplicación se muestra una pantalla de bienvenida" | p.1, párr. "El funcionamiento básico…" | E | Cumplido | Pantalla "Bienvenido a UnizarBank" al arrancar (`src/BANK1.cbl` P1; ejecución Linux tmux y Windows, 2026-09-25) |
| R6 | Descripción | Tras pulsar Enter se pide número de tarjeta y clave | "tras pulsar Enter se le pide al usuario el número de tarjeta y la clave correspondiente" | p.1, ídem | E | Cumplido | Tras Enter pide "Numero de tarjeta" y "Inserte el pin de tarjeta" (`img/obj1-menu-7-opciones/obj1-01-login-tarjeta.png`) |
| R7 | Descripción | Por defecto la tarjeta 000000000000000n tiene clave 000n | "(por defecto, la tarjeta 000000000000000n tiene de clave 000n)" | p.1, ídem | E (ver AMB-16: origen de los datos) | Cumplido | Las tarjetas `000000000000000n` (n = 1-5) entran con PIN `000n` (ejecución tmux 80x25, 2026-09-25) |
| R8 | Descripción | Con tarjeta y clave correctas se muestra el menú principal (legado: 7 opciones; final: 8, ver R18) | "Si la tarjeta y la clave son correctas, se mostrará un menú con siete opciones" | p.1, ídem | A (AMB-4) | Cumplido | Menú final de 8 opciones tras login correcto (`img/obj2-transferencias/obj2-01-menu-8-opciones.png`); el de 7 opciones del legado en `obj1-02` (AMB-4) |
| R9 | Descripción | Menú con el aspecto del pantallazo: título "Cajero Automatico UnizarBank", fecha y hora, opciones numeradas y "ESC - Salir" | Pantallazo del menú | p.1, figura 2 | I | Cumplido | `obj1-02-menu-principal.png`: título, fecha y hora, opciones numeradas y "ESC - Salir" |
| R10 | Descripción | Opción "Cambiar clave": pide clave actual, nueva y repetición de la nueva; si la actual es correcta, pantalla de confirmación | "Para cambiar la clave se pide la clave actual y la nueva clave; en caso de que sea correcta la clave actual, se muestra una pantalla de confirmación" | p.1 final / p.2, "A modo de ejemplo…" | E | Cumplido | `src/BANK8.cbl` (commit `95cc945`): pide clave actual, nueva y repetición; confirmación "La clave se ha cambiado correctamente" (`obj1-16`, `obj1-17`) |
| R11 | Descripción | Pantallas de cambio de clave según pantallazos: "Introduzca clave actual / nueva clave / Repita la nueva clave", "Enter - Confirmar", "ESC - Cancelar", "La clave se ha cambiado correctamente", "Enter-Aceptar" | Pantallazos de cambio de clave | p.2, figuras 3-4 | I | Cumplido | Pantallas de `src/BANK8.cbl` con la colocación y los textos de los pantallazos del guion (p. 2): título fila 6, etiquetas filas 8-10, campos columna 50 vacíos "____" y tecleados "****" (cada cifra cambia un "_" por "*"; Enter con la clave incompleta la borra); confirmación filas 8 y 11 con "Enter-Aceptar" (reabierto y corregido el 2026-09-25, C10). Capturas `obj1-16`, `obj1-17`, `obj2-23` |
| R12 | Descripción | Si la clave actual es incorrecta: pantalla de error con nº de intentos restantes, máximo tres | "se mostrará una pantalla avisando del error y el número de intentos que restan para cambiar la clave (máximo tres intentos)" | p.2, párr. tras pantallazos | E | Cumplido | Clave actual incorrecta: "La clave actual es incorrecta / Le quedan 2 intentos", luego 1 (ejecución tmux, tarjeta 3, 2026-09-25) |
| R13 | Descripción | Tras tres fallos se bloquea la tarjeta y no se puede volver a acceder al sistema con ella | "se bloqueará la tarjeta y no se podrá volver a acceder con esa tarjeta al sistema" | p.2, ídem | E (mecanismo: A, AMB-6) | Cumplido | Al tercer fallo: "Por su seguridad se ha bloqueado la tarjeta", se cierra la sesión y el login posterior con esa tarjeta se rechaza (ejecución tmux, 2026-09-25; D1) |
| R14 | Descripción | Reconstruir el programa de "Cambiar clave": el menú lo llama pero no está en el backup | "ni se está seguro de que se haya recuperado la última versión" + "se muestra el funcionamiento de la opción '7 - Cambiar clave' de la que se han podido recuperar unos pantallazos" | p.1, Descripción | I (verificado: `BANK1.cbl:209` hace `CALL "BANK8"`; no existe BANK8 en el zip) | Cumplido | `src/BANK8.cbl` reconstruido (commit `95cc945`); el menú lo llama con `CALL "BANK8"` |
| R15 | Descripción | Disponer de `movimientos.ubd`, del que dependen los programas de saldo/movimientos/retirar/ingresar/transferir/entradas | "Habrá que conseguir ejecutar el programa" | p.1, Descripción | I (verificado: ese fichero no está en el zip; `BANK2-BANK7` lo abren) | Cumplido | `src/CREAR-MOVIMIENTOS.cbl` crea `movimientos.ubd` con saldos iniciales; lo ejecutan `build.bat`/`build.sh` |

### Objetivos (p.2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R16 | Obj. 1 | Descargar los fuentes COBOL de [1] | "Descarga los ficheros fuente COBOL [1]" | p.2, Objetivos, 1 | E | Cumplido | `ATM Cobol_backup.zip` en `Practica1/` (SHA-256 `ce361871…8171`); la página [1] `webdiis.unizar.es/asignaturas/SL/` lo lista como `Practicas/ATM Cobol_backup.zip` (consultada 2026-09-21); el usuario confirma que es el original |
| R17 | Obj. 1 | Compilarlos con algún compilador COBOL de [2] | "consigue compilarlos utilizando algún compilador de COBOL [2]" | p.2, Objetivos, 1 | A (AMB-3) | Cumplido | GnuCOBOL 3.2 con Berkeley DB: [2] no ofrece un compilador utilizable (análisis en `COMPILADOR.md` §3-4) y GnuCOBOL es válido para la práctica (confirmado el 2026-09-26, D12). Compila los 9 programas en `BANK1.exe` (`build.bat`/`build.sh`) |
| R18 | Obj. 1 | Obtener un ejecutable para Windows | "para obtener un ejecutable para Windows" | p.2, Objetivos, 1 | E (forma exacta: A, AMB-3) | Cumplido | `BANK1.exe` que crea `build.bat` en Windows 11; doble clic en `ejecutable\lanzar.bat` verificado por el usuario (commits `a2750b8`, `a8ab4cb`) |
| R19 | Obj. 1 | Usar la aplicación y comprobar que funcionan correctamente TODAS las funcionalidades del menú principal (subrayado en el guion) | "utiliza la aplicación para asegurarte que funcionan correctamente todas las funcionalidades que se presentan en el menú principal" | p.2, Objetivos, 1 | E (alcance: A, AMB-4) | Cumplido | Las 7 opciones usadas en Windows: 17 capturas en `img/obj1-menu-7-opciones/` (commit `ed76c0e`) y repetición con el ejecutable único. Regresión con el menú de 8 opciones: `obj2-20` a `obj2-23` (2026-09-25) |
| R20 | Obj. 2 | Nueva opción 6 del menú: "Listado de transferencias" | "nueva funcionalidad 'Listado de transferencias' que debe estar disponible como nueva opción 6 del menú principal" | p.2, Obj. 2 | E | Cumplido | Opción 6 "Listado de transferencias" (`src/BANK9.cbl`, commit `ad3a585`): `obj2-01`, `obj2-15` a `obj2-19` |
| R21 | Obj. 2 | Las actuales 6 (Comprar entradas) y 7 (Cambiar clave) pasan a ser 7 y 8; el menú final tiene 8 opciones | "las actuales opciones 6 y 7 pasarán a ser las opciones 7 y 8" | p.2, Obj. 2 | E | Cumplido | Menú de 8 opciones: 7 Comprar entradas y 8 Cambiar clave (`src/BANK1.cbl`, commit `ed01abd`); `obj2-01`, `obj2-22`, `obj2-23` |
| R22 | Obj. 2 | Opción 5 "Ordenar transferencia": añadir ejecución puntual en una fecha dada | "añadiendo la posibilidad de que una transferencia se ejecute puntualmente en una fecha dada" | p.2, Obj. 2 | E (semántica: A, AMB-7) | Cumplido | Puntual en una fecha (`src/BANK6.cbl`, commit `94872a1`): fecha pasada e inexistente rechazadas, hoy admitida (D4, D5); `obj2-06` a `obj2-10` |
| R23 | Obj. 2 | Opción 5: añadir ejecución mensual en el día indicado (ej. los días 3 de cada mes) | "o mensualmente en el día indicado (por ejemplo, los días 3 de cada mes)" | p.2, Obj. 2 | E (semántica: A, AMB-8) | Cumplido | Mensual en un día (D6-D8): `obj2-11` a `obj2-13`; ocurrencias del día 31 el 30-09, 30-11, 28-02 y 30-04 en `obj2-16` |
| R24 | Obj. 2 | Se conserva la transferencia inmediata actual: "añadiendo la posibilidad" indica ampliar, no sustituir | "añadiendo la posibilidad de…" | p.2, Obj. 2 | I | Cumplido | Se conserva la transferencia inmediata (tipo 1): `obj2-03` a `obj2-05`; saldo y movimiento en `obj2-20`, `obj2-21` |
| R25 | Obj. 2 | Opción 6 muestra todas las transferencias ejecutadas o pendientes entre dos fechas dadas por el usuario | "se mostrarán todas las transferencias … que se han ejecutado o están pendientes de hacerse entre dos fechas indicadas por el usuario" | p.2, Obj. 2 | E (detalles: A, AMB-9) | Cumplido | Listado entre dos fechas (inclusivas) de las transferencias ordenadas por la tarjeta, ejecutadas o pendientes (D10, D14): `obj2-15` a `obj2-19`. Alcance de "todas": pendiente de confirmar con el profesor (D10) |
| R26 | Obj. 2 | El listado indica si cada transferencia es puntual o periódica | "(indicando si son puntuales o periódicas)" | p.2, Obj. 2 | E | Cumplido | Columna TIPO: "Puntual" o "Periodica" (`obj2-16`, `obj2-17`) |
| R27 | Obj. 3.a | "Ingresar efectivo" pide cuántos billetes de 10, 20 y 50 € en lugar del total | "pedir cuántos billetes de 10, 20 y 50 euros se van a ingresar" | p.2, 3.a | E | Cumplido | `src/BANK5.cbl` (commit `495c283`, lógica de Paula corregida, C8): campos de billetes de 10, 20 y 50 (`img/obj3-cambios/obj3-03`, `obj3-05`); total 0 rechazado (`obj3-04`) |
| R28 | Obj. 3.a | Al pulsar Enter aparece pantalla que informa de que se han recibido correctamente | "Al pulsar Enter deberá aparecer una pantalla donde se informe que se han recibido correctamente" | p.2, 3.a | E | Cumplido | Al pulsar Enter, pantalla "Se han recibido correctamente" (`obj3-06`) |
| R29 | Obj. 3.a | Esa pantalla muestra la cantidad de euros ingresados | "la cantidad de euros ingresados" | p.2, 3.a | E | Cumplido | Muestra los euros ingresados: 2×10 + 1×20 + 1×50 = 90,00 (`obj3-06`); un único movimiento "Ingreso" (`obj3-08`) |
| R30 | Obj. 3.a | Esa pantalla muestra el saldo disponible tras la operación | "y el saldo disponible tras la operación" | p.2, 3.a | E | Cumplido | Muestra el saldo tras la operación: 190,00 (`obj3-06`) |
| R31 | Obj. 3.b | Texto blanco en lugar de negro cuando haya texto sobre fondo rojo (errores) | "usando color de texto blanco en lugar de negro cuando haya texto presentado en fondo rojo (errores)" | p.2, 3.b | E (verificado en código: `BANK1.cbl:222-276` usa BLACK sobre RED) | Cumplido | Todo texto sobre fondo rojo en blanco brillante (`WHITE HIGHLIGHT`) en BANK1-9 (commit `1de5e9e`): `obj3-01`, `obj3-04`, `obj3-09`, `obj3-10`, `obj2-06`, `obj2-14` |
| R32 | Obj. 3.b | Usar un azul más claro cuando se use el azul | "usando un azul más claro cuando se use este color" | p.2, 3.b | A (AMB-11) | Cumplido | Títulos en `BLUE HIGHLIGHT` (azul más claro, elegido viendo la consola de Windows, AMB-11; commit `1de5e9e`): `obj3-02` frente a `obj1-02` |
| R33 | Obj. 3.c | Eliminar los ceros inútiles de la esquina inferior derecha en las pantallas donde aparezcan | "En algunas pantallas aparecen en la esquina inferior derecha unos ceros que no tienen ninguna utilidad, se deberán eliminar" | p.2, 3.c | E (qué pantallas: por descubrir al ejecutar) | Cumplido | Sin ceros en la esquina ni en la línea 25: esperas con `TECLA PIC X(1)` sin eco en BANK1-8 (commit `cb86aa3`, C9); 17 pantallas comprobadas en tmux; `obj3-07`, `obj3-08` frente a `obj1-03`, `obj1-05` |
| R34 | Obj. 3.d | Ventana de ejecución siempre de 80x25 caracteres | "La ventana de ejecución debería ser siempre de 80x25 caracteres" | p.2, 3.d | A (AMB-13) | Cumplido | `lanzar.bat` abre la consola clásica a 80x25 (commit `a2750b8`, verificado por el usuario); ningún texto sale de 80 columnas ni pasa de la fila 25 (revisión de `src/`); capturas de 720x500 = 80x25 celdas |

### Documentación (p.2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R35 | Documentación | Memoria de unas 4-5 páginas aprox. | "Redactar unas 4-5 páginas aprox." | p.2, Documentación | E | Pendiente | |
| R36 | Documentación | Detallar el proceso de cada uno de los "tres pasos" (principales problemas al compilar/ejecutar, intentos de solución) | "detallando el proceso seguido en cada uno de los tres pasos que has seguido (principales problemas encontrados al intentar compilar/ejecutar la aplicación, intento de soluciones, etc.)" | p.2, ídem | E ("tres pasos" = Objetivos 1-3: A, AMB-14) | Pendiente | |
| R37 | Documentación | Incluir información gráfica (diagramas, esquemas, capturas) cuando sea necesario | "incluyendo información gráfica (diagramas, esquemas, capturas de pantalla, etc.) cuando sea necesario" | p.2, ídem | E | Pendiente | |
| R38 | Documentación | Describir y referenciar las herramientas software usadas, con énfasis en los compiladores COBOL | "describir y referenciar las herramientas software utilizadas, con especial énfasis en los compiladores de COBOL utilizados" | p.2, ídem | E | Pendiente | |
| R39 | Documentación | Indicar qué tarea ha hecho cada miembro del grupo | "qué tarea ha realizado cada miembro del grupo" | p.2, ídem | E | Pendiente | |
| R40 | Documentación | Incluir el cronograma de las tareas | "el cronograma de las mismas" | p.2, ídem | E | Pendiente | |
| R41 | Documentación | Incluir cualquier otra información relevante para entender el esfuerzo | "cualquier otra información que se crea de importancia para entender el esfuerzo invertido" | p.2, ídem | E | Pendiente | |
| R42 | Documentación | Incluir todo lo necesario para ejecutar las prácticas desde Windows con doble clic en un fichero | "incluyendo todo lo necesario para poder ejecutar las practicas desde Windows haciendo doble click en un fichero" | p.2, ídem | A (AMB-15) | Pendiente | |

### Referencias (p.2)

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R43 | Referencias | Las referencias del guion son la fuente de los fuentes [1] `http://webdiis.unizar.es/asignaturas/SL/` y de los compiladores [2] `escobol.com (en archive.org)` | "[1] http://webdiis.unizar.es/asignaturas/SL/ · [2] escobol.com (en archive.org)" | p.2, Referencias | E (disponibilidad de [2] sin comprobar) | Cumplido | [1]: origen de `ATM Cobol_backup.zip` (R16). [2]: inventario y análisis de sus compiladores en `COMPILADOR.md` §3-4 |

---

## 2.2 Restricciones

El guion no contiene prohibiciones explícitas: no aparece "no se permite" ni límites de librerías, tiempo o memoria. Lo que limita es:

- **Plataforma:** ejecutable para Windows (R18) y ejecución con doble clic (R42).
- **Compilador:** de [2] escobol.com (R17). "Cada compilador acepta su propia versión y sintaxis de COBOL", el dialecto original se desconoce.
- **Pantalla:** 80x25 siempre (R34).
- **Colores:** blanco sobre rojo en errores; azul más claro (R31, R32).
- **Nombre de entrega:** `SL-P1-nn.zip/rar` (R3).
- **Memoria:** 4-5 páginas aprox. (R35).
- **Menú:** tras el cambio, 8 opciones en el orden fijado (R20, R21).
- **Transferencias:** puntual = fecha dada; periódica = mensual, día indicado. No se piden otras periodicidades (R22, R23).
- **Ingreso:** solo billetes de 10, 20 y 50 € (R27).

## 2.3 Entregables y fechas

| Qué | Formato / nombre | Cuándo | Dónde |
|---|---|---|---|
| Fichero de entrega | `SL-P1-nn.zip` o `.rar` (nn = nº de grupo) | lunes 28-sep-2026 (hora no indicada) | "enviar": canal no indicado (AMB-1) |
| Contenido (derivado) | Fuentes modificados; ejecutable(s) Windows; ficheros de datos; lanzador de doble clic; memoria 4-5 págs. | ídem | ídem |

El guion no lista el contenido del zip. La lista de arriba se deduce de R4, R18, R35 y R42 y va a confirmación (AMB-14, AMB-15).

---

## Ambigüedades (cada una con su cita)

| ID | Cita | Lecturas posibles | Afecta a |
|---|---|---|---|
| AMB-1 | "Fecha límite: lunes 28 septiembre 2026. Fichero a enviar…" | Hora límite: 23:59 / hora de clase / no indicada. Canal: Moodle / correo / otro. | R2 |
| AMB-2 | "SL-P1-nn.zip/rar (nn: número de grupo)" | `nn` con dos cifras (`SL-P1-03.zip`) o sin ceros (`SL-P1-3.zip`). Se necesita el nº de grupo. | R3 |
| AMB-3 | "utilizando algún compilador de COBOL [2] para obtener un ejecutable para Windows" | (a) solo compiladores de escobol.com (Windows nativo); (b) vale cualquiera, p. ej. GnuCOBOL. "Ejecutable" = `.exe` nativo o módulos + runtime que lo ejecuta. Aquí solo hay `cobc` 3.1.2 en Linux/WSL. | R17, R18, R42 |
| AMB-4 | "un menú con siete opciones" / "todas las funcionalidades … del menú principal" | La verificación de R19 cubre las 7 originales, o las 8 finales incluyendo las nuevas. | R8, R19, R21 |
| AMB-5 | "se pide la clave actual y la nueva clave" | No se dice qué pasa si nueva ≠ repetida, si la nueva tiene otros dígitos que 4 o si es igual a la actual, ni el efecto de ESC. Los pantallazos piden 3 campos. | R10, R11 |
| AMB-6 | "se bloqueará la tarjeta y no se podrá volver a acceder con esa tarjeta al sistema" | El bloqueo debe persistir entre ejecuciones. Puede usar `intentos.ubd` (existe). Falta ver si `BANK1` ya lo aplica al login. Desbloqueo = fuera de alcance ("oficina"). | R13 |
| AMB-7 | "se ejecute puntualmente en una fecha dada" | No hay proceso nocturno. ¿Quién la ejecuta y cuándo? (al iniciar sesión / al listar / nunca, solo se calcula). Fecha pasada, hoy o futura: aceptar, rechazar o ejecutar en el acto. | R22, R25 |
| AMB-8 | "mensualmente en el día indicado (por ejemplo, los días 3 de cada mes)" | Día 29-31 en meses que no lo tienen. Fecha de inicio y de fin (o sin fin). Si sin fin, el listado se acota por las fechas del usuario. | R23, R25 |
| AMB-9 | "todas las transferencias … que se han ejecutado o están pendientes … entre dos fechas indicadas por el usuario" | Rango inclusivo o no; solo ordenadas por el usuario o también recibidas; incluir las inmediatas ya hechas; estado si no hay saldo el día de ejecución; formato de salida y paginación; máximo de filas. | R25, R26 |
| AMB-10 | "pedir cuántos billetes de 10, 20 y 50 euros" | Límites por billete, aceptar 0 en alguno, total 0, ESC para cancelar. Un solo movimiento con el total o uno por denominación. | R27-R30 |
| AMB-11 | "usando un azul más claro cuando se use este color" | Qué azul concreto (`HIGHLIGHT` sobre BLUE, cian, azul claro del compilador). Todos los usos de azul (títulos) o solo alguno. | R32 |
| AMB-12 | "En algunas pantallas aparecen … unos ceros" | Por descubrir cuáles: se determina ejecutando. Se sospecha de los `ACCEPT (24 80)` (guía, sin verificar). | R33 |
| AMB-13 | "La ventana de ejecución debería ser siempre de 80x25 caracteres" | (a) fijar el tamaño de la consola al arrancar (lanzador `.bat` con `mode con`); (b) solo diseñar para 80x25. "Debería" en vez de "deberá". | R34 |
| AMB-14 | "los tres pasos que has seguido" / "unas 4-5 páginas aprox." | "Tres pasos" = Objetivos 1, 2 y 3. Formato de la memoria (PDF/DOCX). Va dentro del zip o aparte. | R35, R36 |
| AMB-15 | "incluyendo todo lo necesario para poder ejecutar las practicas desde Windows haciendo doble click en un fichero" | Frase dentro de "Documentación": instrucciones en la memoria, o material a incluir en el zip (runtime, `.bat`, datos). "las practicas" en plural. | R42 |
| AMB-16 | "(por defecto, la tarjeta 000000000000000n tiene de clave 000n)" | Ese comportamiento sale de `tarjetas.ubd` (existe). El guion no dice con qué saldos arrancar `movimientos.ubd` ni si hay que entregar datos de prueba. | R7, R15 |

---

## Hallazgos en el código (verificados; NO son requisitos del guion)

| # | Hallazgo | Evidencia |
|---|---|---|
| F1 | El menú llama a `BANK8` (Cambiar clave) y no existe su fuente | `BANK1.cbl:209` `CALL "BANK8" USING TNUM`; `unzip -l` lista solo BANK1-BANK7; `grep PROGRAM-ID` |
| F2 | Falta `movimientos.ubd` | Lo abren `BANK2:22`, `BANK3:22`, `BANK4:22`, `BANK5:22`, `BANK6:34`, `BANK7:28`; el zip trae solo `tarjetas`, `intentos` y `espectaculos` |
| F3 | Los errores usan texto negro sobre fondo rojo | `BANK1.cbl:222-276` |
| F4 | Los `.ubd` son Berkeley DB Btree; GnuCOBOL local (`cobc` 3.1.2) usa BDB y los lee sin conversión (5 tarjetas con PIN `000n`; la tarjeta 5 conserva 2 intentos) | `file *.ubd`; `cobc --info`; programa de lectura ejecutado 2026-09-21 |
| F5 | El original no compila con ningún dialecto de `cobc`: 251 errores en 3.1.2, de los que 243 (97%) son `DISPLAY (l c)`; hay 275 usos (259 `DISPLAY` + 16 `ACCEPT`) | `cobc -m` sobre los 7 `BANK` con cada `-std=`; `grep` de `DISPLAY *(` y `ACCEPT *(` |
| F6 | Tras reescribir a `LINE l COL c` y compilar con `-free`, 6 de 7 programas compilan con 0 errores en GnuCOBOL 3.1.2 y 3.2.0; solo falla `BANK6.cbl:114` | Contenedores Docker `ubuntu:22.04` y `debian:trixie` |
| F7 | OpenCOBOL 1.1, GnuCOBOL 2.2 y 4.0-early tampoco aceptan el original; la 4.0-early de Ubuntu no soporta `ORGANIZATION INDEXED` | Contenedores Docker `ubuntu:18.04`, `ubuntu:20.04`, `ubuntu:24.04` |
| F8 | `BANK1` ya bloquea el login con `IINTENTOS = 0` y reinicia a 3 en un login correcto | `BANK1.cbl:154`, `237-240`, `255-275`, `292-294` |
| F9 | `BANK1` comprueba `NOT = 00` tras `OPEN`, pero `BANK2` a `BANK7` comprueban `<> 30` (un `OPEN` correcto da `00`) | `grep` en `BANK1-BANK7`; `cobc` devuelve `00` al abrir `tarjetas.ubd` |
| F10 | `BANK3` ya tiene el formulario "Entre las fechas" y paginación con AvPág/RePág/flechas | `BANK3.cbl:261`, `65-68`, `344-348` |
| F11 | `BANK5` es un bucle de ingresos: cada Enter escribe un movimiento y ESC finaliza y muestra el total | `BANK5.cbl:212-303` |
| F12 | Escobol.com (archive.org) ofrece 114 descargas hacia `descargas.cabasc.com/ftp/` (106 ficheros archivados); solo 3 toolchains (`cob650`, `mscobol5`, `pcobol3`) | CDX de archive.org y `unzip -l` |

Hipótesis de `guia-cobol.pdf` contrastadas: **H1** (275 usos), **H2** (`BANK6:114`), **H5** (falta `BANK8`), **H6** (falta `movimientos.ubd`) verificadas; **H3** (literales que pasan de la columna 72) corroborada (desaparece en formato libre); **H7** (`<> 30`) corroborada por analogía, falta ejecutar. **H4, H8 y H9 sin verificar.**

## Resolución de ambigüedades (fase 4)

| ID | Resolución | Origen | Estado |
|---|---|---|---|
| AMB-1 | Entrega el lunes 28-sep a las 23:59 **por email** | Usuario | Cerrada |
| AMB-2 | Grupo 02, dos cifras: `SL-P1-02.zip` | Usuario | Cerrada |
| AMB-3 | GnuCOBOL 3.2 con Berkeley DB; [2] no ofrece compilador utilizable; MS COBOL 5 y PowerCOBOL 3 sin ejecutar. GnuCOBOL confirmado como válido el 2026-09-26 | D12 | Cerrada |
| AMB-4 | Obj. 1 verifica las 7 opciones base (incluye reconstruir `BANK8`); las 8 finales son regresión al cerrar Obj. 2 y 3 | Usuario | Cerrada |
| AMB-5 | Nueva ≠ repetida: error y reintento sin gastar intento, se comprueba primero la clave actual; nueva clave de 4 dígitos (D2); ESC vuelve al menú sin cambios | Usuario | Cerrada |
| AMB-6 | Contador compartido `intentos.ubd`; tras el bloqueo se cierra sesión (D1) | Usuario | Cerrada |
| AMB-7 | Ejecución al iniciar sesión con cualquier tarjeta (D3); fecha pasada se rechaza (D4); fecha de hoy se ejecuta en el acto (D5) | Usuario | Cerrada |
| AMB-8 | Día 29-31: último día del mes (D6); sin fecha de fin (D7); primera ejecución este mes si el día no ha pasado, si no el siguiente (D8) | Usuario | Cerrada |
| AMB-9 | Enviadas por quien hace login en todos los estados y recibidas solo ya ejecutadas, con columna de sentido (D10, revisada 2026-09-26); inmediatas como "Puntual, ejecutada"; estado por fecha (D14); rango inclusivo, inicio > fin es error; 15 filas por página; un solo fichero `transferencias.ubd` (D13). D9 anulada por el correo del docente (el cajero no ejecuta las programadas) | Usuario | Cerrada |
| AMB-10 | Una operación por visita; 0 parcial permitido y total 0 da error; un movimiento con el total; ESC cancela (D11) | Usuario | Cerrada |
| AMB-11 | Azul más claro: `BLUE`+`HIGHLIGHT` en todos los títulos, elegido viendo en la consola de Windows las opciones azul, azul brillante, cian y cian brillante (2026-09-25) | Usuario | Cerrada |
| AMB-12 | Se descubre ejecutando; sospecha: 16 `ACCEPT (24 80)` | — | **Abierta** |
| AMB-13 | `lanzar.bat` con `mode con cols=80 lines=25` y pantallas diseñadas dentro de 80x25 | Usuario | Cerrada |
| AMB-14 | "Tres pasos" = Objetivos 1, 2 y 3; memoria en PDF dentro del zip | Usuario | Cerrada |
| AMB-15 | Zip autocontenido (ejecutable, runtime, datos, lanzador) e instrucciones en la memoria | Usuario | Cerrada |
| AMB-16 | `movimientos.ubd` se crea con un programa COBOL de inicialización (saldos iniciales de 100 a 500 €, uno por tarjeta); reiniciar `intentos.ubd` | Usuario | Cerrada |

## Decisiones provisionales y pendientes de preguntar al profesor

Actualizado el 2026-09-26: ya no queda ninguna decisión provisional.
- D12: GnuCOBOL es válido (confirmado el 2026-09-26).
- D10: el listado incluye las enviadas y las recibidas ya ejecutadas (decidido el 2026-09-26, como un banco normal).
- D9: anulada; según el correo del docente, el cajero solo registra y consulta las transferencias programadas.

**Pendiente de conocer:** formato y fecha de la defensa oral o demostración.

**Otras condiciones de trabajo acordadas:** matriz de pruebas de casos límite con capturas; `build.sh`, `build.bat` y `lanzar.bat`.
