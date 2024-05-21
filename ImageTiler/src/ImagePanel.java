import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JPanel {
    private BufferedImage image;
    private BufferedImage rotatedImage;
    private float rotationAngle = 0;
    private float scale = 1.0f;

    public void setImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
            rotatedImage = image;
            rotationAngle = 0;
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
        repaint();
    }

    public void rotateImage() {
        rotationAngle += 90;
        if (rotationAngle == 360) {
            rotationAngle = 0;
        }
        rotatedImage = rotateBufferedImage(image, rotationAngle);
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

            g2d.setColor(Color.BLACK);

            for (int row = 0; row < tilingResult.tilesHigh; row++) {
                for (int col = 0; col < tilingResult.tilesWide; col++) {
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
