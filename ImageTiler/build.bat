@echo off
REM ImageTiler Build Script for Windows
REM This script compiles and runs the ImageTiler application

echo Building ImageTiler...

REM Check if Java is installed
javac -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java compiler (javac) not found.
    echo Please download and install the Java JDK from:
    echo https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
    pause
    exit /b 1
)

REM Compile the application
echo Compiling Java sources...
javac -cp "lib/*;." src/*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
) else (
    echo Compilation successful!
    
    REM Ask user if they want to run the application
    set /p choice="Do you want to run ImageTiler now? (y/n): "
    if /i "%choice%"=="y" (
        echo Starting ImageTiler...
        java -cp "lib/*;src;." Main
    )
)
