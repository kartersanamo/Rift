package com.kartersanamo.rift.api.config;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtil {

    // Config values here as 'public static'

    public static void load(FileConfiguration cfg) {
        return;
    }

    private static int normalizeGuiSize(int size, int fallback) {
        if (size < 9 || size > 54 || size % 9 != 0) {
            return fallback;
        }
        return size;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Sound parseSound(String value) {
        if (value == null || value.equalsIgnoreCase("none")) {
            return null;
        }
        try {
            return Sound.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Particle parseParticle(String value) {
        if (value == null || value.equalsIgnoreCase("none")) {
            return null;
        }
        try {
            return Particle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Material parseMaterial(String value, Material fallback) {
        if (value == null) {
            return fallback;
        }
        Material material = Material.matchMaterial(value);
        return material != null ? material : fallback;
    }
}