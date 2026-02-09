import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.rms.*;
import java.io.*;

public class FileReaderUtil {
    private static final String[] PATHS = {
        "file:///SDCard/",
        "file:///MemoryCard/",
        "file:///root1/",
        "file:///E:/",
        "file:///C:/"
    };

    public static String readFile(String filename) {
        // Essayer JSR-75 d'abord
        String result = readFileJSR75(filename);
        if (result != null && !result.startsWith("Erreur")) {
            return result;
        }
        
        // Fallback RMS si JSR-75 échoue
        return readFromRecordStore(filename);
    }

    private static String readFileJSR75(String filename) {
        InputStream is = null;
        FileConnection fc = null;
        
        for (int i = 0; i < PATHS.length; i++) {
            String path = PATHS[i] + filename;
            try {
                fc = (FileConnection) Connector.open(path, Connector.READ);
                if (fc.exists() && !fc.isDirectory()) {  // CORRECTION ICI: isFile() → !isDirectory()
                    if (fc.fileSize() > 10240) { // 10KB max
                        fc.close();
                        return "Fichier trop volumineux (>10KB)";
                    }
                    
                    is = fc.openInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[512];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    String content = new String(baos.toByteArray());
                    
                    // Fermer proprement
                    if (is != null) is.close();
                    if (fc != null) fc.close();
                    return content;
                }
                if (fc != null) fc.close();
            } catch (Exception e) {
                try {
                    if (is != null) is.close();
                    if (fc != null) fc.close();
                } catch (Exception ex) {}
                // Continuer avec le prochain chemin
            }
        }
        return "Fichier non trouve: " + filename;
    }

    public static String readFromRecordStore(String key) {
        try {
            RecordStore rs = RecordStore.openRecordStore("DiscoData", true);
            if (rs.getNumRecords() == 0) {
                rs.closeRecordStore();
                return "Aucune donnee trouvee";
            }
            
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                byte[] data = rs.getRecord(id);
                String entry = new String(data);
                int sep = entry.indexOf(':');
                if (sep > 0 && entry.substring(0, sep).equals(key)) {
                    rs.closeRecordStore();
                    return entry.substring(sep + 1);
                }
            }
            rs.closeRecordStore();
            return "Cle non trouvee: " + key;
            
        } catch (Exception e) {
            return "Erreur RMS: " + e.getMessage();
        }
    }
}