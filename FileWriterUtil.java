import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;

public class FileWriterUtil {
    // Essayer plusieurs chemins possibles
    private static final String[] PATHS = {
        "file:///TFCard/",
        "file:///SDCard/",
        "file:///root1/",
        "file:///E:/",
        "file:///MemoryCard/"
    };
    
    public static String writeFile(String filename, String content) {
        FileConnection fc = null;
        OutputStream os = null;
        
        // Essayer chaque chemin
        for (int i = 0; i < PATHS.length; i++) {
            try {
                String path = PATHS[i] + filename;
                
                fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
                
                if (!fc.exists()) {
                    fc.create();
                }
                
                os = fc.openOutputStream();
                os.write(content.getBytes());
                os.flush();
                
                String msg = "Fichier ecrit: " + path;
                
                // Fermer proprement
                if (os != null) os.close();
                if (fc != null) fc.close();
                
                return msg;
                
            } catch (Exception e) {
                // Continuer avec le prochain chemin
                try {
                    if (os != null) os.close();
                    if (fc != null) fc.close();
                } catch (Exception ex) {}
                
                // Si c'est le dernier chemin, essayer RMS
                if (i == PATHS.length - 1) {
                    return writeToRecordStore(filename, content);
                }
            }
        }
        
        return "Erreur: Impossible d'ecrire";
    }
    
    // Alternative utilisant RecordStore (toujours disponible en J2ME)
    public static String writeToRecordStore(String key, String value) {
        try {
            javax.microedition.rms.RecordStore rs = 
                javax.microedition.rms.RecordStore.openRecordStore("DiscoData", true);
            
            // Format: key:value
            String data = key + ":" + value;
            byte[] bytes = data.getBytes();
            
            // Chercher si la clé existe déjà
            javax.microedition.rms.RecordEnumeration re = rs.enumerateRecords(null, null, false);
            int recordId = -1;
            
            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                byte[] rec = rs.getRecord(id);
                String recStr = new String(rec);
                
                if (recStr.startsWith(key + ":")) {
                    recordId = id;
                    break;
                }
            }
            
            // Mettre à jour ou ajouter
            if (recordId != -1) {
                rs.setRecord(recordId, bytes, 0, bytes.length);
            } else {
                rs.addRecord(bytes, 0, bytes.length);
            }
            
            rs.closeRecordStore();
            
            return "Sauvegarde RMS: " + key;
        } catch (Exception e) {
            return "Erreur RMS: " + e.getMessage();
        }
    }
}