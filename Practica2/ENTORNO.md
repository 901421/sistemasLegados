# Entorno — Práctica 2 (grupo 02)

Versiones y configuración de las herramientas usadas. Se completa conforme se instala cada cosa.

## Emulador 3270 (Windows)

| Elemento | Valor | Cómo se comprobó |
|---|---|---|
| Paquete | `wc3270-4.5ga6-setup.exe` (proyecto x3270, descargado de SourceForge) | instalador |
| Versión | wc3270 v4.5ga6 (Jul 27 2026), 32 bits, TLS Windows Schannel | `wc3270.exe -v` |
| Carpeta | `C:\Program Files\wc3270\` | listado de la carpeta |
| Incluye | `wc3270.exe`, `ws3270.exe` (sin ventana, el que usará la GUI), `x3270if.exe`, `s3270.exe`, `wc3270wiz.exe` | listado de la carpeta |

## Sesión de wc3270

Creada con el *Session Wizard* (`wc3270wiz.exe`), nombre `grupo02`, guardada en `Documentos\wc3270\grupo02.wc3270`:

```
wc3270.hostname: 155.210.152.51:3270
wc3270.model: 4          (3279-4, 43x80)
wc3270.codePage: bracket (EBCDIC 037, el que trae por defecto)
```

## Acceso al mainframe

- Probado el 2026-09-28 desde la wifi de la universidad (eduroam) y desde casa sin VPN: en ambos casos conecta.
- Esa misma mañana, un primer intento dio "tiempo de espera agotado" (error 10060 de Windows) a todos los grupos; más tarde conectó sin cambiar nada.
- Cuenta `grupo_02`, clave `secreto6` sin cambiar (D1).

## Emulador sin ventana (screen-scraping)

| Elemento | Valor | Cómo se comprobó |
|---|---|---|
| Programa | `C:\Program Files\wc3270\ws3270.exe` (viene con wc3270) | listado de la carpeta |
| Versión | v4.5ga6 (Jul 27 2026), 32 bits; se identifica como `s3270` | `ws3270.exe -v` |

Durante el desarrollo, Java se ejecuta en WSL y lanza este mismo `ws3270.exe` de Windows (WSL puede ejecutar programas de Windows). Así se prueba el mismo emulador y la misma versión que van en la entrega. La ruta del emulador es configurable. No se usa s3270 de Linux.

## Java

| Elemento | Valor | Cómo se comprobó |
|---|---|---|
| JDK de desarrollo (WSL) | OpenJDK 25.0.4 LTS (SDKMAN) | `java -version` |
| JDK de Windows (para `jlink`) | OpenJDK 27 (build 27+35-2325), en `C:\Users\imadx\.jdks\openjdk-27` | `java.exe -version` |
| Módulos que usa el programa | `java.base`, `java.desktop` | `jdeps` |

## Java reducido para la entrega

`SL-P2-02/build.sh` (desde WSL) compila el programa, lo empaqueta en `programa/SL-P2-02.jar`, genera con `jlink.exe` de Windows un Java reducido en `java/` y copia `ws3270.exe` a `ws3270/`. Todo queda en `Practica2/entrega/SL-P2-02/`, que no se sube al repositorio.

| Elemento | Valor | Cómo se comprobó |
|---|---|---|
| Opciones de `jlink` | `--add-modules java.base,java.desktop --strip-debug --no-header-files --no-man-pages --compress=zip-9` | `build.sh` |
| Tamaño | `java/` 47 MB, `ws3270/` 3,1 MB, jar 64 KB; unos 34 MB comprimido en zip | `du -sh`, `zip -9` |
| Funciona solo | las pruebas sin conexión pasan con ese Java (`TODO BIEN`); `java.exe --dry-run -jar programa/SL-P2-02.jar` carga `Main`; en ese Windows no hay `java` en el `PATH` | ejecución del 2026-10-18 |
| Emulador incluido | `ws3270.exe` v4.5ga6, un solo fichero sin DLL | `ws3270.exe -v` |

`jlink.exe` no escribe bien en rutas de WSL, así que `build.sh` genera el Java en el `%TEMP%` de Windows y luego lo copia.
