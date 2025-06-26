# Developer Documentation

This document provides technical details for developers who want to understand, modify, or extend ImageTiler.

## Architecture Overview

ImageTiler follows a modular design with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Main.java   │───▶│  ImagePanel     │───▶│ TileCalculator  │
│   (GUI Control) │    │  (Display)      │    │  (Logic)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  SettingsDialog │    │   Settings      │    │  TilePrinter    │
│  (UI Config)    │───▶│ (Persistence)   │    │  (Output)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Core Components

### Main.java
**Purpose**: Application entry point and main GUI controller
**Key Responsibilities**:
- Window creation and layout management
- Event handling for UI controls
- Coordination between components
- File selection dialogs

**Key Methods**:
- `createAndShowGUI()`: Sets up the main window
- `selectImage()`: Handles image file selection
- `printImage()` / `saveToPDF()`: Initiates output operations

### ImagePanel.java
**Purpose**: Custom component for image display and tile visualization
**Key Features**:
- Real-time tile grid overlay
- Mouse interaction for tile selection
- Settings-aware visual rendering
- Performance optimization with caching

**Key Methods**:
- `paintComponent()`: Main rendering logic
- `handleTileClick()`: Processes user tile selections
- `getCachedNonBlankTiles()`: Optimized tile analysis
- `refreshDisplay()`: Updates display after settings changes

**Performance Features**:
- Tile analysis caching to avoid recalculation
- Efficient redraw only when necessary
- Optimized coordinate transformations

### TileCalculator.java
**Purpose**: Core tiling logic and calculations
**Key Algorithms**:
- Optimal layout calculation (portrait vs landscape)
- Tile positioning and sizing
- Blank tile detection using image analysis

**Key Methods**:
- `calculateOptimalTiling()`: Determines best tile arrangement
- `getNonBlankTiles()`: Analyzes which tiles contain content
- `isBlankTile()`: Determines if a tile area is effectively empty

**Algorithm Details**:
```java
// Blank detection uses average brightness threshold
double avgBrightness = calculateAverageBrightness(tileImage);
boolean isBlank = avgBrightness > BLANK_THRESHOLD && 
                  hasLowContrast(tileImage);
```

### Settings.java
**Purpose**: Application configuration management
**Design Pattern**: Singleton for global access
**Storage**: Java Properties file (`imagetiler.properties`)

**Key Features**:
- Type-safe getters/setters
- Automatic default value handling
- Optional auto-save functionality
- Color serialization/deserialization

**Settings Categories**:
- **Directories**: Default paths for file operations
- **Display**: Paper sizes, scale, grid visibility
- **Visual**: Colors, line widths, numbering
- **Behavior**: Auto-save, confirmations

### SettingsDialog.java
**Purpose**: User interface for configuration
**UI Framework**: Java Swing with GridBagLayout

**Key Features**:
- Organized sections with titled borders
- Color picker integration
- Directory browsing
- Real-time preview updates

### TilePrinter.java
**Purpose**: Output generation (printing and PDF)
**Dependencies**: Apache PDFBox for PDF generation

**Key Methods**:
- `printTiledImageWithSelection()`: Direct printer output
- `saveTiledImageToPDFWithSelection()`: PDF file generation
- `drawTileToGraphics()`: Core tile rendering logic

## Adding New Features

### Adding New Settings

1. **Add to Settings.java**:
```java
// Add default constant
public static final YourType DEFAULT_YOUR_SETTING = defaultValue;

// Add getter
public YourType getYourSetting() {
    return YourType.valueOf(properties.getProperty("yourSetting", 
                           DEFAULT_YOUR_SETTING.toString()));
}

// Add setter
public void setYourSetting(YourType value) {
    properties.setProperty("yourSetting", value.toString());
    autoSave();
}
```

2. **Add to SettingsDialog.java**:
```java
// Add UI component in initializeComponents()
private JComponent yourSettingComponent;

// Add to layout in layoutComponents()
// Add event handling in setupEventHandlers()
// Add to loadCurrentSettings() and saveAndClose()
```

3. **Use in ImagePanel.java**:
```java
// Access setting
YourType value = settings.getYourSetting();
// Use in paintComponent() or other methods
```

### Adding New Paper Sizes

1. **Extend PaperSize enum** in Settings.java:
```java
NEW_SIZE(widthInches, heightInches, "Display Name")
```

2. **Update TileCalculator.java** if special handling needed

### Adding New Output Formats

