package com.kartersanamo.rift.api.config;

import com.kartersanamo.rift.Rift;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigFile {

    private final Plugin plugin;
    private final String fileName;
    private final File file;
    private FileConfiguration config;

    public ConfigFile(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
        load();
    }

    public void load() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveResource();
        }
        try {
            config = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            Rift.getLog().warning("Error loading config file " + fileName + ": " + e.getMessage());
            config = new YamlConfiguration();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Rift.getLog().severe("Could not save config file " + fileName);
            e.printStackTrace();
        }
    }

    public void reload() {
        load();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private void saveResource() {
        InputStream resource = plugin.getResource(fileName);
        if (resource != null) {
            try {
                Files.copy(resource, file.toPath());
            } catch (IOException e) {
                Rift.getLog().severe("Could not save default config file " + fileName);
                e.printStackTrace();
            }
        }
    }
}