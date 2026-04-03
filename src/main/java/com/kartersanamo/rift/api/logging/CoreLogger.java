package com.kartersanamo.rift.api.logging;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class CoreLogger {

    private final Plugin plugin;
    private final java.util.logging.Logger bukkitLogger;
    private final SimpleDateFormat dateFormat;
    private LogLevel currentLevel;
    private boolean fileLogging;
    private File logFile;
    private PrintWriter fileWriter;

    public CoreLogger(Plugin plugin) {
        this.plugin = plugin;
        this.bukkitLogger = plugin.getLogger();
        this.currentLevel = LogLevel.INFO;
        this.fileLogging = false;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void setFileLogging(boolean enable) {
        this.fileLogging = enable;

        if (enable && logFile == null) {
            try {
                File logsFolder = new File(plugin.getDataFolder(), "logs");
                if (!logsFolder.exists()) {
                    logsFolder.mkdirs();
                }

                logFile = new File(logsFolder, "sanamocore-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
                fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
            } catch (IOException e) {
                bukkitLogger.log(Level.WARNING, "Failed to initialize file logging", e);
                fileLogging = false;
            }
        } else if (!enable && fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
    }

    public LogLevel getLevel() {
        return currentLevel;
    }

    public void setLevel(LogLevel level) {
        this.currentLevel = level;
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void severe(String message) {
        log(LogLevel.SEVERE, message);
    }

    public void log(LogLevel level, String message) {
        if (currentLevel.shouldLog(level)) {
            return;
        }

        String formattedMessage = formatMessage(level, message);

        // Log to Bukkit logger
        Level bukkitLevel = convertToBukkitLevel(level);
        bukkitLogger.log(bukkitLevel, formattedMessage);

        // Log to file if enabled
        if (fileLogging && fileWriter != null) {
            fileWriter.println(formattedMessage);
        }
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if (currentLevel.shouldLog(level)) {
            return;
        }

        String formattedMessage = formatMessage(level, message);

        // Log to Bukkit logger
        Level bukkitLevel = convertToBukkitLevel(level);
        bukkitLogger.log(bukkitLevel, formattedMessage, throwable);

        // Log to file if enabled
        if (fileLogging && fileWriter != null) {
            fileWriter.println(formattedMessage);
            throwable.printStackTrace(fileWriter);
        }
    }

    private String formatMessage(LogLevel level, String message) {
        String timestamp = dateFormat.format(new Date());
        return String.format("[%s] [%s] %s", timestamp, level.getDisplayName(), message);
    }

    private Level convertToBukkitLevel(LogLevel level) {
        return switch (level) {
            case DEBUG -> Level.FINE;
            case WARNING -> Level.WARNING;
            case ERROR, SEVERE -> Level.SEVERE;
            default -> Level.INFO;
        };
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
    }
}