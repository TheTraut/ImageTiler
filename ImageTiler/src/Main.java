import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class Main {
    private JFrame frame;
    private ImagePanel imagePanel;
    private JTextField scaleField;
    private JTextField originalSizeField;
    private JTextField newSizeField;
    private boolean isRotated = false;
    private JLabel statusLabel;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("ImageTiler - Professional Image Tiling Solution");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        imagePanel = new ImagePanel();
        frame.add(imagePanel, BorderLayout.CENTER);

        JPanel controlPanel = createModernControlPanel();
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JButton settingsButton = createStyledButton("[*] Settings", new Color(63, 81, 181));
        settingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(frame);
            dialog.setVisible(true);
            if (dialog.wereSettingsChanged()) {
                imagePanel.refreshDisplay();
            }
        });

        JButton selectImageButton = createStyledButton("[+] Select Image", new Color(76, 175, 80));
        selectImageButton.addActionListener(e -> selectImage());

        JButton rotateImageButton = createStyledButton("[>] Rotate Image", new Color(156, 39, 176));
        rotateImageButton.addActionListener(e -> rotateImage());

        scaleField = createStyledTextField("1.0");
        scaleField.addActionListener(e -> applyScale()); // Apply scale when Enter is pressed
        originalSizeField = createStyledTextField("");
        newSizeField = createStyledTextField("");

        JButton applyScaleButton = createStyledButton("[√] Apply Scale", new Color(76, 175, 80));
        applyScaleButton.addActionListener(e -> applyScale());
        
        JButton calculateScaleButton = createStyledButton("[=] Calculate Scale", new Color(255, 193, 7));
        calculateScaleButton.addActionListener(e -> calculateScale());

        JButton printButton = createStyledButton("[P] Print Image", new Color(33, 150, 243));
        printButton.addActionListener(e -> printImage());

        JButton savePdfButton = createStyledButton("[S] Save to PDF", new Color(255, 87, 34));
        savePdfButton.addActionListener(e -> saveToPDF());
        
        JButton clearSelectionsButton = createStyledButton("[X] Clear Selections", new Color(158, 158, 158));
        clearSelectionsButton.addActionListener(e -> clearTileSelections());

        controlPanel.add(createStyledLabel("Image:"));
        controlPanel.add(selectImageButton);
        controlPanel.add(createStyledLabel("Rotation:"));
        controlPanel.add(rotateImageButton);
        controlPanel.add(createStyledLabel("Scale:"));
        controlPanel.add(scaleField);
        controlPanel.add(createStyledLabel("Apply Scale:"));
        controlPanel.add(applyScaleButton);
        controlPanel.add(createStyledLabel("Original Size (inches):"));
        controlPanel.add(originalSizeField);
        controlPanel.add(createStyledLabel("New Size (inches):"));
        controlPanel.add(newSizeField);
        controlPanel.add(createStyledLabel("Calculate Scale:"));
        controlPanel.add(calculateScaleButton);
        controlPanel.add(createStyledLabel("Manual Selection:"));
        controlPanel.add(clearSelectionsButton);
        controlPanel.add(createStyledLabel("Settings:"));
        controlPanel.add(settingsButton);
        controlPanel.add(printButton);
        controlPanel.add(savePdfButton);

        // Create status bar
        statusLabel = new JLabel("Ready - Select an image to begin tiling");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        statusLabel.setBackground(new Color(245, 245, 245));
        statusLabel.setOpaque(true);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(controlPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        
        frame.add(southPanel, BorderLayout.SOUTH);
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
                statusLabel.setText("Loading image: " + selectedFile.getName());
                imagePanel.setImage(selectedFile.getPath());
                
                // Reset scale to 1.0 to show single page view
                scaleField.setText("1.0");
                imagePanel.setScale(1.0f);
                
                // Clear size fields
                originalSizeField.setText("");
                newSizeField.setText("");
                
                isRotated = false;
                imagePanel.repaint();
                frame.setTitle("ImageTiler - " + selectedFile.getName());
                statusLabel.setText("Image loaded - Showing single page view. Measure printed size, enter measurements, and calculate scale to tile.");
            } catch (Exception e) {
                statusLabel.setText("Error loading image: " + e.getMessage());
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
        if (imagePanel.getImage() == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Please select an image first.",
                "No Image",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        imagePanel.rotateImage();
        isRotated = !isRotated;
        imagePanel.repaint();
        statusLabel.setText("Image rotated - Manual tile selections cleared to avoid position mismatch");
    }

    private void applyScale() {
        if (imagePanel.getImage() == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Please select an image first.",
                "No Image",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String scaleText = scaleField.getText().trim();
        
        if (scaleText.isEmpty()) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter a scale value.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            float scale = Float.parseFloat(scaleText);
            
            if (scale <= 0) {
                throw new NumberFormatException("Scale must be positive");
            }
            
            imagePanel.setScale(scale);
            imagePanel.repaint();
            statusLabel.setText("Scale applied: " + String.format("%.2fx", scale));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter a valid positive scale value (e.g., 1.0, 2.5, 0.75).",
                "Invalid Scale",
                JOptionPane.ERROR_MESSAGE
            );
        }
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
            statusLabel.setText("Scale calculated and applied: " + String.format("%.2fx", scale));
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
            statusLabel.setText("Printing image with selected tiles...");
            TilePrinter.printTiledImageWithSelection(imagePanel.getRotatedImage(), scale, isRotated, imagePanel);
            statusLabel.setText("Print job sent successfully");
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
            statusLabel.setText("Saving PDF with selected tiles...");
            TilePrinter.saveTiledImageToPDFWithSelection(imagePanel.getRotatedImage(), scale, isRotated, imagePanel);
            statusLabel.setText("PDF saved successfully");
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
        statusLabel.setText("All tile selections cleared - Click tiles to exclude them from printing");
        JOptionPane.showMessageDialog(
            frame,
            "All tile selections have been cleared.\n" +
            "Click on tiles in the preview to exclude them from printing.",
            "Selections Cleared",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private JPanel createModernControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 2, 8, 8));
        panel.setBackground(new Color(250, 250, 250));
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker()),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JTextField createStyledTextField(String initialText) {
        JTextField field = new JTextField(initialText);
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }
}
