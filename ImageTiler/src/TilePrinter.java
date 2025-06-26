import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;

public class TilePrinter {

    public static void printTiledImage(BufferedImage image, float scale, boolean isRotated) {
        PrinterJob job = PrinterJob.getPrinterJob();

        // Pre-calculate non-blank tiles
        int scaledWidth = (int) (image.getWidth() * scale);
        int scaledHeight = (int) (image.getHeight() * scale);
        // Use approximate page size for calculation - will be adjusted in print method
        TileCalculator.TilingResult tilingResult = TileCalculator.calculateOptimalTiling(scaledWidth, scaledHeight, 8.27 * 72, 11.69 * 72);
        java.util.List<TileCalculator.TileInfo> nonBlankTiles = TileCalculator.getNonBlankTiles(tilingResult, image);

        // Manually excluded tiles handled later
        
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex >= nonBlankTiles.size()) {
                    return NO_SUCH_PAGE;
                }
                
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double pageWidth = pageFormat.getImageableWidth();
                double pageHeight = pageFormat.getImageableHeight();

                    // Recalculate tiling with actual printer page dimensions
                    TileCalculator.TilingResult actualTilingResult = TileCalculator.calculateOptimalTiling(scaledWidth, scaledHeight, pageWidth, pageHeight);
                    
                    // Get the tile info for this page
                    TileCalculator.TileInfo tileInfo = nonBlankTiles.get(pageIndex);

                    // Manual exclusion will be handled by filtering the list

                    // Calculate position based on actual tile dimensions
                    int x = -(int)(tileInfo.col * actualTilingResult.tileWidth);
                    int y = -(int)(tileInfo.row * actualTilingResult.tileHeight);

