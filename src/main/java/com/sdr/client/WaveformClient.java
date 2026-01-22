package com.sdr.client;

import com.sdr.server.IRSSServer.Request;
import com.sdr.server.IRSSServer.Response;
import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Waveform Client - Demonstrates IRSS API usage
 * Simulates a radio waveform using cryptographic services
 */
public class WaveformClient {
    
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected;
    
    // Client state
    private IRSSClientImpl irssClient;
    private Scanner scanner; // Add a shared Scanner instance
    
    public WaveformClient(String host, int port, Scanner scanner) {
        this.serverHost = host;
        this.serverPort = port;
        this.connected = false;
        this.irssClient = null;
        this.scanner = scanner; // Use the shared Scanner
    }
    
    /**
     * Connect to IRSS server
     */
    public boolean connect() {
        try {
            System.out.println("[CLIENT] Connecting to " + serverHost + ":" + serverPort);
            
            socket = new Socket(serverHost, serverPort);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            connected = true;
            irssClient = new IRSSClientImpl(this);
            
            System.out.println("[CLIENT] Connected successfully\n");
            return true;
            
        } catch (IOException e) {
            System.err.println("[CLIENT] Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            
            connected = false;
            System.out.println("\n[CLIENT] Disconnected");
            
        } catch (IOException e) {
            System.err.println("[CLIENT] Error during disconnect: " + e.getMessage());
        }
    }
    
    /**
     * Send request to server and get response
     */
    public Response sendRequest(Request request) {
        if (!connected) {
            System.err.println("[CLIENT] Not connected to server");
            return Response.error("Not connected");
        }
        
        try {
            output.writeObject(request);
            output.flush();
            
            Response response = (Response) input.readObject();
            return response;
            
        } catch (Exception e) {
            System.err.println("[CLIENT] Request failed: " + e.getMessage());
            return Response.error(e.getMessage());
        }
    }
    
    /**
     * Run demonstration of IRSS API capabilities
     */
    public void runDemo() {
        if (!connect()) {
            System.err.println("Cannot run demo - connection failed");
            return;
        }
        
        System.out.println("IRSS API Demonstration");
        
        try {
            // Demo 1: Cryptographic Channel
            demonstrateCryptographicChannel();
            
            Thread.sleep(1000);
            
            // Demo 2: Hash Channel
            demonstrateHashChannel();
            
            Thread.sleep(1000);
            
            // Demo 3: Key Management
            demonstrateKeyManagement();
            
            Thread.sleep(1000);
            
            // Demo 4: Statistics
            demonstrateStatistics();
            
        } catch (Exception e) {
            System.err.println("[DEMO] Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println(" Demonstration Complete");
    }
    
    /**
     * Demo 1: Cryptographic Channel Operations
     */
    private void demonstrateCryptographicChannel() {
        System.out.println(" Demo 1: Cryptographic Channel");
        
        try {
            // Create channel
            System.out.println("1. Creating cryptographic channel...");
            long channelId = irssClient.createCryptographicChannel(
                1,              // crypto module ID
                100,            // PT endpoint
                200,            // CT endpoint
                "AES-128",      // algorithm
                "FULL_DUPLEX"   // duplexity
            );
            System.out.println("   Channel created: " + channelId + "\n");
            
            // Store key
            System.out.println("2. Storing encryption key...");
            byte[] keyData = "MySecretKey12345".getBytes();
            long keyId = irssClient.storeKey(keyData);
            System.out.println("   Key stored: " + keyId + "\n");
            
            // Add configuration
            System.out.println("3. Adding channel configuration...");
            long configId = irssClient.addConfiguration(channelId, keyId);
            System.out.println("   Configuration added: " + configId + "\n");
            
            // Activate configuration
            System.out.println("4. Activating configuration...");
            irssClient.activateConfiguration(channelId, configId);
            System.out.println("    Configuration activated\n");
            
            // Encrypt data
            System.out.println("5. Encrypting data...");
            String plaintext = "Hello, Secure Radio World!";
            System.out.println("   Plaintext: \"" + plaintext + "\"");
            
            List<byte[]> packets = Arrays.asList(plaintext.getBytes());
            List<byte[]> encrypted = irssClient.encryptPackets(channelId, packets);
            
            System.out.println("   Data encrypted (" + encrypted.get(0).length + " bytes)\n");
            
            // Decrypt data
            System.out.println("6. Decrypting data...");
            List<byte[]> decrypted = irssClient.decryptPackets(channelId, encrypted);
            String decryptedText = new String(decrypted.get(0));
            
            System.out.println("   Decrypted: \"" + decryptedText + "\"");
            System.out.println("   Verification: " + 
                (plaintext.equals(decryptedText) ? "SUCCESS" : "FAILED") + "\n");
            
            // Cleanup
            System.out.println("7. Destroying channel...");
            irssClient.destroyChannel(channelId);
            System.out.println("  Channel destroyed\n");
            
        } catch (Exception e) {
            System.err.println("   Error: " + e.getMessage());
        }
    }
    
    /**
     * Demo 2: Hash Channel Operations
     */
    private void demonstrateHashChannel() {
        System.out.println(" Demo 2: Hash Channel (Integrity Check)");

        try {
            // Create hash channel
            System.out.println("1. Creating hash channel...");
            long hashChannelId = irssClient.createHashChannel("SHA-256");
            System.out.println("    Hash channel created: " + hashChannelId + "\n");

            // Debug: Check if method is executed
            System.out.println("DEBUG: Waiting for user input...");

            // Get user input for hashing
            System.out.println("2. Enter data to hash:");
            if (scanner == null) {
                System.out.println("DEBUG: Scanner is null!");
            }
            String data = scanner.nextLine(); // Wait for user input
            System.out.println("   Data: \"" + data + "\"");

            // Generate hash
            byte[] hash = irssClient.generateHash(hashChannelId, data.getBytes());

            System.out.println("    Hash generated (" + hash.length + " bytes)");
            System.out.println("   Hash (hex): " + bytesToHex(hash).substring(0, 32) + "...\n");

            // Cleanup
            System.out.println("3. Destroying hash channel...");
            irssClient.destroyChannel(hashChannelId);
            System.out.println("   Channel destroyed\n");

        } catch (Exception e) {
            System.err.println("    Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demo 3: Key Management Operations
     */
    private void demonstrateKeyManagement() {
      
        System.out.println(" Demo 3: Key Management ");
        
        try {
            // Store multiple keys
            System.out.println("1. Storing multiple keys...");
            
            byte[] key1 = "Key1Data12345678".getBytes();
            byte[] key2 = "Key2Data87654321".getBytes();
            
            long keyId1 = irssClient.storeKey(key1);
            long keyId2 = irssClient.storeKey(key2);
            
            System.out.println("   Key 1 stored: ID=" + keyId1);
            System.out.println("   Key 2 stored: ID=" + keyId2 + "\n");
            
            System.out.println("2. Key operations demonstrated");
            System.out.println("   Note: Update and zeroize operations would be");
            System.out.println("   performed through KeyMgmt interface\n");
            
        } catch (Exception e) {
            System.err.println("   Error: " + e.getMessage());
        }
    }
    
    /**
     * Demo 4: System Statistics
     */
    private void demonstrateStatistics() {
        System.out.println(" Demo 4: System Statistics ");
        
        try {
            String stats = irssClient.getStatistics();
            System.out.println(stats);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("   Error: " + e.getMessage());
        }
    }
    
    /**
     * Utility: Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}