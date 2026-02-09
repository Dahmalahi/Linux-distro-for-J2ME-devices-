import java.util.Vector;
import java.util.Hashtable;

/**
 * MiniLua.java v2.1 CORRIGÉ COMPLET
 * Interpréteur Lua + Filesystem DiscoSysUsr
 */
public class MiniLua {
    private Hashtable globals;
    private Vector localScopes;
    private StringBuffer output;
    private DiscoTerminalCore terminal;
    private RealFileSystem realFS;
    
    public MiniLua(DiscoTerminalCore term) {
        this.terminal = term;
        try {
            this.realFS = new RealFileSystem();
        } catch (Exception e) {
            this.realFS = null;
        }
        this.globals = new Hashtable();
        this.localScopes = new Vector();
        this.output = new StringBuffer();
        initBuiltins();
    }
    
    private void initBuiltins() {
        globals.put("_VERSION", "MiniLua 5.1 (Filesystem)");
        globals.put("print", "__BUILTIN_PRINT");
        globals.put("type", "__BUILTIN_TYPE");
        globals.put("tonumber", "__BUILTIN_TONUMBER");
        globals.put("tostring", "__BUILTIN_TOSTRING");
        globals.put("readfile", "__BUILTIN_READFILE");
        globals.put("writefile", "__BUILTIN_WRITEFILE");
        globals.put("listdir", "__BUILTIN_LISTDIR");
        globals.put("mkdir", "__BUILTIN_MKDIR");
        globals.put("deletefile", "__BUILTIN_DELETEFILE");
        globals.put("fileexists", "__BUILTIN_FILEEXISTS");
        globals.put("filesize", "__BUILTIN_FILESIZE");
        globals.put("exec", "__BUILTIN_EXEC");
    }
    
    public String execute(String code) {
        output.setLength(0);
        try {
            eval(code.trim());
            return output.toString();
        } catch (Exception e) {
            return "Lua Error: " + e.getMessage();
        }
    }
    
    private Object eval(String expr) throws Exception {
        expr = expr.trim();
        if (expr.length() == 0) return null;
        if (expr.startsWith("--")) return null;
        
        int eqPos = expr.indexOf('=');
        if (eqPos > 0) {
            boolean isComp = false;
            if (eqPos > 0 && expr.charAt(eqPos - 1) == '=') isComp = true;
            if (eqPos < expr.length() - 1 && expr.charAt(eqPos + 1) == '=') isComp = true;
            if (!isComp) return evalAssignment(expr);
        }
        
        if (expr.indexOf('(') > 0) return evalFunctionCall(expr);
        if (expr.startsWith("\"") || expr.startsWith("'")) return expr.substring(1, expr.length() - 1);
        
        try {
            if (expr.indexOf('.') >= 0) return new Double(Double.parseDouble(expr));
            else return new Integer(Integer.parseInt(expr));
        } catch (NumberFormatException e) {}
        
        if (expr.equals("true")) return Boolean.TRUE;
        if (expr.equals("false")) return Boolean.FALSE;
        if (expr.equals("nil")) return null;
        
        return getVariable(expr);
    }
    
    private Object evalAssignment(String expr) throws Exception {
        int eqPos = expr.indexOf('=');
        String varName = expr.substring(0, eqPos).trim();
        String value = expr.substring(eqPos + 1).trim();
        
        boolean isLocal = varName.startsWith("local ");
        if (isLocal) varName = varName.substring(6).trim();
        
        Object val = eval(value);
        if (isLocal) setLocalVariable(varName, val);
        else globals.put(varName, val);
        return val;
    }
    
    private Object evalFunctionCall(String expr) throws Exception {
        int parenPos = expr.indexOf('(');
        String funcName = expr.substring(0, parenPos).trim();
        int lastParen = expr.lastIndexOf(')');
        String argsStr = expr.substring(parenPos + 1, lastParen).trim();
        
        Vector args = new Vector();
        if (argsStr.length() > 0) {
            String[] parts = splitArgs(argsStr);
            for (int i = 0; i < parts.length; i++) {
                args.addElement(eval(parts[i].trim()));
            }
        }
        
        return callFunction(funcName, args);
    }
    
    private Object callFunction(String name, Vector args) throws Exception {
        Object func = getVariable(name);
        if (func == null) throw new Exception("Undefined: " + name);
        
        String f = func.toString();
        if (f.equals("__BUILTIN_PRINT")) return builtinPrint(args);
        if (f.equals("__BUILTIN_TYPE")) return builtinType(args);
        if (f.equals("__BUILTIN_TONUMBER")) return builtinTonumber(args);
        if (f.equals("__BUILTIN_TOSTRING")) return builtinTostring(args);
        if (f.equals("__BUILTIN_READFILE")) return builtinReadFile(args);
        if (f.equals("__BUILTIN_WRITEFILE")) return builtinWriteFile(args);
        if (f.equals("__BUILTIN_LISTDIR")) return builtinListDir(args);
        if (f.equals("__BUILTIN_MKDIR")) return builtinMkdir(args);
        if (f.equals("__BUILTIN_DELETEFILE")) return builtinDeleteFile(args);
        if (f.equals("__BUILTIN_FILEEXISTS")) return builtinFileExists(args);
        if (f.equals("__BUILTIN_FILESIZE")) return builtinFileSize(args);
        if (f.equals("__BUILTIN_EXEC")) return builtinExec(args);
        
        throw new Exception("Unknown: " + name);
    }
    
