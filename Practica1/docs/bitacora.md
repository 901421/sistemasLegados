# Bitácora — Práctica 1 (Sistemas Legados)

Sirve para la memoria: reparto de tareas (quién hizo qué) y cronograma. Una fila por trabajo terminado.
Formato: fecha · persona · tarea (ID de `TAREAS.md`) · qué se hizo · problemas encontrados y cómo se resolvieron.

| Fecha | Persona | Tarea | Qué se hizo | Problemas y soluciones |
|---|---|---|---|---|
| 2026-09-21 | Imad | Análisis | Lectura del guion, inventario del zip y extracción de 43 requisitos con cita; 16 ambigüedades detectadas | Faltan `BANK8` (lo llama `BANK1.cbl:209`) y `movimientos.ubd`; los pantallazos del guion son de un terminal Linux |
| 2026-09-21 | Imad | Spike de compilación | Prueba de `cobc` sobre los 7 `BANK` originales: 251 errores, de los que 243 son la sintaxis `DISPLAY (l c)` (275 usos) | Ningún dialecto de `cobc` acepta el código original; una reescritura mecánica a `LINE/COL` deja 6 de 7 programas sin errores |
| 2026-09-21 | Imad | Elección de compilador | Inventario completo de escobol.com en archive.org (114 descargas, 106 ficheros) y pruebas en contenedores Docker con OpenCOBOL 1.1 y GnuCOBOL 2.2, 3.1.2, 3.2.0 y 4.0 | [2] solo ofrece 3 toolchains, DOS o sin ejecutar; se elige GnuCOBOL 3.2 con Berkeley DB (D12) |
| 2026-09-21 | Imad | Decisiones | 13 decisiones de diseño anotadas en `DECISIONES.md` (bloqueo, transferencias, listado, ingreso por billetes, compilador, ficheros) | Pendientes de consultar con el profesor: compilador, alcance del listado, transferencia sin saldo |
