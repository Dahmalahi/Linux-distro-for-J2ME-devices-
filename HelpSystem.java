/**
 * HelpSystem.java
 * 
 * Système d'aide en 5 sections optimisé pour
 * écran 240x320
 * 
 * Commandes:
 * help     - Menu principal
 * help1    - Fichiers
 * help2    - Texte
 * help3    - Système
 * help4    - Réseau
 * help5    - Shell
 * man CMD  - Manuel détaillé
 * info CMD - Info rapide
 */
public class HelpSystem {
    
    /**
     * Aide principale - Menu des catégories
     */
    public static String getMainHelp() {
        return "=== DISCOLINUX HELP ===\n\n" +
               "CATEGORIES:\n" +
               "  help1 - Files\n" +
               "  help2 - Text\n" +
               "  help3 - System\n" +
               "  help4 - Network\n" +
               "  help5 - Shell\n\n" +
               "USAGE:\n" +
               "  man CMD  - Manual\n" +
               "  info CMD - Quick info\n\n" +
               "Type category name\n" +
               "for command list";
    }
    
    /**
     * HELP1 - Commandes fichiers
     */
    public static String getFileHelp() {
        return "=== FILES (help1) ===\n\n" +
               "BASIC:\n" +
               "ls    List files\n" +
               "cd    Change dir\n" +
               "pwd   Print dir\n" +
               "cat   Show file\n\n" +
               "CREATE/DELETE:\n" +
               "touch Create file\n" +
               "mkdir Make dir\n" +
               "rm    Remove\n" +
               "rmdir Remove dir\n\n" +
               "COPY/MOVE:\n" +
               "cp    Copy\n" +
               "mv    Move/rename\n\n" +
               "SEARCH:\n" +
               "find  Find files\n" +
               "tree  Show tree\n" +
               "du    Disk usage\n" +
               "df    Disk free\n\n" +
               "Type 'man ls' for\n" +
               "detailed help";
    }
    
    /**
     * HELP2 - Commandes texte
     */
    public static String getTextHelp() {
        return "=== TEXT (help2) ===\n\n" +
               "DISPLAY:\n" +
               "echo  Print text\n" +
               "cat   Show file\n" +
               "head  First lines\n" +
               "tail  Last lines\n\n" +
               "SEARCH/FILTER:\n" +
               "grep  Find pattern\n" +
               "wc    Count words\n" +
               "sort  Sort lines\n" +
               "uniq  Remove dups\n\n" +
               "EDIT:\n" +
               "cut   Cut columns\n" +
               "tr    Translate\n" +
               "rev   Reverse\n\n" +
               "Type 'man grep'\n" +
               "for examples";
    }
    
    /**
     * HELP3 - Commandes système
     */
    public static String getSystemHelp() {
        return "=== SYSTEM (help3) ===\n\n" +
               "PROCESS:\n" +
               "ps      List proc\n" +
               "top     Monitor\n" +
               "kill    Kill proc\n\n" +
               "INFO:\n" +
               "date    Date/time\n" +
               "uptime  Uptime\n" +
               "uname   System info\n" +
               "whoami  Username\n" +
               "hostname Hostname\n" +
               "id      User ID\n\n" +
               "MEMORY:\n" +
               "free    Free RAM\n" +
               "df      Disk space\n\n" +
               "POWER:\n" +
               "reboot  Reboot\n" +
               "shutdown Shutdown\n\n" +
               "Type 'man ps'";
    }
    
    /**
     * HELP4 - Commandes réseau
     */
    public static String getNetworkHelp() {
        return "=== NETWORK (help4) ===\n\n" +
               "TEST:\n" +
               "ping     Test host\n" +
               "ifconfig Network cfg\n" +
               "netstat  Connections\n\n" +
               "DOWNLOAD:\n" +
               "wget     Download\n" +
               "curl     HTTP client\n\n" +
               "APPS:\n" +
               "http     HTTP app\n\n" +
               "EXAMPLES:\n" +
               "ping google.com\n" +
               "wget http://url\n" +
               "curl -X GET url\n\n" +
               "Type 'man ping'\n" +
               "for options";
    }
    
    /**
     * HELP5 - Commandes shell
     */
    public static String getShellHelp() {
        return "=== SHELL (help5) ===\n\n" +
               "NAVIGATION:\n" +
               "history  Command log\n" +
               "clear    Clear screen\n" +
               "exit     Quit\n\n" +
               "ALIAS:\n" +
               "alias    Set alias\n" +
               "unalias  Remove\n\n" +
               "ENV:\n" +
               "env      Variables\n" +
               "export   Set var\n" +
               "unset    Remove var\n" +
               "set      Show all\n\n" +
               "SEARCH:\n" +
               "which    Find cmd\n" +
               "whereis  Locate\n\n" +
               "HELP:\n" +
               "help     This menu\n" +
               "man      Manual\n" +
               "info     Quick info";
    }
    
