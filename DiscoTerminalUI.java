import javax.microedition.lcdui.*;
import java.util.Vector;

public class DiscoTerminalUI extends Canvas implements Runnable {
    private Display display;
    private DiscoOs mainApp;
    private DiscoTerminalCore core;
    
    private Thread cursorThread;
    private boolean running = true;

    private String inputBuffer = "";
    private int cursorPos = 0;
    private boolean cursorVisible = true;

    // MENU LEFT
    private boolean leftMenuOpen = false;
    private boolean symbolMenuOpen = false;
    private int symbolPage = 0;
    private static final int SYMBOLS_PER_PAGE = 10;

    // T9
    private long lastKeyTime = 0;
    private int lastKeyCode = -1;
    private int keyPressCount = 0;
    private boolean t9ReplaceMode = false;

    private String[] t9Letters = {
        " 0", ".,!?1", "abc2", "def3", "ghi4",
        "jkl5", "mno6", "pqrs7", "tuv8", "wxyz9"
    };

    // SYMBOLES COMPLETS
    private String allSymbols = ".,?!:;-_\"'`~|\\/@%#&^*+=<>()[]{}$";
    private String[] symbolShortcuts = {
        "http://", "www.", ".com", ".net", ".org",
        "@gmail.com", "@yahoo.com"
    };

