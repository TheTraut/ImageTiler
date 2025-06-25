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
