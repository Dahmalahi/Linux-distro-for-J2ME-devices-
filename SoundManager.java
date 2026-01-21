import javax.microedition.media.*;

public class SoundManager {
    private static boolean soundEnabled = true;
    
    // Fréquences de notes (en Hz)
    private static final int NOTE_C = 262;
    private static final int NOTE_D = 294;
    private static final int NOTE_E = 330;
    private static final int NOTE_G = 392;
    private static final int NOTE_A = 440;
    
    public static void setEnabled(boolean enabled) {
        soundEnabled = enabled;
    }
    
    public static boolean isEnabled() {
        return soundEnabled;
    }
    
    public static void playBeep() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_A, 100, 100);
        } catch (Exception e) {}
    }
    
    public static void playSuccess() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_C, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_E, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_G, 200, 100);
        } catch (Exception e) {}
    }
    
    public static void playError() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_E, 100, 100);
            Thread.sleep(50);
            Manager.playTone(NOTE_D, 100, 100);
            Thread.sleep(50);
            Manager.playTone(NOTE_C, 200, 100);
        } catch (Exception e) {}
    }
    
    public static void playClick() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_C, 50, 80);
        } catch (Exception e) {}
    }
    
    public static void playNotification() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_G, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_A, 100, 100);
        } catch (Exception e) {}
    }
    
    public static void playStartup() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_C, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_D, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_E, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_G, 200, 100);
        } catch (Exception e) {}
    }
    
    public static void playShutdown() {
        if (!soundEnabled) return;
        try {
            Manager.playTone(NOTE_G, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_E, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_D, 100, 100);
            Thread.sleep(100);
            Manager.playTone(NOTE_C, 200, 100);
        } catch (Exception e) {}
    }
}