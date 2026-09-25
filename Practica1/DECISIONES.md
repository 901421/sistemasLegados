# Decisiones de la práctica (para consultar con el grupo)

Cada decisión lleva: el problema, lo que se decide y por qué es lógico. Fecha de cada entrada entre paréntesis.
Referencias: `Rn` = requisito, `AMBn` = ambigüedad de `docs/requisitos.md`.

---

## D1 — Cómo se cuentan los 3 intentos y qué pasa al bloquear la tarjeta al cambiar la clave (2026-09-21)

**Problema.** El guion pide que, al fallar la clave actual al cambiarla, se muestren los intentos restantes (máximo 3) y que tras el tercer fallo se bloquee la tarjeta y "no se pueda volver a acceder" (R12, R13, AMB6). No dice cómo contar los intentos ni qué ocurre después del bloqueo.

**Decisión.**
- El cambio de clave usa el **mismo contador persistente `intentos.ubd`** que el login. Cada clave actual incorrecta resta 1 y muestra "Le quedan N intentos". Al llegar a 0 la tarjeta queda bloqueada.
- Tras el tercer fallo se muestra el aviso de bloqueo y se **cierra la sesión**, volviendo a la pantalla de bienvenida.

**Por qué es lógico.**
- `BANK1` ya bloquea el login cuando `IINTENTOS = 0` (`BANK1.cbl:154`, `237-240`). Dejar ese campo a 0 cumple "no se podrá volver a acceder" sin tocar la lógica del login.
- El guion describe exactamente la mecánica que `BANK1` ya usa para el PIN (intentos restantes, máximo 3, bloqueo). Se reutiliza el patrón de `BANK1.cbl:255-275`.
- Al ser persistente, no se puede saltar el máximo de 3 saliendo con ESC y volviendo a entrar. Un contador en memoria daría 3 intentos nuevos cada vez.
- Con la tarjeta ya bloqueada, dejar al usuario seguir operando en el menú contradiría "no se podrá volver a acceder", por eso se cierra la sesión.

**Descartado.** Contador solo en memoria (permite saltarse el máximo con ESC y reentrada). Contador propio en fichero o campo nuevo (más código, obliga a tocar `BANK1`, y el guion no lo pide).

**Nota.** El código del `BANK8` original no está en el backup, así que esto es una inferencia razonable, no algo comprobado.

---

## D2 — Formato de la nueva clave: 4 dígitos numéricos (2026-09-21)

**Problema.** El guion pide una "nueva clave" pero no dice qué formato debe tener (R10, AMB5).

**Decisión.** La nueva clave son exactamente **4 dígitos numéricos**.

**Por qué es lógico.**
- El registro de tarjetas guarda la clave como `TPIN PIC 9(4)` (`BANK1.cbl:30`): cualquier otro formato no cabría sin cambiar el fichero de datos.
- El pantallazo del guion muestra 4 huecos (`____`) para la nueva clave y su repetición.
- La clave por defecto del guion (`000n`) también tiene 4 dígitos.

---

## D3 — Las transferencias programadas se ejecutan al iniciar sesión con cualquier tarjeta (2026-09-21) — **anulada el 2026-09-25**

**Revisión (2026-09-25).** Queda **anulada**: el docente indicó por correo que las transferencias puntuales y mensuales solo se **registran** en los ficheros y se **consultan** desde el cajero; el mecanismo que detecta la fecha y las ejecuta no forma parte del cajero (lo hacen otros programas de UnizarBank) y no hay que programarlo. El cajero no ejecuta las programadas; solo las registra (`BANK6`) y las consulta (listado, D14). Se conserva abajo el texto original como parte del proceso seguido.

**Problema.** El guion pide transferencias que "se ejecuten" en una fecha dada o mensualmente, y un listado de las "ejecutadas o pendientes" (R22, R23, R25, AMB7). Pero un cajero es un programa interactivo, sin proceso nocturno, y el guion no dice cuándo se dispara la ejecución.

**Decisión.** Tras un login correcto, un módulo procesa las órdenes vencidas (de **cualquier** tarjeta) y escribe los movimientos reales, igual que hace hoy una transferencia inmediata (dos movimientos: negativo al origen y positivo al destino, `BANK6.cbl:311-347`).

