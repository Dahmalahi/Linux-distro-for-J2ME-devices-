import javax.microedition.lcdui.*;

/**
 * BreakoutGame.java v2.5 OPTIMISÉ
 * JOUEUR ACCÉLÉRÉ, BALLE RALENTIE
 */
public class BreakoutGame extends Canvas implements Runnable {
    private DiscoOs mainApp;
    private Thread gameThread;
    private boolean running = false, paused = false;
    
    private int width, height;
    private int paddleX, paddleY, paddleW, paddleH;
    private int baseSpeed; // Vitesse de base
    
    private int ballX, ballY, ballSize;
    private int ballSpeedX, ballSpeedY;
    
    private int[][] bricks;
    private int brickW, brickH;
    private int rows, cols;
    
    private int score = 0, lives = 3;
    private boolean gameOver = false;
    
    private Command backCmd, pauseCmd;
    
    public BreakoutGame(DiscoOs app) {
        this.mainApp = app;
        width = getWidth();
        height = getHeight();
        
        if (width <= 128) {
            paddleW = 20; paddleH = 4;
            baseSpeed = 5;
            ballSize = 3;
            ballSpeedX = 2; // RÉDUIT (était 3)
            ballSpeedY = -2;
            rows = 4; cols = 6;
            brickW = 18; brickH = 6;
        } else if (width <= 176) {
            paddleW = 30; paddleH = 5;
            baseSpeed = 6;
            ballSize = 4;
            ballSpeedX = 3; // RÉDUIT (était 4)
            ballSpeedY = -3;
            rows = 5; cols = 7;
            brickW = 22; brickH = 8;
        } else {
            paddleW = 40; paddleH = 6;
            baseSpeed = 7;
            ballSize = 5;
            ballSpeedX = 4; // RÉDUIT (était 5)
            ballSpeedY = -4;
            rows = 6; cols = 9;
            brickW = 24; brickH = 10;
        }
        
        paddleX = width/2 - paddleW/2;
        paddleY = height - 20;
        ballX = width/2;
        ballY = height/2;
        
        bricks = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bricks[r][c] = 1;
            }
        }
        
        backCmd = new Command("Back", Command.BACK, 1);
        pauseCmd = new Command("Pause", Command.SCREEN, 2);
        addCommand(backCmd);
        addCommand(pauseCmd);
        
        setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == backCmd) {
                    stop();
                    mainApp.showMainMenu();
                } else if (c == pauseCmd) {
                    paused = !paused;
                }
            }
        });
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
        start();
    }
    
    public void start() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void stop() {
        running = false;
    }
    
    public void run() {
        while (running) {
            if (!paused && !gameOver) {
                update();
                repaint();
            }
            try { Thread.sleep(16); } catch(Exception e) {}
        }
    }
    
    private void update() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;
        
        if (ballX <= 0 || ballX >= width - ballSize) ballSpeedX = -ballSpeedX;
        if (ballY <= 0) ballSpeedY = -ballSpeedY;
        
        if (ballY + ballSize >= paddleY && ballX + ballSize >= paddleX && ballX <= paddleX + paddleW) {
            ballSpeedY = -ballSpeedY;
            ballY = paddleY - ballSize;
        }
        
        if (ballY > height) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
            } else {
                ballX = width/2;
                ballY = height/2;
                ballSpeedY = -Math.abs(ballSpeedY);
            }
        }
        
        int startX = (width - cols * brickW) / 2;
        int startY = 30;
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bricks[r][c] == 1) {
                    int bx = startX + c * brickW;
                    int by = startY + r * brickH;
                    
                    if (ballX + ballSize >= bx && ballX <= bx + brickW &&
                        ballY + ballSize >= by && ballY <= by + brickH) {
                        bricks[r][c] = 0;
                        ballSpeedY = -ballSpeedY;
                        score += 10;
                    }
                }
            }
        }
        
        boolean allGone = true;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bricks[r][c] == 1) allGone = false;
            }
        }
        if (allGone) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    bricks[r][c] = 1;
                }
            }
        }
    }
    
    protected void paint(Graphics g) {
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, width, height);
        
        g.setColor(ThemeManager.getForegroundColor());
        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(f);
        g.drawString("Score:" + score + " Lives:" + lives, 2, 2, Graphics.TOP|Graphics.LEFT);
        
        int startX = (width - cols * brickW) / 2;
        int startY = 30;
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bricks[r][c] == 1) {
                    int color = r % 2 == 0 ? ThemeManager.getHighlightColor() : ThemeManager.getAccentColor();
                    g.setColor(color);
                    g.fillRect(startX + c * brickW, startY + r * brickH, brickW - 2, brickH - 2);
                }
            }
        }
        
        g.setColor(ThemeManager.getAccentColor());
        g.fillRect(paddleX, paddleY, paddleW, paddleH);
        
        g.setColor(ThemeManager.getHighlightColor());
        g.fillRect(ballX, ballY, ballSize, ballSize);
        
        if (gameOver) {
            g.setColor(ThemeManager.getBackgroundColor());
            g.fillRect(width/4, height/2-15, width/2, 30);
            g.setColor(0xFF0000);
            g.drawRect(width/4, height/2-15, width/2, 30);
            g.setColor(ThemeManager.getForegroundColor());
            g.drawString("GAME OVER", width/2, height/2, Graphics.HCENTER|Graphics.VCENTER);
        }
        
        if (paused) {
            g.setColor(ThemeManager.getForegroundColor());
            g.drawString("PAUSED", width/2, height/2, Graphics.HCENTER|Graphics.VCENTER);
        }
        
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, height-12, width, 12);
        g.setColor(ThemeManager.getForegroundColor());
        g.drawString("4/6:Move 5:Pause", width/2, height-1, Graphics.BOTTOM|Graphics.HCENTER);
    }
    
    protected void keyPressed(int keyCode) {
        if (gameOver) return;
        int action = getGameAction(keyCode);
        
        // JOUEUR ACCÉLÉRÉ (baseSpeed * 2 au lieu de juste baseSpeed)
        if (action == LEFT || keyCode == Canvas.KEY_NUM4) {
            paddleX -= baseSpeed * 2; // JOUEUR 2X PLUS RAPIDE
            if (paddleX < 0) paddleX = 0;
        } else if (action == RIGHT || keyCode == Canvas.KEY_NUM6) {
            paddleX += baseSpeed * 2; // JOUEUR 2X PLUS RAPIDE
            if (paddleX > width - paddleW) paddleX = width - paddleW;
        } else if (keyCode == Canvas.KEY_NUM5) {
            paused = !paused;
        }
    }
}