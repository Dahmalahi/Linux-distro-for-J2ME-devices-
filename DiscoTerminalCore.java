import javax.microedition.rms.*;
import java.util.Vector;
import java.util.Date;
import java.util.Random;

/**
 * DiscoTerminalCore.java - Version ENRICHIE
 * 
 * 60+ COMMANDES DISPONIBLES:
 * - Fichiers: ls, cd, pwd, cat, touch, mkdir, rm, cp, mv, find, tree, du
 * - Texte: echo, grep, wc, head, tail, sort, uniq, sed, cut, tr
 * - Systeme: ps, top, kill, reboot, shutdown, date, uptime, who, w
 * - Reseau: ping, ifconfig, netstat, wget, curl, telnet
 * - Utilitaires: calc, cal, bc, factor, seq, yes, true, false
 * - Info: uname, hostname, whoami, id, env, set, which, whereis
 * - Permissions: chmod, chown, chgrp, umask
 * - Divers: history, clear, exit, help, man, alias
 */
public class DiscoTerminalCore {
    private Vector commandHistory;
    private String currentDir;
    private String username;
    private String hostname;
    private Vector envVars;
    private int pidCounter;
    private Random random;
    
    // SYSTÈME DE FICHIERS RÉEL
    private RealFileSystem realFS;
    
    // INTERPRÉTEUR LUA
    private MiniLua luaInterpreter;
    
    private static final String FS_STORE = "DiscoFS";
    private int historyIndex = -1;
    
    // Compteurs systeme
    private long startTime;
    private int commandCount = 0;
    
    // Alias
    private Vector aliases;

