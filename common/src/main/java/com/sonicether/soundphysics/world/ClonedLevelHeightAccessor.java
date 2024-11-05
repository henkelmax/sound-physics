package com.sonicether.soundphysics.world;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

/**
 * Read-only sparse clone of a client level height accessor.
 *
 * @author Saint (@augustsaintfreytag)
 */
public class ClonedLevelHeightAccessor implements LevelHeightAccessor {

    private final int height;
    private final int getMinY;

    public ClonedLevelHeightAccessor(Level level) {
        height = level.getHeight();
        getMinY = level.getMinY();
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getMinY() {
        return getMinY;
    }

    public int getMinBuildHeight() {
        return getMinY;
    }

}