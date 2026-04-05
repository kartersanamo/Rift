package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SlotPickerGUI extends GUI {

    public SlotPickerGUI(String title,
                         int size,
                         boolean innerOnly,
                         Integer selectedSlot,
                         BiConsumer<Player, Integer> onSelect,
                         Consumer<Player> onBack) {
        super("slot_picker_gui", title, size);
        build(innerOnly, selectedSlot, onSelect, onBack);
    }

    private void build(boolean innerOnly,
                       Integer selectedSlot,
                       BiConsumer<Player, Integer> onSelect,
                       Consumer<Player> onBack) {
        for (int slot = 0; slot < getSize(); slot++) {
            boolean allowed = !innerOnly || isInnerSlot(slot);
            Material material = allowed ? Material.LIGHT_GRAY_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            String name = allowed
                    ? "&7Slot &b" + slot
                    : "&8Unavailable";

            if (allowed && selectedSlot != null && selectedSlot == slot) {
                material = Material.LIME_STAINED_GLASS_PANE;
                name = "&aCurrent Slot &7(" + slot + ")";
            }

            setItem(slot, new ItemBuilder(material)
                    .name(ColorUtil.translate(name))
                    .build());

            if (allowed) {
                int chosen = slot;
                setClickHandler(slot, event -> {
                    Player player = (Player) event.getWhoClicked();
                    onSelect.accept(player, chosen);
                });
            }
        }

        int clearSlot = getSize() - 5;
        int backSlot = getSize() - 9;

        setItem(clearSlot, new ItemBuilder(Material.BARRIER)
                .name(ColorUtil.translate("&cClear Slot Override"))
                .build());
        setClickHandler(clearSlot, event -> {
            Player player = (Player) event.getWhoClicked();
            onSelect.accept(player, null);
        });

        setItem(backSlot, new ItemBuilder(Material.ARROW)
                .name(ColorUtil.translate("&eBack"))
                .build());
        setClickHandler(backSlot, event -> {
            Player player = (Player) event.getWhoClicked();
            onBack.accept(player);
        });
    }

    private boolean isInnerSlot(int slot) {
        int row = slot / 9;
        int rows = getSize() / 9;
        return row > 0 && row < rows - 1;
    }
}

