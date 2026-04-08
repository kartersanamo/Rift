package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.warp.Category;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagerGUI extends GUI {

    private final WarpManager warpManager;
    private final String categoryName;

    public CategoryManagerGUI(WarpManager warpManager, String categoryName) {
        super("category_manager_gui", "Category: " + categoryName, 54);
        this.warpManager = warpManager;
        this.categoryName = categoryName;
        build();
    }

    private void build() {
        fillFrame();

        Category category = warpManager.getCategory(categoryName);
        if (category == null) {
            setItem(22, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate("&cCategory not found"))
                    .build());
            return;
        }

        List<String> infoLore = new ArrayList<>();
        infoLore.add(ColorUtil.translate("&7Name: &b" + category.getName()));
        infoLore.add(ColorUtil.translate("&7Menu Slot: &b" + (category.getMenuSlot() == null ? "auto" : category.getMenuSlot())));
        infoLore.add(ColorUtil.translate("&7Warps: &b" + warpManager.getWarpsInCategory(category.getName()).size()));
        if (category.getDescription() != null && !category.getDescription().isEmpty()) {
            infoLore.add(ColorUtil.translate("&7Description:"));
            for (String line : category.getDescription()) {
                infoLore.add(ColorUtil.translate("&8- &7" + line));
            }
        }

        setItem(4, new ItemBuilder(category.getMaterial())
                .name(ColorUtil.translate("&b&lCategory Overview"))
                .lore(infoLore)
                .build());

        setItem(10, new ItemBuilder(Material.NAME_TAG)
                .name(ColorUtil.translate("&bRename Category"))
                .lore(ColorUtil.translate("&7Change the category display name."))
                .build());
        setClickHandler(10, this::renameCategory);

        setItem(11, new ItemBuilder(Material.PAPER)
                .name(ColorUtil.translate("&bEdit Description"))
                .lore(ColorUtil.translate("&7Type lines separated by &b|&7."))
                .build());
        setClickHandler(11, this::editDescription);

        setItem(12, new ItemBuilder(Material.ITEM_FRAME)
                .name(ColorUtil.translate("&bChange Icon"))
                .lore(ColorUtil.translate("&7Set category material/icon."))
                .build());
        setClickHandler(12, this::changeMaterial);

        setItem(13, new ItemBuilder(Material.COMPASS)
                .name(ColorUtil.translate("&bSet Category Slot"))
                .lore(ColorUtil.translate("&7Pick where this category appears in &b/warps&7."))
                .build());
        setClickHandler(13, this::setCategorySlot);

        setItem(14, new ItemBuilder(Material.CHEST_MINECART)
                .name(ColorUtil.translate("&bManage Category Warps"))
                .lore(
                        ColorUtil.translate("&7Open a warp browser for this category."),
                        ColorUtil.translate("&7Left-click: assign | Right-click: move to default")
                )
                .build());
        setClickHandler(14, event -> {
            Player player = (Player) event.getWhoClicked();
            new CategoryWarpAssignmentGUI(warpManager, categoryName).open(player);
        });

        setItem(15, new ItemBuilder(Material.STRUCTURE_VOID)
                .name(ColorUtil.translate("&bClear All Warp Slot Overrides"))
                .lore(ColorUtil.translate("&7Keep warps in this category, but reset layout."))
                .build());
        setClickHandler(15, event -> clearWarpSlotOverrides((Player) event.getWhoClicked(), category));

        setItem(16, new ItemBuilder(Material.BARRIER)
                .name(ColorUtil.translate("&cDelete This Category"))
                .lore(
                        ColorUtil.translate("&7Deletes this category."),
                        ColorUtil.translate("&7Warps in it will move to &bdefault&7.")
                )
                .build());
        setClickHandler(16, this::deleteCategory);

        setItem(18, new ItemBuilder(Material.ARROW)
                .name(ColorUtil.translate("&eBack to Categories"))
                .build());
        setClickHandler(18, event -> {
            Player player = (Player) event.getWhoClicked();
            new WarpsGUI(warpManager, null, player).open(player);
        });

        List<Warp> warps = warpManager.getWarpsInCategorySorted(category.getName());
        List<Integer> previewSlots = getPreviewSlots(warps.size());
        for (int index = 0; index < previewSlots.size(); index++) {
            Warp warp = warps.get(index);
            int slot = previewSlots.get(index);

            Integer overrideSlot = warpManager.getWarpSlotInCategory(category.getName(), warp.getId());
            setItem(slot, new ItemBuilder(warp.getMaterial())
                    .name(ColorUtil.translate("&b" + warp.getName()))
                    .lore(
                            ColorUtil.translate("&7Current override slot: &b" + (overrideSlot == null ? "auto" : overrideSlot)),
                            ColorUtil.translate("&7Left-click to set slot"),
                            ColorUtil.translate("&7Right-click to clear slot override")
                    )
                    .build());

            String warpId = warp.getId();
            String warpName = warp.getName();
            setClickHandler(slot, event -> manageWarpSlot(event, category.getName(), warpId, warpName));
        }
    }

    private void fillFrame() {
        for (int slot = 45; slot < 54; slot++) {
            setItem(slot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name(" ")
                    .build());
        }
    }

    private List<Integer> getPreviewSlots(int itemCount) {
        List<Integer> slots = new ArrayList<>();
        if (itemCount <= 0) {
            return slots;
        }

        // Reserve two lower rows for a concise, non-overlapping preview section.
        int firstRowCount = Math.min(9, itemCount);
        int secondRowCount = Math.min(9, Math.max(0, itemCount - firstRowCount));

        for (int col : distributedColumns(firstRowCount)) {
            slots.add(27 + col);
        }
        for (int col : distributedColumns(secondRowCount)) {
            slots.add(36 + col);
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

    private void renameCategory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type a new category name.",
                input -> {
                    String newName = input.trim();
                    if (newName.isBlank()) {
                        player.sendMessage(ChatFormat.error("Category name cannot be blank."));
                        return;
                    }

                    boolean renamed = warpManager.renameCategory(categoryName, newName);
                    if (!renamed) {
                        player.sendMessage(ChatFormat.error("Could not rename this category."));
                        return;
                    }

                    AuditLogger.action(player, "category.rename", "from=" + categoryName + " to=" + newName);
                    player.sendMessage(ChatFormat.success("Category renamed to " + newName));
                    new CategoryManagerGUI(warpManager, newName).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Category rename cancelled."))
        );
    }

    private void editDescription(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type description lines using &b| &7as separator. Type &bclear &7to remove.",
                input -> {
                    Category category = warpManager.getCategory(categoryName);
                    if (category == null) {
                        player.sendMessage(ChatFormat.error("Category not found."));
                        return;
                    }

                    String raw = input.trim();
                    if (raw.equalsIgnoreCase("clear")) {
                        category.setDescription(new ArrayList<>());
                        warpManager.updateCategory(category);
                        AuditLogger.action(player, "category.description.clear", "category=" + categoryName);
                        player.sendMessage(ChatFormat.success("Category description cleared."));
                        new CategoryManagerGUI(warpManager, categoryName).open(player);
                        return;
                    }

                    List<String> lines = new ArrayList<>();
                    for (String split : raw.split("\\|")) {
                        String cleaned = split.trim();
                        if (!cleaned.isEmpty()) {
                            lines.add(cleaned);
                        }
                    }

                    if (lines.isEmpty()) {
                        player.sendMessage(ChatFormat.error("Description cannot be empty."));
                        return;
                    }

                    category.setDescription(lines);
                    warpManager.updateCategory(category);
                    AuditLogger.action(player, "category.description.update", "category=" + categoryName + " lines=" + lines.size());
                    player.sendMessage(ChatFormat.success("Category description updated."));
                    new CategoryManagerGUI(warpManager, categoryName).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Category description edit cancelled."))
        );
    }

    private void changeMaterial(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type a material name (example: &bBOOKSHELF&7).",
                input -> {
                    Category category = warpManager.getCategory(categoryName);
                    if (category == null) {
                        player.sendMessage(ChatFormat.error("Category not found."));
                        return;
                    }

                    Material material = Material.matchMaterial(input.trim().replace(' ', '_').toUpperCase());
                    if (material == null || !material.isItem()) {
                        player.sendMessage(ChatFormat.error("Invalid material."));
                        return;
                    }

                    category.setMaterial(material);
                    warpManager.updateCategory(category);
                    AuditLogger.action(player, "category.material", "category=" + categoryName + " material=" + material.name());
                    player.sendMessage(ChatFormat.success("Category material updated."));
                    new CategoryManagerGUI(warpManager, categoryName).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Category material edit cancelled."))
        );
    }

    private void setCategorySlot(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Integer current = warpManager.getCategorySlot(categoryName);
        new SlotPickerGUI("Pick Category Slot", calculateCategoryMenuSize(), true, current,
                (p, slot) -> {
                    warpManager.setCategorySlot(categoryName, slot);
                    AuditLogger.action(p, "category.slot", "category=" + categoryName + " slot=" + (slot == null ? "auto" : slot));
                    p.sendMessage(ChatFormat.success("Category slot updated."));
                    new CategoryManagerGUI(warpManager, categoryName).open(p);
                },
                p -> new CategoryManagerGUI(warpManager, categoryName).open(p)
        ).open(player);
    }

    private void clearWarpSlotOverrides(Player player, Category category) {
        if (category == null) {
            player.sendMessage(ChatFormat.error("Category not found."));
            return;
        }
        category.getWarpSlots().clear();
        warpManager.updateCategory(category);
        AuditLogger.action(player, "category.warp-slots.clear", "category=" + category.getName());
        player.sendMessage(ChatFormat.success("Cleared warp slot overrides for " + category.getName()));
        new CategoryManagerGUI(warpManager, categoryName).open(player);
    }

    private void deleteCategory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type &bconfirm &7to delete category &b" + categoryName + "&7.",
                input -> {
                    if (!input.trim().equalsIgnoreCase("confirm")) {
                        player.sendMessage(ChatFormat.warning("Category delete cancelled."));
                        return;
                    }

                    boolean deleted = warpManager.deleteCategory(categoryName);
                    if (!deleted) {
                        player.sendMessage(ChatFormat.error("Could not delete this category."));
                        return;
                    }

                    AuditLogger.action(player, "category.delete", "category=" + categoryName + " movedWarpsTo=default");
                    player.sendMessage(ChatFormat.success("Category deleted. Warps were moved to default."));
                    new WarpsGUI(warpManager, null, player).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Category delete cancelled."))
        );
    }

    private void manageWarpSlot(InventoryClickEvent event, String category, String warpId, String warpName) {
        Player player = (Player) event.getWhoClicked();

        if (event.getClick().isRightClick()) {
            warpManager.setWarpSlotInCategory(category, warpId, null);
            AuditLogger.action(player, "category.warp-slot.clear", "category=" + category + " warp=" + warpName);
            player.sendMessage(ChatFormat.success("Warp slot override cleared for " + warpName));
            new CategoryManagerGUI(warpManager, categoryName).open(player);
            return;
        }

        Integer current = warpManager.getWarpSlotInCategory(category, warpId);
        new SlotPickerGUI("Pick Warp Slot: " + warpName, calculateWarpMenuSize(categoryName), true, current,
                (p, slot) -> {
                    warpManager.setWarpSlotInCategory(category, warpId, slot);
                    AuditLogger.action(p, "category.warp-slot", "category=" + category + " warp=" + warpName + " slot=" + (slot == null ? "auto" : slot));
                    p.sendMessage(ChatFormat.success("Warp slot updated for " + warpName));
                    new CategoryManagerGUI(warpManager, categoryName).open(p);
                },
                p -> new CategoryManagerGUI(warpManager, categoryName).open(p)
        ).open(player);
    }

    private int calculateCategoryMenuSize() {
        return WarpsMenuLayout.menuSizeForItems(Math.max(1, warpManager.getCategoriesForWarpsMenu().size()));
    }

    private int calculateWarpMenuSize(String category) {
        return WarpsMenuLayout.menuSizeForItems(Math.max(1, warpManager.getWarpsInCategory(category).size()));
    }
}
