package com.example.colorcodesmod;

import com.example.colorcodesmod.config.ConfigManager;
import com.example.colorcodesmod.config.ServerConfig;
import com.example.colorcodesmod.handler.ChatColorHandler;
import com.example.colorcodesmod.handler.TextProcessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ColorCodesMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("colorcodesmod");
    public static final String MOD_ID = "colorcodesmod";
    
    public static ConfigManager configManager;
    public static ServerConfig serverConfig;
    public static ChatColorHandler chatColorHandler;
    
    private static KeyBinding openConfigScreenKey;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Color Codes Mod for Minecraft 1.21.4");
        
        try {
            configManager = new ConfigManager();
            serverConfig = new ServerConfig();
            chatColorHandler = ChatColorHandler.getInstance();
            
            // Initialize TextProcessor
            TextProcessor textProcessor = TextProcessor.getInstance();
            textProcessor.initialize();
            
            // Initialize configs
            configManager.loadConfig();
            serverConfig.loadConfig();
            
            // Register lifecycle events
            ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
                LOGGER.info("Client started, registering handlers");
            });
            
            ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
                try {
                    configManager.saveConfig();
                    serverConfig.saveConfig();
                    LOGGER.info("Configs saved successfully");
                } catch (IOException e) {
                    LOGGER.error("Failed to save configs: {}", e.getMessage());
                }
            });
            
            // Register send message event to process outgoing messages
            ClientSendMessageEvents.CHAT.register(message -> {
                // Note: In Minecraft 1.20.1, this event is a consumer, not a function that returns a value
                // The actual processing is handled by the TextProcessor's real-time input handling
            });
            
            // Register key bindings
            registerKeyBindings();
            
            // Register tick events for key binding handling
            ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
            
            LOGGER.info("Color Codes Mod initialized successfully");
            
        } catch (IOException e) {
            LOGGER.error("Failed to initialize mod: {}", e.getMessage());
        }
    }
    
    private void registerKeyBindings() {
        openConfigScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.colorcodesmod.open_config",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_U,
            "category.colorcodesmod.general"
        ));
    }
    
    private void onClientTick(net.minecraft.client.MinecraftClient client) {
        if (openConfigScreenKey.wasPressed() && client.player != null) {
            client.setScreen(new com.example.colorcodesmod.screen.ColorConfigScreen(client.currentScreen));
        }
        
        // Handle TextProcessor toggle key
        if (TextProcessor.getInstance().getToggleKeyBinding().wasPressed()) {
            TextProcessor.getInstance().toggleEnabled();
        }
    }
}