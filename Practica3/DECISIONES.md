# Decisiones de la práctica (para consultar con el grupo)

Cada decisión lleva: el problema, lo que se decide, por qué, lo descartado y lo que queda pendiente. Fecha de cada entrada entre paréntesis.
Referencias: `Rn` = requisito y `AMB-n` = ambigüedad de `docs/requisitos.md`.

---

## D1 — Estructura del código: paquetes `legado`, `servidor` y `pruebas` (2026-11-15)

**Problema.** `src/legado/Main.java` lo hacía todo en 220 líneas: arrancar DOSBox, definir las rutas HTTP, servir los ficheros de la web, las cabeceras CORS, construir el JSON y leer los parámetros. Era difícil de leer y de probar, y mezclaba el wrapper con el servidor web.

**Decisión.** Separar por responsabilidades en paquetes (en Java, cada carpeta de `src/` es un paquete):
- `src/Main.java`: solo el arranque. Lanza DOSBox, arranca el servidor y cierra DOSBox al salir.
- `src/legado/`: el wrapper, es decir, hablar con la aplicación legada (`VentanaDosBox`, `Teclado`, `LectorPantalla` y `fuente-dosbox.txt`, `ServicioLegado`, `ParserPantalla`, `Juego`).
- `src/servidor/`: el servidor web (`ServidorWeb`, `ApiLegado`, `FicherosEstaticos`, `Json`, `Parametros`).
- `src/pruebas/`: herramientas de medición y pruebas automáticas.
- `frontend/` sigue aparte (HTML, JS y estilos).

**Por qué.**
- Cada parte se entiende y se prueba por separado. El wrapper no sabe nada de HTTP y el servidor no sabe nada de DOSBox.
- Los scripts de compilación (`build.bat`, `build.sh`) siguen sirviendo: compilan todo lo que hay en `src/`.

**Descartado.** Carpetas `backend/` y `frontend/` en la raíz del proyecto. La separación es la misma, pero obliga a mover `src/` y `lib/` y a cambiar los scripts y las rutas relativas, con más riesgo de que algo falle en Windows.

**Pendiente.** Hacer la reorganización sin cambiar el comportamiento, con pruebas automáticas antes y después.

---

## D2 — Sincronización con la aplicación: esperar a ver cada pantalla y recuperarse si se pierde (2026-11-15)

**Problema.** El wrapper esperaba tiempos fijos (`Thread.sleep` de 30, 50 o 100 ms) antes de capturar. Si DOSBox no había redibujado aún, se leía la pantalla anterior: un juego repetido, una tecla de más y la aplicación desincronizada. El bucle de la búsqueda no tenía salida si algo fallaba, el listado daba por hecho 43 páginas, y tras un fallo a mitad la aplicación se quedaba fuera del MENÚ y fallaban todas las operaciones siguientes.

**Decisión.**
- Un único método `esperar(condición, tiempo máximo)` que captura hasta ver la marca de la pantalla siguiente, según `docs/mapa-pantallas.md` (MENÚ, `CONTIENE`, `ES ESTE ?` con un Nº mayor que el anterior, `NO HAY NINGUN`, primer Nº de cada página del listado…). En cuanto la ve, sigue; si se agota el tiempo, para con un error que dice qué esperaba y qué había.
- Tiempos máximos como constantes con nombre: pantalla 5 s, ordenar 10 s, arrancar DOSBox 15 s, comprobar el MENÚ al empezar una operación 1 s. El listado se recorre hasta volver al MENÚ, y la búsqueda tiene un tope de 763 resultados.
- Al empezar cada operación: si DOSBox se ha cerrado, se lanza de nuevo; si no se ve el MENÚ en 1 s, se cierra DOSBox y se lanza de nuevo antes de seguir.

**Por qué.** El tiempo máximo solo cuenta cuando algo falla: en el caso normal se sigue en cuanto aparece la pantalla, así que va igual o más rápido que los tiempos fijos y nunca lee una pantalla vieja (R20). Reiniciar es seguro porque la aplicación solo escribe en disco con la opción 2, que el wrapper no pulsa nunca (R5).

**Descartado.** Alargar los tiempos fijos "por si acaso" (más lento y sin garantías). Volver al MENÚ pulsando teclas a ciegas (podría caer en "QUIERES ALTERAR SUS DATOS?").

**Resultado (prueba real, 2026-11-15).** Con DOSBox congelado (Alt+Pausa), la consulta dio error a los 10 s (ordenación) con un mensaje claro y la siguiente reinició DOSBox sola. El tiempo de 1 s para comprobar el MENÚ no dio problemas. Hubo que añadir que la pantalla esté quieta (igual en dos capturas separadas 40 ms): la aplicación dibuja el pie de cada página del listado antes de terminar las filas.

