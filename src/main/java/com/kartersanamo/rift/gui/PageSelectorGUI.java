package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PageSelectorGUI extends GUI {

    private static final int PAGE_ITEMS_PER_VIEW = 45;

    private final WarpManager warpManager;
    private final String categoryFilter;
    private final Player viewer;
    private final int selectedPage;
    private final int selectorPage;

    public PageSelectorGUI(WarpManager warpManager, String categoryFilter, Player viewer, int selectedPage, int selectorPage) {
        super("warps_page_selector", "Select Page", 54);
        this.warpManager = warpManager;
        this.categoryFilter = normalizeFilter(categoryFilter);
        this.viewer = viewer;
        this.selectedPage = selectedPage;
        this.selectorPage = Math.max(1, selectorPage);
        build();
    }

    private void build() {
        int totalPages = getTotalWarpsPages();
        int totalSelectorPages = Math.max(1, (int) Math.ceil(totalPages / (double) PAGE_ITEMS_PER_VIEW));
        int currentSelectorPage = Math.min(selectorPage, totalSelectorPages);

        int firstPage = ((currentSelectorPage - 1) * PAGE_ITEMS_PER_VIEW) + 1;
        int lastPage = Math.min(totalPages, firstPage + PAGE_ITEMS_PER_VIEW - 1);

        for (int slot = 0; slot < 54; slot++) {
            setItem(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build());
        }

        int displaySlot = 0;
        for (int pageNumber = firstPage; pageNumber <= lastPage; pageNumber++) {
            Material icon = pageNumber == selectedPage ? Material.LIME_DYE : Material.PAPER;
            setItem(displaySlot, new ItemBuilder(icon)
                    .name(ColorUtil.translate("&bPage " + pageNumber))
                    .lore(ColorUtil.translate("&7Click to open this page."))
                    .build());

            final int targetPage = pageNumber;
            setClickHandler(displaySlot, event -> new WarpsGUI(warpManager, categoryFilter, viewer, targetPage).open(viewer));
            displaySlot++;
        }

        setItem(49, new ItemBuilder(Material.BARRIER)
                .name(ColorUtil.translate("&cBack"))
                .lore(ColorUtil.translate("&7Return to the warps menu."))
                .build());
        setClickHandler(49, event -> new WarpsGUI(warpManager, categoryFilter, viewer, selectedPage).open(viewer));

        setItem(48, new ItemBuilder(Material.BOOK)
                .name(ColorUtil.translate("&bSelector Page " + currentSelectorPage + "/" + totalSelectorPages))
                .lore(ColorUtil.translate("&7Showing pages &b" + firstPage + " &7to &b" + lastPage))
                .build());

        if (currentSelectorPage > 1) {
            setItem(45, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&ePrevious"))
                    .build());
            setClickHandler(45, event -> new PageSelectorGUI(warpManager, categoryFilter, viewer, selectedPage, currentSelectorPage - 1).open(viewer));
        }

        if (currentSelectorPage < totalSelectorPages) {
            setItem(53, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&eNext"))
                    .build());
            setClickHandler(53, event -> new PageSelectorGUI(warpManager, categoryFilter, viewer, selectedPage, currentSelectorPage + 1).open(viewer));
        }
    }

    private int getTotalWarpsPages() {
        boolean categoryMenu = shouldShowCategoryMenu();
        int itemCount = categoryMenu
                ? warpManager.getCategoriesForWarpsMenu().size()
                : warpManager.getWarpsInCategory(categoryFilter).size();

        int menuSize = WarpsMenuLayout.menuSizeForItems(Math.max(1, itemCount));
        int pageSize = WarpsMenuLayout.innerCapacity(menuSize);
        return Math.max(1, (int) Math.ceil(itemCount / (double) pageSize));
    }

    private boolean shouldShowCategoryMenu() {
        if (!ConfigUtil.warpsGuiCategoryFilterEnabled) {
            return false;
        }
        return (categoryFilter == null || categoryFilter.equalsIgnoreCase("all"))
                && warpManager.hasNonDefaultCategoryWithWarps();
    }

    private String normalizeFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim();
    }
}


