# Decisiones de la práctica (para consultar con el grupo)

Cada decisión lleva: el problema, lo que se decide y por qué es lógico. Fecha de cada entrada entre paréntesis.
Referencias: `Rn` = requisito, `AMB-n` = ambigüedad de `docs/requisitos.md`.

---

## D1 — Credenciales del mainframe: ventana de conexión precargada y clave sin cambiar (2026-09-27)

**Problema.** El guion da una cuenta por grupo (`grupo_XX`) con la clave inicial `secreto6` y añade que "se puede cambiar una vez conectado" (R7, AMB-3). No dice cómo obtiene la GUI esos datos, y el profesor tiene que poder ejecutar el programa sin instrucciones aparte.

**Decisión.**
- Al arrancar, la GUI muestra una **ventana de conexión** con host (`155.210.152.51`), puerto (`3270`), usuario (`grupo_02`) y clave (`secreto6`) **ya rellenados y editables**. Basta con pulsar "Conectar".
- Si el login falla, se muestra el error y se pueden corregir los datos sin cerrar el programa.
- **La clave `secreto6` no se cambia.**

**Por qué es lógico.**
- El profesor conoce la clave inicial y el programa funciona sin tocar nada.
- Si la clave cambia (la cambia el grupo o el profesor resetea las cuentas), basta con escribir la nueva en la ventana: no hay que recompilar ni editar ficheros.
- No cambiar la clave evita tener que comunicarla y que los tres integrantes la conozcan.

**Descartado.** Credenciales fijas en el código (si la clave cambia, el programa deja de funcionar sin remedio). Fichero de configuración (obliga a editarlo a mano).

**Si se cambia la clave.** Escribir la nueva en la ventana de conexión al arrancar. Si se quiere que salga precargada, cambiar el valor por defecto en el código de la ventana de conexión, recompilar y regenerar el zip. Indicarla en la memoria y en el `LEEME.txt` para el profesor.