**Por qué es lógico.**
- El guion habla de que la transferencia "se ejecute" y de las que "se han ejecutado": el dinero tiene que moverse de verdad. Si solo se comparasen fechas, el saldo y los movimientos nunca reflejarían la transferencia.
- No existe saldo guardado: el saldo es el `SALDOPOS` del último movimiento, así que ejecutar = escribir movimientos.
- Procesar al entrar **cualquier** tarjeta (y no solo la origen) evita que el destinatario vea un saldo desfasado si entra antes que el ordenante.
- Se descarta ejecutar al abrir el listado porque una pantalla de consulta no debe tener efectos secundarios.

**Descartado.** Solo calcular el estado al listar (más barato, pero incumple el "se ejecute"). Ejecutar al abrir el listado.

**Consecuencia.** Lo ejecutado quedará como hecho guardado y lo pendiente se calculará a partir de las órdenes. El diseño exacto del fichero se fija en su propia decisión.

---

## D4 — Una transferencia puntual con fecha pasada se rechaza (2026-09-21)

**Problema.** El guion no dice qué hacer si el usuario programa una transferencia puntual con una fecha ya pasada (R22, AMB7).

**Decisión.** Se rechaza con un mensaje de error y se vuelve a pedir la fecha.

**Por qué es lógico.** Es la regla de negocio habitual: no se programa algo en el pasado. Para dejar constancia de una transferencia de hoy existe la opción inmediata o la fecha de hoy (D5), así que no hace falta admitir fechas pasadas.

---

## D5 — Una transferencia puntual con la fecha de hoy se ejecuta en el acto (2026-09-21) — **revisada el 2026-09-25**

**Revisión (2026-09-25).** Como el cajero no ejecuta las programadas (D3 anulada), una puntual con la fecha de hoy **se admite y se registra como las demás**; en el listado aparece como **pendiente** (la ejecutará el banco ese día, D14). Las fechas pasadas se siguen rechazando (D4). Texto original:

**Problema.** Una transferencia puntual programada para hoy no es pasada ni futura (R22, AMB7).

**Decisión.** Se ejecuta inmediatamente y queda registrada como transferencia puntual ejecutada.

**Por qué es lógico.** "Hoy" ya ha llegado: esperar no tiene sentido y equivale a una transferencia inmediata. Además es la forma de tener transferencias "ejecutadas" visibles en el listado sin alterar la fecha del sistema.

---

## D6 — Mensual con día 29, 30 o 31: se ejecuta el último día del mes (2026-09-21)

**Problema.** No todos los meses tienen día 29, 30 o 31, y el guion solo da el ejemplo "los días 3 de cada mes" (R23, AMB8).

**Decisión.** Si el día indicado no existe en un mes, la transferencia se ejecuta el **último día de ese mes** (p. ej. "día 31" se ejecuta el 28 o 29 de febrero y el 30 de abril).

**Por qué es lógico.** Es la convención bancaria habitual y hace que la orden se ejecute los 12 meses. Saltar el mes haría que "día 31" se ejecutara solo 7 meses al año, algo que el usuario no espera de una orden mensual.

**Descartado.** Saltar el mes sin ejecutar.

---

## D7 — Las transferencias mensuales no tienen fecha de fin (2026-09-21)

**Problema.** El guion no dice cuándo termina una transferencia mensual (R23, AMB8).

**Decisión.** La orden mensual no tiene fecha de fin: es una orden permanente. El listado sigue siendo finito porque solo muestra lo comprendido entre las dos fechas que indica el usuario.

**Por qué es lógico.** El ejemplo del guion ("los días 3 de cada mes") describe una orden permanente y el enunciado no pide una fecha de fin. Añadirla ampliaría el formulario y el registro sin que el cliente lo haya solicitado.

**A tener en cuenta.** Es una limitación consciente: **no lo hacemos porque el enunciado no lo pide**, no porque sea imposible. Si el profesor lo esperase, habría que añadir el campo "fecha de fin" al formulario de ordenar transferencia, guardarlo en el registro de órdenes y dejar de generar ocurrencias pasada esa fecha. Conviene mencionarlo en la memoria como mejora no incluida.

---

## D8 — Primera ejecución de una mensual: este mes si el día no ha pasado, si no el siguiente (2026-09-21) — **revisada el 2026-09-25**

