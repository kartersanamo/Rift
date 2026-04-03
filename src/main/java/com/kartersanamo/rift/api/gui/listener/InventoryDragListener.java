package com.kartersanamo.rift.api.gui.listener;

import com.kartersanamo.rift.Rift;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryDragListener implements Listener {

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Cancel drag events in GUIs
        if (Rift.getInstance().getGuiManager().getOpenGUI(player) != null) {
            event.setCancelled(true);
        }
    }
}