    public DiscoTerminalCore(DiscoOs app, String user) {
        this.random = new Random();
        this.username = user != null && user.length() > 0 ? user : "user";
        this.hostname = "linuxme-phone";
        this.currentDir = "/home/" + username;
        this.commandHistory = new Vector();
        this.envVars = new Vector();
        this.aliases = new Vector();
        this.pidCounter = 1000 + random.nextInt(9000);
        this.startTime = System.currentTimeMillis();
        
        // INITIALISER LE SYSTÈME DE FICHIERS RÉEL
        this.realFS = new RealFileSystem();
        boolean fsOk = realFS.initFileSystem();
        
        // INITIALISER L'INTERPRÉTEUR LUA
        this.luaInterpreter = new MiniLua(this);
        
        if (!fsOk) {
            // Afficher erreur si le FS n'a pas pu s'initialiser
            System.err.println("WARNING: Could not initialize filesystem on TFCard");
        }
        
        // Variables d'environnement
        setEnv("PATH", "/bin:/usr/bin:/usr/local/bin:/system/bin");
        setEnv("HOME", "/home/" + username);
        setEnv("USER", username);
        setEnv("HOSTNAME", hostname);
        setEnv("SHELL", "/bin/bash");
        setEnv("PWD", currentDir);
        setEnv("TERM", "xterm-256color");
        setEnv("LANG", "en_US.UTF-8");
        setEnv("EDITOR", "nano");
        setEnv("PAGER", "less");
        
        // Alias par defaut
        addAlias("ll=ls -la");
        addAlias("la=ls -a");
        addAlias("..=cd ..");
        addAlias("cls=clear");
        addAlias("md=mkdir");
        addAlias("rd=rmdir");
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public String getPreviousCommand() {
        if (commandHistory.size() == 0) return "";
        historyIndex--;
        if (historyIndex < 0) historyIndex = commandHistory.size() - 1;
        return (String) commandHistory.elementAt(historyIndex);
    }
    
    public String getNextCommand() {
        if (commandHistory.size() == 0) return "";
        historyIndex++;
        if (historyIndex >= commandHistory.size()) {
            historyIndex = -1;
            return "";
        }
        return (String) commandHistory.elementAt(historyIndex);
    }
    
    public Vector getCommandHistory() {
        return commandHistory;
    }
    
    public String getPrompt() {
        // Format: DiscoSysUsr/chemin/actuel$
        return "DiscoSysUsr" + currentDir + "$ ";
    }
    
    private void initFileSystem() {
        try {
            mkdir("/bin");
            mkdir("/etc");
            mkdir("/home");
            mkdir("/home/" + username);
            mkdir("/tmp");
            mkdir("/dev");
            mkdir("/proc");
            mkdir("/usr");
            mkdir("/usr/bin");
            mkdir("/usr/local");
            mkdir("/var");
            mkdir("/var/log");
            mkdir("/mnt");
            mkdir("/boot");
            mkdir("/opt");
            
            writeFile("/etc/motd", "Welcome to DiscoLinux 1.0 - J2ME Edition\n");
            writeFile("/etc/hostname", hostname);
            writeFile("/etc/passwd", "root:x:0:0:root:/root:/bin/bash\n" + 
                     username + ":x:1000:1000:" + username + ":/home/" + username + ":/bin/bash\n");
            writeFile("/home/" + username + "/.bashrc", "# .bashrc\nexport PS1='\\u@\\h:\\w\\$ '\n");
            writeFile("/home/" + username + "/readme.txt", "Welcome to your home directory!\n");
            
        } catch (Exception e) {}
    }

    public String executeCommand(String cmdLine) {
        if (cmdLine == null || cmdLine.trim().length() == 0) return "";
        
        String originalCmd = cmdLine.trim();
        if (originalCmd.startsWith("#")) return "";
        
        addHistory(originalCmd);
        commandCount++;
        
        // Expandre alias
        cmdLine = expandAlias(cmdLine);
        cmdLine = expandEnvVars(cmdLine);
        
        String[] parts = parseCommand(cmdLine);
        if (parts.length == 0) return "";
        
        String cmd = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        
        try {
            // ========== FICHIERS ==========
            if (cmd.equals("ls")) return cmd_ls(args);
            if (cmd.equals("cd")) return cmd_cd(args);
            if (cmd.equals("pwd")) return cmd_pwd();
            if (cmd.equals("cat")) return cmd_cat(args);
            if (cmd.equals("touch")) return cmd_touch(args);
            if (cmd.equals("mkdir")) return cmd_mkdir(args);
            if (cmd.equals("rmdir")) return cmd_rmdir(args);
            if (cmd.equals("rm")) return cmd_rm(args);
            if (cmd.equals("cp")) return cmd_cp(args);
            if (cmd.equals("mv")) return cmd_mv(args);
            if (cmd.equals("find")) return cmd_find(args);
            if (cmd.equals("tree")) return cmd_tree(args);
            if (cmd.equals("du")) return cmd_du(args);
            if (cmd.equals("df")) return cmd_df(args);
            if (cmd.equals("file")) return cmd_file(args);
            if (cmd.equals("stat")) return cmd_stat(args);
            
            // ========== TEXTE ==========
            if (cmd.equals("echo")) return cmd_echo(args);
            if (cmd.equals("grep")) return cmd_grep(args);
            if (cmd.equals("wc")) return cmd_wc(args);
            if (cmd.equals("head")) return cmd_head(args);
            if (cmd.equals("tail")) return cmd_tail(args);
            if (cmd.equals("sort")) return cmd_sort(args);
            if (cmd.equals("uniq")) return cmd_uniq(args);
            if (cmd.equals("cut")) return cmd_cut(args);
            if (cmd.equals("tr")) return cmd_tr(args);
            if (cmd.equals("rev")) return cmd_rev(args);
            
            // ========== SYSTEME ==========
            if (cmd.equals("ps")) return cmd_ps(args);
            if (cmd.equals("top")) return cmd_top();
            if (cmd.equals("kill")) return cmd_kill(args);
            if (cmd.equals("date")) return cmd_date(args);
            if (cmd.equals("uptime")) return cmd_uptime();
            if (cmd.equals("who")) return cmd_who();
            if (cmd.equals("w")) return cmd_w();
            if (cmd.equals("whoami")) return cmd_whoami();
            if (cmd.equals("id")) return cmd_id();
            if (cmd.equals("uname")) return cmd_uname(args);
            if (cmd.equals("hostname")) return cmd_hostname(args);
            if (cmd.equals("reboot")) return cmd_reboot();
            if (cmd.equals("shutdown")) return cmd_shutdown(args);
            if (cmd.equals("free")) return cmd_free(args);
            
            // ========== RESEAU ==========
            if (cmd.equals("ping")) return cmd_ping(args);
            if (cmd.equals("ifconfig")) return cmd_ifconfig();
            if (cmd.equals("netstat")) return cmd_netstat();
            if (cmd.equals("wget")) return cmd_wget(args);
            if (cmd.equals("curl")) return cmd_curl(args);
            
            // ========== UTILITAIRES ==========
            if (cmd.equals("calc")) return cmd_calc(args);
            if (cmd.equals("cal")) return cmd_cal(args);
            if (cmd.equals("bc")) return cmd_bc(args);
            if (cmd.equals("factor")) return cmd_factor(args);
            if (cmd.equals("seq")) return cmd_seq(args);
            if (cmd.equals("yes")) return cmd_yes(args);
            if (cmd.equals("true")) return cmd_true();
            if (cmd.equals("false")) return cmd_false();
            if (cmd.equals("sleep")) return cmd_sleep(args);
            
            // ========== PERMISSIONS ==========
            if (cmd.equals("chmod")) return cmd_chmod(args);
            if (cmd.equals("chown")) return cmd_chown(args);
            if (cmd.equals("chgrp")) return cmd_chgrp(args);
            if (cmd.equals("umask")) return cmd_umask(args);
            
            // ========== ENVIRONNEMENT ==========
            if (cmd.equals("env")) return cmd_env();
            if (cmd.equals("set")) return cmd_set();
            if (cmd.equals("export")) return cmd_export(args);
            if (cmd.equals("unset")) return cmd_unset(args);
            if (cmd.equals("which")) return cmd_which(args);
            if (cmd.equals("whereis")) return cmd_whereis(args);
            
            // ========== SHELL ==========
            if (cmd.equals("history")) return cmd_history(args);
            if (cmd.equals("alias")) return cmd_alias(args);
            if (cmd.equals("unalias")) return cmd_unalias(args);
            if (cmd.equals("clear")) return "CLEAR_SCREEN";
            if (cmd.equals("exit") || cmd.equals("logout") || cmd.equals("quit")) return "EXIT_TERMINAL";
            if (cmd.equals("help")) return HelpSystem.getMainHelp();
            if (cmd.equals("help1")) return HelpSystem.getFileHelp();
            if (cmd.equals("help2")) return HelpSystem.getTextHelp();
            if (cmd.equals("help3")) return HelpSystem.getSystemHelp();
            if (cmd.equals("help4")) return HelpSystem.getNetworkHelp();
            if (cmd.equals("help5")) return HelpSystem.getShellHelp();
            if (cmd.equals("man")) return HelpSystem.getManPage(args.length > 0 ? args[0] : "");
            if (cmd.equals("info")) return HelpSystem.getInfo(args.length > 0 ? args[0] : "");
            
            // ========== DISCO COMMANDS ==========
            if (cmd.equals("disco")) {
                if (args.length > 0 && args[0].equals("--art")) {
                    return DiscoCommand.getAlternateArt();
                }
                return DiscoCommand.getSystemInfo(this);
            }
            if (cmd.equals("discoinfo")) {
                String device = System.getProperty("microedition.platform");
                if (device == null || device.length() == 0) {
                    device = "Unknown";
                }
                String theme = "Default";
                try {
                    theme = ThemeManager.getThemeName();
                } catch (Exception e) {}
                return DiscoCommand.getDiscoInfo(username, device, theme);
            }
            
            // ========== APPLICATIONS ==========
            if (cmd.equals("calculator") || cmd.equals("calc")) return "LAUNCH_CALC";
            if (cmd.equals("editor") || cmd.equals("edit")) return "LAUNCH_EDITOR";
            if (cmd.equals("snake")) return "LAUNCH_SNAKE";
            if (cmd.equals("tetris")) return "LAUNCH_TETRIS";
            if (cmd.equals("tasks")) return "LAUNCH_TASKS";
            if (cmd.equals("http")) return "LAUNCH_HTTP";
            if (cmd.equals("lua")) return cmd_lua(args);
            
            return cmd + ": command not found\nType 'help' for available commands";
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // ========== IMPLEMENTATION DES COMMANDES ==========
    
    private String cmd_ls(String[] args) {
        boolean longFormat = false;
        boolean showAll = false;
        String path = currentDir;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-l") || args[i].equals("-la") || args[i].equals("-al")) {
                longFormat = true;
                if (args[i].indexOf('a') >= 0) showAll = true;
            } else if (args[i].equals("-a")) {
                showAll = true;
            } else {
                path = resolvePath(args[i]);
            }
        }
        
        Vector files = listFiles(path);
        if (files == null) return "ls: cannot access '" + path + "': No such file or directory";
        
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.elementAt(i);
            if (!showAll && file.startsWith(".")) continue;
            
            if (longFormat) {
                boolean isDir = isDirectory(path + "/" + file);
                result.append(isDir ? "drwxr-xr-x" : "-rw-r--r--");
                result.append(" 1 ").append(username).append(" users");
                result.append(" 1024 ");
                result.append(formatDate(System.currentTimeMillis()));
                result.append(" ").append(file).append("\n");
            } else {
                result.append(file);
                if (isDirectory(path + "/" + file)) result.append("/");
                result.append("  ");
            }
        }
        return result.toString().trim();
    }
    
