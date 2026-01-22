package com.sdr;

import com.sdr.server.IRSSServer;
import com.sdr.client.WaveformClient;
import java.util.Scanner;

/**
 * Main entry point for SDR-IRSS Simulation
 * Demonstrates client-server communication for cryptographic operations
 * Based on WINNF-09-S-0011 IRSS API Specification
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("    SDR IRSS API Simulation System          ");
        System.out.println("    International Radio Security Services     ");
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Display menu
            System.out.println("Select operation mode:");
            System.out.println("1. Start Server");
            System.out.println("2. Start Client");
            System.out.println("3. Start Both (Server + Client Demo)");
            System.out.println("4. Exit");
            System.out.print("\nEnter choice (1-4): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch (choice) {
                case 1:
                    startServer();
                    break;
                case 2:
                    startClient();
                    break;
                case 3:
                    startBoth();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static void startServer() {
        System.out.println("\n[STARTING SERVER MODE]");
        IRSSServer server = new IRSSServer();
        server.start();

        System.out.println("\nType 'exit' and press Enter to stop the server...");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        server.stop();
    }
    
    private static void startClient() {
        System.out.println("\n[STARTING CLIENT MODE]");
        System.out.println("Connecting to server at localhost:8080...\n");

        try (Scanner scanner = new Scanner(System.in)) { // Shared Scanner
            WaveformClient client = new WaveformClient("localhost", 8080, scanner);
            client.runDemo();
            client.disconnect();
        }
    }
    
    private static void startBoth() {
        System.out.println("\n[STARTING INTEGRATED MODE]");

        // Start server in background thread
        IRSSServer server = new IRSSServer();
        Thread serverThread = new Thread(() -> server.start());
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait for server to initialize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start client demo
        System.out.println("\n[STARTING CLIENT DEMO]\n");
        try (Scanner scanner = new Scanner(System.in)) { // Shared Scanner
            WaveformClient client = new WaveformClient("localhost", 8080, scanner);
            client.runDemo();
            client.disconnect();
        }

        // Shutdown server
        System.out.println("\n[SHUTTING DOWN SERVER]");
        server.stop();
    }
}