import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class DiscoOs extends MIDlet implements CommandListener {
    private Display display;
    private Form mainForm;
    private String currentUser = "root";
    
    private Command termCmd, exitCmd, aboutCmd, appsCmd, gamesCmd, settingsCmd;
    
    // Changement ici: DiscoTerminal devient DiscoTerminalUI
    private DiscoTerminalUI terminal;
    
    private CalculatorApp calculator;
    private NotepadApp notepad;
    private TaskManagerApp taskManager;
    private SnakeGame snakeGame;
    private TetrisGame tetrisGame;
    private FileManager fileManager;
    private TextEditor textEditor;
    private HttpClient httpClient;
    private ContactManager contactManager;
    private ThemeSelector themeSelector;
    private AboutScreen aboutScreen;
    
    public DiscoOs() {
        // Formulaire principal
        mainForm = new Form("DiscoOS v2.0");
        
        // En-tête
        StringItem header = new StringItem("", 
            "DISCO OS v2.0\n" +
            "Linux-style Mobile OS\n" +
            "-------------------\n");
        mainForm.append(header);
        
        // Menu
        StringItem menu = new StringItem("", 
            "Menu Principal:\n" +
            "1. Terminal Console\n" +
            "2. Applications\n" +
            "3. Jeux\n" +
            "4. Parametres\n" +
            "5. A propos\n" +
            "6. Quitter\n\n" +
            "User: " + currentUser + "@disco-mobile\n" +
            "Theme: " + ThemeManager.getThemeName());
        mainForm.append(menu);
        
        // Commandes
        termCmd = new Command("Terminal", Command.SCREEN, 1);
        appsCmd = new Command("Apps", Command.SCREEN, 2);
        gamesCmd = new Command("Jeux", Command.SCREEN, 3);
        settingsCmd = new Command("Parametres", Command.SCREEN, 4);
        aboutCmd = new Command("A propos", Command.HELP, 5);
        exitCmd = new Command("Quitter", Command.EXIT, 6);
        
        mainForm.addCommand(termCmd);
        mainForm.addCommand(appsCmd);
        mainForm.addCommand(gamesCmd);
        mainForm.addCommand(settingsCmd);
        mainForm.addCommand(aboutCmd);
        mainForm.addCommand(exitCmd);
        mainForm.setCommandListener(this);
    }
    
    public void startApp() {
        display = Display.getDisplay(this);
        
        // Jouer son de démarrage
        SoundManager.playStartup();
        
        display.setCurrent(mainForm);
    }
    
    public void pauseApp() {}
    
    public void destroyApp(boolean unconditional) {
        SoundManager.playShutdown();
        notifyDestroyed();
    }
    
    public void commandAction(Command c, Displayable d) {
        SoundManager.playClick();
        
        if (c == termCmd) {
            launchTerminal();
        } else if (c == appsCmd) {
            showAppsMenu();
        } else if (c == gamesCmd) {
            showGamesMenu();
        } else if (c == settingsCmd) {
            showSettingsMenu();
        } else if (c == aboutCmd) {
            launchAboutScreen();
        } else if (c == exitCmd) {
            destroyApp(false);
        }
    }
    
    // Modification ici: Utilise DiscoTerminalUI
    private void launchTerminal() {
        if (terminal == null) {
            terminal = new DiscoTerminalUI(this, currentUser);
        }
        terminal.show();
    }
    
    private void showAppsMenu() {
        List appsList = new List("Applications", List.IMPLICIT);
        appsList.append("Calculatrice", null);
        appsList.append("Bloc-notes", null);
        appsList.append("Editeur de texte", null);
        appsList.append("Gestionnaire de fichiers", null);
        appsList.append("Gestionnaire de contacts", null);
        appsList.append("Client HTTP", null);
        appsList.append("Gestionnaire de taches", null);
        
        Command backCmd = new Command("Retour", Command.BACK, 1);
        appsList.addCommand(backCmd);
        
        final List finalList = appsList;
        appsList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    refreshMainForm();
                    display.setCurrent(mainForm);
                } else {
                    SoundManager.playClick();
                    int selected = finalList.getSelectedIndex();
                    if (selected == 0) {
                        launchCalculator();
                    } else if (selected == 1) {
                        launchNotepad();
                    } else if (selected == 2) {
                        launchTextEditor();
                    } else if (selected == 3) {
                        launchFileManager();
                    } else if (selected == 4) {
                        launchContactManager();
                    } else if (selected == 5) {
                        launchHttpClient();
                    } else if (selected == 6) {
                        launchTaskManager();
                    }
                }
            }
        });
        
        display.setCurrent(appsList);
    }
    
    private void showGamesMenu() {
        List gamesList = new List("Jeux", List.IMPLICIT);
        gamesList.append("Snake", null);
        gamesList.append("Tetris", null);
        gamesList.append("Pong (bientot)", null);
        gamesList.append("Space Invaders (bientot)", null);
        
        Command backCmd = new Command("Retour", Command.BACK, 1);
        gamesList.addCommand(backCmd);
        
        final List finalList = gamesList;
        gamesList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    display.setCurrent(mainForm);
                } else {
                    SoundManager.playClick();
                    int selected = finalList.getSelectedIndex();
                    if (selected == 0) {
                        launchSnakeGame();
                    } else if (selected == 1) {
                        launchTetris();
                    }
                }
            }
        });
        
        display.setCurrent(gamesList);
    }
    
    private void showSettingsMenu() {
        List settingsList = new List("Parametres", List.IMPLICIT);
        settingsList.append("Themes", null);
        settingsList.append("Sons: " + (SoundManager.isEnabled() ? "ON" : "OFF"), null);
        settingsList.append("Utilisateur: " + currentUser, null);
        settingsList.append("A propos du systeme", null);
        
        Command backCmd = new Command("Retour", Command.BACK, 1);
        settingsList.addCommand(backCmd);
        
        final List finalList = settingsList;
        settingsList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    refreshMainForm();
                    display.setCurrent(mainForm);
                } else {
                    SoundManager.playClick();
                    int selected = finalList.getSelectedIndex();
                    if (selected == 0) {
                        launchThemeSelector();
                    } else if (selected == 1) {
                        toggleSound();
                        refreshMainForm();
                        display.setCurrent(mainForm);
                    } else if (selected == 2) {
                        showUserSettings();
                    } else if (selected == 3) {
                        showSystemInfo();
                    }
                }
            }
        });
        
        display.setCurrent(settingsList);
    }
    
    private void toggleSound() {
        boolean current = SoundManager.isEnabled();
        SoundManager.setEnabled(!current);
        
        if (SoundManager.isEnabled()) {
            SoundManager.playSuccess();
        }
        
        Alert alert = new Alert("Sons");
        alert.setString("Sons systeme: " + (SoundManager.isEnabled() ? "ACTIVES" : "DESACTIVES"));
        alert.setTimeout(2000);
        alert.setType(AlertType.INFO);
        display.setCurrent(alert, mainForm);
    }
    
    private void showUserSettings() {
        Form userForm = new Form("Utilisateur");
        
        final TextField userField = new TextField("Nom d'utilisateur:", currentUser, 20, TextField.ANY);
        userForm.append(userField);
        
        Command okCmd = new Command("Sauver", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        userForm.addCommand(okCmd);
        userForm.addCommand(cancelCmd);
        
        userForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String newUser = userField.getString();
                    if (newUser.length() > 0) {
                        currentUser = newUser;
                        SoundManager.playSuccess();
                        refreshMainForm();
                    }
                }
                display.setCurrent(mainForm);
            }
        });
        
        display.setCurrent(userForm);
    }
    
    private void showSystemInfo() {
        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        
        Alert info = new Alert("Systeme");
        info.setString("DiscoOS v2.0\n" +
                      "Kernel: J2ME CLDC 1.1\n" +
                      "MIDP: 2.0\n" +
                      "Theme: " + ThemeManager.getThemeName() + "\n" +
                      "Memoire libre: " + (free/1024) + "KB\n" +
                      "Memoire totale: " + (total/1024) + "KB\n" +
                      "Utilisateur: " + currentUser);
        info.setTimeout(Alert.FOREVER);
        info.setType(AlertType.INFO);
        
        display.setCurrent(info, mainForm);
    }
    
    private void refreshMainForm() {
        // Recréer le menu principal avec les infos à jour
        mainForm.deleteAll();
        
        StringItem header = new StringItem("", 
            "DISCO OS v2.0\n" +
            "Linux-style Mobile OS\n" +
            "-------------------\n");
        mainForm.append(header);
        
        StringItem menu = new StringItem("", 
            "Menu Principal:\n" +
            "1. Terminal Console\n" +
            "2. Applications\n" +
            "3. Jeux\n" +
            "4. Parametres\n" +
            "5. A propos\n" +
            "6. Quitter\n\n" +
            "User: " + currentUser + "@disco-mobile\n" +
            "Theme: " + ThemeManager.getThemeName() + "\n" +
            "Sons: " + (SoundManager.isEnabled() ? "ON" : "OFF"));
        mainForm.append(menu);
    }
    
    public void launchCalculator() {
        if (calculator == null) {
            calculator = new CalculatorApp(this);
        }
        calculator.show();
    }
    
    public void launchNotepad() {
        if (notepad == null) {
            notepad = new NotepadApp(this);
        }
        notepad.show();
    }
    
    public void launchTaskManager() {
        if (taskManager == null) {
            taskManager = new TaskManagerApp(this);
        }
        taskManager.show();
    }
    
    public void launchSnakeGame() {
        if (snakeGame == null) {
            snakeGame = new SnakeGame(this);
        }
        snakeGame.show();
    }
    
    public void launchTetris() {
        if (tetrisGame == null) {
            tetrisGame = new TetrisGame(this);
        }
        tetrisGame.show();
    }
    
    public void launchFileManager() {
        if (fileManager == null) {
            fileManager = new FileManager(this);
        }
        fileManager.show();
    }
    
    public void launchTextEditor() {
        if (textEditor == null) {
            textEditor = new TextEditor(this);
        }
        textEditor.show();
    }
    
    public void launchHttpClient() {
        if (httpClient == null) {
            httpClient = new HttpClient(this);
        }
        httpClient.show();
    }
    
    public void launchContactManager() {
        if (contactManager == null) {
            contactManager = new ContactManager(this);
        }
        contactManager.show();
    }
    
    public void launchThemeSelector() {
        if (themeSelector == null) {
            themeSelector = new ThemeSelector(this);
        }
        themeSelector.show();
    }
    
    public void launchAboutScreen() {
        if (aboutScreen == null) {
            aboutScreen = new AboutScreen(this);
        }
        aboutScreen.show();
    }
    
    public Form getMainForm() {
        return mainForm;
    }
    
    public Display getDisplay() {
        return display;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
}