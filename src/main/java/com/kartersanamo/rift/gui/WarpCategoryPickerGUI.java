package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.warp.Category;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarpCategoryPickerGUI extends com.kartersanamo.rift.api.gui.GUI {

    private final WarpManager warpManager;
    private final Warp warp;

    public WarpCategoryPickerGUI(WarpManager warpManager, Warp warp) {
        super("warp_category_picker", "Pick Category", WarpsMenuLayout.menuSizeForItems(Math.max(1, warpManager.getCategoriesForWarpsMenu().size())));
        this.warpManager = warpManager;
        this.warp = warp;
        build();
    }

    private void build() {
        fillFrame();

        List<Category> categories = new ArrayList<>();
        for (Category category : warpManager.getCategoriesSorted()) {
            if (category.getName().equalsIgnoreCase("default")) {
                continue;
            }
            categories.add(category);
        }
        int capacity = WarpsMenuLayout.innerCapacity(getSize());
        List<Integer> fallbackSlots = WarpsMenuLayout.distributedInnerSlots(getSize(), Math.min(capacity, categories.size()));
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (Category category : categories) {
            if (used.size() >= capacity) {
                break;
            }

            Integer preferred = warpManager.getCategorySlot(category.getName());
            int slot = chooseSlot(preferred, fallbackSlots, used, fallbackIndex);
            while (fallbackIndex < fallbackSlots.size() && used.contains(fallbackSlots.get(fallbackIndex))) {
                fallbackIndex++;
            }

            boolean selected = warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(category.getName());
            Material icon = selected ? Material.LIME_DYE : category.getMaterial();

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtil.translate("&7Left-click to assign this warp"));
            lore.add(ColorUtil.translate("&7to category &b" + category.getName()));
            if (selected) {
                lore.add(ColorUtil.translate("&aCurrently selected"));
            }

            setItem(slot, new ItemBuilder(icon)
                    .name(ColorUtil.translate("&b" + category.getName()))
                    .lore(lore)
                    .build());

            String categoryName = category.getName();
            setClickHandler(slot, event -> {
                Player player = (Player) event.getWhoClicked();
                warpManager.assignWarpCategory(warp, categoryName);
                AuditLogger.action(player, "warp.manage.category", "id=" + warp.getId() + " category=" + categoryName);
                player.sendMessage(ChatFormat.success("Warp moved to category " + categoryName));
                new ManageWarpsGUI(warpManager, warp).open(player);
            });
        }

        int createSlot = getSize() - 5;
        setItem(createSlot, new ItemBuilder(Material.ANVIL)
                .name(ColorUtil.translate("&aCreate New Category"))
                .lore(ColorUtil.translate("&7Create and assign in one step."))
                .build());
        setClickHandler(createSlot, event -> createCategory((Player) event.getWhoClicked()));

        int backSlot = getSize() - 9;
        setItem(backSlot, new ItemBuilder(Material.ARROW)
                .name(ColorUtil.translate("&eBack"))
                .build());
        setClickHandler(backSlot, event -> new ManageWarpsGUI(warpManager, warp).open((Player) event.getWhoClicked()));
    }

    private void createCategory(Player player) {
        player.closeInventory();
        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type the new category name.",
                input -> {
                    String newCategory = input.trim();
                    if (newCategory.isBlank()) {
                        player.sendMessage(ChatFormat.error("Category name cannot be blank."));
                        return;
                    }
                    warpManager.assignWarpCategory(warp, newCategory);
                    AuditLogger.action(player, "category.create-and-assign", "category=" + newCategory + " warp=" + warp.getName());
                    player.sendMessage(ChatFormat.success("Category created and warp assigned."));
                    new ManageWarpsGUI(warpManager, warp).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Category creation cancelled."))
        );
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

    private int chooseSlot(Integer preferred, List<Integer> fallbackSlots, Set<Integer> used, int fallbackIndex) {
        if (preferred != null && WarpsMenuLayout.isInnerSlot(getSize(), preferred) && !used.contains(preferred)) {
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

        int emergency = fallbackSlots.isEmpty() ? WarpsMenuLayout.centeredInnerSlot(getSize()) : fallbackSlots.get(0);
        used.add(emergency);
        return emergency;
    }
}

