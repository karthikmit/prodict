package com.buyhatke.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * ProDict is the core class.
 */
public class ProDict {

    private final ProDictListMap cacheEntries;
    private final int capacity;
    private final Logger logger = LoggerFactory.getLogger(ProDict.class);
    private final PersistenceManager persistenceManager;

    public ProDict(int capacity, String directoryPath) {
        this.capacity = capacity;
        cacheEntries = new ProDictListMap(capacity);
        persistenceManager = new PersistenceManager(directoryPath);
    }

    /**
     * Put operation makes sure the capacity constraint is met. Otherwise, it would evict an entry and place the new entry.
     * @param entry which should be added to the cache.
     */
    public void put(Entry entry) {
        ensureCapacity(entry.getKey());
        cacheEntries.put(entry.getKey(), entry);
    }

    /**
     * This method would try to fetch from InMemory, if not found, will check in FileSystem.
     * TODO: Bloom filter can be implemented to avoid unessential lookup in the file system.
     * @param key for the respective entry
     * @return Entry for the respective key will be returned, if key not present or expired, null will be thrown.
     */
    public Entry get(String key) {
        Entry entry = cacheEntries.get(key);

        if(entry == null) {
            entry = checkInFileSystem(key);
        }

        if(entry != null) {
            cacheEntries.remove(entry.getKey());

            // If not expired, keep it at the top of the list, as it is LRU Cache ..
            if(!PersistenceManager.isExpired(entry)) {
                cacheEntries.put(entry.getKey(), entry);
            } else {
                return null;
            }
        }
        return entry;
    }

    /**
     * This would return null, if the key is not at present at InMemory Cache.
     * It wouldn't check the file system for evicted entries.
     * @param key for the respective entry
     * @return Entry for the respective key will be returned, if key not present or expired, null will be thrown.
     */
    public Entry getOnlyIfInMemory(String key) {
        return cacheEntries.get(key);
    }

    /**
     * This method shall be used only for testing, as it would just return the cache entries only in memory.
     * @return List of all the entries, right now in the cache, In Memory.
     */
    public List<Entry> getAll() {
        return cacheEntries.getAll();
    }

    /**
     * Flush would make sure all the cache entries which are In Memory are properly written to the file system.
     * Failing to call this method, would cause active entries in the cache not updated in the file system.
     * TODO: Need to implement automated handling of persisting cache entries.
     * @throws IOException
     */
    public void flush() throws IOException {
        for(Entry entry : cacheEntries.getAll()) {
            persistenceManager.persist(entry);
        }
    }

    private Entry checkInFileSystem(String key) {
        return persistenceManager.fetch(key);
    }

    private void persistEntryInFileSystem(Entry entry) {
        try {
            persistenceManager.persist(entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureCapacity(String key) {
        if(cacheEntries.containsKey(key)) return;

        final int size = cacheEntries.size();
        if(size >= capacity) {
            logger.info("Exceeded Capacity, So, eviction starts.");
            evictLRUEntry();
        }
    }

    private void evictLRUEntry() {
        Entry entry = cacheEntries.removeLastEntry();
        persistEntryInFileSystem(entry);
    }
}