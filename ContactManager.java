import javax.microedition.lcdui.*;
import java.util.Vector;

public class ContactManager extends List implements CommandListener {
    private DiscoOs mainApp;
    private Vector contacts;
    
    private Command addCmd, viewCmd, editCmd, deleteCmd, searchCmd, backCmd;
    
    public ContactManager(DiscoOs app) {
        super("Contacts", List.IMPLICIT);
        this.mainApp = app;
        this.contacts = new Vector();
        
        // Contacts par défaut
        contacts.addElement(new Contact("Alice Martin", "0612345678", "alice@email.com"));
        contacts.addElement(new Contact("Bob Dupont", "0698765432", "bob@email.com"));
        contacts.addElement(new Contact("Charlie Bernard", "0611223344", "charlie@email.com"));
        
        refreshList();
        
        addCmd = new Command("Ajouter", Command.SCREEN, 1);
        viewCmd = new Command("Voir", Command.ITEM, 2);
        editCmd = new Command("Modifier", Command.SCREEN, 3);
        deleteCmd = new Command("Supprimer", Command.SCREEN, 4);
        searchCmd = new Command("Chercher", Command.SCREEN, 5);
        backCmd = new Command("Retour", Command.BACK, 6);
        
        addCommand(addCmd);
        addCommand(viewCmd);
        addCommand(editCmd);
        addCommand(deleteCmd);
        addCommand(searchCmd);
        addCommand(backCmd);
        setCommandListener(this);
    }
    
    private void refreshList() {
        deleteAll();
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = (Contact) contacts.elementAt(i);
            append(c.name, null);
        }
        
        if (contacts.size() == 0) {
            append("(Aucun contact)", null);
        }
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == addCmd) {
            showAddForm();
        } else if (c == viewCmd || c == List.SELECT_COMMAND) {
            viewContact();
        } else if (c == editCmd) {
            editContact();
        } else if (c == deleteCmd) {
            deleteContact();
        } else if (c == searchCmd) {
            showSearchForm();
        } else if (c == backCmd) {
            saveContacts();
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
    
    private void showAddForm() {
        Form form = new Form("Nouveau Contact");
        
        final TextField nameField = new TextField("Nom:", "", 50, TextField.ANY);
        final TextField phoneField = new TextField("Telephone:", "", 20, TextField.PHONENUMBER);
        final TextField emailField = new TextField("Email:", "", 50, TextField.EMAILADDR);
        
        form.append(nameField);
        form.append(phoneField);
        form.append(emailField);
        
        Command okCmd = new Command("Sauver", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        form.addCommand(okCmd);
        form.addCommand(cancelCmd);
        
        form.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String name = nameField.getString();
                    String phone = phoneField.getString();
                    String email = emailField.getString();
                    
                    if (name.length() > 0) {
                        contacts.addElement(new Contact(name, phone, email));
                        refreshList();
                        showAlert("Contact ajoute");
                    } else {
                        showAlert("Nom requis!");
                    }
                }
                mainApp.getDisplay().setCurrent(ContactManager.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(form);
    }
    
    private void viewContact() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= contacts.size()) return;
        
        Contact c = (Contact) contacts.elementAt(idx);
        
        Alert alert = new Alert("Contact");
        alert.setString("Nom: " + c.name + "\n" +
                       "Tel: " + c.phone + "\n" +
                       "Email: " + c.email);
        alert.setTimeout(Alert.FOREVER);
        alert.setType(AlertType.INFO);
        
        mainApp.getDisplay().setCurrent(alert, this);
    }
    
    private void editContact() {
        final int idx = getSelectedIndex();
        if (idx < 0 || idx >= contacts.size()) return;
        
        final Contact contact = (Contact) contacts.elementAt(idx);
        
        Form form = new Form("Modifier Contact");
        
        final TextField nameField = new TextField("Nom:", contact.name, 50, TextField.ANY);
        final TextField phoneField = new TextField("Telephone:", contact.phone, 20, TextField.PHONENUMBER);
        final TextField emailField = new TextField("Email:", contact.email, 50, TextField.EMAILADDR);
        
        form.append(nameField);
        form.append(phoneField);
        form.append(emailField);
        
        Command okCmd = new Command("Sauver", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        form.addCommand(okCmd);
        form.addCommand(cancelCmd);
        
        form.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    contact.name = nameField.getString();
                    contact.phone = phoneField.getString();
                    contact.email = emailField.getString();
                    
                    refreshList();
                    showAlert("Contact modifie");
                }
                mainApp.getDisplay().setCurrent(ContactManager.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(form);
    }
    
    private void deleteContact() {
        int idx = getSelectedIndex();
        if (idx < 0 || idx >= contacts.size()) return;
        
        Contact c = (Contact) contacts.elementAt(idx);
        
        Form confirmForm = new Form("Supprimer?");
        confirmForm.append("Supprimer le contact:\n" + c.name + "?");
        
        Command yesCmd = new Command("Oui", Command.OK, 1);
        Command noCmd = new Command("Non", Command.CANCEL, 2);
        
        confirmForm.addCommand(yesCmd);
        confirmForm.addCommand(noCmd);
        
        final int deleteIdx = idx;
        confirmForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    contacts.removeElementAt(deleteIdx);
                    refreshList();
                    showAlert("Contact supprime");
                }
                mainApp.getDisplay().setCurrent(ContactManager.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(confirmForm);
    }
    
    private void showSearchForm() {
        Form form = new Form("Rechercher");
        
        final TextField searchField = new TextField("Nom:", "", 50, TextField.ANY);
        form.append(searchField);
        
        Command okCmd = new Command("Chercher", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        form.addCommand(okCmd);
        form.addCommand(cancelCmd);
        
        form.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String query = searchField.getString().toLowerCase();
                    searchContacts(query);
                }
                mainApp.getDisplay().setCurrent(ContactManager.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(form);
    }
    
    private void searchContacts(String query) {
        deleteAll();
        int found = 0;
        
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = (Contact) contacts.elementAt(i);
            if (c.name.toLowerCase().indexOf(query) >= 0) {
                append(c.name, null);
                found++;
            }
        }
        
        if (found == 0) {
            append("(Aucun resultat)", null);
        }
        
        showAlert(found + " contact(s) trouve(s)");
    }
    
    private void saveContacts() {
        StringBuffer data = new StringBuffer();
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = (Contact) contacts.elementAt(i);
            data.append(c.name + ";" + c.phone + ";" + c.email + "\n");
        }
        FileWriterUtil.writeFile("contacts.dat", data.toString());
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert("Info", message, null, AlertType.INFO);
        alert.setTimeout(2000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
}

class Contact {
    String name;
    String phone;
    String email;
    
    public Contact(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
}