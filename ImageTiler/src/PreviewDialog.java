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

            double pageWidth = 8.27 * 72; // A4 width in points
            double pageHeight = 11.69 * 72; // A4 height in points

            int tilesWide = (int) Math.ceil((double) imageWidth / pageWidth);
            int tilesHigh = (int) Math.ceil((double) imageHeight / pageHeight);

            g2d.setColor(Color.BLACK);

            for (int row = 0; row < tilesHigh; row++) {
                for (int col = 0; col < tilesWide; col++) {
                    int tileX = x + (int) (col * pageWidth * drawWidth / imageWidth);
                    int tileY = y + (int) (row * pageHeight * drawHeight / imageHeight);
                    int width = (int) Math.min(pageWidth * drawWidth / imageWidth, drawWidth - col * pageWidth * drawWidth / imageWidth);
                    int height = (int) Math.min(pageHeight * drawHeight / imageHeight, drawHeight - row * pageHeight * drawHeight / imageHeight);

                    g2d.drawRect(tileX, tileY, width, height);
                }
            }

            g2d.dispose();
        }
    }
}
