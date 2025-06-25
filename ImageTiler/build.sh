#!/bin/bash

# ImageTiler Build Script
# This script compiles and runs the ImageTiler application

echo "Building ImageTiler..."

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo "Error: Java compiler (javac) not found. Please install Java JDK."
    exit 1
fi

# Compile the application
echo "Compiling Java sources..."
javac -cp "lib/*:." src/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Ask user if they want to run the application
    read -p "Do you want to run ImageTiler now? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Starting ImageTiler..."
        java -cp "lib/*:src:." Main
    fi
else
    echo "❌ Compilation failed!"
    exit 1
fi
