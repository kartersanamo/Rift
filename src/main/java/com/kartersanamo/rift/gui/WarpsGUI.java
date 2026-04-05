package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Category;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        super(
                "warps_gui",
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
            return ColorUtil.translate("Warp Categories");
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
                && warpManager.hasNonDefaultCategoryWithWarps();
    }

    private static int calculateSize(WarpManager warpManager, String categoryFilter) {
        boolean categoryMenu = shouldShowCategoryMenu(warpManager, normalizeFilter(categoryFilter));
        int itemCount = categoryMenu
                ? Math.max(1, warpManager.getCategoriesForWarpsMenu().size())
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
            int centerSlot = getSize() / 2;
            setItem(centerSlot, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate(MessagesUtil.warpsGuiEmptyName))
                    .lore(ColorUtil.translate(MessagesUtil.warpsGuiEmptyLore))
                    .build());
            return;
        }

        Category category = warpManager.getCategory(categoryFilter);
        List<Integer> fallbackSlots = getDistributedSlots(Math.min(warps.size(), getInnerCapacity()));
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (Warp warp : warps) {
            Integer preferred = category != null ? warpManager.getWarpSlotInCategory(category.getName(), warp.getId()) : null;
            int slot = chooseSlot(preferred, fallbackSlots, used, fallbackIndex);
            while (fallbackIndex < fallbackSlots.size() && used.contains(fallbackSlots.get(fallbackIndex))) {
                fallbackIndex++;
            }

            List<String> lore = new ArrayList<>();
            if (warp.getDescription() != null && !warp.getDescription().isEmpty()) {
                for (String line : warp.getDescription()) {
                    lore.add(ColorUtil.translate(line));
                }
            }
            lore.add(ColorUtil.translate(MessagesUtil.blankLine));
            lore.add(ColorUtil.translate(MessagesUtil.warpsGuiInstructionTeleport));
            if (viewerCanManage) {
                lore.add(ColorUtil.translate(MessagesUtil.warpsGuiInstructionManage));
            }

            setItem(slot, new ItemBuilder(warp.getMaterial())
                    .name(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpsGuiHomeName, "%name%", warp.getName())))
                    .lore(lore)
                    .build());

            String warpName = warp.getName();
            setClickHandler(slot, event -> handleWarpClick(event, warpName));
        }
    }

    private void buildCategoryMenu() {
        List<Category> categories = warpManager.getCategoriesForWarpsMenu();
        if (categories.isEmpty()) {
            return;
        }

        List<Integer> fallbackSlots = getDistributedSlots(Math.min(categories.size(), getInnerCapacity()));
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (Category category : categories) {
            int count = warpManager.getWarpsInCategory(category.getName()).size();
            Integer preferred = warpManager.getCategorySlot(category.getName());
            int slot = chooseSlot(preferred, fallbackSlots, used, fallbackIndex);
            while (fallbackIndex < fallbackSlots.size() && used.contains(fallbackSlots.get(fallbackIndex))) {
                fallbackIndex++;
            }

            List<String> lore = new ArrayList<>();
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                for (String line : category.getDescription()) {
                    lore.add(ColorUtil.translate(line));
                }
            }
            lore.add(ColorUtil.translate(MessagesUtil.blankLine));
            lore.add(ColorUtil.translate("&7Warps: &b" + count));
            lore.add(ColorUtil.translate(MessagesUtil.blankLine));
            lore.add(ColorUtil.translate("&7Left-click to open this category"));
            if (viewerCanManage) {
                lore.add(ColorUtil.translate("&7Right-click to manage this category"));
            }

            setItem(slot, new ItemBuilder(category.getMaterial())
                    .name(ColorUtil.translate("&b" + category.getName()))
                    .lore(lore)
                    .build());

            String categoryName = category.getName();
            setClickHandler(slot, event -> handleCategoryClick(event, categoryName));
        }
    }

    private int chooseSlot(Integer preferred, List<Integer> fallbackSlots, Set<Integer> used, int fallbackIndex) {
        if (preferred != null && isInnerSlot(preferred) && !used.contains(preferred)) {
            used.add(preferred);
            return preferred;
        }

        for (int i = fallbackIndex; i < fallbackSlots.size(); i++) {
            int candidate = fallbackSlots.get(i);
            if (!used.contains(candidate)) {
                used.add(candidate);
                return candidate;
            }
        }

        // Should not happen due bounded item counts, but keep safe.
        int emergency = fallbackSlots.isEmpty() ? 13 : fallbackSlots.get(0);
        used.add(emergency);
        return emergency;
    }

    private boolean isInnerSlot(int slot) {
        if (slot < 0 || slot >= getSize()) {
            return false;
        }
        int row = slot / 9;
        int rows = getSize() / 9;
        return row > 0 && row < rows - 1;
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
            int row = innerRowIndex + 1;
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

    private void handleCategoryClick(InventoryClickEvent event, String categoryName) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ClickType clickType = event.getClick();
        if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            if (!player.hasPermission("rift.warp.manage")) {
                player.sendMessage(ChatFormat.error(MessagesUtil.commandNoPermission));
                return;
            }
            new CategoryManagerGUI(warpManager, categoryName).open(player);
            return;
        }

        new WarpsGUI(warpManager, categoryName, player).open(player);
    }

    private void handleWarpClick(InventoryClickEvent event, String warpName) {
        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        ClickType clickType = event.getClick();

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
            new ManageWarpsGUI(warpManager, warp).open(clicker);
            return;
        }

        if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT) {
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
