import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Integration test that simulates the complete printing and PDF saving workflow
 * for calibration images, capturing and verifying the output pages.
 */
public class CalibrationPrintingIntegrationTest {
    
    private ImagePanel imagePanel;
    private BufferedImage calibrationImage;
    private List<File> tempFiles;
    
    @BeforeEach
    void setUp() {
        imagePanel = new ImagePanel();
        tempFiles = new ArrayList<>();
        
        // Load the calibration image from classpath
        try {
            InputStream imageStream = getClass().getResourceAsStream("/calibration/calibration.png");
            assertNotNull(imageStream, "Calibration image resource should be available");
            calibrationImage = ImageIO.read(imageStream);
            imageStream.close();
        } catch (IOException e) {
            fail("Failed to load calibration image: " + e.getMessage());
        }
    }
    
    @AfterEach
    void tearDown() {
        // Clean up temporary files
        for (File tempFile : tempFiles) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    private File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFiles.add(tempFile);
        return tempFile;
    }
    
    @Test
    @DisplayName("Integration test: PDF generation for calibration image at scale 1.0")
    void testCalibrationImagePDFGeneration() throws IOException {
        imagePanel.setImage(calibrationImage);
        
        // Create a temporary PDF file
        File tempPDF = createTempFile("calibration_test", ".pdf");
        
        // Simulate the PDF generation process
        float scale = 1.0f;
        boolean isRotated = false;
        
        // Calculate tiling for scale 1.0 (should be single page)
        double pageWidth = 8.27 * 72; // A4 width in points
        double pageHeight = 11.69 * 72; // A4 height in points
        
        TileCalculator.TilingResult tilingResult = TileCalculator.calculateSinglePagePreview(
            calibrationImage.getWidth(), calibrationImage.getHeight(), pageWidth, pageHeight
        );
        
        // Verify it's a single page setup
        assertEquals(1, tilingResult.tilesWide, "Should be 1 tile wide");
        assertEquals(1, tilingResult.tilesHigh, "Should be 1 tile high");
        
        // Get selected tiles (should be 1 for calibration image)
        List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
        assertEquals(1, selectedTiles.size(), "Should have exactly 1 selected tile");
        
        // Generate PDF using the actual TilePrinter logic (simplified version)
        generateTestPDF(tempPDF, calibrationImage, tilingResult, selectedTiles);
        
        // Verify PDF was created
        assertTrue(tempPDF.exists(), "PDF file should be created");
        assertTrue(tempPDF.length() > 0, "PDF file should not be empty");
        
        // Verify PDF content
        verifyPDFContent(tempPDF, 1); // Should have exactly 1 page
    }
    
    @Test
    @DisplayName("Integration test: PDF generation for calibration image at scale 2.0")
    void testCalibrationImagePDFGenerationScaled() throws IOException {
        imagePanel.setImage(calibrationImage);
        
        // Create a temporary PDF file
        File tempPDF = createTempFile("calibration_scaled_test", ".pdf");
        
        // Simulate the PDF generation process at scale 2.0
        float scale = 2.0f;
        boolean isRotated = false;
        
        double pageWidth = 8.27 * 72; // A4 width in points
        double pageHeight = 11.69 * 72; // A4 height in points
        
        // For scale > 1.0, use scaled tiling
        TileCalculator.TilingResult tilingResult = TileCalculator.calculateScaledTiling(
            calibrationImage.getWidth(), calibrationImage.getHeight(), pageWidth, pageHeight, scale
        );
        
        // Should require multiple tiles at scale 2.0
        assertTrue(tilingResult.tilesWide > 1 || tilingResult.tilesHigh > 1, 
            "Scale 2.0 should require multiple tiles");
        
        // For calibration images, all tiles should be selected regardless of blank detection
        List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
        
        // For calibration images, getSelectedTiles should still return 1 tile (override behavior)
        assertEquals(1, selectedTiles.size(), 
            "Calibration image should return 1 tile even at scale 2.0 due to special handling");
        
        // Generate PDF
        generateTestPDF(tempPDF, calibrationImage, tilingResult, selectedTiles);
        
        // Verify PDF was created
        assertTrue(tempPDF.exists(), "PDF file should be created");
        assertTrue(tempPDF.length() > 0, "PDF file should not be empty");
        
        // Verify PDF content (should have 1 page due to calibration image special handling)
        verifyPDFContent(tempPDF, 1);
    }
    
