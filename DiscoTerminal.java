/**
 * DiscoTerminal.java
 * 
 * Classe wrapper pour maintenir la compatibilité avec l'ancien code.
 * Hérite simplement de DiscoTerminalUI qui contient toute l'interface graphique.
 * 
 * Architecture:
 * - DiscoTerminalCore.java : Logique métier (commandes, processCommand, etc.)
 * - DiscoTerminalUI.java : Interface graphique (Canvas, paint, keyPressed, etc.)
 * - DiscoTerminal.java : Wrapper de compatibilité
 */
public class DiscoTerminal extends DiscoTerminalUI {
    
    /**
     * Constructeur principal
     * Délègue simplement à DiscoTerminalUI
     */
    public DiscoTerminal(DiscoOs app, String user) {
        super(app, user);
    }
}