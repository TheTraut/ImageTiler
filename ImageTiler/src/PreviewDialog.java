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
            int scaledWidth = (int) (image.getWidth() * scale);
            int scaledHeight = (int) (image.getHeight() * scale);

            double pageWidth = 8.27 * 72; // A4 width in points
            double pageHeight = 11.69 * 72; // A4 height in points

            int tilesWide = (int) Math.ceil((double) scaledWidth / pageWidth);
            int tilesHigh = (int) Math.ceil((double) scaledHeight / pageHeight);

            g2d.setColor(Color.BLACK);

            for (int row = 0; row < tilesHigh; row++) {
                for (int col = 0; col < tilesWide; col++) {
                    int x = (int) (col * pageWidth);
                    int y = (int) (row * pageHeight);
                    int width = (int) Math.min(pageWidth, scaledWidth - x);
                    int height = (int) Math.min(pageHeight, scaledHeight - y);

                    g2d.drawImage(image, x, y, x + width, y + height, x, y, x + width, y + height, null);
                    g2d.drawRect(x, y, width, height);
                }
            }
            g2d.dispose();
        }
    }
}
