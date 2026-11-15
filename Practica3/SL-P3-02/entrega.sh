#!/usr/bin/env bash
# Monta la entrega para Windows desde WSL (R29: doble clic, sin instalar nada):
#   1. compila el programa (sin el paquete pruebas) en programa/SL-P3-02.jar,
#      con las librerías que necesita al ejecutarse (JNA) en programa/lib;
#   2. genera con jlink un Java reducido para Windows en java/;
#   3. copia la aplicación legada intacta (y comprueba sus SHA-256), la
#      configuración de DOSBox, la web y lanzar.bat;
#   4. añade el LEEME, la memoria, el vídeo (si están) y el código del commit, y
#      monta el zip.
# Resultado: ../entrega/SL-P3-02/ y ../entrega/SL-P3-02.zip
#
# Necesita: javac y jar en WSL, y un JDK de Windows con jlink.exe (por defecto el
# de C:\Users\imadx\.jdks\openjdk-27; se cambia con JDK_WINDOWS).
set -euo pipefail

PROYECTO="$(cd "$(dirname "$0")" && pwd)"
SALIDA="$PROYECTO/../entrega/SL-P3-02"
JDK_WINDOWS="${JDK_WINDOWS:-/mnt/c/Users/imadx/.jdks/openjdk-27}"
# Módulos que usa el programa (medidos con jdeps sobre el código y JNA).
MODULOS="java.base,java.desktop,java.logging,jdk.httpserver"
MEMORIA="${MEMORIA:-$PROYECTO/../docs/memoria-SL-P3-02.pdf}"
VIDEO="${VIDEO:-$PROYECTO/../docs/video-SL-P3-02.webm}"

# El zip lleva el código de un commit: con cambios sin commit, el programa no
# coincidiría con él. SIN_COMPROBAR_GIT=1 lo salta (solo para pruebas).
if [ -z "${SIN_COMPROBAR_GIT:-}" ] && [ -n "$(git -C "$PROYECTO" status --porcelain -- .)" ]; then
    echo "Hay cambios sin commit en SL-P3-02/: haz commit antes de montar la entrega." >&2
    exit 1
fi

rm -rf "$SALIDA" "$SALIDA.zip"
mkdir -p "$SALIDA/programa/lib"

echo "== 1. Compilar y empaquetar"
CLASES="$(mktemp -d)"
find "$PROYECTO/src" -name '*.java' -not -path '*/pruebas/*' > "$CLASES/fuentes.txt"
javac --release 25 -encoding UTF-8 -cp "$PROYECTO/lib/*" -d "$CLASES/out" @"$CLASES/fuentes.txt"
cp "$PROYECTO/src/legado/fuente-dosbox.txt" "$CLASES/out/legado/"
jar --create --file "$SALIDA/programa/SL-P3-02.jar" --main-class Main -C "$CLASES/out" .
cp "$PROYECTO"/lib/jna-*.jar "$SALIDA/programa/lib/"
rm -rf "$CLASES"

echo "== 2. Java reducido para Windows (jlink)"
# jlink.exe no escribe bien en rutas de WSL: se genera en el %TEMP% de Windows
# y luego se copia.
TEMP_WIN="$(cmd.exe /c 'echo %TEMP%' 2>/dev/null | tr -d '\r')"
DESTINO_WIN="$TEMP_WIN\\SL-P3-02-java"
DESTINO_WSL="$(wslpath -u "$DESTINO_WIN")"
rm -rf "$DESTINO_WSL"
"$JDK_WINDOWS/bin/jlink.exe" --add-modules "$MODULOS" \
    --strip-debug --no-header-files --no-man-pages --compress=zip-9 \
    --output "$DESTINO_WIN"
cp -r "$DESTINO_WSL" "$SALIDA/java"
rm -rf "$DESTINO_WSL"

echo "== 3. Aplicación legada, DOSBox, web y lanzador"
# Desde git, no desde el disco: así van exactamente los ficheros del .rar original
# (DOSBox crea ficheros sueltos, como stdout.txt, al ejecutarse en la copia de trabajo).
git -C "$PROYECTO" archive --format=tar HEAD Database-MSDOS | tar -x -C "$SALIDA"
(cd "$SALIDA/Database-MSDOS" && sha256sum -c --quiet - <<'HASHES'
393209f8a3dfe5dbdabe6b9126bde31d10d284ef39483f44a30d432cdd39f661  Database/DATABASE.BAS
ca37fc1ccbbc62eccdd545bbdb7846e0c43d511300613d2b7e157f809fc52c11  Database/DATOS.DAT
de0ec019014604c7d48e7eab6e384b24476cd1b946356693a481cdb6300ab6d8  Database/gwbasic.bat
7c83bec46ef04c3766c41959dd00b92ee7b206557a97bd67d53d11bd3fbe0b79  Database/GWBASIC.EXE
d27381323bab22e19d5642c8577babcf310427052b7da598c4972df31b98e9a6  Database/GWBASIC.PIF
67cc1f9bb83f0564e2a74bb464c2e31255825482926fa8fb6ec0a7b1a575e50c  Database/INFORM.DAT
b76eaabf4f0da32a9f310f7c95a992eb181f1d4253609d19529813c27d45746a  database.bat
HASHES
) && echo "   aplicación legada: 7/7 SHA-256 iguales al original (R5)"
mkdir -p "$SALIDA/dosbox"
cp "$PROYECTO/dosbox/p3-dosbox.conf" "$SALIDA/dosbox/"
cp -r "$PROYECTO/frontend" "$SALIDA/frontend"
rm -f "$SALIDA"/frontend/styles/*.scss
cp "$PROYECTO/lanzar.bat" "$SALIDA/lanzar.bat"

echo "== 4. LEEME, memoria, vídeo, código y zip"
cp "$PROYECTO/LEEME.txt" "$SALIDA/LEEME.txt"
for f in "$MEMORIA" "$VIDEO"; do
    if [ -f "$f" ]; then cp "$f" "$SALIDA/"; else echo "AVISO: no está $f; el zip sale sin él." >&2; fi
done
# Código del commit actual, sin los ficheros del IDE, el LEEME ni la aplicación legada (ya están arriba).
mkdir "$SALIDA/fuente"
git -C "$PROYECTO" archive --format=tar HEAD . \
    | tar -x -C "$SALIDA/fuente" --exclude='.idea' --exclude='*.iml' --exclude='LEEME.txt' --exclude='Database-MSDOS'
# Documentación del grupo (fuera de SL-P3-02 en el repositorio), también del commit.
mkdir "$SALIDA/fuente/docs"
git -C "$PROYECTO/.." archive --format=tar HEAD docs/mapa-pantallas.md docs/requisitos.md docs/plan.md \
        docs/convenios.md docs/ocr-resultados.md DECISIONES.md REPARTO.md \
    | tar -x -C "$SALIDA/fuente/docs"
(cd "$SALIDA/.." && zip -q -r -9 SL-P3-02.zip SL-P3-02)

echo "== Hecho: $(cd "$SALIDA/.." && pwd)/SL-P3-02.zip ($(du -h "$SALIDA.zip" | cut -f1)), commit $(git -C "$PROYECTO" rev-parse --short HEAD)"