**Revisión (2026-09-25).** Se mantiene la regla salvo el caso "día indicado = hoy": ya no se ejecuta en el acto (D3 anulada), sino que **la primera ocurrencia es hoy** y aparece como pendiente en el listado. Queda así: día indicado igual o posterior a hoy → primera ocurrencia este mes; día ya pasado → el mes siguiente. Para aplicarla se guarda la **fecha de alta** de la orden (D13). Texto original:

**Problema.** Si el día indicado ya pasó este mes (hoy es día 15 y se ordena "día 3"), el guion no dice cuándo empieza la periodicidad (R23, AMB8).

**Decisión.**
- Día indicado = hoy: se ejecuta en el acto (coherente con D5).
- Día indicado aún por llegar este mes: primera ejecución este mes.
- Día indicado ya pasado este mes: primera ejecución el mes siguiente.

**Por qué es lógico.** Es lo que espera un usuario: nunca se ejecuta con retraso ni en una fecha ya pasada (coherente con D4), y no se pierde una ocurrencia que aún podría producirse este mes.

---

## D9 — Sin saldo el día de ejecución: no se ejecuta y queda "Fallida por saldo" (2026-09-21) — **anulada el 2026-09-25**

**Revisión (2026-09-25).** Queda **anulada**: el docente indicó por correo que las transferencias puntuales y mensuales solo se **registran** en los ficheros y se **consultan** desde el cajero; el mecanismo que detecta la fecha y las ejecuta no forma parte del cajero (lo hacen otros programas de UnizarBank) y no hay que programarlo. Quien comprueba el saldo el día de la ejecución es el programa del banco que la ejecuta, y no deja ningún resultado que el cajero pueda leer, así que el listado no puede mostrar "Fallida por saldo" (ver D14, limitación). Al ordenar la transferencia se sigue comprobando que el importe no supera el saldo actual, como ya hacía `BANK6`. Texto original:

**Problema.** Si el día en que toca ejecutar una transferencia programada el origen no tiene saldo suficiente, el guion no dice qué hacer (R22, R23, R25, AMB9).

**Decisión.** La transferencia **no se ejecuta** y queda registrada como **"Fallida por saldo"**, visible en el listado. Si es mensual, esa ocurrencia falla y la del mes siguiente se intenta con normalidad. No hay reintentos.

**Por qué es lógico.** La aplicación no permite descubierto: `BANK6` rechaza cualquier transferencia mayor que el saldo (*"Indique una cantidad menor!!"*). Ejecutarla igualmente contradiría esa regla. Reintentar en cada login añadiría un estado intermedio y complejidad que el guion no pide.

**Descartado.** Ejecutar igualmente (descubierto). Reintentar hasta que haya saldo.

**Pendiente.** Es una decisión que afecta a lo que el profesor espera ver en el listado: **consultarla con el docente**. Hasta que responda, se aplica esta decisión provisional.

---

## D10 — Qué muestra el "Listado de transferencias": alcance e inmediatas (2026-09-21) — **ajustada el 2026-09-25 y revisada el 2026-09-26**

**Revisión (2026-09-26): el listado incluye también las transferencias recibidas ya ejecutadas.** Se toma como referencia lo que muestra un banco normal:
- **Enviadas** (ordenadas por la tarjeta que hace login): todas, inmediatas, puntuales y mensuales, en cualquier estado (ejecutada o pendiente, D14), como hasta ahora.
- **Recibidas** (la tarjeta es la cuenta destino): **solo las ya ejecutadas**, es decir, las inmediatas y las ocurrencias de puntuales y mensuales con fecha anterior a hoy (D14). Las recibidas **pendientes no se muestran**: son órdenes del que paga, que puede anularlas, y un banco no las enseña al receptor.
- Una columna nueva indica el sentido de cada fila (**Enviada** o **Recibida**) y la columna de cuenta muestra la cuenta de la otra parte (destino si se envía, origen si se recibe).

**Por qué.** Es la lectura más amplia razonable de "todas las transferencias" del guion y coincide con lo que un cliente ve en su banco: sus órdenes (con las programadas y periódicas) y lo que ya ha recibido. Mostrar las recibidas pendientes enseñaría órdenes privadas de otra persona que pueden no llegar a ejecutarse.

**Descartado.** Solo enviadas (alcance anterior, más estrecho que "todas"). Enviadas y recibidas en todos los estados (muestra órdenes ajenas pendientes).

