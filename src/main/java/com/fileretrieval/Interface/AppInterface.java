package com.fileretrieval.Interface;

import com.fileretrieval.engine.Engine;
import com.fileretrieval.engine.ProcessingEngine;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class AppInterface {
    private final Scanner scanner;
    private final Engine processingEngine;

    public AppInterface(Scanner scanner, Engine processingEngine) {
        this.scanner = scanner;
        this.processingEngine = processingEngine;
    }

    private void instructions(){
        System.out.println("welcome to file retrieval engine ");
        System.out.println("index < dataset path > : build an index from those files");
        System.out.println("search < AND query > : returns all the files that contain all the terms from the" +
                " AND query");
        System.out.println("quit :  quit the application \n");
    }

    public void start(){
        instructions(); // Display instructions for use
        handleUserInputs();// Start Handling user inputs
    }

    private void handleUserInputs(){
        while(true){
            String input = scanner.nextLine();
            String[] params = input.isEmpty() ? null : input.split(" ");
            switch (Objects.requireNonNull(params)[0]){
                case "index" -> processingEngine.index(params[1]);
                case "search" -> processingEngine.search(params).forEach(System.out::println);
                case "quit" -> System.exit(1);
                default -> System.out.println("Not supported !");
            }
        }
    }
}
