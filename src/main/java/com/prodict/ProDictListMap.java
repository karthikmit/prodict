package com.prodict;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

/**
 * ProDictList Map is kind of LinkedHashMap with added features like removeLastEntry, etc.
 */
class ProDictListMap {

    private final Logger logger = Logger.getLogger(ProDictListMap.class.getTypeName());
    private final Deque<Entry> entriesList;
    private final Map<String, Entry> entriesMap;
    private int currentSize = 0;

    public ProDictListMap(int capacity) {
        entriesList = new ConcurrentLinkedDeque<>();
        entriesMap = new ConcurrentHashMap<>(capacity, 0.75f);
    }

    public void put(String key, Entry entry) {
        entriesList.addFirst(entry);
        entriesMap.put(key, entry);
        currentSize++;
    }

    public boolean containsKey(String key) {
        return entriesMap.containsKey(key);
    }

    public void remove(String key) {
        Entry entry = entriesMap.get(key);
        if(entry != null) {
            entriesList.remove(entry);
            entriesMap.remove(entry);
            currentSize--;
        }
    }

    public int size() {
        return currentSize;
    }

    public Entry get(String key) {
        return entriesMap.get(key);
    }

    public Entry removeLastEntry() {
        logger.info("Removing Last Entry");
        Entry v = entriesList.pollLast();
        entriesMap.remove(v.getKey());
        currentSize--;
        return v;
    }

    public Deque<Entry> getAll() {
        return entriesList;
    }
}