**Ajuste (2026-09-25).** Se mantiene el alcance y el registro de las inmediatas. Cambia lo que dependía de D3, D9 y D13: los estados posibles son solo **ejecutada** y **pendiente** (según D14; ya no hay "fallida por saldo") y las transferencias se anotan en `transferencias.ubd` (D13), no en un fichero de órdenes aparte.

**Problema.** El guion dice: *"se mostrarán todas las transferencias (indicando si son puntuales o periódicas) que se han ejecutado o están pendientes de hacerse entre dos fechas indicadas por el usuario"* (R25, R26, AMB9). Deja abiertas dos cosas:
1. Qué significa "todas": ¿solo las que ordena quien hace login, o también las que recibe?
2. Qué pasa con las transferencias **inmediatas** (la opción 5 de siempre, que se ejecuta al momento) y no son ni puntuales ni periódicas.

**Decisión.**
1. **Alcance:** el listado muestra **solo las transferencias ordenadas por la tarjeta que hace login**, en todos sus estados (ejecutada, pendiente, fallida por saldo, ver D9) y de todos los tipos (puntual y periódica). Las transferencias que la tarjeta *recibe* no se incluyen en este listado.
2. **Inmediatas:** también aparecen. Se registran como **"Puntual, ejecutada"** con la fecha de hoy. Para ello `BANK6` anotará también cada transferencia inmediata en el nuevo fichero de órdenes.

**Ejemplo.** Hoy es 21-09-2026. La tarjeta **A** (la que hace login) y la **B**:
- A ordena a B 50 € **ahora mismo** (inmediata).
- A ordena a B 100 € puntual para el 05-10.
- A ordena a B 30 € mensual el día 3 (el día 3 ya pasó este mes, así que la primera ejecución es el 03-10, ver D8).
- B ordenó a A 20 € el 10-09, ya ejecutada (A la recibió).
- B ordena a A 15 € puntual para el 10-10, pendiente.

A pide el listado entre el 01-09-2026 y el 31-12-2026:

| Fecha | Destino | Importe | Tipo | Estado |
|---|---|---|---|---|
| 21-09-2026 | B | 50,00 | Puntual | Ejecutada |
| 03-10-2026 | B | 30,00 | Periódica | Pendiente |
| 05-10-2026 | B | 100,00 | Puntual | Pendiente |
| 03-11-2026 | B | 30,00 | Periódica | Pendiente |
| 03-12-2026 | B | 30,00 | Periódica | Pendiente |

No aparecen los 20 € que A recibió de B el 10-09 (A ya los ve en "Consultar movimientos" como movimiento positivo), ni los 15 € que B ordenó a A para el 10-10 (es una orden de B, no de A).

**Por qué es lógico.**
- La frase del guion ya enumera dos estados (ejecutadas o pendientes) y dos tipos (puntuales o periódicas): "todas" significa **sin filtrar por estado ni por tipo**, no "de todas las tarjetas".
- Mostrar las pendientes de otra persona enseñaría órdenes privadas de otra tarjeta. Y para añadir las recibidas habría que identificarlas por el texto del concepto en `movimientos.ubd`: más frágil y más trabajo, sin que el guion lo pida.
- Si las inmediatas no se registrasen, una tarjeta haría una transferencia real, pediría "todas las transferencias que se han ejecutado" y **no la vería**: parecería un fallo del listado. Registrarla como "Puntual, ejecutada" es coherente con D5 (una puntual de hoy se ejecuta en el acto). No se pierde ningún dato antiguo porque `movimientos.ubd` no está en el backup y se crea de cero.

**Descartado.** Incluir también las recibidas. No registrar las inmediatas.

**Pendiente.** La lectura de "todas" es la que más puede variar según el profesor. Si se puede, **consultarla con el docente**.

---

## D11 — Ingreso de efectivo por billetes: flujo, ceros, movimientos y ESC (2026-09-21)

**Problema.** El apartado 3.a dice: *"En lugar de pedir el total del efectivo a ingresar, pedir cuántos billetes de 10, 20 y 50 euros se van a ingresar. Al pulsar Enter deberá aparecer una pantalla donde se informe que se han recibido correctamente, la cantidad de euros ingresados, y el saldo disponible tras la operación"* (R27 a R30, AMB10). El guion no dice qué pasa con el bucle actual, con los ceros ni con los movimientos.

