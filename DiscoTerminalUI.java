import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * DiscoTerminalUI.java
 * Terminal avec T9 sur clavier PHYSIQUE numerique
 * 
 * MAPPING TOUCHES:
 * 0-9 physiques = T9 multi-tap (2=abc, 3=def, etc.)
 * # = Backspace (DEL)
 * * = Menu
 * Fire/OK = Executer commande
 * Touches directionnelles (joystick) = Navigation historique/scroll
 */
public class DiscoTerminalUI extends Canvas implements Runnable {
    private Display display;
    private DiscoOs mainApp;
    protected DiscoTerminalCore core;
    
    // Saisie T9
    private T9Input t9;
    
    // Threads
    private Thread cursorThread;
    private Thread timeoutThread;
    private boolean running = true;
    private boolean cursorVisible = true;
    
    // Affichage
    private Vector screenLines;
    private static final int MAX_LINES = 100;
    private int scrollOffset = 0;
    
    // Menus
    private boolean menuOpen = false;
    private int selectedMenuItem = 0;
    private String[] menuItems = {
        "1. EXEC (Fire)",
        "2. CLEAR",
        "3. HISTORIQUE",
        "4. AUTO-COMP",
        "5. MODE MAJ",
        "6. MODE 123",
        "0. QUITTER"
    };
    
    // Auto-completion
    private Vector completionList;
    private int completionIndex = 0;
    
    // Commandes disponibles (60+ commandes!)
    private static final String[] COMMANDS = {
        // Fichiers
        "ls", "cd", "pwd", "cat", "touch", "mkdir", "rmdir", "rm", "cp", "mv",
        "find", "tree", "du", "df", "file", "stat",
        // Texte
        "echo", "grep", "wc", "head", "tail", "sort", "uniq", "cut", "tr", "rev",
        // Systeme
        "ps", "top", "kill", "date", "uptime", "who", "w", "whoami", "id",
        "uname", "hostname", "reboot", "shutdown", "free",
        // Reseau
        "ping", "ifconfig", "netstat", "wget", "curl",
        // Utilitaires
        "calc", "cal", "bc", "factor", "seq", "yes", "true", "false", "sleep",
        // Permissions
        "chmod", "chown", "chgrp", "umask",
        // Environnement
        "env", "set", "export", "unset", "which", "whereis",
        // Shell
        "history", "alias", "unalias", "clear", "exit", "help", "man",
        // Applications
        "calculator", "editor", "edit", "snake", "tetris", "tasks", "http"
    };
    
    public DiscoTerminalUI(DiscoOs app, String user) {
        this.mainApp = app;
        this.display = app.getDisplay();
        this.core = new DiscoTerminalCore(app, user);
        this.t9 = new T9Input();
        this.screenLines = new Vector();
        this.completionList = new Vector();
        
        addLine("DiscoLinux 2.5 (" + core.getHostname() + ")");
        addLine("Type 'help' for commands");
        addLine("");
        addLine(core.getPrompt());
        
        setFullScreenMode(true);
        startThreads();
    }
    
