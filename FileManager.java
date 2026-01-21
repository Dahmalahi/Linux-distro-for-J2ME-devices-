import javax.microedition.lcdui.*;
import java.util.Vector;

public class FileManager extends List implements CommandListener {
    private DiscoOs mainApp;
    private String currentPath;
    private Vector fileList;
    private Vector pathHistory;
    
    private Command openCmd, backCmd, deleteCmd, createCmd, exitCmd, copyCmd, pasteCmd;
    private String clipboardPath = null;
    private String clipboardName = null;
    
    // Simulation de système de fichiers hiérarchique
    private FileNode rootNode;
    
    public FileManager(DiscoOs app) {
        super("Fichiers: /", List.IMPLICIT);
        this.mainApp = app;
        this.currentPath = "/";
        this.fileList = new Vector();
        this.pathHistory = new Vector();
        
        initFileSystem();
        loadDirectory();
        
        openCmd = new Command("Ouvrir", Command.ITEM, 1);
        createCmd = new Command("Creer", Command.SCREEN, 2);
        copyCmd = new Command("Copier", Command.SCREEN, 3);
        pasteCmd = new Command("Coller", Command.SCREEN, 4);
        deleteCmd = new Command("Supprimer", Command.SCREEN, 5);
        backCmd = new Command("Parent", Command.SCREEN, 6);
        exitCmd = new Command("Quitter", Command.BACK, 7);
        
        addCommand(openCmd);
        addCommand(createCmd);
        addCommand(copyCmd);
        addCommand(pasteCmd);
        addCommand(deleteCmd);
        addCommand(backCmd);
        addCommand(exitCmd);
        setCommandListener(this);
    }
    
    private void initFileSystem() {
        rootNode = new FileNode("/", true);
        
        // Créer structure de base
        FileNode system = new FileNode("system", true);
        system.addChild(new FileNode("kernel", false));
        system.addChild(new FileNode("init", false));
        rootNode.addChild(system);
        
        FileNode apps = new FileNode("apps", true);
        apps.addChild(new FileNode("calculator.jar", false));
        apps.addChild(new FileNode("snake.jar", false));
        rootNode.addChild(apps);
        
        FileNode data = new FileNode("data", true);
        FileNode docs = new FileNode("documents", true);
        docs.addChild(new FileNode("readme.txt", false));
        docs.addChild(new FileNode("notes.txt", false));
        data.addChild(docs);
        data.addChild(new FileNode("config.ini", false));
        rootNode.addChild(data);
        
        FileNode home = new FileNode("home", true);
        FileNode userDir = new FileNode("root", true);
        userDir.addChild(new FileNode(".bashrc", false));
        userDir.addChild(new FileNode("todo.txt", false));
        home.addChild(userDir);
        rootNode.addChild(home);
        
        FileNode tfcard = new FileNode("TFCard", true);
        tfcard.addChild(new FileNode("music", true));
        tfcard.addChild(new FileNode("photos", true));
        rootNode.addChild(tfcard);
    }
    
