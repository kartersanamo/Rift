package com.kartersanamo.rift.api.util;

import org.bukkit.Location;

import java.util.Objects;

public final class LocationUtil {

    // Prevent instantiation
    private LocationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String format(Location location) {
        if (location == null) {
            return "null";
        }
        return String.format("%s: %.2f, %.2f, %.2f",
                Objects.requireNonNull(location.getWorld()).getName(),
                location.getX(),
                location.getY(),
                location.getZ());
    }
}