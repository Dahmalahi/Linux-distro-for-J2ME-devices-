import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;

/**
 * HttpClient.java v2.5 COMPLETE
 * GET/POST manual + 25 predefined APIs
 * CLDC 1.1 compatible
 */
public class HttpClient extends Form implements CommandListener, Runnable {
    private DiscoOs mainApp;
    
    // UI Components
    private ChoiceGroup modeChoice;
    private TextField urlField;
    private ChoiceGroup apiChoice;
    private StringItem statusItem;
    private StringItem contentItem;
    
    // Commands
    private Command getCmd, postCmd, apiCmd, saveCmd, clearCmd, historyCmd, backCmd;
    
    // Thread & data
    private Thread httpThread;
    private String currentUrl = "";
    private String currentMethod = "GET";
    private String postData = "";
    private String postContentType = "";
    private String responseContent = "";
    private StringBuffer requestHistory;
    private int requestCount = 0;
    
    // Constants
    private static final int BUFFER_SIZE = 512;
    private static final int MAX_DISPLAY_LENGTH = 500;
    
    // 25+ API ENDPOINTS
    private static final String[][] APIS = {
        {"Random Joke", "https://official-joke-api.appspot.com/random_joke"},
        {"Random Quote", "https://api.quotable.io/random"},
        {"Random User", "https://randomuser.me/api/"},
        {"Cat Fact", "https://catfact.ninja/fact"},
        {"Dog Image", "https://dog.ceo/api/breeds/image/random"},
        {"IP Location", "http://ip-api.com/json/"},
        {"Public IP", "https://api.ipify.org?format=json"},
        {"ISS Position", "http://api.open-notify.org/iss-now.json"},
        {"Sunrise/Sunset", "https://api.sunrise-sunset.org/json?lat=0&lng=0"},
        {"Bored Activity", "https://www.boredapi.com/api/activity"},
        {"Random Advice", "https://api.adviceslip.com/advice"},
        {"Number Fact", "http://numbersapi.com/random/trivia"},
        {"Yes or No", "https://yesno.wtf/api"},
        {"Chuck Norris", "https://api.chucknorris.io/jokes/random"},
        {"Kanye Quote", "https://api.kanye.rest/"},
        {"Hacker News", "https://hacker-news.firebaseio.com/v0/topstories.json"},
        {"GitHub Trending", "https://api.github.com/search/repositories?q=stars:>1&sort=stars"},
        {"Bitcoin Price", "https://api.coindesk.com/v1/bpi/currentprice.json"},
        {"Crypto Prices", "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum&vs_currencies=usd"},
        {"Exchange Rates", "https://api.exchangerate-api.com/v4/latest/USD"},
        {"UUID v4", "https://www.uuidtools.com/api/generate/v4"},
        {"User Agent", "https://httpbin.org/user-agent"},
        {"HTTP Headers", "https://httpbin.org/headers"},
        {"UTC Time", "http://worldtimeapi.org/api/timezone/Etc/UTC"},
        {"JSON Test", "https://jsonplaceholder.typicode.com/todos/1"}
    };
    
