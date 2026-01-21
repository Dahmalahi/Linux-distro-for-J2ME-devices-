public class ThemeManager {
    public static final int THEME_MATRIX = 0;
    public static final int THEME_HACKER = 1;
    public static final int THEME_DEFAULT = 2;
    public static final int THEME_NIGHT = 3;
    public static final int THEME_DAY = 4;
    
    private static int currentTheme = THEME_DEFAULT;
    
    // Couleurs par thème
    private static final int[][] THEMES = {
        // MATRIX: bg, fg, accent, menu, highlight
        {0x000000, 0x00FF00, 0x00AA00, 0x003300, 0x00FF00},
        // HACKER: bg, fg, accent, menu, highlight
        {0x000000, 0x00FF00, 0xFF0000, 0x001100, 0xFFFF00},
        // DEFAULT: bg, fg, accent, menu, highlight
        {0x000000, 0x00FF00, 0xFFFFFF, 0x003300, 0x00FFFF},
        // NIGHT: bg, fg, accent, menu, highlight
        {0x0A0A0A, 0xCCCCCC, 0x666666, 0x1A1A1A, 0x888888},
        // DAY: bg, fg, accent, menu, highlight
        {0xF0F0F0, 0x000000, 0x333333, 0xE0E0E0, 0x666666}
    };
    
    public static void setTheme(int theme) {
        if (theme >= 0 && theme < THEMES.length) {
            currentTheme = theme;
        }
    }
    
    public static int getCurrentTheme() {
        return currentTheme;
    }
    
    public static int getBackgroundColor() {
        return THEMES[currentTheme][0];
    }
    
    public static int getForegroundColor() {
        return THEMES[currentTheme][1];
    }
    
    public static int getAccentColor() {
        return THEMES[currentTheme][2];
    }
    
    public static int getMenuColor() {
        return THEMES[currentTheme][3];
    }
    
    public static int getHighlightColor() {
        return THEMES[currentTheme][4];
    }
    
    public static String getThemeName() {
        switch (currentTheme) {
            case THEME_MATRIX: return "Matrix";
            case THEME_HACKER: return "Hacker";
            case THEME_DEFAULT: return "Default";
            case THEME_NIGHT: return "Night Mode";
            case THEME_DAY: return "Day Mode";
            default: return "Unknown";
        }
    }
    
    public static String getThemeDescription() {
        switch (currentTheme) {
            case THEME_MATRIX:
                return "Classic green on black\nMatrix movie style";
            case THEME_HACKER:
                return "Hacker terminal\nGreen/Red accents";
            case THEME_DEFAULT:
                return "Default DiscoOS theme\nGreen terminal";
            case THEME_NIGHT:
                return "Dark mode\nEasy on the eyes";
            case THEME_DAY:
                return "Light mode\nBright display";
            default:
                return "No description";
        }
    }
}