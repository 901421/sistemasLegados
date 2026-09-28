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

## Pendiente

- s3270 para Linux/WSL.
- JDK de Windows y runtime reducido con `jlink` (tarea B5).
