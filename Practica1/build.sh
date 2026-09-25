#!/bin/sh
# UnizarBank - compilacion para Linux (desarrollo y pruebas).
# Genera ejecutable-linux/ con BANK1 (los 9 programas en un solo ejecutable)
# y los ficheros de datos. Para usarlo: cd ejecutable-linux && ./BANK1
set -e
cd "$(dirname "$0")"
OUT=ejecutable-linux
rm -rf "$OUT"
mkdir -p "$OUT"

echo "Compilando..."
cobc -x -o "$OUT/BANK1" src/BANK1.cbl src/BANK2.cbl src/BANK3.cbl \
    src/BANK4.cbl src/BANK5.cbl src/BANK6.cbl src/BANK7.cbl src/BANK8.cbl \
    src/BANK9.cbl
cobc -x -o "$OUT/CREAR-MOVIMIENTOS" src/CREAR-MOVIMIENTOS.cbl
cobc -x -o "$OUT/CREAR-ESPECTACULOS" src/CREAR-ESPECTACULOS.cbl

echo "Preparando los ficheros de datos..."
cp original/tarjetas.ubd original/intentos.ubd "$OUT/"
(cd "$OUT" && ./CREAR-MOVIMIENTOS && ./CREAR-ESPECTACULOS \
    && rm CREAR-MOVIMIENTOS CREAR-ESPECTACULOS)

echo "Listo. Para usar el cajero: cd $OUT && ./BANK1"
