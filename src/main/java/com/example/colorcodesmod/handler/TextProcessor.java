package com.example.colorcodesmod.handler;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class TextProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger("colorcodesmod");
    
    private static TextProcessor instance;
    
    private boolean isEnabled = true;
    private KeyBinding toggleKeyBinding;
    private long lastProcessTime = 0;
    private static final long PROCESS_DELAY = 50; // 50ms delay between processing to prevent lag
    
    private TextProcessor() {
        // Private constructor for singleton
    }
    
    public static TextProcessor getInstance() {
        if (instance == null) {
            instance = new TextProcessor();
        }
        return instance;
    }
    
    public void initialize() {
        registerKeyBindings();
        registerScreenEvents();
        LOGGER.info("TextProcessor initialized");
    }
    
    private void registerKeyBindings() {
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.colorcodesmod.toggle_text_processing",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.colorcodesmod.general"
        ));
    }
    
    private void registerScreenEvents() {
        // Register screen events to handle chat input
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen) {
                // In Minecraft 1.20.1, we need to access the chat input field differently
                // This is a simplified approach that works for most cases
                try {
                    // Use reflection to get the chat input field (this is a fallback for older versions)
                    java.lang.reflect.Field field = ChatScreen.class.getDeclaredField("chatField");
                    field.setAccessible(true);
                    TextFieldWidget chatInput = (TextFieldWidget) field.get(screen);
                    
                    if (chatInput != null) {
                        // Store original message for comparison using AtomicReference to make it effectively final
                        java.util.concurrent.atomic.AtomicReference<String> originalMessageRef = new java.util.concurrent.atomic.AtomicReference<>(chatInput.getText());
                        
                        // Add text change listener for real-time processing
                        chatInput.setChangedListener(text -> {
                            // Only process if text has changed and enough time has passed
                            long currentTime = System.currentTimeMillis();
                            if (!text.equals(originalMessageRef.get()) && (currentTime - lastProcessTime > PROCESS_DELAY)) {
                                lastProcessTime = currentTime;
                                // Process text in background (non-blocking)
                                processTextInBackground(text, processedText -> {
                                    // Update input field with processed text if it's still the same original text
                                    MinecraftClient.getInstance().execute(() -> {
                                        if (chatInput != null && chatInput.getText().equals(text)) {
                                            chatInput.setText(processedText);
                                            chatInput.setCursor(chatInput.getText().length(), false);
                                        }
                                    });
                                });
                            }
                            originalMessageRef.set(text);
                        });
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to access chat input field: {}", e.getMessage());
                }
            }
        });
    }
    
    private void processTextInBackground(String text, Consumer<String> callback) {
        // Process text in a separate thread to avoid blocking the main thread
        new Thread(() -> {
            String processedText = processText(text);
            callback.accept(processedText);
        }).start();
    }
    
    public String processText(String text) {
        if (!isEnabled) {
            return text;
        }
        
        // Here we implement the actual text processing logic
        // For now, we'll use the existing hex color processing
        String processedText = text;
        
        // Process segmented hex colors (&X&R&R&G&G&B&B)
        processedText = ChatColorHandler.getInstance().previewHexColors(processedText);
        
        return processedText;
    }
    
    private void renderStatusIndicator(DrawContext context, net.minecraft.client.gui.screen.Screen screen, TextFieldWidget chatInput) {
        if (chatInput == null) return;
        
        // Get chat input position and size
        int inputX = chatInput.getX();
        int inputY = chatInput.getY();
        int inputWidth = chatInput.getWidth();
        
        // Render status indicator in top-right corner of chat input
        String status = isEnabled ? "ON" : "OFF";
        int color = isEnabled ? 0xFF00FF00 : 0xFFFF0000;
        
        context.drawTextWithShadow(
            MinecraftClient.getInstance().textRenderer,
            status,
            inputX + inputWidth - 20,
            inputY - 15,
            color
        );
    }
    
    public void toggleEnabled() {
        isEnabled = !isEnabled;
        LOGGER.info("Text processing {}abled", isEnabled ? "en" : "dis");
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    public KeyBinding getToggleKeyBinding() {
        return toggleKeyBinding;
    }
    
    // Update key binding based on config
    public void updateKeyBinding(int keyCode) {
        // This would be implemented if we allow custom key bindings
        // For now, we'll keep it simple
    }
}