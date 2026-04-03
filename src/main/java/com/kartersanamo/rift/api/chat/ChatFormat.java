package com.kartersanamo.rift.api.chat;

import com.kartersanamo.rift.api.util.MessagesUtil;

public final class ChatFormat {

    // Prevent instantiation
    private ChatFormat() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String format(String prefix, String message) {
        if (prefix == null) {
            prefix = "";
        }
        if (message == null) {
            message = "";
        }
        return ColorUtil.translate(prefix + " " + message);
    }

    public static String error(String message) {
        return format(MessagesUtil.chatPrefixDefault, "&r&c" + message);
    }

    public static String info(String message) {
        return format(MessagesUtil.chatPrefixDefault, "&r&7" + message);
    }

    public static String success(String message) {
        return format(MessagesUtil.chatPrefixSuccess, "&r&a" + message);
    }

    public static String warning(String message) {
        return format(MessagesUtil.chatPrefixWarning, "&r&e" + message);
    }
}