import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.rms.*;
import java.io.*;

public class FileWriterUtil {
    private static final String[] PATHS = {
        "file:///SDCard/",
        "file:///MemoryCard/",
        "file:///root1/",
        "file:///E:/",
        "file:///C:/"
    };

    public static String writeFile(String filename, String content) {
        // Essayer JSR-75 d'abord
        String result = writeFileJSR75(filename, content);
        if (result != null && !result.startsWith("Erreur")) {
            // Sauvegarder aussi en RMS pour backup
            writeToRecordStore(filename, content);
            return result;
        }
        
        // Fallback RMS si JSR-75 échoue
        return writeToRecordStore(filename, content);
    }

    private static String writeFileJSR75(String filename, String content) {
        OutputStream os = null;
        FileConnection fc = null;
        
        for (int i = 0; i < PATHS.length; i++) {
            String path = PATHS[i] + filename;
            try {
                // Créer le répertoire parent si nécessaire
                String parentPath = PATHS[i];
                FileConnection parent = (FileConnection) Connector.open(parentPath, Connector.READ);
                if (!parent.exists()) {
                    parent.mkdir();
                }
                parent.close();
                
                // Ouvrir/créer le fichier
                fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
                if (!fc.exists()) {
                    fc.create();
                } else {
                    fc.truncate(0);
                }
                
                os = fc.openOutputStream();
                os.write(content.getBytes());
                os.flush();
                
                // Fermer proprement
                if (os != null) os.close();
                if (fc != null) fc.close();
                return "Sauvegarde reussie: " + path;
                
            } catch (Exception e) {
                try {
                    if (os != null) os.close();
                    if (fc != null) fc.close();
                } catch (Exception ex) {}
                // Continuer avec le prochain chemin
            }
        }
        return "Erreur: Impossible d'ecrire sur carte memoire";
    }

    public static String writeToRecordStore(String key, String value) {
        try {
            RecordStore rs = RecordStore.openRecordStore("DiscoData", true);
            String data = key + ":" + value;
            byte[] bytes = data.getBytes();
            
            // Supprimer l'ancienne entrée si existe
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
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
            re.destroy();
            
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