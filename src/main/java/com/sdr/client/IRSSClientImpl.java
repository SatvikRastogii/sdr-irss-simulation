package com.sdr.client;

import com.sdr.server.IRSSServer.Request;
import com.sdr.server.IRSSServer.Response;
import java.util.*;

/**
 * IRSS Client Implementation
 * Provides convenient methods for IRSS API operations
 * Wraps network communication with type-safe interface
 */
public class IRSSClientImpl {
    
    private WaveformClient client;
    
    public IRSSClientImpl(WaveformClient client) {
        this.client = client;
    }
    
    /**
     * Create cryptographic channel
     * Based on IRSS::Control::ChannelMgmt::createCryptographicChannel
     */
    public long createCryptographicChannel(int cryptoModuleId,
                                          int ptEndpoint,
                                          int ctEndpoint,
                                          String algorithm,
                                          String duplexity) throws Exception {
        
        Request request = new Request("CREATE_CRYPTO_CHANNEL");
        request.addParameter("cryptoModuleId", cryptoModuleId);
        request.addParameter("ptEndpoint", ptEndpoint);
        request.addParameter("ctEndpoint", ctEndpoint);
        request.addParameter("algorithm", algorithm);
        request.addParameter("duplexity", duplexity);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to create channel: " + response.getMessage());
        }
        
        return (Long) response.getData();
    }
    
    /**
     * Create hash channel
     * Based on IRSS::Control::ChannelMgmt::createHashChannel
     */
    public long createHashChannel(String algorithm) throws Exception {
        Request request = new Request("CREATE_HASH_CHANNEL");
        request.addParameter("algorithm", algorithm);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to create hash channel: " + response.getMessage());
        }
        
        return (Long) response.getData();
    }
    
    /**
     * Destroy channel
     * Based on IRSS::Control::ChannelMgmt::destroyChannel
     */
    public void destroyChannel(long channelId) throws Exception {
        Request request = new Request("DESTROY_CHANNEL");
        request.addParameter("channelId", channelId);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to destroy channel: " + response.getMessage());
        }
    }
    
    /**
     * Store encryption key
     * Based on IRSS::Control::KeyMgmt operations
     */
    public long storeKey(byte[] keyData) throws Exception {
        Request request = new Request("STORE_KEY");
        request.addParameter("keyData", keyData);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to store key: " + response.getMessage());
        }
        
        return (Long) response.getData();
    }
    
    /**
     * Add configuration to channel
     * Based on IRSS::Control::ChannelMgmt::addCryptographicConfiguration
     */
    public long addConfiguration(long channelId, long keyId) throws Exception {
        Request request = new Request("ADD_CONFIGURATION");
        request.addParameter("channelId", channelId);
        request.addParameter("keyId", keyId);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to add configuration: " + response.getMessage());
        }
        
        return (Long) response.getData();
    }
    
    /**
     * Activate configuration
     * Based on IRSS::Control::ChannelMgmt::activateConfiguration
     */
    public void activateConfiguration(long channelId, long configId) throws Exception {
        Request request = new Request("ACTIVATE_CONFIGURATION");
        request.addParameter("channelId", channelId);
        request.addParameter("configId", configId);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to activate configuration: " + response.getMessage());
        }
    }
    
    /**
     * Encrypt packets
     * Based on IRSS::Infosec::CryptographicChannel::transformPackets
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> encryptPackets(long channelId, List<byte[]> packets) throws Exception {
        Request request = new Request("ENCRYPT_PACKETS");
        request.addParameter("channelId", channelId);
        request.addParameter("packets", packets);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to encrypt: " + response.getMessage());
        }
        
        return (List<byte[]>) response.getData();
    }
    
    /**
     * Decrypt packets
     * Based on IRSS::Infosec::CryptographicChannel::transformPackets
     */
    @SuppressWarnings("unchecked")
    public List<byte[]> decryptPackets(long channelId, List<byte[]> packets) throws Exception {
        Request request = new Request("DECRYPT_PACKETS");
        request.addParameter("channelId", channelId);
        request.addParameter("packets", packets);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to decrypt: " + response.getMessage());
        }
        
        return (List<byte[]>) response.getData();
    }
    
    /**
     * Generate hash
     * Based on IRSS::IandA::HashChannel::getHash
     */
    public byte[] generateHash(long channelId, byte[] data) throws Exception {
        Request request = new Request("GENERATE_HASH");
        request.addParameter("channelId", channelId);
        request.addParameter("data", data);
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to generate hash: " + response.getMessage());
        }
        
        return (byte[]) response.getData();
    }
    
    /**
     * Get system statistics
     */
    public String getStatistics() throws Exception {
        Request request = new Request("GET_STATS");
        
        Response response = client.sendRequest(request);
        
        if (!response.isSuccess()) {
            throw new Exception("Failed to get statistics: " + response.getMessage());
        }
        
        return response.getMessage();
    }
}