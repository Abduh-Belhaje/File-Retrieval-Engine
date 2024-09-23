package com.fileretrieval.engine;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.util.stream.Collectors;

public class ProcessingEngine implements Engine{
    private final int numThreads;
    private final ExecutorService executorService;

    public ProcessingEngine(int numThreads) {
        this.numThreads = numThreads;
        this.executorService = Executors.newFixedThreadPool(numThreads);
    }

    @Override
    public void index(String path) {
        // Get the folders in the dataset path
        File[] folders = new File(path).listFiles(File::isDirectory);

        if (folders == null || folders.length == 0) {
            System.err.println("No folders found in the specified path: " + path);
            return;
        }

        int numFolders = folders.length;
        if(numFolders >= numThreads){
            startPartition(numFolders,folders);
        }else {
            /*
                If numFolders is greater than 0
                And the numFolders < numThreads
                -----------------------------------
                in order to use all threads even if numFolders < numThreads
                we process each folder and split its files on available threads
             */
            Arrays.stream(folders).forEach(f -> {
                File[] files = f.listFiles();
                if (files == null || files.length == 0) {
                    System.err.println("No files found in the specified path: " + path);
                }else{
                    int numberOfFiles = files.length;
                    if(numberOfFiles < numThreads){
                        ProcessWithMinimumThreads(numberOfFiles ,files);
                    }else{
                        startPartition(numberOfFiles,files);
                    }
                }
            });
        }
        waitForTasks();
    }

    private void ProcessWithMinimumThreads(int numFolders , File[] files){
        Arrays.stream(files).forEach(f -> executorService.submit(new IndexWorker(f)));
    }

    private void startPartition(int numFolders , File[] folders){

        int partitionSize = (int) Math.ceil((double) numFolders / numThreads);
        for (int i = 0; i < numThreads; i++) {
            // Calculate partition range for this thread
            int start = i * partitionSize;
            int end = Math.min(start + partitionSize, numFolders);

            // Create a task to handle a range of folders
            File[] folderPartition = Arrays.copyOfRange(folders, start, end);

            executorService.submit(new IndexWorker(folderPartition));  // Assign folder partition to worker
        }
        executorService.shutdown();  // Signal that no more tasks will be added
    }

    private void waitForTasks(){
        // Wait for all threads to finish
        try {
            System.out.println("Waiting for all indexing tasks to complete...");
            executorService.shutdown(); // Stop accepting new tasks
            if (executorService.awaitTermination(1, TimeUnit.HOURS)) {
                // All tasks finished, perform final action
                System.out.println("All indexing tasks completed. Now performing final action...");
                IndexStore.getInstance().printIndex();
            } else {
                System.err.println("Timeout reached before all indexing tasks were completed.");
            }
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted while waiting for indexing tasks to finish: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<String> search(String[] query) {
        // parse the query and get terms
        List<String> terms = Arrays.stream(query).filter(q -> !Objects.equals(q,"search") && !Objects.equals(q.toUpperCase(),"AND") ).toList();

        // Step 1: Check if each term exists in the index and collect the document maps
        List<Map<String, Integer>> termDocumentMaps = new ArrayList<>();
        termDocumentMaps = findIndexTerms(terms,termDocumentMaps);

        // Step 2: Find common documents (AND query) and calculate scores for the matching documents
        Map<String, Integer> docFrequencySum = new HashMap<>();

        // Start with the set of documents from the first term
        Set<String> commonDocs = new HashSet<>(termDocumentMaps.get(0).keySet());

        // Find the intersection of documents that contain all the terms
        for (Map<String, Integer> docMap : termDocumentMaps) {
            commonDocs.retainAll(docMap.keySet());
        }

        // Step 3: Calculate the sum of frequencies for each common document
        for (String doc : commonDocs) {
            int totalFrequency = 0;
            for (Map<String, Integer> docMap : termDocumentMaps) {
                totalFrequency += docMap.get(doc);
            }
            docFrequencySum.put(doc, totalFrequency);
        }

        // Step 4: Sort documents by frequency in descending order
        // Sort by frequency descending
        // Return top 10 documents

        return docFrequencySum.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort by frequency descending
                .limit(10) // Return top 10 documents
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Map<String, Integer>> findIndexTerms(List<String> terms , List<Map<String, Integer>> termDocumentMaps){
        Map<String, Map<String, Integer>> globalIndex = IndexStore.getInstance().getGlobalIndex();

        for (String term : terms) {
            Map<String, Integer> docMap = globalIndex.get(term);
            if (docMap != null) {
                termDocumentMaps.add(docMap);
            } else {
                // If one of the terms doesn't exist, no documents can match the AND query
                return Collections.emptyList();
            }
        }
        return termDocumentMaps;
    }
}