    public DiscoTerminalUI(DiscoOs app, String user) {
        this.mainApp = app;
        this.display = app.getDisplay();
        this.core = new DiscoTerminalCore(app, user);
        
        cursorThread = new Thread(this);
        cursorThread.start();
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

        g.setColor(ThemeManager.getForegroundColor());
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));

        int lh = g.getFont().getHeight();
        int maxLines = (h - 40) / lh;

        Vector screenLines = core.getScreenLines();
        int start = Math.max(0, screenLines.size() - maxLines);

        for (int i = start; i < screenLines.size(); i++) {
            String line = (String) screenLines.elementAt(i);
            g.drawString(line, 2, (i - start) * lh, Graphics.LEFT | Graphics.TOP);
        }

        int inputY = (screenLines.size() - start) * lh;
        String inputLine = core.getPrompt() + inputBuffer;
        g.drawString(inputLine, 2, inputY, Graphics.LEFT | Graphics.TOP);

        if (cursorVisible) {
            String beforeCursor = core.getPrompt() + inputBuffer.substring(0, cursorPos);
            int cx = g.getFont().stringWidth(beforeCursor);
            g.setColor(ThemeManager.getHighlightColor());
            g.fillRect(cx + 2, inputY + lh - 2, 6, 2);
        }

        g.setColor(ThemeManager.getMenuColor());
        g.drawString("MENU", 2, h - 2, Graphics.LEFT | Graphics.BOTTOM);
        g.drawString("DEL", w - 2, h - 2, Graphics.RIGHT | Graphics.BOTTOM);

        // MENU SYMBOLES
        if (symbolMenuOpen) {
            paintSymbolMenu(g, w, h);
        }
        // MENU PRINCIPAL
        else if (leftMenuOpen) {
            paintMainMenu(g, w, h);
        }
    }

    private void paintSymbolMenu(Graphics g, int w, int h) {
        int menuH = 140;
        int menuY = h - menuH - 25;
        
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(5, menuY, w - 10, menuH);
        
        g.setColor(ThemeManager.getForegroundColor());
        g.drawRect(5, menuY, w - 10, menuH);
        
        g.drawString("SYMBOLES (*/# page)", 10, menuY + 5, Graphics.LEFT | Graphics.TOP);
        
        int startIdx = symbolPage * SYMBOLS_PER_PAGE;
        int endIdx = Math.min(startIdx + SYMBOLS_PER_PAGE, allSymbols.length());
        
        int y = menuY + 20;
        for (int i = startIdx; i < endIdx; i++) {
            int num = (i - startIdx + 1) % 10;
            g.drawString(num + ": " + allSymbols.charAt(i), 10, y, Graphics.LEFT | Graphics.TOP);
            y += 12;
        }
        
        if (symbolPage == (allSymbols.length() / SYMBOLS_PER_PAGE)) {
            for (int i = 0; i < symbolShortcuts.length && i < 7; i++) {
                int num = (i + 1);
                g.drawString(num + ": " + symbolShortcuts[i], 10, y, Graphics.LEFT | Graphics.TOP);
                y += 12;
            }
        }
        
        g.drawString("Page " + (symbolPage + 1), w - 15, menuY + 5, Graphics.RIGHT | Graphics.TOP);
    }

    private void paintMainMenu(Graphics g, int w, int h) {
        int menuW = w - 40;
        int menuH = 100;
        int menuX = 20;
        int menuY = h - menuH - 30;
        
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(menuX, menuY, menuW, menuH);
        
        g.setColor(ThemeManager.getForegroundColor());
        g.drawRect(menuX, menuY, menuW, menuH);
        
        g.drawString("1. EXECUTER", menuX + 10, menuY + 10, Graphics.LEFT | Graphics.TOP);
        g.drawString("2. EFFACER", menuX + 10, menuY + 30, Graphics.LEFT | Graphics.TOP);
        g.drawString("3. CLEAR", menuX + 10, menuY + 50, Graphics.LEFT | Graphics.TOP);
        g.drawString("4. SORTIR", menuX + 10, menuY + 70, Graphics.LEFT | Graphics.TOP);
    }

    protected void keyPressed(int keyCode) {
        if (symbolMenuOpen) {
            handleSymbolMenuKey(keyCode);
            return;
        }

        if (keyCode == Canvas.KEY_STAR && !leftMenuOpen) {
            symbolMenuOpen = true;
            symbolPage = 0;
            repaint();
            return;
        }

        if (keyCode == -6) {
            leftMenuOpen = !leftMenuOpen;
            repaint();
            return;
        }

        if (leftMenuOpen) {
            handleMainMenuKey(keyCode);
            return;
        }

        if (keyCode == -7) {
            if (cursorPos > 0) {
                inputBuffer = inputBuffer.substring(0, cursorPos - 1) + inputBuffer.substring(cursorPos);
                cursorPos--;
            }
            resetT9();
            repaint();
            return;
        }

        if (keyCode == -5) {
            executeCommand();
            return;
        }

        if (keyCode == Canvas.KEY_NUM0) {
            insertChar(' ');
            resetT9();
            return;
        }

        if (keyCode == Canvas.KEY_POUND) {
            insertChar('#');
            resetT9();
            return;
        }

        int digit = -1;
        if (keyCode >= Canvas.KEY_NUM1 && keyCode <= Canvas.KEY_NUM9) {
            digit = keyCode - Canvas.KEY_NUM0;
        }

        if (digit >= 1 && digit <= 9) {
            handleT9(digit);
        }
    }

    private void handleSymbolMenuKey(int keyCode) {
        if (keyCode == Canvas.KEY_STAR) {
            symbolPage++;
            int maxPages = (allSymbols.length() / SYMBOLS_PER_PAGE) + 1;
            if (symbolPage > maxPages) symbolPage = 0;
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_POUND) {
            symbolPage--;
            if (symbolPage < 0) {
                symbolPage = (allSymbols.length() / SYMBOLS_PER_PAGE);
            }
            repaint();
            return;
        }
        
        if (keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9) {
            int num = keyCode - Canvas.KEY_NUM0;
            if (num == 0) num = 10;
            
            int idx = (symbolPage * SYMBOLS_PER_PAGE) + num - 1;
            
            if (idx < allSymbols.length()) {
                insertChar(allSymbols.charAt(idx));
                symbolMenuOpen = false;
                repaint();
                return;
            } else if (symbolPage == (allSymbols.length() / SYMBOLS_PER_PAGE)) {
                int shortcutIdx = num - 1;
                if (shortcutIdx < symbolShortcuts.length) {
                    insertString(symbolShortcuts[shortcutIdx]);
                    symbolMenuOpen = false;
                    repaint();
                    return;
                }
            }
        }
        
        if (keyCode == -7 || keyCode == -6) {
            symbolMenuOpen = false;
            repaint();
            return;
        }
    }

    private void handleMainMenuKey(int keyCode) {
        if (keyCode == Canvas.KEY_NUM1) {
            leftMenuOpen = false;
            executeCommand();
        } else if (keyCode == Canvas.KEY_NUM2) {
            leftMenuOpen = false;
            if (inputBuffer.length() > 0) {
                inputBuffer = "";
                cursorPos = 0;
            }
            repaint();
        } else if (keyCode == Canvas.KEY_NUM3) {
            leftMenuOpen = false;
            core.clearScreen();
            repaint();
        } else if (keyCode == Canvas.KEY_NUM4) {
            leftMenuOpen = false;
            stopCursor();
            display.setCurrent(mainApp.getMainForm());
        } else if (keyCode == -7) {
            leftMenuOpen = false;
            repaint();
        }
    }

    private void handleT9(int digit) {
        long now = System.currentTimeMillis();

        if (digit == lastKeyCode && (now - lastKeyTime) < 1000) {
            keyPressCount++;
            t9ReplaceMode = true;
        } else {
            keyPressCount = 0;
            t9ReplaceMode = false;
        }

        lastKeyCode = digit;
        lastKeyTime = now;

        String chars = t9Letters[digit];
        char ch = chars.charAt(keyPressCount % chars.length());

        if (t9ReplaceMode && cursorPos > 0) {
            inputBuffer = inputBuffer.substring(0, cursorPos - 1) + inputBuffer.substring(cursorPos);
            cursorPos--;
        }

        insertChar(ch);
    }

    private void insertChar(char c) {
        inputBuffer = inputBuffer.substring(0, cursorPos) + c + inputBuffer.substring(cursorPos);
        cursorPos++;
        repaint();
    }

    private void insertString(String s) {
        inputBuffer = inputBuffer.substring(0, cursorPos) + s + inputBuffer.substring(cursorPos);
        cursorPos += s.length();
        repaint();
    }

    private void resetT9() {
        lastKeyCode = -1;
        keyPressCount = 0;
        t9ReplaceMode = false;
    }
private void executeCommand() {
        String cmd = inputBuffer.trim();
        core.addLine(core.getPrompt() + inputBuffer);

        if (cmd.length() == 0) {
            inputBuffer = "";
            cursorPos = 0;
            core.addLine(core.getPrompt());
            repaint();
            return;
        }

        String out = core.processCommand(cmd);
        
        if (out != null && out.equals("EXIT_TERMINAL")) {
            stopCursor();
            display.setCurrent(mainApp.getMainForm());
            return;
        }
        
        if (out != null && out.length() > 0) {
            Vector v = core.split(out, '\n');
            for (int i = 0; i < v.size(); i++) {
                core.addLine((String) v.elementAt(i));
            }
        }

        inputBuffer = "";
        cursorPos = 0;
        core.addLine(core.getPrompt());
        resetT9();
        repaint();
    }

    private void stopCursor() {
        running = false;
        if (cursorThread != null) {
            try {
                cursorThread.join();
            } catch (InterruptedException e) {}
        }
    }
}