import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * FileManager.java v2.1
 * Gestionnaire de fichiers avec JSR-75 et DiscoSysUsr
 */
public class FileManager extends List implements CommandListener {
    private DiscoOs mainApp;
    private String currentPath = "file:///TFCard/DiscoSysUsr/";
    private Vector fileList;
    
    private Command selectCmd, backCmd, parentCmd;
    private Command newFileCmd, newFolderCmd, deleteCmd;
    private Command copyCmd, pasteCmd, renameCmd, infoCmd;
    
    private String clipboardPath = null;
    private boolean clipboardCut = false;
    
    public FileManager(DiscoOs app) {
        super("File Manager", List.IMPLICIT);
        this.mainApp = app;
        this.fileList = new Vector();
        
        selectCmd = new Command("Open", Command.OK, 1);
        parentCmd = new Command("Parent", Command.SCREEN, 2);
        newFileCmd = new Command("New File", Command.SCREEN, 3);
        newFolderCmd = new Command("New Folder", Command.SCREEN, 4);
        deleteCmd = new Command("Delete", Command.SCREEN, 5);
        copyCmd = new Command("Copy", Command.SCREEN, 6);
        pasteCmd = new Command("Paste", Command.SCREEN, 7);
        renameCmd = new Command("Rename", Command.SCREEN, 8);
        infoCmd = new Command("Info", Command.SCREEN, 9);
        backCmd = new Command("Back", Command.BACK, 10);
        
        addCommand(selectCmd);
        addCommand(parentCmd);
        addCommand(newFileCmd);
        addCommand(newFolderCmd);
        addCommand(deleteCmd);
        addCommand(copyCmd);
        addCommand(pasteCmd);
        addCommand(renameCmd);
        addCommand(infoCmd);
        addCommand(backCmd);
        
        setCommandListener(this);
        
        refreshList();
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == selectCmd || c == List.SELECT_COMMAND) {
            openSelected();
        } else if (c == parentCmd) {
            goToParent();
        } else if (c == newFileCmd) {
            createNewFile();
        } else if (c == newFolderCmd) {
            createNewFolder();
        } else if (c == deleteCmd) {
            deleteSelected();
        } else if (c == copyCmd) {
            copySelected(false);
        } else if (c == pasteCmd) {
            pasteClipboard();
        } else if (c == renameCmd) {
            renameSelected();
        } else if (c == infoCmd) {
            showInfo();
        } else if (c == backCmd) {
            mainApp.showMainMenu();
        }
    }
    
    private void refreshList() {
        deleteAll();
        fileList.removeAllElements();
        
        setTitle("FM: " + getShortPath());
        
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath);
            
            if (fc.exists() && fc.isDirectory()) {
                Enumeration files = fc.list();
                
                // Add directories first
                while (files.hasMoreElements()) {
                    String name = (String) files.nextElement();
                    if (name.endsWith("/")) {
                        append("[DIR] " + name.substring(0, name.length() - 1), null);
                        fileList.addElement(name);
                    }
                }
                
                // Then add files
                files = fc.list();
                while (files.hasMoreElements()) {
                    String name = (String) files.nextElement();
                    if (!name.endsWith("/")) {
                        append("[FILE] " + name, null);
                        fileList.addElement(name);
                    }
                }
            }
            
            fc.close();
            
            if (size() == 0) {
                append("(Empty directory)", null);
            }
            
        } catch (Exception e) {
            append("Error: " + e.getMessage(), null);
        }
    }
    
    private void openSelected() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        String name = (String) fileList.elementAt(idx);
        
        if (name.endsWith("/")) {
            // Directory - navigate into it
            currentPath = currentPath + name;
            refreshList();
        } else {
            // File - show preview
            showFilePreview(name);
        }
    }
    
    private void showFilePreview(String filename) {
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath + filename);
            
            if (fc.exists() && !fc.isDirectory()) {
                long size = fc.fileSize();
                
                if (size > 1000) {
                    showAlert("File Info", filename + "\nSize: " + size + " bytes\nToo large to preview");
                } else {
                    InputStream is = fc.openInputStream();
                    byte[] data = new byte[(int)size];
                    is.read(data);
                    is.close();
                    
                    String content = new String(data);
                    showAlert("Preview: " + filename, content);
                }
            }
            
            fc.close();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private void goToParent() {
        if (currentPath.equals("file:///TFCard/DiscoSysUsr/")) {
            showAlert("Info", "Already at root");
            return;
        }
        
        // Remove last directory
        String temp = currentPath.substring(0, currentPath.length() - 1);
        int lastSlash = temp.lastIndexOf('/');
        if (lastSlash > 0) {
            currentPath = temp.substring(0, lastSlash + 1);
            refreshList();
        }
    }
    
    private void createNewFile() {
        TextBox nameBox = new TextBox("New File", "", 50, TextField.ANY);
        Command okCmd = new Command("Create", Command.OK, 1);
        Command cancelCmd = new Command("Cancel", Command.CANCEL, 2);
        
        nameBox.addCommand(okCmd);
        nameBox.addCommand(cancelCmd);
        
        nameBox.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getLabel().equals("Create")) {
                    String name = ((TextBox)d).getString().trim();
                    if (name.length() > 0) {
                        createFile(name);
                    }
                }
                mainApp.getDisplay().setCurrent(FileManager.this);
                refreshList();
            }
        });
        
        mainApp.getDisplay().setCurrent(nameBox);
    }
    
    private void createFile(String name) {
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath + name);
            if (!fc.exists()) {
                fc.create();
                OutputStream os = fc.openOutputStream();
                os.write("".getBytes());
                os.close();
                showAlert("Success", "File created: " + name);
            } else {
                showAlert("Error", "File already exists");
            }
            fc.close();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private void createNewFolder() {
        TextBox nameBox = new TextBox("New Folder", "", 50, TextField.ANY);
        Command okCmd = new Command("Create", Command.OK, 1);
        Command cancelCmd = new Command("Cancel", Command.CANCEL, 2);
        
        nameBox.addCommand(okCmd);
        nameBox.addCommand(cancelCmd);
        
        nameBox.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getLabel().equals("Create")) {
                    String name = ((TextBox)d).getString().trim();
                    if (name.length() > 0) {
                        createFolder(name);
                    }
                }
                mainApp.getDisplay().setCurrent(FileManager.this);
                refreshList();
            }
        });
        
        mainApp.getDisplay().setCurrent(nameBox);
    }
    
    private void createFolder(String name) {
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath + name + "/");
            if (!fc.exists()) {
                fc.mkdir();
                showAlert("Success", "Folder created: " + name);
            } else {
                showAlert("Error", "Folder already exists");
            }
            fc.close();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private void deleteSelected() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        final String name = (String) fileList.elementAt(idx);
        
        Alert confirm = new Alert("Confirm Delete", "Delete " + name + "?", null, AlertType.CONFIRMATION);
        confirm.setTimeout(Alert.FOREVER);
        confirm.addCommand(new Command("Yes", Command.OK, 1));
        confirm.addCommand(new Command("No", Command.CANCEL, 2));
        
        confirm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getLabel().equals("Yes")) {
                    performDelete(name);
                }
                mainApp.getDisplay().setCurrent(FileManager.this);
                refreshList();
            }
        });
        
        mainApp.getDisplay().setCurrent(confirm);
    }
    
    private void performDelete(String name) {
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath + name);
            if (fc.exists()) {
                fc.delete();
                showAlert("Success", "Deleted: " + name);
            }
            fc.close();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private void copySelected(boolean cut) {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        String name = (String) fileList.elementAt(idx);
        clipboardPath = currentPath + name;
        clipboardCut = cut;
        
        showAlert("Copied", name + (cut ? " (cut)" : " (copied)"));
    }
    
    private void pasteClipboard() {
        if (clipboardPath == null) {
            showAlert("Error", "Nothing to paste");
            return;
        }
        
        // Extract filename
        int lastSlash = clipboardPath.lastIndexOf('/');
        String filename = clipboardPath.substring(lastSlash + 1);
        
        try {
            FileConnection source = (FileConnection) Connector.open(clipboardPath);
            FileConnection dest = (FileConnection) Connector.open(currentPath + filename);
            
            if (dest.exists()) {
                showAlert("Error", "File already exists");
            } else {
                // Copy file
                dest.create();
                
                InputStream is = source.openInputStream();
                OutputStream os = dest.openOutputStream();
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }
                
                is.close();
                os.close();
                
                if (clipboardCut) {
                    source.delete();
                }
                
                showAlert("Success", "Pasted: " + filename);
                refreshList();
            }
            
            source.close();
            dest.close();
            
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private void renameSelected() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        final String oldName = (String) fileList.elementAt(idx);
        
        TextBox nameBox = new TextBox("Rename", oldName.endsWith("/") ? 
            oldName.substring(0, oldName.length() - 1) : oldName, 50, TextField.ANY);
        Command okCmd = new Command("Rename", Command.OK, 1);
        Command cancelCmd = new Command("Cancel", Command.CANCEL, 2);
        
        nameBox.addCommand(okCmd);
        nameBox.addCommand(cancelCmd);
        
        nameBox.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getLabel().equals("Rename")) {
                    String newName = ((TextBox)d).getString().trim();
                    if (newName.length() > 0) {
                        performRename(oldName, newName);
                    }
                }
                mainApp.getDisplay().setCurrent(FileManager.this);
                refreshList();
            }
        });
        
        mainApp.getDisplay().setCurrent(nameBox);
    }
    
    private void performRename(String oldName, String newName) {
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath + oldName);
            if (fc.exists()) {
                fc.rename(newName);
                showAlert("Success", "Renamed to: " + newName);
            }
            fc.close();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private void showInfo() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= fileList.size()) return;
        
        String name = (String) fileList.elementAt(idx);
        
        try {
            FileConnection fc = (FileConnection) Connector.open(currentPath + name);
            
            StringBuffer info = new StringBuffer();
            info.append("Name: ").append(name).append("\n");
            info.append("Type: ").append(fc.isDirectory() ? "Directory" : "File").append("\n");
            
            if (!fc.isDirectory()) {
                info.append("Size: ").append(fc.fileSize()).append(" bytes\n");
            }
            
            info.append("Path: ").append(getShortPath()).append(name);
            
            showAlert("File Info", info.toString());
            
            fc.close();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    private String getShortPath() {
        // CORRECTION CLDC 1.1: replace() non disponible
        String path = currentPath;
        if (path.startsWith("file:///TFCard/DiscoSysUsr/")) {
            path = "/" + path.substring("file:///TFCard/DiscoSysUsr/".length());
        }
        if (path.length() > 20) {
            return "..." + path.substring(path.length() - 17);
        }
        return path;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title, message, null, AlertType.INFO);
        alert.setTimeout(3000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
}