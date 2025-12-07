package com.example.colorcodesmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SERVER_CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("colorcodesmod_servers.json").toFile();
    
    private ServerConfigData config;
    
    public ServerConfig() {
        this.config = new ServerConfigData();
    }
    
    public void loadConfig() throws IOException {
        if (SERVER_CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(SERVER_CONFIG_FILE)) {
                this.config = GSON.fromJson(reader, ServerConfigData.class);
                if (this.config == null) {
                    this.config = new ServerConfigData();
                }
            }
        } else {
            saveConfig();
        }
    }
    
    public void saveConfig() throws IOException {
        if (!SERVER_CONFIG_FILE.getParentFile().exists()) {
            SERVER_CONFIG_FILE.getParentFile().mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(SERVER_CONFIG_FILE)) {
            GSON.toJson(config, writer);
        }
    }
    
    public boolean isEnabledOnServer(String serverAddress) {
        if (serverAddress == null || serverAddress.isEmpty()) {
            return config.defaultEnabled;
        }
        
        Boolean status = config.serverRules.get(serverAddress);
        return status != null ? status : config.defaultEnabled;
    }
    
    public void addServerRule(String serverAddress, boolean enabled) {
        config.serverRules.put(serverAddress, enabled);
    }
    
    public void removeServerRule(String serverAddress) {
        config.serverRules.remove(serverAddress);
    }
    
    public Map<String, Boolean> getServerRules() {
        return Collections.unmodifiableMap(config.serverRules);
    }
    
    public boolean isDefaultEnabled() {
        return config.defaultEnabled;
    }
    
    public void setDefaultEnabled(boolean enabled) {
        config.defaultEnabled = enabled;
    }
    
    public static class ServerConfigData {
        public boolean defaultEnabled = true;
        public Map<String, Boolean> serverRules = new HashMap<>();
        
        public ServerConfigData() {}
    }
}