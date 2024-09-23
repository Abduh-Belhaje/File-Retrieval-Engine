package com.fileretrieval.Interface;

import com.fileretrieval.engine.Engine;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;

public class RestServer {
    private final int PORT;
    private final Engine processingEngine;

    public RestServer(int port, Engine processingEngine) {
        this.PORT = port;
        this.processingEngine = processingEngine;
    }

    // Main Method to start the server
    public void start() throws IOException {
        // Create an HttpServer instance
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Create a context for a specific path and set the handler
        server.createContext("/index", new IndexHandler(processingEngine));
        server.createContext("/search", new SearchHandler(processingEngine));

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();

        System.out.println("Server is running on port " + PORT + "...\n");
    }

    // Define a custom HttpHandler (Non-static class to access processingEngine)
    class IndexHandler implements HttpHandler {
        private final Engine processingEngine;

        public IndexHandler(Engine processingEngine) {
            this.processingEngine = processingEngine;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Parse request body (dataset path)
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                JSONObject requestJson = new JSONObject(requestBody);
                String datasetPath = requestJson.getString("dataset_path");

                // Call the ProcessingEngine to index the dataset
                processingEngine.index(datasetPath);

                // Create JSON response
                JSONObject responseJson = new JSONObject();
                responseJson.put("timestamp", LocalDateTime.now().toString());
                responseJson.put("message", "Indexing complete");

                // Send response
                sendResponse(exchange, 200, responseJson.toString());
            } else {
                // Handle unsupported methods
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }

    class SearchHandler implements HttpHandler {
        private final Engine processingEngine;

        public SearchHandler(Engine processingEngine) {
            this.processingEngine = processingEngine;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Parse request body (AND query) using Gson
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                JSONObject requestJson = new JSONObject(requestBody);
                String query = requestJson.getString("query");

                // Parse query into terms (e.g., "cats AND dogs")
                String[] terms = query.split(" ");

                List<String> topFiles = processingEngine.search(terms);

                // Send response
                JSONObject responseJson = new JSONObject();
                responseJson.put("timestamp", LocalDateTime.now().toString());
                responseJson.put("top_files", topFiles);
                sendResponse(exchange, 200, responseJson.toString());

            } else {
                // Handle unsupported methods
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }

    // Method to send JSON response to the client
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