    /**
     * Manuel détaillé d'une commande
     */
    public static String getManPage(String cmd) {
        if (cmd == null || cmd.length() == 0) {
            return "man: No command\n" +
                   "Usage: man COMMAND\n\n" +
                   "Try: man ls";
        }
        
        cmd = cmd.toLowerCase();
        
        // FICHIERS
        if (cmd.equals("ls")) {
            return "LS(1) - List files\n\n" +
                   "SYNOPSIS:\n" +
                   "  ls [OPTIONS] [DIR]\n\n" +
                   "OPTIONS:\n" +
                   "  -l  Long format\n" +
                   "  -a  Show hidden\n" +
                   "  -la Both\n\n" +
                   "EXAMPLES:\n" +
                   "  ls\n" +
                   "  ls -la\n" +
                   "  ls /home\n" +
                   "  ls -l /etc";
        }
        
        if (cmd.equals("cd")) {
            return "CD(1) - Change dir\n\n" +
                   "SYNOPSIS:\n" +
                   "  cd [DIRECTORY]\n\n" +
                   "SPECIAL:\n" +
                   "  cd      Go home\n" +
                   "  cd ..   Parent dir\n" +
                   "  cd /    Root\n" +
                   "  cd ~    Home\n\n" +
                   "EXAMPLES:\n" +
                   "  cd /home/user\n" +
                   "  cd ..\n" +
                   "  cd Documents";
        }
        
        if (cmd.equals("cat")) {
            return "CAT(1) - Show file\n\n" +
                   "SYNOPSIS:\n" +
                   "  cat FILE...\n\n" +
                   "DESCRIPTION:\n" +
                   "  Display file(s)\n\n" +
                   "EXAMPLES:\n" +
                   "  cat file.txt\n" +
                   "  cat f1 f2 f3\n" +
                   "  cat /etc/motd\n" +
                   "  cat readme.txt";
        }
        
        if (cmd.equals("mkdir")) {
            return "MKDIR(1) - Make dir\n\n" +
                   "SYNOPSIS:\n" +
                   "  mkdir DIRECTORY\n\n" +
                   "EXAMPLES:\n" +
                   "  mkdir docs\n" +
                   "  mkdir /tmp/test\n" +
                   "  mkdir folder";
        }
        
        if (cmd.equals("rm")) {
            return "RM(1) - Remove file\n\n" +
                   "SYNOPSIS:\n" +
                   "  rm FILE...\n\n" +
                   "WARNING:\n" +
                   "  Cannot undo!\n\n" +
                   "EXAMPLES:\n" +
                   "  rm file.txt\n" +
                   "  rm f1 f2\n" +
                   "  rm old.log";
        }
        
        if (cmd.equals("cp")) {
            return "CP(1) - Copy file\n\n" +
                   "SYNOPSIS:\n" +
                   "  cp SRC DEST\n\n" +
                   "EXAMPLES:\n" +
                   "  cp a.txt b.txt\n" +
                   "  cp file.txt\n" +
                   "     /tmp/\n" +
                   "  cp old new";
        }
        
        if (cmd.equals("mv")) {
            return "MV(1) - Move/rename\n\n" +
                   "SYNOPSIS:\n" +
                   "  mv SRC DEST\n\n" +
                   "USAGE:\n" +
                   "  Rename or move\n\n" +
                   "EXAMPLES:\n" +
                   "  mv old new\n" +
                   "  mv file.txt\n" +
                   "     /home/\n" +
                   "  mv a.txt b.txt";
        }
        
        if (cmd.equals("find")) {
            return "FIND(1) - Find files\n\n" +
                   "SYNOPSIS:\n" +
                   "  find [PATH]\n" +
                   "    -name PATTERN\n\n" +
                   "PATTERNS:\n" +
                   "  *       Any\n" +
                   "  *.txt   Extension\n" +
                   "  file*   Starts\n\n" +
                   "EXAMPLES:\n" +
                   "  find / -name\n" +
                   "    \"*.txt\"\n" +
                   "  find /home\n" +
                   "    -name readme";
        }
        
        // TEXTE
        if (cmd.equals("grep")) {
            return "GREP(1) - Find text\n\n" +
                   "SYNOPSIS:\n" +
                   "  grep PATTERN FILE\n\n" +
                   "DESCRIPTION:\n" +
                   "  Search pattern\n" +
                   "  in file\n\n" +
                   "EXAMPLES:\n" +
                   "  grep error log\n" +
                   "  grep \"test\"\n" +
                   "     file.txt\n" +
                   "  grep user\n" +
                   "     /etc/passwd";
        }
        
        if (cmd.equals("wc")) {
            return "WC(1) - Count words\n\n" +
                   "SYNOPSIS:\n" +
                   "  wc [OPTIONS] FILE\n\n" +
                   "OPTIONS:\n" +
                   "  -l  Lines\n" +
                   "  -w  Words\n" +
                   "  -c  Chars\n\n" +
                   "EXAMPLES:\n" +
                   "  wc file.txt\n" +
                   "  wc -l log.txt\n" +
                   "  wc -w doc.txt";
        }
        
        if (cmd.equals("sort")) {
            return "SORT(1) - Sort lines\n\n" +
                   "SYNOPSIS:\n" +
                   "  sort FILE\n\n" +
                   "DESCRIPTION:\n" +
                   "  Sort alphabetic\n\n" +
                   "EXAMPLES:\n" +
                   "  sort names.txt\n" +
                   "  sort data.csv";
        }
        
        // SYSTÈME
        if (cmd.equals("ps")) {
            return "PS(1) - List process\n\n" +
                   "SYNOPSIS:\n" +
                   "  ps [OPTIONS]\n\n" +
                   "OPTIONS:\n" +
                   "  -aux  All users\n" +
                   "  -ef   Full list\n\n" +
                   "EXAMPLES:\n" +
                   "  ps\n" +
                   "  ps -aux";
        }
        
        if (cmd.equals("ping")) {
            return "PING(1) - Test host\n\n" +
                   "SYNOPSIS:\n" +
                   "  ping HOST\n\n" +
                   "DESCRIPTION:\n" +
                   "  Test network\n\n" +
                   "EXAMPLES:\n" +
                   "  ping google.com\n" +
                   "  ping 8.8.8.8\n" +
                   "  ping localhost";
        }
        
        if (cmd.equals("alias")) {
            return "ALIAS(1) - Shortcut\n\n" +
                   "SYNOPSIS:\n" +
                   "  alias NAME=CMD\n\n" +
                   "DESCRIPTION:\n" +
                   "  Create shortcut\n\n" +
                   "EXAMPLES:\n" +
                   "  alias ll=\"ls -la\"\n" +
                   "  alias h=history\n" +
                   "  alias c=clear\n\n" +
                   "VIEW ALL:\n" +
                   "  alias";
        }
        
        // Commande inconnue
        return "No manual for\n'" + cmd + "'\n\n" +
               "Try:\n" +
               "  help  - Categories\n" +
               "  help1 - Files\n" +
               "  help2 - Text\n" +
               "  help3 - System\n" +
               "  help4 - Network\n" +
               "  help5 - Shell";
    }
    
