package com.sonicether.soundphysics.integration;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sonicether.soundphysics.Loggers;
import com.sonicether.soundphysics.SoundPhysicsMod;
import com.sonicether.soundphysics.config.blocksound.BlockDefinition;

import de.maxhenkel.configbuilder.entry.BooleanConfigEntry;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.configbuilder.entry.DoubleConfigEntry;
import de.maxhenkel.configbuilder.entry.FloatConfigEntry;
import de.maxhenkel.configbuilder.entry.IntegerConfigEntry;
import de.maxhenkel.configbuilder.entry.StringConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigIntegration {

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder
                .create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("cloth_config.sound_physics_remastered.settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("cloth_config.sound_physics_remastered.category.general"));

        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.enabled"),
                Component.translatable("cloth_config.sound_physics_remastered.enabled.description"),
                SoundPhysicsMod.CONFIG.enabled
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.simple_voice_chat_integration"),
                Component.translatable("cloth_config.sound_physics_remastered.simple_voice_chat_integration.description"),
                SoundPhysicsMod.CONFIG.simpleVoiceChatIntegration
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.simple_voice_chat_hear_self"),
                Component.translatable("cloth_config.sound_physics_remastered.simple_voice_chat_hear_self.description"),
                SoundPhysicsMod.CONFIG.hearSelf
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.attenuation_factor"),
                Component.translatable("cloth_config.sound_physics_remastered.attenuation_factor.description"),
                SoundPhysicsMod.CONFIG.attenuationFactor
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.reverb_attenuation_distance"),
                Component.translatable("cloth_config.sound_physics_remastered.reverb_attenuation_distance.description"),
                SoundPhysicsMod.CONFIG.reverbAttenuationDistance
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.reverb_gain"),
                Component.translatable("cloth_config.sound_physics_remastered.reverb_gain.description"),
                SoundPhysicsMod.CONFIG.reverbGain
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.reverb_brightness"),
                Component.translatable("cloth_config.sound_physics_remastered.reverb_brightness.description"),
                SoundPhysicsMod.CONFIG.reverbBrightness
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.reverb_distance"),
                Component.translatable("cloth_config.sound_physics_remastered.reverb_distance.description"),
                SoundPhysicsMod.CONFIG.reverbDistance
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.block_absorption"),
                Component.translatable("cloth_config.sound_physics_remastered.block_absorption.description"),
                SoundPhysicsMod.CONFIG.blockAbsorption
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.occlusion_variation"),
                Component.translatable("cloth_config.sound_physics_remastered.occlusion_variation.description"),
                SoundPhysicsMod.CONFIG.occlusionVariation
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.default_block_reflectivity"),
                Component.translatable("cloth_config.sound_physics_remastered.default_block_reflectivity.description"),
                SoundPhysicsMod.CONFIG.defaultBlockReflectivity
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.default_block_occlusion_factor"),
                Component.translatable("cloth_config.sound_physics_remastered.default_block_occlusion_factor.description"),
                SoundPhysicsMod.CONFIG.defaultBlockOcclusionFactor
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.sound_distance_allowance"),
                Component.translatable("cloth_config.sound_physics_remastered.sound_distance_allowance.description"),
                SoundPhysicsMod.CONFIG.soundDistanceAllowance
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.air_absorption"),
                Component.translatable("cloth_config.sound_physics_remastered.air_absorption.description"),
                SoundPhysicsMod.CONFIG.airAbsorption
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.underwater_filter"),
                Component.translatable("cloth_config.sound_physics_remastered.underwater_filter.description"),
                SoundPhysicsMod.CONFIG.underwaterFilter
        ));
        general.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.evaluate_ambient_sounds"),
                Component.translatable("cloth_config.sound_physics_remastered.evaluate_ambient_sounds.description"),
                SoundPhysicsMod.CONFIG.evaluateAmbientSounds
        ));

        ConfigCategory performance = builder.getOrCreateCategory(Component.translatable("cloth_config.sound_physics_remastered.category.performance"));

        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.environment_evaluation_ray_count"),
                Component.translatable("cloth_config.sound_physics_remastered.environment_evaluation_ray_count.description"),
                SoundPhysicsMod.CONFIG.environmentEvaluationRayCount
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.environment_evaluation_ray_bounces"),
                Component.translatable("cloth_config.sound_physics_remastered.environment_evaluation_ray_bounces.description"),
                SoundPhysicsMod.CONFIG.environmentEvaluationRayBounces
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.non_full_block_occlusion_factor"),
                Component.translatable("cloth_config.sound_physics_remastered.non_full_block_occlusion_factor.description"),
                SoundPhysicsMod.CONFIG.nonFullBlockOcclusionFactor
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.max_occlusion_rays"),
                Component.translatable("cloth_config.sound_physics_remastered.max_occlusion_rays.description"),
                SoundPhysicsMod.CONFIG.maxOcclusionRays
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.max_occlusion"),
                Component.translatable("cloth_config.sound_physics_remastered.max_occlusion.description"),
                SoundPhysicsMod.CONFIG.maxOcclusion
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.strict_occlusion"),
                Component.translatable("cloth_config.sound_physics_remastered.strict_occlusion.description"),
                SoundPhysicsMod.CONFIG.strictOcclusion
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.sound_direction_evaluation"),
                Component.translatable("cloth_config.sound_physics_remastered.sound_direction_evaluation.description"),
                SoundPhysicsMod.CONFIG.soundDirectionEvaluation
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.redirect_non_occluded_sounds"),
                Component.translatable("cloth_config.sound_physics_remastered.redirect_non_occluded_sounds.description"),
                SoundPhysicsMod.CONFIG.redirectNonOccludedSounds
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.update_moving_sounds"),
                Component.translatable("cloth_config.sound_physics_remastered.update_moving_sounds.description"),
                SoundPhysicsMod.CONFIG.updateMovingSounds
        ));
        performance.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.sound_update_interval"),
                Component.translatable("cloth_config.sound_physics_remastered.sound_update_interval.description"),
                SoundPhysicsMod.CONFIG.soundUpdateInterval
        ));

        ConfigCategory reflectivity = builder.getOrCreateCategory(Component.translatable("cloth_config.sound_physics_remastered.category.reflectivity"));

        Map<BlockDefinition, Float> defaultReflectivityMap = new LinkedHashMap<>();
        SoundPhysicsMod.REFLECTIVITY_CONFIG.addDefaults(defaultReflectivityMap);

        for (Map.Entry<BlockDefinition, Float> entry : SoundPhysicsMod.REFLECTIVITY_CONFIG.getBlockDefinitions().entrySet()) {
            FloatListEntry e = entryBuilder
                    .startFloatField(entry.getKey().getName(), entry.getValue())
                    .setMin(0.01F)
                    .setMax(10F)
                    .setDefaultValue(defaultReflectivityMap.getOrDefault(entry.getKey(), SoundPhysicsMod.CONFIG.defaultBlockReflectivity.get()))
                    .setSaveConsumer(value -> SoundPhysicsMod.REFLECTIVITY_CONFIG.setBlockDefinitionValue(entry.getKey(), value)).build();
            reflectivity.addEntry(e);
        }

        ConfigCategory occlusion = builder.getOrCreateCategory(Component.translatable("cloth_config.sound_physics_remastered.category.occlusion"));

        Map<BlockDefinition, Float> defaultOcclusionMap = new LinkedHashMap<>();
        SoundPhysicsMod.OCCLUSION_CONFIG.addDefaults(defaultOcclusionMap);

        for (Map.Entry<BlockDefinition, Float> entry : SoundPhysicsMod.OCCLUSION_CONFIG.getBlockDefinitions().entrySet()) {
            FloatListEntry e = entryBuilder
                    .startFloatField(entry.getKey().getName(), entry.getValue())
                    .setMin(0F)
                    .setMax(10F)
                    .setDefaultValue(defaultOcclusionMap.getOrDefault(entry.getKey(), SoundPhysicsMod.CONFIG.defaultBlockOcclusionFactor.get()))
                    .setSaveConsumer(value -> SoundPhysicsMod.OCCLUSION_CONFIG.setBlockDefinitionValue(entry.getKey(), value)).build();
            occlusion.addEntry(e);
        }

        ConfigCategory logging = builder.getOrCreateCategory(Component.translatable("cloth_config.sound_physics_remastered.category.debug"));

        logging.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.debug_logging"),
                Component.translatable("cloth_config.sound_physics_remastered.debug_logging.description"),
                SoundPhysicsMod.CONFIG.debugLogging
        ));
        logging.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.occlusion_logging"),
                Component.translatable("cloth_config.sound_physics_remastered.occlusion_logging.description"),
                SoundPhysicsMod.CONFIG.occlusionLogging
        ));
        logging.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.environment_logging"),
                Component.translatable("cloth_config.sound_physics_remastered.environment_logging.description"),
                SoundPhysicsMod.CONFIG.environmentLogging
        ));
        logging.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.performance_logging"),
                Component.translatable("cloth_config.sound_physics_remastered.performance_logging.description"),
                SoundPhysicsMod.CONFIG.performanceLogging
        ));
        logging.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.render_sound_bounces"),
                Component.translatable("cloth_config.sound_physics_remastered.render_sound_bounces.description"),
                SoundPhysicsMod.CONFIG.renderSoundBounces
        ));
        logging.addEntry(fromConfigEntry(entryBuilder,
                Component.translatable("cloth_config.sound_physics_remastered.render_occlusion"),
                Component.translatable("cloth_config.sound_physics_remastered.render_occlusion.description"),
                SoundPhysicsMod.CONFIG.renderOcclusion
        ));

        builder.setSavingRunnable(() -> {
            Loggers.log("Saving configs");
            SoundPhysicsMod.CONFIG.enabled.save();
            SoundPhysicsMod.REFLECTIVITY_CONFIG.save();
            SoundPhysicsMod.OCCLUSION_CONFIG.save();
            SoundPhysicsMod.MAX_SOUNDS_PER_TICK_CONFIG.save();
            SoundPhysicsMod.CONFIG.reloadClient();
        });

        return builder.build();
    }

    private static <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, Component name, Component description, ConfigEntry<T> entry) {
        if (entry instanceof DoubleConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d))
                    .build();
        } else if (entry instanceof FloatConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startFloatField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d))
                    .build();
        } else if (entry instanceof IntegerConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(i -> e.set(i))
                    .build();
        } else if (entry instanceof BooleanConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, e.get())
                    .setTooltip(description)
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(b -> e.set(b))
                    .build();
        } else if (entry instanceof StringConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, e.get())
                    .setTooltip(description)
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(s -> e.set(s))
                    .build();
        }

        return null;
    }

}
