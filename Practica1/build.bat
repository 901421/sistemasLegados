@echo off
rem ------------------------------------------------------------------
rem  UnizarBank - compilacion para Windows (GnuCOBOL 3.2 con BDB)
rem  Genera la carpeta "ejecutable" autocontenida: BANK1.exe, BANK2-8.dll,
rem  las DLL de GnuCOBOL, los ficheros de datos .ubd y lanzar.bat.
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
cobc -x -o "%OUT%\BANK1.exe" src\BANK1.cbl || goto :error
for %%P in (BANK2 BANK3 BANK4 BANK5 BANK6 BANK7 BANK8) do (
    cobc -m -o "%OUT%\%%P.dll" src\%%P.cbl || goto :error
)
cobc -x -o "%OUT%\CREAR-MOVIMIENTOS.exe" src\CREAR-MOVIMIENTOS.cbl || goto :error
cobc -x -o "%OUT%\CREAR-ESPECTACULOS.exe" src\CREAR-ESPECTACULOS.cbl || goto :error

echo Copiando las DLL de GnuCOBOL...
for %%D in (libcob-4.dll libdb-18.1.dll libgcc_s_dw2-1.dll libgmp-10.dll libiconv-2.dll libintl-8.dll pdcurses.dll) do (
    copy /y "%COB_DIR%\bin\%%D" "%OUT%" >NUL || goto :error
)

echo Preparando los ficheros de datos...
copy /y original\tarjetas.ubd "%OUT%" >NUL || goto :error
copy /y original\intentos.ubd "%OUT%" >NUL || goto :error
pushd "%OUT%"
CREAR-MOVIMIENTOS.exe || goto :error_pop
CREAR-ESPECTACULOS.exe || goto :error_pop
del CREAR-MOVIMIENTOS.exe CREAR-ESPECTACULOS.exe
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
