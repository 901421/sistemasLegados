#!/usr/bin/env bash
# Prepara la carpeta de la entrega (para Windows) desde WSL:
#   1. compila el programa y lo empaqueta en programa/SL-P2-02.jar;
#   2. genera con jlink un Java reducido para Windows en java/;
#   3. copia ws3270.exe a ws3270/ y lanzar.bat;
#   4. añade LEEME.txt, la memoria y el código (git archive de HEAD) y
#      monta el zip.
# Resultado: ../entrega/SL-P2-02/ y ../entrega/SL-P2-02.zip
#
# Necesita: javac y jar en WSL, y un JDK de Windows con jlink.exe
# (por defecto el de C:\Users\imadx\.jdks\openjdk-27; se cambia con JDK_WINDOWS).
# La memoria se toma de MEMORIA (por defecto ../memoria-SL-P2-02.pdf).
set -euo pipefail

PROYECTO="$(cd "$(dirname "$0")" && pwd)"
SALIDA="$PROYECTO/../entrega/SL-P2-02"
JDK_WINDOWS="${JDK_WINDOWS:-/mnt/c/Users/imadx/.jdks/openjdk-27}"
WS3270="${WS3270:-/mnt/c/Program Files/wc3270/ws3270.exe}"
# Módulos que usa el programa (medidos con jdeps).
MODULOS="java.base,java.desktop"
MEMORIA="${MEMORIA:-$PROYECTO/../memoria-SL-P2-02.pdf}"

# El zip lleva el código de un commit: si hay cambios sin commit, el jar no
# coincidiría con él. SIN_COMPROBAR_GIT=1 lo salta (solo para pruebas).
if [ -z "${SIN_COMPROBAR_GIT:-}" ] && [ -n "$(git -C "$PROYECTO" status --porcelain -- .)" ]; then
    echo "Hay cambios sin commit en SL-P2-02/: haz commit antes de montar la entrega." >&2
    exit 1
fi

rm -rf "$SALIDA" "$SALIDA.zip"
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

echo "== 4. LEEME, memoria, código y zip"
cp "$PROYECTO/LEEME.txt" "$SALIDA/LEEME.txt"
if [ -f "$MEMORIA" ]; then
    cp "$MEMORIA" "$SALIDA/memoria-SL-P2-02.pdf"
else
    echo "AVISO: no está la memoria ($MEMORIA); el zip sale sin ella." >&2
fi
# Código del commit actual, sin los ficheros del IDE.
mkdir "$SALIDA/fuente"
git -C "$PROYECTO" archive --format=tar HEAD . \
    | tar -x -C "$SALIDA/fuente" --exclude='.idea' --exclude='*.iml'
(cd "$SALIDA/.." && zip -q -r -9 SL-P2-02.zip SL-P2-02)

echo "== Hecho: $(cd "$SALIDA/.." && pwd)/SL-P2-02.zip ($(du -h "$SALIDA.zip" | cut -f1)), commit $(git -C "$PROYECTO" rev-parse --short HEAD)"
