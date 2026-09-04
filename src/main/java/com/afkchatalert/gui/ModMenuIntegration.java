package com.afkchatalert.gui;

import com.afkchatalert.AFKChatAlert;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu entrypoint: exposes the Cloth Config screen.
 *
 * <p>Both Mod Menu and Cloth Config are OPTIONAL (see {@code suggests} in
 * {@code fabric.mod.json}). Mod Menu only invokes this entrypoint when it is
 * installed. When it is, but Cloth Config is missing, we degrade gracefully:
 * the button opens a small vanilla {@link AlertScreen} explaining how to
 * configure the mod instead of crashing with {@code NoClassDefFoundError}.</p>
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            AFKChatAlert.LOGGER.info(
                    "Cloth Config is not installed - the in-game settings screen is unavailable. "
                            + "Configure the mod by editing config/afk-chat-alert.json (a fresh file "
                            + "with defaults is written on first launch).");
            return ModMenuIntegration::createMissingClothScreen;
        }
        return ModConfigScreen::create;
    }

    /** Vanilla alert screen shown when Cloth Config is absent. */
    private static Screen createMissingClothScreen(Screen parent) {
        return new AlertScreen(
                () -> Minecraft.getInstance().setScreen(parent),
                Component.translatable("config.afk-chat-alert.title"),
                Component.translatable("config.afk-chat-alert.clothRequired"));
    }
}
