/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.shedaniel.clothconfig2.api.AbstractConfigListEntry
 *  me.shedaniel.clothconfig2.api.ConfigBuilder
 *  me.shedaniel.clothconfig2.api.ConfigCategory
 *  me.shedaniel.clothconfig2.api.ConfigEntryBuilder
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.afkchatalert.gui;

import com.afkchatalert.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle((Component)Component.translatable((String)"config.afk-chat-alert.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory((Component)Component.translatable((String)"config.afk-chat-alert.category.general"));
        general.addEntry((AbstractConfigListEntry)entryBuilder.startBooleanToggle((Component)Component.translatable((String)"config.afk-chat-alert.option.enabled"), ModConfig.isEnabled()).setDefaultValue(true).setSaveConsumer(ModConfig::setEnabled).build());
        general.addEntry((AbstractConfigListEntry)entryBuilder.startBooleanToggle((Component)Component.translatable((String)"config.afk-chat-alert.option.autoUsername"), ModConfig.isAutoDetectUsername()).setDefaultValue(true).setSaveConsumer(ModConfig::setAutoDetectUsername).build());
        general.addEntry((AbstractConfigListEntry)entryBuilder.startStrList((Component)Component.translatable((String)"config.afk-chat-alert.option.keywords"), new ArrayList<String>(ModConfig.getKeywords())).setDefaultValue(List.of("afk", "hello", "help", "@everyone", "urgent")).setSaveConsumer(ModConfig::setKeywords).build());
        general.addEntry((AbstractConfigListEntry)entryBuilder.startIntSlider((Component)Component.translatable((String)"config.afk-chat-alert.option.cooldown"), ModConfig.getCooldownSeconds(), 0, 60).setDefaultValue(5).setTextGetter(value -> value == 0 ? Component.translatable((String)"config.afk-chat-alert.cooldown.disabled") : Component.translatable((String)"config.afk-chat-alert.cooldown.seconds", (Object[])new Object[]{value})).setSaveConsumer(ModConfig::setCooldownSeconds).build());
        general.addEntry((AbstractConfigListEntry)entryBuilder.startBooleanToggle((Component)Component.translatable((String)"config.afk-chat-alert.option.smartAfk"), ModConfig.isSmartAfkEnabled()).setDefaultValue(true).setSaveConsumer(ModConfig::setSmartAfkEnabled).build());
        general.addEntry((AbstractConfigListEntry)entryBuilder.startIntSlider((Component)Component.translatable((String)"config.afk-chat-alert.option.smartAfkTimeout"), ModConfig.getSmartAfkTimeoutMinutes(), 1, 30).setDefaultValue(2).setTextGetter(value -> Component.translatable((String)"config.afk-chat-alert.timeout.minutes", (Object[])new Object[]{value})).setSaveConsumer(ModConfig::setSmartAfkTimeoutMinutes).build());
        ConfigCategory sound = builder.getOrCreateCategory((Component)Component.translatable((String)"config.afk-chat-alert.category.sound"));
        sound.addEntry((AbstractConfigListEntry)entryBuilder.startEnumSelector((Component)Component.translatable((String)"config.afk-chat-alert.option.alertSound"), ModConfig.AlertSound.class, (Enum)ModConfig.getAlertSound()).setDefaultValue((Enum)ModConfig.AlertSound.EXPLOSION).setEnumNameProvider(anEnum -> {
            if (anEnum instanceof ModConfig.AlertSound) {
                ModConfig.AlertSound alertSound = (ModConfig.AlertSound)((Object)anEnum);
                return Component.literal((String)alertSound.getDisplayName());
            }
            return Component.literal((String)anEnum.name());
        }).setSaveConsumer(ModConfig::setAlertSound).build());
        sound.addEntry((AbstractConfigListEntry)entryBuilder.startIntSlider((Component)Component.translatable((String)"config.afk-chat-alert.option.alertVolume"), ModConfig.getAlertVolume(), 0, 100).setDefaultValue(100).setTextGetter(value -> Component.literal((String)(value + "%"))).setSaveConsumer(ModConfig::setAlertVolume).build());
        builder.setSavingRunnable(ModConfig::save);
        return builder.build();
    }
}

