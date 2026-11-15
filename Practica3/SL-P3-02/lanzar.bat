@echo off
rem ======================================================================
rem  Practica 3 de Sistemas Legados (grupo 02): doble clic para arrancar.
rem
rem  Abre el servidor en su propia ventana (con DOSBox y la aplicacion
rem  GW-BASIC), espera a que responda y abre la web en el navegador.
rem  Para apagarlo todo, se cierra la ventana del servidor (o Ctrl+C en ella).
rem
rem  Uso: lanzar.bat [PUERTO]     (8080 si no se indica)
rem ======================================================================
setlocal
cd /d "%~dp0"
set "PUERTO=%~1"
if "%PUERTO%"=="" set "PUERTO=8080"

title Practica 3 - Sistemas Legados (grupo 02)

rem Java: el incluido en la entrega (java\) o, si no esta, el instalado en el equipo.
set "JAVA=%~dp0java\bin\java.exe"
if not exist "%JAVA%" (
    where java >nul 2>&1
    if errorlevel 1 goto sin_java
    set "JAVA=java"
)

rem Programa: el jar de la entrega o, al desarrollar, las clases compiladas en out\.
set "CP=programa\SL-P3-02.jar;programa\lib\*"
if not exist "programa\SL-P3-02.jar" (
    if not exist "out\Main.class" (
        echo Compilando el proyecto...
        call "%~dp0build.bat"
        if errorlevel 1 goto error_compilar
    )
    set "CP=out;lib\*"
)

if not exist "Database-MSDOS\database.bat" goto faltan

echo Arrancando el servidor y DOSBox en otra ventana...
rem uiScale=1: el area de DOSBox se captura a 640x400 aunque Windows tenga otra escala.
start "Servidor P3 - cierra esta ventana para apagar el servidor y DOSBox" /D "%~dp0" cmd /k ""%JAVA%" --enable-native-access=ALL-UNNAMED -Dsun.java2d.uiScale=1 -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "%CP%" Main %PUERTO%"

echo Esperando a que el servidor responda en http://localhost:%PUERTO% ...
set /a INTENTO=0
:esperar
powershell.exe -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:%PUERTO%/' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if not errorlevel 1 goto abrir
set /a INTENTO+=1
if %INTENTO% GEQ 60 goto error_servidor
timeout /t 1 /nobreak >nul
goto esperar

:abrir
echo Servidor listo. Abriendo http://localhost:%PUERTO% en el navegador.
start "" "http://localhost:%PUERTO%/"
exit /b 0

:sin_java
echo ERROR: no se encuentra Java. Descomprime el zip entero (con la carpeta java)
echo y vuelve a hacer doble clic en lanzar.bat.
pause
exit /b 1

:faltan
echo ERROR: falta la aplicacion legada (Database-MSDOS). Descomprime el zip entero
echo en una carpeta y vuelve a hacer doble clic en lanzar.bat.
pause
exit /b 1

:error_compilar
echo ERROR: no se ha podido compilar el proyecto.
pause
exit /b 1

:error_servidor
echo.
echo ERROR: el servidor no responde. Mira el mensaje en la ventana del servidor.
pause
exit /b 1
