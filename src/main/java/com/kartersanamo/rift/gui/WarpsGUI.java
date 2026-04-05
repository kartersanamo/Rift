package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.chat.ChatFormat;
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

public class WarpsGUI extends GUI {

    private final WarpManager warpManager;
    private final String categoryFilter;
    private final boolean categoryMenu;
    private final boolean viewerCanManage;

    public WarpsGUI(WarpManager warpManager) {
        this(warpManager, null, null);
    }

    public WarpsGUI(WarpManager warpManager, String categoryFilter) {
        this(warpManager, categoryFilter, null);
    }

    public WarpsGUI(WarpManager warpManager, String categoryFilter, Player viewer) {
        super("warps_gui",
                buildTitle(warpManager, categoryFilter),
                calculateSize(warpManager, categoryFilter)
        );
        this.warpManager = warpManager;
        this.categoryFilter = normalizeFilter(categoryFilter);
        this.categoryMenu = shouldShowCategoryMenu(warpManager, this.categoryFilter);
        this.viewerCanManage = viewer != null && viewer.hasPermission("rift.warp.manage");
        build();
    }

    private static String buildTitle(WarpManager warpManager, String categoryFilter) {
        if (shouldShowCategoryMenu(warpManager, normalizeFilter(categoryFilter))) {
            return ColorUtil.translate("&7Warp Categories");
        }

        int count = warpManager.getWarpsInCategory(categoryFilter).size();
        String title = PlaceholderUtil.replace(MessagesUtil.warpsGuiTitle, "%count%", String.valueOf(count));
        if (categoryFilter == null || categoryFilter.isBlank() || categoryFilter.equalsIgnoreCase("all")) {
            return ColorUtil.translate(title);
        }
        return ColorUtil.translate(title + " &8| &b" + categoryFilter);
    }

