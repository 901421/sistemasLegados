@echo off
rem Arranca el programa con el Java y el ws3270 que vienen en esta carpeta.
rem Se usa javaw (sin consola): asi un Ctrl+C o cerrar una consola no puede
rem cortar ws3270 sin esperar el minimo de 1 s antes de desconectar.

set "CARPETA=%~dp0"

if not exist "%CARPETA%java\bin\javaw.exe" goto falta
if not exist "%CARPETA%programa\SL-P2-02.jar" goto falta
if not exist "%CARPETA%ws3270\ws3270.exe" goto falta

start "" "%CARPETA%java\bin\javaw.exe" -jar "%CARPETA%programa\SL-P2-02.jar" "%CARPETA%ws3270\ws3270.exe"
exit /b 0

:falta
echo Faltan ficheros junto a lanzar.bat (java, programa o ws3270).
echo Descomprime el zip entero en una carpeta y vuelve a hacer doble clic.
pause
exit /b 1
