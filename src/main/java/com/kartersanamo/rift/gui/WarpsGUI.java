package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Category;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        String normalizedFilter = normalizeFilter(categoryFilter);
        if (shouldShowCategoryMenu(warpManager, normalizedFilter)) {
            return ColorUtil.translate("Warp Categories");
        }

        int count = warpManager.getWarpsInCategory(normalizedFilter).size();
        String title = PlaceholderUtil.replace(MessagesUtil.warpsGuiTitle, "%count%", String.valueOf(count));
        if (normalizedFilter == null || normalizedFilter.equalsIgnoreCase("all")) {
            return ColorUtil.translate(title);
        }
        return ColorUtil.translate(title + " &8| &b" + normalizedFilter);
    }

    private static String normalizeFilter(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isBlank()) {
            return null;
        }
        return categoryFilter.trim();
    }

    private static boolean shouldShowCategoryMenu(WarpManager warpManager, String categoryFilter) {
        if (!ConfigUtil.warpsGuiCategoryFilterEnabled) {
            return false;
        }
        return (categoryFilter == null || categoryFilter.equalsIgnoreCase("all"))
                && warpManager.hasNonDefaultCategoryWithWarps();
    }

    private static int calculateSize(WarpManager warpManager, String categoryFilter) {
        boolean categoryMenu = shouldShowCategoryMenu(warpManager, normalizeFilter(categoryFilter));
        int itemCount = categoryMenu
                ? Math.max(1, warpManager.getCategoriesForWarpsMenu().size())
                : Math.max(1, warpManager.getWarpsInCategory(categoryFilter).size());
        return WarpsMenuLayout.menuSizeForItems(itemCount);
    }

    public void build() {
        fillFrame();

        if (categoryMenu) {
            buildCategoryMenu();
            addAdminButtons();
            return;
        }

        List<Warp> warps = warpManager.getWarpsInCategorySorted(categoryFilter);
        if (warps.isEmpty()) {
            int centerSlot = WarpsMenuLayout.centeredInnerSlot(getSize());
            setItem(centerSlot, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate(MessagesUtil.warpsGuiEmptyName))
                    .lore(ColorUtil.translate(MessagesUtil.warpsGuiEmptyLore))
                    .build());
            addAdminButtons();
            return;
        }

        Category category = warpManager.getCategory(categoryFilter);
        int visibleCount = Math.min(warps.size(), WarpsMenuLayout.innerCapacity(getSize()));
        List<Integer> fallbackSlots = WarpsMenuLayout.distributedInnerSlots(getSize(), visibleCount);
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (int index = 0; index < visibleCount; index++) {
            Warp warp = warps.get(index);
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

        addAdminButtons();
    }

    private void buildCategoryMenu() {
        List<Category> categories = warpManager.getCategoriesForWarpsMenu();
        if (categories.isEmpty()) {
            int centerSlot = WarpsMenuLayout.centeredInnerSlot(getSize());
            setItem(centerSlot, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate("&cNo categories to display"))
                    .lore(ColorUtil.translate("&7Create a warp or category to get started."))
                    .build());
            return;
        }

        int visibleCount = Math.min(categories.size(), WarpsMenuLayout.innerCapacity(getSize()));
        List<Integer> fallbackSlots = WarpsMenuLayout.distributedInnerSlots(getSize(), visibleCount);
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (int index = 0; index < visibleCount; index++) {
            Category category = categories.get(index);
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

        // Should not happen due bounded item counts, but keep safe.
        int emergency = fallbackSlots.isEmpty() ? WarpsMenuLayout.centeredInnerSlot(getSize()) : fallbackSlots.get(0);
        used.add(emergency);
        return emergency;
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

    private void addAdminButtons() {
        if (!viewerCanManage) {
            if (!categoryMenu && categoryFilter != null && !categoryFilter.equalsIgnoreCase("all")) {
                int backSlot = getSize() - 9;
                setItem(backSlot, new ItemBuilder(Material.ARROW)
                        .name(ColorUtil.translate("&eBack to Categories"))
                        .build());
                setClickHandler(backSlot, event -> new WarpsGUI(warpManager, null, (Player) event.getWhoClicked()).open((Player) event.getWhoClicked()));
            }
            return;
        }

        int createCategorySlot = getSize() - 5;
        setItem(createCategorySlot, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(ColorUtil.translate("&aCreate Category"))
                .lore(
                        ColorUtil.translate("&7Click to create a new category."),
                        ColorUtil.translate("&7Then right-click it to configure layout.")
                )
                .build());
        setClickHandler(createCategorySlot, event -> createCategory((Player) event.getWhoClicked()));

        int createWarpSlot = getSize() - 4;
        setItem(createWarpSlot, new ItemBuilder(Material.ENDER_PEARL)
                .name(ColorUtil.translate("&aCreate Warp Here"))
                .lore(
                        ColorUtil.translate("&7Creates a warp at your current location."),
                        ColorUtil.translate("&7If this menu is a category page, it auto-assigns there.")
                )
                .build());
        setClickHandler(createWarpSlot, event -> createWarpHere((Player) event.getWhoClicked()));

        int manageCategorySlot = getSize() - 3;
        if (!categoryMenu && categoryFilter != null && !categoryFilter.equalsIgnoreCase("all")) {
            setItem(manageCategorySlot, new ItemBuilder(Material.COMPARATOR)
                    .name(ColorUtil.translate("&bManage This Category"))
                    .lore(ColorUtil.translate("&7Open category settings and layout tools."))
                    .build());
            setClickHandler(manageCategorySlot, event -> new CategoryManagerGUI(warpManager, categoryFilter).open((Player) event.getWhoClicked()));
        }

        int backSlot = getSize() - 9;
        if (!categoryMenu && categoryFilter != null && !categoryFilter.equalsIgnoreCase("all")) {
            setItem(backSlot, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&eBack to Categories"))
                    .build());
            setClickHandler(backSlot, event -> new WarpsGUI(warpManager, null, (Player) event.getWhoClicked()).open((Player) event.getWhoClicked()));
        }
    }

    private void createCategory(Player player) {
        player.closeInventory();
        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type the category name.",
                input -> {
                    String name = input.trim();
                    if (name.isBlank()) {
                        player.sendMessage(ChatFormat.error("Category name cannot be blank."));
                        return;
                    }
                    boolean created = warpManager.createCategory(name);
                    if (!created) {
                        player.sendMessage(ChatFormat.warning("Category already exists or name is invalid."));
                    } else {
                        AuditLogger.action(player, "category.create", "category=" + name);
                        player.sendMessage(ChatFormat.success("Created category " + name));
                    }
                    new WarpsGUI(warpManager, null, player).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Category creation cancelled."))
        );
    }

    private void createWarpHere(Player player) {
        player.closeInventory();
        String targetCategory = (!categoryMenu && categoryFilter != null && !categoryFilter.equalsIgnoreCase("all"))
                ? categoryFilter
                : "default";

        Rift.getInstance().getChatInputManager().awaitInput(player,
                "&7Type the warp name to create in &b" + targetCategory,
                input -> {
                    String warpName = input.trim();
                    WarpManager.WarpNameValidationResult validation = warpManager.validateWarpName(warpName);
                    if (validation != WarpManager.WarpNameValidationResult.VALID) {
                        player.sendMessage(ChatFormat.error(warpManager.getWarpNameValidationMessage(validation)));
                        return;
                    }

                    Warp existing = warpManager.getWarp(warpName);
                    if (existing != null) {
                        existing.setLocation(player.getLocation().clone());
                        existing.setCategory(warpManager.ensureCategoryExists(targetCategory));
                        warpManager.update(existing);
                        AuditLogger.action(player, "warp.update.location", "name=" + existing.getName() + " source=warps-gui");
                        player.sendMessage(ChatFormat.success("Updated warp " + existing.getName() + " at your location."));
                    } else {
                        Warp warp = new Warp(
                                warpName,
                                new ArrayList<>(),
                                warpManager.ensureCategoryExists(targetCategory),
                                player.getLocation().clone(),
                                "warp_" + player.getUniqueId() + "_" + UUID.randomUUID(),
                                player,
                                Material.ENDER_PEARL,
                                System.currentTimeMillis(),
                                0
                        );
                        warpManager.addWarp(warp);
                        AuditLogger.action(player, "warp.create", "name=" + warpName + " category=" + warp.getCategory() + " source=warps-gui");
                        player.sendMessage(ChatFormat.success("Created warp " + warpName + "."));
                    }

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new WarpsGUI(warpManager, categoryMenu ? null : targetCategory, player).open(player);
                },
                () -> player.sendMessage(ChatFormat.warning("Warp creation cancelled."))
        );
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
                return;
            }

            Warp warp = warpManager.getWarp(warpName);
            if (warp == null) {
                clicker.sendMessage(ChatFormat.error(
                        PlaceholderUtil.replace(MessagesUtil.warpNotFound, "%name%", warpName)
                ));
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
                return;
            }

            clicker.closeInventory();
            warp.queueTeleport(clicker);
            warpManager.update(warp);
        }
    }
}
