public class TileCalculator {

    public static TilingResult calculateOptimalTiling(int imageWidth, int imageHeight, double pageWidth, double pageHeight) {
        int tilesWidePortrait = (int) Math.ceil((double) imageWidth / pageWidth);
        int tilesHighPortrait = (int) Math.ceil((double) imageHeight / pageHeight);

        int tilesWideLandscape = (int) Math.ceil((double) imageWidth / pageHeight);
        int tilesHighLandscape = (int) Math.ceil((double) imageHeight / pageWidth);

        int totalTilesPortrait = tilesWidePortrait * tilesHighPortrait;
        int totalTilesLandscape = tilesWideLandscape * tilesHighLandscape;

        boolean useLandscape = totalTilesLandscape < totalTilesPortrait;

        if (useLandscape) {
            return new TilingResult(tilesWideLandscape, tilesHighLandscape, pageHeight, pageWidth, imageWidth, imageHeight);
        } else {
            return new TilingResult(tilesWidePortrait, tilesHighPortrait, pageWidth, pageHeight, imageWidth, imageHeight);
        }
    }

    /**
     * Determines if a tile at the given position contains meaningful image content
     */
    public static boolean tileContainsImage(int col, int row, TilingResult tilingResult) {
        return tileContainsMeaningfulContent(col, row, tilingResult, null);
    }
    
    /**
     * Determines if a tile contains meaningful image content by analyzing actual pixels
     */
    public static boolean tileContainsMeaningfulContent(int col, int row, TilingResult tilingResult, java.awt.image.BufferedImage image) {
        // Calculate tile boundaries in the scaled image space
        double tileStartX = col * tilingResult.tileWidth;
        double tileStartY = row * tilingResult.tileHeight;
        double tileEndX = Math.min((col + 1) * tilingResult.tileWidth, tilingResult.imageWidth);
        double tileEndY = Math.min((row + 1) * tilingResult.tileHeight, tilingResult.imageHeight);
        
        // Check if the tile actually has any content (width and height > 0)
        boolean hasContent = (tileEndX > tileStartX) && (tileEndY > tileStartY);
        boolean intersectsImage = tileStartX < tilingResult.imageWidth && tileStartY < tilingResult.imageHeight;
        
        if (!hasContent || !intersectsImage) {
            return false;
        }
        
        // If we don't have the image for pixel analysis, use basic bounds checking
        if (image == null) {
            // For a more conservative approach, consider tiles on the edges that are very small
            double tileWidth = tileEndX - tileStartX;
            double tileHeight = tileEndY - tileStartY;
            double tileArea = tileWidth * tileHeight;
            double fullTileArea = tilingResult.tileWidth * tilingResult.tileHeight;
            
            // If the tile area is less than 10% of a full tile, consider it mostly empty
            return (tileArea / fullTileArea) > 0.1;
        }
        
        // Convert scaled image coordinates back to original image coordinates for pixel analysis
        // The tilingResult.imageWidth/Height represent the scaled dimensions
        // We need to map back to the original image dimensions
        double scaleFactorX = (double) image.getWidth() / tilingResult.imageWidth;
        double scaleFactorY = (double) image.getHeight() / tilingResult.imageHeight;
        
        int originalStartX = (int) (tileStartX * scaleFactorX);
        int originalStartY = (int) (tileStartY * scaleFactorY);
        int originalEndX = (int) Math.min(tileEndX * scaleFactorX, image.getWidth());
        int originalEndY = (int) Math.min(tileEndY * scaleFactorY, image.getHeight());
        
        // Pixel-based analysis using original image coordinates
        return analyzePixelContent(image, originalStartX, originalStartY, originalEndX, originalEndY);
    }
    
    /**
     * Analyzes the actual pixel content within a tile region
     */
    private static boolean analyzePixelContent(java.awt.image.BufferedImage image, int startX, int startY, int endX, int endY) {
        if (image == null) return true;
        
        int meaningfulPixels = 0;
        int totalPixels = 0;
        int sampleStep = Math.max(1, (endX - startX) / 20); // Sample every few pixels for performance
        
        for (int y = startY; y < endY && y < image.getHeight(); y += sampleStep) {
            for (int x = startX; x < endX && x < image.getWidth(); x += sampleStep) {
                totalPixels++;
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                
                // Consider a pixel meaningful if it's not completely transparent
                // and not pure white (common background color)
                if (alpha > 50 && rgb != 0xFFFFFFFF) {
                    meaningfulPixels++;
                }
            }
        }
        
        if (totalPixels == 0) return false;
        
        // Consider the tile meaningful if more than 5% of sampled pixels have content
        double contentRatio = (double) meaningfulPixels / totalPixels;
        return contentRatio > 0.05;
    }

    /**
     * Gets a list of all tiles that contain image content (non-blank tiles)
     */
    public static java.util.List<TileInfo> getNonBlankTiles(TilingResult tilingResult) {
        return getNonBlankTiles(tilingResult, null);
    }
    
    /**
     * Gets a list of all tiles that contain image content (non-blank tiles)
     * Uses pixel analysis if image is provided
     */
    public static java.util.List<TileInfo> getNonBlankTiles(TilingResult tilingResult, java.awt.image.BufferedImage image) {
        java.util.List<TileInfo> nonBlankTiles = new java.util.ArrayList<>();
        
        for (int row = 0; row < tilingResult.tilesHigh; row++) {
            for (int col = 0; col < tilingResult.tilesWide; col++) {
                boolean hasContent = tileContainsMeaningfulContent(col, row, tilingResult, image);
                if (hasContent) {
                    nonBlankTiles.add(new TileInfo(col, row, row * tilingResult.tilesWide + col + 1));
                }
            }
        }
        
        return nonBlankTiles;
    }

    public static class TilingResult {
        public final int tilesWide;
        public final int tilesHigh;
        public final double tileWidth;
        public final double tileHeight;
        public final int imageWidth;
        public final int imageHeight;

        public TilingResult(int tilesWide, int tilesHigh, double tileWidth, double tileHeight, int imageWidth, int imageHeight) {
            this.tilesWide = tilesWide;
            this.tilesHigh = tilesHigh;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }
    }

    /**
     * Represents information about a single tile
     */
    public static class TileInfo {
        public final int col;
        public final int row;
        public final int tileNumber;

        public TileInfo(int col, int row, int tileNumber) {
            this.col = col;
            this.row = row;
            this.tileNumber = tileNumber;
        }
    }
}
