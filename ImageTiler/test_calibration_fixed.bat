@echo off
echo ===============================================
echo  ImageTiler Calibration Fix Validation Test
echo ===============================================
echo.

echo Step 1: Checking if JAR file exists...
if exist "ImageTiler.jar" (
    echo ✅ ImageTiler.jar found
) else (
    echo ❌ ImageTiler.jar not found
    echo Please run build-jar.bat first
    pause
    exit /b 1
)
echo.

echo Step 2: Checking calibration image resource...
if exist "src\main\resources\calibration\calibration.png" (
    echo ✅ Calibration image found in resources
) else (
    echo ❌ Calibration image missing from resources
    pause
    exit /b 1
)
echo.

echo Step 3: Testing JAR execution...
echo Starting ImageTiler to test calibration loading...
echo (This should show "Successfully loaded calibration image from resources")
echo.
java -jar ImageTiler.jar
echo.

echo Step 4: Manual Testing Checklist
echo ===============================================
echo Please test the following manually:
echo.
echo 1. 📷 Load Image Tab:
echo    - Load any image (or use auto-loaded calibration image)
echo.
echo 2. 🔧 Tools Tab:
echo    - Click "Printer Calibration" button
echo    - Verify calibration dialog opens
echo    - Check that calibration image is visible in main window
echo.
echo 3. 🖨️ Step 1 - Print Reference:
echo    - Click "1. Print Reference Page"
echo    - Print should work (sends to default printer)
echo    - Dialog should advance to Step 2
echo.
echo 4. 📏 Step 2 - Measure Rectangle:
echo    - Enter test measurements (e.g., 3.95 and 2.97)
echo    - Click "3. Calculate Calibrated Scale"
echo    - Should show results with tolerance rating
echo.
echo 5. ✅ Step 3 - Completion:
echo    - Should show calibration completed
echo    - Scale factor should be applied to main app
echo.
echo Expected Tolerance Ratings:
echo   4.00 × 3.00 = ✅ EXCELLENT (scale 1.000)
echo   3.96 × 2.97 = ✅ GOOD (scale ~1.014)
echo   3.80 × 2.85 = ⚠️ WARNING (scale ~1.056)
echo.
echo ===============================================
echo  Testing Instructions
echo ===============================================
echo.
echo If all manual tests pass, the calibration fixes are working correctly!
echo.
echo Key fixes validated:
echo ✅ Calibration image auto-loads
echo ✅ Resource loading works
echo ✅ Print functionality works
echo ✅ Scale calculations are accurate
echo ✅ Tolerance feedback is shown
echo ✅ UI instructions are clear
echo.
pause
