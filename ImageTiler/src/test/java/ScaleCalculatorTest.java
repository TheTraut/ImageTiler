import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying scale calculations with known pixel distances
 * and checking that scale output matches expected values.
 */
public class ScaleCalculatorTest {
    
    // Known test values based on calibration image specifications
    private static final int CALIBRATION_WIDTH = 3300;
    private static final int CALIBRATION_HEIGHT = 2550;
    private static final int CALIBRATION_DPI = 300;
    
    @BeforeAll
    static void setup() {
        // Verify GridParameters constants are available for testing
        assertEquals(300, GridParameters.CALIBRATION_DPI, "Calibration DPI should be 300");
        assertEquals(142, GridParameters.GRID_SPACING, "Grid spacing should be 142 pixels per cm");
        assertEquals(1.0f, GridParameters.PHYSICAL_GRID_SPACING_CM, "Physical grid spacing should be 1 cm");
    }

    @Test
    @DisplayName("Test basic scale calculation with simple values")
    void testBasicScaleCalculation() {
        // Test the fundamental scale calculation: scale = newSize / originalSize
        float originalSize = 100.0f;
        float newSize = 150.0f;
        float expectedScale = 1.5f;
        
        float actualScale = ScaleCalculator.calculateScale(originalSize, newSize);
        
        assertEquals(expectedScale, actualScale, 0.001f, 
            "Basic scale calculation should return correct ratio");
    }
    
    @Test
    @DisplayName("Test scale calculation with calibration image dimensions")
    void testScaleCalculationWithCalibrationImageDimensions() {
        // Using calibration image dimensions (3300 x 2550 at 300 DPI)
        int imageWidthPixels = CALIBRATION_WIDTH;
        int imageHeightPixels = CALIBRATION_HEIGHT;
        float desiredWidthInches = 11.0f; // Standard paper width
        float desiredHeightInches = 8.5f;  // Standard paper height
        int imageDPI = CALIBRATION_DPI;
        
        float scale = ScaleCalculator.calculateScaleWithDPI(
            imageWidthPixels, imageHeightPixels, desiredWidthInches, desiredHeightInches, imageDPI
        );
        
        // Calculate expected scale manually for verification
        float currentWidthInches = (float) imageWidthPixels / imageDPI; // 3300/300 = 11.0
        float currentHeightInches = (float) imageHeightPixels / imageDPI; // 2550/300 = 8.5
        
        float expectedScaleX = desiredWidthInches / currentWidthInches; // 11.0/11.0 = 1.0
        float expectedScaleY = desiredHeightInches / currentHeightInches; // 8.5/8.5 = 1.0
        float expectedScale = Math.min(expectedScaleX, expectedScaleY); // 1.0
        
        assertEquals(expectedScale, scale, 0.001f, 
            "Scale calculation with calibration image dimensions should return 1.0 for same-size target");
        assertEquals(1.0f, scale, 0.001f, 
            "Scale should be 1.0 when desired size matches current size");
    }
    
    @Test
    @DisplayName("Test scale calculation with known pixel distances and different target sizes")
    void testScaleCalculationWithDifferentTargetSizes() {
        // Test scaling to different target sizes
        int imageWidthPixels = CALIBRATION_WIDTH;
        int imageHeightPixels = CALIBRATION_HEIGHT;
        int imageDPI = CALIBRATION_DPI;
        
        // Test case 1: Scale up to larger size
        float desiredWidthInches = 22.0f; // Double the width
        float desiredHeightInches = 17.0f; // Double the height
        
        float scale = ScaleCalculator.calculateScaleWithDPI(
            imageWidthPixels, imageHeightPixels, desiredWidthInches, desiredHeightInches, imageDPI
        );
        
        // Expected scale should be 2.0 (doubling the size)
        assertEquals(2.0f, scale, 0.001f, 
            "Scale should be 2.0 when doubling target size");
        
        // Test case 2: Scale down to smaller size
        desiredWidthInches = 5.5f; // Half the width
        desiredHeightInches = 4.25f; // Half the height
        
        scale = ScaleCalculator.calculateScaleWithDPI(
            imageWidthPixels, imageHeightPixels, desiredWidthInches, desiredHeightInches, imageDPI
        );
        
        // Expected scale should be 0.5 (halving the size)
        assertEquals(0.5f, scale, 0.001f, 
            "Scale should be 0.5 when halving target size");
    }
    
    @Test
    @DisplayName("Test expected physical size calculation")
    void testExpectedPhysicalSizeCalculation() {
        int imageWidthPixels = CALIBRATION_WIDTH;
        int imageHeightPixels = CALIBRATION_HEIGHT;
        float scale = 1.5f; // 50% larger
        int targetPrintDPI = 150; // Typical office printer DPI
        
        ScaleCalculator.PhysicalSize physicalSize = ScaleCalculator.calculateExpectedPhysicalSize(
            imageWidthPixels, imageHeightPixels, scale, targetPrintDPI
        );
        
        // Calculate expected values
        int scaledWidthPixels = (int) (imageWidthPixels * scale); // 3300 * 1.5 = 4950
        int scaledHeightPixels = (int) (imageHeightPixels * scale); // 2550 * 1.5 = 3825
        float expectedWidthInches = (float) scaledWidthPixels / targetPrintDPI; // 4950 / 150 = 33.0
        float expectedHeightInches = (float) scaledHeightPixels / targetPrintDPI; // 3825 / 150 = 25.5
        
        assertEquals(expectedWidthInches, physicalSize.widthInches, 0.01f, 
            "Physical width should match calculated value");
        assertEquals(expectedHeightInches, physicalSize.heightInches, 0.01f, 
            "Physical height should match calculated value");
    }
    
