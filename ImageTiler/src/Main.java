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
        frame.setSize(1200, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        imagePanel = new ImagePanel();
        frame.add(imagePanel, BorderLayout.CENTER);

        // Create organized control panel
        JPanel controlPanel = createOrganizedControlPanel();
        
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
    
    private JPanel createOrganizedControlPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create a horizontal layout with sections
        JPanel sectionsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        sectionsPanel.setBackground(new Color(250, 250, 250));
        
        // Section 1: Image & Basic Operations
        JPanel imageSection = createImageSection();
        sectionsPanel.add(imageSection);
        
        // Section 2: Scale & Size Calculations
        JPanel scaleSection = createScaleSection();
        sectionsPanel.add(scaleSection);
        
        // Section 3: Helpers & Tools
        JPanel helpersSection = createHelpersSection();
        sectionsPanel.add(helpersSection);
        
        // Section 4: Output & Print
        JPanel outputSection = createOutputSection();
        sectionsPanel.add(outputSection);
        
        mainPanel.add(sectionsPanel, BorderLayout.CENTER);
        return mainPanel;
    }
    
    private JPanel createImageSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            "📷 Image Operations",
            0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(76, 175, 80)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JButton selectImageButton = createStyledButton("📁 Select Image", new Color(76, 175, 80));
        selectImageButton.addActionListener(e -> selectImage());
        
        JButton rotateImageButton = createStyledButton("🔄 Rotate Image", new Color(156, 39, 176));
        rotateImageButton.addActionListener(e -> rotateImage());
        
        JButton clearSelectionsButton = createStyledButton("❌ Clear Selections", new Color(158, 158, 158));
        clearSelectionsButton.addActionListener(e -> clearTileSelections());
        
        // Row 1: Select Image
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selectImageButton, gbc);
        
        // Row 2: Image operations
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(rotateImageButton, gbc);
        gbc.gridx = 1;
        panel.add(clearSelectionsButton, gbc);
        
        // Add description
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JTextArea description = new JTextArea("Load an image to begin tiling. Rotate if needed and click tiles to exclude them from printing.");
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("SansSerif", Font.ITALIC, 11));
        description.setForeground(Color.GRAY);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        panel.add(description, gbc);
        
        return panel;
    }
    
    private JPanel createScaleSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
            "📏 Scale & Size",
            0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(255, 193, 7)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Initialize text fields
        scaleField = createStyledTextField("1.0");
        scaleField.addActionListener(e -> applyScale());
        originalSizeField = createStyledTextField("");
        newSizeField = createStyledTextField("");
        
        JButton applyScaleButton = createStyledButton("✓ Apply Scale", new Color(76, 175, 80));
        applyScaleButton.addActionListener(e -> applyScale());
        
        JButton calculateScaleButton = createStyledButton("🧮 Calculate Scale", new Color(255, 193, 7));
        calculateScaleButton.addActionListener(e -> calculateScale());
        
        // Scale input section
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createStyledLabel("Scale Factor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(scaleField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(applyScaleButton, gbc);
        
        // Scale calculation section
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        JLabel calcLabel = createStyledLabel("Calculate Scale from Measurements:");
        calcLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(calcLabel, gbc);
        
        gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Original Size (inches):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(originalSizeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        panel.add(createStyledLabel("Desired Size (inches):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(newSizeField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2; gbc.gridheight = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(calculateScaleButton, gbc);
        
        return panel;
    }
    
    private JPanel createHelpersSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(63, 81, 181), 2),
            "🔧 Tools & Helpers",
            0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(63, 81, 181)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton calibrationButton = createStyledButton("🎯 Printer Calibration", new Color(63, 81, 181));
        calibrationButton.addActionListener(e -> showCalibration());
        
        JButton predictSizeButton = createStyledButton("📐 Predict Physical Size", new Color(121, 85, 72));
        predictSizeButton.addActionListener(e -> predictPhysicalSize());
        
        JButton settingsButton = createStyledButton("⚙️ Settings", new Color(96, 125, 139));
        settingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(frame);
            dialog.setVisible(true);
            if (dialog.wereSettingsChanged()) {
                imagePanel.refreshDisplay();
            }
        });
        
        // Layout helpers
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(calibrationButton, gbc);
        
        gbc.gridy = 1;
        panel.add(predictSizeButton, gbc);
        
        gbc.gridy = 2;
        panel.add(settingsButton, gbc);
        
        // Add descriptions
        gbc.gridy = 3;
        JTextArea description = new JTextArea("Use Printer Calibration to ensure accurate scaling. Predict Physical Size shows expected output dimensions.");
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("SansSerif", Font.ITALIC, 11));
        description.setForeground(Color.GRAY);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        panel.add(description, gbc);
        
        return panel;
    }
    
    private JPanel createOutputSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 2),
            "🖨️ Output & Export",
            0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(33, 150, 243)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton printButton = createStyledButton("🖨️ Print Image", new Color(33, 150, 243));
        printButton.addActionListener(e -> printImage());
        
        JButton savePdfButton = createStyledButton("💾 Save to PDF", new Color(255, 87, 34));
        savePdfButton.addActionListener(e -> saveToPDF());
        
        // Layout output options
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(printButton, gbc);
        
        gbc.gridy = 1;
        panel.add(savePdfButton, gbc);
        
        // Add description
        gbc.gridy = 2;
        JTextArea description = new JTextArea("Print directly to your printer or save as PDF. Only selected tiles will be included in the output.");
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("SansSerif", Font.ITALIC, 11));
        description.setForeground(Color.GRAY);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        panel.add(description, gbc);
        
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
    
    /**
     * Shows a calibration dialog to guide users through the printer calibration process
     */
    private void showCalibration() {
        // The calibration dialog automatically loads the calibration image
        // No need to check for existing image since we provide our own
        
        MeasurementHelperDialog dialog = new MeasurementHelperDialog(frame, imagePanel);
        dialog.setVisible(true);
        
        // If user completed calibration, update the scale field
        if (dialog.wasCalibrationCompleted()) {
            float calibratedScale = dialog.getCalibratedScale();
            scaleField.setText(String.format("%.3f", calibratedScale));
            imagePanel.setScale(calibratedScale);
            imagePanel.repaint();
            statusLabel.setText("Calibration completed - Scale set to " + String.format("%.3fx", calibratedScale));
        }
    }
    
    /**
     * Predicts the physical size that will result from current scale settings
     */
    private void predictPhysicalSize() {
        if (imagePanel.getImage() == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Please select an image first.",
                "No Image Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            float scale = Float.parseFloat(scaleField.getText());
            if (scale <= 0) {
                throw new NumberFormatException("Scale must be positive");
            }
            
            // Use the current image dimensions
            int imageWidth = imagePanel.getRotatedImage().getWidth();
            int imageHeight = imagePanel.getRotatedImage().getHeight();
            
            // Calculate expected size at different common print DPIs
            ScaleCalculator.PhysicalSize size150dpi = ScaleCalculator.calculateExpectedPhysicalSize(
                imageWidth, imageHeight, scale, ScaleCalculator.OFFICE_PRINT_DPI);
            ScaleCalculator.PhysicalSize size300dpi = ScaleCalculator.calculateExpectedPhysicalSize(
                imageWidth, imageHeight, scale, ScaleCalculator.PRINT_DPI);
            
            // Also calculate total area when tiled
            double pageWidthInches = 8.27;
            double pageHeightInches = 11.69;
            TileCalculator.TilingResult tilingResult = TileCalculator.calculateScaledTiling(
                imageWidth, imageHeight, pageWidthInches * 72, pageHeightInches * 72, scale);
            
            String message = String.format(
                "Predicted Physical Sizes for Scale %.2fx:\n\n" +
                "At 150 DPI (typical office printer): %s\n" +
                "At 300 DPI (high quality): %s\n\n" +
                "Tiling Information:\n" +
                "Number of tiles: %d wide × %d high = %d total\n" +
                "Total printed area: %.1f × %.1f inches\n\n" +
                "Note: Actual size depends on your printer's DPI setting.",
                scale,
                size150dpi.toString(),
                size300dpi.toString(),
                tilingResult.tilesWide, tilingResult.tilesHigh,
                tilingResult.tilesWide * tilingResult.tilesHigh,
                tilingResult.tilesWide * pageWidthInches,
                tilingResult.tilesHigh * pageHeightInches
            );
            
            JOptionPane.showMessageDialog(
                frame,
                message,
                "Physical Size Prediction",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                frame,
                "Please enter a valid positive scale value first.",
                "Invalid Scale",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
