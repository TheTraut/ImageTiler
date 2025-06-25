import java.awt.*;
import java.io.*;
import java.util.Properties;

/**
 * Manages application settings and user preferences
 */
public class Settings {
    private static final String SETTINGS_FILE = "imagetiler.properties";
    private static Settings instance;
    private Properties properties;
    
    // Default values
    public static final String DEFAULT_IMAGE_DIR = System.getProperty("user.home") + File.separator + "Downloads";
    public static final String DEFAULT_PDF_DIR = System.getProperty("user.home") + File.separator + "Documents";
    public static final PaperSize DEFAULT_PAPER_SIZE = PaperSize.A4;
    public static final boolean DEFAULT_SHOW_GRID = true;
    public static final boolean DEFAULT_SHOW_TILE_NUMBERS = true;
    public static final Color DEFAULT_GRID_COLOR = Color.RED;
    public static final Color DEFAULT_EXCLUDED_COLOR = Color.DARK_GRAY;
    public static final float DEFAULT_SCALE = 1.0f;
    public static final boolean DEFAULT_AUTO_SAVE_SETTINGS = true;
    public static final boolean DEFAULT_CONFIRM_OVERWRITES = true;
    public static final int DEFAULT_GRID_LINE_WIDTH = 2;
    
    // Paper size enumeration
    public enum PaperSize {
        A4(8.27, 11.69, "A4 (210 × 297 mm)"),
        A3(11.69, 16.54, "A3 (297 × 420 mm)"),
        LETTER(8.5, 11.0, "Letter (8.5\" × 11\")"),
        LEGAL(8.5, 14.0, "Legal (8.5\" × 14\")"),
        TABLOID(11.0, 17.0, "Tabloid (11\" × 17\")");
        
        public final double widthInches;
        public final double heightInches;
        public final String displayName;
        
        PaperSize(double widthInches, double heightInches, String displayName) {
            this.widthInches = widthInches;
            this.heightInches = heightInches;
            this.displayName = displayName;
        }
        
        public double getWidthPoints() {
            return widthInches * 72;
        }
        
