# ImageTiler

A powerful Java application for printing large images across multiple pages with precise scaling and alignment.

## Features

- **Smart Tiling**: Automatically calculates optimal tile layouts for large images
- **Printer Calibration**: Built-in calibration system for accurate physical dimensions
- **Manual Tile Selection**: Click to exclude specific tiles from printing
- **Visual Customization**: Customizable grid colors, line widths, and tile numbering
- **Multiple Paper Sizes**: Support for A4, A3, Letter, Legal, and Tabloid formats
- **PDF Export**: Save tiled output as PDF files for later printing
- **Settings Persistence**: All preferences automatically saved between sessions

## Getting Started

### Requirements
- Java 8 or higher
- A printer (for physical output)
- Image files in PNG, JPG, or JPEG format

### Quick Start
1. Launch ImageTiler
2. Load your image using "Load Image"
3. Set your desired scale or use auto-calculate
4. Optional: Use the calibration feature for precise measurements
5. Print directly or export as PDF

## Printer Calibration

ImageTiler includes a calibration system to ensure your printed tiles have accurate physical dimensions. This is especially important for large multi-page prints where small errors can compound.

### What is the Calibration Sheet?

The calibration sheet is a reference image containing a rectangle of known dimensions. When you print this sheet and measure the actual printed rectangle, ImageTiler can calculate your printer's true scaling behavior and compensate accordingly.

**Key Features:**
- Simple rectangle design with clearly marked dimensions
- No confusing grid patterns or multiple measurements needed
- Works with any printer and paper size
- One-time setup per printer

### How to Print and Use the Calibration Sheet

#### Step 1: Access Calibration
1. In ImageTiler, go to **Tools → Printer Calibration** (or click the calibration button)
2. The calibration dialog will open with a reference rectangle

#### Step 2: Print the Reference
1. Click **"Print Reference Page"** 
2. **Important**: Print at 100% scale (no printer scaling)
3. Use the same printer and settings you plan to use for your final prints
4. Wait for the page to finish printing

#### Step 3: Measure the Printed Rectangle
1. Place the printed sheet on a flat surface
2. Use a ruler to measure the rectangle's **width** and **height**
3. The calibration sheet shows the expected dimensions next to the rectangle
4. Measure in inches (or convert from your preferred units)

#### Step 4: Enter Measurements
1. In the calibration dialog, enter your measured width and height
2. Click **"Calculate Calibrated Scale"**
3. ImageTiler will display your printer's calibration factor

### How Calibration Improves Measurement Consistency

Without calibration, your printer might:
- Scale images slightly larger or smaller than expected
- Have different scaling behavior between width and height
- Vary between different print settings or paper types

**With calibration, ImageTiler:**
- **Compensates for printer scaling**: Automatically adjusts for your printer's actual output size
- **Ensures dimensional accuracy**: Your 10-inch measurement will actually print as 10 inches
- **Improves tile alignment**: Multiple pages align perfectly when assembled
- **Reduces waste**: Eliminates reprints due to size mismatches
- **Works across scale factors**: Accurate whether printing at 150% or 500% scale

### Calibration Best Practices

1. **Use the same settings**: Calibrate with the exact printer settings you'll use for final prints
2. **Measure carefully**: Small measurement errors significantly affect accuracy
3. **Re-calibrate when needed**: Recalibrate if you change printers, paper, or print settings
4. **Test with a small print**: Verify calibration with a small multi-tile test before large prints

### Example Calibration Workflow

```
Expected rectangle: 4.0 × 3.0 inches
Measured rectangle: 3.9 × 2.95 inches
Calculated scale factor: 1.026x

Result: ImageTiler will scale all future prints by 1.026x 
to compensate for your printer printing slightly smaller.
```

## Advanced Features

### Manual Tile Exclusion
Click any tile in the preview to exclude it from printing. Useful for:
- Removing tiles that are mostly blank
- Excluding unwanted border areas
- Creating custom print layouts

### Visual Customization
Access the Settings dialog to customize:
- Grid line colors and thickness
- Excluded tile appearance
- Tile numbering display
- Default paper sizes and directories

### PDF Export
Export your tiled layout as a PDF file:
- Each tile becomes a separate PDF page
- Maintains exact scaling and positioning
- Perfect for batch printing or sharing

## Technical Details

### Supported Formats
- **Input**: PNG, JPG, JPEG
- **Output**: Direct printing, PDF export

### Paper Sizes
- A4 (210 × 297 mm)
- A3 (297 × 420 mm)  
- Letter (8.5 × 11 inches)
- Legal (8.5 × 14 inches)
- Tabloid (11 × 17 inches)

### System Requirements
- **Java**: Version 8 or higher
- **Memory**: 512MB RAM minimum (more for large images)
- **Disk Space**: 50MB for application
- **Printer**: Any Java-compatible printer driver

## Building from Source

```bash
# Development build
./build.sh

# Create distributable JAR
./compile.sh
```

## License

[Add your license information here]

## Support

For issues, feature requests, or questions:
- Check the [CHANGELOG.md](CHANGELOG.md) for recent updates
- See [DEVELOPER.md](DEVELOPER.md) for technical details
- Create an issue on the project repository

---

*ImageTiler - Making large-scale printing simple and accurate.*
