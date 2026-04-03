package com.kartersanamo.rift.api.sound;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {

    // Prevent instantiation
    private SoundUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void play(Player player, Sound sound, float volume, float pitch) {
        if (player != null && sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void play(Location location, Sound sound, float volume, float pitch) {
        if (location != null && location.getWorld() != null && sound != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    public static void play(Player player, Sound sound) {
        play(player, sound, 1.0f, 1.0f);
    }
}
