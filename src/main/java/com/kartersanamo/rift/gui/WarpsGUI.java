package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarpsGUI extends GUI {

    private final WarpManager warpManager;

    public WarpsGUI(WarpManager warpManager) {
        super("warps_gui",
                ColorUtil.translate(
                        PlaceholderUtil.replace(
                                MessagesUtil.warpsGuiTitle,
                                "%count%", String.valueOf(warpManager.getWarpCount())
                        )),
                calculateSize(warpManager)
        );
        this.warpManager = warpManager;
        build();
    }

    private static int calculateSize(WarpManager warpManager) {
        int homeCount = warpManager.getWarpCount();
        if (homeCount == 0) {
            return ConfigUtil.warpsGuiMinSize;
        }

        int rows = Math.min(ConfigUtil.warpsGuiMaxRows, (int) Math.ceil(homeCount / 9.0));
        return Math.max(ConfigUtil.warpsGuiMinSize, rows * 9);
    }

    public void build() {
        Map<String, Warp> warps = warpManager.getWarps();

        if (warps == null || warps.isEmpty()) {
            setItem(4, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate(MessagesUtil.warpsGuiEmptyName))
                    .lore(ColorUtil.translate(MessagesUtil.warpsGuiEmptyLore))
                    .build());
            return;
        }

        int slot = 0;
        for (Warp warp : warps.values()) {
            if (slot >= getSize()) break;

            // Build description
            List<String> description = warp.getDescription();
            List<String> displayLore = new ArrayList<>();

            // Add a warp description if it exists
            if (description != null && !description.isEmpty()) {
                for (String line : description) {
                    displayLore.add(ColorUtil.translate(line));
                }
                displayLore.add(ColorUtil.translate(MessagesUtil.blankLine)); // Empty line separator
            }

            // Add instruction lines
            displayLore.add(ColorUtil.translate(MessagesUtil.warpsGuiInstructionTeleport));
            displayLore.add(ColorUtil.translate(MessagesUtil.warpsGuiInstructionManage));

            // Create item
            ItemStack item = new ItemBuilder(warp.getMaterial())
                    .name(ColorUtil.translate(
                            PlaceholderUtil.replace(
                                    MessagesUtil.warpsGuiHomeName,
                                    "%name%", warp.getName()
                            )
                    ))
                    .lore(displayLore)
                    .build();

            final String homeName = warp.getName();
            setItem(slot, item);
            setClickHandler(slot, event -> handleClick(event, homeName));
            slot++;
        }
    }

    private void handleClick(InventoryClickEvent event, String warpName) {
        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        ClickType clickType = event.getClick();

        // Right-click to manage
        if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            Warp warp = warpManager.getWarp(warpName);
            System.out.println("TODO: Open ManageWarpsGUI for " + warpName + ".");
            ManageWarpsGUI gui = new ManageWarpsGUI(warpManager, warp);
            gui.open(player);
        }

        // Left-click to tp
        else if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT) {
            Warp warp = warpManager.getWarp(warpName);
            clicker.closeInventory();
            warp.queueTeleport(clicker);
            warpManager.update(warp);
        }
    }
}
