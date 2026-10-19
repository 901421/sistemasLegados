#!/usr/bin/env bash
# Compila el programa desde WSL en SL-P3-02/out (las librerías van en lib/).
# El montaje de la entrega se añade más adelante.
set -euo pipefail

PROYECTO="$(cd "$(dirname "$0")" && pwd)"
rm -rf "$PROYECTO/out"
find "$PROYECTO/src" -name '*.java' > "$PROYECTO/fuentes.tmp"
javac --release 25 -encoding UTF-8 -cp "$PROYECTO/lib/*" -d "$PROYECTO/out" @"$PROYECTO/fuentes.tmp"
# Recursos (la tabla de plantillas) junto a las clases.
(cd "$PROYECTO/src" && find . -type f ! -name "*.java" -exec cp --parents {} "$PROYECTO/out" \;)
rm -f "$PROYECTO/fuentes.tmp"
echo "Compilado en $PROYECTO/out"
