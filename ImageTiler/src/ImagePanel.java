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
    private float cachedRotationAngle = -1;
    
    // Manual tile selection - three states: auto (default), manually excluded, manually included
    private java.util.Set<String> manuallyExcludedTiles = new java.util.HashSet<>();
    private java.util.Set<String> manuallyIncludedTiles = new java.util.HashSet<>();
    private boolean manualSelectionMode = false;
    
    // For handling mouse clicks on tiles
    private TileCalculator.TilingResult currentTilingResult;
    private int lastDrawX, lastDrawY, lastDrawWidth, lastDrawHeight;
    private int lastImageWidth, lastImageHeight;
    
    // Zoom and pan functionality
    private double zoomFactor = 1.0;
    private double panX = 0;
    private double panY = 0;
    private Point lastPanPoint;
    private boolean isPanning = false;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 10.0;
    private static final double ZOOM_INCREMENT = 0.1;
    
    public ImagePanel() {
        super();
        this.settings = Settings.getInstance();
        
        // Add mouse listeners for tile selection and panning
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isPanning) {
snapToGrid(e.getPoint());
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
                    // Start panning with right click or Ctrl+click
                    lastPanPoint = e.getPoint();
                    isPanning = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isPanning) {
                    isPanning = false;
                    setCursor(Cursor.getDefaultCursor());
                    lastPanPoint = null;
                }
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && lastPanPoint != null) {
                    double dx = e.getX() - lastPanPoint.x;
                    double dy = e.getY() - lastPanPoint.y;
                    panX += dx / zoomFactor;
                    panY += dy / zoomFactor;
                    lastPanPoint = e.getPoint();
                    repaint();
                }
            }
        });
        
        // Add mouse wheel listener for zooming
        addMouseWheelListener(e -> {
            double oldZoom = zoomFactor;
            if (e.getWheelRotation() < 0) {
                // Zoom in
                zoomFactor = Math.min(MAX_ZOOM, zoomFactor * 1.1);
            } else {
                // Zoom out
                zoomFactor = Math.max(MIN_ZOOM, zoomFactor / 1.1);
            }
            
            if (Math.abs(zoomFactor - oldZoom) > 0.001) {
                // Adjust pan to zoom towards mouse position
                Point mousePos = e.getPoint();
                double centerX = getWidth() / 2.0;
                double centerY = getHeight() / 2.0;
                
                // Calculate offset from center
                double offsetX = mousePos.x - centerX;
                double offsetY = mousePos.y - centerY;
                
                // Adjust pan based on zoom change
                double zoomChange = zoomFactor / oldZoom;
                panX = panX + (offsetX / oldZoom) * (1 - 1/zoomChange);
                panY = panY + (offsetY / oldZoom) * (1 - 1/zoomChange);
                
                repaint();
            }
        });
}

private void snapToGrid(Point clickPoint) {
    // For calibration images, snap to grid intersections
    if (isCalibrationImage()) {
        // Calculate nearest grid intersection based on known grid spacing
        int gridX = Math.round(clickPoint.x / (float)GridParameters.GRID_SPACING) * GridParameters.GRID_SPACING;
        int gridY = Math.round(clickPoint.y / (float)GridParameters.GRID_SPACING) * GridParameters.GRID_SPACING;
        
        // Create a synthetic mouse event at the snapped position
        Point snappedPoint = new Point(gridX, gridY);
        handleTileClick(snappedPoint);
    } else {
        // For regular images, use the original click point
        handleTileClick(clickPoint);
    }
}

