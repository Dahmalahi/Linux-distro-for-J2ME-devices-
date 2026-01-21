import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class FileReaderUtil {
    private static final String[] PATHS = {
        "file:///TFCard/",
        "file:///SDCard/",
        "file:///root1/",
        "file:///E:/",
        "file:///MemoryCard/"
    };
    
    public static String readFile(String filename) {
        FileConnection fc = null;
        InputStream is = null;
        
        // Essayer chaque chemin
        for (int i = 0; i < PATHS.length; i++) {
            try {
                String path = PATHS[i] + filename;
                fc = (FileConnection) Connector.open(path, Connector.READ);
                
                if (!fc.exists()) {
                    try {
                        if (fc != null) fc.close();
                    } catch (Exception ex) {}
                    continue; // Essayer le prochain chemin
                }
                
                long fileSize = fc.fileSize();
                if (fileSize > 10240) { // 10KB max
                    if (fc != null) fc.close();
                    return "Fichier trop volumineux (>10KB)";
                }
                
                is = fc.openInputStream();
                byte[] data = new byte[(int) fileSize];
                int bytesRead = is.read(data);
                
                String content = new String(data, 0, bytesRead);
                
                // Fermer proprement
                if (is != null) is.close();
                if (fc != null) fc.close();
                
                return content;
                
            } catch (Exception e) {
                try {
                    if (is != null) is.close();
                    if (fc != null) fc.close();
                } catch (Exception ex) {}
                
                // Si c'est le dernier chemin, essayer RMS
                if (i == PATHS.length - 1) {
                    return readFromRecordStore(filename);
                }
            }
        }
        
        return "Fichier non trouve: " + filename;
    }
    
    // Alternative RecordStore
    public static String readFromRecordStore(String key) {
        try {
            javax.microedition.rms.RecordStore rs = 
                javax.microedition.rms.RecordStore.openRecordStore("DiscoData", false);
            
            if (rs.getNumRecords() == 0) {
                rs.closeRecordStore();
                return "Aucune donnee trouvee";
            }
            
            // Chercher la clé
            javax.microedition.rms.RecordEnumeration re = rs.enumerateRecords(null, null, false);
            
            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                byte[] rec = rs.getRecord(id);
                String recStr = new String(rec);
                
                if (recStr.startsWith(key + ":")) {
                    rs.closeRecordStore();
                    // Extraire la valeur après ":"
                    int colonIndex = recStr.indexOf(':');
                    if (colonIndex != -1 && colonIndex < recStr.length() - 1) {
                        return recStr.substring(colonIndex + 1);
                    }
                    return recStr;
                }
            }
            
            rs.closeRecordStore();
            return "Cle non trouvee: " + key;
            
        } catch (Exception e) {
            return "Erreur lecture RMS: " + e.getMessage();
        }
    }
}