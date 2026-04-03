package com.kartersanamo.rift.api.chat;

import org.bukkit.ChatColor;

public final class ColorUtil {

    // Prevent instantiation
    private ColorUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String translate(String text) {
        if (text == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }


    public static String strip(String text) {
        if (text == null) {
            return null;
        }
        return ChatColor.stripColor(text);
    }

    public static String colorize(ChatColor color, String text) {
        if (text == null) {
            return null;
        }
        return color + text;
    }
}