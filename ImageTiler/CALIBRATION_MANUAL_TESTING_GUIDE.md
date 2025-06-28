# Calibration Sheet Manual Testing Guide

This guide provides comprehensive instructions for manually testing the calibration sheet printing functionality across different printers and PDF viewers to ensure the calibration image prints correctly.

## Overview

The calibration image is a 3300×2550 pixel reference sheet designed to help users accurately measure and calibrate their print scaling. This guide covers testing procedures to verify that:

1. The calibration image is correctly detected and processed
2. Printing produces accurate physical dimensions
3. PDF generation preserves the calibration grid
4. Different printers and viewers handle the calibration sheet consistently

## Prerequisites

### Required Materials
- Computer with ImageTiler installed
- Access to multiple printers (if available):
  - Inkjet printer (e.g., Epson, Canon, HP)
  - Laser printer (e.g., Brother, HP, Canon)
  - Large format printer (if available)
- Various PDF viewers installed:
  - Adobe Acrobat Reader
  - Browser-based PDF viewers (Chrome, Firefox, Safari)
  - OS native viewers (Preview on macOS, Edge on Windows)
- Measuring tools:
  - Ruler (metric and imperial)
  - Calipers for precise measurements
  - Grid transparency or overlay (optional)

### Test Image
- Use the calibration image located at: `src/main/resources/calibration/calibration.png`
- Verify the image dimensions are 3300×2550 pixels
- Confirm the image contains a precise grid pattern for measurement

## Test Procedures

### Test 1: Calibration Image Detection

**Purpose**: Verify that the application correctly identifies the calibration image and applies special handling.

**Steps**:
1. Launch ImageTiler
2. Load the calibration image (`calibration.png`)
3. Observe the application behavior

**Expected Results**:
- Application should detect this as a calibration image
- Special calibration handling should be activated
- Tile selection should be bypassed for calibration images
- Console output should show calibration detection messages

**Verification**:
- Check console logs for `[CALIBRATION]` messages
- Verify that manual tile selection is disabled/ignored
- Confirm that only one tile is selected regardless of tiling calculation

### Test 2: PDF Generation Testing

**Purpose**: Test PDF generation with calibration images across different scales and verify output quality.

#### Test 2a: Scale 1.0 PDF Generation

**Steps**:
1. Load calibration image in ImageTiler
2. Set scale to 1.0 (auto-fit to single page)
3. Use "Save as PDF" function
4. Save to a test location

**Expected Results**:
- PDF should contain exactly 1 page
- Page should contain the full calibration image
- Image should maintain aspect ratio
- No cropping or distortion should occur

**Verification Checklist**:
- [ ] PDF opens without errors
- [ ] Single page contains complete calibration grid
- [ ] No visible compression artifacts
- [ ] Grid lines are clear and straight
- [ ] Text/numbers in grid are legible

#### Test 2b: Scaled PDF Generation (Scale 2.0+)

**Steps**:
1. Load calibration image in ImageTiler
2. Set scale to 2.0 or higher
3. Generate PDF

**Expected Results**:
- PDF should still contain exactly 1 page (special calibration handling)
- Image should be scaled appropriately
- No tiling should occur for calibration images

**Verification**:
- [ ] PDF contains only 1 page despite large scale
- [ ] Calibration grid covers the entire page
- [ ] Scaling appears correct for the specified factor

### Test 3: Printer Testing

**Purpose**: Verify physical printing accuracy across different printer types.

#### Test 3a: Standard Inkjet Printer Testing

**Printer Types to Test**:
- Epson (EcoTank, Expression, etc.)
- Canon (PIXMA series)
- HP (OfficeJet, Envy, etc.)

**Test Procedure**:
1. Load calibration image at scale 1.0
2. Use "Print" function
3. Select target printer
4. Use default print settings initially
5. Print one copy
6. Measure the printed output

**Critical Measurements**:
- Overall dimensions of the printed sheet
- Grid spacing (should be 1cm if calibrated correctly)
- Margin sizes
- Line thickness and clarity

