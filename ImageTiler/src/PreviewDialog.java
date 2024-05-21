import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PreviewDialog extends JDialog {
    private BufferedImage image;
    private float scale;

    public PreviewDialog(Frame owner, BufferedImage image, float scale) {
        super(owner, "Preview", true);
        this.image = image;
        this.scale = scale;
        setSize(800, 600);
        setLocationRelativeTo(owner);
        add(new PreviewPanel(image, scale));
    }
}

class PreviewPanel extends JPanel {
    private BufferedImage image;
    private float scale;

    public PreviewPanel(BufferedImage image, float scale) {
        this.image = image;
        this.scale = scale;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = (int) (image.getWidth() * scale);
            int imageHeight = (int) (image.getHeight() * scale);

            float aspectRatio = (float) imageWidth / imageHeight;
            int drawWidth = panelWidth;
            int drawHeight = (int) (panelWidth / aspectRatio);

            if (drawHeight > panelHeight) {
                drawHeight = panelHeight;
                drawWidth = (int) (panelHeight * aspectRatio);
            }

            int x = (panelWidth - drawWidth) / 2;
            int y = (panelHeight - drawHeight) / 2;

            g2d.drawImage(image, x, y, drawWidth, drawHeight, this);

            double pageWidthPortrait = 8.27 * 72; // A4 width in points (portrait)
            double pageHeightPortrait = 11.69 * 72; // A4 height in points (portrait)

            double pageWidthLandscape = 11.69 * 72; // A4 width in points (landscape)
            double pageHeightLandscape = 8.27 * 72; // A4 height in points (landscape)

            int tilesWidePortrait = (int) Math.ceil((double) imageWidth / pageWidthPortrait);
            int tilesHighPortrait = (int) Math.ceil((double) imageHeight / pageHeightPortrait);

            int tilesWideLandscape = (int) Math.ceil((double) imageWidth / pageWidthLandscape);
            int tilesHighLandscape = (int) Math.ceil((double) imageHeight / pageHeightLandscape);

            int totalTilesPortrait = tilesWidePortrait * tilesHighPortrait;
            int totalTilesLandscape = tilesWideLandscape * tilesHighLandscape;

            boolean useLandscape = totalTilesLandscape < totalTilesPortrait;

            double pageWidth = useLandscape ? pageWidthLandscape : pageWidthPortrait;
            double pageHeight = useLandscape ? pageHeightLandscape : pageHeightPortrait;
            int tilesWide = useLandscape ? tilesWideLandscape : tilesWidePortrait;
            int tilesHigh = useLandscape ? tilesHighLandscape : tilesHighPortrait;

            double tileWidthScaled = drawWidth / (double) imageWidth * pageWidth;
            double tileHeightScaled = drawHeight / (double) imageHeight * pageHeight;

            g2d.setColor(Color.BLACK);

            for (int row = 0; row < tilesHigh; row++) {
                for (int col = 0; col < tilesWide; col++) {
                    int tileX = x + (int) (col * tileWidthScaled);
                    int tileY = y + (int) (row * tileHeightScaled);
                    int width = (int) Math.min(tileWidthScaled, drawWidth - col * tileWidthScaled);
                    int height = (int) Math.min(tileHeightScaled, drawHeight - row * tileHeightScaled);

                    g2d.drawRect(tileX, tileY, width, height);
                }
            }

            g2d.dispose();
        }
    }
}
