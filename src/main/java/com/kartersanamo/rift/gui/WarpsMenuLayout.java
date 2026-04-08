package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.api.config.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

public final class WarpsMenuLayout {

    private WarpsMenuLayout() {
    }

    public static int menuSizeForItems(int itemCount) {
        int minRows = Math.max(3, Math.max(1, ConfigUtil.warpsGuiMinSize / 9));
        int rows = (int) Math.ceil(Math.max(1, itemCount) / 9.0) + 2;
        rows = Math.max(minRows, Math.min(ConfigUtil.warpsGuiMaxRows, rows));
        return rows * 9;
    }

    public static boolean isInnerSlot(int size, int slot) {
        if (slot < 0 || slot >= size) {
            return false;
        }
        int row = slot / 9;
        int rows = size / 9;
        return row > 0 && row < rows - 1;
    }

    public static int innerCapacity(int size) {
        int rows = size / 9;
        return Math.max(1, (rows - 2) * 9);
    }

    public static List<Integer> distributedInnerSlots(int size, int itemCount) {
        List<Integer> slots = new ArrayList<>();
        if (itemCount <= 0) {
            return slots;
        }

        int rows = size / 9;
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

    public static int centeredInnerSlot(int size) {
        int rows = size / 9;
        int middleRow = rows / 2;
        if (middleRow <= 0) {
            middleRow = 1;
        }
        if (middleRow >= rows - 1) {
            middleRow = rows - 2;
        }
        return middleRow * 9 + 4;
    }

    private static List<Integer> distributedColumns(int count) {
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
}