    private FileNode getCurrentNode() {
        if (currentPath.equals("/")) {
            return rootNode;
        }
        
        String[] parts = split(currentPath, '/');
        FileNode node = rootNode;
        
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                node = node.getChild(parts[i]);
                if (node == null) return rootNode;
            }
        }
        
        return node;
    }
    
    private void loadDirectory() {
        deleteAll();
        fileList.removeAllElements();
        
        setTitle("Fichiers: " + currentPath);
        
        FileNode node = getCurrentNode();
        if (node == null) return;
        
        Vector children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            FileNode child = (FileNode) children.elementAt(i);
            String displayName = child.isDirectory ? "[DIR] " + child.name : child.name;
            append(displayName, null);
            fileList.addElement(child);
        }
        
        if (fileList.size() == 0) {
            append("(vide)", null);
        }
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == openCmd || c == List.SELECT_COMMAND) {
            openSelected();
        } else if (c == createCmd) {
            showCreateDialog();
        } else if (c == copyCmd) {
            copySelected();
        } else if (c == pasteCmd) {
            pasteItem();
        } else if (c == deleteCmd) {
            deleteSelected();
        } else if (c == backCmd) {
            goToParent();
        } else if (c == exitCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
    
    private void openSelected() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        FileNode node = (FileNode) fileList.elementAt(idx);
        
        if (node.isDirectory) {
            pathHistory.addElement(currentPath);
            if (currentPath.endsWith("/")) {
                currentPath = currentPath + node.name;
            } else {
                currentPath = currentPath + "/" + node.name;
            }
            loadDirectory();
        } else {
            viewFile(node.name);
        }
    }
    
    private void viewFile(String filename) {
        String content = FileReaderUtil.readFile(filename);
        
        Alert fileView = new Alert("Fichier: " + filename);
        fileView.setString(content);
        fileView.setTimeout(Alert.FOREVER);
        fileView.setType(AlertType.INFO);
        
        mainApp.getDisplay().setCurrent(fileView, this);
    }
    
    private void showCreateDialog() {
        Form createForm = new Form("Creer");
        
        final ChoiceGroup typeChoice = new ChoiceGroup("Type:", Choice.EXCLUSIVE);
        typeChoice.append("Fichier", null);
        typeChoice.append("Dossier", null);
        createForm.append(typeChoice);
        
        final TextField nameField = new TextField("Nom:", "", 30, TextField.ANY);
        createForm.append(nameField);
        
        Command okCmd = new Command("OK", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        createForm.addCommand(okCmd);
        createForm.addCommand(cancelCmd);
        
        createForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String name = nameField.getString();
                    if (name.length() > 0) {
                        boolean isDir = typeChoice.getSelectedIndex() == 1;
                        createItem(name, isDir);
                    }
                }
                mainApp.getDisplay().setCurrent(FileManager.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(createForm);
    }
    
    private void createItem(String name, boolean isDir) {
        FileNode currentNode = getCurrentNode();
        FileNode newNode = new FileNode(name, isDir);
        currentNode.addChild(newNode);
        
        loadDirectory();
        showAlert("Cree: " + name);
    }
    
    private void copySelected() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        FileNode node = (FileNode) fileList.elementAt(idx);
        clipboardPath = currentPath;
        clipboardName = node.name;
        
        showAlert("Copie: " + node.name);
    }
    
    private void pasteItem() {
        if (clipboardName == null) {
            showAlert("Presse-papiers vide");
            return;
        }
        
        FileNode currentNode = getCurrentNode();
        FileNode newNode = new FileNode(clipboardName + "_copy", false);
        currentNode.addChild(newNode);
        
        loadDirectory();
        showAlert("Colle: " + clipboardName);
    }
    
    private void deleteSelected() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        FileNode node = (FileNode) fileList.elementAt(idx);
        FileNode currentNode = getCurrentNode();
        currentNode.removeChild(node);
        
        loadDirectory();
        showAlert("Supprime: " + node.name);
    }
    
    private void goToParent() {
        if (currentPath.equals("/")) {
            showAlert("Deja a la racine");
            return;
        }
        
        int lastSlash = currentPath.lastIndexOf('/');
        if (lastSlash > 0) {
            currentPath = currentPath.substring(0, lastSlash);
        } else {
            currentPath = "/";
        }
        
        loadDirectory();
    }
    
    private String[] split(String s, char delimiter) {
        Vector v = new Vector();
        StringBuffer word = new StringBuffer();
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == delimiter) {
                if (word.length() > 0) {
                    v.addElement(word.toString());
                    word = new StringBuffer();
                }
            } else {
                word.append(c);
            }
        }
        
        if (word.length() > 0) {
            v.addElement(word.toString());
        }
        
        String[] result = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            result[i] = (String) v.elementAt(i);
        }
        
        return result;
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert("Info", message, null, AlertType.INFO);
        alert.setTimeout(2000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
}

// Classe FileNode pour la structure d'arborescence
class FileNode {
    String name;
    boolean isDirectory;
    Vector children;
    
    public FileNode(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.children = new Vector();
    }
    
    public void addChild(FileNode child) {
        children.addElement(child);
    }
    
    public void removeChild(FileNode child) {
        children.removeElement(child);
    }
    
    public FileNode getChild(String name) {
        for (int i = 0; i < children.size(); i++) {
            FileNode node = (FileNode) children.elementAt(i);
            if (node.name.equals(name)) {
                return node;
            }
        }
        return null;
    }
    
    public Vector getChildren() {
        return children;
    }
}