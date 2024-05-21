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
            return new TilingResult(tilesWideLandscape, tilesHighLandscape, pageHeight, pageWidth);
        } else {
            return new TilingResult(tilesWidePortrait, tilesHighPortrait, pageWidth, pageHeight);
        }
    }

    public static class TilingResult {
        public final int tilesWide;
        public final int tilesHigh;
        public final double tileWidth;
        public final double tileHeight;

        public TilingResult(int tilesWide, int tilesHigh, double tileWidth, double tileHeight) {
            this.tilesWide = tilesWide;
            this.tilesHigh = tilesHigh;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
        }
    }
}
