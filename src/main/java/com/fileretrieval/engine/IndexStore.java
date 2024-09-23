package com.fileretrieval.engine;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton class to manage a shared global index between application threads
 * and the HTTP server thread. It provides thread-safe updates to the index.
 */
public class IndexStore {
    private static IndexStore instance;
    private final Map<String, Map<String, Integer>> globalIndex = new HashMap<>();
    private final Lock indexLock = new ReentrantLock();

    // Private constructor to prevent instantiation
    private IndexStore() {}

    /**
     * Returns the singleton instance of IndexStore.
     *
     * @return IndexStore instance
     */
    public static IndexStore getInstance() {
        if (instance == null) {
            synchronized (IndexStore.class) { // Ensure thread-safe lazy initialization
                if (instance == null) {
                    instance = new IndexStore();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the global index map.
     *
     * @return the global index
     */
    public Map<String, Map<String, Integer>> getGlobalIndex() {
        return globalIndex;
    }

    /**
     * Updates the global index with a local index from a worker thread.
     *
     * @param localIndex the local index to merge
     */
    public void updateIndex(Map<String, Map<String, Integer>> localIndex) {
        indexLock.lock(); // Lock for updating the global index
        try {
            localIndex.forEach((term, localDocMap) -> {
                globalIndex.merge(term, localDocMap, this::mergeDocumentMaps);
            });
        } finally {
            indexLock.unlock(); // Ensure the lock is released
        }
    }

    /**
     * Merges two document frequency maps.
     *
     * @param globalDocMap the existing document map in the global index
     * @param newLocalDocMap the new document map to merge
     * @return the merged document map
     */
    private Map<String, Integer> mergeDocumentMaps(Map<String, Integer> globalDocMap, Map<String, Integer> newLocalDocMap) {
        newLocalDocMap.forEach((doc, frequency) ->
                globalDocMap.merge(doc, frequency, Integer::sum));
        return globalDocMap;
    }

    /**
     * Prints the current state of the global index.
     */
    public void printIndex() {
        globalIndex.forEach((term, docMap) -> {
            System.out.println("Term: " + term);
            docMap.forEach((doc, frequency) ->
                    System.out.println("  Document: " + doc + " | Frequency: " + frequency));
        });
    }
}