---

## D3 — Datos fieles: no inventar, validar la entrada y leer bien la petición (2026-11-15)

**Problema.** Si la pantalla no era la esperada, el analizador devolvía 0 registros o un juego con la línea entera como nombre, en lugar de avisar. El nombre que llegaba de la web se enviaba sin comprobar: una coma o unas comillas cambian lo que recibe el `INPUT` de BASIC, y caracteres que Robot no puede teclear (`ñ`, `#`, `@`…) hacían fallar la operación a mitad. Los parámetros de la dirección se partían en todos los `=`.

**Decisión.**
- El analizador comprueba el formato exacto que imprime la aplicación (`CONTIENE n ARCHIVOS`; `Nº - NOMBRE   TIPO   CINTA:c` con los 3 espacios del programa; filas del listado con Nº y registro numéricos). Si no cuadra, error con la línea leída.
- El nombre se valida antes de tocar DOSBox, en el servidor y en la web: de 1 a 30 caracteres, sin `,` ni `"`, y todos escribibles con Robot (`Teclado.puedeEscribir`). Se envía tal cual, sin pasar a mayúsculas; solo se quitan los espacios del principio y del final.
- Los parámetros se parten en el primer `=`; una codificación inválida o un parámetro repetido dan error 400.

**Por qué.** El resultado debe ser el de la aplicación o un error claro, nunca un dato aproximado (R20). Validar antes evita dejar la aplicación a medias.

**Descartado.** Pasar el nombre a mayúsculas (la aplicación distingue mayúsculas: cambiaría el resultado).

---

## D4 — Número de registros siempre al día (2026-11-15)

**Problema.** La web pedía el número de registros solo al cargar la página, y el guion pide verlo "en todo momento" (R9).

**Decisión.** Se pide a la aplicación (opción 4) al cargar la página, después de cada consulta (también si falla) y con un botón "Actualizar". Debajo se muestra la hora de la lectura. Si falla, se muestra "No disponible" con el motivo. Cada operación queda registrada en la consola del servidor (hora, operación, duración y resultado).

**Por qué.** La cifra sale siempre de la aplicación en ese momento (R8), y la hora lo deja a la vista.

**Descartado.** Refrescar cada pocos segundos: ocuparía la aplicación sin parar y la cifra no cambia (la aplicación no se modifica nunca, R5).

---

## D5 — Una sola aplicación para varios usuarios: cola, foco y cierre (2026-11-15)

**Problema.** El servidor atendía todo con un solo hilo, así que mientras se recorría el listado ni siquiera se servía la página, y quien llegaba segundo esperaba sin aviso. Después de cada operación DOSBox se quedaba delante del navegador. Al cerrar el servidor, DOSBox seguía abierto.

**Decisión.**
- Servidor con varios hilos; las operaciones sobre la aplicación entran de una en una y por orden de llegada (candado justo). Si una petición espera más de 30 s, se responde 503 "ocupado" y la web lo muestra como aviso.
- Cada operación guarda la ventana que tenía el foco y se la devuelve al terminar, también si falla. La web avisa de que no se use el teclado ni el ratón de ese equipo mientras dura la consulta.
- Al cerrar el servidor (Ctrl+C o la X de su ventana) se cierra DOSBox. Al arrancar, se cierra un DOSBox anterior lanzado por el programa (con nuestro `.conf`), sin tocar otros.

**Por qué.** Solo hay una ventana que capturar y Robot solo escribe en una (AMB-11). El orden de llegada es lo justo para varios usuarios.

**Descartado.** Responder "ocupado" al instante sin cola. Una DOSBox por usuario. Minimizar DOSBox entre operaciones.

**Resultado (prueba real, 2026-11-15).** El foco vuelve al navegador tras cada consulta. Si alguien usa el equipo servidor durante una consulta (por ejemplo, un clic en su navegador mientras consulta el móvil), el wrapper vuelve a poner DOSBox delante, como mucho 3 veces por operación, y nunca escribe en otra ventana. Al cerrar la ventana del servidor con la X, Windows Terminal termina Java sin ejecutar el gancho de cierre: DOSBox se mete en un "job" de Windows ligado a Java, y Windows lo cierra al terminar Java por cualquier vía (X, Ctrl+C, Administrador de tareas). Un segundo arranque con el servidor en marcha avisa de que el puerto está en uso y no toca DOSBox.

---

## D6 — Bloq Mayús (2026-11-15)

**Problema.** Robot escribe las mayúsculas pulsando Mayúsculas + letra. Con Bloq Mayús activado, Windows invierte el resultado y "DEFENDER" podría llegar como "defender"; como la búsqueda distingue mayúsculas, no encontraría nada y sin dar error. No se sabe si DOSBox se ve afectado.

