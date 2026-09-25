@echo off
rem ------------------------------------------------------------------
rem  UnizarBank - compilacion para Windows (GnuCOBOL 3.2 con BDB)
rem  Genera la carpeta "ejecutable" autocontenida: BANK1.exe (los 9
rem  programas en un solo ejecutable), las DLL de GnuCOBOL, los ficheros
rem  de datos .ubd y lanzar.bat.
rem  Uso: build.bat            (GnuCOBOL en C:\GnuCOBOL)
rem       set COB_DIR=D:\ruta\GnuCOBOL  y luego build.bat
rem ------------------------------------------------------------------
setlocal
cd /d "%~dp0"
if "%COB_DIR%"=="" set "COB_DIR=C:\GnuCOBOL"
if not exist "%COB_DIR%\set_env.cmd" (
    echo No se encuentra GnuCOBOL en "%COB_DIR%".
    echo Instale GnuCOBOL 3.2 con BDB o indique su ruta en COB_DIR.
    exit /b 1
)
call "%COB_DIR%\set_env.cmd" >NUL

set "OUT=%~dp0ejecutable"
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

echo Compilando...
cobc -x -o "%OUT%\BANK1.exe" src\BANK1.cbl src\BANK2.cbl src\BANK3.cbl ^
    src\BANK4.cbl src\BANK5.cbl src\BANK6.cbl src\BANK7.cbl src\BANK8.cbl ^
    src\BANK9.cbl ^
    || goto :error
cobc -x -o "%OUT%\CREAR-MOVIMIENTOS.exe" src\CREAR-MOVIMIENTOS.cbl || goto :error
cobc -x -o "%OUT%\CREAR-ESPECTACULOS.exe" src\CREAR-ESPECTACULOS.cbl || goto :error
cobc -x -o "%OUT%\CREAR-TRANSFERENCIAS.exe" src\CREAR-TRANSFERENCIAS.cbl || goto :error

echo Copiando las DLL de GnuCOBOL...
for %%D in (libcob-4.dll libdb-18.1.dll libgcc_s_dw2-1.dll libgmp-10.dll libiconv-2.dll libintl-8.dll pdcurses.dll) do (
    copy /y "%COB_DIR%\bin\%%D" "%OUT%" >NUL || goto :error
)

echo Preparando los ficheros de datos...
copy /y original\tarjetas.ubd "%OUT%" >NUL || goto :error
copy /y original\intentos.ubd "%OUT%" >NUL || goto :error
pushd "%OUT%"
for %%P in (CREAR-MOVIMIENTOS CREAR-ESPECTACULOS CREAR-TRANSFERENCIAS) do (
    call :ejecutar %%P || goto :error_pop
)
del CREAR-MOVIMIENTOS.exe CREAR-ESPECTACULOS.exe CREAR-TRANSFERENCIAS.exe
popd

copy /y lanzar.bat "%OUT%" >NUL || goto :error
echo.
echo Listo. Para usar el cajero: doble clic en ejecutable\lanzar.bat
exit /b 0

:error_pop
popd
:error
echo.
echo ERROR durante la compilacion.
exit /b 1

rem Ejecuta %1.exe. En Windows 11 con el Control inteligente de
rem aplicaciones, un programa recien compilado y sin firmar puede ser
rem bloqueado ("bloqueado por la directiva de Device Guard"). Windows
rem recuerda la decision para ese fichero, asi que se vuelve a compilar
rem (fichero nuevo) y se reintenta, hasta 5 veces.
:ejecutar
set /a INTENTO=0
:ejecutar_otra
set /a INTENTO+=1
"%~1.exe" && exit /b 0
if %INTENTO% GEQ 5 exit /b 1
echo Windows ha bloqueado %~1.exe (intento %INTENTO% de 5): se recompila y se reintenta...
del "%~1.exe"
cobc -x -o "%~1.exe" "%~dp0src\%~1.cbl" || exit /b 1
goto :ejecutar_otra
