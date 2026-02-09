/**
 * T9Input.java
 * Système de saisie T9 multi-tap complet pour J2ME
 * Compatible CLDC 1.1 / MIDP 2.0
 */

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Enumeration;

public class T9Input {
    private StringBuffer buffer = new StringBuffer();
    private String currentDraft = "";
    private int lastKey = -1;
    private int tapCount = 0;
    
    private Timer timer = new Timer();
    private TimerTask timeoutTask;
    private boolean upperCase = false;
    private boolean numericMode = false;
    private static final long TIMEOUT_MS = 700;
    private static final int MAX_HISTORY = 20;
    
    // FIXED: Escaped special characters (", \) and used Unicode for special symbols
    private static final String[] LOWER_MAP = {
        " 0", 
        ".,!?&\"'(-_)=$^*!:;<>\\u00A7/?\\u00B5%\\u00A3\\u00A8+\\u00B0~#{[|`\\\\^@]}\u00A41", 
        "abc", "def", "ghi",
        "jkl", "mno", "pqrs", "tuv", "wxyz"
    };
    
    private Vector history = new Vector();
    private int historyIndex = -1;

    private Vector dictionary = new Vector();
    private Vector matches = new Vector();
    
    public T9Input() {
        System.out.println("T9 Input");
    }

    public void keyPressed(int key) {
        if (key < 0 || key > 9) return;
        System.out.println("Key: " + key);

        if (numericMode) {
            commitCharacter();
            buffer.append(key);
            updateAutoComp();
            return;
        }

        if (key != lastKey && lastKey != -1) {
            commitCharacter();
        }

        if (key == lastKey) {
            tapCount++;
        } else {
            lastKey = key;
            tapCount = 0;
        }

        String options = LOWER_MAP[key];
        char selected = options.charAt(tapCount % options.length());
        if (upperCase) selected = Character.toUpperCase(selected);

        currentDraft = String.valueOf(selected);
        System.out.println("Draft: " + currentDraft);
        resetTimer();
        updateAutoComp(); 
    }

    private synchronized void commitCharacter() {
        if (currentDraft.length() > 0) {
            buffer.append(currentDraft);
            System.out.println("Committed: " + currentDraft);
            currentDraft = "";
            lastKey = -1;
            tapCount = 0;
            updateAutoComp();
        }
    }

    private void resetTimer() {
        if (timeoutTask != null) timeoutTask.cancel();
        timeoutTask = new TimerTask() {
            public void run() { commitCharacter(); }
        };
        timer.schedule(timeoutTask, TIMEOUT_MS);
    }

    public String getText() {
        return buffer.toString() + currentDraft;
    }

    public String getLiveText() {
        return getText();
    }

    public void backspace() {
        if (currentDraft.length() > 0) {
            currentDraft = "";
            lastKey = -1;
        } else if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        System.out.println("Backspace. Current: " + getText());
        updateAutoComp();
    }
    
    public void clear() {
        buffer.setLength(0);
        currentDraft = "";
        lastKey = -1;
        matches.removeAllElements();
        System.out.println("T9 Buffer Cleared");
    }

    public void setText(String text) {
        flush();
        buffer = new StringBuffer(text);
        lastKey = -1;
        updateAutoComp();
    }

    public void addToHistory(String text) {
        if (text == null || text.trim().length() == 0) return;
        history.removeElement(text);
        history.addElement(text);
        if (history.size() > MAX_HISTORY) history.removeElementAt(0);
        historyIndex = history.size();
        System.out.println("History Saved: " + text);
    }

    public Vector getHistory() { return history; }

    public void toggleCase() { flush(); upperCase = !upperCase; }
    public boolean isUpperCase() { return upperCase; }
    public void toggleNumericMode() { flush(); numericMode = !numericMode; }
    public boolean isNumericMode() { return numericMode; }
    public void flush() { commitCharacter(); }

    public boolean checkTimeout() {
        return currentDraft.length() > 0;
    }

    public void updateAutoComp() {
        String current = getText().toLowerCase();
        matches.removeAllElements();
        if (current.length() == 0) return;
        System.out.println("Searching matches for: " + current);
    }

    public Vector getMatches() { return matches; }
}