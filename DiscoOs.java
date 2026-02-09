import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

/**
 * DiscoOs.java v2.5 FINAL
 * Système d'exploitation Linux Mobile complet
 */
public class DiscoOs extends MIDlet implements CommandListener {
    private Display display;
    private Form mainForm;
    private String currentUsername;
    private int currentUid;

    private Command termCmd, exitCmd, aboutCmd, appsCmd, gamesCmd, settingsCmd;

    // Applications
    private DiscoTerminalUI terminal;
    private CalculatorApp calculator;
    private CalculatorScientific calculatorSci;
    private NotepadApp notepad;
    private TaskManagerApp taskManager;
    private FileManager fileManager;
    private TextEditor textEditor;
    private HttpClient httpClient;
    private ContactManager contactManager;
    private ThemeSelector themeSelector;
    private AboutScreen aboutScreen;
    
    // Jeux (6 au total)
    private SnakeGame snakeGame;
    private TetrisGame tetrisGame;
    private PongGame pongGame;
    private SpaceInvadersGame spaceInvadersGame;
    private BreakoutGame breakoutGame;
    private MemoryGame memoryGame;

    // Login
    private LoginScreen loginScreen;

    public DiscoOs() {
        display = Display.getDisplay(this);
    }

    public void startApp() {
        SoundManager.playStartup();
        if (hasSession()) {
            loadSession();
            showMainMenu();
        } else {
            if (loginScreen == null) {
                loginScreen = new LoginScreen(this);
            }
            loginScreen.show();
        }
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {
        SoundManager.playShutdown();
        notifyDestroyed();
    }

    public void setCurrentUser(String username, int uid) {
        this.currentUsername = username;
        this.currentUid = uid;
    }

    public void showMainMenu() {
        if (mainForm == null) {
            mainForm = new Form("DiscoLinux v2.5");

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

        refreshMainForm();
        display.setCurrent(mainForm);
    }

    public void refreshMainForm() {
        mainForm.deleteAll();

        StringItem header = new StringItem(" ", 
             "DISCOLINUX v2.5\n" +
             "Professional Linux Mobile OS\n" +
             "========================\n");
        mainForm.append(header);

        String userLine = "User: " + currentUsername + "@disco-mobile";
        if (currentUid == 0) {
            userLine += " (root)";
        }

        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory() / 1024;
        long total = rt.totalMemory() / 1024;

        StringItem menu = new StringItem(" ", 
             "Menu Principal:\n" +
             "1. Terminal Console\n" +
             "2. Applications (8)\n" +
             "3. Jeux (6)\n" +
             "4. Parametres\n" +
             "5. A propos v2.5\n" +
             "6. Quitter\n\n" +
             userLine + "\n" +
             "Theme: " + ThemeManager.getThemeName() + "\n" +
             "Sons: " + (SoundManager.isEnabled() ? "ON" : "OFF") + "\n" +
             "Memoire: " + free + "K / " + total + "K\n" +
             "Features: Lua+FS, 95+ cmds");
        mainForm.append(menu);
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

    private void launchTerminal() {
        if (terminal == null) {
            terminal = new DiscoTerminalUI(this, currentUsername);
        }
        terminal.show();
    }

    private void showAppsMenu() {
        List appsList = new List("Applications", List.IMPLICIT);
        appsList.append("1. Calculatrice", null);
        appsList.append("2. Calc Scientifique", null);
        appsList.append("3. Bloc-notes", null);
        appsList.append("4. Editeur de texte", null);
        appsList.append("5. Gestionnaire fichiers", null);
        appsList.append("6. Gestionnaire contacts", null);
        appsList.append("7. Client HTTP", null);
        appsList.append("8. Gestionnaire taches", null);
        
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
                    switch(selected) {
                        case 0: launchCalculator(); break;
                        case 1: launchCalculatorScientific(); break;
                        case 2: launchNotepad(); break;
                        case 3: launchTextEditor(); break;
                        case 4: launchFileManager(); break;
                        case 5: launchContactManager(); break;
                        case 6: launchHttpClient(); break;
                        case 7: launchTaskManager(); break;
                    }
                }
            }
        });
        
