package com.kartersanamo.rift.api.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public final class ParticleUtil {

    // Prevent instantiation
    private ParticleUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void spawn(Location location, Particle particle, int count) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        location.getWorld().spawnParticle(particle, location, count);
    }

    public static void spawn(Player player, Location location, Particle particle, int count) {
        if (player == null || location == null) {
            return;
        }
        player.spawnParticle(particle, location, count);
    }

    public static void spawnCircle(Location center, double radius, Particle particle, int count) {
        if (center == null || center.getWorld() == null) {
            return;
        }

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location loc = new Location(center.getWorld(), x, center.getY(), z);
            spawn(loc, particle, 1);
        }
    }
}