**Cómo funciona hoy `BANK5`.** Es un **bucle**: cada Enter escribe **un movimiento** y vuelve a pedir otro importe. ESC ("Finalizar ingreso efectivo") sale del bucle y muestra la pantalla final con el total acumulado y el saldo (`BANK5.cbl:212-303`). Esa pantalla final es la que pide el guion, pero hoy solo aparece al pulsar **ESC**, no al pulsar Enter.

**Decisión.**
1. **Flujo: una operación por visita.** Se rellenan los tres campos (billetes de 10, 20 y 50), Enter, pantalla de confirmación ("se han recibido correctamente", euros ingresados y saldo tras la operación), Enter y vuelta al menú. El bucle actual desaparece.
2. **Ceros.** Se permite dejar a 0 cualquier denominación (p. ej. ingresar solo billetes de 20). Si el total es 0 se muestra un error (texto blanco sobre rojo, apartado 3.b) y no se escribe nada.
3. **Movimientos.** Se escribe **un único movimiento** con el total (`10*a + 20*b + 50*c`) y concepto `"Ingreso"`.
4. **ESC.** Cancela y vuelve al menú sin ingresar nada.
5. **Capacidad (decisión técnica).** Cada campo de billetes admite hasta 9.999 unidades. El total máximo (`9.999 * 80 = 799.920 €`) cabe en el importe del movimiento (`MOV-IMPORTE-ENT S9(7)`), y el desbordamiento del saldo ya lo controla el código con `ON SIZE ERROR`.

**Qué conlleva.**
- Se reescribe la pantalla de entrada de `BANK5` (`ENTRADA-USUARIO`, definida en `BANK5.cbl:95`, leída en `228`): de un campo de importe a tres campos de billetes, y se calcula el total antes de escribir el movimiento.
- Se elimina el retorno a la pantalla de ingreso tras cada movimiento (`GO TO PANTALLA-INGRESO`, `BANK5.cbl:277`) y el acumulador `CENT-ACUMULADOR` (`80`, `144`, `237`), porque ya no hay varias entradas.
- La pantalla de confirmación reutiliza la que ya existe (`PANT`, `BANK5.cbl:282`), que ya informa de euros recibidos y saldo resultante, pero ahora se muestra al pulsar Enter.
- Aparece una pantalla de error nueva (total 0), que debe cumplir el apartado 3.b (texto blanco sobre fondo rojo).
- El texto "ESC - Finalizar ingreso efectivo" cambia a "ESC - Cancelar".
- Hay que probar: solo un tipo de billete, todos los tipos, total 0, ESC, y saldo que desborde.

**Por qué es lógico.**
- El guion dice que al pulsar **Enter** aparece la pantalla de confirmación: eso es una operación por visita, no un bucle.
- Ingresar 0 € no es una operación, y exigir al menos un billete de cada tipo sería irreal para el usuario.
- Un solo movimiento con el total es lo natural; tres movimientos (uno por denominación) ensuciarían "Consultar movimientos" con líneas que el usuario no espera.
- Como Enter ya confirma, ESC no puede "finalizar": solo puede cancelar.

**Descartado.** Mantener el bucle. Exigir un billete de cada tipo. Un movimiento por denominación.

---

## D12 — Compilador: GnuCOBOL 3.2 con Berkeley DB (2026-09-21) — **preguntar al docente**

**Problema.** El guion pide compilar con *"algún compilador de COBOL [2]"* (`escobol.com` en archive.org) y obtener un ejecutable para Windows, y avisa de que *"cada compilador acepta su propia versión y sintaxis"* y de que no se sabe cuál se usó (R17, R18, AMB3). El código original **no compila** con `cobc`, y el 97% de los errores es un solo problema: la sintaxis posicional `DISPLAY (l c)` / `ACCEPT (l c)` (275 usos: 259 + 16).

**Qué se ha comprobado (todo en copias, sin tocar los originales).**
- **[2] no ofrece ningún compilador utilizable en la práctica.** Se enumeraron las 114 descargas de escobol.com: apuntan a `descargas.cabasc.com/ftp/`, con 106 ficheros archivados y ninguno perdido. Casi todo son ejemplos, utilidades, librerías y manuales. Solo hay 3 toolchains:
  - `cob650.zip`: compilador ANSI DOS de 1990, con enlazador externo. En modo desatendido no produjo resultado.
  - `mscobol5.zip`: Microsoft COBOL 5.0 (1993) "para MS-DOS y Windows". Era DOS/Windows 3.x. **No ejecutado.**
  - `pcobol3.zip`: instalador InstallShield de PowerCOBOL 3 con Fujitsu COBOL85, Windows de 32 bits, gratuito según el propio `leeme` de escobol. **No ejecutado.**
  - Acucobol-GT, NetExpress, Objective COBOL y RM/COBOL solo aparecen como artículos, sin instalador.
