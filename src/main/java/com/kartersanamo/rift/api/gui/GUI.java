package com.kartersanamo.rift.api.gui;

import com.kartersanamo.rift.Rift;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GUI {

    private final String id;
    private final String title;
    private final int size;
    private final Inventory inventory;
    private final Map<Integer, ItemStack> items;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;
    private Consumer<InventoryCloseEvent> closeHandler;

    public GUI(String id, String title, int size) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.items = new HashMap<>();
        this.clickHandlers = new HashMap<>();
    }

    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < size) {
            items.put(slot, item);
            inventory.setItem(slot, item);
        }
    }

    public void setClickHandler(int slot, Consumer<InventoryClickEvent> handler) {
        if (slot >= 0 && slot < size) {
            clickHandlers.put(slot, handler);
        }
    }

    public GUI setCloseHandler(Consumer<InventoryCloseEvent> handler) {
        this.closeHandler = handler;
        return this;
    }

    public void open(Player player) {
        if (player != null) {
            player.openInventory(inventory);
            GUIManager guiM = Rift.getInstance().getGuiManager();
            if (guiM == null) {
                return;
            }
            guiM.registerOpenGUI(player, this);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        Consumer<InventoryClickEvent> handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(event);
        }
    }

    public void handleClose(InventoryCloseEvent event) {
        if (closeHandler != null) {
            closeHandler.accept(event);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void update() {
        // Update all items
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }
}