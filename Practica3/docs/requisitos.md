# Requisitos — Sistemas Legados, Práctica 3 (2026-27)

Fuente: `Practica_3_2026_2027.pdf` (2 páginas). Análisis hecho sobre `docs/fuente/Practica_3_2026_2027.md`, revisado contra el PDF.
SHA-256 del guion: `2eafc25d52721c0f08b476c26b28dca7309ec619f9a7e7fa0a05263da20cdff2`.

## 1. Estructura del guion (orden y nombres exactos)

0. *Objetivo* (cabecera: objetivo general, fecha límite y fichero)
1. *Descripción de sistema legado*
2. *Objetivo* (funcionalidades 1, 2 y 3 de la página web y papel del wrapper)
3. *Generación de la entrada de la aplicación legada*
4. *Captura de la salida de la aplicación legada*
5. *Documentación*
6. *Referencias* ([6] Tesseract-ocr, [7] Tess4J)

El guion tiene dos apartados titulados "Objetivo": se distinguen como "Objetivo (cabecera)" y "Objetivo (funcionalidades)".

## 2. Requisitos

| ID | Apartado | Requisito | Cita | Ubicación | Tipo | Estado | Evidencia |
|---|---|---|---|---|---|---|---|
| R1 | Objetivo (cabecera) | Implementar una página web que acceda a la aplicación legada BASIC/MS-DOS con ayuda de un wrapper que genere la entrada y analice la salida | "Implementar una página web que, con la ayuda de un wrapper que genere la entrada y analice la salida, acceda a una aplicación legada BASIC/MS-DOS con interfaz de texto." | p. 1, Objetivo, viñeta 1 | Explícito | Pendiente | |
| R2 | Objetivo (cabecera) | Entregar como muy tarde el lunes 16-nov-2026 | "Fecha límite: lunes 16 noviembre 2026." | p. 1, Objetivo, viñeta 2 | Explícito | Pendiente | |
| R3 | Objetivo (cabecera) | El fichero entregado se llama `SL-P3-nn.zip` o `.rar`, con nn = número de grupo | "Fichero a enviar: SL-P3-nn.zip/rar (nn: número de grupo)" | p. 1, Objetivo, viñeta 2 | Explícito (formato y medio: AMB-1, AMB-2) | Pendiente | |
| R4 | Descripción | La aplicación se lanza con `database.bat` usando el DOSBox incluido en el `.rar` | "Descomprime Database-MSDOS.rar y database.bat lanzará la aplicación usando el emulador de MS-DOS DOSBox (incluido)" | p. 1, Descripción | Ambiguo (AMB-7) | Cumplido | `database.bat` lanzado en Windows el 2026-10-19 (uso manual y recorrido de 20 capturas); lanzamiento con el mismo DOSBox 0.74 y `.conf` propio fuera de `Database/` (AMB-7): `mapa-pantallas.md`, secciones "Arranque" y "Configuración propia" |
| R5 | Descripción | No se cambia nada de la carpeta `Database` (programa ni ficheros de datos) | "la aplicación legada está en la carpeta Database y no debe cambiarse nada de lo que contiene" | p. 1, Descripción | Explícito | Cumplido hasta ahora | SHA-256 de los 6 ficheros de `Database/` iguales al `.rar` tras el uso manual, el recorrido y el segundo lanzamiento: `mapa-pantallas.md`, "Comprobación…". Se revalida tras cada sesión real y en la entrega |
| R6 | Descripción | Se encapsula el uso de la aplicación, no se reimplementa | "hay que encapsular su uso" | p. 1, Descripción | Explícito | Pendiente | |
| R7 | Objetivo (funcionalidades) | La página web permite acceder a los datos desde un ordenador remoto a través de un navegador | "Crear una página web que permita acceder a los datos manejados por dicha aplicación desde un ordenador remoto a través de un navegador web." | p. 1, Objetivo | Explícito (alcance de "remoto": AMB-10) | Pendiente | |
| R8 | Objetivo (funcionalidades) | Cada funcionalidad se ejecuta accediendo cada vez a la aplicación legada (sin caché ni lectura directa de los `.DAT`) | "accediendo cada vez a la aplicación legada para ejecutarlas" | p. 1, Objetivo | Explícito | Pendiente | |
| R9 | Objetivo (funcionalidades) | F1: visualizar en todo momento el número de registros del sistema legado | "1. Visualizar en todo momento el número de registros del sistema legado." | p. 1, Objetivo, punto 1 | Ambiguo (AMB-3) | Pendiente | |
| R10 | Objetivo (funcionalidades) | F2: dado el nombre de un programa, listar sus datos | "2. Dado el nombre de un programa, listar sus datos." | p. 1, Objetivo, punto 2 | Ambiguo (AMB-4) | Pendiente | |
| R11 | Objetivo (funcionalidades) | F3: dado un identificador de cinta (A-Z, 0-n), listar sus programas en el orden en que están grabados | "3. Dado el identificador de una cinta (A-Z, 0-n), listar los programas que contiene en el orden en que se encuentran grabados." | p. 1, Objetivo, punto 3 | Ambiguo (AMB-5) | Pendiente | |
| R12 | Objetivo (funcionalidades) | Un programa en varias cintas (campo `A-45`) aparece en el listado de cada una de ellas | "NOTA: un programa puede estar en más de una cinta (ej., para un programa que esté en la cinta A y en la 45 cinta="A-45")." | p. 1, Objetivo, punto 3 | Implícito | Pendiente | |
| R13 | Objetivo (funcionalidades) | La página usa un wrapper que genera la entrada de la operación pedida y captura y analiza la salida en pantalla para obtener los datos | "recurrirá a los servicios de un wrapper que encapsulará la aplicación legada, generando la entrada correspondiente a la operación solicitada por el usuario desde el navegador, y capturando y analizando la salida en pantalla" | p. 1, Objetivo | Explícito | Pendiente | |
| R14 | Objetivo (funcionalidades) | Si una operación necesita varias interacciones con la aplicación, se hacen de forma transparente para el usuario | "Posiblemente alguna operación individual a través de la página web requerirá, de forma transparente para el usuario, de varias interacciones con la aplicación legada." | p. 1, Objetivo | Implícito | Pendiente | |
| R15 | Generación de la entrada | La entrada se genera con eventos de teclado de bajo nivel, como `java.awt.Robot` o algo parecido | "podemos usar algo parecido a la clase Robot de Java (java.awt.Robot) que permite generar eventos de bajo nivel de teclado y ratón" | p. 2, Generación de la entrada | Ambiguo (sugerencia, no obligación: AMB-6) | Pendiente | |
| R16 | Captura de la salida | La salida se obtiene capturando la imagen de la pantalla de la aplicación y pasándole OCR (Tesseract / Tess4J) | "una solución para que el wrapper pueda acceder a la salida de la aplicación legada es capturar gráficamente la imagen de la pantalla y hacer un reconocimiento óptico de caracteres (OCR)" | p. 2, Captura de la salida | Ambiguo ("una solución": AMB-8) | Pendiente | |
| R17 | Captura de la salida | Si se usa Tess4J, `spa.traineddata` se copia en su carpeta `tessdata` y se invoca con idioma `spa` | "Este fichero debe ser copiado en la carpeta tessdata de la aplicación Tess4J." / `ocr.setLanguage("spa");` | p. 2, Captura de la salida | Explícito | Pendiente | |
| R18 | Captura de la salida | Si el OCR no es 100 % correcto, se mejora entrenándolo mejor o con otro OCR | "Para mejorar dicho comportamiento podemos bien entrenar mejor al OCR (ver documentación [6]), o bien utilizar cualquier otro OCR disponible en la Web." | p. 2, Captura de la salida | Explícito | Pendiente | |
| R19 | Captura de la salida | Si aun así no es 100 %, vale cualquier otro método de captura, salvo cambiar la aplicación BASIC o dejar de usarla íntegramente | "puede utilizarse cualquier otro método o software (todo vale excepto cambiar la aplicación legada en BASIC o dejar de usarla íntegramente)" | p. 2, Captura, NOTA subrayada | Explícito | Pendiente | |
| R20 | Captura de la salida | El resultado de la encapsulación debe ser lo más parecido posible a ejecutar directamente la aplicación | "que el resultado de su encapsulación sea lo más parecido posible a la ejecución directa de dicha aplicación" | p. 2, Captura, NOTA | Explícito | Pendiente | |
| R21 | Documentación | Memoria de 4-5 páginas aprox. con los pasos realizados para la encapsulación | "Redactar 4-5 páginas aprox. detallando los pasos realizados para hacer la encapsulación de la aplicación legada" | p. 2, Documentación | Explícito | Pendiente | |
| R22 | Documentación | Incluir los pasos que no han servido para llegar a la solución final | "incluyendo aquellos que no han servido para llegar a la solución final" (subrayado) | p. 2, Documentación | Explícito | Pendiente | |
| R23 | Documentación | Incluir las principales dificultades encontradas | "las principales dificultades encontradas" (subrayado) | p. 2, Documentación | Explícito | Pendiente | |
| R24 | Documentación | Incluir el resultado de usar las distintas técnicas o configuraciones del OCR (obliga a probar el OCR de verdad, aunque al final se use otro método) | "el resultado al utilizar las distintas técnicas o configuraciones del OCR" | p. 2, Documentación | Explícito (la obligación de probarlo es implícita) | Pendiente | |
| R25 | Documentación | Incluir pantallazos o vídeo del funcionamiento de la aplicación encapsulada | "pantallazos/video que muestre el funcionamiento de la aplicación encapsulada" | p. 2, Documentación | Explícito | Pendiente | |
| R26 | Documentación | Incluir el reparto de tareas entre los miembros del grupo | "el reparto de tareas entre los miembros del grupo" | p. 2, Documentación | Explícito | Pendiente | |
| R27 | Documentación | Incluir el tiempo real invertido en cada tarea | "el tiempo real invertido en cada tarea" | p. 2, Documentación | Explícito | Pendiente | |
| R28 | Documentación | Incluir cualquier otra información de interés | "y cualquier otra información que se considere de interés" | p. 2, Documentación | Explícito | Pendiente | |
| R29 | Documentación | La práctica se ejecuta desde Windows con doble clic en un fichero, y la memoria explica todo lo necesario para ello | "incluyendo todo lo necesario para poder ejecutar las practicas desde Windows haciendo doble click en un fichero" | p. 2, Documentación | Explícito | Pendiente | |
| R30 | (global) | El wrapper y el servidor web corren en la máquina Windows donde está abierta la ventana de DOSBox (la captura y el teclado simulado necesitan escritorio); el navegador puede estar en otra máquina | Derivado de R1, R7, R15 y R16 | — | Implícito | Pendiente | |

