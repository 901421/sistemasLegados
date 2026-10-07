# Convenios de trabajo — Práctica 2 (grupo 02)

Cómo está organizado el código y cómo trabajamos los tres para que todo encaje. Si algo de aquí no convence, se cambia aquí primero y después en el código.

## 1. Estructura del programa

Proyecto de IntelliJ en `Practica2/SL-P2-02/`, código en `src/`. Cada capa solo usa la que tiene debajo:

```
gui  ──►  tareas  ──►  mainframe  ──►  ws3270  ──►  mainframe MUSIC/SP
```

| Fichero | Qué hace | Estado | Responsable (propuesta) |
|---|---|---|---|
| `mainframe/MainframeClient.java` | Lanza ws3270, envía órdenes, lee pantallas, espera a que estén estables y desconecta respetando el tiempo mínimo | Hecho | Imad |
| `mainframe/Pantalla.java` | Reconoce en qué pantalla estamos a partir de su texto | Esqueleto | Paula |
| `mainframe/SesionMusic.java` | Login, arranque de `tareas.c` y cierre con `off` | Esqueleto | Paula |
| `tareas/Tarea.java` | Los datos de una tarea | Hecho | — |
| `tareas/LectorListado.java` | Saca las tareas del texto de un listado | Esqueleto | Paula |
| `tareas/GestorTareas.java` | `listar()` y `nueva(...)`: navega los menús de `tareas.c` | Esqueleto | Paula (`listar`), Imad (`nueva`) |
| `tareas/Validador.java` | Comprobaciones de D2 antes de enviar un alta | Esqueleto | Ainhoa |
| `gui/VentanaConexion.java` | Ventana inicial con los datos de conexión (D1) | Esqueleto | Ainhoa |
| `gui/VentanaTareas.java` | Ventana principal, trabajo en segundo plano y cierre | Esqueleto | Imad |
| `gui/PanelAlta.java` | Formulario de alta y botón "Nueva" | Esqueleto | Ainhoa |
| `gui/ModeloTablaTareas.java` | Datos de la tabla de tareas | Esqueleto | Paula |
| `pruebas/PruebasOffline.java` | Pruebas con pantallas guardadas, sin conectar | Esqueleto | Todos |
| `Main.java` | Arranque: abre `VentanaConexion` (ruta de ws3270 opcional como argumento) | Hecho | — |

Los métodos de los esqueletos lanzan `UnsupportedOperationException("Pendiente")`: así todo compila desde el primer día y cada uno puede usar las clases de los demás antes de que estén terminadas.

**Las firmas ya escritas (nombre del método, parámetros y lo que devuelve) son el acuerdo entre los tres.** Quien necesite cambiar una, avisa antes al grupo, porque otro puede estar usándola.

## 2. Código

- **Nombres en español**, sin tildes ni ñ en los identificadores (`conectar`, `descripcion`). La clase `MainframeClient` conserva su nombre.
- Clases en `PascalCase`, métodos y variables en `camelCase`, constantes en `MAYUSCULAS_CON_GUION`.
- Sangría de 4 espacios, ficheros en UTF-8.
- **Comentarios:** un comentario `/** ... */` corto encima de cada clase y de cada método público, que diga qué hace y qué error puede dar. Dentro del código, solo donde el porqué no sea evidente. Sin nombres de autor ni fechas (eso está en git).
- **Errores:** todo lo que falla al hablar con el mainframe lanza `IOException` con un mensaje en español que se pueda enseñar tal cual al usuario. La interfaz lo muestra en un cuadro de diálogo; nadie deja un `catch` vacío.
- **Sin librerías externas.** Solo lo que trae el JDK (Swing incluido), para que la entrega funcione sin instalar nada.
- Usuario, clave, host y puerto solo aparecen como valores por defecto en `VentanaConexion` (D1).

## 3. Reglas con el mainframe

Si se desconecta antes de 1 s, el mainframe deja de aceptar conexiones **a todos los grupos** hasta que el profesor lo reinicia.

1. **Solo `MainframeClient` conecta y desconecta.** Nadie lanza ws3270 por su cuenta ni llama a `Process.destroy()`.
2. **Usar siempre** `try (MainframeClient cliente = ...) { ... }` o llamar a `desconectar()` en un `finally`.
3. **No pulsar el botón Stop de IntelliJ** con el programa conectado: dejar que termine solo.
4. **Avisar por el grupo antes de cada conexión real** y no encadenar ejecuciones seguidas. Los tres compartimos la cuenta `grupo_02`: dos sesiones a la vez dan "Userid is in use".
5. **Salir siempre en orden:** opción `3` en `tareas.c`, `off` en MUSIC y después desconectar. Sin `off`, la sesión queda colgada.
6. Si aparece la pantalla de **Hercules**, no reintentar: avisar al grupo.
7. Nada de `nc`, `telnet`, `ping` de puertos ni conexiones "para ver si va".
8. No se modifica `tareas.c` ni se busca su fichero de datos.

**Sincronización:** después de cada `intro()` se llama a `esperarPantallaEstable()` antes de leer o de escribir lo siguiente. Esa espera no pulsa Intro cuando hay `More...`; quien lee decide (`MainframeClient.hayMas()`), porque en un listado hay que guardar la página antes de pasar a la siguiente.

**La interfaz nunca habla con el mainframe desde el hilo de la ventana:** siempre en segundo plano (`SwingWorker`), con los botones desactivados mientras tanto.

## 4. Pruebas

- **Sin conexión siempre que se pueda.** Lo que solo analiza texto (`Pantalla`, `LectorListado`, `Validador`) se prueba en `pruebas/PruebasOffline.java` con las pantallas guardadas en `img/a1` e `img/a2` (`.txt`).
- Se ejecuta desde IntelliJ (triángulo verde en `PruebasOffline`) con la carpeta de trabajo en `SL-P2-02`. Tiene que acabar en `TODO BIEN`.
- Quien añade un método de análisis, añade su comprobación en el mismo commit.
- Las pruebas contra el mainframe siguen las reglas del apartado 3.

## 5. Git

- Se trabaja en **`main`**. Antes de empezar: `git pull --rebase`. Antes de subir: otra vez `git pull --rebase`, y comprobar que compila.
- **Commits pequeños**, uno por cosa terminada, con el mensaje `P2 <apartado del guion>: <qué se ha hecho>`. Ejemplo: `P2 Acceso 3270 displayless: login y arranque de tareas.c`.
- **No se sube nada que no compile.**
- No se suben compilados (`out/`, `*.class`) ni ficheros personales del IDE. Hay un solo `.gitignore`, en la raíz del repositorio.
- Cada uno toca sus ficheros (tabla del apartado 1). Para cambiar el de otra persona, se le avisa.

## 6. Cuándo una tarea está terminada

1. Compila sin avisos y `PruebasOffline` acaba en `TODO BIEN`.
2. Está probada: sin conexión si es análisis de texto; contra el mainframe si es conexión o navegación.
3. La ha revisado otra persona del grupo (Imad revisa a Paula, Paula a Ainhoa, Ainhoa a Imad).
4. Está apuntada en la tabla "Tareas realizadas" de `REPARTO.md` y marcada con `[x]`.
