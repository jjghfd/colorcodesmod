package com.example.colorcodesmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("colorcodesmod.json").toFile();
    
    private Config config;
    
    public ConfigManager() {
        this.config = new Config();
    }
    
    public void loadConfig() throws IOException {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                this.config = GSON.fromJson(reader, Config.class);
                if (this.config == null) {
                    this.config = new Config();
                }
            }
        } else {
            saveConfig();
        }
    }
    
    public void saveConfig() throws IOException {
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        }
    }
    
    public Config getConfig() {
        return config;
    }
    
    public static class Config {
        public boolean enabled = true;
        public boolean enableHexColor = true;
        public boolean enableSegmentedHexColor = true;
        public boolean showPreview = true;
        public String previewText = "Hello, World! This is a color preview. &X&R&R&G&G&B&B #RRGGBB";
        
        // Default colors for preview
        public String[] favoriteColors = {
            "#83daed",
            "#e186c9",
            "#ffffff",
            "#ff0000",
            "#00ff00",
            "#0000ff"
        };
        
        public Config() {}
    }
}