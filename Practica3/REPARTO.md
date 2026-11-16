# Reparto y cronograma — Práctica 3 (grupo 02)

Quién hizo cada tarea, cuándo y cuánto tiempo llevó (R26, R27). Las fechas van por intervalos fijados por el grupo; cada intervalo termina en el commit del repositorio que cierra esa tarea.

> Tiempo invertido en cada tarea (R27), fijado por el grupo el 16 de noviembre de 2026.

## Cronograma

| Fechas | Tarea | Apartado del guion | Quién | Tiempo (h) | Evidencia |
|---|---|---|---|---|---|
| 2026-10-19 | Lectura del guion, requisitos, ambigüedades y plan | Todos | Imad, Paula y Ainhoa | 2 cada uno | `docs/requisitos.md`, `docs/plan.md` |
| 2026-10-19 | La aplicación legada por dentro: mapa de pantallas, configuración propia de DOSBox y comprobación de que no cambia | 1. Descripción de sistema legado | Imad y Paula | 3 cada uno | `docs/mapa-pantallas.md`, `dosbox/p3-dosbox.conf` |
| 2026-10-19 | Entrada con Robot y JNA (teclado español, foco de la ventana, prueba real); captura y OCR (Tess4J con 14 configuraciones, entrenamiento de Tesseract y lector por plantillas) | 3. Generación de la entrada; 4. Captura de la salida | Imad | 9 | `legado/Teclado`, `legado/VentanaDosBox`, `pruebas/PruebaTeclado`, `docs/ocr-resultados.md`, `legado/LectorPantalla` |
| 2026-10-20 a 2026-11-01 | Estudio de Java para el wrapper (Robot, JNA, HttpServer, hilos) | 2, 3 y 4 | Imad | 3 | — |
| 2026-10-20 a 2026-11-01 | Servidor web y API (registros, nombre, cinta), operaciones F1-F3 sobre la aplicación y modelo de datos, con el estudio de HttpServer e hilos | 2. Objetivo (funcionalidades) | Paula | 11 | `servidor/`, `legado/ServicioLegado`, `legado/ParserPantalla`, `legado/Juego` |
| 2026-11-02 a 2026-11-10 | Página web (estructura, estilos SCSS, búsquedas), servidor de ficheros, compilación y arranque en Windows, correcciones del listado y de la validación de cintas, con el estudio de SCSS y fetch | 2. Objetivo (funcionalidades) | Ainhoa | 12 | `frontend/`, `servidor/FicherosEstaticos`, `build.bat`, `lanzar.bat` |
| 2026-11-11 a 2026-11-14 | Pruebas del servidor y de la web en varios navegadores y en el móvil | 2. Objetivo (funcionalidades) | Paula y Ainhoa | 2 cada una | — |
| 2026-11-12 a 2026-11-15 | Revisión y corrección del servidor y la web (reorganización, esperas por pantalla, validación, cola de consultas, cierre de DOSBox, pantalla en directo), pruebas automáticas sin DOSBox y prueba final en Windows | 2. Objetivo (funcionalidades) | Imad | 9 | `DECISIONES.md` D1-D9, `pruebas/PruebasOffline`, `docs/requisitos.md` R6-R14 |
| 2026-11-15 y 2026-11-16 | Lanzador de doble clic con Java reducido, LEEME, montaje de la entrega, memoria y su revisión | 5. Documentación | Imad; Paula; Ainhoa | 4; 2; 2 | `lanzar.bat`, `LEEME.txt`, `entrega.sh`, `docs/memoria-SL-P3-02.pdf` |

## Totales

| Persona | Tareas principales | Tiempo (h) |
|---|---|---|
| Imad | Análisis del guion, mapa de pantallas, entrada, captura y OCR, estudio de Java, revisión y pruebas del código, prueba final, entrega y memoria | 30 |
| Paula | Análisis del guion, mapa de pantallas, servidor web, API y operaciones F1-F3, pruebas, entrega y memoria | 20 |
| Ainhoa | Análisis del guion, página web, servidor de ficheros y arranque en Windows, pruebas, entrega y memoria | 18 |
| **Total** | | **68** |
