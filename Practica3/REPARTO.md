# Reparto y cronograma — Práctica 3 (grupo 02)

Quién hizo cada tarea, cuándo y cuánto tiempo llevó (R26, R27). Las fechas salen de los commits del repositorio.

> **Tiempos estimados, pendientes de confirmar.** El guion pide el tiempo real invertido en cada tarea. Estos valores son una estimación hecha a partir del trabajo de cada día; cada uno corrige los suyos antes de entregar.

## Cronograma

| Fecha | Tarea | Apartado del guion | Quién | Tiempo (h) | Evidencia |
|---|---|---|---|---|---|
| 2026-10-19 | Lectura del guion, requisitos, ambigüedades y plan | Todos | Imad | 2 | `docs/requisitos.md`, `docs/plan.md` |
| 2026-10-19 | La aplicación legada por dentro: mapa de pantallas, configuración propia de DOSBox y comprobación de que no cambia | 1. Descripción de sistema legado | Imad | 3 | `docs/mapa-pantallas.md`, `dosbox/p3-dosbox.conf` |
| 2026-10-19 | Entrada con Robot y JNA: teclado español, foco de la ventana y prueba real | 3. Generación de la entrada | Imad | 3 | `legado/Teclado`, `legado/VentanaDosBox`, `pruebas/PruebaTeclado` |
| 2026-10-19 | Captura y OCR: Tess4J con 14 configuraciones, entrenamiento de Tesseract y lector por plantillas | 4. Captura de la salida | Imad | 6 | `docs/ocr-resultados.md`, `legado/LectorPantalla` |
| 2026-11-01 | Servidor web y API (registros, nombre, cinta), operaciones F1-F3 sobre la aplicación y modelo de datos | 2. Objetivo (funcionalidades) | Paula | 5 | `servidor/`, `legado/ServicioLegado`, `legado/ParserPantalla`, `legado/Juego` |
| 2026-11-10 | Página web (estructura, estilos SCSS, búsquedas), servidor de ficheros, compilación y arranque en Windows, correcciones del listado y de la validación de cintas | 2. Objetivo (funcionalidades) | Ainhoa | 7 | `frontend/`, `servidor/FicherosEstaticos`, `build.bat`, `lanzar.bat` |
| 2026-11-15 | Revisión del código y pruebas automáticas sin DOSBox (68 capturas reales) | 2. Objetivo (funcionalidades) | Imad | 2 | `pruebas/PruebasOffline`, `pruebas/conjunto/` |
| 2026-11-15 | Reorganización en paquetes y corrección: esperas por pantalla, analizador estricto, validación, cola de consultas, cierre de DOSBox | 2. Objetivo (funcionalidades) | Imad | 5 | `DECISIONES.md` D1-D8 |
| 2026-11-15 | Retoques de la web y pantalla de la aplicación en directo | 2. Objetivo (funcionalidades) | Imad | 2 | `DECISIONES.md` D9, `frontend/` |
| 2026-11-15 | Prueba final en Windows (navegador, móvil, dos clientes, cierres) y arreglos que salieron | 2. Objetivo (funcionalidades) | Imad | 2,5 | `docs/requisitos.md` R6-R14, R30 |
| 2026-11-16 | Lanzador de doble clic con Java reducido, LEEME y montaje de la entrega | 5. Documentación | Imad | 1,5 | `lanzar.bat`, `LEEME.txt`, `entrega.sh` |
| 2026-11-16 | Memoria, pantallazos y vídeo | 5. Documentación | Imad | 3 | `docs/memoria-SL-P3-02.pdf` |
| 2026-11-16 | Revisión de la memoria | 5. Documentación | Paula y Ainhoa | 1 cada una | — |

## Totales (estimados)

| Persona | Tareas principales | Tiempo (h) |
|---|---|---|
| Imad | Análisis del guion, aplicación legada, entrada, captura y OCR, revisión y pruebas del código, prueba final, lanzador y memoria | 30 |
| Paula | Servidor web, API y operaciones F1-F3 | 6 |
| Ainhoa | Página web, servidor de ficheros y arranque en Windows | 8 |
| **Total** | | **44** |
