#!/usr/bin/env bash
# Prepara la carpeta de la entrega (para Windows) desde WSL:
#   1. compila el programa y lo empaqueta en programa/SL-P2-02.jar;
#   2. genera con jlink un Java reducido para Windows en java/;
#   3. copia ws3270.exe a ws3270/ y lanzar.bat.
# Resultado: ../entrega/SL-P2-02/
#
# Necesita: javac y jar en WSL, y un JDK de Windows con jlink.exe
# (por defecto el de C:\Users\imadx\.jdks\openjdk-27; se cambia con JDK_WINDOWS).
set -euo pipefail

PROYECTO="$(cd "$(dirname "$0")" && pwd)"
SALIDA="$PROYECTO/../entrega/SL-P2-02"
JDK_WINDOWS="${JDK_WINDOWS:-/mnt/c/Users/imadx/.jdks/openjdk-27}"
WS3270="${WS3270:-/mnt/c/Program Files/wc3270/ws3270.exe}"
# Módulos que usa el programa (medidos con jdeps).
MODULOS="java.base,java.desktop"

rm -rf "$SALIDA"
mkdir -p "$SALIDA/programa" "$SALIDA/ws3270"

echo "== 1. Compilar y empaquetar"
CLASES="$(mktemp -d)"
# El paquete pruebas no va en la entrega.
find "$PROYECTO/src" -name '*.java' -not -path '*/pruebas/*' > "$CLASES/fuentes.txt"
javac --release 25 -encoding UTF-8 -d "$CLASES/out" @"$CLASES/fuentes.txt"
jar --create --file "$SALIDA/programa/SL-P2-02.jar" --main-class Main -C "$CLASES/out" .
rm -rf "$CLASES"

echo "== 2. Java reducido para Windows (jlink)"
# jlink.exe no escribe bien en rutas de WSL: se genera en el %TEMP% de
# Windows y luego se copia.
TEMP_WIN="$(cmd.exe /c 'echo %TEMP%' 2>/dev/null | tr -d '\r')"
DESTINO_WIN="$TEMP_WIN\\SL-P2-02-java"
DESTINO_WSL="$(wslpath -u "$DESTINO_WIN")"
rm -rf "$DESTINO_WSL"
"$JDK_WINDOWS/bin/jlink.exe" --add-modules "$MODULOS" \
    --strip-debug --no-header-files --no-man-pages --compress=zip-9 \
    --output "$DESTINO_WIN"
cp -r "$DESTINO_WSL" "$SALIDA/java"
rm -rf "$DESTINO_WSL"

echo "== 3. Emulador ws3270 y lanzar.bat"
cp "$WS3270" "$SALIDA/ws3270/ws3270.exe"
cp "$PROYECTO/lanzar.bat" "$SALIDA/lanzar.bat"

echo "== Hecho: $(cd "$SALIDA" && pwd) ($(du -sh "$SALIDA" | cut -f1))"
