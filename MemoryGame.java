import javax.microedition.lcdui.*;

/**
 * MemoryGame.java v2.5 OPTIMISÉ ÉCRANS
 * Grille 3x2 pour 128x128/160
 * Grille 3x4 pour 176x208
 * Grille 4x4 pour 240x320+
 */
public class MemoryGame extends Canvas implements CommandListener {
    private DiscoOs mainApp;
    
    private int width, height;
    private int rows, cols;
    private int cardWidth, cardHeight;
    private int offsetX, offsetY;
    
    private String[] symbols = {"A","B","C","D","E","F","G","H"};
    private int[] cards;
    private boolean[] revealed;
    
    private int cursorX = 0, cursorY = 0;
    private int firstCard = -1;
    private int secondCard = -1;
    
    private int moves = 0;
    private int pairsFound = 0;
    private int totalPairs;
    private boolean gameWon = false;
    
    private Command backCmd, resetCmd;
    
    public MemoryGame(DiscoOs app) {
        this.mainApp = app;
        width = getWidth();
        height = getHeight();
        
        // GRILLE ADAPTATIVE SELON TAILLE ÉCRAN
        if (width <= 128) {
            // Petit écran 128x128 ou 128x160
            rows = 3;
            cols = 2;
            cardWidth = 35;
            cardHeight = 35;
        } else if (width <= 176) {
            // Moyen 176x208
            rows = 3;
            cols = 4;
            cardWidth = 38;
            cardHeight = 38;
        } else {
            // Grand 240x320+
            rows = 4;
            cols = 4;
            cardWidth = 50;
            cardHeight = 50;
        }
        
        totalPairs = (rows * cols) / 2;
        
        // Centrer la grille
        int gridWidth = cols * cardWidth;
        int gridHeight = rows * cardHeight;
        offsetX = (width - gridWidth) / 2;
        offsetY = (height - gridHeight - 25) / 2 + 12;
        
        backCmd = new Command("Back", Command.BACK, 1);
        resetCmd = new Command("New", Command.SCREEN, 2);
        addCommand(backCmd);
        addCommand(resetCmd);
        setCommandListener(this);
        
        initGame();
    }
    
    private void initGame() {
        int total = rows * cols;
        cards = new int[total];
        revealed = new boolean[total];
        
        // Créer paires
        for (int i = 0; i < totalPairs; i++) {
            cards[i * 2] = i;
            cards[i * 2 + 1] = i;
        }
        
        // Mélanger
        for (int i = 0; i < total * 3; i++) {
            int a = (int)(System.currentTimeMillis() % total);
            int b = (int)((System.currentTimeMillis() / 7) % total);
            int temp = cards[a];
            cards[a] = cards[b];
            cards[b] = temp;
        }
        
        moves = 0;
        pairsFound = 0;
        gameWon = false;
        firstCard = -1;
        secondCard = -1;
        cursorX = 0;
        cursorY = 0;
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    protected void paint(Graphics g) {
        // Fond
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, width, height);
        
        // Header
        g.setColor(ThemeManager.getForegroundColor());
        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(f);
        g.drawString("Moves:" + moves + " Pairs:" + pairsFound + "/" + totalPairs, 
            width/2, 2, Graphics.TOP|Graphics.HCENTER);
        
        // Cartes
        Font cardFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, 
            width <= 128 ? Font.SIZE_MEDIUM : Font.SIZE_LARGE);
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = row * cols + col;
                int x = offsetX + col * cardWidth;
                int y = offsetY + row * cardHeight;
                
                // Curseur
                if (row == cursorY && col == cursorX) {
                    g.setColor(ThemeManager.getHighlightColor());
                    g.fillRect(x-1, y-1, cardWidth+2, cardHeight+2);
                }
                
                // Carte
                boolean show = revealed[idx] || idx == firstCard || idx == secondCard;
                
                if (show) {
                    // Révélée
                    g.setColor(ThemeManager.getAccentColor());
                    g.fillRect(x, y, cardWidth, cardHeight);
                    g.setColor(ThemeManager.getBackgroundColor());
                    g.setFont(cardFont);
                    String sym = symbols[cards[idx]];
                    g.drawString(sym, x + cardWidth/2, y + cardHeight/2, 
                        Graphics.HCENTER|Graphics.VCENTER);
                } else {
                    // Cachée
                    g.setColor(ThemeManager.getMenuColor());
                    g.fillRect(x, y, cardWidth, cardHeight);
                    g.setColor(ThemeManager.getForegroundColor());
                    g.setFont(cardFont);
                    g.drawString("?", x + cardWidth/2, y + cardHeight/2, 
                        Graphics.HCENTER|Graphics.VCENTER);
                }
                
                // Bordure
                g.setColor(ThemeManager.getForegroundColor());
                g.drawRect(x, y, cardWidth-1, cardHeight-1);
            }
        }
        
        // Victoire
        if (gameWon) {
            int boxW = width - 40;
            int boxH = 30;
            int boxX = 20;
            int boxY = height/2 - 15;
            
            g.setColor(ThemeManager.getBackgroundColor());
            g.fillRect(boxX, boxY, boxW, boxH);
            g.setColor(ThemeManager.getHighlightColor());
            g.drawRect(boxX, boxY, boxW, boxH);
            g.setColor(ThemeManager.getForegroundColor());
            g.setFont(f);
            g.drawString("YOU WIN! " + moves + " moves", width/2, height/2, 
                Graphics.HCENTER|Graphics.VCENTER);
        }
        
        // Instructions
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, height-12, width, 12);
        g.setColor(ThemeManager.getForegroundColor());
        Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(small);
        g.drawString("2468:Move 5:Flip", width/2, height-1, 
            Graphics.BOTTOM|Graphics.HCENTER);
    }
    
    protected void keyPressed(int keyCode) {
        if (gameWon) return;
        
        int action = getGameAction(keyCode);
        
        if (action == UP || keyCode == Canvas.KEY_NUM2) {
            cursorY = (cursorY - 1 + rows) % rows;
            repaint();
        } else if (action == DOWN || keyCode == Canvas.KEY_NUM8) {
            cursorY = (cursorY + 1) % rows;
            repaint();
        } else if (action == LEFT || keyCode == Canvas.KEY_NUM4) {
            cursorX = (cursorX - 1 + cols) % cols;
            repaint();
        } else if (action == RIGHT || keyCode == Canvas.KEY_NUM6) {
            cursorX = (cursorX + 1) % cols;
            repaint();
        } else if (action == FIRE || keyCode == Canvas.KEY_NUM5) {
            flipCard();
        }
    }
    
    private void flipCard() {
        int idx = cursorY * cols + cursorX;
        
        if (revealed[idx]) return;
        
        if (firstCard == -1) {
            firstCard = idx;
            repaint();
        } else if (secondCard == -1 && idx != firstCard) {
            secondCard = idx;
            moves++;
            repaint();
            
            try { Thread.sleep(600); } catch(Exception e) {}
            
            if (cards[firstCard] == cards[secondCard]) {
                revealed[firstCard] = true;
                revealed[secondCard] = true;
                pairsFound++;
                
                if (pairsFound == totalPairs) {
                    gameWon = true;
                }
            }
            
            firstCard = -1;
            secondCard = -1;
            repaint();
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            mainApp.showMainMenu();
        } else if (c == resetCmd) {
            initGame();
            repaint();
        }
    }
}