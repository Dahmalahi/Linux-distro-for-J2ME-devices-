import javax.microedition.lcdui.*;

public class TextEditor extends Form implements CommandListener {
    private DiscoOs mainApp;
    private TextField fileNameField;
    private TextField contentField;
    private StringItem statusItem;
    private Command saveCmd, loadCmd, clearCmd, searchCmd, statsCmd, backCmd;
    private String currentFileName = "";

    public TextEditor(DiscoOs app) {
        super("Editeur de Texte");
        this.mainApp = app;
        
        fileNameField = new TextField("Fichier: ", "document.txt", 30, TextField.ANY);
        append(fileNameField);
        
        contentField = new TextField("Contenu: ", "", 2000, TextField.ANY);
        append(contentField);
        
        statusItem = new StringItem("", "Pret");
        append(statusItem);
        
        saveCmd = new Command("Sauver", Command.OK, 1);
        loadCmd = new Command("Charger", Command.SCREEN, 2);
        searchCmd = new Command("Chercher", Command.SCREEN, 3);
        statsCmd = new Command("Stats", Command.SCREEN, 4);
        clearCmd = new Command("Effacer", Command.SCREEN, 5);
        backCmd = new Command("Quitter", Command.BACK, 6);
        
        addCommand(saveCmd);
        addCommand(loadCmd);
        addCommand(searchCmd);
        addCommand(statsCmd);
        addCommand(clearCmd);
        addCommand(backCmd);
        setCommandListener(this);
    }

    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == saveCmd) {
            saveFile();
        } else if (c == loadCmd) {
            loadFile();
        } else if (c == searchCmd) {
            showSearchDialog();
        } else if (c == statsCmd) {
            showStats();
        } else if (c == clearCmd) {
            contentField.setString("");
            updateStatus("Contenu efface");
        } else if (c == backCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }

    private void saveFile() {
        String filename = fileNameField.getString();
        String content = contentField.getString();
        
        if (filename.length() == 0) {
            updateStatus("Erreur: Nom fichier vide");
            return;
        }
        
        String result = FileWriterUtil.writeFile(filename, content);
        currentFileName = filename;
        updateStatus("Sauvegarde: " + filename);
        
        showAlert("Sauvegarde", result);
    }

    private void loadFile() {
        String filename = fileNameField.getString();
        
        if (filename.length() == 0) {
            updateStatus("Erreur: Nom fichier vide");
            return;
        }
        
        String content = FileReaderUtil.readFile(filename);
        
        if (content.startsWith("Fichier non trouve") || content.startsWith("Erreur")) {
            updateStatus("Fichier non trouve");
            showAlert("Erreur", content);
            return;
        }
        
        contentField.setString(content);
        currentFileName = filename;
        updateStatus("Charge: " + filename);
        
        showAlert("Chargement", "Fichier charge: " + filename);
    }

    private void showSearchDialog() {
        Form searchForm = new Form("Rechercher");
        
        final TextField searchField = new TextField("Rechercher: ", "", 30, TextField.ANY);
        searchForm.append(searchField);
        
        final TextField replaceField = new TextField("Remplacer par: ", "", 30, TextField.ANY);
        searchForm.append(replaceField);
        
        final Command findCmd = new Command("Trouver", Command.OK, 1);
        final Command replaceCmd = new Command("Remplacer", Command.SCREEN, 2);
        final Command cancelCmd = new Command("Annuler", Command.CANCEL, 3);
        
        searchForm.addCommand(findCmd);
        searchForm.addCommand(replaceCmd);
        searchForm.addCommand(cancelCmd);
        
        searchForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                String searchText = searchField.getString();
                String replaceText = replaceField.getString();
                
                if (c == findCmd) {
                    findText(searchText);
                } else if (c == replaceCmd) {
                    replaceText(searchText, replaceText);
                }
                
                mainApp.getDisplay().setCurrent(TextEditor.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(searchForm);
    }

    private void findText(String search) {
        if (search.length() == 0) {
            updateStatus("Texte recherche vide");
            return;
        }
        
        String content = contentField.getString();
        int index = content.indexOf(search);
        
        if (index >= 0) {
            updateStatus("Trouve a position: " + index);
            showAlert("Recherche", "Texte trouve a position " + index);
        } else {
            updateStatus("Texte non trouve");
            showAlert("Recherche", "Texte non trouve");
        }
    }

    private void replaceText(String search, String replace) {
        if (search.length() == 0) {
            updateStatus("Texte recherche vide");
            return;
        }
        
        String content = contentField.getString();
        String newContent = replaceAll(content, search, replace);
        
        int count = countOccurrences(content, search);
        
        if (count > 0) {
            contentField.setString(newContent);
            updateStatus("Remplace " + count + " occurrence(s)");
            showAlert("Remplacement", count + " remplacement(s) effectue(s)");
        } else {
            updateStatus("Aucun remplacement");
            showAlert("Remplacement", "Texte non trouve");
        }
    }

    private String replaceAll(String text, String search, String replace) {
        StringBuffer result = new StringBuffer();
        int start = 0;
        int index;
        
        while ((index = text.indexOf(search, start)) >= 0) {
            result.append(text.substring(start, index));
            result.append(replace);
            start = index + search.length();
        }
        result.append(text.substring(start));
        
        return result.toString();
    }

    private int countOccurrences(String text, String search) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(search, index)) >= 0) {
            count++;
            index += search.length();
        }
        
        return count;
    }

    private void showStats() {
        String content = contentField.getString();
        
        int chars = content.length();
        int words = countWords(content);
        int lines = countLines(content);
        
        String stats = "Statistiques:\n" +
                      "Caracteres: " + chars + "\n" +
                      "Mots: " + words + "\n" +
                      "Lignes: " + lines;
        
        updateStatus("Stats affichees");
        showAlert("Statistiques", stats);
    }

    private int countWords(String text) {
        if (text.length() == 0) return 0;
        
        int count = 0;
        boolean inWord = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\n' || c == '\t') {
                inWord = false;
            } else {
                if (!inWord) {
                    count++;
                    inWord = true;
                }
            }
        }
        
        return count;
    }

    private int countLines(String text) {
        if (text.length() == 0) return 0;
        
        int count = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private void updateStatus(String message) {
        statusItem.setText(message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title, message, null, AlertType.INFO);
        alert.setTimeout(3000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
}