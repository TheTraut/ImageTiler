# ImageTiler Calibration QA Testing Checklist

## Step 10: QA and Iterative Testing

### Testing Overview
This document outlines the systematic testing process for the ImageTiler calibration functionality, including printing calibration sheets on different printers, running the measurement helper, and confirming scale accuracy within tolerance.

---

## 1. Print Calibration Sheet on Different Printers

### Printer Test Matrix
Test the calibration sheet on various printer types to ensure consistent results:

#### Printer Types to Test:
- [ ] **Inkjet Printer** (Consumer grade)
- [ ] **Laser Printer** (Office grade)  
- [ ] **Photo Printer** (High-resolution)
- [ ] **All-in-One Printer** (Multi-function)

#### Test Procedure for Each Printer:
1. **Launch ImageTiler**:
   ```bash
   java -jar ImageTiler.jar
   ```

2. **Load Calibration Image**:
   - File → Open → Select `calibration_rectangle.png` OR `calibration_reference.png`
   - Verify the calibration rectangle displays correctly

3. **Access Measurement Helper**:
   - Tools → Measurement Helper (or equivalent menu option)
   - Verify dialog opens properly

4. **Print Reference Page**:
   - Click "1. Print Reference Page" button
   - **CRITICAL**: Ensure printer settings are:
     - Scale: 100% (no scaling)
     - Page setup: Default margins
     - Quality: Normal/Standard (not draft)
   - Document and record printer model and settings used

5. **Physical Verification**:
   - Measure printed rectangle with precise ruler
   - Expected size: **4.00" × 3.00"** exactly
   - Document actual measurements for each printer

---

## 2. Run Measurement Helper Tests

### Functional Testing

#### Test Case 1: Normal Flow
- [ ] **Step 1 Display**: Verify instructions are clear and expected size is shown (4.00" × 3.00")
- [ ] **Print Function**: Confirm print dialog appears and page prints
- [ ] **Step 2 Transition**: Verify UI transitions to measurement input step
- [ ] **Input Validation**: Test with valid measurements (e.g., 3.95" × 2.98")
- [ ] **Step 3 Completion**: Verify calibration results display correctly

#### Test Case 2: Edge Cases
- [ ] **Invalid Input**: Test negative numbers, zero, non-numeric input
- [ ] **Extreme Values**: Test very large/small measurements
- [ ] **Cancel Operation**: Test canceling at each step
- [ ] **Dialog Reopening**: Test opening helper multiple times

#### Test Case 3: Error Handling
- [ ] **Print Failure**: Test when printer is unavailable
- [ ] **Missing Calibration Image**: Test when calibration files are missing
- [ ] **Resource Loading**: Verify calibration image loads from correct location

---

## 3. Scale Accuracy and Tolerance Testing

### Tolerance Calculations

The system uses this calibration formula:
```java
// From GridParameters.java
float widthScale = CALIBRATION_RECTANGLE_WIDTH_INCHES / measuredWidthInches;
float heightScale = CALIBRATION_RECTANGLE_HEIGHT_INCHES / measuredHeightInches;
return (widthScale + heightScale) / 2.0f;
```

### Acceptable Tolerance Ranges

#### Measurement Accuracy:
- **Expected**: 4.00" × 3.00"
- **Tolerance**: ±0.02" (±0.5mm) for consumer printers
- **Precision**: ±0.01" (±0.25mm) for professional printers

#### Scale Factor Validation:
- **Ideal Scale**: 1.000
- **Acceptable Range**: 0.990 - 1.010 (±1%)
- **Warning Range**: 0.980 - 1.020 (±2%)
- **Error Range**: Outside ±2%

### Test Scenarios:

#### Scenario A: Perfect Calibration
- Measured: 4.00" × 3.00"
- Expected Scale: 1.000
- **Status**: ✅ PASS

#### Scenario B: Slight Under-scaling
- Measured: 3.98" × 2.97"
- Expected Scale: ~1.006
- **Status**: ✅ PASS (within tolerance)

#### Scenario C: Slight Over-scaling  
- Measured: 4.02" × 3.02"
- Expected Scale: ~0.995
- **Status**: ✅ PASS (within tolerance)

#### Scenario D: Significant Error
- Measured: 3.85" × 2.85"
- Expected Scale: ~1.039
- **Status**: ⚠️ WARNING (requires investigation)

---

## 4. User Interface and Experience Testing

### UI/UX Checklist:
- [ ] **Instructions Clarity**: Are step-by-step instructions easy to follow?
- [ ] **Visual Design**: Is the calibration rectangle clearly visible when printed?
- [ ] **Error Messages**: Are error messages helpful and actionable?
- [ ] **Progress Indicators**: Is the current step clearly indicated?
- [ ] **Results Display**: Are calibration results clearly presented?

### UI Improvements Completed ✅:

#### New Organized Interface:
1. **Tabbed Layout**: 
   - 📷 Image: Load, rotate, clear selections
   - 📏 Scale: Scale factor input and calculations  
   - 🔧 Tools: Calibration, size prediction, settings
   - 🖨️ Output: Print and PDF export

2. **Better Visual Organization**:
   - Each section has clear descriptions
   - Consistent button styling with emoji icons
   - Proper spacing and grouping
   - Larger window size (1200x800) for better readability

3. **Enhanced User Experience**:
   - Clear workflow progression
   - Contextual help text in each section
   - Better button labeling with icons
   - Improved status bar messages

---

## 5. Automated Testing Script

### Test Script: `test_calibration.sh`

```bash
#!/bin/bash
echo "=== ImageTiler Calibration QA Test ==="

# Test 1: Check calibration files exist
echo "Checking calibration files..."
files=("calibration_rectangle.png" "calibration_reference.png" "calibration_rectangle.svg")
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file found"
    else
        echo "❌ $file missing"
    fi
done

# Test 2: Verify image dimensions
echo "Verifying calibration image dimensions..."
# This would use ImageMagick or similar tool to check dimensions

# Test 3: Launch application for manual testing
echo "Launching ImageTiler for manual testing..."
java -jar ImageTiler.jar &

echo "Manual testing checklist:"
echo "1. Load calibration image"
echo "2. Open Measurement Helper"
echo "3. Print reference page"
echo "4. Measure and input values"
echo "5. Verify scale calculation"
```

---

## 6. Results Documentation

### Test Results Template:

```
Date: ___________
Tester: ___________

Printer Information:
- Model: ___________
- Type: ___________
- Driver Version: ___________
- Settings Used: ___________

Measurements:
- Expected: 4.00" × 3.00"
- Measured: _____" × _____"
- Scale Factor: _______
- Tolerance Status: ___________

Issues Found:
- [ ] UI/UX issues
- [ ] Calculation errors  
- [ ] Print quality problems
- [ ] Other: ___________

Recommendations:
___________
```

---

## 7. Pass/Fail Criteria

### ✅ PASS Criteria:
- Calibration sheet prints correctly on all tested printers
- Measurement helper runs without errors
- Scale calculations are within ±2% tolerance
- UI is intuitive and error-free
- All edge cases handled gracefully

### ❌ FAIL Criteria:
- Print failures or incorrect scaling
- Application crashes or errors
- Scale calculations outside ±2% tolerance
- Confusing UI or poor user experience
- Critical edge cases not handled

---

## Next Steps After QA:
1. Document all findings
2. Address any issues found
3. Refine UI text based on user feedback
4. Update tolerance ranges if needed
5. Create user documentation
6. Prepare for release testing