**Expected Results for Scale 1.0**:
- Printed image should fit on standard paper (A4/Letter)
- Grid squares should measure exactly 1cm × 1cm
- No distortion or stretching should occur
- Lines should be crisp and well-defined

#### Test 3b: Laser Printer Testing

**Follow the same procedure as 3a but note any differences in**:
- Line quality (often sharper than inkjet)
- Color reproduction (if using color laser)
- Paper handling differences
- Scaling accuracy

#### Test 3c: Different Paper Sizes

**Test on various paper sizes**:
- A4 (210 × 297 mm)
- Letter (8.5 × 11 inches)
- Legal (8.5 × 14 inches)
- A3 (if printer supports)

**Verification Points**:
- [ ] Image centers correctly on paper
- [ ] Maintains aspect ratio regardless of paper size
- [ ] Grid spacing remains consistent
- [ ] No clipping occurs at paper edges

### Test 4: PDF Viewer Compatibility

**Purpose**: Ensure calibration PDFs display correctly across different viewers.

#### Test 4a: Adobe Acrobat Reader

**Steps**:
1. Open calibration PDF in Adobe Acrobat Reader
2. View at 100% zoom
3. Test printing from Adobe Reader
4. Measure printed output

**Verification**:
- [ ] PDF displays correctly at 100% zoom
- [ ] Print preview shows correct dimensions
- [ ] Actual printout matches expected measurements
- [ ] No scaling warnings appear during print

#### Test 4b: Browser PDF Viewers

**Test in multiple browsers**:
- Google Chrome
- Mozilla Firefox
- Safari (macOS)
- Microsoft Edge

**For each browser**:
1. Open PDF in browser
2. Check display quality
3. Test browser's print function
4. Compare measurements

**Common Issues to Watch For**:
- Browser auto-scaling during print
- Different rendering quality
- Margin handling differences
- Print dialog scale settings

#### Test 4c: OS Native Viewers

**macOS Preview**:
1. Open PDF in Preview
2. Check "Actual Size" view
3. Test printing with "Scale to Fit" disabled

**Windows Photos/Edge**:
1. Open PDF in default Windows viewer
2. Verify scaling options
3. Print with correct settings

### Test 5: Measurement Verification

**Purpose**: Validate that printed calibration sheets provide accurate measurement references.

#### Physical Measurement Protocol

**Tools Needed**:
- Metric ruler (minimum 30cm)
- Digital calipers
- Magnifying glass (for fine details)

**Measurement Points**:
1. **Grid Spacing**: Measure distance between grid lines
   - Should be exactly 10mm (1cm) for properly calibrated output
   - Take measurements at multiple locations
   - Check both horizontal and vertical spacing

2. **Overall Dimensions**: Measure total image area
   - Record width and height
   - Compare to expected dimensions based on scale

3. **Line Quality**: Assess visual quality
   - Lines should be straight
   - Intersections should be precise
   - No bleeding or blurring

4. **Registration**: Check alignment
   - Grid should be square (90-degree angles)
   - No skewing or rotation
   - Consistent spacing throughout

#### Documentation Template

Create a measurement log for each test:

```
Test Date: ___________
Printer Model: _______________
Paper Type: _________________
Scale Setting: ______________

Measurements:
- Grid Spacing (H): _____ mm
- Grid Spacing (V): _____ mm
- Total Width: ______ mm
- Total Height: ______ mm
- Line Quality: _____________
- Overall Assessment: _______

Issues Found:
□ Scaling incorrect
□ Lines blurry
□ Grid distorted
□ Margins wrong
□ Other: ________________
```

### Test 6: Edge Cases and Error Conditions

**Purpose**: Test robustness and error handling.

#### Test 6a: Rotated Calibration Image

**Steps**:
1. Rotate the calibration image 90 degrees in an image editor
2. Save as new file with 2550×3300 dimensions
3. Load in ImageTiler
4. Verify detection and handling

