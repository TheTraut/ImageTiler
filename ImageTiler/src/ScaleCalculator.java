public class ScaleCalculator {
    
    /**
     * Standard DPI assumptions for different contexts
     */
    public static final int SCREEN_DPI = 72;  // Standard screen DPI
    public static final int PRINT_DPI = 300;  // Standard print DPI
    public static final int OFFICE_PRINT_DPI = 150; // Typical office printer DPI
    
    /**
     * Calculate scale based on measured physical dimensions
     * This is the primary method for users who print and measure
     */
    public static float calculateScale(float originalSize, float newSize) {
        return newSize / originalSize;
    }
    
    /**
     * Calculate scale based on desired physical size and image DPI
     * This method accounts for the relationship between pixels and physical size
     */
    public static float calculateScaleWithDPI(int imageWidthPixels, int imageHeightPixels, 
                                              float desiredWidthInches, float desiredHeightInches,
                                              int imageDPI) {
        // Calculate the current physical size of the image at its native DPI
        float currentWidthInches = (float) imageWidthPixels / imageDPI;
        float currentHeightInches = (float) imageHeightPixels / imageDPI;
        
        // Calculate scale factors for width and height
        float scaleX = desiredWidthInches / currentWidthInches;
        float scaleY = desiredHeightInches / currentHeightInches;
        
        // Use the minimum scale to ensure both dimensions fit
        return Math.min(scaleX, scaleY);
    }
    
    /**
     * Calculate what the actual printed size should be for a given scale
     * This helps users understand what physical size to expect
     */
    public static PhysicalSize calculateExpectedPhysicalSize(int imageWidthPixels, int imageHeightPixels,
                                                             float scale, int targetPrintDPI) {
        // Calculate the scaled pixel dimensions
        int scaledWidthPixels = (int) (imageWidthPixels * scale);
        int scaledHeightPixels = (int) (imageHeightPixels * scale);
        
        // Convert to physical inches at target print DPI
        float widthInches = (float) scaledWidthPixels / targetPrintDPI;
        float heightInches = (float) scaledHeightPixels / targetPrintDPI;
        
        return new PhysicalSize(widthInches, heightInches);
    }
    
    /**
     * Estimate the DPI of an image based on measurement feedback
     * This is useful when users print at scale 1.0 and measure the result
     */
    public static int estimateImageDPI(int imageWidthPixels, int imageHeightPixels,
                                       float measuredWidthInches, float measuredHeightInches,
                                       int printerDPI) {
        // Use width measurement for estimation (can also average with height)
        // The formula: measured_size = image_pixels / printer_DPI
        // Therefore: printer_DPI = image_pixels / measured_size
        int estimatedDPI = Math.round(imageWidthPixels / measuredWidthInches);
        
        // Clamp to reasonable DPI values
        return Math.max(50, Math.min(600, estimatedDPI));
    }
    
    /**
     * Calculate the scale needed to achieve a specific number of tiles
     * Useful for reverse engineering: "I want this to be exactly 4 tiles wide"
     */
    public static float calculateScaleForTileCount(int imageWidthPixels, int imageHeightPixels,
                                                   int desiredTilesWide, int desiredTilesHigh,
                                                   double pageWidthPoints, double pageHeightPoints) {
        // Calculate what the scaled image dimensions need to be
        double targetImageWidth = desiredTilesWide * pageWidthPoints;
        double targetImageHeight = desiredTilesHigh * pageHeightPoints;
        
        // Calculate scale factors
        float scaleX = (float) (targetImageWidth / imageWidthPixels);
        float scaleY = (float) (targetImageHeight / imageHeightPixels);
        
        // Use the larger scale to ensure we get at least the requested number of tiles
        return Math.max(scaleX, scaleY);
    }
    
    /**
     * Represents a physical size in inches
     */
    public static class PhysicalSize {
        public final float widthInches;
        public final float heightInches;
        
        public PhysicalSize(float widthInches, float heightInches) {
            this.widthInches = widthInches;
            this.heightInches = heightInches;
        }
        
        @Override
        public String toString() {
            return String.format("%.2f × %.2f inches", widthInches, heightInches);
        }
    }
    
    /**
     * Helper method to convert points to inches (72 points = 1 inch)
     */
    public static float pointsToInches(double points) {
        return (float) (points / 72.0);
    }
    
    /**
     * Helper method to convert inches to points (1 inch = 72 points)
     */
    public static double inchesToPoints(float inches) {
        return inches * 72.0;
    }
}
