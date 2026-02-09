import javax.microedition.lcdui.*;

public class AboutScreen extends Canvas implements CommandListener, Runnable {
    private DiscoOs mainApp;
    private Command backCmd;
    private Thread scrollThread;
    private boolean running = true;
    private int scrollY = 0;
    private String[] credits = {
         " ",
         "DiscoLinux ",
         "Version 2.5 ",
         " ",
         "=================== ",
         " ",
         "Developpe par: ",
         "Dash Animation V2 ",
         " ",
         "Avec l'aide de: ",
         "Claude (Anthropic AI) ",
         " ",
         "=================== ",
         " ",
         "Plateforme: ",
         "J2ME CLDC 1.1 ",
         "MIDP 2.0 ",
         " ",
         "Compatible: ",
         "Nokia N95 ",
         "Itel 5615 ",
         "Feature Phones ",
         " ",
         "=================== ",
         " ",
         "Nouveautes v2.5: ",
         " ",
         "- 15 Themes pros ",
         "- Lua 5.1 interpreter ",
         "- Prompt DiscoSysUsr ",
         "- Fichiers reels TFCard ",
         "- 65+ commandes Unix ",
         " ",
         "=================== ",
         " ",
         "Fonctionnalites: ",
         " ",
         "- Terminal Linux ",
         "- Login/Session ",
         "- Calculatrice ",
         "- Editeur de texte ",
         "- Gestionnaire fichiers ",
         "- Gestionnaire contacts ",
         "- Client HTTP ",
         "- Jeux (Snake, Tetris) ",
         "- Gestionnaire taches ",
         "- Themes personnalises ",
         "- Et plus... ",
         " ",
         "=================== ",
         " ",
         "Themes disponibles: ",
         " ",
         "Matrix, Hacker, ",
         "Night Mode, Ocean, ",
         "Fire, Purple Dream, ",
         "Dracula, Nord, ",
         "Monokai, Tokyo Night ",
         "Et 5 autres! ",
         " ",
         "=================== ",
         " ",
         "Remerciements: ",
         " ",
         "- Oracle (J2ME) ",
         "- Communaute Java ",
         "- Feature Phone Users ",
         "- Takumi123w and Mr Lima ",
         " ",
         "=================== ",
         " ",
         "License: ",
         "Open Source ",
         "Libre d'utilisation ",
         " ",
         "=================== ",
         " ",
         "Contact: ",
         "https://github.com/Dahmalahi and on Youtube as DASH ANIMATION V2",
         " ",
         "=================== ",
         " ",
         "Merci d'utiliser ",
         "DiscoLinux v2.5! ",
         " ",
         "2025-2026 ",
         " ",
         " "
    };

    public AboutScreen(DiscoOs app) {
        this.mainApp = app;
        
        backCmd = new Command("Retour", Command.BACK, 1);
        addCommand(backCmd);
        setCommandListener(this);
    }

    public void show() {
        mainApp.getDisplay().setCurrent(this);
        running = true;
        scrollY = getHeight(); // CORRIGÉ: getHeight() au lieu de "get Height()"
        scrollThread = new Thread(this);
        scrollThread.start();
    }

    public void run() {
        while (running) {
            scrollY--;
            if (scrollY < -(credits.length * 20 + 100)) {
                scrollY = getHeight();
            }
            
            repaint();
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {} // CORRIGÉ: InterruptedException au lieu de "Int erruptedException"
        }
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        // Appliquer thème
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, w, h);
        
        // Logo ASCII
        g.setColor(ThemeManager.getHighlightColor());
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
        
        int y = scrollY;
        
        // Afficher les crédits
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        
        for (int i = 0; i < credits.length; i++) {
            int lineY = y + (i * 20);
            
            // Ne dessiner que les lignes visibles
            if (lineY > -20 && lineY < h + 20) {
                // Centrer les lignes
                if (credits[i].indexOf("===") >= 0) {
                    g.setColor(ThemeManager.getHighlightColor());
                } else if (credits[i].length() == 0) {
                    continue;
                } else if (i < 3) {
                    g.setColor(ThemeManager.getAccentColor());
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
                } else {
                    g.setColor(ThemeManager.getForegroundColor());
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
                }
                
                g.drawString(credits[i], w/2, lineY, Graphics.HCENTER | Graphics.TOP);
            }
        }
        
        // Softkey
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, h - 20, w, 20); // CORRIGÉ: fillRect au lieu de "fillR ect"
        g.setColor(ThemeManager.getForegroundColor());
        g.drawString("Retour", w/2, h - 2, Graphics.HCENTER | Graphics.BOTTOM);
    }

    protected void keyPressed(int keyCode) {
        stop();
        mainApp.getDisplay().setCurrent(mainApp.getMainForm());
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            stop();
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }

    public void stop() {
        running = false;
        if (scrollThread != null) {
            try {
                scrollThread.join();
            } catch (InterruptedException e) {} // CORRIGÉ
        }
    }
}