## 3. Restricciones (lo prohibido o limitado)

- **No modificar nada de la carpeta `Database`** (R5): ni `DATABASE.BAS` ni `DATOS.DAT` ni `INFORM.DAT`. Consecuencia: el wrapper **no puede usar la opción 2 (GRABAR DATOS)** ni dejar datos introducidos con la opción 1 y grabarlos; tampoco "ALTERAR SUS DATOS" en la opción 7 si luego se graba.
- **Prohibido cambiar la aplicación BASIC o dejar de usarla íntegramente** (R19). Leer `DATOS.DAT` directamente desde el wrapper equivale a dejar de usarla: prohibido (R8).
- **Cada operación de la web accede a la aplicación legada** (R8): sin caché de datos en el wrapper.
- Se ejecuta en **Windows con doble clic** (R29).
- Memoria de **4-5 páginas aprox.** (R21).
- La entrada se da **por la interfaz de texto de la aplicación** (teclado), con Robot o algo parecido (R15).

## 4. Entregables y fechas

- **Qué:** `SL-P3-nn.zip` o `.rar` (nn = número de grupo) con la página web, el wrapper, la aplicación legada intacta, el lanzador de doble clic y la memoria (R3, R21-R29).
- **Cuándo:** lunes **16-nov-2026** (R2). La hora no se indica (AMB-2).
- **Dónde / cómo:** el guion no lo dice (AMB-2).

