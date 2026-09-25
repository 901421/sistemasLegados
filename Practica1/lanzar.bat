@echo off
rem UnizarBank - lanzador: ventana de 80x25 y arranque del cajero.
rem Windows Terminal ignora "mode con": si existe la consola clasica
rem (conhost.exe), el lanzador se vuelve a abrir en ella. Si no, sigue
rem en la ventana actual.
if /i "%~1"=="consola" goto cajero
if exist "%SystemRoot%\System32\conhost.exe" (
  start "" "%SystemRoot%\System32\conhost.exe" cmd.exe /c ""%~f0" consola"
  exit /b
)
:cajero
cd /d "%~dp0"
title Cajero Automatico UnizarBank
mode con cols=80 lines=25
BANK1.exe
