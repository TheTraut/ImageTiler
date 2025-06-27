import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Dialog to help users calibrate their printer by creating a reference print
 * and measuring the actual output to calculate accurate scaling.
 */
public class MeasurementHelperDialog extends JDialog {
    private JFrame parent;
    private ImagePanel imagePanel;
    private boolean calibrationCompleted = false;
    private float calibratedScale = 1.0f;
    
    // UI Components
    private JTextField measuredWidthField;
    private JTextField measuredHeightField;
    private JLabel expectedSizeLabel;
    private JButton printReferenceButton;
    private JButton calculateScaleButton;
    private JTextArea instructionsArea;
    private JPanel currentStepPanel;
    private JLabel previewLabel;
    private int currentStep = 1;
    
    public MeasurementHelperDialog(JFrame parent, ImagePanel imagePanel) {
        super(parent, "Printer Calibration Helper", true);
        this.parent = parent;
        this.imagePanel = imagePanel;
        
        // Automatically load the calibration image
        loadCalibrationImage();
        
        initializeComponents();
        setupLayout();
        updateStepDisplay();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        measuredWidthField = new JTextField(10);
        measuredHeightField = new JTextField(10);
        expectedSizeLabel = new JLabel();
        
        printReferenceButton = new JButton("1. Print Reference Page");
        printReferenceButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        printReferenceButton.setBackground(new Color(33, 150, 243));
        printReferenceButton.setForeground(Color.WHITE);
        printReferenceButton.addActionListener(this::printReference);
        
        calculateScaleButton = new JButton("3. Calculate Calibrated Scale");
        calculateScaleButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        calculateScaleButton.setBackground(new Color(76, 175, 80));
        calculateScaleButton.setForeground(Color.WHITE);
        calculateScaleButton.addActionListener(this::calculateCalibratedScale);
        calculateScaleButton.setEnabled(false);
        
        instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(new Color(245, 245, 245));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        instructionsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        
        currentStepPanel = new JPanel();
        currentStepPanel.setBorder(BorderFactory.createTitledBorder("Current Step"));
        currentStepPanel.setBackground(Color.WHITE);
        
        // Initialize preview label with calibration image
        previewLabel = new JLabel();
        try {
            BufferedImage calibration = ImageIO.read(getClass().getResource("/calibration/calibration.png"));
            previewLabel.setIcon(new ImageIcon(calibration));
        } catch (Exception e) {
            previewLabel.setText("Calibration image not found");
        }
        
        calculateExpectedSize();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(63, 81, 181));
        JLabel titleLabel = new JLabel("Printer Calibration Helper");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Instructions panel
        JScrollPane instructionsScroll = new JScrollPane(instructionsArea);
        instructionsScroll.setPreferredSize(new Dimension(550, 120));
        instructionsScroll.setBorder(BorderFactory.createTitledBorder("Instructions"));
        
