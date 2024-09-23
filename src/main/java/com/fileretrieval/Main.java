package com.fileretrieval;

import com.fileretrieval.Interface.AppInterface;
import com.fileretrieval.Interface.RestServer;
import com.fileretrieval.engine.Engine;
import com.fileretrieval.engine.ProcessingEngine;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Main <numThreads>");
            System.exit(1);
        }
        Engine processingEngine = new ProcessingEngine(Integer.parseInt(args[0]));
        try {
            RestServer restServer = new RestServer(3000,processingEngine);
            restServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Scanner scanner = new Scanner(System.in);
        AppInterface app = new AppInterface(scanner, processingEngine);
        app.start();
    }
}