    private void startThreads() {
        cursorThread = new Thread(this);
        cursorThread.start();
        
        timeoutThread = new Thread(new Runnable() {
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(50);
                repaint();         // Rafraîchir toujours
                    } catch (InterruptedException e) {}
                }
            }
        });
        timeoutThread.start();
    }
    
    public void run() {
        while (running) {
            try {
                Thread.sleep(500);
                cursorVisible = !cursorVisible;
                repaint();
            } catch (InterruptedException e) {}
        }
    }
    
    public void show() {
        display.setCurrent(this);
    }
    
    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, w, h);
        
        Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(font);
        
        int lineHeight = font.getHeight() + 2;
        int visibleLines = (h - 40) / lineHeight;
        
        g.setColor(ThemeManager.getForegroundColor());
        int startLine = Math.max(0, screenLines.size() - visibleLines - scrollOffset);
        int endLine = Math.min(screenLines.size(), startLine + visibleLines);
        
        for (int i = startLine; i < endLine; i++) {
            int y = (i - startLine) * lineHeight + 5;
            String line = (String) screenLines.elementAt(i);
            
            if (font.stringWidth(line) > w - 10) {
                while (font.stringWidth(line + "...") > w - 10 && line.length() > 0) {
                    line = line.substring(0, line.length() - 1);
                }
                line += "...";
            }
            
            g.drawString(line, 5, y, Graphics.LEFT | Graphics.TOP);
        }
        
        int inputY = h - 35;
        
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, inputY - 2, w, lineHeight + 4);
        
        String prompt = core.getPrompt();
        g.setColor(ThemeManager.getAccentColor());
        g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL));
        g.drawString(prompt, 2, inputY, Graphics.LEFT | Graphics.TOP);
        
        int promptWidth = g.getFont().stringWidth(prompt);
        String inputText = t9.getLiveText();
        
        String modeIndicator = "";
        if (t9.isNumericMode()) {
            modeIndicator = "[123]";
        } else if (t9.isUpperCase()) {
            modeIndicator = "[ABC]";
        }
        
        g.setColor(ThemeManager.getHighlightColor());
        g.drawString(modeIndicator, w - 40, inputY, Graphics.LEFT | Graphics.TOP);
        
        g.setColor(ThemeManager.getForegroundColor());
        if (cursorVisible) {
            g.drawString(inputText, promptWidth + 2, inputY, Graphics.LEFT | Graphics.TOP);
        } else {
            String textNoCursor = inputText;
            if (textNoCursor.endsWith("_")) {
                textNoCursor = textNoCursor.substring(0, textNoCursor.length() - 1);
            }
            g.drawString(textNoCursor, promptWidth + 2, inputY, Graphics.LEFT | Graphics.TOP);
        }
        
        if (completionList.size() > 0 && !menuOpen) {
            paintCompletions(g, w, h, inputY);
        }
        
        if (menuOpen) {
            paintMenu(g, w, h);
        }
        
        paintSoftkeys(g, w, h);
    }
    
    private void paintCompletions(Graphics g, int w, int h, int inputY) {
        int compY = inputY - 10 - (completionList.size() * 12);
        if (compY < 0) compY = inputY + 20;
        
        g.setColor(0x333333);
        g.fillRect(5, compY, w - 10, completionList.size() * 12 + 4);
        g.setColor(ThemeManager.getHighlightColor());
        g.drawRect(5, compY, w - 10, completionList.size() * 12 + 4);
        
        g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        
        for (int i = 0; i < Math.min(completionList.size(), 5); i++) {
            String completion = (String) completionList.elementAt(i);
            
            if (i == completionIndex % completionList.size()) {
                g.setColor(ThemeManager.getAccentColor());
                g.fillRect(7, compY + 2 + i * 12, w - 14, 11);
                g.setColor(ThemeManager.getBackgroundColor());
            } else {
                g.setColor(ThemeManager.getForegroundColor());
            }
            
            g.drawString(completion, 10, compY + 2 + i * 12, Graphics.LEFT | Graphics.TOP);
        }
    }
    
    private void paintMenu(Graphics g, int w, int h) {
        int menuW = Math.min(w - 20, 160);
        int menuH = menuItems.length * 16 + 30;
        int menuX = (w - menuW) / 2;
        int menuY = (h - menuH) / 2;
        
        g.setColor(0x000000);
        g.fillRect(menuX + 2, menuY + 2, menuW, menuH);
        
        g.setColor(0x222222);
        g.fillRect(menuX, menuY, menuW, menuH);
        g.setColor(ThemeManager.getHighlightColor());
        g.drawRect(menuX, menuY, menuW, menuH);
        
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.setColor(ThemeManager.getAccentColor());
        g.drawString("TERMINAL MENU", menuX + menuW/2, menuY + 5, Graphics.HCENTER | Graphics.TOP);
        
        g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        int itemY = menuY + 25;
        
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedMenuItem) {
                g.setColor(ThemeManager.getHighlightColor());
                g.fillRect(menuX + 5, itemY - 2, menuW - 10, 14);
                g.setColor(ThemeManager.getBackgroundColor());
            } else {
                g.setColor(ThemeManager.getForegroundColor());
            }
            
            g.drawString(menuItems[i], menuX + 10, itemY, Graphics.LEFT | Graphics.TOP);
            itemY += 16;
        }
    }
    
    private void paintSoftkeys(Graphics g, int w, int h) {
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, h - 18, w, 18);
        
        g.setColor(ThemeManager.getForegroundColor());
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        
        g.drawString("MENU", 2, h - 2, Graphics.LEFT | Graphics.BOTTOM);
        g.drawString("EXEC", w/2, h - 2, Graphics.HCENTER | Graphics.BOTTOM);
        g.drawString("DEL", w - 2, h - 2, Graphics.RIGHT | Graphics.BOTTOM);
    }
    
    /**
     * GESTION DES TOUCHES - CORRIGEE POUR T9 PHYSIQUE
     * 
     * LOGIQUE:
     * 1. Touches 0-9 physiques = TOUJOURS T9 (jamais navigation!)
     * 2. # = Backspace
     * 3. * = Menu
     * 4. Fire/OK = Executer
     * 5. Touches directionnelles (joystick) = Navigation UNIQUEMENT
     */
    protected void keyPressed(int keyCode) {
        // TOUCHES NUMERIQUES 0-9 = T9 UNIQUEMENT
        if (keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9) {
            if (menuOpen) {
                int num = keyCode - Canvas.KEY_NUM0;
                if (num < menuItems.length) {
                    selectedMenuItem = num;
                    executeMenuItem();
                }
                return;
            }
            
            // T9 multi-tap
            int num = keyCode - Canvas.KEY_NUM0;
            t9.keyPressed(num);
            updateCompletions();
            repaint();
            return;
        }
        
        // TOUCHE * = MENU
        if (keyCode == Canvas.KEY_STAR || keyCode == -6) {
            toggleMenu();
            return;
        }
        
        // TOUCHE # = BACKSPACE
        if (keyCode == Canvas.KEY_POUND || keyCode == -7) {
            t9.backspace();
            updateCompletions();
            repaint();
            return;
        }
        
        // FIRE/OK = EXECUTER
        if (keyCode == -5) {
            if (menuOpen) {
                executeMenuItem();
            } else {
                executeCommand();
            }
            return;
        }
        
        // TOUCHES DIRECTIONNELLES (joystick ou fleches)
        // Ces touches ne sont PAS 2468!
        int gameAction = getGameAction(keyCode);
        
        if (menuOpen) {
            if (gameAction == Canvas.UP) {
                selectedMenuItem = (selectedMenuItem - 1 + menuItems.length) % menuItems.length;
                repaint();
            } else if (gameAction == Canvas.DOWN) {
                selectedMenuItem = (selectedMenuItem + 1) % menuItems.length;
                repaint();
            }
            return;
        }
        
        // Historique (UP/DOWN)
        if (gameAction == Canvas.UP) {
            navigateHistory(-1);
            return;
        }
        if (gameAction == Canvas.DOWN) {
            navigateHistory(1);
            return;
        }
        
        // Scroll (LEFT/RIGHT)
        if (gameAction == Canvas.LEFT) {
            scrollOffset = Math.min(scrollOffset + 1, screenLines.size() - 5);
            repaint();
            return;
        }
        if (gameAction == Canvas.RIGHT) {
            scrollOffset = Math.max(scrollOffset - 1, 0);
            repaint();
            return;
        }
    }
    
    private void toggleMenu() {
        menuOpen = !menuOpen;
        selectedMenuItem = 0;
        repaint();
    }
    
    private void executeMenuItem() {
        menuOpen = false;
        
        switch (selectedMenuItem) {
            case 0: executeCommand(); break;
            case 1: clearScreen(); break;
            case 2: showHistory(); break;
            case 3: cycleCompletion(); break;
            case 4: t9.toggleCase(); break;
            case 5: t9.toggleNumericMode(); break;
            case 6: exitTerminal(); break;
        }
        
        repaint();
    }
    
    private void navigateHistory(int direction) {
        String historyCmd = "";
        
        if (direction < 0) {
            historyCmd = core.getPreviousCommand();
        } else {
            historyCmd = core.getNextCommand();
        }
        
        if (historyCmd != null) {
            t9.setText(historyCmd);
            updateCompletions();
            repaint();
        }
    }
    
    private void updateCompletions() {
        String input = t9.getText().trim();
        
        if (input.length() == 0) {
            completionList.removeAllElements();
            return;
        }
        
        int spaceIdx = input.lastIndexOf(' ');
        String prefix = spaceIdx >= 0 ? input.substring(spaceIdx + 1) : input;
        
        if (prefix.length() < 2) {
            completionList.removeAllElements();
            return;
        }
        
        completionList.removeAllElements();
        String lowerPrefix = prefix.toLowerCase();
        
        for (int i = 0; i < COMMANDS.length; i++) {
            if (COMMANDS[i].startsWith(lowerPrefix)) {
                completionList.addElement(COMMANDS[i]);
            }
        }
        
        completionIndex = 0;
    }
    
    private void cycleCompletion() {
        if (completionList.size() == 0) {
            updateCompletions();
            if (completionList.size() == 0) return;
        }
        
        String completion = (String) completionList.elementAt(completionIndex % completionList.size());
        String input = t9.getText();
        
        int spaceIdx = input.lastIndexOf(' ');
        if (spaceIdx >= 0) {
            t9.setText(input.substring(0, spaceIdx + 1) + completion);
        } else {
            t9.setText(completion);
        }
        
        completionIndex = (completionIndex + 1) % completionList.size();
        completionList.removeAllElements();
    }
    
    private void executeCommand() {
        String command = t9.getText().trim();
        
        if (command.length() == 0) return;
        
        addLine(core.getPrompt() + command);
        t9.addToHistory(command);
        
        String result = core.executeCommand(command);
        
        if (result != null) {
            if (result.equals("CLEAR_SCREEN")) {
                clearScreen();
                return;
            } else if (result.equals("EXIT_TERMINAL")) {
                exitTerminal();
                return;
            } else if (result.startsWith("LAUNCH_")) {
                handleLaunchCommand(result);
                return;
            }
        }
        
        if (result != null && result.length() > 0) {
            String[] lines = splitLines(result);
            for (int i = 0; i < lines.length; i++) {
                addLine(lines[i]);
            }
        }
        
        addLine(core.getPrompt());
        t9.clear();
        scrollOffset = 0;
        completionList.removeAllElements();
        repaint();
    }
    
    private void handleLaunchCommand(String cmd) {
        if (cmd.equals("LAUNCH_CALC")) {
            mainApp.launchCalculator();
        } else if (cmd.equals("LAUNCH_EDITOR")) {
            mainApp.launchTextEditor();
        } else if (cmd.equals("LAUNCH_SNAKE")) {
            mainApp.launchSnakeGame();
        } else if (cmd.equals("LAUNCH_TETRIS")) {
            mainApp.launchTetrisGame();
        } else if (cmd.equals("LAUNCH_TASKS")) {
            mainApp.launchTaskManager();
        } else if (cmd.equals("LAUNCH_HTTP")) {
            mainApp.launchHttpClient();
        }
    }
    
    private void clearScreen() {
        screenLines.removeAllElements();
        addLine(core.getPrompt());
        t9.clear();
        scrollOffset = 0;
        completionList.removeAllElements();
        repaint();
    }
    
    private void showHistory() {
        Vector history = t9.getHistory();
        screenLines.removeAllElements();
        
        addLine("=== Command History ===");
        for (int i = 0; i < history.size(); i++) {
            addLine((i + 1) + ": " + history.elementAt(i));
        }
        addLine("");
        addLine(core.getPrompt());
        
        t9.clear();
        scrollOffset = 0;
        repaint();
    }
    
    private void exitTerminal() {
        stop();
        mainApp.getDisplay().setCurrent(mainApp.getMainForm());
    }
    
    private void addLine(String line) {
        if (line == null) return;
        screenLines.addElement(line);
        
        if (screenLines.size() > MAX_LINES) {
            screenLines.removeElementAt(0);
        }
    }
    
    private String[] splitLines(String text) {
        Vector lines = new Vector();
        int start = 0;
        
        while (start < text.length()) {
            int end = text.indexOf('\n', start);
            if (end == -1) end = text.length();
            
            String line = text.substring(start, end);
            lines.addElement(line);
            start = end + 1;
        }
        
        String[] result = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            result[i] = (String) lines.elementAt(i);
        }
        
        return result;
    }
    
    public void stop() {
        running = false;
        
        if (cursorThread != null) {
            try {
                cursorThread.join();
            } catch (InterruptedException e) {}
        }
        
        if (timeoutThread != null) {
            try {
                timeoutThread.join();
            } catch (InterruptedException e) {}
        }
    }
}