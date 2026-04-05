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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WarpsGUI extends GUI {

    private final WarpManager warpManager;
    private final String categoryFilter;

    public WarpsGUI(WarpManager warpManager) {
        this(warpManager, null);
    }

    public WarpsGUI(WarpManager warpManager, String categoryFilter) {
        super("warps_gui",
                ColorUtil.translate(
                        PlaceholderUtil.replace(
                                MessagesUtil.warpsGuiTitle,
                                "%count%", String.valueOf(warpManager.getWarpCount())
                        ) + buildFilterSuffix(categoryFilter)),
                calculateSize(warpManager)
        );
        this.warpManager = warpManager;
        this.categoryFilter = normalizeFilter(categoryFilter);
        build();
    }

    private static String buildFilterSuffix(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isBlank()) {
            return "";
        }
        return " &8| &b" + categoryFilter;
    }

    private static String normalizeFilter(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isBlank()) {
            return null;
        }
        return categoryFilter.trim();
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

        List<Warp> filteredWarps = warps.values().stream()
                .filter(this::matchesFilter)
                .collect(Collectors.toList());

        List<String> categories = warps.values().stream()
                .map(Warp::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .sorted(Comparator.comparing(String::toLowerCase))
                .collect(Collectors.toCollection(ArrayList::new));
        categories.add(0, "all");

        boolean showFilterControls = ConfigUtil.warpsGuiCategoryFilterEnabled && categories.size() > 1;
        int maxWarpSlots = showFilterControls ? getSize() - 3 : getSize();

        if (filteredWarps.isEmpty()) {
            String filterLabel = categoryFilter == null ? "all" : categoryFilter;
            setItem(4, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate("&cNo warps in this category"))
                    .lore(
                            ColorUtil.translate("&7Current filter: &b" + filterLabel),
                            ColorUtil.translate("&7Use the filter controls to change category.")
                    )
                    .build());
        }

        int slot = 0;
        for (Warp warp : filteredWarps) {
            if (slot >= maxWarpSlots) {
                break;
            }

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

        if (showFilterControls) {
            int currentIndex = getCurrentCategoryIndex(categories);
            int previousIndex = (currentIndex - 1 + categories.size()) % categories.size();
            int nextIndex = (currentIndex + 1) % categories.size();

            int previousSlot = getSize() - 3;
            int currentSlot = getSize() - 2;
            int nextSlot = getSize() - 1;

            setItem(previousSlot, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&ePrevious Category"))
                    .lore(ColorUtil.translate("&7Switch to &b" + categories.get(previousIndex)))
                    .build());
            setClickHandler(previousSlot, event -> {
                Player player = (Player) event.getWhoClicked();
                openFilter(player, categories.get(previousIndex));
            });

            String currentCategory = currentIndex == 0 ? "all" : categories.get(currentIndex);
            setItem(currentSlot, new ItemBuilder(Material.HOPPER)
                    .name(ColorUtil.translate("&bCategory Filter"))
                    .lore(
                            ColorUtil.translate("&7Current: &b" + currentCategory),
                            ColorUtil.translate("&7Right/left arrows to cycle")
                    )
                    .build());

            setItem(nextSlot, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&eNext Category"))
                    .lore(ColorUtil.translate("&7Switch to &b" + categories.get(nextIndex)))
                    .build());
            setClickHandler(nextSlot, event -> {
                Player player = (Player) event.getWhoClicked();
                openFilter(player, categories.get(nextIndex));
            });
        }
    }

    private int getCurrentCategoryIndex(List<String> categories) {
        if (categoryFilter == null) {
            return 0;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).equalsIgnoreCase(categoryFilter)) {
                return i;
            }
        }
        return 0;
    }

    private boolean matchesFilter(Warp warp) {
        if (categoryFilter == null) {
            return true;
        }
        return warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(categoryFilter);
    }

    private void openFilter(Player player, String category) {
        if (category == null || category.equalsIgnoreCase("all")) {
            new WarpsGUI(warpManager).open(player);
            return;
        }
        new WarpsGUI(warpManager, category).open(player);
    }

    private void handleClick(InventoryClickEvent event, String warpName) {
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
