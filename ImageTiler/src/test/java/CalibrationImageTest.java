import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Test class for verifying calibration image loading from classpath
 * and validating the image properties for calibration purposes.
 */
public class CalibrationImageTest {
    
    private static BufferedImage calibrationImage;
    
    @BeforeAll
    static void loadCalibrationImage() {
        try {
            // Load the calibration image from classpath
            InputStream imageStream = CalibrationImageTest.class.getResourceAsStream("/calibration/calibration.png");
            assertNotNull(imageStream, "Calibration image resource stream should not be null");
            
            calibrationImage = ImageIO.read(imageStream);
            imageStream.close();
        } catch (IOException e) {
            fail("Failed to load calibration image: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Verify calibration.png loads from classpath")
    void testCalibrationImageLoadsFromClasspath() {
        // Test that the image resource can be found
        InputStream imageStream = getClass().getResourceAsStream("/calibration/calibration.png");
        assertNotNull(imageStream, "Calibration image should be found in classpath at /calibration/calibration.png");
        
        try {
            imageStream.close();
        } catch (IOException e) {
            fail("Failed to close image stream: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Verify calibration image loads successfully as BufferedImage")
    void testCalibrationImageLoadsAsBufferedImage() {
        assertNotNull(calibrationImage, "Calibration image should load successfully as BufferedImage");
    }
    
    @Test
    @DisplayName("Verify calibration image has expected dimensions")
    void testCalibrationImageDimensions() {
        assertNotNull(calibrationImage, "Calibration image should be loaded");
        
        // Based on the MeasurementHelperDialog code, the calibration image is 3300 x 2550 pixels
        int expectedWidth = 3300;
        int expectedHeight = 2550;
        
        assertEquals(expectedWidth, calibrationImage.getWidth(), 
            "Calibration image width should be " + expectedWidth + " pixels");
        assertEquals(expectedHeight, calibrationImage.getHeight(), 
            "Calibration image height should be " + expectedHeight + " pixels");
    }
    
    @Test
    @DisplayName("Verify calibration image is not empty")
    void testCalibrationImageIsNotEmpty() {
        assertNotNull(calibrationImage, "Calibration image should be loaded");
        assertTrue(calibrationImage.getWidth() > 0, "Calibration image width should be greater than 0");
        assertTrue(calibrationImage.getHeight() > 0, "Calibration image height should be greater than 0");
    }
    
    @Test
    @DisplayName("Verify calibration image has correct color type")
    void testCalibrationImageColorType() {
        assertNotNull(calibrationImage, "Calibration image should be loaded");
        
        // The image should have a valid color model
        assertNotNull(calibrationImage.getColorModel(), "Calibration image should have a color model");
        
        // Should be able to get pixel data
        int[] pixels = new int[calibrationImage.getWidth() * calibrationImage.getHeight()];
        assertDoesNotThrow(() -> {
            calibrationImage.getRGB(0, 0, calibrationImage.getWidth(), calibrationImage.getHeight(), pixels, 0, calibrationImage.getWidth());
        }, "Should be able to read pixel data from calibration image");
    }
    
    @Test
    @DisplayName("Verify calibration image can be used with MeasurementHelperDialog detection")
    void testCalibrationImageDetection() {
        assertNotNull(calibrationImage, "Calibration image should be loaded");
        
        // Test the isCalibrationImage logic from MeasurementHelperDialog
        boolean isCalibrationImage = (calibrationImage.getWidth() == 3300 && calibrationImage.getHeight() == 2550) ||
                                   (calibrationImage.getWidth() == 2550 && calibrationImage.getHeight() == 3300);
        
        assertTrue(isCalibrationImage, "Calibration image should be detected as a calibration image");
    }
    
    @Test
    @DisplayName("Test alternative resource loading methods")
    void testAlternativeResourceLoadingMethods() {
        // Test using ClassLoader.getResourceAsStream
        InputStream stream1 = ClassLoader.getSystemResourceAsStream("calibration/calibration.png");
        assertNotNull(stream1, "Should be able to load calibration image using ClassLoader.getSystemResourceAsStream");
        
        try {
            stream1.close();
        } catch (IOException e) {
            fail("Failed to close stream1: " + e.getMessage());
        }
        
        // Test using class loader from this class
        InputStream stream2 = this.getClass().getClassLoader().getResourceAsStream("calibration/calibration.png");
        assertNotNull(stream2, "Should be able to load calibration image using getClassLoader().getResourceAsStream");
        
        try {
            stream2.close();
        } catch (IOException e) {
            fail("Failed to close stream2: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Verify calibration image loading matches MeasurementHelperDialog implementation")
    void testMatchesMeasurementHelperDialogLoading() {
        // This replicates the loading logic from MeasurementHelperDialog
        BufferedImage testImage = null;
        Exception loadException = null;
        
        try {
            testImage = ImageIO.read(getClass().getResource("/calibration/calibration.png"));
        } catch (Exception e) {
            loadException = e;
        }
        
        assertNull(loadException, "Should not throw exception when loading calibration image");
        assertNotNull(testImage, "Should successfully load calibration image using getResource().getResource()");
        
        // Verify the loaded image matches our expectations
        assertEquals(calibrationImage.getWidth(), testImage.getWidth(), 
            "Width should match between different loading methods");
        assertEquals(calibrationImage.getHeight(), testImage.getHeight(), 
            "Height should match between different loading methods");
    }
}
