# Resultados del OCR (Tess4J 5.20.0, Tesseract 5.5.3)

Conjunto: 68 capturas reales de DOSBox (menú, información, las 43 páginas del listado y 6 búsquedas). Texto esperado generado con los LOCATE/PRINT de DATABASE.BAS y los 763 registros de DATOS.DAT (solo para medir). Generado por `pruebas.MedirOcr`.

- **Caracteres**: precisión por carácter (1 − distancia de edición / longitud esperada), espacios seguidos contados como uno.
- **Líneas**: % de líneas esperadas que aparecen exactas.
- **Registros**: % de filas del listado (Nº, NOMBRE, TIPO, CINTA y REGISTRO) leídas sin ningún error; es lo que necesita el wrapper.

| Configuración | Caracteres | Líneas | Registros | Tiempo |
|---|---|---|---|---|
| a. guion (tal cual, PSM 3) | 30,78 % | 0,22 % (2/921) | 0,26 % (2/763) | 6 s |
| b. tal cual, PSM 6 | 86,64 % | 3,58 % (33/921) | 4,33 % (33/763) | 6 s |
| b. invertida, PSM 6 | 86,64 % | 3,58 % (33/921) | 4,33 % (33/763) | 6 s |
| b. binarizada, PSM 6 | 86,64 % | 3,58 % (33/921) | 4,33 % (33/763) | 6 s |
| b. binarizada x2, PSM 6 | 93,43 % | 24,32 % (224/921) | 23,33 % (178/763) | 10 s |
| b. binarizada x3, PSM 6 | 89,64 % | 12,60 % (116/921) | 11,01 % (84/763) | 18 s |
| b. binarizada x4, PSM 6 | 87,76 % | 5,43 % (50/921) | 6,42 % (49/763) | 24 s |
| b. tal cual x2, PSM 6 | 93,43 % | 24,32 % (224/921) | 23,33 % (178/763) | 9 s |
| b. tal cual x3, PSM 6 | 89,64 % | 12,60 % (116/921) | 11,01 % (84/763) | 17 s |
| c. binarizada x2, PSM 3 | 29,01 % | 2,93 % (27/921) | 2,62 % (20/763) | 12 s |
| c. binarizada x2, PSM 4 | 93,74 % | 27,04 % (249/921) | 27,65 % (211/763) | 10 s |
| d. binarizada x2, PSM 4 + lista blanca | 93,74 % | 27,04 % (249/921) | 27,65 % (211/763) | 10 s |
| d. binarizada x2, PSM 4 + sin diccionario | 94,03 % | 28,34 % (261/921) | 28,44 % (217/763) | 10 s |
| d. binarizada x2, PSM 4 + las dos | 94,03 % | 28,34 % (261/921) | 28,44 % (217/763) | 10 s |

Lista blanca usada: `!&'()-./0123456789:;?ABCDEFGHIJKLMNOPQRSTUVWXYZacdfhirtyº`

## Configuración del guion: a. guion (tal cual, PSM 3)

Confusiones más frecuentes (esperado→leído, en líneas de igual longitud):

- `'N'→'R'`: 88
- `'U'→'O'`: 67
- `'0'→'D'`: 55
- `'B'→'R'`: 35
- `'N'→'M'`: 32
- `'?'→'7'`: 30
- `'-'→'K'`: 29
- `'K'→'R'`: 29
- `'8'→'O'`: 28
- `'W'→'O'`: 25
- `'G'→'C'`: 25
- `'4'→'I'`: 23

| Captura | Esperado | Leído |
|---|---|---|
| menu | `M E N U` | `M E M U` |
| menu | `1 - INTRODUCIR DATOS` | `IMTRODUCIR DATOS` |
| menu | `2 - GRABAR DATOS` | `GRABAR DATOS` |
| menu | `3 - ORDENAR DATOS` | `ORDEMAR DATOS` |
| menu | `4 - INFORMACION BASE DE DATOS` | `IMFORMACIOM BASE DE DATDS` |
| menu | `5 - IMPRIMIR DATOS SEGUN ORDEN` | `IMPRIMIR DATOS SEGUM ORDEM` |

## Mejor configuración: d. binarizada x2, PSM 4 + sin diccionario

Confusiones más frecuentes (esperado→leído, en líneas de igual longitud):

- `'M'→'N'`: 302
- `'9'→'5'`: 287
- `'0'→'O'`: 212
- `'-'→'M'`: 84
- `'2'→'Z'`: 57
- `'?'→'7'`: 50
- `'.'→'I'`: 42
- `'A'→'H'`: 38
- `'0'→'6'`: 36
- `'''→'I'`: 32
- `'M'→'P'`: 27
- `'('→'I'`: 22

| Captura | Esperado | Leído |
|---|---|---|
| menu | `M E N U` | `N E N U` |
| menu | `1 - INTRODUCIR DATOS` | `INTRODUCIR DATOS` |
| menu | `2 - GRABAR DATOS` | `GRABAR DATOS` |
| menu | `3 - ORDENAR DATOS` | `ORDENAR DATOS` |
| menu | `4 - INFORMACION BASE DE DATOS` | `INFORPHCION BASE DE DATOS` |
| menu | `5 - IMPRIMIR DATOS SEGUN ORDEN` | `INPRINIR DATOS SEGUN ORDEN` |

## Entrenamiento de Tesseract con las capturas (R18)

Ajuste fino del modelo LSTM `spa` de tessdata_best con tesstrain (Tesseract 5.3.4 en WSL): 609 líneas de las pantallas de entrenamiento (menú y páginas 1-30 del listado, texto negro sobre blanco, x2), 3000 iteraciones en 6 min 18 s, error por carácter en entrenamiento 1,35 %. Medido con Tess4J **solo con las 37 pantallas que no vio** (páginas 31-43, información y búsquedas):

| Configuración | Caracteres | Líneas | Registros | Tiempo |
|---|---|---|---|---|
| spa de la asignatura, binarizada x2, PSM 4 + sin diccionario | 93,25 % | 26,60 % (83/312) | 30,94 % (69/223) | 4 s |
| e. dosbox entrenado (LSTM), binarizada x2, PSM 6 | 95,90 % | 38,14 % (119/312) | 39,01 % (87/223) | 68 s |
| e. dosbox entrenado (LSTM), binarizada x2, PSM 4 | 95,98 % | 39,42 % (123/312) | 39,01 % (87/223) | 71 s |

Errores típicos del modelo entrenado: se come la primera cifra de los números de 3 cifras que empiezan por 5 (`541` → `41`), `5→2`, `I→T` y parte palabras (`REG ISTRO`).

Tampoco llega al 100 %, así que se pasa a otro método, como permite la NOTA del guion.

## Método final: reconocimiento por plantillas (R19)

`legado.LectorPantalla` con `fuente-dosbox.txt` (87 plantillas de 8x16 píxeles sacadas de las pantallas de entrenamiento y de la captura del juego de caracteres; 0 conflictos). Comprobado con `pruebas.VerificarPlantillas`:

| Pantallas | Exactas | Celdas |
|---|---|---|
| Usadas para las plantillas (31) | 31/31 | 62.000/62.000 (100 %) |
| No vistas (37) | 37/37 | 73.920/73.920 (100 %) |

Leer las 68 pantallas lleva 1,5 s en total.
