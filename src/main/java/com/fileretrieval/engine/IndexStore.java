package com.fileretrieval.engine;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/*
    Using Singleton pattern to have a shared IndexStore
    between the App thread and the HttpServer Thread
 */
public class IndexStore {
    private static IndexStore indexStore;
    private final Map<String, Map<String, Integer>> globalIndex = new HashMap<>();
    // Lock to ensure thread safety during updates to the global index
    private final Lock indexLock ;

    private IndexStore() {
        indexLock = new ReentrantLock();
    }

    public static IndexStore getInstance(){
        if(indexStore == null){
            indexStore = new IndexStore();
        }
        return indexStore;
    }


    // Method to update the global index with a local index from a worker thread
    public void updateIndex(String fileName ,Map<String, Map<String, Integer>> localIndex) {
        indexLock.lock();
        try {
            for (Map.Entry<String, Map<String, Integer>> entry : localIndex.entrySet()) {

                String term = entry.getKey();
                Map<String, Integer> localDocMap = entry.getValue();

                globalIndex.merge(term, localDocMap, (globalDocMap, newLocalDocMap) -> {
                    for (Map.Entry<String, Integer> docEntry : newLocalDocMap.entrySet()) {
                        String doc = docEntry.getKey();
                        int frequency = docEntry.getValue();
                        // Update the frequency if the document already exists
                        globalDocMap.merge(doc, frequency, Integer::sum);
                    }
                    return globalDocMap;
                });
            }
        } finally {
            indexLock.unlock();
        }
    }

    public void printIndex() {
        for (Map.Entry<String, Map<String, Integer>> entry : globalIndex.entrySet()) {
            System.out.println("Term: " + entry.getKey());
            for (Map.Entry<String, Integer> docEntry : entry.getValue().entrySet()) {
                System.out.println("  Document: " + docEntry.getKey() + " | Frequency: " + docEntry.getValue());
            }
        }
    }

}