**Decisión.** Probarlo siempre en la prueba real con Bloq Mayús activado. Si falla, el wrapper desactiva Bloq Mayús durante la operación y lo deja como estaba al terminar.

**Descartado.** Pedir al usuario que lo desactive: puede estar en otro equipo, sin acceso al teclado del servidor.

**Resultado (prueba real, 2026-11-15).** Con Bloq Mayús activado en el equipo, DEFENDER da los mismos resultados (50 y 323): DOSBox lleva su propio estado de Bloq Mayús, que solo cambia si la tecla se pulsa con DOSBox delante. No hace falta el arreglo. Limitación documentada: pulsar Bloq Mayús, o Alt+Enter (pantalla completa), con DOSBox delante; en el segundo caso, la consulta siguiente reinicia DOSBox.

---

## D7 — Errores y mensajes; API sin CORS (2026-11-15)

**Problema.** Los errores se enviaban como JSON construido a mano sin escapar (un mensaje con comillas lo rompía), la web no mostraba el mensaje del servidor, y los tres puntos de la API repetían el mismo código. La API enviaba `Access-Control-Allow-Origin: *`, que deja a cualquier página leerla.

**Decisión.**
- Un único punto que atiende todas las rutas de la API: solo GET, registro de cada operación y un formato de error común `{"error": …, "tipo": entrada | ocupado | legado | interno}` con 400, 503 o 500, y el texto pensado para el usuario. El detalle técnico va a la consola.
- Escapado de JSON en una sola función.
- La web muestra el mensaje: "ocupado" como aviso y el resto como error.
- Se quitan las cabeceras CORS: la web y la API las sirve el mismo servidor.

**Por qué.** Una regla en un solo sitio no puede cumplirse en una ruta y olvidarse en otra.

---

## D8 — Pruebas automáticas sin DOSBox y comprobación de R5 (2026-11-15)

**Problema.** El código del servidor y del analizador no tenía pruebas, y la comprobación de que la aplicación legada no cambia (R5) era manual.

**Decisión.** `pruebas.PruebasOffline`: un programa sin librerías nuevas que acaba en `TODO BIEN` o `N FALLOS`. Usa las 68 capturas reales, subidas a `pruebas/conjunto/`, y compara con `DATOS.DAT`. Solo lo lee la prueba, para saber qué debería leerse: el wrapper nunca lo lee (R8). Comprueba el analizador, el filtro de cintas, las esperas, la cola, la validación, el JSON, la API con un wrapper falso, el servidor de ficheros y los SHA-256 de `Database-MSDOS` frente al original. La copia `SL-P3-02/Database-MSDOS/` es la extracción exacta del `.rar` y no se edita nunca.

**Por qué.** Cualquiera del grupo puede repetir las pruebas en segundos, y si algo cambia la aplicación legada la prueba falla.

**Descartado.** JUnit: otra dependencia y otra forma de ejecutar, sin necesidad para este tamaño.

---

## D9 — Aspecto de la web: "carátula y pantalla" (2026-11-15)

**Problema.** La web tenía un aspecto genérico (tarjetas blancas, violeta, fuente del sistema) que no decía nada del tema, y algunos detalles confundían: columna "Registro" vacía en la búsqueda, cabecera "Archivo de programas MS-DOS", puerto fijo sin aviso si está ocupado.

**Decisión.**
- Página clara, como una carátula impresa, con un único elemento oscuro: la última pantalla de la aplicación legada que ha capturado el wrapper (gris 170 sobre negro, fuente de 8x16 de los PC de MS-DOS). La API devuelve esas 25 líneas con cada consulta.
- El número de registros como contador de cinta; la lista de una cinta como la lista de temas de una carátula, numerada en orden.
- Fuentes libres incluidas en la entrega (Atkinson Hyperlegible Next y Px437 IBM VGA 8x16), porque el equipo puede no tener internet. Logos de la Universidad de Zaragoza y de la EINA en el pie.
- Cada consulta muestra solo las columnas que da la aplicación. Cabecera: "Programas de ZX Spectrum", base de datos GW-BASIC en MS-DOS. Puerto 8080 cambiable al arrancar, con un mensaje claro si está ocupado y las direcciones para entrar desde otro equipo.

**Por qué.** Enseñar la aplicación antigua y la web moderna una al lado de la otra es lo que hace la práctica, y las cintas son el objeto real de los datos.

**Descartado.** Toda la web oscura con la fuente de DOS (se lee peor). Los colores y franjas del Spectrum (estridente y es la imagen de marca de Sinclair).
