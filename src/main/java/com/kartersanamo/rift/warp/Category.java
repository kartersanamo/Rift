package com.kartersanamo.rift.warp;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Category {

    private String name;
    private List<String> description;
    private Material material;
    private Integer menuSlot;
    private final Map<String, Integer> warpSlots;

    public Category(String name, List<String> description, Material material, Integer menuSlot, Map<String, Integer> warpSlots) {
        this.name = name;
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
        this.material = material != null ? material : Material.BOOKSHELF;
        this.menuSlot = menuSlot;
        this.warpSlots = warpSlots != null ? new HashMap<>(warpSlots) : new HashMap<>();
    }

    public static Category defaultCategory() {
        return new Category("default", new ArrayList<>(), Material.BOOKSHELF, null, new HashMap<>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description != null ? new ArrayList<>(description) : new ArrayList<>();
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Integer getMenuSlot() {
        return menuSlot;
    }

    public void setMenuSlot(Integer menuSlot) {
        this.menuSlot = menuSlot;
    }

    public Map<String, Integer> getWarpSlots() {
        return warpSlots;
    }
}

