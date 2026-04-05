package com.kartersanamo.rift.warp;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.particle.ParticleUtil;
import com.kartersanamo.rift.api.sound.SoundUtil;
import com.kartersanamo.rift.api.util.LocationUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class TeleportManager {
    private static final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();

    public static void teleportToWarp(Player player, Warp warp) {
        if (player == null || warp == null) {
            return;
        }
        startTeleportCountdown(player, warp);
    }

    private static void startTeleportCountdown(Player player, Warp warp) {
        if (ConfigUtil.teleportDelayEnabled) {
            BukkitTask task = new BukkitRunnable() {
                int timeLeft = ConfigUtil.teleportDelaySeconds;

                @Override
                public void run() {
                    if (!player.isOnline()) {
                        removeTeleport(player.getUniqueId());
                        this.cancel();
                        return;
                    }

                    if (timeLeft <= 0) {
                        doTeleport(player, warp.getLocation());
                        this.cancel();
                        return;
                    }
                    player.sendMessage(ChatFormat.info(
                            PlaceholderUtil.replace(MessagesUtil.teleportCountdown, "%seconds%", String.valueOf(timeLeft))
                    ));
                    if (ConfigUtil.teleportDelayParticle != null) {
                        ParticleUtil.spawnCircle(
                                player.getLocation(),
                                ConfigUtil.teleportDelayParticleRadius,
                                ConfigUtil.teleportDelayParticle,
                                ConfigUtil.teleportDelayParticleCount
                        );
                    }
                    if (ConfigUtil.teleportDelaySound != null) {
                        SoundUtil.play(player, ConfigUtil.teleportDelaySound, ConfigUtil.teleportDelaySoundVolume, ConfigUtil.teleportDelaySoundPitch);
                    }
                    timeLeft -= 1;
                }
            }.runTaskTimer(Rift.getInstance(), 0L, 20L);
            tasks.put(player.getUniqueId(), task);
        } else {
            doTeleport(player, warp.getLocation());
        }
    }

    private static void doTeleport(Player player, Location toLocation) {
        if (toLocation == null || toLocation.getWorld() == null) {
            player.sendMessage(ChatFormat.error(MessagesUtil.teleportInvalidTarget));
            removeTeleport(player.getUniqueId());
            return;
        }

        player.teleport(toLocation);
        if (ConfigUtil.teleportCompleteSound != null) {
            SoundUtil.play(player, ConfigUtil.teleportCompleteSound, ConfigUtil.teleportCompleteSoundVolume, ConfigUtil.teleportCompleteSoundPitch);
        }
        if (ConfigUtil.teleportCompleteParticle != null) {
            ParticleUtil.spawnCircle(
                    player.getLocation(),
                    ConfigUtil.teleportCompleteParticleRadius,
                    ConfigUtil.teleportCompleteParticle,
                    ConfigUtil.teleportCompleteParticleCount
            );
        }
        player.sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.teleportSuccessLocation,
                        "%location%", LocationUtil.format(toLocation)
                )
        ));
        removeTeleport(player.getUniqueId());
    }

    public static void playerMoved(Player player) {
        BukkitTask task = tasks.get(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        removeTeleport(player.getUniqueId());
        player.sendMessage(ChatFormat.error(MessagesUtil.teleportCancelledMoved));
    }

    public static boolean isPlayerTeleporting(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    public static void removeTeleport(UUID uuid) {
        BukkitTask task = tasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}
