# Plan — Sistemas Legados, Práctica 3 (2026-27)

Calcado al guion: mismos apartados, mismo orden, mismos nombres. Requisitos en `requisitos.md`.

## 0. Objetivo (cabecera)

- **Qué pide:** una página web que acceda a la aplicación BASIC/MS-DOS mediante un wrapper, entregada como `SL-P3-nn.zip/rar` antes del lunes 16-nov-2026.
- **Requisitos:** R1, R2, R3.
- **Tareas:** montar `SL-P3-02.zip` desde un commit (`git archive` + runtime jlink + DOSBox + app intacta + memoria); probarlo en una carpeta limpia de Windows; subirlo a Drive y enviar el correo.
- **Verificación:** el zip descomprimido en limpio arranca con doble clic y responde a F1-F3 desde otro equipo; el correo está enviado antes de la fecha.
- **Estado:** Pendiente.

## 1. Descripción de sistema legado

- **Qué pide:** la app legada (BASIC, DOSBox incluido, `database.bat`) se usa sin cambiar nada de `Database/`; hay que encapsular su uso.
- **Requisitos:** R4, R5, R6.
- **Tareas:**
  - Extraer el `.rar` en una carpeta de trabajo y lanzar `database.bat` en Windows (con OK).
  - Mapa de pantallas (`docs/mapa-pantallas.md`): menú y opciones 3, 4, 6 y 7 con posiciones de fila y columna, textos de espera y teclas, a partir del código destokenizado y capturas reales.
  - `.conf` propio de DOSBox fuera de `Database/` (ventana 640x400 sin escalar, cycles) y lanzador que lo usa.
  - Manifiesto SHA-256 de `Database/` y comprobación de que sigue igual tras usar la app.
- **Verificación:** capturas reales de cada pantalla del mapa; el manifiesto coincide antes y después de una sesión de pruebas.
- **Estado:** Hecho (commit `286b311`).

## 2. Objetivo (funcionalidades)

- **Qué pide:** página web accesible desde un equipo remoto con F1 (número de registros siempre visible), F2 (datos por nombre) y F3 (programas de una cinta en orden de grabación), usando cada vez el wrapper sobre la app; las interacciones múltiples son transparentes.
- **Requisitos:** R7-R14, R30.
- **Tareas:**
  - Servidor `HttpServer` en todas las interfaces: página HTML/JS y endpoints `/api/registros`, `/api/nombre?q=`, `/api/cinta?id=`.
  - Wrapper: operaciones F1 (opción 4), F2 (opción 7 con N hasta agotar coincidencias), F3 (opción 3→4 y opción 6 con todas las pantallas, filtrado por partes de la cinta); vuelta al MENÚ y comprobación de estado.
  - Cola de peticiones (una a la vez) y aviso de ocupado; validación de la entrada; mensajes de "no encontrado" y de error.
- **Verificación:** pruebas reales en Windows desde el navegador local y desde otro equipo o móvil; casos: nombre exacto, subcadena, duplicado (DEFENDER), sin resultados, minúsculas; cintas A, 1 (no coge 10), 29 (incluye E-29), B-E, inexistente (45); comparación con la salida directa de la app.
- **Estado:** Hecho (2026-11-15). Backend y web revisados, corregidos y probados; decisiones D1-D9. `PruebasOffline`: 137 casos TODO BIEN sin DOSBox. Prueba final real con Imad: F1-F3, foco, Bloq Mayús, móvil, dos clientes a la vez, DOSBox congelado, cierre con la X, Ctrl+C y Administrador de tareas, doble arranque; R5 7/7. R6-R14, R20 y R30 cumplidos.

## 3. Generación de la entrada de la aplicación legada

- **Qué pide:** el wrapper genera la entrada con eventos de teclado de bajo nivel (`java.awt.Robot` o similar).
- **Requisitos:** R15 (y R13).
- **Tareas:** clase de teclado con Robot; tabla de caracteres → teclas para la distribución española de DOSBox (letras, dígitos, espacio, `'`, `-`, `.`, `&`, `!`…); foco en la ventana de DOSBox (JNA); pausas mínimas entre teclas.
- **Verificación:** prueba real que escribe cada carácter del conjunto permitido en un INPUT de la app y lo lee en pantalla.
- **Estado:** Hecho (2026-10-19, prueba real `TODO BIEN`). Traer DOSBox al frente con el navegador en el mismo PC y devolverle el foco: comprobado en la prueba final del apartado 2 (2026-11-15).

## 4. Captura de la salida de la aplicación legada

- **Qué pide:** capturar la pantalla de la app y reconocer el texto (Tess4J con `spa.traineddata` en `tessdata`); si no es 100 % correcto, mejorar el OCR o usar cualquier otro método sin cambiar ni dejar de usar la app, buscando el resultado más parecido a la ejecución directa.
- **Requisitos:** R16-R20 (y R24 para la memoria).
- **Tareas:**
  - Captura del área de cliente de DOSBox con Robot (`createScreenCapture`), localizada con JNA.
  - Tess4J + `spa.traineddata` (formato 3.x: comprobar con qué versión de Tess4J/Tesseract carga). Medir la tasa de aciertos por carácter y por línea sobre pantallas conocidas, con varias configuraciones (escala, binarización, PSM, OEM, lista blanca).
  - Si no llega al 100 %: reconocimiento exacto por plantillas de la fuente 8x16 de DOSBox (celdas de 80x25), con plantillas sacadas de capturas reales. Documentar ambos.
  - Analizador de pantallas (texto 80x25 → datos) con tests offline sobre capturas guardadas.
- **Verificación:** tabla de resultados del OCR por configuración; 100 % de aciertos del método final sobre todas las pantallas de prueba (tests offline y prueba real).
- **Estado:** Hecho (2026-11-15): OCR medido y entrenado (máx. 39 % de registros), método final por plantillas con 100 % en 68 pantallas. Tess4J queda solo en `pruebas.MedirOcr` (medición para la memoria, R24); no se integra en el wrapper. El 100 % de punta a punta se verifica en la prueba real del apartado 2.

## 5. Documentación

- **Qué pide:** memoria de 4-5 páginas con pasos (también los fallidos), dificultades, resultados del OCR, capturas o vídeo, reparto, horas reales, otra información y cómo ejecutar en Windows con doble clic.
- **Requisitos:** R21-R29.
- **Tareas:** `REPARTO.md` con horas reales; `lanzar.bat` (doble clic) y `LEEME`; capturas y un vídeo corto de demostración; memoria (docx → PDF) con portada y logos de `../img/`.
- **Verificación:** la memoria cubre cada punto de R21-R29 (lista de comprobación) y tiene 4-5 páginas; el doble clic funciona en una carpeta limpia.
- **Estado:** Pendiente.

## 6. Referencias

- **Qué pide:** nada que implementar ([6] Tesseract-ocr, [7] Tess4J). Se citan en la memoria.
- **Estado:** —
