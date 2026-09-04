package com.afkchatalert.config;

import com.afkchatalert.AFKChatAlert;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Mod configuration, persisted as JSON at
 * {@code config/afk-chat-alert.json}.
 *
 * <p>All state is intentionally static: the mod has a single client instance
 * and the config screen (Cloth Config) mutates it through the setters, each
 * of which persists the file immediately.</p>
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("afk-chat-alert.json");

    private static boolean enabled = true;
    private static boolean autoDetectUsername = true;
    private static List<String> keywords =
            new ArrayList<>(List.of("afk", "hello", "help", "@everyone", "urgent"));
    private static int cooldownSeconds = 5;
    private static long lastAlertTime = 0L;

    private static boolean smartAfkEnabled = true;
    private static int smartAfkTimeoutMinutes = 2;
    private static long lastActivityTime = System.currentTimeMillis();
    private static boolean isAfkMode = false;

    private static AlertSound alertSound = AlertSound.EXPLOSION;
    private static int alertVolume = 100;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    public static boolean isAutoDetectUsername() {
        return autoDetectUsername;
    }

    public static void setAutoDetectUsername(boolean value) {
        autoDetectUsername = value;
        save();
    }

    public static List<String> getKeywords() {
        return keywords;
    }

    public static void setKeywords(List<String> newKeywords) {
        keywords = new ArrayList<>(newKeywords);
        save();
    }

    public static int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public static void setCooldownSeconds(int seconds) {
        cooldownSeconds = Math.max(0, seconds);
        save();
    }

    /**
     * Cooldown gate. Consumes the cooldown window when returning true so
     * busy global chats cannot spam the alert sound.
     */
    public static boolean canPlayAlert() {
        if (cooldownSeconds <= 0) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAlertTime >= (long) cooldownSeconds * 1000L) {
            lastAlertTime = currentTime;
            return true;
        }
        return false;
    }

    public static boolean isSmartAfkEnabled() {
        return smartAfkEnabled;
    }

    public static void setSmartAfkEnabled(boolean value) {
        smartAfkEnabled = value;
        save();
    }

    public static int getSmartAfkTimeoutMinutes() {
        return smartAfkTimeoutMinutes;
    }

    public static void setSmartAfkTimeoutMinutes(int minutes) {
        smartAfkTimeoutMinutes = Math.max(1, minutes);
        save();
    }

    /** Called on every detected player activity: resets the AFK state. */
    public static void updateActivity() {
        lastActivityTime = System.currentTimeMillis();
        isAfkMode = false;
    }

    /**
     * AFK mode is latched: once the inactivity timeout elapses, the player
     * stays "AFK" until actual activity clears the flag. With smart AFK
     * detection disabled the mod simply always considers itself in AFK mode
     * (alert whenever a keyword appears).
     */
    public static boolean isInAfkMode() {
        if (!smartAfkEnabled) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActivityTime >= (long) (smartAfkTimeoutMinutes * 60) * 1000L) {
            isAfkMode = true;
        }
        return isAfkMode;
    }

    public static AlertSound getAlertSound() {
        return alertSound;
    }

    public static void setAlertSound(AlertSound sound) {
        alertSound = sound;
        save();
    }

    public static int getAlertVolume() {
        return alertVolume;
    }

    public static void setAlertVolume(int volume) {
        alertVolume = Math.max(0, Math.min(100, volume));
        save();
    }

    public static float getAlertVolumeFloat() {
        return (float) alertVolume / 100.0f;
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.enabled = enabled;
        data.autoDetectUsername = autoDetectUsername;
        data.keywords = keywords;
        data.cooldownSeconds = cooldownSeconds;
        data.smartAfkEnabled = smartAfkEnabled;
        data.smartAfkTimeoutMinutes = smartAfkTimeoutMinutes;
        data.alertSound = alertSound.name();
        data.alertVolume = alertVolume;
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            AFKChatAlert.LOGGER.error("Failed to save config", e);
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                if (data.enabled != null) {
                    enabled = data.enabled;
                }
                if (data.autoDetectUsername != null) {
                    autoDetectUsername = data.autoDetectUsername;
                }
                if (data.keywords != null) {
                    keywords = data.keywords;
                }
                if (data.cooldownSeconds != null) {
                    cooldownSeconds = data.cooldownSeconds;
                }
                if (data.smartAfkEnabled != null) {
                    smartAfkEnabled = data.smartAfkEnabled;
                }
                if (data.smartAfkTimeoutMinutes != null) {
                    smartAfkTimeoutMinutes = data.smartAfkTimeoutMinutes;
                }
                if (data.alertSound != null) {
                    try {
                        alertSound = AlertSound.valueOf(data.alertSound);
                    } catch (IllegalArgumentException e) {
                        alertSound = AlertSound.EXPLOSION;
                    }
                }
                if (data.alertVolume != null) {
                    alertVolume = data.alertVolume;
                }
            }
        } catch (Exception e) {
            AFKChatAlert.LOGGER.error("Failed to load config", e);
            save();
        }
    }

    /** Selectable vanilla alert sounds. */
    public enum AlertSound {
        EXPLOSION("Explosion", "Strong and loud alert"),
        EXPERIENCE_ORB("Experience Orb", "Classic and loved sound"),
        VILLAGE_BELL("Village Bell", "Sharp and continuous"),
        NOTE_BLOCK_BELL("Note Block Bell", "Soft and calm");

        private final String displayName;
        private final String description;

        AlertSound(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /** Gson serialization shape (all fields nullable for forward/backward compat). */
    private static class ConfigData {
        Boolean enabled;
        Boolean autoDetectUsername;
        List<String> keywords;
        Integer cooldownSeconds;
        Boolean smartAfkEnabled;
        Integer smartAfkTimeoutMinutes;
        String alertSound;
        Integer alertVolume;
    }
}
