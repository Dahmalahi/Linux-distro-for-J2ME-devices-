import javax.microedition.lcdui.*;
import java.util.Vector;

public class CalculatorApp extends Canvas implements CommandListener, Runnable {
    private DiscoOs mainApp;
    private String display = "0";
    private String operator = "";
    private double firstNumber = 0;
    private boolean newNumber = true;
    private boolean calculating = false;
    
    private Vector history;
    private boolean showHistory = false;
    
    private Command clearCmd, backCmd, historyCmd;
    private Thread displayThread;
    private boolean running = true;
    
    public CalculatorApp(DiscoOs app) {
        this.mainApp = app;
        this.history = new Vector();
        
        clearCmd = new Command("C", Command.SCREEN, 1);
        historyCmd = new Command("Historique", Command.SCREEN, 2);
        backCmd = new Command("Retour", Command.BACK, 3);
        
        addCommand(clearCmd);
        addCommand(historyCmd);
        addCommand(backCmd);
        setCommandListener(this);
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
        running = true;
        displayThread = new Thread(this);
        displayThread.start();
    }
    
    public void run() {
        while (running) {
            try {
                Thread.sleep(100);
                repaint();
            } catch (InterruptedException e) {}
        }
    }
    
    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        // Appliquer le thème
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, w, h);
        
        if (showHistory) {
            paintHistory(g, w, h);
            return;
        }
        
        // Titre
        g.setColor(ThemeManager.getForegroundColor());
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString("Calculatrice", w/2, 5, Graphics.HCENTER | Graphics.TOP);
        
        // Écran d'affichage
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(5, 25, w - 10, 50);
        g.setColor(ThemeManager.getForegroundColor());
        g.drawRect(5, 25, w - 10, 50);
        
        // Afficher le nombre et l'opérateur
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
        
        // Afficher l'opérateur en cours
        if (operator.length() > 0 && !newNumber) {
            g.setColor(ThemeManager.getHighlightColor());
            g.drawString(operator, 10, 35, Graphics.LEFT | Graphics.TOP);
        }
        
        // Afficher le nombre
        g.setColor(ThemeManager.getForegroundColor());
        String displayText = display;
        if (displayText.length() > 15) {
            displayText = displayText.substring(0, 15);
        }
        g.drawString(displayText, w - 10, 50, Graphics.RIGHT | Graphics.VCENTER);
        
        // Instructions
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        g.setColor(ThemeManager.getAccentColor());
        
        int y = 85;
        g.drawString("=== TOUCHES ===", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 18;
        
        g.setColor(ThemeManager.getForegroundColor());
        g.drawString("0-9 : Chiffres", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 15;
        g.drawString("* : Point (.)", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 15;
        g.drawString("# : Effacer", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 20;
        
        g.setColor(ThemeManager.getHighlightColor());
        g.drawString("2 : + (Addition)", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 15;
        g.drawString("4 : - (Soustraction)", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 15;
        g.drawString("6 : x (Multiplication)", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 15;
        g.drawString("8 : / (Division)", 10, y, Graphics.LEFT | Graphics.TOP);
        y += 20;
        
        g.setColor(ThemeManager.getAccentColor());
        g.drawString("5 ou Fire : = (Egal)", 10, y, Graphics.LEFT | Graphics.TOP);
        
        // Softkeys
        g.setColor(ThemeManager.getMenuColor());
        g.fillRect(0, h - 20, w, 20);
        g.setColor(ThemeManager.getForegroundColor());
        g.drawString("Effacer", 2, h - 2, Graphics.LEFT | Graphics.BOTTOM);
        g.drawString("Retour", w - 2, h - 2, Graphics.RIGHT | Graphics.BOTTOM);
    }
    
    private void paintHistory(Graphics g, int w, int h) {
        g.setColor(ThemeManager.getBackgroundColor());
        g.fillRect(0, 0, w, h);
        
        g.setColor(ThemeManager.getForegroundColor());
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString("Historique", w/2, 5, Graphics.HCENTER | Graphics.TOP);
        
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        
        int y = 30;
        int maxLines = (h - 50) / 15;
        int start = Math.max(0, history.size() - maxLines);
        
        for (int i = start; i < history.size(); i++) {
            String entry = (String) history.elementAt(i);
            g.drawString(entry, 5, y, Graphics.LEFT | Graphics.TOP);
            y += 15;
        }
        
        if (history.size() == 0) {
            g.drawString("(Aucun calcul)", w/2, h/2, Graphics.HCENTER | Graphics.VCENTER);
        }
        
        g.setColor(ThemeManager.getAccentColor());
        g.drawString("Appuyez sur une touche", w/2, h - 20, Graphics.HCENTER | Graphics.BOTTOM);
    }
    
    protected void keyPressed(int keyCode) {
        if (showHistory) {
            showHistory = false;
            repaint();
            return;
        }
        
        // Chiffres 0-9
        if (keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9) {
            int digit = keyCode - Canvas.KEY_NUM0;
            appendDigit(String.valueOf(digit));
            repaint();
        }
        // Point décimal
        else if (keyCode == Canvas.KEY_STAR) {
            appendDecimal();
            repaint();
        }
        // Effacer tout
        else if (keyCode == Canvas.KEY_POUND) {
            clear();
            repaint();
        }
        // Addition (touche 2)
        else if (keyCode == Canvas.KEY_NUM2) {
            if (!calculating) {
                setOperator("+");
                repaint();
            }
        }
        // Soustraction (touche 4)
        else if (keyCode == Canvas.KEY_NUM4) {
            if (!calculating) {
                setOperator("-");
                repaint();
            }
        }
        // Multiplication (touche 6)
        else if (keyCode == Canvas.KEY_NUM6) {
            if (!calculating) {
                setOperator("*");
                repaint();
            }
        }
        // Division (touche 8)
        else if (keyCode == Canvas.KEY_NUM8) {
            if (!calculating) {
                setOperator("/");
                repaint();
            }
        }
        // Égal (touche 5 ou Fire)
        else if (keyCode == Canvas.KEY_NUM5 || keyCode == -5) {
            if (!calculating) {
                calculate();
                repaint();
            }
        }
    }
    
    private void appendDigit(String digit) {
        if (newNumber) {
            display = digit;
            newNumber = false;
        } else {
            if (display.equals("0")) {
                display = digit;
            } else {
                // Limiter à 12 chiffres
                if (display.length() < 12) {
                    display += digit;
                }
            }
        }
    }
    
    private void appendDecimal() {
        if (display.indexOf('.') == -1) {
            if (newNumber) {
                display = "0.";
                newNumber = false;
            } else {
                display += ".";
            }
        }
    }
    
    private void setOperator(String op) {
        calculating = true;
        try {
            // Si on a déjà un opérateur, calculer d'abord
            if (operator.length() > 0 && !newNumber) {
                calculate();
            } else {
                firstNumber = Double.parseDouble(display);
            }
            
            operator = op;
            newNumber = true;
            calculating = false;
        } catch (NumberFormatException e) {
            display = "Erreur";
            operator = "";
            newNumber = true;
            calculating = false;
        }
    }
    
    private void calculate() {
        if (operator.length() == 0 || newNumber) {
            calculating = false;
            return;
        }
        
        calculating = true;
        
        try {
            double secondNumber = Double.parseDouble(display);
            double result = 0;
            boolean error = false;
            
            String calcStr = firstNumber + " " + operator + " " + secondNumber + " = ";
            
            if (operator.equals("+")) {
                result = firstNumber + secondNumber;
            } else if (operator.equals("-")) {
                result = firstNumber - secondNumber;
            } else if (operator.equals("*")) {
                result = firstNumber * secondNumber;
            } else if (operator.equals("/")) {
                if (secondNumber == 0) {
                    display = "Div/0 Error";
                    error = true;
                } else {
                    result = firstNumber / secondNumber;
                }
            }
            
            if (!error) {
                // Formater le résultat
                if (result == (long) result) {
                    display = String.valueOf((long) result);
                } else {
                    display = String.valueOf(result);
                    if (display.length() > 12) {
                        display = display.substring(0, 12);
                    }
                }
                
                // Ajouter à l'historique
                calcStr += display;
                history.addElement(calcStr);
                if (history.size() > 50) {
                    history.removeElementAt(0);
                }
                
                firstNumber = result;
            }
            
            operator = "";
            newNumber = true;
            calculating = false;
            
        } catch (NumberFormatException e) {
            display = "Erreur";
            operator = "";
            newNumber = true;
            calculating = false;
        }
    }
    
    private void clear() {
        display = "0";
        operator = "";
        firstNumber = 0;
        newNumber = true;
        calculating = false;
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == clearCmd) {
            clear();
            repaint();
        } else if (c == historyCmd) {
            showHistory = !showHistory;
            repaint();
        } else if (c == backCmd) {
            running = false;
            if (displayThread != null) {
                try {
                    displayThread.join();
                } catch (InterruptedException e) {}
            }
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
}