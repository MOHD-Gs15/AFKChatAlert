/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AFKChatAlert
implements ClientModInitializer {
    public static final String MOD_ID = "afk-chat-alert";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"afk-chat-alert");
    private static KeyMapping toggleKeyMapping;
    private static String cachedUsername;

    public void onInitializeClient() {
        ModConfig.load();
        LOGGER.info("AFK Chat Alert initializing...");
        toggleKeyMapping = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.afk-chat-alert.toggle", InputConstants.Type.KEYSYM, 75, KeyMapping.Category.MISC));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            this.processMessage(message);
            return true;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            this.processMessage(message);
            return true;
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        LOGGER.info("AFK Chat Alert initialized successfully!");
    }

    private void onClientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        this.trackPlayerActivity(minecraft);
        if (toggleKeyMapping != null && toggleKeyMapping.consumeClick()) {
            this.toggleMod(minecraft);
        }
    }

    private void trackPlayerActivity(Minecraft minecraft) {
        double velocity;
        if (minecraft.player != null && ((velocity = minecraft.player.getDeltaMovement().lengthSqr()) > 0.001 || minecraft.options.keyUp.isDown() || minecraft.options.keyDown.isDown() || minecraft.options.keyLeft.isDown() || minecraft.options.keyRight.isDown() || minecraft.options.keyJump.isDown() || minecraft.options.keyShift.isDown() || minecraft.options.keyAttack.isDown() || minecraft.options.keyUse.isDown())) {
            ModConfig.updateActivity();
        }
    }

    private void toggleMod(Minecraft minecraft) {
        boolean newState = !ModConfig.isEnabled();
        ModConfig.setEnabled(newState);
        if (minecraft.player != null) {
            MutableComponent message = newState ? Component.translatable((String)"message.afk-chat-alert.enabled") : Component.translatable((String)"message.afk-chat-alert.disabled");
            minecraft.player.sendOverlayMessage((Component)message);
        }
        LOGGER.info("AFK Chat Alert {}", (Object)(newState ? "ENABLED" : "DISABLED"));
    }

    private String getUsername(Minecraft minecraft) {
        if (cachedUsername == null && minecraft.getUser() != null) {
            cachedUsername = minecraft.getUser().getName();
        }
        return cachedUsername;
    }

    private List<String> getAllKeywords(Minecraft minecraft) {
        String username;
        ArrayList<String> allKeywords = new ArrayList<String>(ModConfig.getKeywords());
        if (ModConfig.isAutoDetectUsername() && (username = this.getUsername(minecraft)) != null && !username.isEmpty() && !allKeywords.contains(username)) {
            allKeywords.add(username);
        }
        return allKeywords;
    }

    private void processMessage(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (!ModConfig.isEnabled() || !ModConfig.isInAfkMode()) {
            return;
        }
        String messageContent = message.getString();
        for (String keyword : this.getAllKeywords(minecraft)) {
            if (keyword == null || keyword.isEmpty() || !messageContent.toLowerCase().contains(keyword.toLowerCase())) continue;
            if (!ModConfig.canPlayAlert()) break;
            this.playAlertSound(minecraft);
            LOGGER.info("Keyword '{}' detected: {}", (Object)keyword, (Object)messageContent);
            break;
        }
    }

    private void playAlertSound(Minecraft minecraft) {
        minecraft.execute(() -> {
            if (minecraft.player != null && minecraft.level != null) {
                float volume = ModConfig.getAlertVolumeFloat();
                ModConfig.AlertSound selectedSound = ModConfig.getAlertSound();
                switch (selectedSound) {
                    case EXPLOSION: {
                        minecraft.level.playSound((Entity)minecraft.player, minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(), (Holder)SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, volume, 1.0f);
                        break;
                    }
                    case EXPERIENCE_ORB: {
                        minecraft.level.playSound((Entity)minecraft.player, minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, volume, 1.0f);
                        break;
                    }
                    case VILLAGE_BELL: {
                        minecraft.level.playSound((Entity)minecraft.player, minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(), SoundEvents.BELL_BLOCK, SoundSource.MASTER, volume, 1.0f);
                        break;
                    }
                    case NOTE_BLOCK_BELL: {
                        minecraft.level.playSound((Entity)minecraft.player, minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(), (Holder)SoundEvents.NOTE_BLOCK_BELL, SoundSource.MASTER, volume, 1.0f);
                    }
                }
            }
        });
    }

    static {
        cachedUsername = null;
    }
}

