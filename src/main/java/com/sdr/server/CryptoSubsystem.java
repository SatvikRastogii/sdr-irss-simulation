package com.sdr.server;

import com.sdr.server.impl.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cryptographic Subsystem (CSS) - Core security engine
 * Manages channels, keys, and cryptographic operations
 * Thread-safe implementation for concurrent operations
 */
public class CryptoSubsystem {
    
    // Channel management
    private final Map<Long, CryptoChannelImpl> cryptoChannels;
    private final Map<Long, HashChannelImpl> hashChannels;
    private final AtomicLong channelIdGenerator;
    
    // Key management
    private final KeyStoreImpl keyStore;
    
    // Configuration
    private static final int MAX_PACKET_SIZE = 65536; // 64KB
    private static final int MAX_PAYLOAD_SIZE = 32768; // 32KB
    private static final int MAX_BYPASS_SIZE = 4096;   // 4KB
    
    public CryptoSubsystem() {
        this.cryptoChannels = new ConcurrentHashMap<>();
        this.hashChannels = new ConcurrentHashMap<>();
        this.channelIdGenerator = new AtomicLong(1);
        this.keyStore = new KeyStoreImpl();
        
        System.out.println("[CSS] Cryptographic Subsystem initialized");
    }
    
    /**
     * Create a cryptographic channel for encryption/decryption
     * Based on IRSS::Control::ChannelMgmt::createCryptographicChannel
     */
    public long createCryptographicChannel(int cryptoModuleId, 
                                          int ptEndpoint, 
                                          int ctEndpoint,
                                          String algorithm,
                                          String duplexity) throws Exception {
        
        long channelId = channelIdGenerator.getAndIncrement();
        
        System.out.println("[CSS] Creating crypto channel " + channelId);
        System.out.println("      Algorithm: " + algorithm);
        System.out.println("      Duplexity: " + duplexity);
        System.out.println("      Endpoints: PT=" + ptEndpoint + ", CT=" + ctEndpoint);
        
        CryptoChannelImpl channel = new CryptoChannelImpl(
            channelId, algorithm, duplexity, keyStore
        );
        
        cryptoChannels.put(channelId, channel);
        
        return channelId;
    }
    
    /**
     * Create a hash channel for integrity verification
     * Based on IRSS::Control::ChannelMgmt::createHashChannel
     */
    public long createHashChannel(String algorithm) {
        long channelId = channelIdGenerator.getAndIncrement();
        
        System.out.println("[CSS] Creating hash channel " + channelId);
        System.out.println("      Algorithm: " + algorithm);
        
        HashChannelImpl channel = new HashChannelImpl(channelId, algorithm);
        hashChannels.put(channelId, channel);
        
        return channelId;
    }
    
    /**
     * Destroy a channel and release resources
     */
    public void destroyChannel(long channelId) {
        if (cryptoChannels.remove(channelId) != null) {
            System.out.println("[CSS] Destroyed crypto channel " + channelId);
        } else if (hashChannels.remove(channelId) != null) {
            System.out.println("[CSS] Destroyed hash channel " + channelId);
        } else {
            System.out.println("[CSS] Channel " + channelId + " not found");
        }
    }
    
    /**
     * Transform (encrypt/decrypt) packets
     * Based on IRSS::Infosec::CryptographicChannel::transformPackets
     */
    public List<byte[]> transformPackets(long channelId, 
                                         List<byte[]> packets, 
                                         boolean encrypt) throws Exception {
        
        CryptoChannelImpl channel = cryptoChannels.get(channelId);
        if (channel == null) {
            throw new Exception("Invalid channel ID: " + channelId);
        }
        
        if (!channel.isConfigured()) {
            throw new Exception("Channel not configured");
        }
        
        List<byte[]> results = new ArrayList<>();
        
        for (byte[] packet : packets) {
            if (packet.length > MAX_PACKET_SIZE) {
                throw new Exception("Packet size exceeds maximum: " + packet.length);
            }
            
            byte[] result = encrypt ? 
                channel.encrypt(packet) : 
                channel.decrypt(packet);
            
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Transform stream data
     * Based on IRSS::Infosec::CryptographicChannel::transformStream
     */
    public byte[] transformStream(long channelId, 
                                  byte[] data, 
                                  boolean som, 
                                  boolean eom,
                                  boolean encrypt) throws Exception {
        
        CryptoChannelImpl channel = cryptoChannels.get(channelId);
        if (channel == null) {
            throw new Exception("Invalid channel ID: " + channelId);
        }
        
        if (!channel.isConfigured()) {
            throw new Exception("Channel not configured");
        }
        
        // Handle stream markers
        if (som) {
            channel.resetStreamState();
        }
        
        byte[] result = encrypt ? 
            channel.encrypt(data) : 
            channel.decrypt(data);
        
        if (eom) {
            channel.finalizeStream();
        }
        
        return result;
    }
    
    /**
     * Generate hash of data
     * Based on IRSS::IandA::HashChannel::getHash
     */
    public byte[] generateHash(long channelId, byte[] data) throws Exception {
        HashChannelImpl channel = hashChannels.get(channelId);
        if (channel == null) {
            throw new Exception("Invalid hash channel ID: " + channelId);
        }
        
        channel.pushData(data);
        return channel.getHash();
    }
    
    /**
     * Configure cryptographic channel with key
     * Based on IRSS::Control::ChannelMgmt::addCryptographicConfiguration
     */
    public long addConfiguration(long channelId, long keyId) throws Exception {
        CryptoChannelImpl channel = cryptoChannels.get(channelId);
        if (channel == null) {
            throw new Exception("Invalid channel ID: " + channelId);
        }
        
        byte[] key = keyStore.getKey(keyId);
        if (key == null) {
            throw new Exception("Invalid key ID: " + keyId);
        }
        
        long configId = channel.addConfiguration(keyId, key);
        System.out.println("[CSS] Added configuration " + configId + 
                         " to channel " + channelId);
        
        return configId;
    }
    
    /**
     * Activate a configuration
     * Based on IRSS::Control::ChannelMgmt::activateConfiguration
     */
    public void activateConfiguration(long channelId, long configId) throws Exception {
        CryptoChannelImpl channel = cryptoChannels.get(channelId);
        if (channel == null) {
            throw new Exception("Invalid channel ID: " + channelId);
        }
        
        channel.activateConfiguration(configId);
        System.out.println("[CSS] Activated configuration " + configId + 
                         " on channel " + channelId);
    }
    
    /**
     * Store a new key
     * Based on IRSS::Control::KeyMgmt::updateKey
     */
    public long storeKey(byte[] keyData) {
        return keyStore.storeKey(keyData);
    }
    
    /**
     * Get maximum packet size
     */
    public int getMaxPacketSize() {
        return MAX_PACKET_SIZE;
    }
    
    /**
     * Get maximum payload size
     */
    public int getMaxPayloadSize() {
        return MAX_PAYLOAD_SIZE;
    }
    
    /**
     * Get statistics
     */
    public String getStatistics() {
        return String.format(
            "CSS Statistics:\n" +
            "  Crypto Channels: %d\n" +
            "  Hash Channels: %d\n" +
            "  Stored Keys: %d",
            cryptoChannels.size(),
            hashChannels.size(),
            keyStore.getKeyCount()
        );
    }
    
    /**
     * Cleanup resources
     */
    public void shutdown() {
        cryptoChannels.clear();
        hashChannels.clear();
        keyStore.clear();
        System.out.println("[CSS] Cryptographic Subsystem shutdown");
    }
}