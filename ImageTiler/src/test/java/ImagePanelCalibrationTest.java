import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.List;

/**
 * Unit tests for ImagePanel methods specifically focused on calibration image behavior.
 * Tests the getSelectedTiles() and isCalibrationImage() methods with the calibration resource.
 */
public class ImagePanelCalibrationTest {
    
    private ImagePanel imagePanel;
    private BufferedImage calibrationImage;
    private BufferedImage normalImage;
    
    @BeforeEach
    void setUp() {
        imagePanel = new ImagePanel();
        
        // Load the calibration image from classpath
        try {
            InputStream imageStream = getClass().getResourceAsStream("/calibration/calibration.png");
            assertNotNull(imageStream, "Calibration image resource should be available");
            calibrationImage = ImageIO.read(imageStream);
            imageStream.close();
        } catch (IOException e) {
            fail("Failed to load calibration image: " + e.getMessage());
        }
        
        // Create a normal (non-calibration) test image
        normalImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        // Fill with some content so it's not blank
        for (int y = 0; y < normalImage.getHeight(); y++) {
            for (int x = 0; x < normalImage.getWidth(); x++) {
                normalImage.setRGB(x, y, 0xFF0000); // Red pixels
            }
        }
    }
    
    @Test
    @DisplayName("isCalibrationImage() should return true for calibration resource")
    void testIsCalibrationImageWithCalibrationResource() {
        imagePanel.setImage(calibrationImage);
        
        assertTrue(imagePanel.isCalibrationImage(), 
            "Instance method should detect calibration image dimensions");
    }
    
    @Test
    @DisplayName("isCalibrationImage() static method should return true for calibration resource")
    void testIsCalibrationImageStaticWithCalibrationResource() {
        assertTrue(ImagePanel.isCalibrationImage(calibrationImage), 
            "Static method should detect calibration image dimensions");
    }
    
    @Test
    @DisplayName("isCalibrationImage() should return false for normal images")
    void testIsCalibrationImageWithNormalImage() {
        imagePanel.setImage(normalImage);
        
        assertFalse(imagePanel.isCalibrationImage(), 
            "Instance method should not detect normal image as calibration");
        assertFalse(ImagePanel.isCalibrationImage(normalImage), 
            "Static method should not detect normal image as calibration");
    }
    
    @Test
    @DisplayName("isCalibrationImage() should handle null images gracefully")
    void testIsCalibrationImageWithNullImage() {
        assertFalse(ImagePanel.isCalibrationImage(null), 
            "Static method should return false for null image");
        
        // Test instance method with no image set
        ImagePanel emptyPanel = new ImagePanel();
        assertFalse(emptyPanel.isCalibrationImage(), 
            "Instance method should return false when no image is set");
    }
    
    @Test
    @DisplayName("isCalibrationImage() should detect rotated calibration images")
    void testIsCalibrationImageWithRotatedCalibration() {
        // Create a rotated version of calibration image (2550 x 3300 instead of 3300 x 2550)
        BufferedImage rotatedCalibrationImage = new BufferedImage(2550, 3300, BufferedImage.TYPE_INT_RGB);
        
        assertTrue(ImagePanel.isCalibrationImage(rotatedCalibrationImage), 
            "Should detect rotated calibration image dimensions");
    }
    
    @Test
    @DisplayName("getSelectedTiles() should return single tile for calibration image")
    void testGetSelectedTilesWithCalibrationImage() {
        imagePanel.setImage(calibrationImage);
        
        // Create a simple tiling result for testing
        TileCalculator.TilingResult tilingResult = new TileCalculator.TilingResult(
            2, 2, 400.0, 300.0, 3300, 2550
        );
        
        List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
        
        assertEquals(1, selectedTiles.size(), 
            "Calibration image should return exactly one tile");
        
        TileCalculator.TileInfo tile = selectedTiles.get(0);
        assertEquals(0, tile.col, "Single tile should be at column 0");
        assertEquals(0, tile.row, "Single tile should be at row 0");
        assertEquals(1, tile.tileNumber, "Single tile should be numbered 1");
    }
    
    @Test
    @DisplayName("getSelectedTiles() should process normal images normally")
    void testGetSelectedTilesWithNormalImage() {
        imagePanel.setImage(normalImage);
        
        // Create a tiling result that would normally produce multiple tiles
        TileCalculator.TilingResult tilingResult = new TileCalculator.TilingResult(
            2, 2, 400.0, 300.0, 800, 600
        );
        
        List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, normalImage);
        
