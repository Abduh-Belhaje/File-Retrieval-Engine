package com.fileretrieval;

import com.fileretrieval.Interface.AppInterface;
import com.fileretrieval.Interface.RestServer;
import com.fileretrieval.engine.Engine;
import com.fileretrieval.engine.ProcessingEngine;
import org.json.JSONObject;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Main <numThreads>");
            System.exit(1);
        }
        Engine processingEngine = new ProcessingEngine(Integer.parseInt(args[0]));
        try {
            String configPath = args[1];
            // Start the REST API
            int port = loadPortFromConfig(configPath);
            RestServer restServer = new RestServer(port,processingEngine);
            restServer.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        AppInterface app = new AppInterface(scanner, processingEngine);
        app.start();
    }

    private static int loadPortFromConfig(String configPath) throws IOException {
        File configFile = new File(configPath);
        BufferedReader br = new BufferedReader(new FileReader(configFile));

        JSONObject requestJson = new JSONObject(br.readLine());
        return requestJson.getInt("rest_endpoint_port");
    }

}
