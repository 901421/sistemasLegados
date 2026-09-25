@echo off
rem UnizarBank - lanzador: ventana de 80x25 y arranque del cajero.
rem Windows Terminal ignora "mode con": si existe la consola clasica
rem (conhost.exe), el lanzador se vuelve a abrir en ella. Si no, sigue
rem en la ventana actual. Sin bloques ( ... ): una ruta con parentesis,
rem como "SL-P1-02 (1)", los romperia.
if /i "%~1"=="consola" goto cajero
if not exist "%SystemRoot%\System32\conhost.exe" goto cajero
rem Se relanza desde su carpeta (/D) y solo con el nombre del fichero:
rem con la ruta completa, los espacios rompen las comillas al pasar por
rem conhost.
start "" /D "%~dp0" "%SystemRoot%\System32\conhost.exe" cmd.exe /c %~nx0 consola
exit /b
:cajero
cd /d "%~dp0"
title Cajero Automatico UnizarBank
mode con cols=80 lines=25
BANK1.exe
