import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

/**
 * LoginScreen.java
 * 
 * Écran de connexion avec:
 * - Création de compte au premier démarrage
 * - Authentification username/password
 * - Support root et utilisateurs normaux
 * - Stockage sécurisé dans RMS
 */
public class LoginScreen extends Form implements CommandListener {
    private DiscoOs mainApp;
    private Display display;
    
    // Champs de saisie
    private TextField usernameField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    
    // Commandes
    private Command loginCmd;
    private Command createCmd;
    private Command cancelCmd;
    
    // Mode
    private boolean isFirstRun = false;
    private boolean isCreateMode = false;
    
    // RMS
    private static final String USERS_STORE = "DiscoUsers";
    private static final String SESSION_STORE = "DiscoSession";
    
    /**
     * Constructeur
     */
    public LoginScreen(DiscoOs app) {
        super("DiscoLinux Login");
        this.mainApp = app;
        this.display = app.getDisplay();
        
        // Vérifier si c'est le premier démarrage
        checkFirstRun();
        
        if (isFirstRun) {
            showCreateAccountScreen();
        } else {
            showLoginScreen();
        }
    }
    
    /**
     * Vérifie si c'est le premier démarrage
     */
    private void checkFirstRun() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(USERS_STORE, false);
            isFirstRun = (rs.getNumRecords() == 0);
            rs.closeRecordStore();
        } catch (RecordStoreNotFoundException e) {
            isFirstRun = true;
        } catch (Exception e) {
            isFirstRun = true;
        }
    }
    
    /**
     * Affiche l'écran de création de compte
     */
    private void showCreateAccountScreen() {
        isCreateMode = true;
        deleteAll();
        
        // Message de bienvenue
        StringItem welcome = new StringItem("", 
            "=== FIRST RUN ===\n\n" +
            "Welcome to DiscoLinux!\n\n" +
            "Please create your\nadmin account:\n\n");
        welcome.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        append(welcome);
        
        // Champs
        usernameField = new TextField("Username:", "", 20, TextField.ANY);
        passwordField = new TextField("Password:", "", 30, TextField.PASSWORD);
        confirmPasswordField = new TextField("Confirm:", "", 30, TextField.PASSWORD);
        
        append(usernameField);
        append(passwordField);
        append(confirmPasswordField);
        
        // Info
        StringItem info = new StringItem("", 
            "\nNote: First user\nwill have root\nprivileges (UID 0)\n");
        info.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_SMALL));
        append(info);
        
        // Commandes
        createCmd = new Command("Create", Command.OK, 1);
        cancelCmd = new Command("Exit", Command.EXIT, 2);
        
        addCommand(createCmd);
        addCommand(cancelCmd);
        setCommandListener(this);
    }
    
    /**
     * Affiche l'écran de connexion
     */
    private void showLoginScreen() {
        isCreateMode = false;
        deleteAll();
        
        // Logo ASCII
        StringItem logo = new StringItem("",
            "=================\n" +
            "|  DISCOLINUX   |\n" +
            "|   J2ME v2.5   |\n" +
            "=================\n\n");
        logo.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL));
        append(logo);
        
        // Champs
        usernameField = new TextField("Username:", "", 20, TextField.ANY);
        passwordField = new TextField("Password:", "", 30, TextField.PASSWORD);
        
        append(usernameField);
        append(passwordField);
        
        // Info
        StringItem info = new StringItem("", 
            "\nEnter credentials\nto continue\n");
        info.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_SMALL));
        append(info);
        
        // Commandes
        loginCmd = new Command("Login", Command.OK, 1);
        cancelCmd = new Command("Exit", Command.EXIT, 2);
        
        addCommand(loginCmd);
        addCommand(cancelCmd);
        setCommandListener(this);
    }
    
    /**
     * Gestion des commandes
     */
    public void commandAction(Command c, Displayable d) {
        if (c == createCmd) {
            handleCreateAccount();
        } else if (c == loginCmd) {
            handleLogin();
        } else if (c == cancelCmd) {
            mainApp.notifyDestroyed();
        }
    }
    
    /**
     * Création de compte
     */
    private void handleCreateAccount() {
        String username = usernameField.getString().trim();
        String password = passwordField.getString();
        String confirm = confirmPasswordField.getString();
        
        // Validation
        if (username.length() < 3) {
            showAlert("Error", "Username must be\nat least 3 chars");
            return;
        }
        
        if (password.length() < 4) {
            showAlert("Error", "Password must be\nat least 4 chars");
            return;
        }
        
        if (!password.equals(confirm)) {
            showAlert("Error", "Passwords don't\nmatch!");
            return;
        }
        
        // Créer l'utilisateur
        if (createUser(username, password, 0)) {
            mainApp.setCurrentUser(username, 0);
            storeSession(username, 0);
            showAlert("Success", "Compte admin créé !\nConnexion automatique...");
            mainApp.showMainMenu();
        } else {
            showAlert("Error", "Failed to create\naccount!");
        }
    }
    
    /**
     * Connexion
     */
    private void handleLogin() {
        String username = usernameField.getString().trim();
        String password = passwordField.getString();
        
        if (username.length() == 0 || password.length() == 0) {
            showAlert("Error", "Please enter\ncredentials");
            return;
        }
        
        // Vérifier les credentials
        int uid = authenticateUser(username, password);
        
        if (uid >= 0) {
            // Connexion réussie
            mainApp.setCurrentUser(username, uid);
            storeSession(username, uid);
            mainApp.showMainMenu();
        } else {
            showAlert("Error", "Invalid username\nor password!");
            passwordField.setString("");
        }
    }
    
    /**
     * Crée un utilisateur dans RMS
     */
    private boolean createUser(String username, String password, int uid) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(USERS_STORE, true);
            
            // Format: username:passwordHash:uid
            String hash = simpleHash(password);
            String record = username + ":" + hash + ":" + uid;
            
            byte[] data = record.getBytes();
            rs.addRecord(data, 0, data.length);
            
            rs.closeRecordStore();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Authentifie un utilisateur
     * @return UID si succès, -1 si échec
     */
    private int authenticateUser(String username, String password) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(USERS_STORE, false);
            
            String hash = simpleHash(password);
            
            // Parcourir tous les enregistrements
            for (int i = 1; i <= rs.getNumRecords(); i++) {
                try {
                    byte[] data = rs.getRecord(i);
                    String record = new String(data);
                    
                    // Format: username:hash:uid
                    int firstColon = record.indexOf(':');
                    int secondColon = record.indexOf(':', firstColon + 1);
                    
                    if (firstColon > 0 && secondColon > 0) {
                        String storedUser = record.substring(0, firstColon);
                        String storedHash = record.substring(firstColon + 1, secondColon);
                        String storedUid = record.substring(secondColon + 1);
                        
                        if (storedUser.equals(username) && storedHash.equals(hash)) {
                            rs.closeRecordStore();
                            return Integer.parseInt(storedUid);
                        }
                    }
                } catch (Exception e) {
                    // Enregistrement corrompu, continuer
                }
            }
            
            rs.closeRecordStore();
            return -1;
            
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Stocke la session
     */
    private boolean storeSession(String username, int uid) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(SESSION_STORE, true);
            
            String record = username + ":" + uid;
            byte[] data = record.getBytes();
            
            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, data, 0, data.length);
            } else {
                rs.addRecord(data, 0, data.length);
            }
            
            rs.closeRecordStore();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Hash simple (pour CLDC 1.1)
     */
    private String simpleHash(String input) {
        int hash = 0;
        for (int i = 0; i < input.length(); i++) {
            hash = 31 * hash + input.charAt(i);
        }
        return Integer.toString(hash);
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title, message, null, AlertType.INFO);
        alert.setTimeout(3000);
        display.setCurrent(alert, this);
    }
    
    /**
     * Affiche l'écran
     */
    public void show() {
        display.setCurrent(this);
    }
}