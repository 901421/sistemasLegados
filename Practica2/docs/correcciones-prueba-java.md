# Correcciones a la prueba Java con ws3270 (`SL-P2-02/src/Main.java`)

Revisión del commit `9ce24e5` ("Primer proyecto de prueba IntelliJ IDEA", 2026-09-28). La idea es correcta (lanzar ws3270 con `ProcessBuilder`, escribir órdenes y leer hasta `ok`) y se mantiene. Estos son los cambios y el porqué.

| # | Qué pasaba | Por qué es un problema | Qué se ha cambiado |
|---|---|---|---|
| 1 | La espera de 1,5 s antes de desconectar solo estaba en el camino normal. Si algo fallaba después de `Connect`, el `finally` hacía `process.destroy()` sin esperar. | Matar ws3270 corta la conexión en milisegundos. El guion (nota *i*, "MUY IMPORTANTE") exige al menos 1 s entre conexión y desconexión **también ante errores**; si no, sale la pantalla de Hercules y **nadie puede conectarse** hasta que el profesor resetee el mainframe. | Se guarda la hora del `Connect` y hay **un único punto de desconexión**, `disconnect()`, llamado siempre desde el `finally`: espera hasta 1,5 s desde el `Connect`, envía `Disconnect()` y `Quit()`, y solo si ws3270 no responde lo mata (ya pasado el mínimo). Muestra el tiempo que ha estado conectado. |
| 2 | Java lee y escribe en UTF-8, pero ws3270 en Windows usa por defecto la página de códigos ANSI (cp1252). | Las tildes y la ñ llegarían mal (ya lo vimos: `Camión` salía estropeado). | Se arranca ws3270 con `-localcp 65001` (UTF-8), opción que aparece en su ayuda. **Pendiente de comprobar** con una tarea con tildes. |
| 3 | La pantalla se leía justo después de `Wait(InputField)`. | En una prueba real (2026-09-28) esa primera lectura salió **vacía, con `Working`**: el mainframe aún no había dibujado la pantalla. | Nuevo `waitForScreen()`: repite `Ascii()` hasta que la pantalla tenga texto y no ponga `Working`, con un máximo de 10 s. `readScreen()` devuelve la pantalla y detecta `error`. |
| 4 | El proyecto traía su propio `.gitignore` y otro en `.idea/`. | Acordamos un solo `.gitignore`, en la raíz del repositorio. | Sus reglas útiles (`out/`, `*.class`, ficheros locales de IntelliJ) pasan al `.gitignore` de la raíz; se borran los dos del proyecto. |

**Sin cambiar, para más adelante:**
- Usuario y clave escritos en el código: vale para esta prueba, pero la versión final usa la ventana de conexión (decisión D1).
- `.idea/misc.xml` apunta a un JDK llamado `ms-25` (el de tu máquina); a otra persona IntelliJ le pedirá elegir su JDK. No afecta a la compilación.
- `mainframe/MainframeClient.java` está vacío: ahí irá la clase de conexión definitiva.

**Comprobado:** compila con `javac` (JDK 25). **No se ha ejecutado** contra el mainframe todavía: se hará anunciándolo antes y con una sola conexión.
