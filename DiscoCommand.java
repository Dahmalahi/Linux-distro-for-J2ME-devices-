/**
 * DiscoCommand.java v2.5 FINAL
 * Commande 'disco' + 'discoInfo' - Affiche ASCII art DiscoLinux
 */
public class DiscoCommand {
    
    /**
     * Génère l'ASCII art DiscoLinux
     */
    public static String getDiscoArt() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("\n");
        sb.append(" ====================================\n");
        sb.append(" ||  DISCO  LINUX  v2.5  Mobile  ||\n");
        sb.append(" ====================================\n");
        sb.append("\n");
        sb.append("   ___  _                _     _\n");
        sb.append("  / _ \\(_)___  ___ ___  | |   (_)_ __  _   ___  __\n");
        sb.append(" | | | | / __|/ __/ _ \\ | |   | | '_ \\| | | \\ \\/ /\n");
        sb.append(" | |_| | \\__ \\ (_| (_) || |___| | | | | |_| |>  <\n");
        sb.append("  \\___/|_|___/\\___\\___/ |_____|_|_| |_|\\__,_/_/\\_\\\n");
        sb.append("\n");
        sb.append(" Professional Linux Mobile OS\n");
        sb.append(" ====================================\n");
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * NOUVELLE COMMANDE: discoInfo (comme sur l'image)
     * Format avec logo ASCII en boîte + infos système
     */
    public static String getDiscoInfo(String username, String device, String theme) {
        StringBuffer sb = new StringBuffer();
        
        // Logo ASCII en forme de boîte (comme sur l'image)
        sb.append("\n");
        sb.append("  .---.\n");
        sb.append(" | [|] |  OS: DiscoLinux 2.5\n");
        sb.append(" |     |  Kernel: J2ME CLDC\n");
        sb.append("  '---'   Shell: DiscoShell\n");
        sb.append("          Theme: ").append(theme).append("\n");
        sb.append("          Device: ").append(device).append("\n");
        sb.append("          User: ").append(username).append("\n");
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Génère les informations système complètes
     */
    public static String getSystemInfo(DiscoTerminalCore terminal) {
        StringBuffer sb = new StringBuffer();
        Runtime rt = Runtime.getRuntime();
        
        long free = rt.freeMemory() / 1024;
        long total = rt.totalMemory() / 1024;
        long used = total - free;
        int percent = (int)((used * 100) / total);
        
        sb.append(" SYSTEM INFO:\n");
        sb.append(" ------------------------------------\n");
        sb.append(" Kernel:    J2ME CLDC 1.1 | MIDP 2.0\n");
        sb.append(" Version:   DiscoLinux v2.5\n");
        sb.append(" Theme:     ").append(ThemeManager.getThemeName()).append("\n");
        sb.append(" Commands:  95+ Unix commands\n");
        sb.append(" Features:  Lua 5.1 + Filesystem\n");
        sb.append(" Memory:    ").append(used).append("K / ").append(total).append("K (").append(percent).append("%)\n");
        sb.append(" Filesystem: DiscoSysUsr/ (TFCard)\n");
        sb.append("\n");
        sb.append(" FEATURES:\n");
        sb.append(" ------------------------------------\n");
        sb.append(" [x] 95+ Unix commands\n");
        sb.append(" [x] Lua 5.1 interpreter + filesystem\n");
        sb.append(" [x] 15 professional themes\n");
        sb.append(" [x] Real filesystem on TFCard\n");
        sb.append(" [x] T9 input with 100+ Unicode symbols\n");
        sb.append(" [x] Multi-user with login/session\n");
        sb.append(" [x] Scientific calculator\n");
        sb.append(" [x] HTTP client + 8 APIs\n");
        sb.append(" [x] 6 games (Snake, Tetris, Pong, etc.)\n");
        sb.append("\n");
        sb.append(" QUICK START:\n");
        sb.append(" ------------------------------------\n");
        sb.append(" help         - Show all commands\n");
        sb.append(" help1-5      - Commands by category\n");
        sb.append(" lua          - Start Lua interpreter\n");
        sb.append(" discoInfo    - System info (compact)\n");
        sb.append(" disco --art  - More ASCII art\n");
        sb.append("\n");
        sb.append(" Type 'help' for full command list!\n");
        sb.append(" ====================================\n");
        
        return sb.toString();
    }
    
    /**
     * Art alternatif (disco --art)
     */
    public static String getAlternateArt() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("\n");
        sb.append("    ____  __  ____  ____  ____\n");
        sb.append("   / __ \\/  |/  \\ \\/ / / / / /\n");
        sb.append("  / / / / /|_/ /\\  / /_/ / / \n");
        sb.append(" / /_/ / /  / / / / __  /_/  \n");
        sb.append("/_____/_/  /_/ /_/_/ /_(_)   \n");
        sb.append("\n");
        sb.append(" DISCOLINUX v2.5\n");
        sb.append(" Linux Mobile OS\n");
        sb.append(" Made with ♥ by Dash Animation\n");
        sb.append(" Powered by Claude AI\n");
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Logo compact pour boot screen
     */
    public static String getBootLogo() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("\n");
        sb.append(" ========================\n");
        sb.append(" ||   DISCOLINUX v2.5  ||\n");
        sb.append(" ||  Professional OS   ||\n");
        sb.append(" ========================\n");
        sb.append("\n");
        sb.append(" Booting...\n");
        
        return sb.toString();
    }
}