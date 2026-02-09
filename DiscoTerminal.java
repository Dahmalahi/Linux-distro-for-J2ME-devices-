/**
 * DiscoTerminal.java
 * Wrapper de compatibilité avec getter pour accès au core
 */
public class DiscoTerminal extends DiscoTerminalUI {
    public DiscoTerminal(DiscoOs app, String user) {
        super(app, user);
        // CORRIGÉ: Supprimé setMainApp(app) - inutile et inexistant
    }
    
    /**
     * Exécuter une commande programmatiquement
     */
    public String executeCommand(String command) {
        return core.executeCommand(command);
    }
    
    /**
     * Accès au noyau du terminal pour intégration système
     */
    public DiscoTerminalCore getCore() {
        return core;
    }
}