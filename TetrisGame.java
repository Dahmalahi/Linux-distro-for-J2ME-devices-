import javax.microedition.lcdui.*;
import java.util.Random;

public class TetrisGame extends Canvas implements Runnable {
    private DiscoOs mainApp;
    private Thread gameThread;
    private boolean running = false;
    private boolean gameOver = false;
    
    private static final int CELL_SIZE = 8;
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 20;
    
    private int[][] grid;
    private int currentPiece;
    private int currentRotation;
    private int pieceX, pieceY;
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    private Random random;
    
    // Pièces Tetris (I, O, T, S, Z, J, L)
    private int[][][][] pieces = {
        // I
        {{{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}},
         {{0,0,1,0},{0,0,1,0},{0,0,1,0},{0,0,1,0}}},
        // O
        {{{0,1,1,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}}},
        // T
        {{{0,1,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
         {{0,1,0,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}},
         {{0,0,0,0},{1,1,1,0},{0,1,0,0},{0,0,0,0}},
         {{0,1,0,0},{1,1,0,0},{0,1,0,0},{0,0,0,0}}},
        // S
        {{{0,1,1,0},{1,1,0,0},{0,0,0,0},{0,0,0,0}},
         {{0,1,0,0},{0,1,1,0},{0,0,1,0},{0,0,0,0}}},
        // Z
        {{{1,1,0,0},{0,1,1,0},{0,0,0,0},{0,0,0,0}},
         {{0,0,1,0},{0,1,1,0},{0,1,0,0},{0,0,0,0}}},
        // J
        {{{1,0,0,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
         {{0,1,1,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}},
         {{0,0,0,0},{1,1,1,0},{0,0,1,0},{0,0,0,0}},
         {{0,1,0,0},{0,1,0,0},{1,1,0,0},{0,0,0,0}}},
        // L
        {{{0,0,1,0},{1,1,1,0},{0,0,0,0},{0,0,0,0}},
         {{0,1,0,0},{0,1,0,0},{0,1,1,0},{0,0,0,0}},
         {{0,0,0,0},{1,1,1,0},{1,0,0,0},{0,0,0,0}},
         {{1,1,0,0},{0,1,0,0},{0,1,0,0},{0,0,0,0}}}
    };
    
    private int[] pieceColors = {0x00FFFF, 0xFFFF00, 0xFF00FF, 0x00FF00, 0xFF0000, 0x0000FF, 0xFFA500};
    
    public TetrisGame(DiscoOs app) {
        this.mainApp = app;
        this.random = new Random();
        initGame();
    }
    
    private void initGame() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                grid[i][j] = 0;
            }
        }
        
        score = 0;
        level = 1;
        linesCleared = 0;
        gameOver = false;
        spawnPiece();
    }
    
    private void spawnPiece() {
        currentPiece = Math.abs(random.nextInt()) % 7;
        currentRotation = 0;
        pieceX = GRID_WIDTH / 2 - 2;
        pieceY = 0;
        
        if (checkCollision(pieceX, pieceY, currentRotation)) {
            gameOver = true;
        }
    }
    
    private boolean checkCollision(int x, int y, int rotation) {
        int[][] shape = pieces[currentPiece][rotation % pieces[currentPiece].length];
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    
                    if (newX < 0 || newX >= GRID_WIDTH || newY >= GRID_HEIGHT) {
                        return true;
                    }
                    if (newY >= 0 && grid[newY][newX] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void lockPiece() {
        int[][] shape = pieces[currentPiece][currentRotation % pieces[currentPiece].length];
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (shape[i][j] != 0) {
                    int newX = pieceX + j;
                    int newY = pieceY + i;
                    if (newY >= 0 && newY < GRID_HEIGHT && newX >= 0 && newX < GRID_WIDTH) {
                        grid[newY][newX] = currentPiece + 1;
                    }
                }
            }
        }
        
        clearLines();
        spawnPiece();
    }
    
    private void clearLines() {
        int cleared = 0;
        
        for (int i = GRID_HEIGHT - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < GRID_WIDTH; j++) {
                if (grid[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            
            if (full) {
                cleared++;
                for (int k = i; k > 0; k--) {
                    for (int j = 0; j < GRID_WIDTH; j++) {
                        grid[k][j] = grid[k-1][j];
                    }
                }
                for (int j = 0; j < GRID_WIDTH; j++) {
                    grid[0][j] = 0;
                }
                i++;
            }
        }
        
        if (cleared > 0) {
            linesCleared += cleared;
            score += cleared * cleared * 100;
            level = (linesCleared / 10) + 1;
        }
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void run() {
        long lastTime = System.currentTimeMillis();
        int dropDelay = 500;
        
        while (running) {
            long currentTime = System.currentTimeMillis();
            
            if (!gameOver && currentTime - lastTime > dropDelay - (level * 30)) {
                if (!checkCollision(pieceX, pieceY + 1, currentRotation)) {
                    pieceY++;
                } else {
                    lockPiece();
                }
                lastTime = currentTime;
            }
            
            repaint();
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
    }
    
    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        // Fond
        g.setColor(0x000000);
        g.fillRect(0, 0, w, h);
        
        int offsetX = (w - GRID_WIDTH * CELL_SIZE) / 2;
        int offsetY = 30;
        
        // Grille
        g.setColor(0x222222);
        for (int i = 0; i <= GRID_HEIGHT; i++) {
            g.drawLine(offsetX, offsetY + i * CELL_SIZE, 
                      offsetX + GRID_WIDTH * CELL_SIZE, offsetY + i * CELL_SIZE);
        }
        for (int i = 0; i <= GRID_WIDTH; i++) {
            g.drawLine(offsetX + i * CELL_SIZE, offsetY, 
                      offsetX + i * CELL_SIZE, offsetY + GRID_HEIGHT * CELL_SIZE);
        }
        
        // Blocs fixés
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                if (grid[i][j] != 0) {
                    g.setColor(pieceColors[grid[i][j] - 1]);
                    g.fillRect(offsetX + j * CELL_SIZE + 1, 
                              offsetY + i * CELL_SIZE + 1, 
                              CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        }
        
        // Pièce actuelle
        if (!gameOver) {
            int[][] shape = pieces[currentPiece][currentRotation % pieces[currentPiece].length];
            g.setColor(pieceColors[currentPiece]);
            
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (shape[i][j] != 0) {
                        int drawX = offsetX + (pieceX + j) * CELL_SIZE;
                        int drawY = offsetY + (pieceY + i) * CELL_SIZE;
                        g.fillRect(drawX + 1, drawY + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                    }
                }
            }
        }
        
        // Score
        g.setColor(0xFFFFFF);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
        g.drawString("Score: " + score, 5, 5, Graphics.LEFT | Graphics.TOP);
        g.drawString("Level: " + level, 5, 20, Graphics.LEFT | Graphics.TOP);
        g.drawString("Lines: " + linesCleared, w - 5, 5, Graphics.RIGHT | Graphics.TOP);
        
        if (gameOver) {
            g.setColor(0xFF0000);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
            g.drawString("GAME OVER", w/2, h/2 - 20, Graphics.HCENTER | Graphics.TOP);
            g.drawString("Score: " + score, w/2, h/2 + 10, Graphics.HCENTER | Graphics.TOP);
            
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            g.setColor(0xFFFFFF);
            g.drawString("5=Rejouer  0=Quitter", w/2, h/2 + 40, Graphics.HCENTER | Graphics.TOP);
        }
        
        // Contrôles
        g.setColor(0x888888);
        g.drawString("4/6=Gauche/Droite", 5, h - 15, Graphics.LEFT | Graphics.BOTTOM);
        g.drawString("2=Rotation 8=Drop", w - 5, h - 15, Graphics.RIGHT | Graphics.BOTTOM);
    }
    
    protected void keyPressed(int keyCode) {
        if (gameOver) {
            if (keyCode == Canvas.KEY_NUM5) {
                initGame();
            } else if (keyCode == Canvas.KEY_NUM0) {
                stop();
                mainApp.getDisplay().setCurrent(mainApp.getMainForm());
            }
            return;
        }
        
        if (keyCode == Canvas.KEY_NUM4) {
            if (!checkCollision(pieceX - 1, pieceY, currentRotation)) {
                pieceX--;
            }
        } else if (keyCode == Canvas.KEY_NUM6) {
            if (!checkCollision(pieceX + 1, pieceY, currentRotation)) {
                pieceX++;
            }
        } else if (keyCode == Canvas.KEY_NUM2) {
            int newRotation = (currentRotation + 1) % pieces[currentPiece].length;
            if (!checkCollision(pieceX, pieceY, newRotation)) {
                currentRotation = newRotation;
            }
        } else if (keyCode == Canvas.KEY_NUM8) {
            while (!checkCollision(pieceX, pieceY + 1, currentRotation)) {
                pieceY++;
            }
            lockPiece();
        } else if (keyCode == Canvas.KEY_NUM0) {
            stop();
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
    
    public void stop() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {}
        }
    }
}