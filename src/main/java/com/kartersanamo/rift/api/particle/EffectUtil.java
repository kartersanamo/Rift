package com.kartersanamo.rift.api.particle;

import com.kartersanamo.rift.api.sound.SoundUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class EffectUtil {

    private EffectUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void play(Player player, Sound sound, float volume, float pitch,
                            Particle particle, int particleCount, double particleRadius) {
        if (player == null) {
            return;
        }
        if (sound != null) {
            SoundUtil.play(player, sound, volume, pitch);
        }
        if (particle != null && particleCount > 0 && particleRadius > 0) {
            ParticleUtil.spawnCircle(player.getLocation(), particleRadius, particle, particleCount);
        }
    }
}
