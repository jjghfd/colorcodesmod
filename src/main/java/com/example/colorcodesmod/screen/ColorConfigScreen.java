package com.example.colorcodesmod.screen;

import com.example.colorcodesmod.ColorCodesMod;
import com.example.colorcodesmod.config.ConfigManager;
import com.example.colorcodesmod.config.ServerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;

public class ColorConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 5;
    
    private final Screen parent;
    
    private TextFieldWidget previewTextField;
    private TextWidget previewTextWidget;
    
    private ButtonWidget toggleHexColorButton;
    private ButtonWidget toggleSegmentedHexButton;
    private ButtonWidget serverConfigButton;
    private ButtonWidget saveButton;
    private ButtonWidget resetButton;
    
    private ConfigManager.Config config;
    private ServerConfig serverConfig;
    
    public ColorConfigScreen(Screen parent) {
        super(Text.literal("Color Codes Mod Config"));
        this.parent = parent;
        this.config = ColorCodesMod.configManager.getConfig();
        this.serverConfig = ColorCodesMod.serverConfig;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = 30;
        
        // Title
        TextWidget titleWidget = new TextWidget(
            centerX - 100, startY - 20, 200, 20,
            Text.literal("Color Codes Mod Configuration").formatted(Formatting.BOLD),
            this.textRenderer
        );
        addDrawableChild(titleWidget);
        
        // Preview text field
        previewTextField = new TextFieldWidget(
            this.textRenderer, centerX - 150, startY, 300, 20,
            Text.literal("Preview Text")
        );
        previewTextField.setText(config.previewText);
        previewTextField.setChangedListener(this::onPreviewTextChanged);
        addDrawableChild(previewTextField);
        
        // Preview display area
        previewTextWidget = new TextWidget(
            centerX - 150, startY + 30, 300, 60,
            Text.literal(""),
            this.textRenderer
        );
        addDrawableChild(previewTextWidget);
        updatePreviewText();
        
        // Toggle buttons
        int buttonStartX = centerX - BUTTON_WIDTH / 2;
        int buttonY = startY + 100;
        
        toggleHexColorButton = ButtonWidget.builder(
            getToggleButtonText("Standard Hex Colors", config.enableHexColor),
            button -> toggleHexColor()
        ).dimensions(buttonStartX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(toggleHexColorButton);
        
        buttonY += BUTTON_HEIGHT + SPACING;
        
        toggleSegmentedHexButton = ButtonWidget.builder(
            getToggleButtonText("Segmented Hex Colors", config.enableSegmentedHexColor),
            button -> toggleSegmentedHex()
        ).dimensions(buttonStartX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(toggleSegmentedHexButton);
        
        buttonY += BUTTON_HEIGHT + SPACING;
        
        serverConfigButton = ButtonWidget.builder(
            Text.literal("Server Configuration"),
            button -> openServerConfig()
        ).dimensions(buttonStartX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(serverConfigButton);
        
        // Save and reset buttons
        int bottomButtonStartX = centerX - BUTTON_WIDTH - SPACING;
        int bottomY = this.height - 30;
        
        saveButton = ButtonWidget.builder(
            Text.literal("Save"),
            button -> saveConfig()
        ).dimensions(bottomButtonStartX, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(saveButton);
        
        resetButton = ButtonWidget.builder(
            Text.literal("Reset"),
            button -> resetConfig()
        ).dimensions(centerX + SPACING, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        addDrawableChild(resetButton);
        
        // Done button
        ButtonWidget doneButton = ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> close()
        ).dimensions(centerX - 75, bottomY + BUTTON_HEIGHT + SPACING, 150, BUTTON_HEIGHT).build();
        addDrawableChild(doneButton);
    }
    
    private Text getToggleButtonText(String featureName, boolean enabled) {
        String status = enabled ? "Enabled" : "Disabled";
        Formatting statusFormat = enabled ? Formatting.GREEN : Formatting.RED;
        return Text.literal(featureName + ": " + status).formatted(statusFormat);
    }
    
    private void toggleHexColor() {
        config.enableHexColor = !config.enableHexColor;
        toggleHexColorButton.setMessage(getToggleButtonText("Standard Hex Colors", config.enableHexColor));
        updatePreviewText();
    }
    
    private void toggleSegmentedHex() {
        config.enableSegmentedHexColor = !config.enableSegmentedHexColor;
        toggleSegmentedHexButton.setMessage(getToggleButtonText("Segmented Hex Colors", config.enableSegmentedHexColor));
        updatePreviewText();
    }
    
    private void openServerConfig() {
        MinecraftClient.getInstance().setScreen(new ServerConfigScreen(this));
    }
    
    private void saveConfig() {
        try {
            ColorCodesMod.configManager.saveConfig();
            ColorCodesMod.serverConfig.saveConfig();
            // In Minecraft 1.20.1, we can't use addMessage, so we'll log it instead
            ColorCodesMod.LOGGER.info("Config saved successfully!");
        } catch (IOException e) {
            ColorCodesMod.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }
    
    private void resetConfig() {
        this.config = new ConfigManager.Config();
        ColorCodesMod.configManager.getConfig().enabled = config.enabled;
        ColorCodesMod.configManager.getConfig().enableHexColor = config.enableHexColor;
        ColorCodesMod.configManager.getConfig().enableSegmentedHexColor = config.enableSegmentedHexColor;
        ColorCodesMod.configManager.getConfig().showPreview = config.showPreview;
        ColorCodesMod.configManager.getConfig().previewText = config.previewText;
        ColorCodesMod.configManager.getConfig().favoriteColors = config.favoriteColors;
        
        previewTextField.setText(config.previewText);
        toggleHexColorButton.setMessage(getToggleButtonText("Standard Hex Colors", config.enableHexColor));
        toggleSegmentedHexButton.setMessage(getToggleButtonText("Segmented Hex Colors", config.enableSegmentedHexColor));
        updatePreviewText();
        
        // Log the reset instead of using addMessage
        ColorCodesMod.LOGGER.info("Config reset to defaults!");
    }
    
    private void onPreviewTextChanged(String text) {
        config.previewText = text;
        updatePreviewText();
    }
    
    private void updatePreviewText() {
        String previewText = config.previewText;
        // Process the text with hex colors for preview
        previewText = ColorCodesMod.chatColorHandler.previewHexColors(previewText);
        // Convert to MutableText for proper rendering
        net.minecraft.text.MutableText processedText = net.minecraft.text.Text.literal(previewText);
        processedText = ColorCodesMod.chatColorHandler.processHexColors(processedText);
        previewTextWidget.setMessage(processedText);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}