package com.afkchatalert;

import com.afkchatalert.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AFK Chat Alert — 100% client-side Fabric mod.
 *
 * Plays a distinct audio alert whenever your username or a custom keyword is
 * mentioned in chat while you are AFK. Smart AFK detection keeps the mod
 * silent while you are actively playing.
 */
public class AFKChatAlert implements ClientModInitializer {

    public static final String MOD_ID = "afk-chat-alert";
    public static final Logger LOGGER = LoggerFactory.getLogger("afk-chat-alert");

    /** GLFW key code for K. */
    private static final int TOGGLE_KEY = 75;

    private static KeyMapping toggleKeyMapping;
    private static String cachedUsername;

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        LOGGER.info("AFK Chat Alert initializing...");

        toggleKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.afk-chat-alert.toggle", InputConstants.Type.KEYSYM, TOGGLE_KEY, KeyMapping.Category.MISC));

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            processMessage(message);
            return true;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            processMessage(message);
            return true;
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        LOGGER.info("AFK Chat Alert initialized successfully!");
    }

    private void onClientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        trackPlayerActivity(minecraft);
        if (toggleKeyMapping != null && toggleKeyMapping.consumeClick()) {
            toggleMod(minecraft);
        }
    }

    /**
     * Any movement or held gameplay key counts as activity and resets the
     * AFK timer.
     */
    private void trackPlayerActivity(Minecraft minecraft) {
        double velocity = minecraft.player.getDeltaMovement().lengthSqr();
        if (velocity > 0.001
                || minecraft.options.keyUp.isDown()
                || minecraft.options.keyDown.isDown()
                || minecraft.options.keyLeft.isDown()
                || minecraft.options.keyRight.isDown()
                || minecraft.options.keyJump.isDown()
                || minecraft.options.keyShift.isDown()
                || minecraft.options.keyAttack.isDown()
                || minecraft.options.keyUse.isDown()) {
            ModConfig.updateActivity();
        }
    }

    private void toggleMod(Minecraft minecraft) {
        boolean newState = !ModConfig.isEnabled();
        ModConfig.setEnabled(newState);
        if (minecraft.player != null) {
            MutableComponent message = newState
                    ? Component.translatable("message.afk-chat-alert.enabled")
                    : Component.translatable("message.afk-chat-alert.disabled");
            minecraft.player.sendOverlayMessage(message);
        }
        LOGGER.info("AFK Chat Alert {}", newState ? "ENABLED" : "DISABLED");
    }

    private String getUsername(Minecraft minecraft) {
        if (cachedUsername == null && minecraft.getUser() != null) {
            cachedUsername = minecraft.getUser().getName();
        }
        return cachedUsername;
    }

    /**
     * Custom keywords plus, optionally, the local player's username.
     */
    private List<String> getAllKeywords(Minecraft minecraft) {
        List<String> allKeywords = new ArrayList<>(ModConfig.getKeywords());
        if (ModConfig.isAutoDetectUsername()) {
            String username = getUsername(minecraft);
            if (username != null && !username.isEmpty() && !allKeywords.contains(username)) {
                allKeywords.add(username);
            }
        }
        return allKeywords;
    }

    /**
     * Core detection: alert only while AFK mode is active and the mod is
     * enabled. First keyword match wins and is throttled by the cooldown.
     */
    private void processMessage(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (!ModConfig.isEnabled() || !ModConfig.isInAfkMode()) {
            return;
        }
        String messageContent = message.getString();
        for (String keyword : getAllKeywords(minecraft)) {
            if (keyword == null || keyword.isEmpty() || !messageContent.toLowerCase().contains(keyword.toLowerCase())) {
                continue;
            }
            if (!ModConfig.canPlayAlert()) {
                break;
            }
            playAlertSound(minecraft);
            LOGGER.info("Keyword '{}' detected: {}", keyword, messageContent);
            break;
        }
    }

    private void playAlertSound(Minecraft minecraft) {
        minecraft.execute(() -> {
            if (minecraft.player == null || minecraft.level == null) {
                return;
            }
            float volume = ModConfig.getAlertVolumeFloat();
            ModConfig.AlertSound selectedSound = ModConfig.getAlertSound();
            switch (selectedSound) {
                case EXPLOSION:
                    minecraft.level.playSound(minecraft.player, minecraft.player.getX(), minecraft.player.getY(),
                            minecraft.player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, volume, 1.0f);
                    break;
                case EXPERIENCE_ORB:
                    minecraft.level.playSound(minecraft.player, minecraft.player.getX(), minecraft.player.getY(),
                            minecraft.player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, volume, 1.0f);
                    break;
                case VILLAGE_BELL:
                    minecraft.level.playSound(minecraft.player, minecraft.player.getX(), minecraft.player.getY(),
                            minecraft.player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.MASTER, volume, 1.0f);
                    break;
                case NOTE_BLOCK_BELL:
                    minecraft.level.playSound(minecraft.player, minecraft.player.getX(), minecraft.player.getY(),
                            minecraft.player.getZ(), SoundEvents.NOTE_BLOCK_BELL, SoundSource.MASTER, volume, 1.0f);
                    break;
            }
        });
    }
}
