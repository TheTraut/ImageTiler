#!/bin/bash

# ImageTiler Build Script
# This script compiles and runs the ImageTiler application

echo "Building ImageTiler..."

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

# Copy resources to build directory
echo "Copying resources..."
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* build/
    echo "✅ Resources copied (including calibration image)"
else
    echo "⚠️  Warning: src/main/resources directory not found"
fi

# Compile the application
echo "Compiling Java sources..."
javac -cp "lib/*:build:." -d build src/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Ask user if they want to run the application
    read -p "Do you want to run ImageTiler now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Starting ImageTiler..."
        java -cp "lib/*:build:." Main
    fi
else
    echo "❌ Compilation failed!"
    exit 1
fi
