package com.afkchatalert.gui;

import com.afkchatalert.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Cloth Config screen shown from Mod Menu. Two categories: General
 * (detection + keywords) and Alert Sound (sound + volume).
 */
public final class ModConfigScreen {

    private ModConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.afk-chat-alert.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("config.afk-chat-alert.category.general"));

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("config.afk-chat-alert.option.enabled"), ModConfig.isEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig::setEnabled)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("config.afk-chat-alert.option.autoUsername"), ModConfig.isAutoDetectUsername())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig::setAutoDetectUsername)
                .build());

        general.addEntry(entryBuilder
                .startStrList(Component.translatable("config.afk-chat-alert.option.keywords"), new ArrayList<>(ModConfig.getKeywords()))
                .setDefaultValue(List.of("afk", "hello", "help", "@everyone", "urgent"))
                .setSaveConsumer(ModConfig::setKeywords)
                .build());

        general.addEntry(entryBuilder
                .startIntSlider(Component.translatable("config.afk-chat-alert.option.cooldown"), ModConfig.getCooldownSeconds(), 0, 60)
                .setDefaultValue(5)
                .setTextGetter(value -> value == 0
                        ? Component.translatable("config.afk-chat-alert.cooldown.disabled")
                        : Component.translatable("config.afk-chat-alert.cooldown.seconds", value))
                .setSaveConsumer(ModConfig::setCooldownSeconds)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("config.afk-chat-alert.option.smartAfk"), ModConfig.isSmartAfkEnabled())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig::setSmartAfkEnabled)
                .build());

        general.addEntry(entryBuilder
                .startIntSlider(Component.translatable("config.afk-chat-alert.option.smartAfkTimeout"), ModConfig.getSmartAfkTimeoutMinutes(), 1, 30)
                .setDefaultValue(2)
                .setTextGetter(value -> Component.translatable("config.afk-chat-alert.timeout.minutes", value))
                .setSaveConsumer(ModConfig::setSmartAfkTimeoutMinutes)
                .build());

        ConfigCategory sound = builder.getOrCreateCategory(
                Component.translatable("config.afk-chat-alert.category.sound"));

        sound.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("config.afk-chat-alert.option.alertSound"), ModConfig.AlertSound.class, ModConfig.getAlertSound())
                .setDefaultValue(ModConfig.AlertSound.EXPLOSION)
                .setEnumNameProvider(anEnum -> {
                    if (anEnum instanceof ModConfig.AlertSound alertSound) {
                        return Component.literal(alertSound.getDisplayName());
                    }
                    return Component.literal(anEnum.name());
                })
                .setSaveConsumer(ModConfig::setAlertSound)
                .build());

        sound.addEntry(entryBuilder
                .startIntSlider(Component.translatable("config.afk-chat-alert.option.alertVolume"), ModConfig.getAlertVolume(), 0, 100)
                .setDefaultValue(100)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setSaveConsumer(ModConfig::setAlertVolume)
                .build());

        builder.setSavingRunnable(ModConfig::save);
        return builder.build();
    }
}