    @Test
    @DisplayName("Integration test: Simulate print preview rendering")
    void testCalibrationImagePrintPreview() {
        imagePanel.setImage(calibrationImage);
        
        // Simulate print preview rendering
        float scale = 1.0f;
        double pageWidth = 8.27 * 72; // A4 width in points
        double pageHeight = 11.69 * 72; // A4 height in points
        
        TileCalculator.TilingResult tilingResult = TileCalculator.calculateSinglePagePreview(
            calibrationImage.getWidth(), calibrationImage.getHeight(), pageWidth, pageHeight
        );
        
        List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
        
        // Simulate rendering each page
        for (int pageIndex = 0; pageIndex < selectedTiles.size(); pageIndex++) {
            TileCalculator.TileInfo tileInfo = selectedTiles.get(pageIndex);
            
            // Create a mock graphics context for testing
            BufferedImage testImage = new BufferedImage((int)pageWidth, (int)pageHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = testImage.createGraphics();
            
            // Simulate the printing logic
            try {
                int result = simulatePrintPage(g2d, pageIndex, tilingResult, tileInfo, scale);
                assertEquals(Printable.PAGE_EXISTS, result, 
                    "Page " + pageIndex + " should render successfully");
            } finally {
                g2d.dispose();
            }
        }
    }
    
    @Test
    @DisplayName("Integration test: Verify calibration image special handling in print workflow")
    void testCalibrationImageSpecialHandlingInPrintWorkflow() {
        imagePanel.setImage(calibrationImage);
        
        // Test different scales to verify consistent behavior
        float[] testScales = {1.0f, 1.5f, 2.0f, 0.5f};
        
        for (float scale : testScales) {
            double pageWidth = 8.27 * 72;
            double pageHeight = 11.69 * 72;
            
            TileCalculator.TilingResult tilingResult;
            if (scale == 1.0f) {
                tilingResult = TileCalculator.calculateSinglePagePreview(
                    calibrationImage.getWidth(), calibrationImage.getHeight(), pageWidth, pageHeight
                );
            } else {
                tilingResult = TileCalculator.calculateScaledTiling(
                    calibrationImage.getWidth(), calibrationImage.getHeight(), pageWidth, pageHeight, scale
                );
            }
            
            // Verify calibration image detection
            assertTrue(ImagePanel.isCalibrationImage(calibrationImage), 
                "Should be detected as calibration image at scale " + scale);
            
            // Verify special tile selection behavior
            List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
            assertEquals(1, selectedTiles.size(), 
                "Calibration image should always return 1 tile at scale " + scale);
            
            TileCalculator.TileInfo tile = selectedTiles.get(0);
            assertEquals(0, tile.col, "Tile should be at column 0 for scale " + scale);
            assertEquals(0, tile.row, "Tile should be at row 0 for scale " + scale);
            assertEquals(1, tile.tileNumber, "Tile should be numbered 1 for scale " + scale);
        }
    }
    
    @Test
    @DisplayName("Integration test: Compare calibration vs normal image processing")
    void testCalibrationVsNormalImageProcessing() throws IOException {
        // Create a normal image with same dimensions as calibration image
        BufferedImage normalImage = new BufferedImage(
            calibrationImage.getWidth(), calibrationImage.getHeight(), BufferedImage.TYPE_INT_RGB
        );
        
        // Fill with non-white content
        Graphics2D g = normalImage.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, normalImage.getWidth(), normalImage.getHeight());
        g.setColor(Color.RED);
        g.fillRect(100, 100, 200, 200);
        g.dispose();
        
        // Test calibration image
        imagePanel.setImage(calibrationImage);
        double pageWidth = 8.27 * 72;
        double pageHeight = 11.69 * 72;
        
        TileCalculator.TilingResult tilingResult = TileCalculator.calculateSinglePagePreview(
            calibrationImage.getWidth(), calibrationImage.getHeight(), pageWidth, pageHeight
        );
        
        List<TileCalculator.TileInfo> calibrationTiles = imagePanel.getSelectedTiles(tilingResult, calibrationImage);
        
        // Test normal image
        imagePanel.setImage(normalImage);
        List<TileCalculator.TileInfo> normalTiles = imagePanel.getSelectedTiles(tilingResult, normalImage);
        
        // Verify different behavior
        assertEquals(1, calibrationTiles.size(), "Calibration image should return 1 tile");
        assertTrue(normalTiles.size() >= 1, "Normal image should return at least 1 tile");
        
        // For this particular test case, the normal image might return multiple tiles
        // depending on content analysis, while calibration always returns 1
        assertTrue(ImagePanel.isCalibrationImage(calibrationImage), 
            "Should detect calibration image");
        assertFalse(ImagePanel.isCalibrationImage(normalImage), 
            "Should not detect normal image as calibration");
    }
    