**Expected Results**:
- Should still be detected as calibration image
- Rotation should be handled correctly
- Print output should maintain accuracy

#### Test 6b: Modified Calibration Image

**Test with slightly altered versions**:
- Image with different color space
- Compressed/JPEG version
- Image with added watermark
- Cropped version

**Verification**:
- Check which modifications break calibration detection
- Verify behavior when detection fails
- Ensure graceful fallback to normal processing

#### Test 6c: Print Driver Variations

**Test with different print drivers**:
- Standard printer drivers
- Universal/generic drivers
- Third-party drivers

**Check for**:
- Consistent scaling behavior
- Driver-specific quirks
- Quality differences

### Test 7: Multi-Platform Testing

**Purpose**: Verify consistent behavior across operating systems.

#### Test Platforms
- Windows 10/11
- macOS (latest version)
- Linux (Ubuntu/similar)

#### Platform-Specific Checks
- Java font rendering differences
- OS print dialog variations
- Default printer settings
- File path handling

### Troubleshooting Common Issues

#### Issue: Grid Not Measuring Correctly

**Possible Causes**:
- Printer scaling settings incorrect
- Driver auto-scaling enabled
- Wrong paper size selected
- Application scale setting incorrect

**Solutions**:
1. Check printer preferences for scaling options
2. Disable "Fit to Page" or similar options
3. Verify paper size matches print settings
4. Recalibrate application scale settings

#### Issue: PDF Not Displaying Correctly

**Possible Causes**:
- PDF viewer scaling
- Font/graphics rendering issues
- Corrupted PDF generation

**Solutions**:
1. Try different PDF viewer
2. Check PDF generation process
3. Verify source image integrity
4. Test with different PDF settings

#### Issue: Calibration Not Detected

**Possible Causes**:
- Image dimensions modified
- File corruption
- Unsupported image format

**Solutions**:
1. Verify image dimensions are exactly 3300×2550 or 2550×3300
2. Use original PNG format
3. Check console logs for detection messages
4. Reload image or restart application

## Test Results Documentation

### Results Summary Template

```
Testing Date: ___________
ImageTiler Version: _____
Java Version: __________

Printers Tested:
□ [Printer 1]: __________ - Status: ____
□ [Printer 2]: __________ - Status: ____
□ [Printer 3]: __________ - Status: ____

PDF Viewers Tested:
□ Adobe Acrobat Reader - Status: ____
□ Chrome Browser - Status: ____
□ Safari/Firefox - Status: ____
□ OS Native Viewer - Status: ____

Critical Issues Found:
1. ________________________
2. ________________________
3. ________________________

Recommendations:
1. ________________________
2. ________________________
3. ________________________

Overall Assessment: ________
```

### Success Criteria

**Calibration sheet printing is considered successful when**:
- ✅ Calibration image is correctly detected (100% of cases)
- ✅ PDF generation produces single-page output
- ✅ Printed grid spacing is accurate within ±2% tolerance
- ✅ Image quality is clear and usable for measurements
- ✅ Consistent behavior across tested printers (>90%)
- ✅ PDF viewers display correctly (100% of tested viewers)
- ✅ No critical errors or crashes during testing

### Failure Scenarios Requiring Investigation

**Immediate attention required if**:
- ❌ Calibration detection fails
- ❌ Grid measurements are off by >5%
- ❌ PDF generation creates multiple pages
- ❌ Image quality is too poor for measurement use
- ❌ Consistent failures across multiple printers
- ❌ Application crashes during calibration processing

## Conclusion

This manual testing guide ensures comprehensive validation of the calibration sheet functionality. Regular execution of these tests, especially after code changes affecting the calibration logic, will help maintain the accuracy and reliability of the ImageTiler calibration system.

For automated testing coverage, refer to the companion unit tests (`ImagePanelCalibrationTest.java`) and integration tests (`CalibrationPrintingIntegrationTest.java`).