    /**
     * Info rapide d'une commande
     */
    public static String getInfo(String cmd) {
        if (cmd == null || cmd.length() == 0) {
            return "info: No command\n" +
                   "Usage: info CMD\n\n" +
                   "Try: info ls";
        }
        
        cmd = cmd.toLowerCase();
        
        if (cmd.equals("ls")) return "ls - List files\nUsage: ls [-la] [dir]";
        if (cmd.equals("cd")) return "cd - Change dir\nUsage: cd [dir]";
        if (cmd.equals("pwd")) return "pwd - Print dir\nUsage: pwd";
        if (cmd.equals("cat")) return "cat - Show file\nUsage: cat FILE";
        if (cmd.equals("mkdir")) return "mkdir - Make dir\nUsage: mkdir NAME";
        if (cmd.equals("rm")) return "rm - Remove\nUsage: rm FILE";
        if (cmd.equals("cp")) return "cp - Copy\nUsage: cp SRC DEST";
        if (cmd.equals("mv")) return "mv - Move\nUsage: mv SRC DEST";
        if (cmd.equals("grep")) return "grep - Find text\nUsage: grep PAT FILE";
        if (cmd.equals("find")) return "find - Find files\nUsage: find -name PAT";
        if (cmd.equals("ps")) return "ps - Processes\nUsage: ps [-aux]";
        if (cmd.equals("ping")) return "ping - Test host\nUsage: ping HOST";
        if (cmd.equals("top")) return "top - Monitor\nUsage: top";
        if (cmd.equals("free")) return "free - Free RAM\nUsage: free [-h]";
        if (cmd.equals("df")) return "df - Disk space\nUsage: df [-h]";
        if (cmd.equals("alias")) return "alias - Shortcut\nUsage: alias N=CMD";
        if (cmd.equals("history")) return "history - History\nUsage: history [N]";
        if (cmd.equals("env")) return "env - Variables\nUsage: env";
        if (cmd.equals("export")) return "export - Set var\nUsage: export V=val";
        
        return "No info for '" + cmd + "'\nTry: man " + cmd;
    }
}
