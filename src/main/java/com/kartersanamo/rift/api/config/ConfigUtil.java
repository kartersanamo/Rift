package com.kartersanamo.rift.api.config;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtil {

    public static boolean teleportDelayEnabled;
    public static int teleportDelaySeconds;
    public static Particle teleportDelayParticle;
    public static int teleportDelayParticleRadius;
    public static int teleportDelayParticleCount;
    public static Sound teleportDelaySound;
    public static int teleportDelaySoundVolume;
    public static int teleportDelaySoundPitch;
    public static Sound teleportCompleteSound;
    public static int teleportCompleteSoundVolume;
    public static int teleportCompleteSoundPitch;
    public static Particle teleportCompleteParticle;
    public static int teleportCompleteParticleRadius;
    public static int teleportCompleteParticleCount;

    public static int warpNameMinLength;
    public static int warpNameMaxLength;
    public static int warpDescriptionMaxLines;
    public static int warpDescriptionMaxLength;
    public static int warpsGuiMinSize;
    public static int warpsGuiMaxRows;

    public static void load(FileConfiguration cfg) {
        teleportDelayEnabled = cfg.getBoolean("teleport-delay.enabled", true);
        teleportDelaySeconds = clamp(cfg.getInt("teleport-delay.seconds", 5), 1, 60);
        teleportDelayParticle = cfg.contains("teleport-delay.particle") ? parseParticle(cfg.getString("teleport-delay.particle")) : Particle.HAPPY_VILLAGER;
        teleportDelayParticleRadius = clamp(cfg.getInt("teleport-delay.particle-radius", 1), 0, 10);
        teleportDelayParticleCount = clamp(cfg.getInt("teleport-delay.particle-count", 10), 0, 100);
        teleportDelaySound = cfg.contains("teleport-delay.sound") ? parseSound(cfg.getString("teleport-delay.sound")) : Sound.ENTITY_ENDERMAN_TELEPORT;
        teleportDelaySoundVolume = clamp(cfg.getInt("teleport-delay.sound-volume", 1), 0, 10);
        teleportDelaySoundPitch = clamp(cfg.getInt("teleport-delay.sound-pitch", 1), 0, 10);
        teleportCompleteSound = cfg.contains("teleport-complete.sound") ? parseSound(cfg.getString("teleport-complete.sound")) : Sound.ENTITY_ENDERMAN_TELEPORT;
        teleportCompleteSoundVolume = clamp(cfg.getInt("teleport-complete.sound-volume", 1), 0, 10);
        teleportCompleteSoundPitch = clamp(cfg.getInt("teleport-complete.sound-pitch", 1), 0, 10);
        teleportCompleteParticle = cfg.contains("teleport-complete.particle") ? parseParticle(cfg.getString("teleport-complete.particle")) : Particle.FLAME;
        teleportCompleteParticleRadius = clamp(cfg.getInt("teleport-complete.particle-radius", 1), 0, 10);
        teleportCompleteParticleCount = clamp(cfg.getInt("teleport-complete.particle-count", 10), 0, 100);

        warpNameMinLength = clamp(cfg.getInt("warp-name.min-length", 3), 1, 100);
        warpNameMaxLength = clamp(cfg.getInt("warp-name.max-length", 16), 1, 100);
        warpDescriptionMaxLines = clamp(cfg.getInt("warp-description.max-lines", 10), 1, 50);
        warpDescriptionMaxLength = clamp(cfg.getInt("warp-description.max-length-per-line", 100), 20, 500);
        warpsGuiMinSize = normalizeGuiSize(cfg.getInt("warps.gui.min-size", 9), 9);
        warpsGuiMaxRows = clamp(cfg.getInt("warps.gui.max-rows", 6), 1, 6);
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