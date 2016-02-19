package com.prodict.utils;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prodict.Entry;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PersistenceManager takes care of persisting cache entries in Local FileSystem.
 */
public class PersistenceManager {

    private final String directoryPath;
    private static final String filePrefix = "bucket_";
    private static final String fileSuffix = ".bin";

    public PersistenceManager(String directoryPath) {
        this.directoryPath = directoryPath;
    }


    public void persist(Entry entry) throws IOException {
        String filePath = getRandomFileName(entry.getKey());
        String fileContent = "";
        final File file = new File(filePath);
        if(file.exists()) {
            try {
                fileContent = FileUtils.readFileToString(file);
            } catch (IOException e) {
                // It should never happen.
                e.printStackTrace();
            }
        }
        Map<String, Entry> allEntries = new HashMap<>(10, 0.75f);
        if(!fileContent.equals("")) {
            allEntries = new Gson().fromJson(fileContent, new TypeToken<HashMap<String, Entry>>() {

            }.getType());
        }

        allEntries.put(entry.getKey(), entry);

        // Remove all the expired values from the list before writing ...
        allEntries.values().removeIf(PersistenceManager::isExpired);
        FileUtils.writeStringToFile(file, new Gson().toJson(allEntries));
    }

    private String getRandomFileName(String key) {
        int bucket = Math.abs(Hashing.sha256().hashBytes(key.getBytes()).asInt() % 65535);
        return directoryPath + filePrefix + bucket + fileSuffix;
    }

    public Entry fetch(String key) {
        String filePath = getRandomFileName(key);
        String fileContent = "";
        final File file = new File(filePath);
        if(file.exists()) {
            try {
                fileContent = FileUtils.readFileToString(file);
            } catch (IOException e) {
                // It should never happen.
                e.printStackTrace();
            }
        }
        Map<String, Entry> allEntries = new HashMap<>();
        if(!fileContent.equals("")) {
            allEntries = new Gson().fromJson(fileContent, new TypeToken<HashMap<String, Entry>>() {

            }.getType());
        }
        if(allEntries.containsKey(key)) {
            final Entry entry = allEntries.get(key);
            if(!isExpired(entry)) {
                return entry;
            }
        }

        return null;
    }

    public static boolean isExpired(Entry entry) {
        long timeNow = new Date().getTime();
        long createdTime = entry.getCreatedAt().getTime();

        long expiresIn = createdTime + entry.getExpiresInUnit().toMillis(entry.getExpiresIn());
        return expiresIn <= timeNow;
    }
}