                    g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);

                    return PAGE_EXISTS;
                }
            });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(
                    null,
                    "Printing completed!\n" +
                    "Pages printed: " + nonBlankTiles.size() + " out of " + (tilingResult.tilesWide * tilingResult.tilesHigh) + " total tiles\n" +
                    "Paper saved: " + (tilingResult.tilesWide * tilingResult.tilesHigh - nonBlankTiles.size()) + " blank pages avoided!",
                    "Print Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error during printing: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }

    public static void saveTiledImageToPDF(BufferedImage image, float scale, boolean isRotated) {
        // Let user choose where to save the PDF
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF As...");
        fileChooser.setSelectedFile(new File("tiled_image.pdf"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
            }
            @Override
            public String getDescription() {
                return "PDF Files (*.pdf)";
            }
        });

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // User cancelled
        }

        File outputFile = fileChooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(outputFile.getAbsolutePath() + ".pdf");
        }

        // Check if file exists and ask for confirmation
        if (outputFile.exists()) {
            int response = JOptionPane.showConfirmDialog(
                null,
                "File already exists. Do you want to overwrite it?",
                "File Exists",
                JOptionPane.YES_NO_OPTION
            );
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }

        PDDocument document = new PDDocument();
        double pageWidth = PDRectangle.A4.getWidth();
        double pageHeight = PDRectangle.A4.getHeight();
        int scaledWidth = (int) (image.getWidth() * scale);
        int scaledHeight = (int) (image.getHeight() * scale);

        TileCalculator.TilingResult tilingResult = TileCalculator.calculateOptimalTiling(scaledWidth, scaledHeight, pageWidth, pageHeight);

        File tempFile = null;
        try {
            // Create temporary image file
            tempFile = File.createTempFile("tempImage", ".png");
            ImageIO.write(image, "png", tempFile);
            PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);

            // Calculate the actual tile dimensions based on the optimal orientation
            double actualTileWidth = tilingResult.tileWidth;
            double actualTileHeight = tilingResult.tileHeight;

            // Get the list of non-blank tiles using pixel analysis
            java.util.List<TileCalculator.TileInfo> nonBlankTiles = TileCalculator.getNonBlankTiles(tilingResult, image);

            // Only draw tiles that contain image content
            for (TileCalculator.TileInfo tileInfo : nonBlankTiles) {
                // Manual exclusion will be handled by caller

                PDPage page = new PDPage(new PDRectangle((float)actualTileWidth, (float)actualTileHeight));
                document.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Calculate the position of this tile within the overall image
                    float sourceX = tileInfo.col * (float)actualTileWidth;
                    float sourceY = tileInfo.row * (float)actualTileHeight;
                    
                    // In PDF coordinate system, origin is at bottom-left
                    // We need to position the image so that the correct portion is visible
                    float imageX = -sourceX;
                    float imageY = (float)actualTileHeight - scaledHeight + sourceY;
                    
                    // Draw the scaled image at the calculated position
                    contentStream.drawImage(
                            pdImage,
                            imageX,
                            imageY,
                            scaledWidth,
                            scaledHeight
                    );

                    // Optionally add border to see the tile area clearly
                    contentStream.setStrokingColor(Color.LIGHT_GRAY);
                    contentStream.setLineWidth(1);
                    contentStream.addRect(0, 0, (float)actualTileWidth, (float)actualTileHeight);
                    contentStream.stroke();
                }
            }

            document.save(outputFile);
            document.close();
            
            JOptionPane.showMessageDialog(
                null,
                "PDF saved successfully to: " + outputFile.getAbsolutePath() + "\n" +
                "Pages with content: " + nonBlankTiles.size() + " out of " + (tilingResult.tilesWide * tilingResult.tilesHigh) + " total tiles\n" +
                "Paper saved: " + (tilingResult.tilesWide * tilingResult.tilesHigh - nonBlankTiles.size()) + " blank pages avoided!",
                "PDF Saved",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error saving PDF: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    /**
     * Print image with manual tile selection considerations
     */
    public static void printTiledImageWithSelection(BufferedImage image, float scale, boolean isRotated, ImagePanel imagePanel) {
        PrinterJob job = PrinterJob.getPrinterJob();

        // Pre-calculate non-blank tiles using the same logic as the display
        double pageWidth = 8.27 * 72; // A4 width in points (portrait)
        double pageHeight = 11.69 * 72; // A4 height in points (portrait)
        
        TileCalculator.TilingResult tilingResult;
        if (scale == 1.0f) {
            // At scale 1.0, use single page preview (auto-fit to one page)
            tilingResult = TileCalculator.calculateSinglePagePreview(image.getWidth(), image.getHeight(), pageWidth, pageHeight);
        } else {
            // When scaled, calculate actual tiling for the scaled dimensions
            tilingResult = TileCalculator.calculateScaledTiling(image.getWidth(), image.getHeight(), pageWidth, pageHeight, scale);
        }
        java.util.List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, image);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex >= selectedTiles.size()) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double pageWidth = pageFormat.getImageableWidth();
                double pageHeight = pageFormat.getImageableHeight();

                // Recalculate tiling with actual printer page dimensions
                TileCalculator.TilingResult actualTilingResult;
                if (scale == 1.0f) {
                    actualTilingResult = TileCalculator.calculateSinglePagePreview(image.getWidth(), image.getHeight(), pageWidth, pageHeight);
                } else {
                    actualTilingResult = TileCalculator.calculateScaledTiling(image.getWidth(), image.getHeight(), pageWidth, pageHeight, scale);
                }

                // Get the tile info for this page
                TileCalculator.TileInfo tileInfo = selectedTiles.get(pageIndex);

                // Calculate the actual scaled dimensions for rendering
                // Always use single page baseline approach
                TileCalculator.TilingResult baselineResult = TileCalculator.calculateSinglePagePreview(image.getWidth(), image.getHeight(), pageWidth, pageHeight);
                int renderWidth, renderHeight;
                if (scale == 1.0f) {
                    // For scale 1.0, use the scaled dimensions from single page preview
                    renderWidth = baselineResult.imageWidth;
                    renderHeight = baselineResult.imageHeight;
                } else {
                    // For other scales, scale from the single page baseline
                    renderWidth = (int) (baselineResult.imageWidth * scale);
                    renderHeight = (int) (baselineResult.imageHeight * scale);
                }

                // Calculate position based on actual tile dimensions
                int x = -(int) (tileInfo.col * actualTilingResult.tileWidth);
                int y = -(int) (tileInfo.row * actualTilingResult.tileHeight);

                g2d.drawImage(image, x, y, renderWidth, renderHeight, null);

                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(
                    null,
                    "Printing completed!\n" +
                    "Pages printed: " + selectedTiles.size() + " out of " + (tilingResult.tilesWide * tilingResult.tilesHigh) + " total tiles\n" +
                    "Paper saved: " + (tilingResult.tilesWide * tilingResult.tilesHigh - selectedTiles.size()) + " blank pages avoided!",
                    "Print Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error during printing: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }

    /**
     * Save image to PDF with manual tile selection considerations
     */
    public static void saveTiledImageToPDFWithSelection(BufferedImage image, float scale, boolean isRotated, ImagePanel imagePanel) {
        // Let user choose where to save the PDF
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF As...");
        fileChooser.setSelectedFile(new File("tiled_image.pdf"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".pdf");
            }
            
            @Override
            public String getDescription() {
                return "PDF Files (*.pdf)";
            }
        });

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // User cancelled
        }

        File outputFile = fileChooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(outputFile.getAbsolutePath() + ".pdf");
        }

        // Check if file exists and ask for confirmation
        if (outputFile.exists()) {
            int response = JOptionPane.showConfirmDialog(
                null,
                "File already exists. Do you want to overwrite it?",
                "File Exists",
                JOptionPane.YES_NO_OPTION
            );
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }

        PDDocument document = new PDDocument();
        double pageWidth = PDRectangle.A4.getWidth();
        double pageHeight = PDRectangle.A4.getHeight();
        
        TileCalculator.TilingResult tilingResult;
        if (scale == 1.0f) {
            // At scale 1.0, use single page preview (auto-fit to one page)
            tilingResult = TileCalculator.calculateSinglePagePreview(image.getWidth(), image.getHeight(), pageWidth, pageHeight);
        } else {
            // When scaled, calculate actual tiling for the scaled dimensions
            tilingResult = TileCalculator.calculateScaledTiling(image.getWidth(), image.getHeight(), pageWidth, pageHeight, scale);
        }
        java.util.List<TileCalculator.TileInfo> selectedTiles = imagePanel.getSelectedTiles(tilingResult, image);
        
        // Calculate the actual scaled dimensions for rendering using baseline approach
        TileCalculator.TilingResult baselineResult = TileCalculator.calculateSinglePagePreview(image.getWidth(), image.getHeight(), pageWidth, pageHeight);
        int scaledWidth, scaledHeight;
        if (scale == 1.0f) {
            // For scale 1.0, use the scaled dimensions from single page preview
            scaledWidth = baselineResult.imageWidth;
            scaledHeight = baselineResult.imageHeight;
        } else {
            // For other scales, scale from the single page baseline
            scaledWidth = (int) (baselineResult.imageWidth * scale);
            scaledHeight = (int) (baselineResult.imageHeight * scale);
        }

        File tempFile = null;
        try {
            // Create temporary image file
            tempFile = File.createTempFile("tempImage", ".png");
            ImageIO.write(image, "png", tempFile);
            PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);

            // Calculate the actual tile dimensions based on the optimal orientation
            double actualTileWidth = tilingResult.tileWidth;
            double actualTileHeight = tilingResult.tileHeight;

            // Only draw tiles that are selected by user
            for (TileCalculator.TileInfo tileInfo : selectedTiles) {
                PDPage page = new PDPage(new PDRectangle((float) actualTileWidth, (float) actualTileHeight));
                document.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Calculate the position of this tile within the overall image
                    float sourceX = tileInfo.col * (float) actualTileWidth;
                    float sourceY = tileInfo.row * (float) actualTileHeight;

                    // In PDF coordinate system, origin is at bottom-left
                    // We need to position the image so that the correct portion is visible
                    float imageX = -sourceX;
                    float imageY = (float) actualTileHeight - scaledHeight + sourceY;
                    
                    // Draw the scaled image at the calculated position
                    contentStream.drawImage(
                            pdImage,
                            imageX,
                            imageY,
                            scaledWidth,
                            scaledHeight
                    );

                    // Optionally add border to see the tile area clearly
                    contentStream.setStrokingColor(Color.LIGHT_GRAY);
                    contentStream.setLineWidth(1);
                    contentStream.addRect(0, 0, (float) actualTileWidth, (float) actualTileHeight);
                    contentStream.stroke();
                }
            }

            document.save(outputFile);
            document.close();

            JOptionPane.showMessageDialog(
                null,
                "PDF saved successfully to: " + outputFile.getAbsolutePath() + "\n" +
                "Pages with content: " + selectedTiles.size() + " out of " + (tilingResult.tilesWide * tilingResult.tilesHigh) + " total tiles\n" +
                "Paper saved: " + (tilingResult.tilesWide * tilingResult.tilesHigh - selectedTiles.size()) + " blank pages avoided!",
                "PDF Saved",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error saving PDF: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
