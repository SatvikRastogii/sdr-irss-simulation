package com.sdr.server.impl;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Key Store Implementation
 * Manages cryptographic keys securely
 * Based on IRSS::Control::KeyMgmt
 */
public class KeyStoreImpl {
    
    private final Map<Long, KeyEntry> keyStore;
    private final AtomicLong keyIdGenerator;
    private final SecureRandom secureRandom;
    
    public KeyStoreImpl() {
        this.keyStore = new ConcurrentHashMap<>();
        this.keyIdGenerator = new AtomicLong(1);
        this.secureRandom = new SecureRandom();
        
        // Pre-populate with default keys for demo
        initializeDefaultKeys();
    }
    
    /**
     * Initialize default keys for demonstration
     */
    private void initializeDefaultKeys() {
        // Default AES-128 key
        byte[] aesKey128 = new byte[16];
        Arrays.fill(aesKey128, (byte) 0x42);
        storeKey(aesKey128, "AES-128", "Default AES 128-bit key");
        
        // Default AES-256 key
        byte[] aesKey256 = new byte[32];
        Arrays.fill(aesKey256, (byte) 0x24);
        storeKey(aesKey256, "AES-256", "Default AES 256-bit key");
        
        // Random test key
        byte[] randomKey = new byte[16];
        secureRandom.nextBytes(randomKey);
        storeKey(randomKey, "AES-128", "Random test key");
        
        System.out.println("[KEYSTORE] Initialized with " + keyStore.size() + " default keys");
    }
    
    /**
     * Store a new key
     * Based on IRSS::Control::KeyMgmt operations
     */
    public long storeKey(byte[] keyData) {
        return storeKey(keyData, "UNKNOWN", "User-provided key");
    }
    
    public long storeKey(byte[] keyData, String type, String description) {
        if (keyData == null || keyData.length == 0) {
            throw new IllegalArgumentException("Key data cannot be null or empty");
        }
        
        long keyId = keyIdGenerator.getAndIncrement();
        KeyEntry entry = new KeyEntry(keyId, keyData.clone(), type, description);
        keyStore.put(keyId, entry);
        
        System.out.println("[KEYSTORE] Stored key " + keyId + 
                         " (" + type + ", " + keyData.length + " bytes)");
        
        return keyId;
    }
    
    /**
     * Retrieve a key
     */
    public byte[] getKey(long keyId) {
        KeyEntry entry = keyStore.get(keyId);
        if (entry == null) {
            return null;
        }
        
        entry.incrementAccessCount();
        return entry.getKeyData().clone();
    }
    
    /**
     * Update a key (key derivation/rotation)
     * Based on IRSS::Control::KeyMgmt::updateKey
     */
    public boolean updateKey(long keyId) {
        KeyEntry entry = keyStore.get(keyId);
        if (entry == null) {
            return false;
        }
        
        // Simple key update: XOR with random bytes
        byte[] oldKey = entry.getKeyData();
        byte[] newKey = new byte[oldKey.length];
        byte[] random = new byte[oldKey.length];
        secureRandom.nextBytes(random);
        
        for (int i = 0; i < oldKey.length; i++) {
            newKey[i] = (byte) (oldKey[i] ^ random[i]);
        }
        
        entry.updateKeyData(newKey);
        System.out.println("[KEYSTORE] Updated key " + keyId);
        
        return true;
    }
    
    /**
     * Get update count for a key
     * Based on IRSS::Control::KeyMgmt::getUpdateCount
     */
    public int getUpdateCount(long keyId) {
        KeyEntry entry = keyStore.get(keyId);
        return entry != null ? entry.getUpdateCount() : -1;
    }
    
    /**
     * Zeroize (delete) a key
     * Based on IRSS::Control::KeyMgmt::zeroizeKey
     */
    public boolean zeroizeKey(long keyId) {
        KeyEntry entry = keyStore.remove(keyId);
        if (entry != null) {
            entry.zeroize();
            System.out.println("[KEYSTORE] Zeroized key " + keyId);
            return true;
        }
        return false;
    }
    
    /**
     * Check if key exists
     */
    public boolean keyExists(long keyId) {
        return keyStore.containsKey(keyId);
    }
    
    /**
     * Get number of stored keys
     */
    public int getKeyCount() {
        return keyStore.size();
    }
    
    /**
     * Get all key IDs
     */
    public Set<Long> getAllKeyIds() {
        return new HashSet<>(keyStore.keySet());
    }
    
    /**
     * Get key information (without exposing key data)
     */
    public String getKeyInfo(long keyId) {
        KeyEntry entry = keyStore.get(keyId);
        if (entry == null) {
            return "Key not found";
        }
        return entry.toString();
    }
    
    /**
     * Clear all keys
     */
    public void clear() {
        keyStore.values().forEach(KeyEntry::zeroize);
        keyStore.clear();
        System.out.println("[KEYSTORE] All keys cleared");
    }
    
    /**
     * Inner class representing a key entry
     */
    private static class KeyEntry {
        private final long keyId;
        private byte[] keyData;
        private final String type;
        private final String description;
        private final long creationTime;
        private int updateCount;
        private int accessCount;
        
        public KeyEntry(long keyId, byte[] keyData, String type, String description) {
            this.keyId = keyId;
            this.keyData = keyData;
            this.type = type;
            this.description = description;
            this.creationTime = System.currentTimeMillis();
            this.updateCount = 0;
            this.accessCount = 0;
        }
        
        public byte[] getKeyData() {
            return keyData;
        }
        
        public void updateKeyData(byte[] newKeyData) {
            // Zeroize old key
            Arrays.fill(keyData, (byte) 0);
            this.keyData = newKeyData.clone();
            this.updateCount++;
        }
        
        public int getUpdateCount() {
            return updateCount;
        }
        
        public void incrementAccessCount() {
            accessCount++;
        }
        
        public void zeroize() {
            Arrays.fill(keyData, (byte) 0);
        }
        
        @Override
        public String toString() {
            return String.format(
                "KeyEntry[id=%d, type=%s, size=%d bytes, updates=%d, accesses=%d, desc=%s]",
                keyId, type, keyData.length, updateCount, accessCount, description
            );
        }
    }
}