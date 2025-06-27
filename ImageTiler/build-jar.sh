#!/bin/bash

# ImageTiler JAR Build Script for Unix/Linux/macOS
# This script compiles the application and creates a standalone JAR file

echo "Building ImageTiler JAR..."

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo "❌ Error: Java compiler (javac) not found."
    echo "Please download and install the Java JDK from:"
    echo "  • Oracle JDK: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html"
    echo "  • OpenJDK (Adoptium): https://adoptium.net/"
    echo ""
    echo "On macOS, you can also install via Homebrew:"
    echo "  brew install openjdk@11"
    echo ""
    echo "On Ubuntu/Debian:"
    echo "  sudo apt update && sudo apt install openjdk-11-jdk"
    echo ""
    echo "On CentOS/RHEL/Fedora:"
    echo "  sudo yum install java-11-openjdk-devel"
    exit 1
fi

# Create build directory
mkdir -p build

# Compile the application
echo "Compiling Java sources..."
javac -cp "lib/pdfbox-app-3.0.5.jar" -d build src/*.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    echo "Please check that all source files are present and Java is properly installed."
    exit 1
fi

echo "✅ Compilation successful!"

# Extract dependencies
echo "Extracting dependencies..."
cd build

for jarfile in ../lib/*.jar; do
    echo "Extracting $jarfile..."
    jar xf "$jarfile"
done

# Remove META-INF to avoid conflicts
if [ -d "META-INF" ]; then
    rm -rf META-INF
fi

# Create manifest
cat > manifest.txt << EOF
Manifest-Version: 1.0
Main-Class: Main

EOF

# Create JAR with resources included
echo "Creating JAR file..."
jar cfm ../ImageTiler.jar manifest.txt * -C ../src/main/resources .
cd ..

if [ $? -ne 0 ]; then
    echo "❌ JAR creation failed!"
    echo "Please check that the jar command is available and working properly."
    exit 1
fi

echo "✅ JAR creation successful!"
echo "JAR file created: ImageTiler.jar"
echo ""

# Ask user if they want to run the JAR
read -p "Do you want to run the JAR now? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Starting ImageTiler from JAR..."
    java -jar ImageTiler.jar
fi