        public double getHeightPoints() {
            return heightInches * 72;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private Settings() {
        properties = new Properties();
        loadSettings();
    }
    
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
    
    private void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);
        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Could not load settings: " + e.getMessage());
            }
        }
        setDefaultsIfMissing();
    }
    
    private void setDefaultsIfMissing() {
        if (!properties.containsKey("defaultImageDir")) {
            properties.setProperty("defaultImageDir", DEFAULT_IMAGE_DIR);
        }
        if (!properties.containsKey("defaultPdfDir")) {
            properties.setProperty("defaultPdfDir", DEFAULT_PDF_DIR);
        }
        if (!properties.containsKey("paperSize")) {
            properties.setProperty("paperSize", DEFAULT_PAPER_SIZE.name());
        }
        if (!properties.containsKey("showGrid")) {
            properties.setProperty("showGrid", String.valueOf(DEFAULT_SHOW_GRID));
        }
        if (!properties.containsKey("showTileNumbers")) {
            properties.setProperty("showTileNumbers", String.valueOf(DEFAULT_SHOW_TILE_NUMBERS));
        }
        if (!properties.containsKey("gridColor")) {
            properties.setProperty("gridColor", colorToString(DEFAULT_GRID_COLOR));
        }
        if (!properties.containsKey("excludedColor")) {
            properties.setProperty("excludedColor", colorToString(DEFAULT_EXCLUDED_COLOR));
        }
        if (!properties.containsKey("defaultScale")) {
            properties.setProperty("defaultScale", String.valueOf(DEFAULT_SCALE));
        }
        if (!properties.containsKey("autoSaveSettings")) {
            properties.setProperty("autoSaveSettings", String.valueOf(DEFAULT_AUTO_SAVE_SETTINGS));
        }
        if (!properties.containsKey("confirmOverwrites")) {
            properties.setProperty("confirmOverwrites", String.valueOf(DEFAULT_CONFIRM_OVERWRITES));
        }
        if (!properties.containsKey("gridLineWidth")) {
            properties.setProperty("gridLineWidth", String.valueOf(DEFAULT_GRID_LINE_WIDTH));
        }
    }
    
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "ImageTiler Settings");
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }
    
    // Getters
    public String getDefaultImageDir() {
        return properties.getProperty("defaultImageDir", DEFAULT_IMAGE_DIR);
    }
    
    public String getDefaultPdfDir() {
        return properties.getProperty("defaultPdfDir", DEFAULT_PDF_DIR);
    }
    
    public PaperSize getPaperSize() {
        try {
            return PaperSize.valueOf(properties.getProperty("paperSize", DEFAULT_PAPER_SIZE.name()));
        } catch (IllegalArgumentException e) {
            return DEFAULT_PAPER_SIZE;
        }
    }
    
    public boolean isShowGrid() {
        return Boolean.parseBoolean(properties.getProperty("showGrid", String.valueOf(DEFAULT_SHOW_GRID)));
    }
    
    public boolean isShowTileNumbers() {
        return Boolean.parseBoolean(properties.getProperty("showTileNumbers", String.valueOf(DEFAULT_SHOW_TILE_NUMBERS)));
    }
    
    public Color getGridColor() {
        return stringToColor(properties.getProperty("gridColor", colorToString(DEFAULT_GRID_COLOR)));
    }
    
    public Color getExcludedColor() {
        return stringToColor(properties.getProperty("excludedColor", colorToString(DEFAULT_EXCLUDED_COLOR)));
    }
    
    public float getDefaultScale() {
        try {
            return Float.parseFloat(properties.getProperty("defaultScale", String.valueOf(DEFAULT_SCALE)));
        } catch (NumberFormatException e) {
            return DEFAULT_SCALE;
        }
    }
    
    public boolean isAutoSaveSettings() {
        return Boolean.parseBoolean(properties.getProperty("autoSaveSettings", String.valueOf(DEFAULT_AUTO_SAVE_SETTINGS)));
    }
    
    public boolean isConfirmOverwrites() {
        return Boolean.parseBoolean(properties.getProperty("confirmOverwrites", String.valueOf(DEFAULT_CONFIRM_OVERWRITES)));
    }
    
    public int getGridLineWidth() {
        try {
            return Integer.parseInt(properties.getProperty("gridLineWidth", String.valueOf(DEFAULT_GRID_LINE_WIDTH)));
        } catch (NumberFormatException e) {
            return DEFAULT_GRID_LINE_WIDTH;
        }
    }
    
    // Setters
    public void setDefaultImageDir(String dir) {
        properties.setProperty("defaultImageDir", dir);
        autoSave();
    }
    
    public void setDefaultPdfDir(String dir) {
        properties.setProperty("defaultPdfDir", dir);
        autoSave();
    }
    
    public void setPaperSize(PaperSize paperSize) {
        properties.setProperty("paperSize", paperSize.name());
        autoSave();
    }
    
    public void setShowGrid(boolean showGrid) {
        properties.setProperty("showGrid", String.valueOf(showGrid));
        autoSave();
    }
    
    public void setShowTileNumbers(boolean showTileNumbers) {
        properties.setProperty("showTileNumbers", String.valueOf(showTileNumbers));
        autoSave();
    }
    
    public void setGridColor(Color gridColor) {
        properties.setProperty("gridColor", colorToString(gridColor));
        autoSave();
    }
    
    public void setExcludedColor(Color excludedColor) {
        properties.setProperty("excludedColor", colorToString(excludedColor));
        autoSave();
    }
    
    public void setDefaultScale(float scale) {
        properties.setProperty("defaultScale", String.valueOf(scale));
        autoSave();
    }
    
    public void setAutoSaveSettings(boolean autoSave) {
        properties.setProperty("autoSaveSettings", String.valueOf(autoSave));
        // Don't auto-save this setting to avoid recursion
        if (autoSave) {
            saveSettings();
        }
    }
    
    public void setConfirmOverwrites(boolean confirm) {
        properties.setProperty("confirmOverwrites", String.valueOf(confirm));
        autoSave();
    }
    
    public void setGridLineWidth(int width) {
        properties.setProperty("gridLineWidth", String.valueOf(width));
        autoSave();
    }
    
    private void autoSave() {
        if (isAutoSaveSettings()) {
            saveSettings();
        }
    }
    
    private String colorToString(Color color) {
        return String.format("%d,%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    
    private Color stringToColor(String colorStr) {
        try {
            String[] parts = colorStr.split(",");
            if (parts.length == 4) {
                return new Color(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
                );
            } else if (parts.length == 3) {
                return new Color(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                );
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Fall back to default color
        }
        return DEFAULT_GRID_COLOR;
    }
    
    /**
     * Reset all settings to defaults
     */
    public void resetToDefaults() {
        properties.clear();
        setDefaultsIfMissing();
        saveSettings();
    }
}
