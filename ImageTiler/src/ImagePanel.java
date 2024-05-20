import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JPanel {
    private BufferedImage image;
    private float rotationAngle = 0;

    public void setImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
            rotationAngle = 0;
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rotateImage() {
        rotationAngle += 90;
        if (rotationAngle == 360) {
            rotationAngle = 0;
        }
        repaint();
    }

    public void previewImage() {
        // Implement preview functionality
    }

    public void printImage(float scale) {
        TilePrinter.printTiledImage(image, scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            int x = (getWidth() - image.getWidth()) / 2;
            int y = (getHeight() - image.getHeight()) / 2;
            g2d.rotate(Math.toRadians(rotationAngle), getWidth() / 2, getHeight() / 2);
            g2d.drawImage(image, x, y, this);
            g2d.dispose();
        }
    }
}
