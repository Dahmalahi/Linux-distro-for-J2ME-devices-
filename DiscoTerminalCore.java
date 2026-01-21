import java.util.Vector;

public class DiscoTerminalCore {
    private DiscoOs mainApp;
    private String currentUser;
    private String hostname = "disco-mobile";
    
    private Vector screenLines;
    
    public DiscoTerminalCore(DiscoOs app, String user) {
        this.mainApp = app;
        this.currentUser = user;
        this.screenLines = new Vector();
        
        // Messages de bienvenue
        addLine("DiscoLinux 2.0 (tty1)");
        addLine("");
        addLine(currentUser + " login: " + currentUser);
        addLine("Welcome to DiscoLinux!");
        addLine("");
        addLine(getPrompt());
    }
    
    public String getPrompt() {
        return currentUser + "@" + hostname + ":~$ ";
    }
    
    public void addLine(String line) {
        screenLines.addElement(line);
        if (screenLines.size() > 100) {
            screenLines.removeElementAt(0);
        }
    }
    
    public Vector getScreenLines() {
        return screenLines;
    }
    
    public void clearScreen() {
        screenLines.removeAllElements();
        addLine(getPrompt());
    }
    
    public String processCommand(String cmd) {
        String c = cmd.toLowerCase();
        String[] parts = splitCommand(cmd);

        // MAN PAGES
        if (c.startsWith("man ")) {
            if (parts.length < 2) {
                return "Usage: man <commande>";
            }
            return getManPage(parts[1]);
        }
        
        if (c.equals("help") || c.equals("man")) {
            return "Commandes disponibles:\n" +
                   "Tapez 'man <cmd>' pour aide\n" +
                   "---\n" +
                   "ls cat write rm mkdir\n" +
                   "calc note edit files\n" +
                   "http snake tetris tasks\n" +
                   "grep find clear pwd\n" +
                   "info date mem theme\n" +
                   "uname whoami uptime\n" +
                   "exit logout";
        }
        if (c.equals("clear") || c.equals("cls")) {
            clearScreen();
            return null;
        }
        if (c.equals("ls") || c.equals("dir")) {
            return "total 7\n" +
                   "drwxr-xr-x system/\n" +
                   "drwxr-xr-x apps/\n" +
                   "drwxr-xr-x data/\n" +
                   "drwxr-xr-x config/\n" +
                   "drwxr-xr-x TFCard/\n" +
                   "-rw-r--r-- readme.txt\n" +
                   "-rw-r--r-- notes.txt";
        }
        if (c.equals("pwd")) {
            return "/home/" + currentUser;
        }
        if (c.startsWith("echo ")) {
            return cmd.substring(5);
        }
        if (c.startsWith("cat ")) {
            if (parts.length < 2) {
                return "Usage: cat <fichier>";
            }
            return FileReaderUtil.readFile(parts[1]);
        }
        if (c.startsWith("write ")) {
            if (parts.length < 3) {
                return "Usage: write <fichier> <contenu>";
            }
            String content = cmd.substring(6 + parts[1].length()).trim();
            return FileWriterUtil.writeFile(parts[1], content);
        }
        if (c.startsWith("mkdir ")) {
            if (parts.length < 2) {
                return "Usage: mkdir <dossier>";
            }
            return "mkdir: created directory '" + parts[1] + "'";
        }
        if (c.startsWith("rm ")) {
            if (parts.length < 2) {
                return "Usage: rm <fichier>";
            }
            return "rm: removed '" + parts[1] + "'";
        }
        if (c.startsWith("grep ")) {
            if (parts.length < 3) {
                return "Usage: grep <pattern> <file>";
            }
            return grepCommand(parts[1], parts[2]);
        }
        if (c.startsWith("find ")) {
            if (parts.length < 2) {
                return "Usage: find <name>";
            }
            return findCommand(parts[1]);
        }
        if (c.equals("calc")) {
            mainApp.launchCalculator();
            return null;
        }
        if (c.equals("note")) {
            mainApp.launchNotepad();
            return null;
        }
        if (c.equals("edit")) {
            mainApp.launchTextEditor();
            return null;
        }
        if (c.equals("files")) {
            mainApp.launchFileManager();
            return null;
        }
        if (c.equals("http")) {
            mainApp.launchHttpClient();
            return null;
        }
        if (c.equals("snake")) {
            mainApp.launchSnakeGame();
            return null;
        }
        if (c.equals("tetris")) {
            mainApp.launchTetris();
            return null;
        }
        if (c.equals("contacts")) {
            mainApp.launchContactManager();
            return null;
        }
        if (c.equals("tasks") || c.equals("top")) {
            mainApp.launchTaskManager();
            return null;
        }
        if (c.equals("theme")) {
            mainApp.launchThemeSelector();
            return null;
        }
        if (c.equals("about")) {
            mainApp.launchAboutScreen();
            return null;
        }
        if (c.equals("info") || c.equals("neofetch")) {
            return "     _____\n" +
                   "    /  _  \\\n" +
                   "   |  | |  |   OS: DiscoLinux 2.0\n" +
                   "   |  |_|  |   Kernel: J2ME CLDC\n" +
                   "    \\_____/    Shell: DiscoShell\n" +
                   "              Theme: " + ThemeManager.getThemeName() + "\n" +
                   "              Device: Itel 5615\n" +
                   "              User: " + currentUser;
        }
        if (c.equals("uname")) {
            return "DiscoLinux 2.0.0-j2me #1 J2ME CLDC";
        }
        if (c.equals("whoami")) {
            return currentUser;
        }
        if (c.equals("uptime")) {
            long uptime = System.currentTimeMillis() / 1000;
            return "up " + (uptime/60) + " minutes";
        }
        if (c.equals("date")) {
            long time = System.currentTimeMillis();
            return "Timestamp: " + time;
        }
        if (c.equals("mem") || c.equals("free")) {
            long free = Runtime.getRuntime().freeMemory();
            long total = Runtime.getRuntime().totalMemory();
            long used = total - free;
            return "       total  used  free\n" +
                   "Mem:   " + (total/1024) + "K  " + (used/1024) + "K  " + (free/1024) + "K";
        }
        if (c.equals("exit") || c.equals("quit") || c.equals("logout")) {
            return "EXIT_TERMINAL";
        }
        
        return "bash: " + parts[0] + ": command not found";
    }
    
