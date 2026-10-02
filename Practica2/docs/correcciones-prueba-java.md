# Correcciones a la prueba Java con ws3270 (`SL-P2-02/src/Main.java`)

Revisión del commit `9ce24e5` ("Primer proyecto de prueba IntelliJ IDEA", 2026-09-28). La idea es correcta (lanzar ws3270 con `ProcessBuilder`, escribir órdenes y leer hasta `ok`) y se mantiene. Estos son los cambios y el porqué.

## Primera revisión (2026-09-28)

| # | Qué pasaba | Por qué es un problema | Qué se ha cambiado |
|---|---|---|---|
| 1 | La espera de 1,5 s antes de desconectar solo estaba en el camino normal. Si algo fallaba después de `Connect`, el `finally` hacía `process.destroy()` sin esperar. | Matar ws3270 corta la conexión en milisegundos. El guion (nota *i*, "MUY IMPORTANTE") exige al menos 1 s entre conexión y desconexión **también ante errores**; si no, sale la pantalla de Hercules y **nadie puede conectarse** hasta que el profesor resetee el mainframe. | Se guarda la hora del `Connect` y hay **un único punto de desconexión**, `disconnect()`, llamado siempre desde el `finally`: espera hasta 1,5 s desde el `Connect`, envía `Disconnect()` y `Quit()`, y solo si ws3270 no responde lo mata (ya pasado el mínimo). Muestra el tiempo que ha estado conectado. |
| 2 | Java lee y escribe en UTF-8, pero ws3270 en Windows usa por defecto la página de códigos ANSI (cp1252). | Las tildes y la ñ llegarían mal (ya lo vimos: `Camión` salía estropeado). | Se arranca ws3270 con `-localcp 65001` (UTF-8), opción que aparece en su ayuda. **Comprobado** el 2026-09-28 con una tarea real: el listado devolvió `TASK 0: GENERAL 0510 ----- Camión`. |
| 3 | La pantalla se leía justo después de `Wait(InputField)`. | En una prueba real (2026-09-28) esa primera lectura salió **vacía, con `Working`**: el mainframe aún no había dibujado la pantalla. | Nuevo `waitForScreen()`: repite `Ascii()` hasta que la pantalla tenga texto y no ponga `Working`, con un máximo de 10 s. `readScreen()` devuelve la pantalla y detecta `error`. (Mejorado en el punto 6.) |
| 4 | El proyecto traía su propio `.gitignore` y otro en `.idea/`. | Acordamos un solo `.gitignore`, en la raíz del repositorio. | Sus reglas útiles (`out/`, `*.class`, ficheros locales de IntelliJ) pasan al `.gitignore` de la raíz; se borran los dos del proyecto. |

## Segunda revisión (2026-10-02)

El código de conexión pasa de `Main.java` a la clase `mainframe/MainframeClient.java`, que estaba vacía. `Main.java` queda como una prueba corta que usa esa clase (conecta, muestra la primera pantalla y desconecta). Los nombres de la clase están en español (`conectar`, `enviar`, `pantalla`, `esperarPantallaEstable`, `desconectar`).

| # | Qué pasaba | Por qué es un problema | Qué se ha cambiado |
|---|---|---|---|
| 5 | La ruta de ws3270 estaba fija en el código (`C:\Program Files\wc3270\ws3270.exe`). | No se puede ejecutar desde WSL ni desde la carpeta de la entrega, donde ws3270 irá en otra ruta. | La ruta se puede dar al crear `MainframeClient`, o con `-Dws3270.ruta=...`. Si no se da, usa la instalación de wc3270 (desde WSL, `/mnt/c/Program Files/wc3270/ws3270.exe`). |
| 6 | `waitForScreen()` daba la pantalla por buena en cuanto tenía texto y no ponía `Working`. | MUSIC dibuja la pantalla a trozos: se puede leer una pantalla a medias. Lo vimos en la prueba por script del 2026-09-28. | `esperarPantallaEstable()`: repite `Ascii()` cada 0,4 s hasta obtener **dos lecturas iguales**, con texto y sin `Working`. Ya no se usa `Wait(InputField)`, que volvía antes de tiempo. |
| 7 | Con Ctrl+C (o un error no capturado fuera del `try`) el programa terminaba sin pasar por `disconnect()`. | ws3270 muere con el programa y la conexión se corta sin esperar el segundo mínimo. | Al conectar se registra un cierre de emergencia (*shutdown hook*) que llama al mismo `desconectar()`. La espera del mínimo no se puede interrumpir. |
| 8 | `waitForOk()` y `readScreen()` leían de ws3270 sin tiempo máximo. | Si ws3270 no contesta, el programa (y más adelante la ventana) se queda bloqueado para siempre. | Un hilo aparte lee la salida de ws3270 y la deja en una cola; cada orden espera su respuesta como mucho 40 s (5 s durante el cierre) y, si no llega, da un error claro. |
| 9 | ws3270 se arrancaba sin indicar el modelo de terminal y con la opción `-script`. | El mapa de pantallas está hecho con el modelo 4 (43x80); con otro tamaño las filas no coinciden. `-script` no se usó en la prueba real, así que no estaba comprobada. | Se arranca con `-model 4 -localcp 65001`, igual que en la prueba real del 2026-09-28. |
| 10 | La hora de conexión se anotaba solo antes de enviar `Connect`. | Si la conexión tarda en establecerse, el tiempo realmente conectado sería menor que el contado. | Se anota antes de `Connect` (por si falla a medias) y otra vez cuando `Connect` responde `ok`; el mínimo de 1,5 s cuenta desde ahí. |
| 11 | Si ws3270 respondía `error`, el mensaje se perdía (solo se mostraba la palabra `error`). | No se sabía qué había rechazado ws3270. | El error incluye la orden y el texto que devuelve ws3270. |

**Sin cambiar, para más adelante:**
- Usuario y clave: la versión final usa la ventana de conexión (decisión D1). `Main.java` ya no los contiene porque no hace login.
- `.idea/misc.xml` apunta a un JDK llamado `ms-25` (el de tu máquina); a otra persona IntelliJ le pedirá elegir su JDK. No afecta a la compilación.

**Comprobado (2026-10-02):** compila con `javac -Xlint:all` (JDK 25) sin avisos. Probado **sin conectar al mainframe**, con un programa que imita las respuestas de ws3270: cierre normal, pantalla de Hercules, Ctrl+C, error no capturado y ws3270 que no contesta. En los cuatro primeros casos pasaron 1,50 s entre `Connect` y `Disconnect`; en el último, el programa terminó con error a los 40 s en lugar de quedarse bloqueado.

**Ejecutado contra el mainframe (2026-10-02, 14:16), una sola conexión desde IntelliJ:** `Connect`, bienvenida de MUSIC/SP estable a la tercera lectura (43 filas), aviso de `More...` y `DESCONECTADO tras 1501 ms`; ws3270 terminó con `Quit` (código de salida 0). Falta probar contra el mainframe el login y el cierre por error y por Ctrl+C.
