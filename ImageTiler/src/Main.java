import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    private JFrame frame;
    private ImagePanel imagePanel;
    private JTextField scaleField;
    private JTextField originalSizeField;
    private JTextField newSizeField;

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
        controlPanel.setLayout(new GridLayout(5, 2));

        JButton selectImageButton = new JButton("Select Image");
        selectImageButton.addActionListener(e -> selectImage());

        JButton rotateImageButton = new JButton("Rotate Image");
        rotateImageButton.addActionListener(e -> imagePanel.rotateImage());

        scaleField = new JTextField("1.0");
        originalSizeField = new JTextField();
        newSizeField = new JTextField();

        JButton calculateScaleButton = new JButton("Calculate Scale");
        calculateScaleButton.addActionListener(e -> calculateScale());

        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(e -> previewImage());

        JButton printButton = new JButton("Print Image");
        printButton.addActionListener(e -> imagePanel.printImage(Float.parseFloat(scaleField.getText())));

        controlPanel.add(new JLabel("Scale:"));
        controlPanel.add(scaleField);
        controlPanel.add(new JLabel("Original Size:"));
        controlPanel.add(originalSizeField);
        controlPanel.add(new JLabel("New Size:"));
        controlPanel.add(newSizeField);
        controlPanel.add(calculateScaleButton);
        controlPanel.add(selectImageButton);
        controlPanel.add(rotateImageButton);
        controlPanel.add(previewButton);
        controlPanel.add(printButton);

        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home") + "/Downloads");
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
            imagePanel.setImage(fileChooser.getSelectedFile().getPath());
        }
    }

    private void calculateScale() {
        String originalSize = originalSizeField.getText();
        String newSize = newSizeField.getText();
        if (!originalSize.isEmpty() && !newSize.isEmpty()) {
            float scale = ScaleCalculator.calculateScale(Float.parseFloat(originalSize), Float.parseFloat(newSize));
            scaleField.setText(String.format("%.2f", scale));
        }
    }

    private void previewImage() {
        if (imagePanel.getImage() != null) {
            float scale = Float.parseFloat(scaleField.getText());
            PreviewDialog previewDialog = new PreviewDialog(frame, imagePanel.getRotatedImage(), scale);
            previewDialog.setVisible(true);
        }
    }
}