    private static String normalizeFilter(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isBlank()) {
            return null;
        }
        return categoryFilter.trim();
    }

    private static boolean shouldShowCategoryMenu(WarpManager warpManager, String categoryFilter) {
        return (categoryFilter == null || categoryFilter.equalsIgnoreCase("all"))
                && warpManager.getWarpCount() > 0;
    }

    private static int calculateSize(WarpManager warpManager, String categoryFilter) {
        boolean categoryMenu = shouldShowCategoryMenu(warpManager, normalizeFilter(categoryFilter));
        int itemCount = categoryMenu
                ? Math.max(1, warpManager.getCategories().size())
                : Math.max(1, warpManager.getWarpsInCategory(categoryFilter).size());

        int minRows = Math.max(3, Math.max(1, ConfigUtil.warpsGuiMinSize / 9));
        int rows = (int) Math.ceil(itemCount / 9.0) + 2;
        rows = Math.max(minRows, Math.min(ConfigUtil.warpsGuiMaxRows, rows));
        return rows * 9;
    }

    public void build() {
        if (categoryMenu) {
            buildCategoryMenu();
            return;
        }

        List<Warp> warps = warpManager.getWarpsInCategory(categoryFilter);
        if (warps.isEmpty()) {
            setItem(4, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate(MessagesUtil.warpsGuiEmptyName))
                    .lore(ColorUtil.translate(MessagesUtil.warpsGuiEmptyLore))
                    .build());
            return;
        }

        List<Integer> slots = getDistributedSlots(Math.min(warps.size(), getInnerCapacity()));
        for (int i = 0; i < slots.size(); i++) {
            Warp warp = warps.get(i);

            List<String> lore = new ArrayList<>();
            if (warp.getDescription() != null && !warp.getDescription().isEmpty()) {
                for (String line : warp.getDescription()) {
                    lore.add(ColorUtil.translate(line));
                }
            }
            lore.add(ColorUtil.translate(MessagesUtil.blankLine));
            lore.add(ColorUtil.translate(MessagesUtil.warpsGuiInstructionTeleport));
            lore.add(ColorUtil.translate(MessagesUtil.blankLine));
            lore.add(ColorUtil.translate("&8Category: &7" + warp.getCategory()));
            if (viewerCanManage) {
                lore.add(ColorUtil.translate(MessagesUtil.blankLine));
                lore.add(ColorUtil.translate(MessagesUtil.warpsGuiInstructionManage));
            }

            int slot = slots.get(i);
            setItem(slot, new ItemBuilder(warp.getMaterial())
                    .name(ColorUtil.translate(
                            PlaceholderUtil.replace(MessagesUtil.warpsGuiHomeName, "%name%", warp.getName())
                    ))
                    .lore(lore)
                    .build());
            String warpName = warp.getName();
            setClickHandler(slot, event -> handleWarpClick(event, warpName));
        }
    }

    private void buildCategoryMenu() {
        List<String> categories = warpManager.getCategories();
        if (categories.isEmpty()) {
            return;
        }

        List<Integer> slots = getDistributedSlots(Math.min(categories.size(), getInnerCapacity()));
        for (int i = 0; i < slots.size(); i++) {
            String category = categories.get(i);
            int count = warpManager.getWarpsInCategory(category).size();
            int slot = slots.get(i);

            setItem(slot, new ItemBuilder(Material.BOOKSHELF)
                    .name(ColorUtil.translate("&b" + category))
                    .lore(
                            ColorUtil.translate("&7Warps: &b" + count),
                            ColorUtil.translate(MessagesUtil.blankLine),
                            ColorUtil.translate("&7Left-click to open this category")
                    )
                    .build());

            setClickHandler(slot, event -> {
                Player player = (Player) event.getWhoClicked();
                new WarpsGUI(warpManager, category, player).open(player);
            });
        }
    }

    private int getInnerCapacity() {
        int rows = getSize() / 9;
        return Math.max(1, (rows - 2) * 9);
    }

    private List<Integer> getDistributedSlots(int itemCount) {
        List<Integer> slots = new ArrayList<>();
        if (itemCount <= 0) {
            return slots;
        }

        int rows = getSize() / 9;
        int innerRows = Math.max(1, rows - 2);
        int base = itemCount / innerRows;
        int remainder = itemCount % innerRows;

        for (int innerRowIndex = 0; innerRowIndex < innerRows; innerRowIndex++) {
            int itemsInRow = base + (innerRowIndex < remainder ? 1 : 0);
            if (itemsInRow == 0) {
                continue;
            }
            int row = innerRowIndex + 1; // Skip top border row.
            for (int column : distributedColumns(itemsInRow)) {
                slots.add(row * 9 + column);
            }
        }
        return slots;
    }

    private List<Integer> distributedColumns(int count) {
        List<Integer> columns = new ArrayList<>();
        if (count <= 0) {
            return columns;
        }

        if (count == 1) {
            columns.add(4);
            return columns;
        }

        int previous = -1;
        for (int i = 0; i < count; i++) {
            int column = (int) Math.round(i * (8.0 / (count - 1)));
            if (column <= previous) {
                column = Math.min(8, previous + 1);
            }
            columns.add(column);
            previous = column;
        }
        return columns;
    }

    private void handleWarpClick(InventoryClickEvent event, String warpName) {
        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        ClickType clickType = event.getClick();

        // Right-click to manage
        if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            if (!clicker.hasPermission("rift.warp.manage")) {
                clicker.sendMessage(ChatFormat.error(MessagesUtil.commandNoPermission));
                clicker.closeInventory();
                return;
            }

            Warp warp = warpManager.getWarp(warpName);
            if (warp == null) {
                clicker.sendMessage(ChatFormat.error(
                        PlaceholderUtil.replace(MessagesUtil.warpNotFound, "%name%", warpName)
                ));
                clicker.closeInventory();
                return;
            }
            ManageWarpsGUI gui = new ManageWarpsGUI(warpManager, warp);
            gui.open(clicker);
        }

        // Left-click to tp
        else if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT) {
            Warp warp = warpManager.getWarp(warpName);
            if (warp == null) {
                clicker.sendMessage(ChatFormat.error(
                        PlaceholderUtil.replace(MessagesUtil.warpNotFound, "%name%", warpName)
                ));
                clicker.closeInventory();
                return;
            }

            clicker.closeInventory();
            warp.queueTeleport(clicker);
            warpManager.update(warp);
        }
    }
}
