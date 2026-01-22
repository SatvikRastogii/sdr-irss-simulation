package com.sdr.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * IRSS Server - Handles client connections and requests
 * Provides IRSS API services over network
 */
public class IRSSServer {
    
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_CLIENTS = 10;
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private CryptoSubsystem cryptoSubsystem;
    private volatile boolean running;
    private int port;
    
    public IRSSServer() {
        this(DEFAULT_PORT);
    }
    
    public IRSSServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.cryptoSubsystem = new CryptoSubsystem();
        this.running = false;
    }
    
    /**
     * Start the IRSS server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            System.out.println("  IRSS Server Started   ");
            System.out.println("  Port: " + port + "             ");
            System.out.println("  Waiting for client connections...     ");
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New client connected: " + 
                                     clientSocket.getInetAddress());
                    
                    // Handle client in separate thread
                    threadPool.execute(new ClientHandler(clientSocket));
                    
                } catch (SocketException e) {
                    if (!running) {
                        break; // Server stopped
                    }
                    System.err.println("[SERVER] Socket error: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("[SERVER] Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Stop the IRSS server
     */
    public void stop() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            threadPool.shutdown();
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
            
            cryptoSubsystem.shutdown();
            
            System.out.println("\n[SERVER] IRSS Server stopped");
            
        } catch (Exception e) {
            System.err.println("[SERVER] Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Client handler - processes individual client requests
     */
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                // Setup streams
                output = new ObjectOutputStream(socket.getOutputStream());
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());
                
                System.out.println("[HANDLER] Client handler started");
                
                // Process requests
                while (running && !socket.isClosed()) {
                    try {
                        Request request = (Request) input.readObject();
                        Response response = processRequest(request);
                        output.writeObject(response);
                        output.flush();
                        
                    } catch (EOFException e) {
                        break; // Client disconnected
                    }
                }
                
            } catch (Exception e) {
                System.err.println("[HANDLER] Error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }
        
        /**
         * Process client request and generate response
         */
        private Response processRequest(Request req) {
            System.out.println("[HANDLER] Processing: " + req.getOperation());
            
            try {
                switch (req.getOperation()) {
                    
                    case "CREATE_CRYPTO_CHANNEL":
                        return createCryptoChannel(req);
                    
                    case "CREATE_HASH_CHANNEL":
                        return createHashChannel(req);
                    
                    case "DESTROY_CHANNEL":
                        return destroyChannel(req);
                    
                    case "STORE_KEY":
                        return storeKey(req);
                    
                    case "ADD_CONFIGURATION":
                        return addConfiguration(req);
                    
                    case "ACTIVATE_CONFIGURATION":
                        return activateConfiguration(req);
                    
                    case "ENCRYPT_PACKETS":
                        return encryptPackets(req);
                    
                    case "DECRYPT_PACKETS":
                        return decryptPackets(req);
                    
                    case "GENERATE_HASH":
                        return generateHash(req);
                    
                    case "GET_STATS":
                        return getStats();
                    
                    default:
                        return Response.error("Unknown operation: " + req.getOperation());
                }
                
            } catch (Exception e) {
                return Response.error(e.getMessage());
            }
        }
        
        private Response createCryptoChannel(Request req) throws Exception {
            Map<String, Object> params = req.getParameters();
            
            long channelId = cryptoSubsystem.createCryptographicChannel(
                (Integer) params.get("cryptoModuleId"),
                (Integer) params.get("ptEndpoint"),
                (Integer) params.get("ctEndpoint"),
                (String) params.get("algorithm"),
                (String) params.get("duplexity")
            );
            
            return Response.success("Channel created", channelId);
        }
        
        private Response createHashChannel(Request req) {
            String algorithm = (String) req.getParameters().get("algorithm");
            long channelId = cryptoSubsystem.createHashChannel(algorithm);
            return Response.success("Hash channel created", channelId);
        }
        
        private Response destroyChannel(Request req) {
            long channelId = (Long) req.getParameters().get("channelId");
            cryptoSubsystem.destroyChannel(channelId);
            return Response.success("Channel destroyed", null);
        }
        
        private Response storeKey(Request req) {
            byte[] keyData = (byte[]) req.getParameters().get("keyData");
            long keyId = cryptoSubsystem.storeKey(keyData);
            return Response.success("Key stored", keyId);
        }
        
        private Response addConfiguration(Request req) throws Exception {
            Map<String, Object> params = req.getParameters();
            long configId = cryptoSubsystem.addConfiguration(
                (Long) params.get("channelId"),
                (Long) params.get("keyId")
            );
            return Response.success("Configuration added", configId);
        }
        
        private Response activateConfiguration(Request req) throws Exception {
            Map<String, Object> params = req.getParameters();
            cryptoSubsystem.activateConfiguration(
                (Long) params.get("channelId"),
                (Long) params.get("configId")
            );
            return Response.success("Configuration activated", null);
        }
        
        @SuppressWarnings("unchecked")
        private Response encryptPackets(Request req) throws Exception {
            Map<String, Object> params = req.getParameters();
            List<byte[]> encrypted = cryptoSubsystem.transformPackets(
                (Long) params.get("channelId"),
                (List<byte[]>) params.get("packets"),
                true
            );
            return Response.success("Packets encrypted", encrypted);
        }
        
        @SuppressWarnings("unchecked")
        private Response decryptPackets(Request req) throws Exception {
            Map<String, Object> params = req.getParameters();
            List<byte[]> decrypted = cryptoSubsystem.transformPackets(
                (Long) params.get("channelId"),
                (List<byte[]>) params.get("packets"),
                false
            );
            return Response.success("Packets decrypted", decrypted);
        }
        
        private Response generateHash(Request req) throws Exception {
            Map<String, Object> params = req.getParameters();
            byte[] hash = cryptoSubsystem.generateHash(
                (Long) params.get("channelId"),
                (byte[]) params.get("data")
            );
            return Response.success("Hash generated", hash);
        }
        
        private Response getStats() {
            String stats = cryptoSubsystem.getStatistics();
            return Response.success(stats, null);
        }
        
        private void cleanup() {
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
                System.out.println("[HANDLER] Client disconnected");
            } catch (IOException e) {
                System.err.println("[HANDLER] Cleanup error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Request object for client-server communication
     */
    public static class Request implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String operation;
        private Map<String, Object> parameters;
        
        public Request(String operation) {
            this.operation = operation;
            this.parameters = new HashMap<>();
        }
        
        public void addParameter(String key, Object value) {
            parameters.put(key, value);
        }
        
        public String getOperation() {
            return operation;
        }
        
        public Map<String, Object> getParameters() {
            return parameters;
        }
    }
    
    /**
     * Response object for client-server communication
     */
    public static class Response implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private boolean success;
        private String message;
        private Object data;
        
        private Response(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static Response success(String message, Object data) {
            return new Response(true, message, data);
        }
        
        public static Response error(String message) {
            return new Response(false, message, null);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Object getData() {
            return data;
        }
    }
}