public void setImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
            rotatedImage = image;
            rotationAngle = 0;
            resetZoomAndPan(); // Reset zoom and pan when new image is loaded
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
        resetZoomAndPan(); // Reset zoom and pan when scale changes
        invalidateCache(); // Clear cache when scale changes
        repaint();
    }

    public void rotateImage() {
        rotationAngle += 90;
        if (rotationAngle == 360) {
            rotationAngle = 0;
        }
        rotatedImage = rotateBufferedImage(image, rotationAngle);
        
        // Clear manual selections when rotating to avoid position mismatch
        clearManualSelections();
        resetZoomAndPan(); // Reset zoom and pan when rotating
        
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
            
            double pageWidth = 8.27 * 72; // A4 width in points (portrait)
            double pageHeight = 11.69 * 72; // A4 height in points (portrait)
            
            // Calculate the effective image dimensions based on scale
            // Always use single page baseline as reference
            TileCalculator.TilingResult baselineResult = TileCalculator.calculateSinglePagePreview(rotatedImage.getWidth(), rotatedImage.getHeight(), pageWidth, pageHeight);
            
            int effectiveImageWidth, effectiveImageHeight;
            TileCalculator.TilingResult tilingResult;
            
            if (scale == 1.0f) {
                // At scale 1.0, use single page preview dimensions
                tilingResult = baselineResult;
                effectiveImageWidth = tilingResult.imageWidth;
                effectiveImageHeight = tilingResult.imageHeight;
            } else {
                // When scaled, scale from the single page baseline
                effectiveImageWidth = (int) (baselineResult.imageWidth * scale);
                effectiveImageHeight = (int) (baselineResult.imageHeight * scale);
                tilingResult = TileCalculator.calculateScaledTiling(rotatedImage.getWidth(), rotatedImage.getHeight(), pageWidth, pageHeight, scale);
            }

            // Calculate display dimensions based on effective image size
            float aspectRatio = (float) effectiveImageWidth / effectiveImageHeight;
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

            // Apply zoom and pan transformations
            int baseX = (panelWidth - drawWidth) / 2 + margin;
            int baseY = (panelHeight - drawHeight) / 2 + margin;
            
            // Apply zoom transformation
            Graphics2D g2dTransformed = (Graphics2D) g2d.create();
            g2dTransformed.translate(panelWidth / 2.0, panelHeight / 2.0);
            g2dTransformed.scale(zoomFactor, zoomFactor);
            g2dTransformed.translate(-panelWidth / 2.0 + panX, -panelHeight / 2.0 + panY);
            
            int x = baseX;
            int y = baseY;
            
            g2dTransformed.drawImage(rotatedImage, x, y, drawWidth, drawHeight, this);

            double tileWidthScaled = drawWidth / (double) effectiveImageWidth * tilingResult.tileWidth;
            double tileHeightScaled = drawHeight / (double) effectiveImageHeight * tilingResult.tileHeight;

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
                lastImageWidth = effectiveImageWidth;
                lastImageHeight = effectiveImageHeight;
                g2dTransformed.dispose();
                g2d.dispose();
                return;
            }
            
            g2dTransformed.setStroke(new BasicStroke(settings.getGridLineWidth()));

            for (int row = 0; row < tilingResult.tilesHigh; row++) {
                for (int col = 0; col < tilingResult.tilesWide; col++) {
                    int tileX = x + (int) (col * tileWidthScaled);
                    int tileY = y + (int) (row * tileHeightScaled);
                    int width = (int) Math.min(tileWidthScaled, drawWidth - col * tileWidthScaled);
                    int height = (int) Math.min(tileHeightScaled, drawHeight - row * tileHeightScaled);

                    String tileKey = col + "," + row;
                    boolean isNonBlank = nonBlankPositions.contains(tileKey);
                    boolean isExcluded = manuallyExcludedTiles.contains(tileKey);
                    boolean isIncluded = manuallyIncludedTiles.contains(tileKey);
                    
                    // Draw semi-transparent overlay for different tile states
                    if (isExcluded) {
                        // Red overlay for excluded tiles
                        Color excludedColor = settings.getExcludedColor();
                        g2dTransformed.setColor(new Color(excludedColor.getRed(), excludedColor.getGreen(), excludedColor.getBlue(), 120));
                        g2dTransformed.fillRect(tileX, tileY, width, height);
                    } else if (isIncluded) {
                        // Blue overlay for manually included tiles
                        g2dTransformed.setColor(new Color(0, 100, 255, 100));
                        g2dTransformed.fillRect(tileX, tileY, width, height);
                    } else if (isNonBlank) {
                        // Green tint for auto-selected tiles
                        Color gridColor = settings.getGridColor();
                        g2dTransformed.setColor(new Color(gridColor.getRed(), gridColor.getGreen(), gridColor.getBlue(), 60));
                        g2dTransformed.fillRect(tileX, tileY, width, height);
                    }

                    // Draw border around tile
                    if (isExcluded) {
                        g2dTransformed.setColor(settings.getExcludedColor()); // Red border for excluded
                        g2dTransformed.setStroke(new BasicStroke(settings.getGridLineWidth() + 1));
                    } else if (isIncluded) {
                        g2dTransformed.setColor(new Color(0, 100, 255)); // Blue border for manually included
                        g2dTransformed.setStroke(new BasicStroke(settings.getGridLineWidth() + 1));
                    } else if (isNonBlank) {
                        g2dTransformed.setColor(settings.getGridColor()); // Green border for auto-selected
                        g2dTransformed.setStroke(new BasicStroke(settings.getGridLineWidth()));
                    } else {
                        g2dTransformed.setColor(Color.GRAY); // Gray border for blank
                        g2dTransformed.setStroke(new BasicStroke(1));
                    }
                    g2dTransformed.drawRect(tileX, tileY, width, height);
                    
                    // Add tile numbers if enabled in settings
                    if (settings.isShowTileNumbers()) {
                        g2dTransformed.setColor(Color.BLACK);
                        g2dTransformed.setFont(new Font("Arial", Font.BOLD, 12));
                        int tileNum = row * tilingResult.tilesWide + col + 1;
                        g2dTransformed.drawString(String.valueOf(tileNum), tileX + width / 2 - 6, tileY + height / 2 + 6);
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
            
            // Draw enhanced information overlay with modern styling
            drawModernInfoPanel(g2d, selectedTiles, tilingResult, excludedNonBlankTiles);

            // Save current drawing parameters for mouse handling
            currentTilingResult = tilingResult;
            lastDrawX = x;
            lastDrawY = y;
            lastDrawWidth = drawWidth;
            lastDrawHeight = drawHeight;
            lastImageWidth = effectiveImageWidth;
            lastImageHeight = effectiveImageHeight;

            g2dTransformed.dispose();
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
        cachedRotationAngle = -1;
    }
    
    /**
     * Gets non-blank tiles using cache or recalculates if needed
     */
    private java.util.List<TileCalculator.TileInfo> getCachedNonBlankTiles(TileCalculator.TilingResult tilingResult, BufferedImage currentImage) {
        // Check if we need to recalculate
        boolean needsRecalculation = cachedNonBlankTiles == null ||
                                      cachedScale != scale ||
                                      cachedAnalysisImage != currentImage ||
                                      cachedRotationAngle != rotationAngle ||
                                      !tilingResultsEqual(cachedTilingResult, tilingResult);
        
        if (needsRecalculation) {
            // Recalculate and cache
            cachedNonBlankTiles = TileCalculator.getNonBlankTiles(tilingResult, currentImage);
            cachedTilingResult = tilingResult;
            cachedScale = scale;
            cachedAnalysisImage = currentImage;
            cachedRotationAngle = rotationAngle;
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
     * Checks if the current loaded image is a calibration image
     */
    private boolean isCalibrationImage() {
        BufferedImage image = getRotatedImage();
        if (image == null) return false;
        
        // Check if dimensions match known calibration image dimensions
        // The calibration image is 3300 x 2550 pixels
        return (image.getWidth() == 3300 && image.getHeight() == 2550) ||
               (image.getWidth() == 2550 && image.getHeight() == 3300); // Account for rotation
    }
    
    /**
     * Handles tile clicks using a Point coordinate
     */
    private void handleTileClick(Point point) {
        if (currentTilingResult == null) return;
        
        // Transform coordinates to account for zoom and pan
        double transformedX = (point.x - getWidth() / 2.0) / zoomFactor + getWidth() / 2.0 - panX;
        double transformedY = (point.y - getHeight() / 2.0) / zoomFactor + getHeight() / 2.0 - panY;
        
        double tileWidthScaled = (double) lastDrawWidth / currentTilingResult.tilesWide;
        double tileHeightScaled = (double) lastDrawHeight / currentTilingResult.tilesHigh;

        int col = (int) ((transformedX - lastDrawX) / tileWidthScaled);
        int row = (int) ((transformedY - lastDrawY) / tileHeightScaled);

        // Check if click is within bounds
        if (col >= 0 && col < currentTilingResult.tilesWide && row >= 0 && row < currentTilingResult.tilesHigh) {
            String tileKey = col + "," + row;
            
            // Determine current state and cycle to next state
            boolean isExcluded = manuallyExcludedTiles.contains(tileKey);
            boolean isIncluded = manuallyIncludedTiles.contains(tileKey);
            
            if (!isExcluded && !isIncluded) {
                // Auto → Excluded
                manuallyExcludedTiles.add(tileKey);
            } else if (isExcluded) {
                // Excluded → Included
                manuallyExcludedTiles.remove(tileKey);
                manuallyIncludedTiles.add(tileKey);
            } else if (isIncluded) {
                // Included → Auto
                manuallyIncludedTiles.remove(tileKey);
            }
            
            repaint();
        }
    }
    
    /**
     * Handles mouse clicks on tiles for manual selection
     * Three-state toggle: auto → excluded → included → auto
     */
    private void handleTileClick(MouseEvent e) {
        if (currentTilingResult == null) return;
        
        // Transform mouse coordinates to account for zoom and pan
        double transformedX = (e.getX() - getWidth() / 2.0) / zoomFactor + getWidth() / 2.0 - panX;
        double transformedY = (e.getY() - getHeight() / 2.0) / zoomFactor + getHeight() / 2.0 - panY;
        
        double tileWidthScaled = (double) lastDrawWidth / currentTilingResult.tilesWide;
        double tileHeightScaled = (double) lastDrawHeight / currentTilingResult.tilesHigh;

        int col = (int) ((transformedX - lastDrawX) / tileWidthScaled);
        int row = (int) ((transformedY - lastDrawY) / tileHeightScaled);

        // Check if click is within bounds
        if (col >= 0 && col < currentTilingResult.tilesWide && row >= 0 && row < currentTilingResult.tilesHigh) {
            String tileKey = col + "," + row;
            
            // Determine current state and cycle to next state
            boolean isExcluded = manuallyExcludedTiles.contains(tileKey);
            boolean isIncluded = manuallyIncludedTiles.contains(tileKey);
            
            if (!isExcluded && !isIncluded) {
                // Auto → Excluded
                manuallyExcludedTiles.add(tileKey);
            } else if (isExcluded) {
                // Excluded → Included
                manuallyExcludedTiles.remove(tileKey);
                manuallyIncludedTiles.add(tileKey);
            } else if (isIncluded) {
                // Included → Auto
                manuallyIncludedTiles.remove(tileKey);
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
     * Refreshes the display after settings changes
     */
    public void refreshDisplay() {
        invalidateCache();
        repaint();
    }
    
    /**
     * Gets the final list of tiles to print/save (auto-selected + manually included - manually excluded)
     */
    public java.util.List<TileCalculator.TileInfo> getSelectedTiles(TileCalculator.TilingResult tilingResult, BufferedImage image) {
        java.util.List<TileCalculator.TileInfo> allNonBlankTiles = TileCalculator.getNonBlankTiles(tilingResult, image);
        java.util.List<TileCalculator.TileInfo> selectedTiles = new java.util.ArrayList<>();
        java.util.Set<String> addedTiles = new java.util.HashSet<>();
        
        // Add auto-selected tiles (not manually excluded)
        for (TileCalculator.TileInfo tile : allNonBlankTiles) {
            String tileKey = tile.col + "," + tile.row;
            if (!manuallyExcludedTiles.contains(tileKey)) {
                selectedTiles.add(tile);
                addedTiles.add(tileKey);
            }
        }
        
        // Add manually included tiles (even if they weren't auto-selected)
        for (String tileKey : manuallyIncludedTiles) {
            if (!addedTiles.contains(tileKey)) {
                String[] parts = tileKey.split(",");
                int col = Integer.parseInt(parts[0]);
                int row = Integer.parseInt(parts[1]);
                int tileNumber = row * tilingResult.tilesWide + col + 1;
                selectedTiles.add(new TileCalculator.TileInfo(col, row, tileNumber));
            }
        }
        
        return selectedTiles;
    }
    
    /**
     * Draws a modern styled information panel
     */
    private void drawModernInfoPanel(Graphics2D g2d, int selectedTiles, TileCalculator.TilingResult tilingResult, int excludedNonBlankTiles) {
        // Create a rounded rectangle background
        int panelWidth = 350;
        int panelHeight = 220;
        int panelX = 15;
        int panelY = 15;
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(panelX + 2, panelY + 2, panelWidth, panelHeight, 15, 15);
        
        // Draw main panel background
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Draw border
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Header section
        g2d.setColor(new Color(60, 120, 180));
        g2d.fillRoundRect(panelX, panelY, panelWidth, 35, 15, 15);
        g2d.fillRect(panelX, panelY + 20, panelWidth, 15);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.drawString("[i] Tiling Information", panelX + 15, panelY + 23);
        
        // Content area
        int contentY = panelY + 50;
        int lineHeight = 20;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        // Pages to print (highlighted)
        g2d.setColor(new Color(34, 139, 34));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("[#] Pages to print: " + selectedTiles, panelX + 15, contentY);
        contentY += lineHeight + 3;
        
        // Grid size
        g2d.setColor(new Color(60, 60, 60));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.drawString("[G] Grid size: " + tilingResult.tilesWide + " x " + tilingResult.tilesHigh, panelX + 15, contentY);
        contentY += lineHeight;
        
        // Paper saved (environmental impact)
        int totalBlankPages = tilingResult.tilesWide * tilingResult.tilesHigh - selectedTiles;
        g2d.setColor(new Color(76, 175, 80));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.drawString("[E] Paper saved: " + totalBlankPages + " pages!", panelX + 15, contentY);
        contentY += lineHeight;
        
        // Manually excluded tiles
        if (excludedNonBlankTiles > 0) {
            g2d.setColor(new Color(255, 152, 0));
            g2d.drawString("[-] Manually excluded: " + excludedNonBlankTiles + " tiles", panelX + 15, contentY);
            contentY += lineHeight;
        }
        
        // Page size
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("[P] Page Size: " + String.format("%.1f x %.1f inches", 
            tilingResult.tileWidth / 72.0, tilingResult.tileHeight / 72.0), panelX + 15, contentY);
        contentY += lineHeight;
        
        // Scale information
        if (scale != 1.0f) {
            g2d.setColor(new Color(63, 81, 181));
            g2d.drawString("[S] Scale: " + String.format("%.2fx", scale), panelX + 15, contentY);
            contentY += lineHeight;
        }
        
        // Zoom information
        if (zoomFactor != 1.0) {
            g2d.setColor(new Color(156, 39, 176));
            g2d.drawString("[Z] Zoom: " + String.format("%.1fx", zoomFactor), panelX + 15, contentY);
            contentY += lineHeight;
        }
        
        // Controls information
        g2d.setColor(new Color(96, 125, 139));
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2d.drawString("[?] Mouse wheel: zoom | Right-click + drag: pan", panelX + 15, contentY);
        contentY += 15;
        
        // Legend section with modern styling
        contentY += 5;
        g2d.setColor(new Color(120, 120, 120));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.drawString("Legend:", panelX + 15, contentY);
        contentY += 15;
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        // Will be printed (auto-selected)
        g2d.setColor(settings.getGridColor());
        g2d.fillRect(panelX + 20, contentY - 8, 12, 10);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("Will be printed", panelX + 38, contentY);
        contentY += 16;
        
        // Excluded
        g2d.setColor(settings.getExcludedColor());
        g2d.fillRect(panelX + 20, contentY - 8, 12, 10);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("Excluded (click to toggle)", panelX + 38, contentY);
        contentY += 16;
        
        // Manually included
        g2d.setColor(new Color(0, 100, 255));
        g2d.fillRect(panelX + 20, contentY - 8, 12, 10);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("Manually included", panelX + 38, contentY);
        contentY += 16;
        
        // Blank
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(panelX + 20, contentY - 8, 12, 10);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(panelX + 20, contentY - 8, 12, 10);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("Blank (auto-skipped)", panelX + 38, contentY);
    }
    
    /**
     * Resets zoom and pan to default values
     */
    public void resetZoomAndPan() {
        zoomFactor = 1.0;
        panX = 0;
        panY = 0;
    }
    
    /**
     * Gets current zoom factor
     */
    public double getZoomFactor() {
        return zoomFactor;
    }
    
    /**
     * Sets zoom factor with bounds checking
     */
    public void setZoomFactor(double zoom) {
        this.zoomFactor = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        repaint();
    }
    
}
