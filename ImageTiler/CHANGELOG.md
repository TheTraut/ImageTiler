# Changelog

All notable changes to ImageTiler will be documented in this file.

## [v2.0.0] - 2024-06-25

### 🆕 Major New Features

#### Comprehensive Settings System
- **Settings Dialog**: Full-featured configuration interface with organized sections
- **Persistent Settings**: All preferences automatically saved to `imagetiler.properties`
- **Default Directories**: Set preferred folders for images and PDF output
- **Multiple Paper Sizes**: Support for A4, A3, Letter, Legal, and Tabloid formats
- **Auto-save Settings**: Optional automatic saving of configuration changes

#### Visual Customization
- **Custom Grid Colors**: Choose your own colors for tile borders and overlays
- **Excluded Tile Colors**: Customize the appearance of manually excluded tiles
- **Grid Line Width**: Adjustable tile border thickness (1-10 pixels)
- **Grid Visibility Toggle**: Show/hide the entire grid while maintaining functionality
- **Tile Numbering Toggle**: Show/hide tile numbers in the preview

#### Manual Tile Selection
- **Click-to-Exclude**: Click any tile in the preview to exclude it from printing
- **Visual Feedback**: Excluded tiles appear with custom colors and overlays
- **Clear All Selections**: One-click removal of all manual exclusions
- **Smart Integration**: Works seamlessly with automatic blank tile detection

#### Enhanced User Experience
- **Real-time Updates**: Settings changes apply immediately to the preview
- **Color-coded Legend**: Dynamic legend showing current custom colors
- **Professional UI**: Improved interface design with organized sections
- **Intuitive Controls**: Better labeling and layout of all interface elements

### 🔧 Technical Improvements

#### Build System Enhancement
- **Automated Build Scripts**: `build.sh` for quick development builds
- **JAR Compilation**: `compile.sh` for creating distributable JAR files
- **Comprehensive .gitignore**: Proper exclusion of build artifacts and user files
- **Enhanced Documentation**: Complete README, developer docs, and changelog

#### Performance Optimizations
- **Tile Analysis Caching**: Avoid recalculating tile analysis on every redraw
- **Efficient Rendering**: Optimized paint operations and coordinate transformations
- **Memory Management**: Better handling of large images and tile data
- **Settings Persistence**: Lightweight property-based configuration storage

#### Code Quality
- **Modular Architecture**: Clear separation of concerns across components
- **Error Handling**: Comprehensive error checking and user feedback
- **Code Documentation**: Extensive inline comments and Javadoc
- **Type Safety**: Proper handling of settings types and conversions

### 🛠️ Infrastructure
- **Enhanced .gitignore**: Comprehensive exclusions for Java projects
- **Developer Documentation**: Complete technical guide for contributors
- **Build Scripts**: Automated compilation and JAR creation
- **Project Structure**: Organized codebase with clear component separation

---

## [v1.0.0] - Previous Version

### Core Features
- **Smart Tiling**: Automatic calculation of optimal tile layouts
- **Image Scaling**: Precise scaling with manual or calculated values
- **Image Rotation**: 90-degree rotation support
- **Live Preview**: Visual tile grid overlay with information display
- **Print Support**: Direct printing to installed printers
- **PDF Export**: Save tiled output as PDF files
- **Format Support**: PNG, JPG, and JPEG image formats

### Basic Functionality
- **File Selection**: Image file chooser with format filtering
- **Scale Calculation**: Automatic scale calculation from size inputs
- **Blank Tile Detection**: Automatic exclusion of empty tiles
- **Print Preview**: Information overlay showing tile count and layout
- **Error Handling**: Basic error messages and input validation

### Technical Foundation
- **Java Swing GUI**: Cross-platform desktop interface
- **Apache PDFBox**: PDF generation and manipulation
- **Coordinate System**: Proper handling of print coordinates
- **Image Processing**: Basic image loading and transformation

---

## Version History Summary

| Version | Release Date | Key Features |
|---------|--------------|-------------|
| v2.0.0  | 2024-06-25   | Settings system, visual customization, manual tile selection |
| v1.0.0  | Previous     | Core tiling functionality, basic GUI, PDF export |

## Upcoming Features (Roadmap)

### Planned for v2.1.0
- [ ] **Image Compression Options**: Control PDF file size
- [ ] **Batch Processing**: Process multiple images at once
- [ ] **Templates**: Save and load tiling configurations
- [ ] **Print Preview**: Enhanced preview with actual page margins

### Planned for v2.2.0
- [ ] **Custom Paper Sizes**: User-defined paper dimensions
- [ ] **Overlap Settings**: Configure tile overlap for easier assembly
- [ ] **Export Formats**: Additional output formats (TIFF, PNG tiles)
- [ ] **Advanced Scaling**: Non-uniform scaling options

### Long-term Goals
- [ ] **Plugin System**: Extensible architecture for custom features
- [ ] **Cloud Integration**: Save/sync settings across devices
- [ ] **Mobile Companion**: Mobile app for viewing tile assembly guides
- [ ] **Professional Features**: Crop marks, registration marks, color calibration

## Breaking Changes

### v2.0.0
- **Settings File Location**: Settings now saved to `imagetiler.properties` in the application directory
- **Default Colors**: Grid colors may appear different due to new customizable color system
- **JAR Distribution**: New build system creates different JAR structure

## Migration Guide

### Upgrading from v1.0.0 to v2.0.0
1. **Settings Reset**: First run will use default settings; customize via Settings dialog
2. **File Paths**: Re-configure preferred directories in Settings
3. **Visual Appearance**: Adjust colors and grid settings to your preference
4. **Build Process**: Use new `compile.sh` script for creating JAR files

## Contributors

- **Primary Development**: Core application and features
- **Settings System**: Comprehensive configuration management
- **UI/UX Design**: Interface improvements and user experience
- **Documentation**: Complete documentation overhaul
- **Build System**: Automated compilation and distribution

---

*For technical details and development information, see [DEVELOPER.md](DEVELOPER.md)*
