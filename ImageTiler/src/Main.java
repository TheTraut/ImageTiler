import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    private JFrame frame;
    private ImagePanel imagePanel;
    private JTextField scaleField;
    private JTextField originalSizeField;
    private JTextField newSizeField;
    private boolean isRotated = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Printer Tiling");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        imagePanel = new ImagePanel();
        frame.add(imagePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(8, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton selectImageButton = new JButton("Select Image");
        selectImageButton.addActionListener(e -> selectImage());

        JButton rotateImageButton = new JButton("Rotate Image");
        rotateImageButton.addActionListener(e -> rotateImage());

        scaleField = new JTextField("1.0");
        originalSizeField = new JTextField();
        newSizeField = new JTextField();

        JButton calculateScaleButton = new JButton("Calculate Scale");
        calculateScaleButton.addActionListener(e -> calculateScale());

        JButton printButton = new JButton("Print Image");
        printButton.addActionListener(e -> printImage());

        JButton savePdfButton = new JButton("Save to PDF");
        savePdfButton.addActionListener(e -> saveToPDF());
        
        JButton clearSelectionsButton = new JButton("Clear Tile Selections");
        clearSelectionsButton.addActionListener(e -> clearTileSelections());

        controlPanel.add(new JLabel("Image:"));
        controlPanel.add(selectImageButton);
        controlPanel.add(new JLabel("Rotation:"));
        controlPanel.add(rotateImageButton);
        controlPanel.add(new JLabel("Scale:"));
        controlPanel.add(scaleField);
        controlPanel.add(new JLabel("Original Size (inches):"));
        controlPanel.add(originalSizeField);
        controlPanel.add(new JLabel("New Size (inches):"));
        controlPanel.add(newSizeField);
        controlPanel.add(new JLabel("Calculate:"));
        controlPanel.add(calculateScaleButton);
        controlPanel.add(new JLabel("Manual Selection:"));
        controlPanel.add(clearSelectionsButton);
        controlPanel.add(printButton);
        controlPanel.add(savePdfButton);

        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        // Try to start in Downloads, but fall back to home if it doesn't exist
        File downloadsDir = new File(System.getProperty("user.home") + "/Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            fileChooser.setCurrentDirectory(downloadsDir);
        }
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String ext = getFileExtension(f);
                return ext != null && (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"));
            }

            @Override
            public String getDescription() {
                return "Image Files (*.png, *.jpg, *.jpeg)";
            }

            private String getFileExtension(File f) {
                String name = f.getName();
                int lastIndexOf = name.lastIndexOf('.');
                if (lastIndexOf == -1) {
                    return null;
                }
                return name.substring(lastIndexOf + 1);
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                imagePanel.setImage(selectedFile.getPath());
                isRotated = false;
                imagePanel.repaint();
                frame.setTitle("Printer Tiling - " + selectedFile.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error loading image: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void rotateImage() {
        imagePanel.rotateImage();
        isRotated = !isRotated;
        imagePanel.repaint();
    }

    private void calculateScale() {
        String originalSize = originalSizeField.getText().trim();
        String newSize = newSizeField.getText().trim();
        
        if (originalSize.isEmpty() || newSize.isEmpty()) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter both original size and new size.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            float originalFloat = Float.parseFloat(originalSize);
            float newFloat = Float.parseFloat(newSize);
            
            if (originalFloat <= 0 || newFloat <= 0) {
                throw new NumberFormatException("Size must be positive");
            }
            
            float scale = ScaleCalculator.calculateScale(originalFloat, newFloat);
            scaleField.setText(String.format("%.2f", scale));
            imagePanel.setScale(scale);
            imagePanel.repaint();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter valid positive numbers for sizes.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void printImage() {
        if (imagePanel.getImage() == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Please select an image first.",
                "No Image",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            float scale = Float.parseFloat(scaleField.getText());
            if (scale <= 0) {
                throw new NumberFormatException("Scale must be positive");
            }
            TilePrinter.printTiledImageWithSelection(imagePanel.getRotatedImage(), scale, isRotated, imagePanel);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter a valid positive scale value.",
                "Invalid Scale",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void saveToPDF() {
        if (imagePanel.getImage() == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Please select an image first.",
                "No Image",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            float scale = Float.parseFloat(scaleField.getText());
            if (scale <= 0) {
                throw new NumberFormatException("Scale must be positive");
            }
            TilePrinter.saveTiledImageToPDFWithSelection(imagePanel.getRotatedImage(), scale, isRotated, imagePanel);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter a valid positive scale value.",
                "Invalid Scale",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void clearTileSelections() {
        if (imagePanel.getImage() == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Please select an image first.",
                "No Image",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        imagePanel.clearManualSelections();
        JOptionPane.showMessageDialog(
            frame,
            "All tile selections have been cleared.\n" +
            "Click on tiles in the preview to exclude them from printing.",
            "Selections Cleared",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