        // For normal images, should use the standard non-blank tile detection
        assertTrue(selectedTiles.size() >= 1, 
            "Normal image should return at least one tile based on content analysis");
        
        // Verify that all returned tiles have valid coordinates
        for (TileCalculator.TileInfo tile : selectedTiles) {
            assertTrue(tile.col >= 0 && tile.col < tilingResult.tilesWide, 
                "Tile column should be within bounds");
            assertTrue(tile.row >= 0 && tile.row < tilingResult.tilesHigh, 
                "Tile row should be within bounds");
            assertTrue(tile.tileNumber >= 1, 
                "Tile number should be positive");
        }
    }
    
    @Test
    @DisplayName("getSelectedTiles() should handle manual tile exclusions for normal images")
    void testGetSelectedTilesWithManualExclusions() {
        imagePanel.setImage(normalImage);
        
        // Create a tiling result
        TileCalculator.TilingResult tilingResult = new TileCalculator.TilingResult(
            2, 2, 400.0, 300.0, 800, 600
        );
        
        // Get initial selection
        List<TileCalculator.TileInfo> initialTiles = imagePanel.getSelectedTiles(tilingResult, normalImage);
        int initialCount = initialTiles.size();
        
        // Manually exclude a tile (if any exist)
        if (initialCount > 0) {
            TileCalculator.TileInfo firstTile = initialTiles.get(0);
            String tileKey = firstTile.col + "," + firstTile.row;
            imagePanel.getManuallyExcludedTiles().add(tileKey);
            
            // Get selection after exclusion
            List<TileCalculator.TileInfo> tilesAfterExclusion = imagePanel.getSelectedTiles(tilingResult, normalImage);
            
            assertTrue(tilesAfterExclusion.size() < initialCount, 
                "Manual exclusion should reduce the number of selected tiles");
        }
    }
    
    @Test
    @DisplayName("getSelectedTiles() should bypass manual selections for calibration images")
    void testGetSelectedTilesBypassesManualSelectionsForCalibration() {
        imagePanel.setImage(calibrationImage);
        
        // Create a tiling result
        TileCalculator.TilingResult tilingResult = new TileCalculator.TilingResult(
            2, 2, 400.0, 300.0, 3300, 2550
        );
        
        // Try to manually exclude tiles (should be ignored for calibration images)
        imagePanel.getManuallyExcludedTiles().add("0,0");
        imagePanel.getManuallyExcludedTiles().add("1,1");
        
        List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
        
        assertEquals(1, selectedTiles.size(), 
            "Calibration image should still return exactly one tile despite manual exclusions");
    }
    
    @Test
    @DisplayName("Calibration image detection should be consistent across dimensions")
    void testCalibrationImageDetectionConsistency() {
        // Test both possible orientations of calibration image
        BufferedImage calibration1 = new BufferedImage(3300, 2550, BufferedImage.TYPE_INT_RGB);
        BufferedImage calibration2 = new BufferedImage(2550, 3300, BufferedImage.TYPE_INT_RGB);
        
        assertTrue(ImagePanel.isCalibrationImage(calibration1), 
            "3300x2550 should be detected as calibration image");
        assertTrue(ImagePanel.isCalibrationImage(calibration2), 
            "2550x3300 should be detected as calibration image");
        
        // Test images that are close but not exact
        BufferedImage nearCalibration1 = new BufferedImage(3299, 2550, BufferedImage.TYPE_INT_RGB);
        BufferedImage nearCalibration2 = new BufferedImage(3300, 2549, BufferedImage.TYPE_INT_RGB);
        
        assertFalse(ImagePanel.isCalibrationImage(nearCalibration1), 
            "3299x2550 should not be detected as calibration image");
        assertFalse(ImagePanel.isCalibrationImage(nearCalibration2), 
            "3300x2549 should not be detected as calibration image");
    }
    
    @Test
    @DisplayName("Verify calibration image resource has expected properties")
    void testCalibrationImageResourceProperties() {
        assertNotNull(calibrationImage, "Calibration image should be loaded");
        
        // Verify dimensions match calibration specifications
        boolean isCorrectDimensions = (calibrationImage.getWidth() == 3300 && calibrationImage.getHeight() == 2550) ||
                                     (calibrationImage.getWidth() == 2550 && calibrationImage.getHeight() == 3300);
        
        assertTrue(isCorrectDimensions, 
            "Calibration image resource should have correct dimensions (3300x2550 or 2550x3300)");
        
        // Verify it's detected as calibration image
        assertTrue(ImagePanel.isCalibrationImage(calibrationImage), 
            "Loaded calibration resource should be detected as calibration image");
    }
}