        // Current step panel setup
        updateStepPanelLayout();
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(instructionsScroll, BorderLayout.NORTH);
        contentPanel.add(currentStepPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(doneButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateStepPanelLayout() {
        currentStepPanel.removeAll();
        currentStepPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        switch (currentStep) {
            case 1:
                gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
                currentStepPanel.add(new JLabel("Expected size of reference print:"), gbc);
                
                gbc.gridy = 1;
                currentStepPanel.add(expectedSizeLabel, gbc);
                
                gbc.gridy = 2;
                currentStepPanel.add(printReferenceButton, gbc);
                break;
                
            case 2:
                gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
                currentStepPanel.add(new JLabel("Measured Width (inches):"), gbc);
                gbc.gridx = 1;
                currentStepPanel.add(measuredWidthField, gbc);
                
                gbc.gridx = 0; gbc.gridy = 1;
                currentStepPanel.add(new JLabel("Measured Height (inches):"), gbc);
                gbc.gridx = 1;
                currentStepPanel.add(measuredHeightField, gbc);
                
                gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
                currentStepPanel.add(calculateScaleButton, gbc);
                break;
                
            case 3:
                gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
                JLabel completionLabel = new JLabel("Calibration completed!");
                completionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                completionLabel.setForeground(new Color(76, 175, 80));
                currentStepPanel.add(completionLabel, gbc);
                
                gbc.gridy = 1;
                currentStepPanel.add(new JLabel("Calibrated scale: " + String.format("%.3fx", calibratedScale)), gbc);
                break;
        }
        
        currentStepPanel.revalidate();
        currentStepPanel.repaint();
    }
    
    private void updateStepDisplay() {
        String instructions = "";
        
        switch (currentStep) {
            case 1:
                instructions = "STEP 1: Print Reference\n\n" +
                    "Follow these steps carefully for accurate calibration:\n\n" +
                    "1. Print the calibration sheet at 100% (no scaling)\n" +
                    "2. Place it flat on a clean surface\n" +
                    "3. Measure the printed rectangle with a ruler\n\n" +
                    "IMPORTANT: The calibration sheet contains a simple rectangle with known dimensions. " +
                    "Just measure the width and height of this rectangle for easy, accurate calibration.\n\n" +
                    "Click 'Print Reference Page' to print your calibration sheet. " +
                    "Make sure to use the same printer and settings you'll use for the final tiled output.";
                break;
                
            case 2:
                instructions = "STEP 2: Measure the Calibration Rectangle\n\n" +
                    "Use a ruler to measure the actual printed size of the calibration rectangle (the black outline rectangle). " +
                    "Measure from outer edge to outer edge of the rectangle outline.\n\n" +
                    "Expected dimensions: 4.00 × 3.00 inches\n" +
                    "Enter your actual measurements in inches below. Be as accurate as possible - " +
                    "small measurement errors can lead to significant scaling problems in large tiled prints.";
                calculateScaleButton.setEnabled(true);
                break;
                
            case 3:
                instructions = "STEP 3: Calibration Complete\n\n" +
                    "Based on your measurements, we've calculated the calibrated scale factor. " +
                    "This scale accounts for your specific printer's behavior.\n\n" +
                    "Click 'Done' to apply this scale to your image. You can now proceed with " +
                    "confidence that larger scale factors will produce accurate physical sizes.";
                break;
        }
        
        instructionsArea.setText(instructions);
    }
    
    private void calculateExpectedSize() {
        // For the rectangle calibration, we know the exact expected size
        float expectedWidthInches = GridParameters.CALIBRATION_RECTANGLE_WIDTH_INCHES;
        float expectedHeightInches = GridParameters.CALIBRATION_RECTANGLE_HEIGHT_INCHES;
        
        expectedSizeLabel.setText(String.format(
            "<html><b>%.2f × %.2f inches</b><br>" +
            "<small>(Rectangle dimensions in calibration sheet)</small></html>",
            expectedWidthInches, expectedHeightInches));
    }
    
    private void printReference(ActionEvent e) {
        try {
            // Print the image at scale 1.0 (single page baseline)
            TilePrinter.printTiledImageWithSelection(
                imagePanel.getRotatedImage(), 1.0f, false, imagePanel);
            
            currentStep = 2;
            updateStepDisplay();
            updateStepPanelLayout();
            
            JOptionPane.showMessageDialog(this,
                "Reference page sent to printer.\n" +
                "Please wait for it to print, then measure the actual size.",
                "Print Job Sent",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error printing reference page: " + ex.getMessage(),
                "Print Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calculateCalibratedScale(ActionEvent e) {
        try {
            float measuredWidth = Float.parseFloat(measuredWidthField.getText().trim());
            float measuredHeight = Float.parseFloat(measuredHeightField.getText().trim());
            
            if (measuredWidth <= 0 || measuredHeight <= 0) {
                throw new NumberFormatException("Measurements must be positive");
            }
            
            BufferedImage image = imagePanel.getRotatedImage();
            
            // Calculate the expected size based on single page preview
            double pageWidthPoints = 8.27 * 72;
            double pageHeightPoints = 11.69 * 72;
            
            TileCalculator.TilingResult singlePageResult = TileCalculator.calculateSinglePagePreview(
                image.getWidth(), image.getHeight(), pageWidthPoints, pageHeightPoints);
            
            // Calculate what DPI the printer actually used
            float actualWidthDPI = singlePageResult.imageWidth / measuredWidth;
            float actualHeightDPI = singlePageResult.imageHeight / measuredHeight;
            
            // Use the average DPI for calibration
            float averageDPI = (actualWidthDPI + actualHeightDPI) / 2.0f;
            
            // Calculate the scale using the simple rectangle approach
            // Use the direct measurement vs expected size
            calibratedScale = GridParameters.calculateCalibratedScale(measuredWidth, measuredHeight);
            
            calibrationCompleted = true;
            currentStep = 3;
            updateStepDisplay();
            updateStepPanelLayout();
            
            // Calculate accuracy and provide feedback
            String toleranceMessage = getToleranceMessage(calibratedScale);
            
            // Show detailed results
            String message = String.format(
                "Calibration Results:\n\n" +
                "Measured size: %.2f × %.2f inches\n" +
                "Expected size: %.2f × %.2f inches\n" +
                "Calculated printer DPI: %.1f\n" +
                "Calibrated scale factor: %.3fx\n\n" +
                "Accuracy: %s\n\n" +
                "This scale factor will be applied to compensate for your printer's actual output size.",
                measuredWidth, measuredHeight, 
                GridParameters.CALIBRATION_RECTANGLE_WIDTH_INCHES, 
                GridParameters.CALIBRATION_RECTANGLE_HEIGHT_INCHES,
                averageDPI, calibratedScale, toleranceMessage);
            
            JOptionPane.showMessageDialog(this, message, "Calibration Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid positive numbers for both width and height measurements.",
                "Invalid Measurements",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean wasCalibrationCompleted() {
        return calibrationCompleted;
    }
    
    public float getCalibratedScale() {
        return calibratedScale;
    }
    
    /**
     * Automatically loads the calibration image into the image panel
     */
    private void loadCalibrationImage() {
        try {
            // Load calibration image from classpath resources
            java.io.InputStream imageStream = getClass().getResourceAsStream("/calibration/calibration.png");
            
            if (imageStream != null) {
                BufferedImage calibrationImage = javax.imageio.ImageIO.read(imageStream);
                imageStream.close();
                
                if (calibrationImage != null) {
                    imagePanel.setImage(calibrationImage);
                    imagePanel.repaint();
                    System.out.println("Successfully loaded calibration image from resources");
                } else {
                    throw new Exception("Failed to decode calibration image");
                }
            } else {
                throw new Exception("Calibration image resource not found at /calibration/calibration.png");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading calibration image: " + e.getMessage());
            e.printStackTrace();
            
            // Show user-friendly error message
            JOptionPane.showMessageDialog(this,
                "Unable to load the calibration image. Please ensure the application is properly installed.",
                "Calibration Image Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Checks if the current image is a calibration image based on its properties
     */
    private boolean isCalibrationImage(BufferedImage image) {
        if (image == null) return false;
        
        // Check if dimensions match known calibration image dimensions
        // The calibration image is 3300 x 2550 pixels
        return (image.getWidth() == 3300 && image.getHeight() == 2550) ||
               (image.getWidth() == 2550 && image.getHeight() == 3300); // Account for rotation
    }
    
    /**
     * Provides tolerance feedback based on the calibrated scale factor
     */
    private String getToleranceMessage(float scale) {
        float deviation = Math.abs(scale - 1.0f);
        float percentError = deviation * 100.0f;
        
        if (percentError <= 0.5f) {
            return "✅ EXCELLENT (±0.5% or better)";
        } else if (percentError <= 1.0f) {
            return "✅ GOOD (±1% tolerance)";
        } else if (percentError <= 2.0f) {
            return "⚠️ ACCEPTABLE (±2% tolerance)";
        } else if (percentError <= 5.0f) {
            return "⚠️ WARNING (±5% error - check measurements)";
        } else {
            return "❌ POOR (>5% error - please re-measure or check printer settings)";
        }
    }
}
