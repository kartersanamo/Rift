package com.kartersanamo.rift.warp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class Warp {

    private final String name;
    private final List<String> description;
    private final String category;
    private Location location;
    private final String id;
    private final Player creator;
    private Material material;
    private final long createdAt;
    private int uses;

    public Warp(String name, List<String> description, String category, Location location, String id, Player creator, Material material, long createdAt, int uses) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.location = location;
        this.id = id;
        this.creator = creator;
        this.material = material;
        this.createdAt = createdAt;
        this.uses = uses;
    }

    public void teleport(Player player) {
        // If the player is currently counting down for a teleport, cancel the old one and start the new one
        if (TeleportManager.isPlayerTeleporting(player)) {
            TeleportManager.removeTeleport(player.getUniqueId());
        }
        TeleportManager.teleportToWarp(player, this);
        uses++;
    }

    @Override
    public String toString() {
        return "Warp{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", location=" + location +
                ", id='" + id + '\'' +
                ", creator=" + (creator != null ? creator.getUniqueId() : "null") +
                ", material=" + material +
                ", createdAt=" + createdAt +
                ", uses=" + uses +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getUses() {
        return uses;
    }

    public Player getCreator() {
        return creator;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
