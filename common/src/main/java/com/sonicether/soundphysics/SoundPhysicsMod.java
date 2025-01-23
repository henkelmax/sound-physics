package com.sonicether.soundphysics;

import com.sonicether.soundphysics.config.MaxSoundsPerTickConfig;
import com.sonicether.soundphysics.config.OcclusionConfig;
import com.sonicether.soundphysics.config.ReflectivityConfig;
import com.sonicether.soundphysics.config.SoundPhysicsConfig;
import de.maxhenkel.configbuilder.ConfigBuilder;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public abstract class SoundPhysicsMod {

    public static final String MODID = "sound_physics_remastered";

    public static SoundPhysicsConfig CONFIG;
    public static ReflectivityConfig REFLECTIVITY_CONFIG;
    public static OcclusionConfig OCCLUSION_CONFIG;
    public static MaxSoundsPerTickConfig MAX_SOUNDS_PER_TICK_CONFIG;

    public void init() {
        initConfig();
    }

    public void initClient() {
        initConfig();
        CONFIG.reloadClient();

        renameAllowedSounds();

        REFLECTIVITY_CONFIG = new ReflectivityConfig(getConfigFolder().resolve(MODID).resolve("reflectivity.properties"));
        OCCLUSION_CONFIG = new OcclusionConfig(getConfigFolder().resolve(MODID).resolve("occlusion.properties"));
        MAX_SOUNDS_PER_TICK_CONFIG = new MaxSoundsPerTickConfig(getConfigFolder().resolve(MODID).resolve("max_sounds_per_tick.properties"));
    }

    private void initConfig() {
        if (CONFIG == null) {
            CONFIG = ConfigBuilder.builder(SoundPhysicsConfig::new).path(getConfigFolder().resolve(MODID).resolve("soundphysics.properties")).build();
        }
    }

    private void renameAllowedSounds() {
        Path oldPath = getConfigFolder().resolve(MODID).resolve("allowed_sounds.properties");
        Path newPath = getConfigFolder().resolve(MODID).resolve("max_sounds_per_tick.properties");

        try {
            Files.move(oldPath, newPath);
            Loggers.warn(oldPath.getFileName() + " file renamed to " + newPath.getFileName());
        } catch (NoSuchFileException ignored) {
        } catch (FileAlreadyExistsException ignored) {
            try {
                Files.delete(oldPath); // If max_sounds_per_tick was already generated but allowed_sounds for some reason returned(by user, etc) we should delete it
                Loggers.warn(oldPath.getFileName() + " was deleted due to " + newPath.getFileName() + " already exists.");
            } catch (IOException ignored1) {
            }
        } catch (IOException e) {
            Loggers.error("Error renaming file: " + e);
        }
    }

    public abstract Path getConfigFolder();

}