    private String grepCommand(String pattern, String filename) {
        String content = FileReaderUtil.readFile(filename);
        
        if (content.startsWith("Fichier non trouve")) {
            return content;
        }
        
        StringBuffer result = new StringBuffer();
        Vector lines = split(content, '\n');
        int matchCount = 0;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            if (line.toLowerCase().indexOf(pattern.toLowerCase()) >= 0) {
                result.append(line + "\n");
                matchCount++;
            }
        }
        
        if (matchCount == 0) {
            return "grep: no matches found";
        }
        
        return result.toString().trim();
    }
    
    private String findCommand(String name) {
        StringBuffer result = new StringBuffer();
        result.append("Searching for: " + name + "\n");
        
        String[] locations = {
            "/system/" + name,
            "/apps/" + name,
            "/data/documents/" + name,
            "/home/root/" + name,
            "/TFCard/" + name
        };
        
        for (int i = 0; i < locations.length; i++) {
            if (name.equals("readme.txt") && locations[i].indexOf("documents") >= 0) {
                result.append("Found: " + locations[i] + "\n");
            } else if (name.equals("notes.txt") && locations[i].indexOf("documents") >= 0) {
                result.append("Found: " + locations[i] + "\n");
            } else if (name.equals("todo.txt") && locations[i].indexOf("root") >= 0) {
                result.append("Found: " + locations[i] + "\n");
            }
        }
        
        if (result.toString().indexOf("Found:") < 0) {
            result.append("No files found");
        }
        
        return result.toString();
    }
    
    private String getManPage(String cmd) {
        if (cmd.equals("ls")) {
            return "NAME\n  ls - list directory\n\nSYNOPSIS\n  ls\n\nDESCRIPTION\n  List files and dirs";
        }
        if (cmd.equals("cat")) {
            return "NAME\n  cat - read file\n\nSYNOPSIS\n  cat <file>\n\nDESCRIPTION\n  Display file content";
        }
        if (cmd.equals("write")) {
            return "NAME\n  write - write file\n\nSYNOPSIS\n  write <file> <text>\n\nEXAMPLE\n  write test.txt hello";
        }
        if (cmd.equals("calc")) {
            return "NAME\n  calc - calculator\n\nSYNOPSIS\n  calc\n\nDESCRIPTION\n  Launch calculator app\n  2=+ 4=- 6=* 8=/ 5=equals";
        }
        if (cmd.equals("rm")) {
            return "NAME\n  rm - remove file\n\nSYNOPSIS\n  rm <file>\n\nWARNING\n  Cannot be undone!";
        }
        if (cmd.equals("mkdir")) {
            return "NAME\n  mkdir - make directory\n\nSYNOPSIS\n  mkdir <dirname>";
        }
        if (cmd.equals("clear")) {
            return "NAME\n  clear - clear screen\n\nSYNOPSIS\n  clear";
        }
        if (cmd.equals("grep")) {
            return "NAME\n  grep - search pattern\n\nSYNOPSIS\n  grep <pattern> <file>\n\nEXAMPLE\n  grep hello test.txt";
        }
        if (cmd.equals("find")) {
            return "NAME\n  find - find files\n\nSYNOPSIS\n  find <name>\n\nEXAMPLE\n  find readme.txt";
        }
        if (cmd.equals("theme")) {
            return "NAME\n  theme - change theme\n\nSYNOPSIS\n  theme\n\nDESCRIPTION\n  Open theme selector";
        }
        if (cmd.equals("tetris")) {
            return "NAME\n  tetris - play tetris\n\nSYNOPSIS\n  tetris\n\nCONTROLS\n  4/6=left/right 2=rotate 8=drop";
        }
        if (cmd.equals("snake")) {
            return "NAME\n  snake - play snake\n\nSYNOPSIS\n  snake\n\nCONTROLS\n  2/4/6/8=directions";
        }
        if (cmd.equals("contacts")) {
            return "NAME\n  contacts - contact manager\n\nSYNOPSIS\n  contacts\n\nDESCRIPTION\n  Add, Edit, Delete, Search";
        }
        
        return "No manual entry for " + cmd;
    }
    
    public Vector split(String s, char d) {
        Vector v = new Vector();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == d) {
                v.addElement(b.toString());
                b = new StringBuffer();
            } else {
                b.append(c);
            }
        }
        if (b.length() > 0) {
            v.addElement(b.toString());
        }
        return v;
    }
    
    private String[] splitCommand(String cmd) {
        Vector v = new Vector();
        StringBuffer word = new StringBuffer();
        
        for (int i = 0; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            if (c == ' ') {
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
}