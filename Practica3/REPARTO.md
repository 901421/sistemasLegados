# Reparto y cronograma — Práctica 3 (grupo 02)

Quién hizo cada tarea, cuándo y cuánto tiempo llevó (R26, R27). Las fechas salen de los commits del repositorio.

> Tiempo invertido en cada tarea (R27), fijado por el grupo el 16 de noviembre de 2026.

## Cronograma

| Fecha | Tarea | Apartado del guion | Quién | Tiempo (h) | Evidencia |
|---|---|---|---|---|---|
| 2026-10-19 | Lectura del guion, requisitos, ambigüedades y plan | Todos | Imad, Paula y Ainhoa | 2 cada uno | `docs/requisitos.md`, `docs/plan.md` |
| 2026-10-19 | La aplicación legada por dentro: mapa de pantallas, configuración propia de DOSBox y comprobación de que no cambia | 1. Descripción de sistema legado | Imad y Paula | 3 cada uno | `docs/mapa-pantallas.md`, `dosbox/p3-dosbox.conf` |
| 2026-10-19 | Entrada con Robot y JNA: teclado español, foco de la ventana y prueba real | 3. Generación de la entrada | Imad | 3 | `legado/Teclado`, `legado/VentanaDosBox`, `pruebas/PruebaTeclado` |
| 2026-10-19 | Captura y OCR: Tess4J con 14 configuraciones, entrenamiento de Tesseract y lector por plantillas | 4. Captura de la salida | Imad | 6 | `docs/ocr-resultados.md`, `legado/LectorPantalla` |
| 2026-11-01 | Servidor web y API (registros, nombre, cinta), operaciones F1-F3 sobre la aplicación y modelo de datos | 2. Objetivo (funcionalidades) | Paula | 8 | `servidor/`, `legado/ServicioLegado`, `legado/ParserPantalla`, `legado/Juego` |
| 2026-11-10 | Página web (estructura, estilos SCSS, búsquedas), servidor de ficheros, compilación y arranque en Windows, correcciones del listado y de la validación de cintas | 2. Objetivo (funcionalidades) | Ainhoa | 9 | `frontend/`, `servidor/FicherosEstaticos`, `build.bat`, `lanzar.bat` |
| 2026-11-14 y 2026-11-15 | Revisión y corrección del servidor y la web (reorganización, esperas por pantalla, validación, cola de consultas, cierre de DOSBox, pantalla en directo), pruebas automáticas sin DOSBox y prueba final en Windows | 2. Objetivo (funcionalidades) | Imad | 7 | `DECISIONES.md` D1-D9, `pruebas/PruebasOffline`, `docs/requisitos.md` R6-R14 |
| 2026-11-15 | Lanzador de doble clic con Java reducido, LEEME, montaje de la entrega, memoria y su revisión | 5. Documentación | Imad; Paula; Ainhoa | 4; 2; 2 | `lanzar.bat`, `LEEME.txt`, `entrega.sh`, `docs/memoria-SL-P3-02.pdf` |

## Totales

| Persona | Tareas principales | Tiempo (h) |
|---|---|---|
| Imad | Análisis del guion, mapa de pantallas, entrada, captura y OCR, revisión y pruebas del código, prueba final, entrega y memoria | 25 |
| Paula | Análisis del guion, mapa de pantallas, servidor web, API y operaciones F1-F3, entrega y memoria | 15 |
| Ainhoa | Análisis del guion, página web, servidor de ficheros y arranque en Windows, entrega y memoria | 13 |
| **Total** | | **53** |
