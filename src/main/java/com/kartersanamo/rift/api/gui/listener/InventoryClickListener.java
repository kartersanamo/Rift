package com.kartersanamo.rift.api.gui.listener;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        GUI gui = Rift.getInstance().getGuiManager().getOpenGUI(player);

        if (gui != null) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }
}
