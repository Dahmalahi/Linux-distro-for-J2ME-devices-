import javax.microedition.lcdui.*;
import java.util.Vector;

public class TaskManagerApp extends List implements CommandListener {
    private DiscoOs mainApp;
    private Vector tasks;
    
    private Command addCmd, deleteCmd, backCmd, detailsCmd;
    
    public TaskManagerApp(DiscoOs app) {
        super("Gestionnaire de Taches", List.IMPLICIT);
        this.mainApp = app;
        this.tasks = new Vector();
        
        // Tâches par défaut
        addTask("Terminal actif");
        addTask("Systeme");
        addTask("Affichage");
        
        addCmd = new Command("Ajouter", Command.SCREEN, 1);
        deleteCmd = new Command("Supprimer", Command.SCREEN, 2);
        detailsCmd = new Command("Details", Command.ITEM, 3);
        backCmd = new Command("Retour", Command.BACK, 4);
        
        addCommand(addCmd);
        addCommand(deleteCmd);
        addCommand(detailsCmd);
        addCommand(backCmd);
        setCommandListener(this);
    }
    
    private void addTask(String taskName) {
        tasks.addElement(taskName);
        append(taskName, null);
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == addCmd) {
            showAddTaskForm();
        } else if (c == deleteCmd) {
            int selected = getSelectedIndex();
            if (selected >= 0) {
                delete(selected);
                tasks.removeElementAt(selected);
            }
        } else if (c == detailsCmd) {
            showTaskDetails();
        } else if (c == backCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        } else if (c == List.SELECT_COMMAND) {
            showTaskDetails();
        }
    }
    
    private void showAddTaskForm() {
        Form addForm = new Form("Nouvelle Tache");
        final TextField nameField = new TextField("Nom:", "", 30, TextField.ANY);
        addForm.append(nameField);
        
        Command okCmd = new Command("OK", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        addForm.addCommand(okCmd);
        addForm.addCommand(cancelCmd);
        
        addForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String name = nameField.getString();
                    if (name.length() > 0) {
                        addTask(name);
                    }
                }
                mainApp.getDisplay().setCurrent(TaskManagerApp.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(addForm);
    }
    
    private void showTaskDetails() {
        int selected = getSelectedIndex();
        if (selected >= 0) {
            String taskName = (String) tasks.elementAt(selected);
            
            long free = Runtime.getRuntime().freeMemory();
            long total = Runtime.getRuntime().totalMemory();
            
            Alert details = new Alert("Details",
                "Tache: " + taskName + "\n" +
                "Index: " + selected + "\n" +
                "Memoire libre: " + (free/1024) + "KB\n" +
                "Memoire totale: " + (total/1024) + "KB",
                null, AlertType.INFO);
            details.setTimeout(Alert.FOREVER);
            mainApp.getDisplay().setCurrent(details, this);
        }
    }
}