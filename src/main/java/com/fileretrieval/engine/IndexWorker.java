package com.fileretrieval.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class IndexWorker implements Runnable{
    private final File[] files;

    public IndexWorker(File[] files) {
        this.files = files;
    }

    public IndexWorker(File file) {
        this.files = new File[]{file};
    }

    @Override
    public void run() {
        for (File file : files) {
            if (file.isDirectory()) {
                // If it's a directory, recursively index files in this folder
                indexFolder(file);
            } else {
                // If it's a file, index the file
                indexFile(file);
            }
        }
    }

    private void indexFolder(File folder) {
        // Recursively index the files in this folder
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                indexFile(file);
            }
        }
    }

    private void indexFile(File file) {

        Map<String, Map<String, Integer>> localIndex = new HashMap<>();  // Local index for this file
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;

            // Read file line by line
            while ((line = br.readLine()) != null) {
                String[] words = extractTerms(line);  // Extract words from each line

                for (String word : words) {
                    String term = word.toLowerCase();  // Normalize to lowercase

                    // If the term is not in the local index, add it with an empty document-frequency map
                    localIndex.putIfAbsent(term, new HashMap<>());

                    // Get the document-frequency map for this term
                    Map<String, Integer> docMap = localIndex.get(term);

                    // Update the frequency of the term in this document (file.getName())
                    docMap.put(file.getName(), docMap.getOrDefault(file.getName(), 0) + 1);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Update global index with the local index
        mergeWithGlobalIndex(localIndex);
    }

    // Extract words/terms from a line of text (split by non-alphanumeric characters)
    private String[] extractTerms(String line) {
        // Split by any non-alphanumeric character, remove punctuation
        return line.toLowerCase().split("\\W+");
    }

    private void mergeWithGlobalIndex(Map<String, Map<String, Integer>> localIndex){
        IndexStore.getInstance().updateIndex(localIndex);
    }

}