1. **Extend TilePrinter.java** with new methods
2. **Add UI controls** in Main.java
3. **Update file filters** as needed

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (`ImagePanel`)
- **Methods**: camelCase (`calculateOptimalTiling`)
- **Variables**: camelCase (`selectedTiles`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_GRID_COLOR`)

### Documentation Standards
- **Public methods**: Always include Javadoc
- **Complex algorithms**: Inline comments explaining logic
- **Settings**: Document default values and valid ranges

### Error Handling
- Use try-catch blocks for file operations
- Show user-friendly error messages via JOptionPane
- Log errors to System.err for debugging

## Testing Guidelines

### Manual Testing Checklist
- [ ] Load different image formats (PNG, JPG, JPEG)
- [ ] Test various image sizes and aspect ratios
- [ ] Verify scaling calculations
- [ ] Test all paper size options
- [ ] Verify settings persistence
- [ ] Test manual tile selection
- [ ] Verify PDF output quality
- [ ] Test printing functionality

### Performance Testing
- [ ] Large images (>50MB)
- [ ] High tile counts (>100 tiles)
- [ ] Rapid settings changes
- [ ] Memory usage during extended use

## Build System

### Prerequisites
- **Java Development Kit (JDK) 11+**: Full JDK required, not just JRE
  - Must include `javac` (compiler) and `jar` (archiver) tools
  - JDK bin directory must be in your system PATH
- **Apache PDFBox**: Automatically included via Git repository

### Scripts Overview
- **build.bat / build.sh**: Quick development build and run
- **build-jar.bat / build-jar.sh**: Production JAR creation with dependencies

### Directory Structure
```
ImageTiler/imagetiler/          # ← Main build directory
├── src/                        # Source files
├── lib/                        # Dependencies
│   └── pdfbox-app-3.0.5.jar  # ← Required dependency
├── build.bat                   # Windows build script
├── build-jar.bat              # Windows JAR creation
└── MANIFEST.MF                 # JAR manifest
```

### Dependencies
- **PDFBox 3.0.5**: PDF generation and manipulation
  - **Location**: `lib/pdfbox-app-3.0.5.jar`
  - **Git Status**: Tracked in repository (exception in .gitignore)
  - **Size**: ~13.5 MB
- **Java Swing**: GUI framework (built-in)
- **Java AWT**: Graphics and printing (built-in)

### Build Process

#### Compilation
```bash
# Windows
javac -cp "lib\pdfbox-app-3.0.5.jar" src\*.java

# Unix/Linux/macOS
javac -cp "lib/pdfbox-app-3.0.5.jar" src/*.java
```

#### JAR Creation Steps
1. **Compile source files** with PDFBox in classpath
2. **Extract PDFBox JAR** to temporary directory
3. **Copy compiled classes** to temporary directory
4. **Create final JAR** with MANIFEST.MF
5. **Cleanup** temporary files

### JAR Structure
```
ImageTiler.jar
├── *.class                     # Compiled application classes
├── org/apache/pdfbox/          # PDFBox classes
├── org/apache/fontbox/         # FontBox classes (PDFBox dependency)
├── org/apache/commons/         # Commons classes (PDFBox dependency)
├── META-INF/
│   ├── MANIFEST.MF             # JAR manifest with Main-Class
│   └── services/               # Service provider configurations
└── (other PDFBox dependencies)
```

### Git Dependency Management

**.gitignore Configuration:**
```gitignore
# Ignore all JAR files
*.jar
# Exception: Allow tracking of PDFBox dependency
!lib/pdfbox-app-3.0.5.jar
```

**Benefits:**
- ✅ No manual dependency downloads required
- ✅ Consistent builds across environments
- ✅ Version-locked dependency (3.0.5)
- ✅ Simplified setup for contributors

### Troubleshooting Builds

#### Common Issues

**"'javac' is not recognized"**
- **Cause**: JDK not installed or not in PATH
- **Solution**: Install full JDK and add bin directory to PATH

**"'jar' is not recognized"**
- **Cause**: JDK tools not in PATH
- **Solution**: Ensure JDK bin directory (not just JRE) is in PATH

**"package org.apache.pdfbox.pdmodel does not exist"**
- **Cause**: PDFBox JAR not found or not in classpath
- **Solution**: Verify `lib/pdfbox-app-3.0.5.jar` exists

**"Unable to access jarfile ImageTiler.jar"**
- **Cause**: JAR creation failed
- **Solution**: Check compilation errors, verify dependencies

#### Verification Commands
```bash
# Check JDK installation
java -version      # Should show Java runtime
javac -version     # Should show Java compiler
jar --version      # Should show JAR archiver

# Check dependency
dir lib\pdfbox-app-3.0.5.jar     # Windows
ls lib/pdfbox-app-3.0.5.jar      # Unix/Linux/macOS

# Verify classpath
javac -cp "lib/pdfbox-app-3.0.5.jar" -verbose src/Main.java
```

## Performance Considerations

### Memory Management
- Image objects are cached but can be large
- Tile analysis results are cached per scale/rotation
- Settings are lightweight and singleton

### Optimization Opportunities
- **Image Scaling**: Pre-scale images for display vs. processing
- **Tile Analysis**: Parallel processing for large images
- **UI Updates**: Debounce rapid setting changes

## Contributing

1. **Fork** the repository
2. **Create feature branch** (`feature/your-feature-name`)
3. **Follow code style** guidelines
4. **Add documentation** for new features
5. **Test thoroughly** with various inputs
6. **Submit pull request** with clear description

## Common Issues and Solutions

### Memory Issues
- **Problem**: OutOfMemoryError with large images
- **Solution**: Increase JVM heap size: `java -Xmx2g -jar ImageTiler.jar`

### Print Quality
- **Problem**: Blurry output
- **Solution**: Use higher resolution source images, verify DPI settings

### PDF Size
- **Problem**: Large PDF files
- **Solution**: Consider image compression options in future versions

### Settings Not Saving
- **Problem**: Settings reset on restart
- **Solution**: Check file permissions in application directory
