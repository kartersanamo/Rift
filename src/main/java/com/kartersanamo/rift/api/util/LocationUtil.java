package com.kartersanamo.rift.api.util;

import org.bukkit.Location;

public final class LocationUtil {

    // Prevent instantiation
    private LocationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String format(Location location) {
        if (location == null || location.getWorld() == null) {
            return "null";
        }
        return String.format("%s: %.2f, %.2f, %.2f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
    }
}