package com.sdr.server.impl;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cryptographic Channel Implementation
 * Performs encryption/decryption operations
 * Based on IRSS::Infosec::CryptographicChannel
 */
public class CryptoChannelImpl {
    
    private final long channelId;
    private final String algorithm;
    private final String duplexity;
    private final KeyStoreImpl keyStore;
    
    // Configuration management
    private final Map<Long, ChannelConfiguration> configurations;
    private final AtomicLong configIdGenerator;
    private ChannelConfiguration activeConfig;
    
    // Stream state
    private boolean streamActive;
    private int packetsProcessed;
    
    public CryptoChannelImpl(long channelId, String algorithm, 
                            String duplexity, KeyStoreImpl keyStore) {
        this.channelId = channelId;
        this.algorithm = algorithm;
        this.duplexity = duplexity;
        this.keyStore = keyStore;
        this.configurations = new HashMap<>();
        this.configIdGenerator = new AtomicLong(1);
        this.activeConfig = null;
        this.streamActive = false;
        this.packetsProcessed = 0;
    }
    
    /**
     * Add a new configuration to this channel
     */
    public long addConfiguration(long keyId, byte[] keyData) throws Exception {
        long configId = configIdGenerator.getAndIncrement();
        
        ChannelConfiguration config = new ChannelConfiguration(
            configId, keyId, keyData, algorithm
        );
        
        configurations.put(configId, config);
        
        return configId;
    }
    
    /**
     * Activate a configuration
     */
    public void activateConfiguration(long configId) throws Exception {
        ChannelConfiguration config = configurations.get(configId);
        if (config == null) {
            throw new Exception("Invalid configuration ID: " + configId);
        }
        
        this.activeConfig = config;
        this.streamActive = false;
        this.packetsProcessed = 0;
    }
    
    /**
     * Check if channel is configured
     */
    public boolean isConfigured() {
        return activeConfig != null;
    }
    
    /**
     * Encrypt data
     */
    public byte[] encrypt(byte[] plaintext) throws Exception {
        if (activeConfig == null) {
            throw new Exception("No active configuration");
        }
        
        try {
            Cipher cipher = Cipher.getInstance(activeConfig.getCipherMode());
            cipher.init(Cipher.ENCRYPT_MODE, 
                       activeConfig.getSecretKey(), 
                       activeConfig.getIvSpec());
            
            byte[] ciphertext = cipher.doFinal(plaintext);
            packetsProcessed++;
            
            return ciphertext;
            
        } catch (Exception e) {
            throw new Exception("Encryption failed: " + e.getMessage());
        }
    }
    
    /**
     * Decrypt data
     */
    public byte[] decrypt(byte[] ciphertext) throws Exception {
        if (activeConfig == null) {
            throw new Exception("No active configuration");
        }
        
        try {
            Cipher cipher = Cipher.getInstance(activeConfig.getCipherMode());
            cipher.init(Cipher.DECRYPT_MODE, 
                       activeConfig.getSecretKey(), 
                       activeConfig.getIvSpec());
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            packetsProcessed++;
            
            return plaintext;
            
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage());
        }
    }
    
    /**
     * Reset stream state (SOM marker)
     */
    public void resetStreamState() {
        streamActive = true;
        packetsProcessed = 0;
    }
    
    /**
     * Finalize stream (EOM marker)
     */
    public void finalizeStream() {
        streamActive = false;
    }
    
    /**
     * Get channel statistics
     */
    public int getPacketsProcessed() {
        return packetsProcessed;
    }
    
    /**
     * Inner class for channel configuration
     */
    private static class ChannelConfiguration {
        private final long configId;
        private final long keyId;
        private final SecretKey secretKey;
        private final IvParameterSpec ivSpec;
        private final String cipherMode;
        
        public ChannelConfiguration(long configId, long keyId, 
                                   byte[] keyData, String algorithm) 
                                   throws Exception {
            this.configId = configId;
            this.keyId = keyId;
            
            // Setup cipher based on algorithm
            switch (algorithm.toUpperCase()) {
                case "AES":
                case "AES-128":
                    this.cipherMode = "AES/CBC/PKCS5Padding";
                    this.secretKey = createAESKey(keyData, 128);
                    this.ivSpec = generateIV(16);
                    break;
                
                case "AES-256":
                    this.cipherMode = "AES/CBC/PKCS5Padding";
                    this.secretKey = createAESKey(keyData, 256);
                    this.ivSpec = generateIV(16);
                    break;
                
                case "DES":
                    this.cipherMode = "DES/CBC/PKCS5Padding";
                    this.secretKey = createDESKey(keyData);
                    this.ivSpec = generateIV(8);
                    break;
                
                default:
                    throw new Exception("Unsupported algorithm: " + algorithm);
            }
        }
        
        private SecretKey createAESKey(byte[] keyData, int keySize) throws Exception {
            byte[] key = new byte[keySize / 8];
            
            if (keyData.length >= key.length) {
                System.arraycopy(keyData, 0, key, 0, key.length);
            } else {
                // Pad with zeros if key too short
                System.arraycopy(keyData, 0, key, 0, keyData.length);
            }
            
            return new SecretKeySpec(key, "AES");
        }
        
        private SecretKey createDESKey(byte[] keyData) throws Exception {
            byte[] key = new byte[8];
            
            if (keyData.length >= 8) {
                System.arraycopy(keyData, 0, key, 0, 8);
            } else {
                System.arraycopy(keyData, 0, key, 0, keyData.length);
            }
            
            return new SecretKeySpec(key, "DES");
        }
        
        private IvParameterSpec generateIV(int size) {
            byte[] iv = new byte[size];
            new SecureRandom().nextBytes(iv);
            return new IvParameterSpec(iv);
        }
        
        public SecretKey getSecretKey() {
            return secretKey;
        }
        
        public IvParameterSpec getIvSpec() {
            return ivSpec;
        }
        
        public String getCipherMode() {
            return cipherMode;
        }
    }
}