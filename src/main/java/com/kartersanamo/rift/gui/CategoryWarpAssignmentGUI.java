package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class CategoryWarpAssignmentGUI extends GUI {

    private final WarpManager warpManager;
    private final String categoryName;

    public CategoryWarpAssignmentGUI(WarpManager warpManager, String categoryName) {
        super("category_warp_assignment", "Warps: " + categoryName, WarpsMenuLayout.menuSizeForItems(Math.max(1, warpManager.getWarps().size())));
        this.warpManager = warpManager;
        this.categoryName = categoryName;
        build();
    }

    private void build() {
        fillFrame();

        List<Warp> warps = warpManager.getAllWarpsSortedByName();
        int capacity = WarpsMenuLayout.innerCapacity(getSize());
        List<Integer> slots = WarpsMenuLayout.distributedInnerSlots(getSize(), Math.min(capacity, warps.size()));

        for (int index = 0; index < slots.size(); index++) {
            Warp warp = warps.get(index);
            int slot = slots.get(index);

            boolean inCategory = warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(categoryName);
            Material icon = inCategory ? Material.LIME_DYE : warp.getMaterial();

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.translate("&7Current category: &b" + warp.getCategory()));
            lore.add(ColorUtil.translate(" "));
            lore.add(ColorUtil.translate("&7Left-click: assign to &b" + categoryName));
            lore.add(ColorUtil.translate("&7Right-click: move to &bdefault"));
            lore.add(ColorUtil.translate("&7Middle-click: open warp editor"));

            setItem(slot, new ItemBuilder(icon)
                    .name(ColorUtil.translate("&b" + warp.getName()))
                    .lore(lore)
                    .build());

            setClickHandler(slot, event -> handleWarpClick((Player) event.getWhoClicked(), event.getClick(), warp));
        }

        int backSlot = getSize() - 9;
        setItem(backSlot, new ItemBuilder(Material.ARROW)
                .name(ColorUtil.translate("&eBack"))
                .build());
        setClickHandler(backSlot, event -> new CategoryManagerGUI(warpManager, categoryName).open((Player) event.getWhoClicked()));
    }

    private void handleWarpClick(Player player, ClickType clickType, Warp warp) {
        if (clickType == ClickType.MIDDLE) {
            new ManageWarpsGUI(warpManager, warp).open(player);
            return;
        }

        if (clickType.isRightClick()) {
            warpManager.assignWarpCategory(warp, "default");
            AuditLogger.action(player, "category.remove-warp", "category=" + categoryName + " warp=" + warp.getName());
            player.sendMessage(ChatFormat.success("Warp moved to default."));
            new CategoryWarpAssignmentGUI(warpManager, categoryName).open(player);
            return;
        }

        warpManager.assignWarpCategory(warp, categoryName);
        AuditLogger.action(player, "category.assign", "category=" + categoryName + " warp=" + warp.getName());
        player.sendMessage(ChatFormat.success("Warp assigned to " + categoryName));
        new CategoryWarpAssignmentGUI(warpManager, categoryName).open(player);
    }

    private void fillFrame() {
        int rows = getSize() / 9;
        for (int slot = 0; slot < getSize(); slot++) {
            int row = slot / 9;
            if (row != 0 && row != rows - 1) {
                continue;
            }
            setItem(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build());
        }
    }
}

