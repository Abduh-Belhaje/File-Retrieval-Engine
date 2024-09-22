package com.fileretrieval.engine;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class ProcessingEngine implements Engine{
    private final int numThreads;
    private final ExecutorService executorService;
    private static final Logger logger = Logger.getLogger(ProcessingEngine.class.getName());

    public ProcessingEngine(int numThreads) {
        this.numThreads = numThreads;
        this.executorService = Executors.newFixedThreadPool(numThreads);
    }

    @Override
    public void index(String path) {
        logger.info("Indexing started for path: " + path);

        // Get the folders in the dataset path
        File[] folders = new File(path).listFiles(File::isDirectory);

        if (folders == null || folders.length == 0) {
            logger.warning("No folders found in the specified path: " + path);
            return;
        }

        int numFolders = folders.length;
        if(numFolders >= numThreads){
            logger.info("Number of folders to process: " + (numFolders));
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
                    logger.warning("No files found in the specified path: " + path);

                }else{
                    int numberOfFiles = files.length;
                    logger.info("Number of files to process: " + numberOfFiles);
                    if(numberOfFiles < numThreads){
                        ProcessWithMinimumThreads(numberOfFiles ,files);
                    }else{
                        startPartition(numberOfFiles,files);
                    }
                }
            });
        }
        waitForTask();
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

            logger.info("Thread " + i + " assigned to folders from index " + start + " to " + end);

            executorService.submit(new IndexWorker(folderPartition));  // Assign folder partition to worker
        }

        logger.info("All tasks submitted. ExecutorService is shutting down.");
        executorService.shutdown();  // Signal that no more tasks will be added
    }

    private void waitForTask(){
        // Wait for all threads to finish
        try {
            logger.info("Waiting for all indexing tasks to complete...");
            executorService.shutdown(); // Stop accepting new tasks
            if (executorService.awaitTermination(1, TimeUnit.HOURS)) {
                // All tasks finished, perform final action
                logger.info("All indexing tasks completed. Now performing final action...");
                IndexStore.getInstance().printIndex();
            } else {
                logger.warning("Timeout reached before all indexing tasks were completed.");
            }
        } catch (InterruptedException e) {
            logger.severe("Thread was interrupted while waiting for indexing tasks to finish: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<String> search(List<String> params) {
        return List.of();
    }
}