    public HttpClient(DiscoOs app) {
        super("HTTP Client v2.5");
        this.mainApp = app;
        this.requestHistory = new StringBuffer();
        
        // Mode selection
        modeChoice = new ChoiceGroup("Mode:", Choice.EXCLUSIVE);
        modeChoice.append("Manual URL", null);
        modeChoice.append("Predefined APIs", null);
        modeChoice.setSelectedIndex(0, true);
        append(modeChoice);
        
        // Manual URL field
        urlField = new TextField("URL:", "http://", 100, TextField.URL);
        append(urlField);
        
        // API selection
        apiChoice = new ChoiceGroup("Select API:", Choice.EXCLUSIVE);
        for (int i = 0; i < APIS.length; i++) {
            apiChoice.append(APIS[i][0], null);
        }
        append(apiChoice);
        
        // Status
        statusItem = new StringItem("Status:", "Ready");
        append(statusItem);
        
        // Response
        contentItem = new StringItem("Response:", "");
        append(contentItem);
        
        // Commands
        getCmd = new Command("GET", Command.OK, 1);
        postCmd = new Command("POST", Command.SCREEN, 2);
        apiCmd = new Command("API Request", Command.SCREEN, 3);
        saveCmd = new Command("Save", Command.SCREEN, 4);
        clearCmd = new Command("Clear", Command.SCREEN, 5);
        historyCmd = new Command("History", Command.SCREEN, 6);
        backCmd = new Command("Back", Command.BACK, 7);
        
        addCommand(getCmd);
        addCommand(postCmd);
        addCommand(apiCmd);
        addCommand(saveCmd);
        addCommand(clearCmd);
        addCommand(historyCmd);
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
        } else if (c == apiCmd) {
            performApiRequest();
        } else if (c == saveCmd) {
            saveResponse();
        } else if (c == clearCmd) {
            clearResponse();
        } else if (c == historyCmd) {
            showHistory();
        } else if (c == backCmd) {
            mainApp.showMainMenu();
        }
    }
    
    private void clearResponse() {
        contentItem.setText("");
        responseContent = "";
        statusItem.setText("Cleared");
    }
    
    private void performGet() {
        int mode = modeChoice.getSelectedIndex();
        
        if (mode == 0) {
            // Manual URL
            currentUrl = urlField.getString().trim();
        } else {
            // Predefined API
            int selected = apiChoice.getSelectedIndex();
            currentUrl = APIS[selected][1];
        }
        
        if (!isValidUrl(currentUrl)) {
            statusItem.setText("Invalid URL");
            showAlert("Error", "Please enter a valid URL (http://...)", AlertType.ERROR);
            return;
        }
        
        if (httpThread != null && httpThread.isAlive()) {
            statusItem.setText("Request in progress...");
            return;
        }
        
        currentMethod = "GET";
        statusItem.setText("Connecting...");
        contentItem.setText("Loading...");
        
        httpThread = new Thread(this);
        httpThread.start();
    }
    
    private void performApiRequest() {
        modeChoice.setSelectedIndex(1, true);
        performGet();
    }
    
    private void showPostDialog() {
        Form postForm = new Form("POST Data");
        
        final TextField dataField = new TextField("Data:", "", 200, TextField.ANY);
        final TextField contentTypeField = new TextField("Content-Type:", 
            "application/x-www-form-urlencoded", 50, TextField.ANY);
        
        postForm.append(dataField);
        postForm.append(contentTypeField);
        postForm.append(new StringItem("", "\nExample: name=value&key=data"));
        
        Command sendCmd = new Command("Send", Command.OK, 1);
        Command cancelCmd = new Command("Cancel", Command.CANCEL, 2);
        
        postForm.addCommand(sendCmd);
        postForm.addCommand(cancelCmd);
        
        postForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    postData = dataField.getString();
                    postContentType = contentTypeField.getString();
                    performPost();
                }
                mainApp.getDisplay().setCurrent(HttpClient.this);
            }
        });
        
        mainApp.getDisplay().setCurrent(postForm);
    }
    
    private void performPost() {
        currentUrl = urlField.getString().trim();
        
        if (!isValidUrl(currentUrl)) {
            statusItem.setText("Invalid URL");
            showAlert("Error", "Please enter a valid URL", AlertType.ERROR);
            return;
        }
        
        if (postData == null || postData.length() == 0) {
            statusItem.setText("No data");
            showAlert("Warning", "No data to send", AlertType.WARNING);
            return;
        }
        
        currentMethod = "POST";
        statusItem.setText("Sending...");
        contentItem.setText("Posting data...");
        
        httpThread = new Thread(this);
        httpThread.start();
    }
    
    public void run() {
        HttpConnection conn = null;
        InputStream is = null;
        OutputStream os = null;
        long startTime = System.currentTimeMillis();
        
        try {
            conn = (HttpConnection) Connector.open(currentUrl);
            conn.setRequestMethod(currentMethod);
            conn.setRequestProperty("User-Agent", "DiscoLinux/2.5");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "close");
            
            // Handle POST data
            if (currentMethod.equals("POST") && postData.length() > 0) {
                conn.setRequestProperty("Content-Type", postContentType);
                byte[] data = postData.getBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                
                os = conn.openOutputStream();
                os.write(data);
                os.flush();
            }
            
            int responseCode = conn.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            
            statusItem.setText("Code: " + responseCode + " (" + responseTime + "ms)");
            
            if (responseCode == HttpConnection.HTTP_OK || 
                responseCode == HttpConnection.HTTP_CREATED ||
                responseCode == HttpConnection.HTTP_ACCEPTED) {
                
                is = conn.openInputStream();
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                int totalBytes = 0;
                
                while ((bytesRead = is.read(buffer)) != -1 && totalBytes < 4096) {
                    baos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                
                responseContent = new String(baos.toByteArray());
                
                // Format and display
                String formatted = formatJson(responseContent);
                if (formatted.length() > MAX_DISPLAY_LENGTH) {
                    formatted = formatted.substring(0, MAX_DISPLAY_LENGTH) + "...";
                }
                
                contentItem.setText(formatted);
                statusItem.setText("OK - " + totalBytes + " bytes");
                
                // Add to history
                addToHistory(currentMethod, currentUrl, responseCode, totalBytes);
                
            } else {
                handleHttpError(responseCode);
            }
            
        } catch (IOException e) {
            statusItem.setText("Error: " + e.getMessage());
            contentItem.setText("Connection failed:\n" + e.getMessage());
        } catch (Exception e) {
            statusItem.setText("Error: " + e.toString());
            contentItem.setText("Unexpected error: " + e.getMessage());
        } finally {
            closeResources(is, os, conn);
        }
    }
    
    private boolean isValidUrl(String url) {
        return url != null && url.length() > 0 && 
               (url.startsWith("http://") || url.startsWith("https://"));
    }
    
    /**
     * Format JSON manually (CLDC 1.1 compatible)
     */
    private String formatJson(String json) {
        if (json == null || json.length() == 0) return "";
        
        StringBuffer formatted = new StringBuffer();
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            formatted.append(c);
            
            if (c == '{' || c == ',') {
                formatted.append('\n');
            }
        }
        
        return formatted.toString();
    }
    
    private void handleHttpError(int code) {
        String errorMsg = "HTTP Error: " + code;
        
        switch (code) {
            case HttpConnection.HTTP_BAD_REQUEST:
                errorMsg += " (Bad Request)";
                break;
            case HttpConnection.HTTP_UNAUTHORIZED:
                errorMsg += " (Unauthorized)";
                break;
            case HttpConnection.HTTP_FORBIDDEN:
                errorMsg += " (Forbidden)";
                break;
            case HttpConnection.HTTP_NOT_FOUND:
                errorMsg += " (Not Found)";
                break;
            case HttpConnection.HTTP_INTERNAL_ERROR:
                errorMsg += " (Server Error)";
                break;
            case HttpConnection.HTTP_UNAVAILABLE:
                errorMsg += " (Service Unavailable)";
                break;
        }
        
        statusItem.setText(errorMsg);
        contentItem.setText(errorMsg);
    }
    
    private void addToHistory(String method, String url, int code, int bytes) {
        requestCount++;
        if (requestCount <= 10) {
            requestHistory.append(requestCount).append(". ");
            requestHistory.append(method).append(" ");
            requestHistory.append(url).append(" -> ");
            requestHistory.append(code).append(" (");
            requestHistory.append(bytes).append(" bytes)\n");
        }
    }
    
    private void showHistory() {
        if (requestHistory.length() == 0) {
            showAlert("History", "No requests yet", AlertType.INFO);
        } else {
            Alert historyAlert = new Alert("Request History", 
                requestHistory.toString(), null, AlertType.INFO);
            historyAlert.setTimeout(Alert.FOREVER);
            mainApp.getDisplay().setCurrent(historyAlert, this);
        }
    }
    
    private void saveResponse() {
        if (responseContent == null || responseContent.length() == 0) {
            statusItem.setText("Nothing to save");
            showAlert("Info", "No response to save", AlertType.INFO);
            return;
        }
        
        statusItem.setText("Saved (" + responseContent.length() + " bytes)");
        showAlert("Success", "Response saved to memory\n" + 
            responseContent.length() + " bytes", AlertType.INFO);
    }
    
    private void closeResources(InputStream is, OutputStream os, HttpConnection conn) {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
        if (os != null) {
            try { os.close(); } catch (Exception e) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (Exception e) {}
        }
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(title, message, null, type);
        alert.setTimeout(3000);
        mainApp.getDisplay().setCurrent(alert, this);
    }
}