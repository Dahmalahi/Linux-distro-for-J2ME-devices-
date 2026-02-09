import javax.microedition.lcdui.*;

/**
 * PongGame.java v2.5 OPTIMISÉ
 * IA RALENTIE, JOUEUR ACCÉLÉRÉ, BALLE RALENTIE
 */
public class PongGame extends Canvas implements Runnable {
    private DiscoOs mainApp;
    private Thread gameThread;
    private boolean running = false, paused = false;
    
    private int width, height;
    private int paddleW, paddleH;
    private int playerY, aiY;
    private int baseSpeed; // Vitesse de base
    
    private int ballX, ballY, ballSize;
    private int ballSpeedX, ballSpeedY;
    
    private int playerScore = 0, aiScore = 0;
    private Command backCmd, pauseCmd;
    
    public PongGame(DiscoOs app) {
        this.mainApp = app;
        width = getWidth();
        height = getHeight();
        
        // ADAPTATION ÉCRAN
        if (width <= 128) {
            paddleW = 3; paddleH = 20;
            baseSpeed = 5;
            ballSize = 3;
            ballSpeedX = 2; // RÉDUIT (était 3)
            ballSpeedY = 2;
        } else if (width <= 176) {
            paddleW = 4; paddleH = 25;
            baseSpeed = 6;
            ballSize = 4;
            ballSpeedX = 3; // RÉDUIT (était 4)
            ballSpeedY = 3;
        } else {
            paddleW = 5; paddleH = 30;
            baseSpeed = 7;
            ballSize = 5;
            ballSpeedX = 4; // RÉDUIT (était 5)
            ballSpeedY = 4;
        }
        
        playerY = aiY = height/2 - paddleH/2;
        resetBall();
        
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
            if (!paused) {
                update();
                repaint();
            }
            try { Thread.sleep(16); } catch(Exception e) {}
        }
    }
    
    private void update() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;
        
        if (ballY <= 0 || ballY >= height - ballSize) ballSpeedY = -ballSpeedY;
        
        if (ballX <= paddleW && ballY + ballSize >= playerY && ballY <= playerY + paddleH) {
            ballSpeedX = -ballSpeedX;
            ballX = paddleW;
        }
        
        if (ballX + ballSize >= width - paddleW && ballY + ballSize >= aiY && ballY <= aiY + paddleH) {
            ballSpeedX = -ballSpeedX;
            ballX = width - paddleW - ballSize;
        }
        
        if (ballX < 0) {
            aiScore++;
            resetBall();
        } else if (ballX > width) {
            playerScore++;
            resetBall();
        }
        
        // IA - RALENTIE (baseSpeed / 2 au lieu de baseSpeed)
        int aiCenter = aiY + paddleH/2;
        int ballCenter = ballY + ballSize/2;
        
        if (ballCenter < aiCenter - 2) {
            aiY -= baseSpeed / 2; // IA 2X PLUS LENTE
        } else if (ballCenter > aiCenter + 2) {
            aiY += baseSpeed / 2; // IA 2X PLUS LENTE
        }
        
        if (playerY < 0) playerY = 0;
        if (playerY > height - paddleH) playerY = height - paddleH;
        if (aiY < 0) aiY = 0;
        if (aiY > height - paddleH) aiY = height - paddleH;
    }
    
    protected void paint(Graphics g) {
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, width, height);
        
        g.setColor(ThemeManager.getForegroundColor());
        for (int i = 0; i < height; i += 10) {
            g.drawLine(width/2, i, width/2, i+5);
        }
        
        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, 
            width <= 128 ? Font.SIZE_SMALL : Font.SIZE_LARGE);
        g.setFont(f);
        g.drawString("" + playerScore, width/4, 2, Graphics.TOP|Graphics.HCENTER);
        g.drawString("" + aiScore, width*3/4, 2, Graphics.TOP|Graphics.HCENTER);
        
        g.setColor(ThemeManager.getAccentColor());
        g.fillRect(0, playerY, paddleW, paddleH);
        g.fillRect(width - paddleW, aiY, paddleW, paddleH);
        
        g.setColor(ThemeManager.getHighlightColor());
        g.fillRect(ballX, ballY, ballSize, ballSize);
        
        if (paused) {
            g.setColor(ThemeManager.getForegroundColor());
            g.drawString("PAUSED", width/2, height/2, Graphics.HCENTER|Graphics.VCENTER);
        }
        
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, height-12, width, 12);
        g.setColor(ThemeManager.getForegroundColor());
        Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(small);
        g.drawString("2/8:Move 5:Pause", width/2, height-1, Graphics.BOTTOM|Graphics.HCENTER);
    }
    
    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        
        // JOUEUR ACCÉLÉRÉ (baseSpeed * 3 au lieu de * 2)
        if (action == UP || keyCode == Canvas.KEY_NUM2) {
            playerY -= baseSpeed * 3; // JOUEUR 1.5X PLUS RAPIDE
        } else if (action == DOWN || keyCode == Canvas.KEY_NUM8) {
            playerY += baseSpeed * 3; // JOUEUR 1.5X PLUS RAPIDE
        } else if (keyCode == Canvas.KEY_NUM5) {
            paused = !paused;
        }
    }
    
    private void resetBall() {
        ballX = width/2;
        ballY = height/2;
        if (System.currentTimeMillis() % 2 == 0) ballSpeedX = -ballSpeedX;
    }
}