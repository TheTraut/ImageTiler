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

    public void previewImage() {
        // Implement preview functionality
    }

    public void printImage(float scale) {
        TilePrinter.printTiledImage(rotatedImage, scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (rotatedImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = rotatedImage.getWidth();
            int imageHeight = rotatedImage.getHeight();

            float aspectRatio = (float) imageWidth / imageHeight;
            int drawWidth = panelWidth;
            int drawHeight = (int) (panelWidth / aspectRatio);

            if (drawHeight > panelHeight) {
                drawHeight = panelHeight;
                drawWidth = (int) (panelHeight * aspectRatio);
            }

            int x = (panelWidth - drawWidth) / 2;
            int y = (panelHeight - drawHeight) / 2;

            g2d.drawImage(rotatedImage, x, y, drawWidth, drawHeight, this);
            g2d.dispose();
        }
    }
}