    private Object builtinPrint(Vector args) {
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) output.append("\t");
            output.append(toString(args.elementAt(i)));
        }
        output.append("\n");
        return null;
    }
    
    private Object builtinType(Vector args) {
        if (args.size() == 0) return "nil";
        Object obj = args.elementAt(0);
        if (obj == null) return "nil";
        if (obj instanceof Integer || obj instanceof Double) return "number";
        if (obj instanceof String) return "string";
        if (obj instanceof Boolean) return "boolean";
        return "userdata";
    }
    
    private Object builtinTonumber(Vector args) {
        if (args.size() == 0) return null;
        Object obj = args.elementAt(0);
        if (obj instanceof Integer || obj instanceof Double) return obj;
        try {
            String str = obj.toString();
            if (str.indexOf('.') >= 0) return new Double(Double.parseDouble(str));
            else return new Integer(Integer.parseInt(str));
        } catch (Exception e) { return null; }
    }
    
    private Object builtinTostring(Vector args) {
        return args.size() > 0 ? toString(args.elementAt(0)) : "nil";
    }
    
    private Object builtinReadFile(Vector args) {
        if (args.size() == 0) {
            output.append("Error: readfile(path)\n");
            return null;
        }
        String path = toString(args.elementAt(0));
        try {
            String content = realFS.readRealFile(path);
            return content != null ? content : "nil";
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage()).append("\n");
            return null;
        }
    }
    
    private Object builtinWriteFile(Vector args) {
        if (args.size() < 2) {
            output.append("Error: writefile(path, content)\n");
            return Boolean.FALSE;
        }
        String path = toString(args.elementAt(0));
        String content = toString(args.elementAt(1));
        try {
            realFS.writeRealFile(path, content);
            return Boolean.TRUE;
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage()).append("\n");
            return Boolean.FALSE;
        }
    }
    
    // CORRECTION COMPLÈTE: builtinListDir
    private Object builtinListDir(Vector args) {
        if (args.size() == 0) {
            output.append("Error: listdir(path)\n");
            return null;
        }
        String path = toString(args.elementAt(0));
        try {
            // listRealFiles() retourne Vector
            Vector files = realFS.listRealFiles(path);
            if (files != null && files.size() > 0) {
                // Itérer sur le Vector et afficher chaque fichier
                for (int i = 0; i < files.size(); i++) {
                    output.append(files.elementAt(i).toString()).append("\n");
                }
                return new Integer(files.size());
            }
            return new Integer(0);
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage()).append("\n");
            return null;
        }
    }
    
    private Object builtinMkdir(Vector args) {
        if (args.size() == 0) {
            output.append("Error: mkdir(path)\n");
            return Boolean.FALSE;
        }
        String path = toString(args.elementAt(0));
        try {
            realFS.writeRealFile(path + "/.directory", "");
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }
    
    private Object builtinDeleteFile(Vector args) {
        if (args.size() == 0) return Boolean.FALSE;
        String path = toString(args.elementAt(0));
        try {
            realFS.deleteRealFile(path);
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }
    
    private Object builtinFileExists(Vector args) {
        if (args.size() == 0) return Boolean.FALSE;
        String path = toString(args.elementAt(0));
        try {
            return realFS.fileExists(path) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }
    
    private Object builtinFileSize(Vector args) {
        if (args.size() == 0) return new Integer(0);
        String path = toString(args.elementAt(0));
        try {
            return new Integer((int)realFS.getFileSize(path));
        } catch (Exception e) {
            return new Integer(0);
        }
    }
    
    private Object builtinExec(Vector args) {
        if (args.size() == 0) return null;
        String cmd = toString(args.elementAt(0));
        try {
            return terminal.executeCommand(cmd);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Object getVariable(String name) {
        for (int i = localScopes.size() - 1; i >= 0; i--) {
            Hashtable scope = (Hashtable) localScopes.elementAt(i);
            if (scope.containsKey(name)) return scope.get(name);
        }
        return globals.get(name);
    }
    
    private void setLocalVariable(String name, Object value) {
        if (localScopes.size() == 0) localScopes.addElement(new Hashtable());
        Hashtable scope = (Hashtable) localScopes.elementAt(localScopes.size() - 1);
        scope.put(name, value);
    }
    
    private String toString(Object obj) {
        return obj == null ? "nil" : obj.toString();
    }
    
    private String[] splitArgs(String args) {
        Vector parts = new Vector();
        StringBuffer current = new StringBuffer();
        boolean inString = false;
        char stringChar = 0;
        
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if ((c == '"' || c == '\'') && !inString) {
                inString = true;
                stringChar = c;
                current.append(c);
            } else if (c == stringChar && inString) {
                inString = false;
                current.append(c);
            } else if (c == ',' && !inString) {
                parts.addElement(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) parts.addElement(current.toString());
        
        String[] result = new String[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            result[i] = (String) parts.elementAt(i);
        }
        return result;
    }
    
    public void reset() {
        globals.clear();
        localScopes.removeAllElements();
        output.setLength(0);
        initBuiltins();
    }
    
    public String repl(String line) {
        output.setLength(0);
        try {
            Object result = eval(line);
            if (result != null && output.length() == 0) {
                output.append(toString(result)).append("\n");
            }
            return output.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }
}