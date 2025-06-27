/**
 * Parameters for the calibration rectangle system
 */
public class GridParameters {
    
    // Calibration rectangle dimensions in the reference image
    // Rectangle is 4.0 inches × 3.0 inches
    public static final float CALIBRATION_RECTANGLE_WIDTH_INCHES = 4.0f;
    public static final float CALIBRATION_RECTANGLE_HEIGHT_INCHES = 3.0f;
    
    // Expected DPI for calculations
    public static final int EXPECTED_DPI = 150;
    
    // Grid spacing for snap-to-grid functionality (in pixels)
    public static final int GRID_SPACING = 20;
    
    /**
     * Calculates calibrated scale based on measured rectangle dimensions
     * @param measuredWidthInches Width of printed rectangle in inches
     * @param measuredHeightInches Height of printed rectangle in inches
     * @return Calibrated scale factor
     */
    public static float calculateCalibratedScale(float measuredWidthInches, float measuredHeightInches) {
        // Calculate scale factors for both dimensions
        float widthScale = CALIBRATION_RECTANGLE_WIDTH_INCHES / measuredWidthInches;
        float heightScale = CALIBRATION_RECTANGLE_HEIGHT_INCHES / measuredHeightInches;
        
        // Use average scale factor
        return (widthScale + heightScale) / 2.0f;
    }
    
    /**
     * Gets the expected DPI based on measured rectangle size
     * @param measuredWidthInches Width of printed rectangle in inches  
     * @param measuredHeightInches Height of printed rectangle in inches
     * @param imageWidthPixels Width of image that was printed in pixels
     * @param imageHeightPixels Height of image that was printed in pixels
     * @return Actual DPI of the printer
     */
    public static float getActualDPI(float measuredWidthInches, float measuredHeightInches, 
                                     int imageWidthPixels, int imageHeightPixels) {
        float widthDPI = imageWidthPixels / measuredWidthInches;
        float heightDPI = imageHeightPixels / measuredHeightInches;
        return (widthDPI + heightDPI) / 2.0f;
    }
}
