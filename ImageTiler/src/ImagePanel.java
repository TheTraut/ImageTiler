import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JPanel {
    private BufferedImage image;
    private BufferedImage rotatedImage;
    private float rotationAngle = 0;
    private float scale = 1.0f;
    private Settings settings;
    
    // Cache for tile analysis to avoid recalculating every paint
    private java.util.List<TileCalculator.TileInfo> cachedNonBlankTiles;
    private TileCalculator.TilingResult cachedTilingResult;
    private float cachedScale = -1;
    private BufferedImage cachedAnalysisImage;
    
    // Manual tile selection
    private java.util.Set<String> manuallyExcludedTiles = new java.util.HashSet<>();
    private java.util.Set<String> manuallyIncludedTiles = new java.util.HashSet<>();
    private boolean manualSelectionMode = false;
    
    // For handling mouse clicks on tiles
    private TileCalculator.TilingResult currentTilingResult;
    private int lastDrawX, lastDrawY, lastDrawWidth, lastDrawHeight;
    private int lastImageWidth, lastImageHeight;
    
    public ImagePanel() {
        super();
        this.settings = Settings.getInstance();
        
        // Add mouse listener for tile selection
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTileClick(e);
            }
        });
    }

    public void setImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
            rotatedImage = image;
            rotationAngle = 0;
            invalidateCache(); // Clear cache when new image is loaded
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage getRotatedImage() {
        return rotatedImage;
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidateCache(); // Clear cache when scale changes
        repaint();
    }

    public void rotateImage() {
        rotationAngle += 90;
        if (rotationAngle == 360) {
            rotationAngle = 0;
        }
        rotatedImage = rotateBufferedImage(image, rotationAngle);
        invalidateCache(); // Clear cache when image is rotated
        repaint();
    }

    private BufferedImage rotateBufferedImage(BufferedImage img, float angle) {
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = angle == 90 || angle == 270 ? h : w;
        int newHeight = angle == 90 || angle == 270 ? w : h;
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, img.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.rotate(Math.toRadians(angle), newWidth / 2.0, newHeight / 2.0);
        graphic.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);
        graphic.drawImage(img, 0, 0, null);
        graphic.dispose();
        return rotated;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (rotatedImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = (int) (rotatedImage.getWidth() * scale);
            int imageHeight = (int) (rotatedImage.getHeight() * scale);

            float aspectRatio = (float) imageWidth / imageHeight;
            int drawWidth = panelWidth;
            int drawHeight = (int) (panelWidth / aspectRatio);

            if (drawHeight > panelHeight) {
                drawHeight = panelHeight;
                drawWidth = (int) (panelHeight * aspectRatio);
            }

            // Define margin
            int margin = 20;
            drawWidth -= 2 * margin;
            drawHeight -= 2 * margin;

            int x = (panelWidth - drawWidth) / 2 + margin;
            int y = (panelHeight - drawHeight) / 2 + margin;

            g2d.drawImage(rotatedImage, x, y, drawWidth, drawHeight, this);

            double pageWidth = 8.27 * 72; // A4 width in points (portrait)
            double pageHeight = 11.69 * 72; // A4 height in points (portrait)

            TileCalculator.TilingResult tilingResult = TileCalculator.calculateOptimalTiling(imageWidth, imageHeight, pageWidth, pageHeight);

            double tileWidthScaled = drawWidth / (double) imageWidth * tilingResult.tileWidth;
            double tileHeightScaled = drawHeight / (double) imageHeight * tilingResult.tileHeight;

            // Get non-blank tiles using cached analysis or recalculate if needed
            java.util.List<TileCalculator.TileInfo> nonBlankTiles = getCachedNonBlankTiles(tilingResult, rotatedImage);
            java.util.Set<String> nonBlankPositions = new java.util.HashSet<>();
            for (TileCalculator.TileInfo tile : nonBlankTiles) {
                nonBlankPositions.add(tile.col + "," + tile.row);
            }

            // Draw tile grid only if enabled in settings
            if (!settings.isShowGrid()) {
                // Store current drawing parameters for mouse handling even if grid is hidden
                currentTilingResult = tilingResult;
                lastDrawX = x;
                lastDrawY = y;
                lastDrawWidth = drawWidth;
                lastDrawHeight = drawHeight;
                lastImageWidth = imageWidth;
                lastImageHeight = imageHeight;
                g2d.dispose();
                return;
            }
            
            g2d.setStroke(new BasicStroke(settings.getGridLineWidth()));

            for (int row = 0; row < tilingResult.tilesHigh; row++) {
                for (int col = 0; col < tilingResult.tilesWide; col++) {
                    int tileX = x + (int) (col * tileWidthScaled);
                    int tileY = y + (int) (row * tileHeightScaled);
                    int width = (int) Math.min(tileWidthScaled, drawWidth - col * tileWidthScaled);
                    int height = (int) Math.min(tileHeightScaled, drawHeight - row * tileHeightScaled);

                    boolean isNonBlank = nonBlankPositions.contains(col + "," + row);
                    
                    // Draw semi-transparent overlay for different tile states
                    if (manuallyExcludedTiles.contains(col + "," + row)) {
                        // Semi-transparent overlay for excluded tiles using settings color
                        Color excludedColor = settings.getExcludedColor();
                        g2d.setColor(new Color(excludedColor.getRed(), excludedColor.getGreen(), excludedColor.getBlue(), 120));
                        g2d.fillRect(tileX, tileY, width, height);
                    } else if (isNonBlank) {
                        // Light tint for tiles that will be printed using settings grid color
                        Color gridColor = settings.getGridColor();
                        g2d.setColor(new Color(gridColor.getRed(), gridColor.getGreen(), gridColor.getBlue(), 60));
                        g2d.fillRect(tileX, tileY, width, height);
                    }

                    // Draw border around tile
                    if (manuallyExcludedTiles.contains(col + "," + row)) {
                        g2d.setColor(settings.getExcludedColor()); // Use settings color for excluded
                        g2d.setStroke(new BasicStroke(settings.getGridLineWidth() + 1));
                    } else if (isNonBlank) {
                        g2d.setColor(settings.getGridColor()); // Use settings color for included
                        g2d.setStroke(new BasicStroke(settings.getGridLineWidth()));
                    } else {
                        g2d.setColor(Color.GRAY); // Gray border for blank
                        g2d.setStroke(new BasicStroke(1));
                    }
                    g2d.drawRect(tileX, tileY, width, height);
                    
                    // Add tile numbers if enabled in settings
                    if (settings.isShowTileNumbers()) {
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(new Font("Arial", Font.BOLD, 12));
                        int tileNum = row * tilingResult.tilesWide + col + 1;
                        g2d.drawString(String.valueOf(tileNum), tileX + width / 2 - 6, tileY + height / 2 + 6);
                    }
                }
            }
            
            // Calculate selected tiles count
            int selectedTiles = 0;
            int excludedNonBlankTiles = 0;
            for (TileCalculator.TileInfo tile : nonBlankTiles) {
                String tileKey = tile.col + "," + tile.row;
                if (!manuallyExcludedTiles.contains(tileKey)) {
                    selectedTiles++;
                } else {
                    excludedNonBlankTiles++;
                }
            }
            
            // Draw information overlay
            g2d.setColor(new Color(0, 0, 0, 140)); // Semi-transparent black
            g2d.fillRect(10, 10, 320, 180);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Tiling Information:", 20, 30);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Pages to print: " + selectedTiles, 20, 50);
            g2d.drawString("Grid size: " + tilingResult.tilesWide + " × " + tilingResult.tilesHigh, 20, 70);
            
            int totalBlankPages = tilingResult.tilesWide * tilingResult.tilesHigh - selectedTiles;
            g2d.setColor(new Color(144, 238, 144)); // Light green
            g2d.drawString("Paper saved: " + totalBlankPages + " pages!", 20, 90);
            
            if (excludedNonBlankTiles > 0) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("Manually excluded: " + excludedNonBlankTiles + " tiles", 20, 110);
            }
            
            g2d.setColor(Color.WHITE);
            g2d.drawString("Page Size: " + String.format("%.1f × %.1f inches", 
                tilingResult.tileWidth / 72.0, tilingResult.tileHeight / 72.0), 20, 130);
            
            // Show scale information if scaled
            if (scale != 1.0f) {
                g2d.drawString("Scale: " + String.format("%.2fx", scale), 20, 150);
            }
            
            // Legend with better visual indicators using settings colors
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.setColor(settings.getGridColor()); // Use settings grid color
            g2d.drawString("■ Will be printed (custom color)", 20, 170);
            g2d.setColor(settings.getExcludedColor()); // Use settings excluded color
            g2d.drawString("■ Excluded (custom color - click to toggle)", 20, 185);
            g2d.setColor(Color.GRAY);
            g2d.drawString("■ Blank (gray border - auto-skipped)", 20, 200);

            // Save current drawing parameters for mouse handling
            currentTilingResult = tilingResult;
            lastDrawX = x;
            lastDrawY = y;
            lastDrawWidth = drawWidth;
            lastDrawHeight = drawHeight;
            lastImageWidth = imageWidth;
            lastImageHeight = imageHeight;

            g2d.dispose();
        }
    }
    
    /**
     * Invalidates the tile analysis cache
     */
    private void invalidateCache() {
        cachedNonBlankTiles = null;
        cachedTilingResult = null;
        cachedScale = -1;
        cachedAnalysisImage = null;
    }
    
    /**
     * Gets non-blank tiles using cache or recalculates if needed
     */
    private java.util.List<TileCalculator.TileInfo> getCachedNonBlankTiles(TileCalculator.TilingResult tilingResult, BufferedImage currentImage) {
        // Check if we need to recalculate
        boolean needsRecalculation = cachedNonBlankTiles == null ||
                                      cachedScale != scale ||
                                      cachedAnalysisImage != currentImage ||
                                      !tilingResultsEqual(cachedTilingResult, tilingResult);
        
        if (needsRecalculation) {
            // Recalculate and cache
            cachedNonBlankTiles = TileCalculator.getNonBlankTiles(tilingResult, currentImage);
            cachedTilingResult = tilingResult;
            cachedScale = scale;
            cachedAnalysisImage = currentImage;
        }
        
        return cachedNonBlankTiles;
    }
    
    /**
     * Compares two tiling results for equality
     */
    private boolean tilingResultsEqual(TileCalculator.TilingResult a, TileCalculator.TilingResult b) {
        if (a == null || b == null) return false;
        return a.tilesWide == b.tilesWide &&
               a.tilesHigh == b.tilesHigh &&
               Math.abs(a.tileWidth - b.tileWidth) < 0.01 &&
               Math.abs(a.tileHeight - b.tileHeight) < 0.01 &&
               a.imageWidth == b.imageWidth &&
               a.imageHeight == b.imageHeight;
    }
    
    /**
     * Handles mouse clicks on tiles for manual selection
     */
    private void handleTileClick(MouseEvent e) {
        if (currentTilingResult == null) return;
        
        int clickX = e.getX();
        int clickY = e.getY();
        
        double tileWidthScaled = (double) lastDrawWidth / currentTilingResult.tilesWide;
        double tileHeightScaled = (double) lastDrawHeight / currentTilingResult.tilesHigh;

        int col = (int) ((clickX - lastDrawX) / tileWidthScaled);
        int row = (int) ((clickY - lastDrawY) / tileHeightScaled);

        // Check if click is within bounds
        if (col >= 0 && col < currentTilingResult.tilesWide && row >= 0 && row < currentTilingResult.tilesHigh) {
            String tileKey = col + "," + row;
            if (manuallyExcludedTiles.contains(tileKey)) {
                manuallyExcludedTiles.remove(tileKey);
            } else {
                manuallyExcludedTiles.add(tileKey);
            }
            repaint();
        }
    }
    
    /**
     * Gets the set of manually excluded tiles
     */
    public java.util.Set<String> getManuallyExcludedTiles() {
        return manuallyExcludedTiles;
    }
    
    /**
     * Clears all manual tile selections
     */
    public void clearManualSelections() {
        manuallyExcludedTiles.clear();
        manuallyIncludedTiles.clear();
        repaint();
    }
    
    /**
     * Gets the final list of tiles to print/save (excluding manually excluded ones)
     */
    public java.util.List<TileCalculator.TileInfo> getSelectedTiles(TileCalculator.TilingResult tilingResult, BufferedImage image) {
        java.util.List<TileCalculator.TileInfo> allNonBlankTiles = TileCalculator.getNonBlankTiles(tilingResult, image);
        java.util.List<TileCalculator.TileInfo> selectedTiles = new java.util.ArrayList<>();
        
        for (TileCalculator.TileInfo tile : allNonBlankTiles) {
            String tileKey = tile.col + "," + tile.row;
            if (!manuallyExcludedTiles.contains(tileKey)) {
                selectedTiles.add(tile);
            }
        }
        
        return selectedTiles;
    }
    
    /**
     * Refreshes the display to reflect current settings
     */
    public void refreshDisplay() {
        repaint();
    }
}
