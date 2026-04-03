package com.kartersanamo.rift.api.gui.listener;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        GUI gui = Rift.getInstance().getGuiManager().getOpenGUI(player);

        if (gui != null) {
            gui.handleClose(event);
            Rift.getInstance().getGuiManager().unregisterOpenGUI(player);
        }
    }
}
