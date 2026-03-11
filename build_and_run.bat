@echo off
REM ─────────────────────────────────────────────────────────────────
REM  Library Management System - Build & Run Script (Windows)
REM ─────────────────────────────────────────────────────────────────

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "LIB_DIR=%SCRIPT_DIR%lib"
set "SRC_DIR=%SCRIPT_DIR%src"
set "OUT_DIR=%SCRIPT_DIR%out"
set "JAR_NAME=LibraryManagement.jar"
set "SQLITE_JAR=%LIB_DIR%\sqlite-jdbc.jar"

REM ── Check Java ──────────────────────────────────────────────────
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not on PATH.
    echo Please install JDK 17+ from https://adoptium.net/
    pause
    exit /b 1
)

javac -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: javac not found. Please install JDK (not just JRE).
    pause
    exit /b 1
)

REM ── Check SQLite JDBC ───────────────────────────────────────────
REM Using 3.36.0.3 - versions 3.40+ require SLF4J which is not bundled
if not exist "%SQLITE_JAR%" (
    echo.
    echo Downloading SQLite JDBC driver (v3.36.0.3^)...
    mkdir "%LIB_DIR%" 2>nul

    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/xerial/sqlite-jdbc/releases/download/3.36.0.3/sqlite-jdbc-3.36.0.3.jar' -OutFile '%SQLITE_JAR%'" 2>nul
    if errorlevel 1 (
        echo Could not auto-download. Please download manually from:
        echo   https://github.com/xerial/sqlite-jdbc/releases/tag/3.36.0.3
        echo Rename the file to sqlite-jdbc.jar and place it in: %LIB_DIR%
        pause
        exit /b 1
    )
    echo Downloaded successfully.
)

REM ── Compile ─────────────────────────────────────────────────────
echo Compiling...
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\sources.txt"
javac -source 8 -target 8 -cp "%SQLITE_JAR%" -d "%OUT_DIR%" @"%TEMP%\sources.txt"
if errorlevel 1 (
    echo Compilation FAILED.
    pause
    exit /b 1
)
echo Compilation successful.

REM ── Package fat JAR ─────────────────────────────────────────────
echo Packaging JAR...
cd /d "%OUT_DIR%"
jar xf "%SQLITE_JAR%"
if exist module-info.class del module-info.class
cd /d "%SCRIPT_DIR%"
jar cfe "%JAR_NAME%" library.Main -C "%OUT_DIR%" .
echo JAR created: %JAR_NAME%

REM ── Run ─────────────────────────────────────────────────────────
echo Launching Library Management System...
java -jar "%JAR_NAME%"
pause
