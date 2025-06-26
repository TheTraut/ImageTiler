import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Settings dialog for configuring application preferences
 */
public class SettingsDialog extends JDialog {
    private final Settings settings;
    private boolean settingsChanged = false;
    
    // UI Components
    private JTextField imageDirectoryField;
    private JTextField pdfDirectoryField;
    private JComboBox<Settings.PaperSize> paperSizeCombo;
    private JCheckBox showGridCheckbox;
    private JCheckBox showTileNumbersCheckbox;
    private JButton gridColorButton;
    private JButton excludedColorButton;
    private JSpinner scaleSpinner;
    private JSpinner gridLineWidthSpinner;
    private JCheckBox autoSaveCheckbox;
    private JCheckBox confirmOverwritesCheckbox;
    
    private Color selectedGridColor;
    private Color selectedExcludedColor;

    public SettingsDialog(Frame parent) {
        super(parent, "Settings", true);
        this.settings = Settings.getInstance();
        this.selectedGridColor = settings.getGridColor();
        this.selectedExcludedColor = settings.getExcludedColor();
        
        initializeComponents();
        layoutComponents();
        loadCurrentSettings();
        setupEventHandlers();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Set modern background color
        getContentPane().setBackground(new Color(245, 245, 245));
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Directory fields with modern styling
        imageDirectoryField = new JTextField(30);
        imageDirectoryField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        imageDirectoryField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        pdfDirectoryField = new JTextField(30);
        pdfDirectoryField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pdfDirectoryField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        // Paper size combo with modern styling
        paperSizeCombo = new JComboBox<>(Settings.PaperSize.values());
        paperSizeCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        // Checkboxes with modern styling
        showGridCheckbox = new JCheckBox("Show tile grid");
        showGridCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showGridCheckbox.setBackground(new Color(245, 245, 245));
        
        showTileNumbersCheckbox = new JCheckBox("Show tile numbers");
        showTileNumbersCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        showTileNumbersCheckbox.setBackground(new Color(245, 245, 245));
        
        autoSaveCheckbox = new JCheckBox("Auto-save settings");
        autoSaveCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        autoSaveCheckbox.setBackground(new Color(245, 245, 245));
        
        confirmOverwritesCheckbox = new JCheckBox("Confirm file overwrites");
        confirmOverwritesCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        confirmOverwritesCheckbox.setBackground(new Color(245, 245, 245));
        
        // Color buttons with modern styling
        gridColorButton = createStyledButton("Grid Color");
        excludedColorButton = createStyledButton("Excluded Tile Color");
        
        // Spinners with modern styling
        scaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
        scaleSpinner.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        gridLineWidthSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        gridLineWidthSpinner.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // File Directories Section
        JPanel dirPanel = createTitledPanel("Default Directories");
        dirPanel.setLayout(new GridBagLayout());
        GridBagConstraints dirGbc = new GridBagConstraints();
        dirGbc.insets = new Insets(5, 5, 5, 5);
        
        dirGbc.gridx = 0; dirGbc.gridy = 0;
        dirPanel.add(new JLabel("Image Directory:"), dirGbc);
        dirGbc.gridx = 1;
        dirPanel.add(imageDirectoryField, dirGbc);
        dirGbc.gridx = 2;
        JButton browseImageBtn = createStyledButton("Browse...");
        browseImageBtn.addActionListener(e -> browseForDirectory(imageDirectoryField));
        dirPanel.add(browseImageBtn, dirGbc);
        
        dirGbc.gridx = 0; dirGbc.gridy = 1;
        dirPanel.add(new JLabel("PDF Directory:"), dirGbc);
        dirGbc.gridx = 1;
        dirPanel.add(pdfDirectoryField, dirGbc);
        dirGbc.gridx = 2;
        JButton browsePdfBtn = createStyledButton("Browse...");
        browsePdfBtn.addActionListener(e -> browseForDirectory(pdfDirectoryField));
        dirPanel.add(browsePdfBtn, dirGbc);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(dirPanel, gbc);
        
        // Display Settings Section
        JPanel displayPanel = createTitledPanel("Display Settings");
        displayPanel.setLayout(new GridBagLayout());
        GridBagConstraints dispGbc = new GridBagConstraints();
        dispGbc.insets = new Insets(5, 5, 5, 5);
        dispGbc.anchor = GridBagConstraints.WEST;
        
        dispGbc.gridx = 0; dispGbc.gridy = 0;
        displayPanel.add(new JLabel("Paper Size:"), dispGbc);
        dispGbc.gridx = 1;
        displayPanel.add(paperSizeCombo, dispGbc);
        
        dispGbc.gridx = 0; dispGbc.gridy = 1;
        displayPanel.add(new JLabel("Default Scale:"), dispGbc);
        dispGbc.gridx = 1;
        displayPanel.add(scaleSpinner, dispGbc);
        
        dispGbc.gridx = 0; dispGbc.gridy = 2; dispGbc.gridwidth = 2;
        displayPanel.add(showGridCheckbox, dispGbc);
        
        dispGbc.gridy = 3;
        displayPanel.add(showTileNumbersCheckbox, dispGbc);
        
        gbc.gridy = 1;
        mainPanel.add(displayPanel, gbc);
        
        // Visual Settings Section
        JPanel visualPanel = createTitledPanel("Visual Settings");
        visualPanel.setLayout(new GridBagLayout());
        GridBagConstraints visGbc = new GridBagConstraints();
        visGbc.insets = new Insets(5, 5, 5, 5);
        visGbc.anchor = GridBagConstraints.WEST;
        
        visGbc.gridx = 0; visGbc.gridy = 0;
        visualPanel.add(new JLabel("Grid Line Width:"), visGbc);
        visGbc.gridx = 1;
        visualPanel.add(gridLineWidthSpinner, visGbc);
        
        visGbc.gridx = 0; visGbc.gridy = 1;
        visualPanel.add(gridColorButton, visGbc);
        visGbc.gridx = 1;
        visualPanel.add(excludedColorButton, visGbc);
        
        gbc.gridy = 2;
        mainPanel.add(visualPanel, gbc);
        
        // General Settings Section
        JPanel generalPanel = createTitledPanel("General Settings");
        generalPanel.setLayout(new GridBagLayout());
        GridBagConstraints genGbc = new GridBagConstraints();
        genGbc.insets = new Insets(5, 5, 5, 5);
        genGbc.anchor = GridBagConstraints.WEST;
        
        genGbc.gridx = 0; genGbc.gridy = 0; genGbc.gridwidth = 2;
        generalPanel.add(autoSaveCheckbox, genGbc);
        genGbc.gridy = 1;
        generalPanel.add(confirmOverwritesCheckbox, genGbc);
        
        gbc.gridy = 3;
        mainPanel.add(generalPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        JButton okButton = createActionButton("OK", new Color(76, 175, 80), Color.WHITE);
        JButton cancelButton = createActionButton("Cancel", new Color(158, 158, 158), Color.WHITE);
        JButton resetButton = createActionButton("Reset to Defaults", new Color(244, 67, 54), Color.WHITE);
        
        okButton.addActionListener(e -> saveAndClose());
        cancelButton.addActionListener(e -> dispose());
        resetButton.addActionListener(e -> resetToDefaults());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(resetButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5), 
                title, 
                TitledBorder.LEFT, 
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(60, 60, 60)
            )
        ));
        return panel;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
            }
        });
        
        return button;
    }
    
    private JButton createActionButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker()),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void loadCurrentSettings() {
        imageDirectoryField.setText(settings.getDefaultImageDir());
        pdfDirectoryField.setText(settings.getDefaultPdfDir());
        paperSizeCombo.setSelectedItem(settings.getPaperSize());
        showGridCheckbox.setSelected(settings.isShowGrid());
        showTileNumbersCheckbox.setSelected(settings.isShowTileNumbers());
        scaleSpinner.setValue(settings.getDefaultScale());
        gridLineWidthSpinner.setValue(settings.getGridLineWidth());
        autoSaveCheckbox.setSelected(settings.isAutoSaveSettings());
        confirmOverwritesCheckbox.setSelected(settings.isConfirmOverwrites());
        
        updateColorButtons();
    }
    
    private void setupEventHandlers() {
        gridColorButton.addActionListener(e -> chooseGridColor());
        excludedColorButton.addActionListener(e -> chooseExcludedColor());
    }
    
    private void browseForDirectory(JTextField textField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(textField.getText()));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void chooseGridColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Grid Color", selectedGridColor);
        if (newColor != null) {
            selectedGridColor = newColor;
            updateColorButtons();
        }
    }
    
    private void chooseExcludedColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Excluded Tile Color", selectedExcludedColor);
        if (newColor != null) {
            selectedExcludedColor = newColor;
            updateColorButtons();
        }
    }
    
    private void updateColorButtons() {
        gridColorButton.setBackground(selectedGridColor);
        gridColorButton.setOpaque(true);
        excludedColorButton.setBackground(selectedExcludedColor);
        excludedColorButton.setOpaque(true);
    }
    
    private void saveAndClose() {
        // Save all settings
        settings.setDefaultImageDir(imageDirectoryField.getText());
        settings.setDefaultPdfDir(pdfDirectoryField.getText());
        settings.setPaperSize((Settings.PaperSize) paperSizeCombo.getSelectedItem());
        settings.setShowGrid(showGridCheckbox.isSelected());
        settings.setShowTileNumbers(showTileNumbersCheckbox.isSelected());
        settings.setGridColor(selectedGridColor);
        settings.setExcludedColor(selectedExcludedColor);
        settings.setDefaultScale(((Number) scaleSpinner.getValue()).floatValue());
        settings.setGridLineWidth((Integer) gridLineWidthSpinner.getValue());
        settings.setAutoSaveSettings(autoSaveCheckbox.isSelected());
        settings.setConfirmOverwrites(confirmOverwritesCheckbox.isSelected());
        
        settingsChanged = true;
        dispose();
    }
    
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all settings to their default values?",
            "Reset Settings",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            settings.resetToDefaults();
            selectedGridColor = Settings.DEFAULT_GRID_COLOR;
            selectedExcludedColor = Settings.DEFAULT_EXCLUDED_COLOR;
            loadCurrentSettings();
        }
    }
    
    public boolean wereSettingsChanged() {
        return settingsChanged;
    }
}
