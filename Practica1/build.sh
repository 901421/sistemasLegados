#!/bin/sh
# UnizarBank - compilacion para Linux (desarrollo y pruebas).
# Genera ejecutable-linux/ con BANK1, BANK2-8.so y los ficheros de datos.
# Para usarlo: cd ejecutable-linux && COB_LIBRARY_PATH=. ./BANK1
set -e
cd "$(dirname "$0")"
OUT=ejecutable-linux
rm -rf "$OUT"
mkdir -p "$OUT"

echo "Compilando..."
cobc -x -o "$OUT/BANK1" src/BANK1.cbl
for P in BANK2 BANK3 BANK4 BANK5 BANK6 BANK7 BANK8; do
    cobc -m -o "$OUT/$P.so" "src/$P.cbl"
done
cobc -x -o "$OUT/CREAR-MOVIMIENTOS" src/CREAR-MOVIMIENTOS.cbl
cobc -x -o "$OUT/CREAR-ESPECTACULOS" src/CREAR-ESPECTACULOS.cbl

echo "Preparando los ficheros de datos..."
cp original/tarjetas.ubd original/intentos.ubd "$OUT/"
(cd "$OUT" && ./CREAR-MOVIMIENTOS && ./CREAR-ESPECTACULOS \
    && rm CREAR-MOVIMIENTOS CREAR-ESPECTACULOS)

echo "Listo. Para usar el cajero: cd $OUT && COB_LIBRARY_PATH=. ./BANK1"
