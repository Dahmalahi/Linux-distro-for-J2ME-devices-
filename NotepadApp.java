import javax.microedition.lcdui.*;

public class NotepadApp extends Form implements CommandListener {
    private TextField titleField;
    private TextField contentField;
    private DiscoOs mainApp;
    
    private Command saveCmd, loadCmd, clearCmd, backCmd;
    
    public NotepadApp(DiscoOs app) {
        super("Bloc-notes");
        this.mainApp = app;
        
        titleField = new TextField("Nom fichier:", "note.txt", 30, TextField.ANY);
        append(titleField);
        
        contentField = new TextField("Contenu:", "", 500, TextField.ANY);
        append(contentField);
        
        saveCmd = new Command("Sauver", Command.OK, 1);
        loadCmd = new Command("Charger", Command.SCREEN, 2);
        clearCmd = new Command("Effacer", Command.SCREEN, 3);
        backCmd = new Command("Retour", Command.BACK, 4);
        
        addCommand(saveCmd);
        addCommand(loadCmd);
        addCommand(clearCmd);
        addCommand(backCmd);
        setCommandListener(this);
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == saveCmd) {
            saveNote();
        } else if (c == loadCmd) {
            loadNote();
        } else if (c == clearCmd) {
            contentField.setString("");
        } else if (c == backCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
    
    private void saveNote() {
        String filename = titleField.getString();
        String content = contentField.getString();
        
        if (filename.length() == 0) {
            showAlert("Erreur", "Entrez un nom de fichier");
            return;
        }
        
        String result = FileWriterUtil.writeFile(filename, content);
        showAlert("Sauvegarde", result);
    }
    
    private void loadNote() {
        String filename = titleField.getString();
        
        if (filename.length() == 0) {
            showAlert("Erreur", "Entrez un nom de fichier");
            return;
        }
        
        String content = FileReaderUtil.readFile(filename);
        
        if (content.startsWith("Contenu de")) {
            // Extraire juste le contenu
            int idx = content.indexOf(":\n");
            if (idx > 0) {
                content = content.substring(idx + 2);
            }
        }
        
        contentField.setString(content);
        showAlert("Chargement", "Fichier charge");
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title, message, null, AlertType.INFO);
        alert.setTimeout(2000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
}