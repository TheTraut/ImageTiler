@echo off
REM ImageTiler Build Script for Windows
REM This script compiles and runs the ImageTiler application

echo Building ImageTiler...

REM Check if Java is installed
javac -version >nul 2>&1
if errorlevel 1 goto :no_java

REM Create build directory
if not exist build mkdir build

REM Compile the application
echo Compiling Java sources...
javac -encoding UTF-8 -cp lib\pdfbox-app-3.0.5.jar -d build src\*.java
if errorlevel 1 goto :compile_failed

echo Compilation successful!

REM Copy resources to build directory
echo Copying resources...
if exist src\main\resources (
    xcopy /E /I /Y src\main\resources\* build\
    echo Resources copied successfully!
) else (
    echo Warning: No resources directory found at src\main\resources
)

echo.
set /p choice="Do you want to run ImageTiler now? (y/n): "
if /i "%choice%"=="y" (
    echo Starting ImageTiler...
    java -cp "lib\pdfbox-app-3.0.5.jar;build;." Main
)
goto :end

:no_java
echo Error: Java compiler (javac) not found.
echo Please download and install the Java JDK from:
echo https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
echo.
echo Alternative: You can also install OpenJDK from:
echo https://adoptium.net/
pause
exit /b 1

:compile_failed
echo Compilation failed!
echo Please check that all source files are present and Java is properly installed.
pause
exit /b 1

:end
pause
