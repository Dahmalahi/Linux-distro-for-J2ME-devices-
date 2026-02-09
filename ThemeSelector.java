import javax.microedition.lcdui.*;

/**
 * ThemeSelector.java
 * Interface de sélection des 15 thèmes
 */
public class ThemeSelector extends List implements CommandListener {
    private DiscoOs mainApp;
    private Command selectCmd, backCmd, previewCmd;
    
    public ThemeSelector(DiscoOs app) {
        super("Choisir Theme (15)", List.IMPLICIT);
        this.mainApp = app;
        
        // Ajouter TOUS les 15 thèmes
        append("1. Matrix - Vert classique", null);
        append("2. Hacker - Neon vert", null);
        append("3. Default - Blanc/noir", null);
        append("4. Night Mode - Gris doux", null);
        append("5. Day Mode - Noir/blanc", null);
        append("6. Ocean - Cyan bleu", null);
        append("7. Fire - Rouge/orange", null);
        append("8. Purple Dream - Violet", null);
        append("9. Amber Terminal - Orange retro", null);
        append("10. Dracula - Violet-gris", null);
        append("11. Nord - Bleu scandinave", null);
        append("12. Monokai - Gris chaud", null);
        append("13. Solarized Dark - Bleu-gris", null);
        append("14. Gruvbox - Vintage chaud", null);
        append("15. Tokyo Night - Neon moderne", null);
        
        selectCmd = new Command("Appliquer", Command.OK, 1);
        previewCmd = new Command("Apercu", Command.ITEM, 2);
        backCmd = new Command("Retour", Command.BACK, 3);
        
        addCommand(selectCmd);
        addCommand(previewCmd);
        addCommand(backCmd);
        setCommandListener(this);
        
        // Sélectionner le thème actuel
        setSelectedIndex(ThemeManager.getCurrentTheme(), true);
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == selectCmd || c == List.SELECT_COMMAND) {
            applyTheme();
        } else if (c == previewCmd) {
            showPreview();
        } else if (c == backCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
    
    private void applyTheme() {
        int selected = getSelectedIndex();
        ThemeManager.setTheme(selected);
        
        Alert alert = new Alert("Theme applique");
        alert.setString("Theme: " + ThemeManager.getThemeName() + "\n\n" +
                       ThemeManager.getThemeDescription());
        alert.setTimeout(3000);
        alert.setType(AlertType.CONFIRMATION);
        
        mainApp.refreshMainForm();
        mainApp.getDisplay().setCurrent(alert, mainApp.getMainForm());
    }
    
    private void showPreview() {
        int selected = getSelectedIndex();
        int oldTheme = ThemeManager.getCurrentTheme();
        
        ThemeManager.setTheme(selected);
        String description = ThemeManager.getThemeDescription();
        
        Alert preview = new Alert("Apercu: " + ThemeManager.getThemeName());
        preview.setString(description);
        preview.setTimeout(5000);
        preview.setType(AlertType.INFO);
        
        ThemeManager.setTheme(oldTheme);
        mainApp.getDisplay().setCurrent(preview, this);
    }
}