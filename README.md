# ImageTiler

A powerful Java desktop application that automatically divides large images into printable tiles that fit on standard paper sizes. Perfect for printing posters, technical drawings, or artwork on regular-sized printer paper with advanced customization options.

## ✨ Features

### Core Functionality
- **🧩 Smart Tiling**: Automatically calculates optimal tile layout (portrait vs landscape) to minimize total pages
- **📏 Image Scaling**: Scale images up or down before tiling with precise control
- **🔄 Image Rotation**: Rotate images in 90-degree increments
- **👀 Live Preview**: Visual preview showing customizable tile grid with optional numbering
- **🖨️ Print Support**: Direct printing to any installed printer
- **📄 PDF Export**: Save tiled output as PDF with user-selectable location
- **🖼️ Multiple Formats**: Supports PNG, JPG, and JPEG image files

### Advanced Features
- **⚙️ Comprehensive Settings System**: Customize colors, grid appearance, and behavior
- **🎨 Visual Customization**: Custom colors for grid lines and excluded tiles
- **🎯 Manual Tile Selection**: Click tiles to exclude them from printing
- **📊 Smart Analysis**: Automatically excludes blank tiles to save paper
- **💾 Persistent Preferences**: Settings automatically save between sessions
- **🔧 Multiple Paper Sizes**: Support for A4, A3, Letter, Legal, and Tabloid sizes

## How to Use

### Quick Start
1. **Select Image**: Click "Select Image" to choose your image file
2. **Adjust Scale**: (Optional) Enter original and new sizes to calculate scale, or manually set scale
3. **Rotate**: (Optional) Click "Rotate Image" to rotate in 90° increments
4. **Customize**: (Optional) Click "Settings" to customize colors, grid appearance, and preferences
5. **Manual Selection**: (Optional) Click individual tiles in the preview to exclude them from printing
6. **Preview**: The main panel shows your image with customizable tile grid overlay and information
7. **Export**: Choose "Print Image" for direct printing or "Save to PDF" for PDF export

### Scale Calculation
- Enter the **original size** (current size of your image in inches)
- Enter the **new size** (desired size in inches)
- Click "Calculate Scale" to automatically set the scale factor
- Or manually enter a scale value (1.0 = original size, 2.0 = double size, 0.5 = half size)

### Settings System
Click the **Settings** button to access comprehensive customization options:

#### Default Directories
- **Image Directory**: Set your preferred folder for selecting images
- **PDF Directory**: Set your preferred folder for saving PDF files
- Browse buttons available for easy folder selection

#### Display Settings
- **Paper Size**: Choose from A4, A3, Letter, Legal, or Tabloid
- **Default Scale**: Set your preferred starting scale factor
- **Show Grid**: Toggle tile grid visibility on/off
- **Show Tile Numbers**: Toggle tile numbering display

#### Visual Customization
- **Grid Color**: Customize the color of tile borders and overlays
- **Excluded Tile Color**: Set the color for manually excluded tiles
- **Grid Line Width**: Adjust the thickness of tile borders (1-10 pixels)

#### General Preferences
- **Auto-save Settings**: Automatically save changes when modified
- **Confirm Overwrites**: Show confirmation when overwriting files

*All settings are automatically saved and restored when you restart the application.*

### Manual Tile Selection
- **Click any tile** in the preview to exclude it from printing
- **Excluded tiles** appear with your custom excluded color
- **Click again** to include the tile back
- **Clear Selections** button removes all manual exclusions
- Perfect for removing unwanted border areas or blank sections

### Preview Information
The preview panel displays:
- **Total Pages**: Number of sheets needed
- **Grid Layout**: Width × Height arrangement (e.g., "3 × 2" means 3 pages wide, 2 pages tall)
- **Page Size**: Actual dimensions of each printed page
- **Scale Factor**: Current scaling applied to the image
- **Paper Saved**: How many blank pages are automatically excluded
- **Manual Exclusions**: Count of tiles you've manually excluded
- **Color-coded Legend**: Shows your current custom colors

## Technical Details

