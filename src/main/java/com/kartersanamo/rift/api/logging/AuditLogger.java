package com.kartersanamo.rift.api.logging;

import com.kartersanamo.rift.Rift;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AuditLogger {

    private AuditLogger() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void action(CommandSender sender, String action, String details) {
        String actorName = sender != null ? sender.getName() : "unknown";
        String actorType = sender instanceof Player ? "player" : "console";
        String actorId = sender instanceof Player player ? player.getUniqueId().toString() : "-";

        Rift.getLog().info(String.format(
                "[AUDIT] actor=%s actorType=%s actorId=%s action=%s details=%s",
                sanitize(actorName),
                actorType,
                sanitize(actorId),
                sanitize(action),
                sanitize(details)
        ));
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.replace(' ', '_');
    }
}

