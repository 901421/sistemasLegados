@echo off
setlocal
cd /d "%~dp0"

echo ========================================
echo Compilando SL-P3-02
echo ========================================

where javac >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se encuentra javac.
    echo Instala JDK 25 y configura el PATH.
    exit /b 1
)

if exist "out" rmdir /s /q "out"
mkdir "out"

rem Crear lista de archivos Java
dir /s /b "src\*.java" > "fuentes.tmp"

rem Compilar clases Java
javac --release 25 -encoding UTF-8 -cp "lib/*" -d "out" "@fuentes.tmp"

if errorlevel 1 (
    echo ERROR: La compilacion ha fallado.
    del "fuentes.tmp"
    exit /b 1
)

del "fuentes.tmp"

rem Copiar recursos que no sean Java
xcopy "src" "out" /E /I /Y /Q /EXCLUDE:%~dp0exclude-java.txt >nul

if errorlevel 2 (
    echo ERROR: No se han podido copiar los recursos.
    exit /b 1
)

echo.
echo Compilacion completada correctamente.
exit /b 0
