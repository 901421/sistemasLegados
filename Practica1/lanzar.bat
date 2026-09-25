@echo off
rem UnizarBank - lanzador: ventana de 80x25 y arranque del cajero.
cd /d "%~dp0"
title Cajero Automatico UnizarBank
mode con cols=80 lines=25
set "COB_LIBRARY_PATH=%~dp0"
BANK1.exe