- **Ningún GnuCOBOL/OpenCOBOL acepta el código original.** Errores al compilar los 7 `BANK` originales: OpenCOBOL 1.1 = 62 (solo informa del primero por párrafo), GnuCOBOL 2.2 = 249, 3.1.2 = 251, 3.2.0 = 255, 4.0-early = 249 y sin soporte de ficheros indexados.
- **Con una reescritura mecánica de las 275 sentencias, en formato libre, 6 de 7 programas compilan con 0 errores** en la 3.1.2 y en la 3.2.0. Queda solo `BANK6.cbl:114` (`SIGN IS LEADING SEPARATE` sobre `PIC 9(7)` sin `S`), un arreglo de una línea.
- Los `.ubd` originales son Berkeley DB y `cobc` los lee sin conversión: 5 tarjetas con PIN `000n`, como dice el guion.

**Decisión.** Usar **GnuCOBOL 3.2 con Berkeley DB** (binarios de Windows de terceros). Desarrollo en WSL con `cobc` 3.1.2 y referencia de la 3.2.0 en Docker. Cambio de código: un script mecánico + una línea manual.

**Por qué es lógico.**
- **Es el que menos cambia vuestro código de los que se han podido probar.** El cambio necesario es idéntico en todas las versiones de la familia (todas rechazan la misma sintaxis), así que una versión más antigua **no ahorra ninguna modificación**.
- **No hay que migrar datos**: Fujitsu y Microsoft COBOL usan formatos indexados propios, y los `.ubd` originales son de Berkeley DB.
- Es libre, hay binarios de Windows con Berkeley DB, y el resultado es reproducible.

**Limitaciones (no ocultar en la memoria).**
- **MS COBOL 5 y PowerCOBOL 3/Fujitsu no se han ejecutado.** Puede que alguno acepte `DISPLAY (l c)` y requiera menos cambios de sintaxis. Aun así, MS COBOL 5 es de 16 bits y no daría un ejecutable de Windows, y Fujitsu obligaría a migrar los datos.
- **Sin verificar aún:** que el binario de Windows de la 3.2 lea los `.ubd` originales (la versión de Berkeley DB puede variar) y que el ejecutable arranque con doble clic. Se comprobará en un Windows real al final.
- El guion pide un compilador de [2] y aquí se usa uno que **no** procede de [2]. Es la caída prevista en B3.

**Pendiente.** **Preguntar al docente** si es aceptable usar GnuCOBOL en lugar de un compilador de [2]. Hasta que responda, se aplica esta decisión provisional.

**Descartado.** OpenCOBOL 1.1 y GnuCOBOL 2.2 (mismo cambio, sin ventaja). GnuCOBOL 4.0-early (sin ficheros indexados). Los compiladores DOS (no dan ejecutable de Windows).

---

## D13 — Las transferencias se guardan en dos ficheros: `ordenes.ubd` y `ejecuciones.ubd` (2026-09-21) — **revisada el 2026-09-25**

**Revisión (2026-09-25).** Como el cajero no ejecuta las programadas (D3 anulada), no hay ejecuciones que guardar y **basta un solo fichero, `transferencias.ubd`**, con una fila por transferencia ordenada. Se toma el diseño que ya había hecho Ainhoa (commits `6ce57e0`, `9320811`) y se le añade la fecha de alta, necesaria para saber desde cuándo vale una mensual (D8):

| Campo | Contenido |
|---|---|
| número (clave) | correlativo |
| tarjeta origen, cuenta destino | 16 dígitos cada una |
| importe | entero y decimales |
| tipo | 1 inmediata, 2 puntual, 3 mensual |
| fecha | inmediata: hoy; puntual: la fecha indicada; mensual: solo el día del mes |
| fecha de alta | día en que se ordenó |

El estado (ejecutada o pendiente) no se guarda: se deduce al listar (D14). El fichero se crea vacío en la instalación (`build.bat`/`build.sh`), como `movimientos.ubd`. Texto original:

