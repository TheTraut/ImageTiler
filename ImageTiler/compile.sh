#!/bin/bash

# ImageTiler Compile Script
# Compiles Java sources and creates executable JAR

echo "🔨 Compiling ImageTiler..."
echo "=========================="

# Clean previous build
echo "🧹 Cleaning previous build..."
rm -rf build/*
rm -f ImageTiler.jar

# Create build directory if it doesn't exist
mkdir -p build

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

# Check if jar command is available
if ! command -v jar &> /dev/null; then
    echo "❌ Error: jar command not found."
    echo "The jar command is usually included with the JDK installation."
    echo "Please ensure you have the full JDK installed, not just the JRE."
    exit 1
fi

# Compile Java sources
echo "☕ Compiling Java sources..."
javac -cp "lib/*" -d build src/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    
    # Create JAR file
    echo "📦 Creating JAR file..."
    jar cfm ImageTiler.jar MANIFEST.MF -C build . -C lib .
    
    if [ $? -eq 0 ]; then
        echo "✅ JAR created successfully!"
        
        # Show file size
        JAR_SIZE=$(ls -lh ImageTiler.jar | awk '{print $5}')
        echo "📊 JAR size: $JAR_SIZE"
        
        echo ""
        echo "🎉 Build complete!"
        echo "📁 Output: ImageTiler.jar"
        echo ""
        echo "To run: java -jar ImageTiler.jar"
        echo "Or double-click the JAR file if Java is properly configured."
    else
        echo "❌ Failed to create JAR file!"
        exit 1
    fi
else
    echo "❌ Compilation failed!"
    exit 1
fi
