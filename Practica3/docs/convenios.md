# Convenios de la práctica 3 (grupo 02)

Cómo está organizado el código y cómo se trabaja con él. Las decisiones de fondo están en `DECISIONES.md` (D1-D9) y los requisitos en `docs/requisitos.md`.

## Estructura (D1)

```
SL-P3-02/
├─ src/
│  ├─ Main.java          arranque: reserva el puerto, lanza DOSBox, publica la web, cierre ordenado
│  ├─ legado/            el wrapper: hablar con la aplicación legada
│  ├─ servidor/          el servidor web (HTTP) y la API
│  └─ pruebas/           pruebas automáticas y herramientas de medición
├─ frontend/             la página web (HTML, JS, SCSS → CSS, fuentes, logos)
├─ dosbox/p3-dosbox.conf nuestra configuración de DOSBox (fuera de Database/)
├─ Database-MSDOS/       la aplicación legada, intacta
└─ lib/                  JNA (ventanas y teclado de Windows) y Tess4J (solo para medir el OCR)
```

- `legado/` no sabe nada de HTTP y `servidor/` no sabe nada de DOSBox: se comunican por la interfaz `legado.AplicacionLegada`.
- Las filas, columnas y textos de las pantallas de la aplicación están solo en `legado.PantallaLegado`, contados como en `docs/mapa-pantallas.md` (fila 1-25, columna 1-80) y con la línea de `DATABASE.BAS` de la que salen.

## La aplicación legada (R5, E9)

- `SL-P3-02/Database-MSDOS/` es la extracción exacta de `Database-MSDOS.rar` y **no se edita nunca**.
- Git la guarda byte a byte (`Practica3/SL-P3-02/Database-MSDOS/** -text` en `.gitattributes`).
- `pruebas.PruebasOffline` comprueba los SHA-256 de `Database/` y `database.bat` frente al original: si algo la cambia, la prueba falla.
- El wrapper nunca lee `DATOS.DAT` ni `INFORM.DAT` (R8). Solo las pruebas lo leen, para saber qué debería verse en pantalla.

## Comentarios (E4)

Como en `Teclado`, `VentanaDosBox` y `LectorPantalla`:

- Cada clase empieza con un comentario que explica qué hace, por qué así y los conceptos que no son obvios (JNA, Robot, HttpServer…).
- Cada método público tiene Javadoc. `@param`, `@return` y `@throws` se ponen cuando aportan algo.
- Los comentarios dentro del código explican el porqué, no repiten la línea.
- Se cita la fuente cuando un dato sale de la aplicación o del guion: `mapa-pantallas.md`, `L7060`, `R20`, `D2`.
- Se dicen los casos raros y los límites.
- En español, con frases completas.

Comprobación: `javadoc -Xdoclint:all -Xmaxwarns 2000` sobre `src/` sin errores ni avisos de "no comment", "no main description" o "default constructor".

## Nombres

- Clases, métodos y variables en español, sin tildes en los identificadores (`ServicioLegado`, `buscarPorNombre`, `filasUltimaPagina`).
- Constantes en mayúsculas con nombre que diga qué son (`ESPERA_PANTALLA`, `LISTADO_PRIMERA_FILA`), nunca números sueltos.
- En la web se mantienen los `id` de los elementos (`total-registros`, `form-nombre`…): `app.js` depende de ellos.

## Compilar, ejecutar y probar

- Compilar: `build.sh` (WSL/Linux) o `build.bat` (Windows). Dejan las clases y `legado/fuente-dosbox.txt` en `out/`.
- Ejecutar (Windows, con el escritorio visible): `java --enable-native-access=ALL-UNNAMED -Dsun.java2d.uiScale=1 -cp "out;lib/*" Main [PUERTO]`. El lanzador de doble clic de la entrega se describe en el LEEME.
- Pruebas sin DOSBox, en cualquier equipo: `java -cp "out:lib/*" pruebas.PruebasOffline` (en Windows, `out;lib/*`). Acaban en `TODO BIEN` o en el número de fallos. Usan las 68 capturas reales de `pruebas/conjunto/`.
- Lectura por plantillas: `pruebas.VerificarPlantillas ../pruebas/conjunto Database-MSDOS/Database/DATOS.DAT` (68/68 pantallas al 100 %).
- Prueba real de teclado (Windows, toma el teclado): `pruebas.PruebaTeclado`.
- Toda corrección lleva su prueba en `PruebasOffline`: primero la prueba (que debe fallar), luego el arreglo.

## Estilos de la web (E8)

- Se editan los `.scss` de `frontend/styles/` (`_variables`, `_components`, `main`). El navegador solo lee `main.css`, que se genera con Sass:

  ```
  npm install -g sass          (una vez)
  cd SL-P3-02/frontend
  sass --no-source-map styles/main.scss styles/main.css
  ```

- `main.css` se sube generado: para ejecutar la web no hace falta Sass.
- Las fuentes van en `frontend/fonts/` con sus licencias (SIL OFL 1.1 y CC BY-SA 4.0), porque el equipo puede no tener internet.

## IntelliJ (E7)

- `.idea/` y `SL-P3-02.iml` están en el repositorio para abrir el proyecto ya configurado (File → Open → `SL-P3-02`). No van en el zip de la entrega.
- Si pide JDK, elegir uno de la versión 25 o superior.
- Si se añade un `.jar` a `lib/`, añadirlo también a `.idea/libraries/lib.xml`.

## Commits (E10)

- Formato: `P3 <apartado del guion>: <qué>`, por ejemplo `P3 Objetivo: cola de operaciones, varios hilos y cierre de DOSBox`.
- Un commit por cambio con sentido propio, con las pruebas pasando.
- Rama `main`; `git pull --rebase` antes de empezar y antes de subir.

## Autoría

Quien crea un fichero, una clase o un conjunto de pruebas consta como su autor. Las revisiones y correcciones posteriores se registran como "revisión y corrección", sin cambiar la autoría original. El detalle de cada corrección se lleva fuera del repositorio.
