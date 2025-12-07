package com.example.colorcodesmod.handler;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("colorcodesmod");
    private static ChatColorHandler instance;
    
    // Regex patterns for color codes
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#([0-9A-Fa-f]{6})");
    private static final Pattern SEGMENTED_HEX_PATTERN = Pattern.compile("&X&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])");
    
    private ChatColorHandler() {
        registerMessageHandlers();
    }
    
    public static ChatColorHandler getInstance() {
        if (instance == null) {
            instance = new ChatColorHandler();
        }
        return instance;
    }
    
    private void registerMessageHandlers() {
        // Register send message event to handle outgoing messages (for preview)
        ClientSendMessageEvents.CHAT.register(message -> {
            // This is just for preview purposes, the actual processing happens on the server side
            // But we can log it for debugging
            LOGGER.debug("Outgoing message: {}", message);
        });
        
        // Note: In Minecraft 1.20.1, incoming chat message events are consumers, not functions
        // They don't allow modifying the message directly
        // We'll use a different approach for handling incoming messages (e.g., Mixins)
    }
    
    private void handleOutgoingMessage(String message) {
        // This is just for preview purposes, the actual processing happens on the server side
        // But we can log it for debugging
        LOGGER.debug("Outgoing message: {}", message);
    }
    
    public MutableText processHexColors(Text text) {
        // Convert the text to string for regex processing
        String rawText = text.getString();
        
        // Process segmented hex colors first (&X&R&R&G&G&B&B)
        rawText = processSegmentedHexColors(rawText);
        
        // Process standard hex colors (#RRGGBB)
        rawText = processStandardHexColors(rawText);
        
        // Reconstruct the text with formatting
        return parseFormattedText(rawText);
    }
    
    // Overload for MutableText
    public MutableText processHexColors(MutableText text) {
        return processHexColors((Text) text);
    }
    
    private String processSegmentedHexColors(String text) {
        Matcher matcher = SEGMENTED_HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        
        while (matcher.find()) {
            result.append(text, lastIndex, matcher.start());
            
            // Extract the hex digits
            String r1 = matcher.group(1);
            String r2 = matcher.group(2);
            String g1 = matcher.group(3);
            String g2 = matcher.group(4);
            String b1 = matcher.group(5);
            String b2 = matcher.group(6);
            
            // Combine into a standard hex color
            String hexColor = "#" + r1 + r2 + g1 + g2 + b1 + b2;
            result.append(hexColor);
            
            lastIndex = matcher.end();
        }
        
        result.append(text.substring(lastIndex));
        return result.toString();
    }
    
    private String processStandardHexColors(String text) {
        // This method processes #RRGGBB colors and converts them to Minecraft formatting
        // We'll handle the actual formatting in parseFormattedText
        return text;
    }
    
    private MutableText parseFormattedText(String text) {
        MutableText result = Text.empty();
        List<Formatting> activeFormattings = new ArrayList<>();
        
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            
            if (c == '#' && i + 6 < text.length()) {
                // Check if this is a hex color
                String hex = text.substring(i, i + 7);
                if (HEX_COLOR_PATTERN.matcher(hex).matches()) {
                    // Apply the hex color
                    Color color = Color.decode(hex);
                    int rgb = color.getRGB();
                    Style style = Style.EMPTY.withColor(rgb);
                    
                    // Add active formattings to the style
                    for (Formatting formatting : activeFormattings) {
                        style = style.withFormatting(formatting);
                    }
                    
                    result.setStyle(style);
                    i += 7;
                    continue;
                }
            }
            
            if (c == '&' && i + 1 < text.length()) {
                // Handle Minecraft color codes
                char colorCode = text.charAt(i + 1);
                Formatting formatting = Formatting.byCode(colorCode);
                
                if (formatting != null) {
                    if (formatting.isColor()) {
                        // Clear all active formattings if it's a color
                        activeFormattings.clear();
                        Style style = Style.EMPTY.withFormatting(formatting);
                        result.setStyle(style);
                    } else if (formatting == Formatting.RESET) {
                        // Reset all formattings
                        activeFormattings.clear();
                        result.setStyle(Style.EMPTY);
                    } else {
                        // Add formatting to active list
                        if (!activeFormattings.contains(formatting)) {
                            activeFormattings.add(formatting);
                            Style style = result.getStyle();
                            for (Formatting fmt : activeFormattings) {
                                style = style.withFormatting(fmt);
                            }
                            result.setStyle(style);
                        }
                    }
                    i += 2;
                    continue;
                }
            }
            
            // Add the character as-is with current style
            result.append(Text.literal(String.valueOf(c)).setStyle(result.getStyle()));
            i++;
        }
        
        return result;
    }
    
    // Utility method to preview color formatting
    public String previewHexColors(String text) {
        // Process the text and return the formatted version
        String processedText = processSegmentedHexColors(text);
        processedText = processStandardHexColors(processedText);
        return processedText;
    }
    
    // Method to convert hex string to Color object
    public static Color hexToColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid hex color: {}", hex);
            return Color.WHITE;
        }
    }
    
    // Method to validate hex color strings
    public static boolean isValidHexColor(String hex) {
        return HEX_COLOR_PATTERN.matcher(hex).matches();
    }
}