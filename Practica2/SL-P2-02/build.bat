@echo off
rem Prepara la carpeta de la entrega desde Windows (lo mismo que build.sh):
rem   1. compila el programa y lo empaqueta en programa\SL-P2-02.jar;
rem   2. genera con jlink un Java reducido en java\;
rem   3. copia ws3270.exe a ws3270\ y lanzar.bat.
rem Resultado: ..\entrega\SL-P2-02\
rem
rem Necesita un JDK con jlink (por defecto %USERPROFILE%\.jdks\openjdk-27;
rem se cambia con la variable JDK_WINDOWS) y wc3270 instalado (o WS3270).
setlocal

if not defined JDK_WINDOWS set "JDK_WINDOWS=%USERPROFILE%\.jdks\openjdk-27"
if not defined WS3270 set "WS3270=C:\Program Files\wc3270\ws3270.exe"
rem Modulos que usa el programa (medidos con jdeps).
set "MODULOS=java.base,java.desktop"

rem pushd tambien funciona si el proyecto esta en una ruta de red o de WSL.
pushd "%~dp0"
set "SALIDA=%CD%\..\entrega\SL-P2-02"
set "TRABAJO=%TEMP%\SL-P2-02-build"

if not exist "%JDK_WINDOWS%\bin\jlink.exe" goto sinjdk
if not exist "%WS3270%" goto sinws3270

if exist "%SALIDA%" rmdir /s /q "%SALIDA%"
if exist "%TRABAJO%" rmdir /s /q "%TRABAJO%"
mkdir "%SALIDA%\programa" "%SALIDA%\ws3270" "%TRABAJO%"

echo == 1. Compilar y empaquetar
rem El paquete pruebas no va en la entrega.
dir /s /b src\*.java | findstr /v /i "\\pruebas\\" > "%TRABAJO%\fuentes.txt"
"%JDK_WINDOWS%\bin\javac.exe" --release 25 -encoding UTF-8 -d "%TRABAJO%\clases" @"%TRABAJO%\fuentes.txt" || goto error
"%JDK_WINDOWS%\bin\jar.exe" --create --file "%SALIDA%\programa\SL-P2-02.jar" --main-class Main -C "%TRABAJO%\clases" . || goto error

echo == 2. Java reducido (jlink)
rem Se genera en %TEMP% y se copia: jlink no escribe bien en rutas de WSL.
"%JDK_WINDOWS%\bin\jlink.exe" --add-modules %MODULOS% --strip-debug --no-header-files --no-man-pages --compress=zip-9 --output "%TRABAJO%\java" || goto error
xcopy /e /i /q "%TRABAJO%\java" "%SALIDA%\java" > nul || goto error

echo == 3. Emulador ws3270 y lanzar.bat
copy /y "%WS3270%" "%SALIDA%\ws3270\ws3270.exe" > nul || goto error
copy /y lanzar.bat "%SALIDA%\lanzar.bat" > nul || goto error

rmdir /s /q "%TRABAJO%"
echo == Hecho: %SALIDA%
popd
exit /b 0

:sinjdk
echo No se encuentra jlink.exe en "%JDK_WINDOWS%\bin". Indica el JDK con: set JDK_WINDOWS=ruta
goto fin
:sinws3270
echo No se encuentra ws3270.exe en "%WS3270%". Indica la ruta con: set WS3270=ruta
goto fin
:error
echo Ha fallado un paso; la carpeta de la entrega no esta completa.
:fin
popd
exit /b 1
