@echo off
REM ImageTiler JAR Build Script for Windows
REM This script compiles the application and creates a standalone JAR file

echo Building ImageTiler JAR...

REM Check if Java is installed
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java compiler (javac) not found. Please install Java JDK.
    pause
    exit /b 1
)

REM Create build directory
if not exist "build" mkdir build

REM Compile the application
echo Compiling Java sources...
javac -cp "lib/*;." -d build src/*.java

if %errorlevel% neq 0 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!

REM Extract dependencies
echo Extracting dependencies...
cd build
for %%f in (..\lib\*.jar) do (
    echo Extracting %%f...
    jar xf "%%f"
)

REM Remove META-INF to avoid conflicts
if exist "META-INF" rmdir /s /q META-INF

REM Create manifest
echo Manifest-Version: 1.0 > manifest.txt
echo Main-Class: Main >> manifest.txt
echo. >> manifest.txt

REM Create JAR
echo Creating JAR file...
jar cfm ..\ImageTiler.jar manifest.txt *

cd ..

if %errorlevel% equ 0 (
    echo ✅ JAR creation successful!
    echo JAR file created: ImageTiler.jar
    
    REM Ask user if they want to run the JAR
    set /p choice="Do you want to run the JAR now? (y/n): "
    if /i "%choice%"=="y" (
        echo Starting ImageTiler from JAR...
        java -jar ImageTiler.jar
    )
) else (
    echo ❌ JAR creation failed!
    pause
    exit /b 1
)

pause
