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
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Directory fields
        imageDirectoryField = new JTextField(30);
        pdfDirectoryField = new JTextField(30);
        
        // Paper size combo
        paperSizeCombo = new JComboBox<>(Settings.PaperSize.values());
        
        // Checkboxes
        showGridCheckbox = new JCheckBox("Show tile grid");
        showTileNumbersCheckbox = new JCheckBox("Show tile numbers");
        autoSaveCheckbox = new JCheckBox("Auto-save settings");
        confirmOverwritesCheckbox = new JCheckBox("Confirm file overwrites");
        
        // Color buttons
        gridColorButton = new JButton("Grid Color");
        excludedColorButton = new JButton("Excluded Tile Color");
        
        // Spinners
        scaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
        gridLineWidthSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
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
        JButton browseImageBtn = new JButton("Browse...");
        browseImageBtn.addActionListener(e -> browseForDirectory(imageDirectoryField));
        dirPanel.add(browseImageBtn, dirGbc);
        
        dirGbc.gridx = 0; dirGbc.gridy = 1;
        dirPanel.add(new JLabel("PDF Directory:"), dirGbc);
        dirGbc.gridx = 1;
        dirPanel.add(pdfDirectoryField, dirGbc);
        dirGbc.gridx = 2;
        JButton browsePdfBtn = new JButton("Browse...");
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
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton resetButton = new JButton("Reset to Defaults");
        
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
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title, 
            TitledBorder.LEFT, TitledBorder.TOP));
        return panel;
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
