import javax.microedition.lcdui.*;
import java.util.Vector;
import java.util.Random;

public class SnakeGame extends Canvas implements Runnable {
    private DiscoOs mainApp;
    private Thread gameThread;
    private boolean running = false;
    private boolean gameOver = false;
    
    private static final int CELL_SIZE = 8;
    private int gridWidth;
    private int gridHeight;
    
    private Vector snake;
    private int direction = 2; // 0=haut, 1=bas, 2=droite, 3=gauche
    private int foodX, foodY;
    private int score = 0;
    private Random random;
    
    public SnakeGame(DiscoOs app) {
        this.mainApp = app;
        this.random = new Random();
        
        gridWidth = getWidth() / CELL_SIZE;
        gridHeight = (getHeight() - 20) / CELL_SIZE;
        
        initGame();
    }
    
    private void initGame() {
        snake = new Vector();
        snake.addElement(new int[]{gridWidth/2, gridHeight/2});
        snake.addElement(new int[]{gridWidth/2-1, gridHeight/2});
        snake.addElement(new int[]{gridWidth/2-2, gridHeight/2});
        
        direction = 2;
        score = 0;
        gameOver = false;
        
        placeFood();
    }
    
    private void placeFood() {
        do {
            foodX = Math.abs(random.nextInt()) % gridWidth;
            foodY = Math.abs(random.nextInt()) % gridHeight;
        } while (isSnakeAt(foodX, foodY));
    }
    
    private boolean isSnakeAt(int x, int y) {
        for (int i = 0; i < snake.size(); i++) {
            int[] segment = (int[]) snake.elementAt(i);
            if (segment[0] == x && segment[1] == y) {
                return true;
            }
        }
        return false;
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void run() {
        while (running) {
            if (!gameOver) {
                update();
                repaint();
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {}
        }
    }
    
    private void update() {
        int[] head = (int[]) snake.elementAt(0);
        int newX = head[0];
        int newY = head[1];
        
        switch (direction) {
            case 0: newY--; break; // haut
            case 1: newY++; break; // bas
            case 2: newX++; break; // droite
            case 3: newX--; break; // gauche
        }
        
        // Collision avec les bords
        if (newX < 0 || newX >= gridWidth || newY < 0 || newY >= gridHeight) {
            gameOver = true;
            return;
        }
        
        // Collision avec soi-même
        if (isSnakeAt(newX, newY)) {
            gameOver = true;
            return;
        }
        
        // Ajouter nouvelle tête
        snake.insertElementAt(new int[]{newX, newY}, 0);
        
        // Manger nourriture
        if (newX == foodX && newY == foodY) {
            score += 10;
            placeFood();
        } else {
            // Retirer queue
            snake.removeElementAt(snake.size() - 1);
        }
    }
    
    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        // Fond
        g.setColor(0x000000);
        g.fillRect(0, 0, w, h);
        
        if (gameOver) {
            g.setColor(0xFF0000);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
            g.drawString("GAME OVER", w/2, h/2 - 20, Graphics.HCENTER | Graphics.TOP);
            g.drawString("Score: " + score, w/2, h/2 + 10, Graphics.HCENTER | Graphics.TOP);
            
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            g.setColor(0xFFFFFF);
            g.drawString("5=Rejouer  0=Quitter", w/2, h/2 + 40, Graphics.HCENTER | Graphics.TOP);
            return;
        }
        
        // Grille
        g.setColor(0x003300);
        for (int i = 0; i <= gridWidth; i++) {
            g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, gridHeight * CELL_SIZE);
        }
        for (int i = 0; i <= gridHeight; i++) {
            g.drawLine(0, i * CELL_SIZE, gridWidth * CELL_SIZE, i * CELL_SIZE);
        }
        
        // Serpent
        for (int i = 0; i < snake.size(); i++) {
            int[] segment = (int[]) snake.elementAt(i);
            if (i == 0) {
                g.setColor(0x00FF00); // Tête
            } else {
                g.setColor(0x00AA00); // Corps
            }
            g.fillRect(segment[0] * CELL_SIZE + 1, segment[1] * CELL_SIZE + 1, 
                      CELL_SIZE - 2, CELL_SIZE - 2);
        }
        
        // Nourriture
        g.setColor(0xFF0000);
        g.fillRect(foodX * CELL_SIZE + 1, foodY * CELL_SIZE + 1, 
                  CELL_SIZE - 2, CELL_SIZE - 2);
        
        // Score
        g.setColor(0xFFFFFF);
        g.drawString("Score: " + score, 2, h - 18, Graphics.LEFT | Graphics.TOP);
        g.drawString("2/8/4/6=Direction", 2, h - 2, Graphics.LEFT | Graphics.BOTTOM);
    }
    
    protected void keyPressed(int keyCode) {
        if (gameOver) {
            if (keyCode == Canvas.KEY_NUM5) {
                initGame();
                repaint();
            } else if (keyCode == Canvas.KEY_NUM0) {
                stop();
                mainApp.getDisplay().setCurrent(mainApp.getMainForm());
            }
            return;
        }
        
        // Contrôles
        if (keyCode == Canvas.KEY_NUM2 && direction != 1) {
            direction = 0; // haut
        } else if (keyCode == Canvas.KEY_NUM8 && direction != 0) {
            direction = 1; // bas
        } else if (keyCode == Canvas.KEY_NUM6 && direction != 3) {
            direction = 2; // droite
        } else if (keyCode == Canvas.KEY_NUM4 && direction != 2) {
            direction = 3; // gauche
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