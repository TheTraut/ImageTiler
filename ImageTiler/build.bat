@echo off
REM ImageTiler Build Script for Windows
REM This script compiles and runs the ImageTiler application

echo Building ImageTiler...

REM Check if Java is installed
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java compiler (javac) not found. Please install Java JDK.
    pause
    exit /b 1
)

REM Compile the application
echo Compiling Java sources...
javac -cp "lib/*;." src/*.java

if %errorlevel% equ 0 (
    echo ✅ Compilation successful!
    
    REM Ask user if they want to run the application
    set /p choice="Do you want to run ImageTiler now? (y/n): "
    if /i "%choice%"=="y" (
        echo Starting ImageTiler...
        java -cp "lib/*;src;." Main
    )
) else (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

pause