- **Target Paper Size**: A4 (8.27" × 11.69")
- **Built With**: Java Swing for GUI, Apache PDFBox for PDF generation
- **Image Formats**: PNG, JPG, JPEG
- **PDF Output**: Each tile becomes a separate page in the PDF

## Building and Running

### Prerequisites
- Java 8 or higher
- Apache PDFBox library (included in `lib/` directory)

### Easy Build & Run (Recommended)

#### Using the Build Script
```bash
cd ImageTiler
./build.sh
```

#### Using the Compile Script (JAR Creation)
```bash
cd ImageTiler
./compile.sh
```
This creates `ImageTiler.jar` which can be:
- Run with `java -jar ImageTiler.jar`
- Double-clicked to run (if Java is properly configured)
- Distributed as a standalone application

### Manual Building

#### macOS/Linux
```bash
cd ImageTiler
javac -cp "lib/*" -d build src/*.java
java -cp "lib/*:build" Main
```

#### Windows
```cmd
cd ImageTiler
javac -cp "lib/*" -d build src/*.java
java -cp "lib/*;build" Main
```

### Creating Executable JAR
```bash
cd ImageTiler
javac -cp "lib/*" -d build src/*.java
jar cfm ImageTiler.jar MANIFEST.MF -C build . -C lib .
```

## Project Structure

```
ImageTiler/
├── src/
│   ├── Main.java           # Main GUI application
│   ├── ImagePanel.java     # Custom panel for image display with tile overlay
│   ├── TileCalculator.java # Logic for optimal tile layout calculation
│   ├── TilePrinter.java    # Handles printing and PDF generation
│   ├── ScaleCalculator.java # Simple scale calculation utility
│   ├── Settings.java       # Settings management and persistence
│   └── SettingsDialog.java # Settings configuration dialog
├── lib/
│   └── pdfbox-app-3.0.2.jar # Apache PDFBox library
├── build/                  # Compiled class files (auto-generated)
├── build.sh               # Quick build and run script
├── compile.sh             # JAR compilation script
├── MANIFEST.MF            # JAR manifest file
├── .gitignore             # Git ignore configuration
└── README.md              # This documentation
```

## Recent Improvements

### Core Features
- ✅ Fixed PDF generation coordinate system issues
- ✅ Added file chooser for PDF save location
- ✅ Improved error handling and user feedback
- ✅ Enhanced preview with tile numbering and information overlay
- ✅ Better input validation for scale and size fields
- ✅ Confirmation dialogs for file overwriting
- ✅ More intuitive GUI layout and labeling

### Advanced Features (Latest)
- 🆕 **Comprehensive Settings System**: Complete user preferences management
- 🆕 **Visual Customization**: Custom colors for grid lines and excluded tiles
- 🆕 **Manual Tile Selection**: Click-to-exclude tiles functionality
- 🆕 **Multiple Paper Sizes**: Support for A4, A3, Letter, Legal, and Tabloid
- 🆕 **Persistent Settings**: Automatic save/restore of user preferences
- 🆕 **Grid Visibility Toggle**: Hide/show grid while maintaining functionality
- 🆕 **Enhanced Build System**: Automated compilation and JAR creation scripts
- 🆕 **Smart Paper Saving**: Automatic detection and exclusion of blank tiles
- 🆕 **Professional UI**: Color-coded legend and intuitive interface design

## Use Cases

- **Art Projects**: Print large artwork or photography on multiple sheets
- **Technical Drawings**: Print engineering drawings, blueprints, or schematics
- **Posters**: Create large posters from digital images
- **Maps**: Print detailed maps that are too large for single-page printing
- **Educational Materials**: Create large visual aids for presentations

## Tips for Best Results

1. **High Resolution**: Use high-resolution images for better print quality
2. **Plan Assembly**: Note the tile numbers in the preview for easier assembly
3. **Test Print**: Try a small scale first to verify the output looks correct
4. **Paper Alignment**: Ensure consistent paper orientation when printing multiple pages
5. **Overlap**: Consider leaving small margins when cutting/assembling for overlap
