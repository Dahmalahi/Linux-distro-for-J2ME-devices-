/**
 * ThemeManager.java
 * Gestionnaire de 15 thèmes professionnels
 */
public class ThemeManager {
    private static int currentTheme = 0;
    
    public static final int THEME_MATRIX = 0;
    public static final int THEME_HACKER = 1;
    public static final int THEME_DEFAULT = 2;
    public static final int THEME_NIGHT = 3;
    public static final int THEME_DAY = 4;
    public static final int THEME_OCEAN = 5;
    public static final int THEME_FIRE = 6;
    public static final int THEME_PURPLE = 7;
    public static final int THEME_AMBER = 8;
    public static final int THEME_DRACULA = 9;
    public static final int THEME_NORD = 10;
    public static final int THEME_MONOKAI = 11;
    public static final int THEME_SOLARIZED = 12;
    public static final int THEME_GRUVBOX = 13;
    public static final int THEME_TOKYO = 14;
    
    private static final String[] THEME_NAMES = {
        "Matrix", "Hacker", "Default", "Night Mode", "Day Mode",
        "Ocean", "Fire", "Purple Dream", "Amber Terminal", "Dracula",
        "Nord", "Monokai", "Solarized Dark", "Gruvbox", "Tokyo Night"
    };
    
    public static void setTheme(int theme) {
        if (theme >= 0 && theme < THEME_NAMES.length) {
            currentTheme = theme;
        }
    }
    
    public static int getTheme() { return currentTheme; }
    
    // NOUVELLE MÉTHODE MANQUANTE
    public static int getCurrentTheme() { return currentTheme; }
    
    public static String getThemeName() { return THEME_NAMES[currentTheme]; }
    public static String[] getAllThemeNames() { return THEME_NAMES; }
    public static int getThemeCount() { return THEME_NAMES.length; }
    
    public static int getBackgroundColor() {
        switch (currentTheme) {
            case 0: return 0x000000; case 1: return 0x001100; case 2: return 0x000000;
            case 3: return 0x0A0A0A; case 4: return 0xFFFFFF; case 5: return 0x001030;
            case 6: return 0x1A0000; case 7: return 0x1A0A2E; case 8: return 0x0F0F00;
            case 9: return 0x282A36; case 10: return 0x2E3440; case 11: return 0x272822;
            case 12: return 0x002B36; case 13: return 0x282828; case 14: return 0x1A1B26;
            default: return 0x000000;
        }
    }
    
    public static int getForegroundColor() {
        switch (currentTheme) {
            case 0: return 0x00FF00; case 1: return 0x00FF41; case 2: return 0xFFFFFF;
            case 3: return 0xCCCCCC; case 4: return 0x000000; case 5: return 0x00D9FF;
            case 6: return 0xFF4500; case 7: return 0xDA70D6; case 8: return 0xFFB000;
            case 9: return 0xF8F8F2; case 10: return 0xD8DEE9; case 11: return 0xF8F8F2;
            case 12: return 0x839496; case 13: return 0xEBDBB2; case 14: return 0xA9B1D6;
            default: return 0xFFFFFF;
        }
    }
    
    public static int getAccentColor() {
        switch (currentTheme) {
            case 0: return 0x00DD00; case 1: return 0x39FF14; case 2: return 0x00FF00;
            case 3: return 0x4CAF50; case 4: return 0x2196F3; case 5: return 0x00FFFF;
            case 6: return 0xFF6347; case 7: return 0xFF00FF; case 8: return 0xFFCC00;
            case 9: return 0xFF79C6; case 10: return 0x88C0D0; case 11: return 0x66D9EF;
            case 12: return 0x268BD2; case 13: return 0xFE8019; case 14: return 0x7AA2F7;
            default: return 0x00FF00;
        }
    }
    
    public static int getHighlightColor() {
        switch (currentTheme) {
            case 0: return 0x00AA00; case 1: return 0x00FF00; case 2: return 0xFFFF00;
            case 3: return 0x8BC34A; case 4: return 0xFF9800; case 5: return 0x4DD0E1;
            case 6: return 0xFFD700; case 7: return 0xBA55D3; case 8: return 0xFFA500;
            case 9: return 0x8BE9FD; case 10: return 0x81A1C1; case 11: return 0xA6E22E;
            case 12: return 0x2AA198; case 13: return 0xB8BB26; case 14: return 0x9ECE6A;
            default: return 0xFFFF00;
        }
    }
    
    public static int getMenuColor() {
        switch (currentTheme) {
            case 0: return 0x001100; case 1: return 0x003300; case 2: return 0x222222;
            case 3: return 0x1E1E1E; case 4: return 0xEEEEEE; case 5: return 0x002040;
            case 6: return 0x330000; case 7: return 0x2E1A47; case 8: return 0x332200;
            case 9: return 0x44475A; case 10: return 0x3B4252; case 11: return 0x3E3D32;
            case 12: return 0x073642; case 13: return 0x3C3836; case 14: return 0x24283B;
            default: return 0x222222;
        }
    }
    
    public static int getErrorColor() {
        switch (currentTheme) {
            case 0: return 0xFF0000; case 1: return 0xFF0000; case 2: return 0xFF0000;
            case 3: return 0xF44336; case 4: return 0xD32F2F; case 5: return 0xFF5252;
            case 6: return 0xFF0000; case 7: return 0xFF1744; case 8: return 0xFF0000;
            case 9: return 0xFF5555; case 10: return 0xBF616A; case 11: return 0xF92672;
            case 12: return 0xDC322F; case 13: return 0xCC241D; case 14: return 0xF7768E;
            default: return 0xFF0000;
        }
    }
    
    public static int getSuccessColor() {
        switch (currentTheme) {
            case 0: return 0x00FF00; case 1: return 0x00FF00; case 2: return 0x00FF00;
            case 3: return 0x4CAF50; case 4: return 0x388E3C; case 5: return 0x00E676;
            case 6: return 0x00FF00; case 7: return 0x00E676; case 8: return 0x00FF00;
            case 9: return 0x50FA7B; case 10: return 0xA3BE8C; case 11: return 0xA6E22E;
            case 12: return 0x859900; case 13: return 0x98971A; case 14: return 0x9ECE6A;
            default: return 0x00FF00;
        }
    }
    
    public static String getThemeDescription() {
        switch (currentTheme) {
            case 0: return "Matrix\nGreen on black\nClassic hacker";
            case 1: return "Hacker\nNeon green\nCyberpunk style";
            case 2: return "Default\nWhite on black\nStandard terminal";
            case 3: return "Night Mode\nSoft gray\nEye-friendly";
            case 4: return "Day Mode\nBlack on white\nHigh contrast";
            case 5: return "Ocean\nCyan waves\nDeep sea vibes";
            case 6: return "Fire\nRed & orange\nHot terminal";
            case 7: return "Purple Dream\nViolet hues\nMystic theme";
            case 8: return "Amber Terminal\nRetro orange\n80s computer";
            case 9: return "Dracula\nPurple-gray\nPopular IDE";
            case 10: return "Nord\nArctic blue\nScandinavian";
            case 11: return "Monokai\nWarm gray\nSublime Text";
            case 12: return "Solarized Dark\nBlue-gray\nLow contrast";
            case 13: return "Gruvbox\nRetro warm\nVintage colors";
            case 14: return "Tokyo Night\nNeon city\nModern dark";
            default: return "Unknown";
        }
    }
}