## 5. Ambigüedades

| ID | Cita | Lecturas posibles | Estado |
|---|---|---|---|
| AMB-1 | "SL-P3-nn.zip/rar" | zip o rar; nn = 02 si se mantiene el grupo de P1 y P2 | Resuelta: `SL-P3-02.zip` (grupo 02) |
| AMB-2 | "Fichero a enviar" | Medio (email como P1 y P2, Moodle) y hora límite no dichos | Resuelta (provisional): email con enlace de Drive como en la P2; hora 23:59 supuesta |
| AMB-3 | "Visualizar en todo momento el número de registros" | (a) siempre visible en la página y refrescado al cargar y tras cada operación (opción 4 de la app); (b) refresco periódico; (c) solo bajo demanda | Resuelta: cifra fija en la cabecera, pedida a la app (opción 4) al cargar y tras cada operación, más botón de refrescar |
| AMB-4 | "Dado el nombre de un programa, listar sus datos" | (a) igual que la opción 7 de la app: búsqueda por subcadena y todas las coincidencias ("ES ESTE?" → N hasta acabar); (b) solo nombre exacto; (c) solo la primera coincidencia. Hay 5 nombres duplicados (DEFENDER, GALLIPOLI…) y 146 pares nombre-contenido-en-otro | Resuelta: como la opción 7, todas las coincidencias por subcadena (N a "ES ESTE?" hasta agotar) |
| AMB-5 | "listar los programas que contiene en el orden en que se encuentran grabados" | La app no guarda la posición en la cinta. (a) orden de registro (= orden "ANTIGUEDAD", columna REGISTRO); (b) orden de la opción 6 tras ordenar por CINTA (quicksort inestable: no conserva el orden). Además: cómo casar "1" sin coger "10" ni "13-26", y "A" en "A-45" | Resuelta: orden de registro; ordenar por ANTIGUEDAD, listar las 763 entradas (opción 6) y filtrar por cada parte del campo cinta |
| AMB-6 | "podemos usar algo parecido a la clase Robot de Java" | Lenguaje del wrapper libre o Java; servidor web libre | Resuelta: Java + `com.sun.net.httpserver` + Robot + Tess4J |
| AMB-7 | "database.bat lanzará la aplicación usando el emulador de MS-DOS DOSBox (incluido)" | (a) usar el DOSBox 0.74 incluido; (b) se permite otro emulador/configuración (DOSBox-X…) si ayuda a capturar | Resuelta: DOSBox 0.74 incluido, con `.conf` propio fuera de `Database/` (ventana fija 640x400, cycles alto) |
| AMB-8 | "una solución … es capturar gráficamente la imagen de la pantalla y hacer un reconocimiento óptico de caracteres (OCR)" / "puede utilizarse cualquier otro método" | Método final: Tesseract entrenado, otro OCR, comparación exacta de la fuente de 8x16 píxeles de DOSBox (reconocimiento por plantillas), lectura de la memoria de vídeo… Siempre usando la app | Resuelta: primero Tess4J medido con varias configuraciones; si no llega al 100 %, plantillas de la fuente 8x16. Se documentan ambos |
| AMB-9 | "pantallazos/video" | Capturas dentro de la memoria, vídeo aparte, o ambas | Resuelta: pantallazos en la memoria y vídeo corto en el zip |
| AMB-10 | "desde un ordenador remoto" | (a) servidor escuchando en la red (accesible desde otro PC); (b) basta con `localhost` en la demo | Resuelta: servidor en todas las interfaces, probado desde otro equipo o móvil |
| AMB-11 | (no lo menciona) | Varios navegadores a la vez sobre una única instancia de la app: serializar peticiones | Resuelta: una instancia, peticiones en cola con aviso de ocupado |
| AMB-12 | (no lo menciona) | `SL_wrapper.avi`, distribuido con el material: ¿demo del profesor del resultado esperado? | Resuelta: es la explicación de otro alumno; no se usa por ahora |

