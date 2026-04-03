package com.kartersanamo.rift.api.util;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class PlaceholderUtil {
    public static String message(FileConfiguration cfg, String key, String fallback, Map<String, String> placeholders) {
        String msg = cfg.getString(key);
        if (msg == null) msg = fallback;

        return replace(msg, placeholders);
    }

    public static String replace(String message, Map<String, String> placeholders) {
        String msg = message;
        if (msg == null) {
            return null;
        }
        if (placeholders != null) {
            for (var e : placeholders.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k != null && v != null) {
                    msg = msg.replace(k, v);
                }
            }
        }
        return msg;
    }

    public static String replace(String message, String... placeholders) {
        String msg = message;
        if (msg == null) {
            return null;
        }
        if (placeholders != null) {
            for (int i = 0; i + 1 < placeholders.length; i += 2) {
                String k = placeholders[i];
                String v = placeholders[i + 1];
                if (k != null && v != null) {
                    msg = msg.replace(k, v);
                }
            }
        }
        return msg;
    }
}
