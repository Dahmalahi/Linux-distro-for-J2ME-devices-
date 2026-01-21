import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;

public class HttpClient extends Form implements CommandListener, Runnable {
    private DiscoOs mainApp;
    private TextField urlField;
    private StringItem statusItem;
    private StringItem contentItem;
    
    private Command getCmd, postCmd, saveCmd, clearCmd, backCmd;
    private Thread httpThread;
    private String currentUrl = "";
    private String responseContent = "";
    
    public HttpClient(DiscoOs app) {
        super("Client HTTP");
        this.mainApp = app;
        
        urlField = new TextField("URL:", "http://", 100, TextField.URL);
        append(urlField);
        
        statusItem = new StringItem("Status:", "Pret");
        append(statusItem);
        
        contentItem = new StringItem("Reponse:", "");
        append(contentItem);
        
        getCmd = new Command("GET", Command.OK, 1);
        postCmd = new Command("POST", Command.SCREEN, 2);
        saveCmd = new Command("Sauver", Command.SCREEN, 3);
        clearCmd = new Command("Effacer", Command.SCREEN, 4);
        backCmd = new Command("Quitter", Command.BACK, 5);
        
        addCommand(getCmd);
        addCommand(postCmd);
        addCommand(saveCmd);
        addCommand(clearCmd);
        addCommand(backCmd);
        setCommandListener(this);
    }
    
    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == getCmd) {
            performGet();
        } else if (c == postCmd) {
            showPostDialog();
        } else if (c == saveCmd) {
            saveResponse();
        } else if (c == clearCmd) {
            contentItem.setText("");
            responseContent = "";
            updateStatus("Efface");
        } else if (c == backCmd) {
            mainApp.getDisplay().setCurrent(mainApp.getMainForm());
        }
    }
    
    private void performGet() {
        currentUrl = urlField.getString();
        
        if (currentUrl.length() == 0 || !currentUrl.startsWith("http")) {
            updateStatus("URL invalide");
            return;
        }
        
        updateStatus("Connexion...");
        contentItem.setText("Chargement...");
        
        httpThread = new Thread(this);
        httpThread.start();
    }
    
    public void run() {
        HttpConnection conn = null;
        InputStream is = null;
        
        try {
            conn = (HttpConnection) Connector.open(currentUrl);
            conn.setRequestMethod(HttpConnection.GET);
            
            int responseCode = conn.getResponseCode();
            updateStatus("Code: " + responseCode);
            
            if (responseCode == HttpConnection.HTTP_OK) {
                is = conn.openInputStream();
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[256];
                int bytesRead;
                
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                
                responseContent = new String(baos.toByteArray());
                
                // Limiter affichage à 500 caractères
                if (responseContent.length() > 500) {
                    contentItem.setText(responseContent.substring(0, 500) + "...\n[Tronque]");
                } else {
                    contentItem.setText(responseContent);
                }
                
                updateStatus("OK - " + responseContent.length() + " octets");
            } else {
                updateStatus("Erreur HTTP: " + responseCode);
                contentItem.setText("Erreur HTTP " + responseCode);
            }
            
        } catch (IOException e) {
            updateStatus("Erreur: " + e.getMessage());
            contentItem.setText("Erreur: " + e.getMessage());
        } finally {
            try {
                if (is != null) is.close();
                if (conn != null) conn.close();
            } catch (IOException e) {}
        }
    }
    
    private void showPostDialog() {
        Form postForm = new Form("POST Data");
        
        final TextField dataField = new TextField("Donnees:", "", 200, TextField.ANY);
        postForm.append(dataField);
        
        Command sendCmd = new Command("Envoyer", Command.OK, 1);
        Command cancelCmd = new Command("Annuler", Command.CANCEL, 2);
        
        postForm.addCommand(sendCmd);
        postForm.addCommand(cancelCmd);
        
        postForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    String data = dataField.getString();
                    performPost(data);
                }
                mainApp.getDisplay().setCurrent(HttpClient.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(postForm);
    }
    
    private void performPost(String data) {
        currentUrl = urlField.getString();
        
        if (currentUrl.length() == 0 || !currentUrl.startsWith("http")) {
            updateStatus("URL invalide");
            return;
        }
        
        HttpConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        
        try {
            conn = (HttpConnection) Connector.open(currentUrl);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            os = conn.openOutputStream();
            os.write(data.getBytes());
            os.flush();
            
            int responseCode = conn.getResponseCode();
            updateStatus("POST Code: " + responseCode);
            
            if (responseCode == HttpConnection.HTTP_OK) {
                is = conn.openInputStream();
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[256];
                int bytesRead;
                
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                
                responseContent = new String(baos.toByteArray());
                
                if (responseContent.length() > 500) {
                    contentItem.setText(responseContent.substring(0, 500) + "...");
                } else {
                    contentItem.setText(responseContent);
                }
            } else {
                contentItem.setText("Erreur POST: " + responseCode);
            }
            
        } catch (IOException e) {
            updateStatus("Erreur POST: " + e.getMessage());
            contentItem.setText("Erreur: " + e.getMessage());
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
                if (conn != null) conn.close();
            } catch (IOException e) {}
        }
    }
    
    private void saveResponse() {
        if (responseContent.length() == 0) {
            updateStatus("Rien a sauver");
            return;
        }
        
        String filename = "http_response.txt";
        String result = FileWriterUtil.writeFile(filename, responseContent);
        updateStatus("Sauve: " + filename);
        
        Alert alert = new Alert("Sauvegarde", result, null, AlertType.INFO);
        alert.setTimeout(2000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
    
    private void updateStatus(String message) {
        statusItem.setText(message);
    }
}