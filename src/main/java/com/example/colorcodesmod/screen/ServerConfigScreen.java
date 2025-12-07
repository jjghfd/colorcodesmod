package com.example.colorcodesmod.screen;

import com.example.colorcodesmod.ColorCodesMod;
import com.example.colorcodesmod.config.ServerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 5;
    
    private final Screen parent;
    private List<Element> children = new ArrayList<>();
    private List<Selectable> selectables = new ArrayList<>();
    
    private CheckboxWidget defaultEnabledCheckbox;
    private TextFieldWidget serverInputField;
    private CheckboxWidget serverEnabledCheckbox;
    private ButtonWidget addServerButton;
    
    private List<ServerEntry> serverEntries = new ArrayList<>();
    
    private ServerConfig serverConfig;
    
    public ServerConfigScreen(Screen parent) {
        super(Text.literal("Server Configuration"));
        this.parent = parent;
        this.serverConfig = ColorCodesMod.serverConfig;
    }
    
    @Override
    protected void init() {
        super.init();
        
        children.clear();
        selectables.clear();
        serverEntries.clear();
        
        int centerX = this.width / 2;
        int startY = 30;
        
        // Title
        TextWidget titleWidget = new TextWidget(
            centerX - 100, startY - 20, 200, 20,
            Text.literal("Server-Specific Settings").formatted(Formatting.BOLD),
            this.textRenderer
        );
        addDrawableChild(titleWidget);
        
        // Default enabled toggle
        TextWidget defaultEnabledLabel = new TextWidget(
            centerX - 150, startY, 120, 20,
            Text.literal("Default Enabled:"),
            this.textRenderer
        );
        addDrawableChild(defaultEnabledLabel);
        
        defaultEnabledCheckbox = new CheckboxWidget(
            centerX - 30, startY, 20,
            Text.empty(),
            this.textRenderer,
            serverConfig.isDefaultEnabled(),
            (checkbox, checked) -> {}
        );
        addDrawableChild(defaultEnabledCheckbox);
        
        // Add server section
        int addServerY = startY + 40;
        
        TextWidget addServerLabel = new TextWidget(
            centerX - 150, addServerY, 100, 20,
            Text.literal("Add Server:"),
            this.textRenderer
        );
        addDrawableChild(addServerLabel);
        
        serverInputField = new TextFieldWidget(
            this.textRenderer, centerX - 150, addServerY + 20, 150, 20,
            Text.literal("Server Address")
        );
        addDrawableChild(serverInputField);
        
        TextWidget enabledLabel = new TextWidget(
            centerX + 10, addServerY + 20, 60, 20,
            Text.literal("Enabled:"),
            this.textRenderer
        );
        addDrawableChild(enabledLabel);
        
        serverEnabledCheckbox = new CheckboxWidget(
            centerX + 70, addServerY + 20, 20,
            Text.empty(),
            this.textRenderer,
            true,
            (checkbox, checked) -> {}
        );
        addDrawableChild(serverEnabledCheckbox);
        
        addServerButton = ButtonWidget.builder(
            Text.literal("Add"),
            button -> addServer()
        ).dimensions(centerX + 100, addServerY + 20, 50, 20).build();
        addDrawableChild(addServerButton);
        
        // Server list section
        int serverListY = addServerY + 60;
        
        TextWidget serverListLabel = new TextWidget(
            centerX - 150, serverListY, 200, 20,
            Text.literal("Server Rules List:").formatted(Formatting.UNDERLINE),
            this.textRenderer
        );
        addDrawableChild(serverListLabel);
        
        // Populate server entries
        int entryY = serverListY + 25;
        for (Map.Entry<String, Boolean> entry : serverConfig.getServerRules().entrySet()) {
            addServerEntry(entry.getKey(), entry.getValue(), entryY);
            entryY += 30;
        }
        
        // Done button
        ButtonWidget doneButton = ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> close()
        ).dimensions(centerX - 75, this.height - 30, 150, BUTTON_HEIGHT).build();
        addDrawableChild(doneButton);
    }
    
    private void addServerEntry(String serverAddress, boolean enabled, int y) {
        int centerX = this.width / 2;
        
        TextWidget serverAddressWidget = new TextWidget(
            centerX - 150, y, 150, 20,
            Text.literal(serverAddress),
            this.textRenderer
        );
        addDrawableChild(serverAddressWidget);
        
        CheckboxWidget enabledCheckbox = new CheckboxWidget(
            centerX, y, 20,
            Text.empty(),
            this.textRenderer,
            enabled,
            (checkbox, checked) -> {}
        );
        addDrawableChild(enabledCheckbox);
        
        ButtonWidget removeButton = ButtonWidget.builder(
            Text.literal("Remove"),
            button -> removeServer(serverAddress)
        ).dimensions(centerX + 30, y, 80, 20).build();
        addDrawableChild(removeButton);
        
        serverEntries.add(new ServerEntry(serverAddress, enabledCheckbox, removeButton));
    }
    
    private void addServer() {
        String serverAddress = serverInputField.getText().trim();
        if (!serverAddress.isEmpty()) {
            boolean enabled = serverEnabledCheckbox.isChecked();
            serverConfig.addServerRule(serverAddress, enabled);
            
            // Refresh the screen
            init();
        }
    }
    
    private void removeServer(String serverAddress) {
        serverConfig.removeServerRule(serverAddress);
        
        // Refresh the screen
        init();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        // Render additional UI elements if needed
    }
    
    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
    
    private static class ServerEntry {
        private final String serverAddress;
        private final CheckboxWidget enabledCheckbox;
        private final ButtonWidget removeButton;
        
        public ServerEntry(String serverAddress, CheckboxWidget enabledCheckbox, ButtonWidget removeButton) {
            this.serverAddress = serverAddress;
            this.enabledCheckbox = enabledCheckbox;
            this.removeButton = removeButton;
        }
    }
}