package com.kartersanamo.rift.listeners;

import com.kartersanamo.rift.warp.TeleportManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeleportMoveListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        assert to != null;
        if (from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ()) {
            if (TeleportManager.isPlayerTeleporting(event.getPlayer())) {
                TeleportManager.playerMoved(event.getPlayer());
            }
        }
    }
}