import javax.microedition.lcdui.*;

public class ThemeSelector extends List implements CommandListener {
    private DiscoOs mainApp;
    private Command selectCmd, backCmd;
    
    public ThemeSelector(DiscoOs app) {
        super("Choisir Theme", List.IMPLICIT);
        this.mainApp = app;
        
        append("Matrix (Vert classique)", null);
        append("Hacker (Vert/Rouge)", null);
        append("Default (Vert terminal)", null);
        append("Night Mode (Sombre)", null);
        append("Day Mode (Clair)", null);
        
        selectCmd = new Command("Appliquer", Command.ITEM, 1);
        backCmd = new Command("Retour", Command.BACK, 2);
        
        addCommand(selectCmd);
        addCommand(backCmd);
        setCommandListener(this);
        
        setSelectedIndex(ThemeManager.getCurrentTheme(), true);
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == selectCmd || c == List.SELECT_COMMAND) {
            int selected = getSelectedIndex();
            ThemeManager.setTheme(selected);
            
            Alert alert = new Alert("Theme applique");
            alert.setString("Theme: " + ThemeManager.getThemeName() + "\n\n" +
                          ThemeManager.getThemeDescription());
            alert.setTimeout(3000);
            alert.setType(AlertType.INFO);
            
            mainApp.getDisplay().setCurrent(alert, mainApp.getMainForm());
        } else if (c == backCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
}