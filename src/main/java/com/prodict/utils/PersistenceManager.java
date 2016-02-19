package com.prodict.utils;

import com.prodict.Entry;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by karthik on 2/18/16.
 */
public class PersistenceManager {

    private static final String directoryPath = "/data/prodict/";
    private static final String filePrefix = "bucket_";
    private static final String fileSuffix = ".bin";

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
        Set<Entry> allEntries = new HashSet<Entry>(10, 0.75f);
        if(!fileContent.equals("")) {
            allEntries = new Gson().fromJson(fileContent, new TypeToken<HashSet<Entry>>() {

            }.getType());
        }
        if(allEntries.contains(entry)) {
            allEntries.remove(entry);
        }
        allEntries.add(entry);
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
        Set<Entry> allEntries = new HashSet<Entry>();
        if(!fileContent.equals("")) {
            allEntries = new Gson().fromJson(fileContent, new TypeToken<HashSet<Entry>>() {

            }.getType());
        }
        for(Entry entry : allEntries) {
            if(entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }
}