**Problema.** Para las transferencias puntuales y mensuales hay que guardar dos cosas distintas (R22, R23, R25, R26; D3, D9, D10):
- **La orden:** "de A a B, 30 €, mensual, día 3". Vive mientras esté vigente.
- **Cada ejecución real:** "el 03-10 se ejecutó" o "el 03-11 falló por saldo". Es un hecho ya ocurrido.

El listado de la opción 6 mezcla ambas: los hechos guardados dentro del rango de fechas, más las próximas fechas que se calculan a partir de las órdenes vigentes.

**Decisión.** Dos ficheros indexados, como el resto de la aplicación (Berkeley DB):

| Fichero | Una fila por | Campos previstos (diseño provisional) |
|---|---|---|
| `ordenes.ubd` | orden | número de orden (clave), tarjeta origen, cuenta destino, importe (entero y decimales), tipo (P puntual, M mensual), fecha (puntual) o día del mes (mensual), fecha de alta, fecha de la última ocurrencia procesada (0 si ninguna) |
| `ejecuciones.ubd` | ocurrencia procesada | número de ejecución (clave), número de orden, tarjeta origen, fecha de la ocurrencia, estado (E ejecutada, F fallida por saldo, ver D9) |

- Las transferencias **inmediatas** también se anotan como una orden puntual con fecha de hoy y su ejecución ya en estado E (D10).
- La ejecución al iniciar sesión (D3) recorre `ordenes.ubd`, procesa lo vencido, escribe los movimientos reales en `movimientos.ubd` y una fila en `ejecuciones.ubd` por cada ocurrencia. La "última ocurrencia procesada" evita duplicados.

**Por qué es lógico.**
- Cada fichero tiene un **registro fijo**, que es lo más sencillo en COBOL y lo que ya hace toda la aplicación (un fichero por tipo de dato). Evita `REDEFINES`, más delicado para quien empieza.
- Guardar cada ocurrencia permite mostrar el **historial exacto**, incluida "Fallida por saldo" (D9), y saber qué mes de una mensual se ejecutó y cuál no.
- Separar el hecho (ejecución) de la regla (orden) permite calcular las pendientes sin escribir nada por adelantado.

**Descartado.** Un solo fichero con dos tipos de fila (obliga a `REDEFINES`). Guardar solo órdenes con "última ejecución" (no permite mostrar fallidas ni historial de mensuales, rompe D9).

**Nota.** Los campos son provisionales: se fijan al implementar el apartado 2 y se actualiza esta entrada si cambian.

---

## D14 — Estado de cada transferencia en el listado: se deduce de la fecha (2026-09-25)

**Problema.** El listado debe mostrar las transferencias *"que se han ejecutado o están pendientes de hacerse"* (R25). Pero el cajero no ejecuta las programadas (D3 anulada por el correo del docente): las ejecutan otros programas del banco, que no dejan ningún resultado que el cajero pueda leer.

**Decisión.**
- Inmediata: **ejecutada** (el cajero la hace en el acto).
- Puntual y cada ocurrencia de una mensual: fecha **anterior a hoy → ejecutada**; fecha de **hoy o posterior → pendiente**.

**Por qué es lógico.** Según el correo, las programadas se ejecutan en su fecha por los programas del banco; si la fecha ya pasó, el banco ya la ha procesado. Es la única información de la que dispone el cajero, y no obliga a escribir nada por adelantado.

**Descartado.**
- Marcar todas las programadas como pendientes aunque su fecha haya pasado (el listado diría que nunca se ejecuta nada).
- Buscar en `movimientos.ubd` el movimiento de cada ocurrencia para saber si se ejecutó: no hay programa ejecutor en esta práctica, así que nunca aparecería, y habría que identificar las transferencias por el texto del concepto.
- Recalcular el saldo en cada fecha pasada para marcar "fallida por saldo": sería simular una ejecución que no ha ocurrido (el saldo no se ha reducido por ella).

**Limitación.** "Ejecutada" significa "su fecha ya pasó y la procesó el banco"; si el programa del banco no pudo ejecutarla (por ejemplo, por saldo insuficiente), el cajero no lo sabe. Mejora posible, fuera de lo pedido: que el programa ejecutor anote el resultado de cada ocurrencia en un fichero que el listado consulte para mostrar también "fallida".