## 6. Datos de la aplicación legada (lectura, sin modificar nada)

Obtenido del `.rar` extraído en un directorio temporal y de `DATABASE.BAS` destokenizado para leerlo:

- GW-BASIC en DOSBox 0.74 con teclado español (`stdout.txt`). 763 registros (`INFORM.DAT`: `763,"ANTIGUEDAD",1`).
- Menú 1-8 con `INKEY$` (una tecla, sin Enter). Opción 4: "CONTIENE 763 ARCHIVOS" (+ "PULSA ENTER"). Opción 7: búsqueda por número o por nombre con `INSTR` (subcadena, sensible a mayúsculas) y bucle "ES ESTE ? (S/N)". Opción 3: ordena en memoria por NOMBRE/TIPO/CINTA/ANTIGUEDAD (con Enter final salvo ANTIGUEDAD). Opción 6: lista 18 filas por pantalla (columnas Nº, NOMBRE, TIPO, CINTA, REGISTRO) desde un dato inicial buscado con `INSTR`, "PULSA SPACE PARA CONTINUAR".
- Cintas: 104 valores; A-Z, 0-33 y combinadas (`B-E`, `0-33`, `M-25`…). No hay ninguna cinta 45.
- Nombres de hasta 30 caracteres, casi todo mayúsculas (salvo `BARRY McGUIGAN'S BOXING`, `FRIDAY 13th`), con apóstrofos.

## 7. Decisiones de la fase 4 que afectan a requisitos

- Entrada desde la web validada (no vacía, sin comas ni comillas, longitud máxima de la app) y enviada tal cual, sin pasar a mayúsculas (`INSTR` distingue mayúsculas, R20).
- Tabla de teclas probada con la distribución española de DOSBox.
- Esperas por contenido de pantalla con tiempo máximo; la app vuelve siempre al MENÚ.
- Pendientes de preguntar al profesor: ninguno.