        display.setCurrent(appsList);
    }

    private void showGamesMenu() {
        List gamesList = new List("Jeux (6)", List.IMPLICIT);
        gamesList.append("1. Snake", null);
        gamesList.append("2. Tetris", null);
        gamesList.append("3. Pong", null);
        gamesList.append("4. Space Invaders", null);
        gamesList.append("5. Breakout", null);
        gamesList.append("6. Memory Game", null);
        
        Command backCmd = new Command("Retour", Command.BACK, 1);
        gamesList.addCommand(backCmd);
        
        final List finalList = gamesList;
        gamesList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    refreshMainForm();
                    display.setCurrent(mainForm);
                } else {
                    SoundManager.playClick();
                    int selected = finalList.getSelectedIndex();
                    switch(selected) {
                        case 0: launchSnakeGame(); break;
                        case 1: launchTetrisGame(); break;
                        case 2: launchPongGame(); break;
                        case 3: launchSpaceInvadersGame(); break;
                        case 4: launchBreakoutGame(); break;
                        case 5: launchMemoryGame(); break;
                    }
                }
            }
        });
        
        display.setCurrent(gamesList);
    }

    private void showSettingsMenu() {
        List settingsList = new List("Parametres", List.IMPLICIT);
        settingsList.append("Themes (15)", null);
        settingsList.append("Sons: " + (SoundManager.isEnabled() ? "ON" : "OFF"), null);
        
        String userLine = "Utilisateur: " + currentUsername;
        if (currentUid == 0) userLine += " (root)";
        settingsList.append(userLine, null);
        
        settingsList.append("Info systeme", null);
        
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
        SoundManager.setEnabled(!SoundManager.isEnabled());
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
        
        final TextField userField = new TextField("Nom: ", currentUsername, 20, TextField.ANY);
        userForm.append(userField);
        
        final Command okCmd = new Command("Sauver", Command.OK, 1);
        final Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        userForm.addCommand(okCmd);
        userForm.addCommand(cancelCmd);
        
        userForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == okCmd) {
                    String newUser = userField.getString().trim();
                    if (newUser.length() > 0) {
                        currentUsername = newUser;
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
        
        String rootStatus = (currentUid == 0) ? " (root)" : "";
        
        Alert info = new Alert("Systeme");
        info.setString("DiscoLinux v2.5\n" +
                       "Kernel: J2ME CLDC 1.1\n" +
                       "MIDP: 2.0\n" +
                       "Theme: " + ThemeManager.getThemeName() + "\n" +
                       "Memoire: " + (free/1024) + "K / " + (total/1024) + "K\n" +
                       "User: " + currentUsername + rootStatus + "\n" +
                       "UID: " + currentUid + "\n" +
                       "Commands: 95+\n" +
                       "Games: 6\n" +
                       "Apps: 8");
        info.setTimeout(Alert.FOREVER);
        info.setType(AlertType.INFO);
        
        display.setCurrent(info, mainForm);
    }

    // ========== APPLICATIONS ==========
    public void launchCalculator() {
        if (calculator == null) {
            calculator = new CalculatorApp(this);
        }
        calculator.show();
    }

    public void launchCalculatorScientific() {
        if (calculatorSci == null) {
            calculatorSci = new CalculatorScientific(this);
        }
        calculatorSci.show();
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

    // ========== JEUX (6) ==========
    public void launchSnakeGame() {
        if (snakeGame == null) {
            snakeGame = new SnakeGame(this);
        }
        snakeGame.show();
    }

    public void launchTetrisGame() {
        if (tetrisGame == null) {
            tetrisGame = new TetrisGame(this);
        }
        tetrisGame.show();
    }

    public void launchPongGame() {
        if (pongGame == null) {
            pongGame = new PongGame(this);
        }
        pongGame.show();
    }

    public void launchSpaceInvadersGame() {
        if (spaceInvadersGame == null) {
            spaceInvadersGame = new SpaceInvadersGame(this);
        }
        spaceInvadersGame.show();
    }

    public void launchBreakoutGame() {
        if (breakoutGame == null) {
            breakoutGame = new BreakoutGame(this);
        }
        breakoutGame.show();
    }

    public void launchMemoryGame() {
        if (memoryGame == null) {
            memoryGame = new MemoryGame(this);
        }
        memoryGame.show();
    }

    // ========== GETTERS ==========
    public Form getMainForm() {
        return mainForm;
    }

    public Display getDisplay() {
        return display;
    }

    public String getCurrentUser() {
        return currentUsername;
    }

    public int getCurrentUid() {
        return currentUid;
    }

    // ========== SESSION ==========
    private boolean hasSession() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore("DiscoSession", false);
            boolean has = rs.getNumRecords() > 0;
            rs.closeRecordStore();
            return has;
        } catch (Exception e) {
            return false;
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception ignore) {}
            }
        }
    }

    private void loadSession() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore("DiscoSession", false);
            if (rs.getNumRecords() > 0) {
                byte[] data = rs.getRecord(1);
                String record = new String(data);
                int colon = record.indexOf(':');
                if (colon > 0) {
                    String username = record.substring(0, colon);
                    int uid = Integer.parseInt(record.substring(colon + 1));
                    setCurrentUser(username, uid);
                }
            }
        } catch (Exception e) {
            // Fail silently
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception ignore) {}
            }
        }
    }
}