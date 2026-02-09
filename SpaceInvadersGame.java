import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * SpaceInvadersGame.java v2.5
 * VITESSE JOUEUR = ALIENS (équitable)
 */
public class SpaceInvadersGame extends Canvas implements Runnable {
    private DiscoOs mainApp;
    private Thread gameThread;
    private boolean running = false, paused = false;
    
    private int width, height;
    private int playerX, playerY, playerW, playerH;
    private int speed; // MÊME POUR JOUEUR ET ALIENS
    
    private Vector aliens;
    private int alienW, alienH;
    private int alienDir = 1;
    private long lastAlienMove = 0;
    private int alienDelay;
    
    private Vector bullets, alienBullets;
    private int bulletSpeed;
    private long lastShot = 0, shootDelay;
    
    private int score = 0, lives = 3;
    private boolean gameOver = false;
    
    private Command backCmd, pauseCmd;
    
    class Alien { int x, y, type; Alien(int x, int y, int t) { this.x=x; this.y=y; type=t; }}
    class Bullet { int x, y; Bullet(int x, int y) { this.x=x; this.y=y; }}
    
    public SpaceInvadersGame(DiscoOs app) {
        this.mainApp = app;
        width = getWidth();
        height = getHeight();
        
        if (width <= 128) {
            playerW = 8; playerH = 6;
            speed = 5; // MÊME POUR JOUEUR ET ALIENS
            alienW = 8; alienH = 6;
            alienDelay = 500;
            bulletSpeed = 5;
            shootDelay = 300;
        } else if (width <= 176) {
            playerW = 10; playerH = 8;
            speed = 6;
            alienW = 10; alienH = 8;
            alienDelay = 400;
            bulletSpeed = 6;
            shootDelay = 250;
        } else {
            playerW = 12; playerH = 10;
            speed = 7;
            alienW = 12; alienH = 10;
            alienDelay = 350;
            bulletSpeed = 7;
            shootDelay = 200;
        }
        
        playerX = width/2;
        playerY = height - playerH - 15;
        
        aliens = new Vector();
        bullets = new Vector();
        alienBullets = new Vector();
        createAliens();
        
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
    
    private void createAliens() {
        aliens.removeAllElements();
        int rows = width <= 128 ? 3 : 4;
        int cols = width <= 128 ? 5 : 7;
        int startX = (width - cols * (alienW + 4)) / 2;
        int startY = 30;
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                aliens.addElement(new Alien(startX + c * (alienW + 4), startY + r * (alienH + 4), r));
            }
        }
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
        long now = System.currentTimeMillis();
        
        if (now - lastAlienMove >= alienDelay) {
            boolean change = false;
            for (int i = 0; i < aliens.size(); i++) {
                Alien a = (Alien)aliens.elementAt(i);
                a.x += alienDir * speed; // MÊME VITESSE
                if (a.x <= 0 || a.x >= width - alienW) change = true;
            }
            if (change) {
                alienDir = -alienDir;
                for (int i = 0; i < aliens.size(); i++) {
                    ((Alien)aliens.elementAt(i)).y += alienH/2;
                }
            }
            lastAlienMove = now;
            
            if (aliens.size() > 0 && System.currentTimeMillis() % 100 < 3) {
                Alien a = (Alien)aliens.elementAt((int)(System.currentTimeMillis() % aliens.size()));
                alienBullets.addElement(new Bullet(a.x + alienW/2, a.y + alienH));
            }
        }
        
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = (Bullet)bullets.elementAt(i);
            b.y -= bulletSpeed;
            if (b.y < 0) bullets.removeElementAt(i);
        }
        
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Bullet b = (Bullet)alienBullets.elementAt(i);
            b.y += bulletSpeed - 2;
            if (b.y > height) alienBullets.removeElementAt(i);
        }
        
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = (Bullet)bullets.elementAt(i);
            for (int j = aliens.size() - 1; j >= 0; j--) {
                Alien a = (Alien)aliens.elementAt(j);
                if (b.x >= a.x && b.x <= a.x + alienW && b.y >= a.y && b.y <= a.y + alienH) {
                    bullets.removeElementAt(i);
                    aliens.removeElementAt(j);
                    score += 10;
                    break;
                }
            }
        }
        
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Bullet b = (Bullet)alienBullets.elementAt(i);
            if (b.x >= playerX && b.x <= playerX + playerW && b.y >= playerY && b.y <= playerY + playerH) {
                alienBullets.removeElementAt(i);
                lives--;
                if (lives <= 0) gameOver = true;
            }
        }
        
        if (aliens.size() == 0) {
            alienDelay = Math.max(200, alienDelay - 50);
            createAliens();
        }
        
        for (int i = 0; i < aliens.size(); i++) {
            if (((Alien)aliens.elementAt(i)).y + alienH >= playerY) gameOver = true;
        }
    }
    
    protected void paint(Graphics g) {
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, width, height);
        
        g.setColor(ThemeManager.getForegroundColor());
        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(f);
        g.drawString("Score:" + score + " Lives:" + lives, 2, 2, Graphics.TOP|Graphics.LEFT);
        
        g.setColor(ThemeManager.getAccentColor());
        g.fillRect(playerX, playerY, playerW, playerH);
        g.fillRect(playerX + playerW/2 - 1, playerY - 3, 2, 3);
        
        for (int i = 0; i < aliens.size(); i++) {
            Alien a = (Alien)aliens.elementAt(i);
            g.setColor(a.type == 0 ? ThemeManager.getHighlightColor() : ThemeManager.getMenuColor());
            g.fillRect(a.x, a.y, alienW, alienH);
        }
        
        g.setColor(ThemeManager.getHighlightColor());
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = (Bullet)bullets.elementAt(i);
            g.fillRect(b.x, b.y, 2, 4);
        }
        
        g.setColor(0xFF0000); // Rouge pour balles aliens
        for (int i = 0; i < alienBullets.size(); i++) {
            Bullet b = (Bullet)alienBullets.elementAt(i);
            g.fillRect(b.x, b.y, 2, 4);
        }
        
        if (gameOver) {
            g.setColor(ThemeManager.getBackgroundColor());
            g.fillRect(width/4, height/2-15, width/2, 30);
            g.setColor(0xFF0000); // Rouge pour game over
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
        g.drawString("4/6:Move 5:Fire", width/2, height-1, Graphics.BOTTOM|Graphics.HCENTER);
    }
    
    protected void keyPressed(int keyCode) {
        if (gameOver) return;
        int action = getGameAction(keyCode);
        
        if (action == LEFT || keyCode == Canvas.KEY_NUM4) {
            playerX -= speed;
            if (playerX < 0) playerX = 0;
        } else if (action == RIGHT || keyCode == Canvas.KEY_NUM6) {
            playerX += speed;
            if (playerX > width - playerW) playerX = width - playerW;
        } else if (action == FIRE || keyCode == Canvas.KEY_NUM5) {
            long now = System.currentTimeMillis();
            if (now - lastShot >= shootDelay) {
                bullets.addElement(new Bullet(playerX + playerW/2, playerY));
                lastShot = now;
            }
        }
    }
}