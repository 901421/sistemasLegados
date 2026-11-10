@echo off
setlocal
cd /d "%~dp0"

title ZX Spectrum Archive - Servidor

echo ========================================
echo   ZX Spectrum Archive
echo   Sistemas Legados - Practica 3
echo ========================================
echo.

rem Comprobar que Java esta disponible
where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java no esta instalado o no esta en el PATH.
    pause
    exit /b 1
)

rem Compilar si no existe el programa compilado
if not exist "out\legado\Main.class" (
    echo Compilando el proyecto...
    call "%~dp0build.bat"

    if errorlevel 1 (
        echo ERROR: No se ha podido compilar el proyecto.
        pause
        exit /b 1
    )
)

echo Iniciando servidor Java y DOSBox...
echo.

rem Lanzar el servidor en una nueva ventana
start "ZX Spectrum Archive - Backend" /D "%~dp0" cmd /k java -cp "out;lib/*" legado.Main

rem Esperar a que el servidor HTTP este disponible
echo Esperando al servidor...

set "MAX_INTENTOS=60"
set /a INTENTO=0

:esperar
powershell.exe -NoProfile -Command ^
  "try { $r = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"

if not errorlevel 1 goto abrir

set /a INTENTO+=1

if %INTENTO% GEQ %MAX_INTENTOS% goto error_servidor

timeout /t 1 /nobreak >nul
goto esperar

:abrir
echo Servidor disponible. Abriendo navegador...
start "" "http://localhost:8080/"
exit /b 0

:error_servidor
echo.
echo ERROR: No se ha podido iniciar el servidor.
echo Revisa la ventana del backend para consultar el error.
pause
exit /b 1
