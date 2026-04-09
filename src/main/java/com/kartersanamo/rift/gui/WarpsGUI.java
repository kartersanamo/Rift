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

    private static final int SLOT_PREVIOUS = 45;
    private static final int SLOT_BACK = 46;
    private static final int SLOT_CREATE_CATEGORY = 47;
    private static final int SLOT_CREATE_WARP = 48;
    private static final int SLOT_PAGE_INFO = 49;
    private static final int SLOT_MANAGE_CATEGORY = 50;
    private static final int SLOT_NEXT = 52;

    private final WarpManager warpManager;
    private final String categoryFilter;
    private final boolean categoryMenu;
    private final boolean viewerCanManage;
    private final int page;

    public WarpsGUI(WarpManager warpManager) {
        this(warpManager, null, null, 1);
    }

    public WarpsGUI(WarpManager warpManager, String categoryFilter) {
        this(warpManager, categoryFilter, null, 1);
    }

    public WarpsGUI(WarpManager warpManager, String categoryFilter, Player viewer) {
        this(warpManager, categoryFilter, viewer, 1);
    }

    public WarpsGUI(WarpManager warpManager, String categoryFilter, Player viewer, int page) {
        super(
                "warps_gui",
                buildTitle(
                        warpManager,
                        categoryFilter,
                        calculateSize(warpManager, categoryFilter),
                        clampPage(
                                warpManager,
                                normalizeFilter(categoryFilter),
                                calculateSize(warpManager, categoryFilter),
                                page
                        )
                ),
                calculateSize(warpManager, categoryFilter)
        );
        this.warpManager = warpManager;
        this.categoryFilter = normalizeFilter(categoryFilter);
        this.categoryMenu = shouldShowCategoryMenu(warpManager, this.categoryFilter);
        this.viewerCanManage = viewer != null && viewer.hasPermission("rift.warp.manage");
        this.page = clampPage(warpManager, this.categoryFilter, getSize(), page);
        build();
    }

    private static String buildTitle(WarpManager warpManager, String categoryFilter, int size, int requestedPage) {
        String normalizedFilter = normalizeFilter(categoryFilter);
        if (shouldShowCategoryMenu(warpManager, normalizedFilter)) {
            int pageCount = getPageCount(warpManager, normalizedFilter, size);
            return ColorUtil.translate(appendPageSuffix("Warp Categories", requestedPage, pageCount));
        }

        int count = warpManager.getWarpsInCategory(normalizedFilter).size();
        String title = PlaceholderUtil.replace(MessagesUtil.warpsGuiTitle, "%count%", String.valueOf(count));
        int pageCount = getPageCount(warpManager, normalizedFilter, size);
        if (normalizedFilter == null || normalizedFilter.equalsIgnoreCase("all")) {
            return ColorUtil.translate(appendPageSuffix(title, requestedPage, pageCount));
        }
        return ColorUtil.translate(appendPageSuffix(title + " &8| &b" + normalizedFilter, requestedPage, pageCount));
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

    private static int getPageCount(WarpManager warpManager, String categoryFilter, int size) {
        int itemCount = shouldShowCategoryMenu(warpManager, categoryFilter)
                ? warpManager.getCategoriesForWarpsMenu().size()
                : warpManager.getWarpsInCategory(categoryFilter).size();
        int pageSize = WarpsMenuLayout.innerCapacity(size);
        return Math.max(1, (int) Math.ceil(Math.max(0, itemCount) / (double) pageSize));
    }

    private static int clampPage(WarpManager warpManager, String categoryFilter, int size, int requestedPage) {
        return Math.max(1, Math.min(requestedPage, getPageCount(warpManager, categoryFilter, size)));
    }

    private static String appendPageSuffix(String title, int page, int pageCount) {
        if (pageCount <= 1) {
            return title;
        }
        return title + " &8| &7Page &b" + page + "&7/&b" + pageCount;
    }

    public void build() {
        fillFrame();

        if (categoryMenu) {
            buildCategoryMenu();
            addNavigationButtons();
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
            addNavigationButtons();
            addAdminButtons();
            return;
        }

        Category category = warpManager.getCategory(categoryFilter);
        int pageSize = WarpsMenuLayout.innerCapacity(getSize());
        long skipWarps = Math.max(0L, (long) (page - 1) * pageSize);
        List<Warp> pageWarps = warps.stream().skip(skipWarps).limit(pageSize).toList();
        List<Integer> fallbackSlots = WarpsMenuLayout.distributedInnerSlots(getSize(), pageWarps.size());
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (Warp warp : pageWarps) {
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

        addNavigationButtons();
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
            addNavigationButtons();
            return;
        }

        int pageSize = WarpsMenuLayout.innerCapacity(getSize());
        int startIndex = Math.max(0, (page - 1) * pageSize);
        List<Category> pageCategories = new ArrayList<>();
        for (int i = startIndex; i < categories.size() && pageCategories.size() < pageSize; i++) {
            pageCategories.add(categories.get(i));
        }
        List<Integer> fallbackSlots = WarpsMenuLayout.distributedInnerSlots(getSize(), pageCategories.size());
        Set<Integer> used = new HashSet<>();
        int fallbackIndex = 0;

        for (Category category : pageCategories) {
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

        addNavigationButtons();
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
            return;
        }

        if (categoryMenu) {
            setItem(SLOT_CREATE_CATEGORY, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(ColorUtil.translate("&aCreate Category"))
                .lore(
                        ColorUtil.translate("&7Click to create a new category."),
                        ColorUtil.translate("&7Then right-click it to configure layout.")
                )
                .build());
            setClickHandler(SLOT_CREATE_CATEGORY, event -> createCategory((Player) event.getWhoClicked()));
        }

        setItem(SLOT_CREATE_WARP, new ItemBuilder(Material.ENDER_PEARL)
                .name(ColorUtil.translate("&aCreate Warp Here"))
                .lore(
                        ColorUtil.translate("&7Creates a warp at your current location."),
                        ColorUtil.translate("&7If this menu is a category page, it auto-assigns there.")
                )
                .build());
        setClickHandler(SLOT_CREATE_WARP, event -> createWarpHere((Player) event.getWhoClicked()));

        if (!categoryMenu && categoryFilter != null && !categoryFilter.equalsIgnoreCase("all")) {
            setItem(SLOT_MANAGE_CATEGORY, new ItemBuilder(Material.COMPARATOR)
                    .name(ColorUtil.translate("&bManage This Category"))
                    .lore(ColorUtil.translate("&7Open category settings and layout tools."))
                    .build());
            setClickHandler(SLOT_MANAGE_CATEGORY, event -> new CategoryManagerGUI(warpManager, categoryFilter).open((Player) event.getWhoClicked()));
        }
    }

    private void addNavigationButtons() {
        int totalPages = getPageCount(warpManager, categoryFilter, getSize());
        if (totalPages <= 1) {
            return;
        }

        if (page > 1) {
            setItem(SLOT_PREVIOUS, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&ePrevious Page"))
                    .lore(ColorUtil.translate("&7Go to page &b" + (page - 1)))
                    .build());
            setClickHandler(SLOT_PREVIOUS, event -> new WarpsGUI(warpManager, categoryFilter, (Player) event.getWhoClicked(), page - 1).open((Player) event.getWhoClicked()));
        }

        List<String> pageLore = new ArrayList<>();
        pageLore.add(ColorUtil.translate("&7Browsing " + (categoryMenu ? "categories" : "warps") + "."));
        pageLore.add(ColorUtil.translate("&7Click to open page selector."));

        setItem(SLOT_PAGE_INFO, new ItemBuilder(Material.PAPER)
                .name(ColorUtil.translate("&bPage " + page + "/" + totalPages))
                .lore(pageLore)
                .build());
        setClickHandler(SLOT_PAGE_INFO, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }
            new PageSelectorGUI(warpManager, categoryFilter, player, page, 1).open(player);
        });

        if (page < totalPages) {
            setItem(SLOT_NEXT, new ItemBuilder(Material.ARROW)
                    .name(ColorUtil.translate("&eNext Page"))
                    .lore(ColorUtil.translate("&7Go to page &b" + (page + 1)))
                    .build());
            setClickHandler(SLOT_NEXT, event -> new WarpsGUI(warpManager, categoryFilter, (Player) event.getWhoClicked(), page + 1).open((Player) event.getWhoClicked()));
        }

        if (!categoryMenu && categoryFilter != null && !categoryFilter.equalsIgnoreCase("all")) {
            setItem(SLOT_BACK, new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate("&cBack to Categories"))
                    .lore(ColorUtil.translate("&7Return to the main category menu."))
                    .build());
            setClickHandler(SLOT_BACK, event -> new WarpsGUI(warpManager, null, (Player) event.getWhoClicked(), page).open((Player) event.getWhoClicked()));
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
                    new WarpsGUI(warpManager, null, player, page).open(player);
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
                    new WarpsGUI(warpManager, categoryMenu ? null : targetCategory, player, page).open(player);
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

        new WarpsGUI(warpManager, categoryName, player, page).open(player);
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