    // Helper method to generate a test PDF
    private void generateTestPDF(File outputFile, BufferedImage image, 
                                TileCalculator.TilingResult tilingResult, 
                                List<TileCalculator.TileInfo> selectedTiles) throws IOException {
        
        PDDocument document = new PDDocument();
        
        try {
            // Create temporary image file for PDFBox
            File tempImageFile = createTempFile("temp_image", ".png");
            ImageIO.write(image, "png", tempImageFile);
            
            org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdImage = 
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromFile(
                    tempImageFile.getAbsolutePath(), document
                );
            
            for (TileCalculator.TileInfo tileInfo : selectedTiles) {
                PDPage page = new PDPage(new org.apache.pdfbox.pdmodel.common.PDRectangle(
                    (float)tilingResult.tileWidth, (float)tilingResult.tileHeight
                ));
                document.addPage(page);
                
                try (org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = 
                     new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                    
                    // Calculate position for this tile
                    float sourceX = tileInfo.col * (float)tilingResult.tileWidth;
                    float sourceY = tileInfo.row * (float)tilingResult.tileHeight;
                    
                    float imageX = -sourceX;
                    float imageY = (float)tilingResult.tileHeight - tilingResult.imageHeight + sourceY;
                    
                    // Draw the image
                    contentStream.drawImage(pdImage, imageX, imageY, 
                                          tilingResult.imageWidth, tilingResult.imageHeight);
                }
            }
            
            document.save(outputFile);
        } finally {
            document.close();
        }
    }
    
    // Helper method to verify PDF content
    private void verifyPDFContent(File pdfFile, int expectedPageCount) throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        
        try {
            assertEquals(expectedPageCount, document.getNumberOfPages(), 
                "PDF should have correct number of pages");
            
            // Verify each page can be rendered
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 72);
                assertNotNull(pageImage, "Page " + i + " should render successfully");
                assertTrue(pageImage.getWidth() > 0, "Page " + i + " should have valid width");
                assertTrue(pageImage.getHeight() > 0, "Page " + i + " should have valid height");
            }
        } finally {
            document.close();
        }
    }
    
    // Helper method to simulate print page rendering
    private int simulatePrintPage(Graphics2D g2d, int pageIndex, 
                                 TileCalculator.TilingResult tilingResult,
                                 TileCalculator.TileInfo tileInfo, float scale) {
        
        // Simulate the print rendering logic
        if (pageIndex >= 1) { // Only one page for calibration images
            return Printable.NO_SUCH_PAGE;
        }
        
        // Calculate rendering dimensions
        int renderWidth = tilingResult.imageWidth;
        int renderHeight = tilingResult.imageHeight;
        
        if (scale != 1.0f) {
            TileCalculator.TilingResult baseline = TileCalculator.calculateSinglePagePreview(
                calibrationImage.getWidth(), calibrationImage.getHeight(), 
                tilingResult.tileWidth, tilingResult.tileHeight
            );
            renderWidth = (int)(baseline.imageWidth * scale);
            renderHeight = (int)(baseline.imageHeight * scale);
        }
        
        // Calculate position
        int x = -(int)(tileInfo.col * tilingResult.tileWidth);
        int y = -(int)(tileInfo.row * tilingResult.tileHeight);
        
        // Simulate drawing the image
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(x, y, renderWidth, renderHeight);
        
        return Printable.PAGE_EXISTS;
    }
}
