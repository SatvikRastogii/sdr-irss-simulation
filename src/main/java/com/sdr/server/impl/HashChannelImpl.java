package com.sdr.server.impl;

import java.security.*;
import java.util.*;

/**
 * Hash Channel Implementation
 * Generates cryptographic hashes for integrity verification
 * Based on IRSS::IandA::HashChannel
 */
public class HashChannelImpl {
    
    private final long channelId;
    private final String algorithm;
    private MessageDigest messageDigest;
    private List<byte[]> dataBuffer;
    
    public HashChannelImpl(long channelId, String algorithm) {
        this.channelId = channelId;
        this.algorithm = algorithm;
        this.dataBuffer = new ArrayList<>();
        
        try {
            initializeDigest();
        } catch (Exception e) {
            System.err.println("[HASH] Error initializing: " + e.getMessage());
        }
    }
    
    /**
     * Initialize message digest based on algorithm
     */
    private void initializeDigest() throws NoSuchAlgorithmException {
        switch (algorithm.toUpperCase()) {
            case "SHA-256":
            case "SHA256":
                messageDigest = MessageDigest.getInstance("SHA-256");
                break;
            
            case "SHA-512":
            case "SHA512":
                messageDigest = MessageDigest.getInstance("SHA-512");
                break;
            
            case "MD5":
                messageDigest = MessageDigest.getInstance("MD5");
                break;
            
            case "SHA-1":
            case "SHA1":
                messageDigest = MessageDigest.getInstance("SHA-1");
                break;
            
            default:
                // Default to SHA-256
                messageDigest = MessageDigest.getInstance("SHA-256");
        }
    }
    
    /**
     * Push data to be hashed
     * Based on IRSS::IandA::Channel::pushData
     */
    public void pushData(byte[] data) {
        if (data != null && data.length > 0) {
            dataBuffer.add(data.clone());
            messageDigest.update(data);
        }
    }
    
    /**
     * Get computed hash
     * Based on IRSS::IandA::HashChannel::getHash
     */
    public byte[] getHash() {
        byte[] hash = messageDigest.digest();
        
        System.out.println("[HASH] Generated " + algorithm + " hash");
        System.out.println("       Input size: " + getTotalDataSize() + " bytes");
        System.out.println("       Hash size: " + hash.length + " bytes");
        System.out.println("       Hash (hex): " + bytesToHex(hash));
        
        return hash;
    }
    
    /**
     * Reset channel state
     * Based on IRSS::IandA::Channel::reset
     */
    public void reset() {
        messageDigest.reset();
        dataBuffer.clear();
        System.out.println("[HASH] Channel " + channelId + " reset");
    }
    
    /**
     * Get maximum data size that can be processed
     */
    public int getMaxDataSize() {
        return Integer.MAX_VALUE; // Practically unlimited for hashing
    }
    
    /**
     * Get total size of buffered data
     */
    private int getTotalDataSize() {
        return dataBuffer.stream()
            .mapToInt(arr -> arr.length)
            .sum();
    }
    
    /**
     * Convert bytes to hex string for display
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, 16); i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        if (bytes.length > 16) {
            sb.append("...");
        }
        return sb.toString();
    }
    
    /**
     * Get channel info
     */
    public String getInfo() {
        return String.format("HashChannel[id=%d, algorithm=%s, buffered=%d bytes]",
            channelId, algorithm, getTotalDataSize());
    }
}