    private String cmd_tree(String[] args) {
        String path = args.length > 0 ? resolvePath(args[0]) : currentDir;
        StringBuffer result = new StringBuffer();
        result.append(path).append("\n");
        buildTree(path, "", result, 0);
        return result.toString();
    }
    
    private void buildTree(String path, String prefix, StringBuffer result, int level) {
        if (level > 3) return; // Limite profondeur
        Vector files = listFiles(path);
        if (files == null) return;
        
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.elementAt(i);
            boolean isLast = (i == files.size() - 1);
            result.append(prefix).append(isLast ? "└── " : "├── ").append(file).append("\n");
            
            String fullPath = path.equals("/") ? "/" + file : path + "/" + file;
            if (isDirectory(fullPath)) {
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                buildTree(fullPath, newPrefix, result, level + 1);
            }
        }
    }
    
    private String cmd_du(String[] args) {
        String path = args.length > 0 ? resolvePath(args[0]) : currentDir;
        boolean human = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) human = true;
        }
        
        long size = calculateDirSize(path);
        if (human) {
            return formatSize(size) + "\t" + path;
        }
        return (size / 1024) + "\t" + path;
    }
    
    private long calculateDirSize(String path) {
        long total = 0;
        Vector files = listFiles(path);
        if (files == null) return 0;
        
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.elementAt(i);
            String fullPath = path.equals("/") ? "/" + file : path + "/" + file;
            if (isDirectory(fullPath)) {
                total += calculateDirSize(fullPath);
            } else {
                String content = readFile(fullPath);
                if (content != null) total += content.length();
            }
        }
        return total;
    }
    
    private String cmd_cd(String[] args) {
        if (args.length == 0) {
            currentDir = getEnv("HOME");
            setEnv("PWD", currentDir);
            return "";
        }
        
        String newDir = resolvePath(args[0]);
        if (isDirectory(newDir)) {
            currentDir = newDir;
            setEnv("PWD", currentDir);
            return "";
        }
        return "cd: " + args[0] + ": No such file or directory";
    }
    
    private String cmd_pwd() {
        return currentDir;
    }
    
    private String cmd_cat(String[] args) {
        if (args.length == 0) return "cat: missing file operand";
        
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            String path = resolvePath(args[i]);
            String content = readFile(path);
            if (content == null) {
                result.append("cat: ").append(args[i]).append(": No such file\n");
            } else {
                result.append(content);
                if (i < args.length - 1) result.append("\n");
            }
        }
        return result.toString();
    }
    
    private String cmd_echo(String[] args) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            result.append(args[i]);
            if (i < args.length - 1) result.append(" ");
        }
        return result.toString();
    }
    
    private String cmd_grep(String[] args) {
        if (args.length < 2) return "grep: Usage: grep PATTERN FILE";
        
        String pattern = args[0];
        String path = resolvePath(args[1]);
        String content = readFile(path);
        
        if (content == null) return "grep: " + args[1] + ": No such file";
        
        StringBuffer result = new StringBuffer();
        int start = 0;
        while (start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            String line = content.substring(start, end);
            if (line.indexOf(pattern) >= 0) {
                result.append(line).append("\n");
            }
            start = end + 1;
        }
        return result.toString().trim();
    }
    
    private String cmd_wc(String[] args) {
        if (args.length == 0) return "wc: missing file operand";
        
        boolean lines = false, words = false, chars = false;
        String file = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-l")) lines = true;
            else if (args[i].equals("-w")) words = true;
            else if (args[i].equals("-c")) chars = true;
            else file = args[i];
        }
        
        if (!lines && !words && !chars) {
            lines = words = chars = true;
        }
        
        String content = readFile(resolvePath(file));
        if (content == null) return "wc: " + file + ": No such file";
        
        int lineCount = countLines(content);
        int wordCount = countWords(content);
        int charCount = content.length();
        
        StringBuffer result = new StringBuffer();
        if (lines) result.append(" ").append(lineCount);
        if (words) result.append(" ").append(wordCount);
        if (chars) result.append(" ").append(charCount);
        result.append(" ").append(file);
        
        return result.toString().trim();
    }
    
    private String cmd_head(String[] args) {
        int n = 10;
        String file = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-n") && i + 1 < args.length) {
                try { n = Integer.parseInt(args[++i]); } catch (Exception e) {}
            } else {
                file = args[i];
            }
        }
        
        if (file == null) return "head: missing file operand";
        
        String content = readFile(resolvePath(file));
        if (content == null) return "head: " + file + ": No such file";
        
        StringBuffer result = new StringBuffer();
        int count = 0, start = 0;
        while (count < n && start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            result.append(content.substring(start, end)).append("\n");
            start = end + 1;
            count++;
        }
        return result.toString().trim();
    }
    
    private String cmd_tail(String[] args) {
        int n = 10;
        String file = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-n") && i + 1 < args.length) {
                try { n = Integer.parseInt(args[++i]); } catch (Exception e) {}
            } else {
                file = args[i];
            }
        }
        
        if (file == null) return "tail: missing file operand";
        
        String content = readFile(resolvePath(file));
        if (content == null) return "tail: " + file + ": No such file";
        
        Vector lines = new Vector();
        int start = 0;
        while (start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            lines.addElement(content.substring(start, end));
            start = end + 1;
        }
        
        StringBuffer result = new StringBuffer();
        int startIdx = Math.max(0, lines.size() - n);
        for (int i = startIdx; i < lines.size(); i++) {
            result.append(lines.elementAt(i)).append("\n");
        }
        return result.toString().trim();
    }
    
    private String cmd_sort(String[] args) {
        if (args.length == 0) return "sort: missing file operand";
        
        String content = readFile(resolvePath(args[0]));
        if (content == null) return "sort: " + args[0] + ": No such file";
        
        Vector lines = new Vector();
        int start = 0;
        while (start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            String line = content.substring(start, end).trim();
            if (line.length() > 0) lines.addElement(line);
            start = end + 1;
        }
        
        // Bubble sort
        for (int i = 0; i < lines.size() - 1; i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                String a = (String) lines.elementAt(i);
                String b = (String) lines.elementAt(j);
                if (a.compareTo(b) > 0) {
                    lines.setElementAt(b, i);
                    lines.setElementAt(a, j);
                }
            }
        }
        
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < lines.size(); i++) {
            result.append(lines.elementAt(i)).append("\n");
        }
        return result.toString().trim();
    }
    
    private String cmd_uniq(String[] args) {
        if (args.length == 0) return "uniq: missing file operand";
        
        String content = readFile(resolvePath(args[0]));
        if (content == null) return "uniq: " + args[0] + ": No such file";
        
        StringBuffer result = new StringBuffer();
        String lastLine = null;
        int start = 0;
        
        while (start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            String line = content.substring(start, end);
            
            if (!line.equals(lastLine)) {
                result.append(line).append("\n");
                lastLine = line;
            }
            start = end + 1;
        }
        return result.toString().trim();
    }
    
    private String cmd_cut(String[] args) {
        if (args.length < 2) return "cut: Usage: cut -f N FILE";
        
        int field = 1;
        String delim = "\t";
        String file = null;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f") && i + 1 < args.length) {
                try { field = Integer.parseInt(args[++i]); } catch (Exception e) {}
            } else if (args[i].equals("-d") && i + 1 < args.length) {
                delim = args[++i];
            } else {
                file = args[i];
            }
        }
        
        if (file == null) return "cut: missing file";
        
        String content = readFile(resolvePath(file));
        if (content == null) return "cut: " + file + ": No such file";
        
        StringBuffer result = new StringBuffer();
        int start = 0;
        while (start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            String line = content.substring(start, end);
            
            int fieldIdx = 1;
            int pos = 0;
            while (pos < line.length()) {
                int nextDelim = line.indexOf(delim, pos);
                if (nextDelim == -1) nextDelim = line.length();
                
                if (fieldIdx == field) {
                    result.append(line.substring(pos, nextDelim)).append("\n");
                    break;
                }
                
                pos = nextDelim + delim.length();
                fieldIdx++;
            }
            start = end + 1;
        }
        return result.toString().trim();
    }
    
    private String cmd_tr(String[] args) {
        if (args.length < 2) return "tr: Usage: tr SET1 SET2";
        
        String from = args[0];
        String to = args[1];
        
        // Simuler lecture depuis stdin
        return "tr: reading from stdin not supported in this version";
    }
    
    private String cmd_rev(String[] args) {
        if (args.length == 0) return "rev: missing file operand";
        
        String content = readFile(resolvePath(args[0]));
        if (content == null) return "rev: " + args[0] + ": No such file";
        
        StringBuffer result = new StringBuffer();
        int start = 0;
        while (start < content.length()) {
            int end = content.indexOf('\n', start);
            if (end == -1) end = content.length();
            String line = content.substring(start, end);
            
            for (int i = line.length() - 1; i >= 0; i--) {
                result.append(line.charAt(i));
            }
            result.append("\n");
            start = end + 1;
        }
        return result.toString().trim();
    }
    
    private String cmd_touch(String[] args) {
        if (args.length == 0) return "touch: missing file operand";
        
        for (int i = 0; i < args.length; i++) {
            String path = resolvePath(args[i]);
            if (!fileExists(path)) {
                writeFile(path, "");
            }
        }
        return "";
    }
    
    private String cmd_mkdir(String[] args) {
        if (args.length == 0) return "mkdir: missing operand";
        
        for (int i = 0; i < args.length; i++) {
            String path = resolvePath(args[i]);
            mkdir(path);
        }
        return "";
    }
    
    private String cmd_rmdir(String[] args) {
        if (args.length == 0) return "rmdir: missing operand";
        return "rmdir: " + args[0] + " (directory removal not fully implemented)";
    }
    
    private String cmd_rm(String[] args) {
        if (args.length == 0) return "rm: missing operand";
        
        for (int i = 0; i < args.length; i++) {
            String path = resolvePath(args[i]);
            deleteFile(path);
        }
        return "";
    }
    
    private String cmd_cp(String[] args) {
        if (args.length < 2) return "cp: missing destination";
        
        String src = resolvePath(args[0]);
        String dest = resolvePath(args[1]);
        
        String content = readFile(src);
        if (content == null) return "cp: " + args[0] + ": No such file";
        
        writeFile(dest, content);
        return "";
    }
    
    private String cmd_mv(String[] args) {
        if (args.length < 2) return "mv: missing destination";
        
        String src = resolvePath(args[0]);
        String dest = resolvePath(args[1]);
        
        String content = readFile(src);
        if (content == null) return "mv: " + args[0] + ": No such file";
        
        writeFile(dest, content);
        deleteFile(src);
        return "";
    }
    
    private String cmd_find(String[] args) {
        String path = "/";
        String pattern = "*";
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-name") && i + 1 < args.length) {
                pattern = args[++i];
            } else if (!args[i].startsWith("-")) {
                path = resolvePath(args[i]);
            }
        }
        
        StringBuffer result = new StringBuffer();
        findFiles(path, pattern, result);
        return result.toString().trim();
    }
    
    private void findFiles(String path, String pattern, StringBuffer result) {
        Vector files = listFiles(path);
        if (files == null) return;
        
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.elementAt(i);
            String fullPath = path.equals("/") ? "/" + file : path + "/" + file;
            
            if (matchPattern(file, pattern)) {
                result.append(fullPath).append("\n");
            }
            
            if (isDirectory(fullPath)) {
                findFiles(fullPath, pattern, result);
            }
        }
    }
    
    private boolean matchPattern(String str, String pattern) {
        if (pattern.equals("*")) return true;
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            String mid = pattern.substring(1, pattern.length() - 1);
            return str.indexOf(mid) >= 0;
        }
        if (pattern.startsWith("*")) {
            return str.endsWith(pattern.substring(1));
        }
        if (pattern.endsWith("*")) {
            return str.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return str.equals(pattern);
    }
    
    private String cmd_file(String[] args) {
        if (args.length == 0) return "file: missing operand";
        
        String path = resolvePath(args[0]);
        if (!fileExists(path)) return args[0] + ": cannot open (No such file)";
        
        if (isDirectory(path)) return args[0] + ": directory";
        
        String content = readFile(path);
        if (content == null) return args[0] + ": empty";
        
        if (content.length() == 0) return args[0] + ": empty file";
        if (content.indexOf('\n') >= 0) return args[0] + ": ASCII text";
        
        return args[0] + ": data";
    }
    
    private String cmd_stat(String[] args) {
        if (args.length == 0) return "stat: missing operand";
        
        String path = resolvePath(args[0]);
        if (!fileExists(path)) return "stat: " + args[0] + ": No such file";
        
        StringBuffer result = new StringBuffer();
        result.append("  File: ").append(args[0]).append("\n");
        result.append("  Size: ");
        
        String content = readFile(path);
        result.append(content != null ? content.length() : 0).append("\n");
        result.append("Access: (0644/-rw-r--r--)  Uid: ( 1000/").append(username).append(")\n");
        
        return result.toString();
    }
    
    private String cmd_df(String[] args) {
        boolean human = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) human = true;
        }
        
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        
        StringBuffer result = new StringBuffer();
        result.append("Filesystem     ");
        if (human) result.append("Size  Used Avail Use%");
        else result.append("1K-blocks   Used  Available Use%");
        result.append(" Mounted on\n");
        
        if (human) {
            result.append("rootfs         ").append(formatSize(total));
            result.append("  ").append(formatSize(used));
            result.append("  ").append(formatSize(free));
        } else {
            result.append("rootfs         ").append(total/1024);
            result.append("  ").append(used/1024);
            result.append("  ").append(free/1024);
        }
        
        int percent = (int)((used * 100) / total);
        result.append("  ").append(percent).append("%");
        result.append("  /\n");
        
        return result.toString();
    }
    
    private String cmd_ps(String[] args) {
        boolean all = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-aux") || args[i].equals("-ef")) all = true;
        }
        
        StringBuffer result = new StringBuffer();
        result.append("PID   USER     STAT  %CPU %MEM   TIME COMMAND\n");
        result.append(pidCounter).append("  ").append(username).append("  S      0.0  1.2   0:02 DiscoOs\n");
        result.append(pidCounter+1).append("  ").append(username).append("  R      0.0  0.5   0:00 bash\n");
        
        if (all) {
            result.append((pidCounter+2)).append("  root     S      0.0  0.3   0:00 init\n");
            result.append((pidCounter+3)).append("  root     S      0.0  0.2   0:00 kthreadd\n");
        }
        
        return result.toString();
    }
    
    private String cmd_top() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        
        StringBuffer result = new StringBuffer();
        result.append("top - ").append(formatTime(System.currentTimeMillis()));
        result.append(" up ").append(uptime/60).append(" min\n");
        result.append("Tasks: 4 total, 1 running\n");
        result.append("Mem: ").append(total/1024).append("k total, ");
        result.append((total-free)/1024).append("k used\n");
        result.append("\nPID USER   %CPU %MEM COMMAND\n");
        result.append(pidCounter).append(" ").append(username).append("   0.0  1.2 DiscoOs\n");
        
        return result.toString();
    }
    
    private String cmd_kill(String[] args) {
        if (args.length == 0) return "kill: missing PID";
        return "kill: process " + args[0] + " terminated";
    }
    
    private String cmd_date(String[] args) {
        Date now = new Date();
        if (args.length > 0 && args[0].startsWith("+")) {
            // Format personnalise
            return formatDate(now.getTime());
        }
        return now.toString();
    }
    
    private String cmd_uptime() {
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        long hours = uptime / 3600;
        long minutes = (uptime % 3600) / 60;
        
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        
        return "up " + hours + ":" + minutes + ", " + commandCount + " commands, load average: 0.00, 0.00, 0.00";
    }
    
    private String cmd_who() {
        return username + "  pts/0        " + formatDate(startTime);
    }
    
    private String cmd_w() {
        StringBuffer result = new StringBuffer();
        result.append(cmd_uptime()).append("\n");
        result.append("USER  TTY   FROM      LOGIN@   IDLE  COMMAND\n");
        result.append(username).append(" pts/0 localhost ").append(formatTime(startTime));
        result.append("  0.00s bash\n");
        return result.toString();
    }
    
    private String cmd_whoami() {
        return username;
    }
    
    private String cmd_id() {
        int uid = username.equals("root") ? 0 : 1000;
        return "uid=" + uid + "(" + username + ") gid=" + uid + "(users) groups=" + uid + "(users),4(adm),20(dialout)";
    }
    
    private String cmd_uname(String[] args) {
        boolean all = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a")) all = true;
        }
        
        if (all) {
            return "DiscoLinux " + hostname + " 1.0.0 #1 SMP " + new Date() + " armv5tejl GNU/Linux";
        }
        return "DiscoLinux";
    }
    
    private String cmd_hostname(String[] args) {
        if (args.length > 0) {
            hostname = args[0];
            setEnv("HOSTNAME", hostname);
            return "";
        }
        return hostname;
    }
    
    private String cmd_reboot() {
        return "System is going down for reboot NOW!\n(This is a simulation - press OK to continue)";
    }
    
    private String cmd_shutdown(String[] args) {
        String time = args.length > 0 ? args[0] : "now";
        return "Shutdown scheduled for " + time + "\n(This is a simulation)";
    }
    
    private String cmd_free(String[] args) {
        boolean human = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h") || args[i].equals("-m")) human = true;
        }
        
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        
        StringBuffer result = new StringBuffer();
        result.append("              total        used        free\n");
        result.append("Mem:      ");
        
        if (human) {
            result.append(formatSize(total)).append("    ");
            result.append(formatSize(used)).append("    ");
            result.append(formatSize(free)).append("\n");
        } else {
            result.append(total/1024).append("    ");
            result.append(used/1024).append("    ");
            result.append(free/1024).append("\n");
        }
        
        return result.toString();
    }
    
    private String cmd_ping(String[] args) {
        if (args.length == 0) return "ping: missing host operand";
        
        String host = args[0];
        StringBuffer result = new StringBuffer();
        result.append("PING ").append(host).append(" (simulated)\n");
        
        for (int i = 0; i < 4; i++) {
            int time = 10 + random.nextInt(40);
            result.append("64 bytes from ").append(host).append(": time=").append(time).append("ms\n");
        }
        
        result.append("\n4 packets transmitted, 4 received, 0% packet loss");
        return result.toString();
    }
    
    private String cmd_ifconfig() {
        StringBuffer result = new StringBuffer();
        result.append("gprs0: flags=4163<UP,BROADCAST,RUNNING>\n");
        result.append("       inet 10.0.0.2  netmask 255.255.255.0\n");
        result.append("       RX packets:1234  errors:0  dropped:0\n");
        result.append("       TX packets:987   errors:0  dropped:0\n\n");
        result.append("lo: flags=73<UP,LOOPBACK,RUNNING>\n");
        result.append("       inet 127.0.0.1  netmask 255.0.0.0\n");
        return result.toString();
    }
    
    private String cmd_netstat() {
        StringBuffer result = new StringBuffer();
        result.append("Active Internet connections\n");
        result.append("Proto Recv-Q Send-Q Local Address   Foreign Address  State\n");
        result.append("tcp        0      0 127.0.0.1:8080  0.0.0.0:*        LISTEN\n");
        result.append("tcp        0      0 10.0.0.2:54321  93.184.216.34:80 ESTABLISHED\n");
        return result.toString();
    }
    
    private String cmd_wget(String[] args) {
        if (args.length == 0) return "wget: missing URL";
        return "wget: downloading " + args[0] + "...\n(Network operations require HTTP client)";
    }
    
    private String cmd_curl(String[] args) {
        if (args.length == 0) return "curl: no URL specified";
        return "curl: " + args[0] + "\n(Use 'http' command to launch HTTP client)";
    }
    
    private String cmd_calc(String[] args) {
        if (args.length == 0) return "LAUNCH_CALC";
        
        // Simple evaluation
        try {
            String expr = "";
            for (int i = 0; i < args.length; i++) expr += args[i];
            
            // Basic evaluation (addition/subtraction only)
            if (expr.indexOf('+') > 0) {
                String[] parts = split(expr, '+');
                int result = 0;
                for (int i = 0; i < parts.length; i++) {
                    result += Integer.parseInt(parts[i].trim());
                }
                return String.valueOf(result);
            }
            
            return expr;
        } catch (Exception e) {
            return "calc: invalid expression\nUse: calc or calc 2+2";
        }
    }
    
    private String cmd_cal(String[] args) {
        StringBuffer result = new StringBuffer();
        result.append("    February 2026\n");
        result.append("Su Mo Tu We Th Fr Sa\n");
        result.append(" 1  2  3  4  5  6  7\n");
        result.append(" 8  9 10 11 12 13 14\n");
        result.append("15 16 17 18 19 20 21\n");
        result.append("22 23 24 25 26 27 28\n");
        return result.toString();
    }
    
    private String cmd_bc(String[] args) {
        return "bc: arbitrary precision calculator\n(Limited implementation - use 'calc' for basic operations)";
    }
    
    private String cmd_factor(String[] args) {
        if (args.length == 0) return "factor: missing operand";
        
        try {
            int num = Integer.parseInt(args[0]);
            StringBuffer result = new StringBuffer();
            result.append(num).append(":");
            
            for (int i = 2; i <= num; i++) {
                while (num % i == 0) {
                    result.append(" ").append(i);
                    num /= i;
                }
            }
            
            return result.toString();
        } catch (Exception e) {
            return "factor: invalid number";
        }
    }
    
    private String cmd_seq(String[] args) {
        if (args.length == 0) return "seq: missing operand";
        
        try {
            int start = 1, end = 1, step = 1;
            
            if (args.length == 1) {
                end = Integer.parseInt(args[0]);
            } else if (args.length == 2) {
                start = Integer.parseInt(args[0]);
                end = Integer.parseInt(args[1]);
            } else {
                start = Integer.parseInt(args[0]);
                step = Integer.parseInt(args[1]);
                end = Integer.parseInt(args[2]);
            }
            
            StringBuffer result = new StringBuffer();
            for (int i = start; step > 0 ? i <= end : i >= end; i += step) {
                result.append(i).append("\n");
            }
            return result.toString().trim();
        } catch (Exception e) {
            return "seq: invalid arguments";
        }
    }
    
    private String cmd_yes(String[] args) {
        String msg = args.length > 0 ? args[0] : "y";
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            result.append(msg).append("\n");
        }
        result.append("... (output limited to 10 lines)");
        return result.toString();
    }
    
    private String cmd_true() {
        return "";
    }
    
    private String cmd_false() {
        return "";
    }
    
    private String cmd_sleep(String[] args) {
        if (args.length == 0) return "sleep: missing operand";
        return "sleep: pausing for " + args[0] + " seconds (simulated)";
    }
    
    private String cmd_chmod(String[] args) {
        if (args.length < 2) return "chmod: missing operand";
        return "chmod: permissions changed for " + args[1];
    }
    
    private String cmd_chown(String[] args) {
        if (args.length < 2) return "chown: missing operand";
        return "chown: ownership changed for " + args[1];
    }
    
    private String cmd_chgrp(String[] args) {
        if (args.length < 2) return "chgrp: missing operand";
        return "chgrp: group changed for " + args[1];
    }
    
    private String cmd_umask(String[] args) {
        if (args.length == 0) return "0022";
        return "umask: set to " + args[0];
    }
    
    private String cmd_env() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < envVars.size(); i++) {
            result.append(envVars.elementAt(i)).append("\n");
        }
        return result.toString().trim();
    }
    
    private String cmd_set() {
        return cmd_env();
    }
    
    private String cmd_export(String[] args) {
        if (args.length == 0) return cmd_env();
        
        for (int i = 0; i < args.length; i++) {
            int eq = args[i].indexOf('=');
            if (eq > 0) {
                String key = args[i].substring(0, eq);
                String value = args[i].substring(eq + 1);
                setEnv(key, value);
            }
        }
        return "";
    }
    
    private String cmd_unset(String[] args) {
        if (args.length == 0) return "unset: missing variable name";
        
        for (int i = 0; i < args.length; i++) {
            unsetEnv(args[i]);
        }
        return "";
    }
    
    private String cmd_which(String[] args) {
        if (args.length == 0) return "which: missing command name";
        return "/usr/bin/" + args[0];
    }
    
    private String cmd_whereis(String[] args) {
        if (args.length == 0) return "whereis: missing command name";
        return args[0] + ": /usr/bin/" + args[0] + " /usr/share/man/man1/" + args[0] + ".1";
    }
    
    private String cmd_history(String[] args) {
        int limit = commandHistory.size();
        if (args.length > 0) {
            try { limit = Integer.parseInt(args[0]); } catch (Exception e) {}
        }
        
        StringBuffer result = new StringBuffer();
        int start = Math.max(0, commandHistory.size() - limit);
        for (int i = start; i < commandHistory.size(); i++) {
            result.append("  ").append(i + 1).append("  ").append(commandHistory.elementAt(i)).append("\n");
        }
        return result.toString().trim();
    }
    
    private String cmd_alias(String[] args) {
        if (args.length == 0) {
            // List all aliases
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < aliases.size(); i++) {
                result.append(aliases.elementAt(i)).append("\n");
            }
            return result.toString().trim();
        }
        
        // Add alias
        String alias = "";
        for (int i = 0; i < args.length; i++) {
            alias += args[i];
            if (i < args.length - 1) alias += " ";
        }
        addAlias(alias);
        return "";
    }
    
    private String cmd_unalias(String[] args) {
        if (args.length == 0) return "unalias: missing alias name";
        
        for (int i = 0; i < aliases.size(); i++) {
            String alias = (String) aliases.elementAt(i);
            if (alias.startsWith(args[0] + "=")) {
                aliases.removeElementAt(i);
                break;
            }
        }
        return "";
    }
    
    /**
     * Commande lua - Exécute du code Lua
     */
    private String cmd_lua(String[] args) {
        if (args.length == 0) {
            return "MiniLua 5.1 (J2ME)\n\n" +
                   "Usage:\n" +
                   "  lua -e \"code\"    Execute code\n" +
                   "  lua script.lua   Run script\n" +
                   "  lua              Interactive\n\n" +
                   "Examples:\n" +
                   "  lua -e \"print('Hello')\"\n" +
                   "  lua -e \"x = 5+3; print(x)\"\n" +
                   "  lua test.lua\n\n" +
                   "Built-in functions:\n" +
                   "  print, type, tonumber,\n" +
                   "  tostring, assert, error\n\n" +
                   "Supported:\n" +
                   "  Variables, if/then/else,\n" +
                   "  while, for, functions,\n" +
                   "  tables (basic)\n";
        }
        
        // Exécuter du code direct: lua -e "code"
        if (args[0].equals("-e")) {
            if (args.length < 2) {
                return "lua: -e requires code";
            }
            
            // Reconstituer le code
            StringBuffer code = new StringBuffer();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) code.append(" ");
                code.append(args[i]);
            }
            
            // Enlever les quotes si présentes
            String codeStr = code.toString();
            if (codeStr.startsWith("\"") && codeStr.endsWith("\"")) {
                codeStr = codeStr.substring(1, codeStr.length() - 1);
            }
            
            return luaInterpreter.execute(codeStr);
        }
        
        // Exécuter un fichier: lua script.lua
        String scriptPath = resolvePath(args[0]);
        String scriptContent = readFile(scriptPath);
        
        if (scriptContent == null) {
            return "lua: cannot open " + args[0] + ": No such file";
        }
        
        return luaInterpreter.execute(scriptContent);
    }
    
    // ========== UTILITAIRES ==========
    
    private void addHistory(String cmd) {
        if (cmd == null || cmd.length() == 0) return;
        if (commandHistory.size() > 0) {
            String last = (String) commandHistory.elementAt(commandHistory.size() - 1);
            if (last.equals(cmd)) return;
        }
        commandHistory.addElement(cmd);
        if (commandHistory.size() > 100) commandHistory.removeElementAt(0);
        historyIndex = commandHistory.size();
    }
    
    private void addAlias(String alias) {
        int eq = alias.indexOf('=');
        if (eq < 0) {
            aliases.addElement(alias);
            return;
        }
        
        String name = alias.substring(0, eq);
        
        // Remove existing
        for (int i = 0; i < aliases.size(); i++) {
            String a = (String) aliases.elementAt(i);
            if (a.startsWith(name + "=")) {
                aliases.removeElementAt(i);
                break;
            }
        }
        
        aliases.addElement(alias);
    }
    
    private String expandAlias(String cmd) {
        String[] parts = parseCommand(cmd);
        if (parts.length == 0) return cmd;
        
        for (int i = 0; i < aliases.size(); i++) {
            String alias = (String) aliases.elementAt(i);
            int eq = alias.indexOf('=');
            if (eq < 0) continue;
            
            String name = alias.substring(0, eq);
            String value = alias.substring(eq + 1);
            
            if (parts[0].equals(name)) {
                String result = value;
                for (int j = 1; j < parts.length; j++) {
                    result += " " + parts[j];
                }
                return result;
            }
        }
        
        return cmd;
    }
    
    private String expandEnvVars(String text) {
        StringBuffer result = new StringBuffer();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '$') {
                int j = i + 1;
                while (j < text.length() && isAlphaNumeric(text.charAt(j))) {
                    j++;
                }
                String var = text.substring(i + 1, j);
                String value = getEnv(var);
                result.append(value != null ? value : "");
                i = j;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }
    
    private String[] parseCommand(String cmd) {
        Vector parts = new Vector();
        StringBuffer current = new StringBuffer();
        boolean inQuotes = false;
        
        for (int i = 0; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    parts.addElement(current.toString());
                    current = new StringBuffer();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            parts.addElement(current.toString());
        }
        
        String[] result = new String[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            result[i] = (String) parts.elementAt(i);
        }
        return result;
    }
    
    private String resolvePath(String path) {
        if (path.startsWith("/")) return path;
        if (path.equals("~")) return getEnv("HOME");
        if (path.startsWith("~/")) return getEnv("HOME") + path.substring(1);
        if (path.equals("..")) {
            int lastSlash = currentDir.lastIndexOf('/');
            return lastSlash > 0 ? currentDir.substring(0, lastSlash) : "/";
        }
        if (path.startsWith("../")) {
            String parent = resolvePath("..");
            return parent + "/" + path.substring(3);
        }
        if (path.equals(".")) return currentDir;
        if (path.startsWith("./")) return currentDir + "/" + path.substring(2);
        
        return currentDir.equals("/") ? "/" + path : currentDir + "/" + path;
    }
    
    private void setEnv(String key, String value) {
        for (int i = 0; i < envVars.size(); i++) {
            String env = (String) envVars.elementAt(i);
            if (env.startsWith(key + "=")) {
                envVars.setElementAt(key + "=" + value, i);
                return;
            }
        }
        envVars.addElement(key + "=" + value);
    }
    
    private String getEnv(String key) {
        for (int i = 0; i < envVars.size(); i++) {
            String env = (String) envVars.elementAt(i);
            if (env.startsWith(key + "=")) {
                return env.substring(key.length() + 1);
            }
        }
        return null;
    }
    
    private void unsetEnv(String key) {
        for (int i = 0; i < envVars.size(); i++) {
            String env = (String) envVars.elementAt(i);
            if (env.startsWith(key + "=")) {
                envVars.removeElementAt(i);
                return;
            }
        }
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "K";
        return (bytes / (1024 * 1024)) + "M";
    }
    
    private String formatDate(long time) {
        Date d = new Date(time);
        String s = d.toString();
        if (s.length() > 16) return s.substring(4, 16);
        return s;
    }
    
    private String formatTime(long time) {
        Date d = new Date(time);
        String s = d.toString();
        if (s.length() > 19) return s.substring(11, 19);
        return s;
    }
    
    private int countLines(String text) {
        int count = text.length() > 0 ? 1 : 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') count++;
        }
        return count;
    }
    
    private int countWords(String text) {
        if (text.length() == 0) return 0;
        int count = 0;
        boolean inWord = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\n' || c == '\t') {
                inWord = false;
            } else if (!inWord) {
                count++;
                inWord = true;
            }
        }
        return count;
    }
    
    private String[] split(String s, char delim) {
        Vector parts = new Vector();
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == delim) {
                if (i > start) {
                    parts.addElement(s.substring(start, i));
                }
                start = i + 1;
            }
        }
        if (start < s.length()) {
            parts.addElement(s.substring(start));
        }
        
        String[] result = new String[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            result[i] = (String) parts.elementAt(i);
        }
        return result;
    }
    
    /**
     * Verifie si un caractere est alphanum ou underscore (CLDC 1.1 compatible)
     */
    private boolean isAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               (c >= '0' && c <= '9') || 
               c == '_';
    }
    
    // File system - utilise RealFileSystem
    private boolean fileExists(String path) {
        return realFS.fileExists(normalizePath(path));
    }
    
    private boolean isDirectory(String path) {
        return realFS.isDirectory(normalizePath(path));
    }
    
    private String readFile(String path) {
        try {
            return realFS.readRealFile(normalizePath(path));
        } catch (Exception e) {
            return null;
        }
    }
    
    private void writeFile(String path, String content) {
        try {
            realFS.writeRealFile(normalizePath(path), content);
        } catch (Exception e) {}
    }
    
    private void deleteFile(String path) {
        try {
            realFS.deleteRealFile(normalizePath(path));
        } catch (Exception e) {}
    }
    
    private void mkdir(String path) {
        try {
            realFS.mkdir(normalizePath(path));
        } catch (Exception e) {}
    }
    
    private Vector listFiles(String path) {
        try {
            return realFS.listRealFiles(normalizePath(path));
        } catch (Exception e) {
            return new Vector();
        }
    }
    
    /**
     * Normalise le chemin (enlève le / initial pour RealFileSystem)
     */
    private String normalizePath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}