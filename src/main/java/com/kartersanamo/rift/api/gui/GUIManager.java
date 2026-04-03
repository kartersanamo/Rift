package com.kartersanamo.rift.api.gui;

import com.kartersanamo.rift.api.gui.listener.InventoryClickListener;
import com.kartersanamo.rift.api.gui.listener.InventoryCloseListener;
import com.kartersanamo.rift.api.gui.listener.InventoryDragListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {

    private static GUIManager instance;
    private final Plugin plugin;
    private final Map<String, GUI> guis;
    private final Map<UUID, GUI> openGuis;

    public GUIManager(Plugin plugin) {
        this.plugin = plugin;
        this.guis = new HashMap<>();
        this.openGuis = new HashMap<>();
        registerListeners();
    }


    public static GUIManager getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new GUIManager(plugin);
        }
        return instance;
    }


    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new InventoryClickListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryCloseListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryDragListener(), plugin);
    }

    public void registerGUI(GUI gui) {
        if (gui != null) {
            guis.put(gui.getId(), gui);
        }
    }

    public GUI getGUI(String id) {
        return guis.get(id);
    }

    public void registerOpenGUI(Player player, GUI gui) {
        if (player != null && gui != null) {
            openGuis.put(player.getUniqueId(), gui);
        }
    }

    public void unregisterOpenGUI(Player player) {
        if (player != null) {
            openGuis.remove(player.getUniqueId());
        }
    }

    public GUI getOpenGUI(Player player) {
        if (player == null) {
            return null;
        }
        return openGuis.get(player.getUniqueId());
    }
}