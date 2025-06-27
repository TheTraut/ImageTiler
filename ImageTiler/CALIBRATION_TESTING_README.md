# Calibration Testing Suite

This directory contains a comprehensive test suite for the ImageTiler calibration functionality, covering both automated and manual testing scenarios.

## Overview

The calibration testing suite verifies that:
1. `getSelectedTiles()` method correctly handles calibration images
2. `isCalibrationImage()` method accurately detects calibration resources
3. Printing and PDF generation work correctly with calibration images
4. Physical output maintains accurate measurements

## Test Files

### Automated Tests

#### Unit Tests
- **`ImagePanelCalibrationTest.java`** - Unit tests for `getSelectedTiles()` and `isCalibrationImage()` methods
  - Tests calibration image detection
  - Verifies single-tile behavior for calibration images
  - Tests manual selection bypass for calibration images
  - Validates dimension-based detection logic

#### Integration Tests
- **`CalibrationPrintingIntegrationTest.java`** - Integration tests for printing and PDF workflows
  - Simulates complete PDF generation process
  - Tests print preview rendering
  - Validates multi-scale behavior
  - Compares calibration vs normal image processing

#### Existing Tests
- **`CalibrationImageTest.java`** - Existing tests for calibration image loading
- **`ScaleCalculatorTest.java`** - Scale calculation tests

### Manual Testing
- **`CALIBRATION_MANUAL_TESTING_GUIDE.md`** - Comprehensive manual testing guide
  - Printer compatibility testing
  - PDF viewer testing
  - Physical measurement verification
  - Cross-platform testing procedures

### Test Runner
- **`run_calibration_tests.sh`** - Automated test execution script
  - Compiles and runs all automated tests
  - Provides colored output and summary
  - Handles dependency checking

## Running the Tests

### Quick Start
```bash
# Run all automated tests
./run_calibration_tests.sh
```

### Manual Execution
```bash
# Compile sources
javac -cp "lib/*:build:src" -d build src/*.java
javac -cp "lib/*:build:src" -d build/test src/test/java/*.java

# Run specific test class
java -cp "lib/*:build:build/test" org.junit.platform.console.ConsoleLauncher \
    --select-class ImagePanelCalibrationTest
```

### Prerequisites
- Java JDK 11 or later
- JUnit 5 (junit-platform-console-standalone JAR in lib/ directory)
- Apache PDFBox (should be in lib/ directory)

## Test Coverage

### Unit Test Coverage
✅ **`isCalibrationImage()` method**
- Calibration resource detection
- Rotated calibration images
- Normal image rejection
- Null image handling
- Dimension boundary testing

✅ **`getSelectedTiles()` method**
- Single tile return for calibration images
- Manual selection bypass
- Scale-independent behavior
- Comparison with normal image processing

### Integration Test Coverage
✅ **PDF Generation**
- Single page output for calibration images
- Scale 1.0 behavior
- Scaled output behavior (2.0+)
- PDF content verification

✅ **Print Simulation**
- Print preview rendering
- Multi-scale consistency
- Page existence validation

✅ **Workflow Testing**
- End-to-end calibration processing
- Normal vs calibration image comparison
- Special handling verification

## Expected Test Results

### Success Criteria
When all tests pass, you should see:
- ✅ All calibration images correctly detected
- ✅ Single tile selection for calibration images regardless of scale
- ✅ PDF generation produces exactly 1 page for calibration images
- ✅ Print simulation handles calibration images correctly
- ✅ Manual tile selections are bypassed for calibration images

### Key Behaviors Verified
1. **Calibration Detection**: Images with dimensions 3300×2550 or 2550×3300 are detected as calibration images
2. **Tile Override**: Calibration images always return a single tile (col=0, row=0, tileNumber=1)
3. **Selection Bypass**: Manual tile exclusions/inclusions are ignored for calibration images
4. **PDF Consistency**: PDFs contain exactly 1 page regardless of scale for calibration images
5. **Print Behavior**: Print workflows handle calibration images with special logic

## Manual Testing

After running automated tests, proceed with manual testing using the comprehensive guide:

1. **Read**: `CALIBRATION_MANUAL_TESTING_GUIDE.md`
2. **Test**: Different printers and PDF viewers
3. **Measure**: Physical output accuracy
4. **Document**: Results using provided templates

## Troubleshooting

### Common Issues

**Tests won't compile**
- Ensure Java JDK is installed
- Check that all JAR dependencies are in lib/ directory
- Verify classpath settings

**Tests fail with "calibration.png not found"**
- Confirm calibration image exists at: `src/main/resources/calibration/calibration.png`
- Verify file has correct dimensions (3300×2550 pixels)

**PDF tests fail**
- Ensure PDFBox JAR is in lib/ directory
- Check write permissions for temporary files
- Verify sufficient disk space

**Integration tests fail**
- Check that all main classes compile successfully
- Verify ImagePanel and TileCalculator classes are available
- Review console output for specific error messages

### Debug Information

The tests produce detailed console output including:
- `[DEBUG]` messages for general debugging
- `[CALIBRATION]` messages for calibration-specific logic
- JUnit assertions with descriptive failure messages
- File operation results and temporary file locations

## Contributing

When adding new calibration functionality:

1. **Add unit tests** to `ImagePanelCalibrationTest.java`
2. **Add integration tests** to `CalibrationPrintingIntegrationTest.java`
3. **Update manual testing guide** with new test procedures
4. **Run full test suite** to ensure no regressions
5. **Document expected behaviors** in this README

## Related Documentation

- `CALIBRATION_FIXES.md` - Implementation details for calibration fixes
- `QA_TESTING_CHECKLIST.md` - General QA procedures
- `DEVELOPER.md` - Development setup and guidelines
