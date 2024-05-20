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
        BufferedImage rotated = new BufferedImage(h, w, img.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.rotate(Math.toRadians(angle), w / 2.0, h / 2.0);
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
            int x = (getWidth() - rotatedImage.getWidth()) / 2;
            int y = (getHeight() - rotatedImage.getHeight()) / 2;
            g2d.drawImage(rotatedImage, x, y, this);
            g2d.dispose();
        }
    }
}
