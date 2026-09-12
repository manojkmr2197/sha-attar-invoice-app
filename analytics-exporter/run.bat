@echo off
REM Firebase CSV Exporter - Windows Batch Script
REM This script compiles and runs the Firebase CSV exporter

echo.
echo ====================================================
echo  Firebase to CSV Analytics Exporter
echo ====================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo.
    echo Install Maven from: https://maven.apache.org/download.cgi
    echo Or use: choco install maven
    echo.
    pause
    exit /b 1
)

REM Check if serviceAccountKey.json exists
if not exist "serviceAccountKey.json" (
    echo [ERROR] serviceAccountKey.json not found!
    echo.
    echo Please:
    echo 1. Go to: https://console.firebase.google.com/
    echo 2. Select project: sha-attar-invoice
    echo 3. Settings ^> Service Accounts
    echo 4. Click "Generate New Private Key"
    echo 5. Save the file as: serviceAccountKey.json in this folder
    echo.
    pause
    exit /b 1
)

echo [1/3] Cleaning and compiling...
call mvn clean compile
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Running Firebase CSV Exporter...
call mvn exec:java -Dexec.mainClass="FirebaseCsvExporter"
if %errorlevel% neq 0 (
    echo [ERROR] Execution failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Done! Check output folder for CSV files
echo.
echo ====================================================
echo  CSV files generated in: output\
echo ====================================================
echo.
pause