    @Test
    @DisplayName("Test DPI estimation based on measurements")
    void testDPIEstimation() {
        // Simulate a scenario where user prints at scale 1.0 and measures the result
        int imageWidthPixels = CALIBRATION_WIDTH;
        int imageHeightPixels = CALIBRATION_HEIGHT;
        float measuredWidthInches = 11.0f; // What they actually measured
        float measuredHeightInches = 8.5f; // What they actually measured
        int printerDPI = 150; // Assumed printer DPI
        
        int estimatedDPI = ScaleCalculator.estimateImageDPI(
            imageWidthPixels, imageHeightPixels, measuredWidthInches, measuredHeightInches, printerDPI
        );
        
        // Expected DPI calculation: imageWidthPixels / measuredWidthInches = 3300 / 11.0 = 300
        int expectedDPI = Math.round(imageWidthPixels / measuredWidthInches);
        
        assertEquals(expectedDPI, estimatedDPI, 
            "Estimated DPI should match calculated value");
        assertEquals(300, estimatedDPI, 
            "Estimated DPI should be 300 for calibration image");
    }
    
    @Test
    @DisplayName("Test scale calculation for specific tile count")
    void testScaleCalculationForTileCount() {
        int imageWidthPixels = CALIBRATION_WIDTH;
        int imageHeightPixels = CALIBRATION_HEIGHT;
        int desiredTilesWide = 2;
        int desiredTilesHigh = 2;
        double pageWidthPoints = 8.27 * 72; // A4 width in points
        double pageHeightPoints = 11.69 * 72; // A4 height in points
        
        float scale = ScaleCalculator.calculateScaleForTileCount(
            imageWidthPixels, imageHeightPixels, desiredTilesWide, desiredTilesHigh, 
            pageWidthPoints, pageHeightPoints
        );
        
        // Calculate expected scale
        double targetImageWidth = desiredTilesWide * pageWidthPoints;
        double targetImageHeight = desiredTilesHigh * pageHeightPoints;
        float expectedScaleX = (float) (targetImageWidth / imageWidthPixels);
        float expectedScaleY = (float) (targetImageHeight / imageHeightPixels);
        float expectedScale = Math.max(expectedScaleX, expectedScaleY);
        
        assertEquals(expectedScale, scale, 0.001f, 
            "Scale for tile count should match calculated value");
    }
    
    @Test
    @DisplayName("Test GridParameters pixel-to-physical conversions")
    void testGridParametersConversions() {
        // Test known conversions based on grid spacing
        float pixels = 142.0f; // One grid spacing
        float expectedCm = 1.0f; // Should be 1 cm
        
        float actualCm = GridParameters.pixelsToPhysicalCm(pixels);
        assertEquals(expectedCm, actualCm, 0.001f, 
            "142 pixels should convert to 1 cm");
        
        // Test reverse conversion
        float cm = 1.0f;
        float expectedPixels = 142.0f;
        
        float actualPixels = GridParameters.physicalCmToPixels(cm);
        assertEquals(expectedPixels, actualPixels, 0.001f, 
            "1 cm should convert to 142 pixels");
    }
    
    @Test
    @DisplayName("Test calibrated scale calculation with simulated measurements")
    void testCalibratedScaleCalculation() {
        // Simulate a calibration scenario
        float measuredPixels = 150.0f; // User measured 150 pixels between grid points
        float knownPhysicalSizeInPixels = 142.0f; // Known grid spacing in pixels
        
        float calibratedScale = GridParameters.calculateCalibratedScale(
            measuredPixels, knownPhysicalSizeInPixels
        );
        
        // Expected scale = measured / known = 150 / 142 ≈ 1.056
        float expectedScale = measuredPixels / knownPhysicalSizeInPixels;
        
        assertEquals(expectedScale, calibratedScale, 0.001f, 
            "Calibrated scale should match measured/known ratio");
        assertEquals(1.056f, calibratedScale, 0.01f, 
            "Calibrated scale should be approximately 1.056 for this example");
    }
    
    @Test
    @DisplayName("Test helper conversions between points and inches")
    void testPointsInchesConversions() {
        // Test points to inches (72 points = 1 inch)
        double points = 72.0;
        float expectedInches = 1.0f;
        
        float actualInches = ScaleCalculator.pointsToInches(points);
        assertEquals(expectedInches, actualInches, 0.001f, 
            "72 points should equal 1 inch");
        
        // Test inches to points
        float inches = 1.0f;
        double expectedPoints = 72.0;
        
        double actualPoints = ScaleCalculator.inchesToPoints(inches);
        assertEquals(expectedPoints, actualPoints, 0.001, 
            "1 inch should equal 72 points");
    }
    
    @Test
    @DisplayName("Test edge cases and boundary conditions")
    void testEdgeCases() {
        // Test with very small values
        float smallOriginal = 0.1f;
        float smallNew = 0.2f;
        float scale = ScaleCalculator.calculateScale(smallOriginal, smallNew);
        assertEquals(2.0f, scale, 0.001f, "Should handle small values correctly");
        
        // Test with large values
        float largeOriginal = 10000.0f;
        float largeNew = 20000.0f;
        scale = ScaleCalculator.calculateScale(largeOriginal, largeNew);
        assertEquals(2.0f, scale, 0.001f, "Should handle large values correctly");
        
        // Test DPI estimation boundary clamping
        int estimatedDPI = ScaleCalculator.estimateImageDPI(100, 100, 10.0f, 10.0f, 150);
        assertTrue(estimatedDPI >= 50 && estimatedDPI <= 600, 
            "Estimated DPI should be clamped between 50 and 600");
    }
}
