package com.sonicether.soundphysics.utils;

import com.sonicether.soundphysics.Loggers;
import com.sonicether.soundphysics.profiling.TaskProfiler;
import com.sonicether.soundphysics.world.CachingClientLevel;
import com.sonicether.soundphysics.world.ClientLevelProxy;
import com.sonicether.soundphysics.world.ClonedClientLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Utility module to manage creation, invalidation, and updating of client level clones.
 * 
 * Level clones are created on a client tick basis and retained for some time.
 * Any module on any thread may access the cached level clone for read-only world operations.
 * 
 * @author Saint (@augustsaintfreytag)
 */
public class LevelAccessUtils {

    // Configuration

    private static final boolean USE_UNSAFE_LEVEL_ACCESS = false;           // Disable level clone and cache and fall back to original main thread access. (Default: false)
    private static final int LEVEL_CLONE_RANGE = 4;                         // Cloned number of chunks in radius around player position. (Default: 4 chunks)
    private static final long LEVEL_CLONE_MAX_RETAIN_TICKS = 20;            // Maximum number of ticks to retain level clone in cache. (Default: 20 ticks / 1 second)
    private static final long LEVEL_CLONE_MAX_RETAIN_BLOCK_DISTANCE = 16;   // Maximum distance player can move from cloned origin before invalidation. (Default: 25% clone radius)

    private static final TaskProfiler profiler = new TaskProfiler("Level Caching");

    // Cache Write

    public static void tickLevelCache(ClientLevel clientLevel, Player player) {
        if (USE_UNSAFE_LEVEL_ACCESS) {
            // Disable all level cloning, use direct unsafe main thread access (original behavior).
            return;
        }

        var currentTick = clientLevel.getGameTime();
        var origin = levelOriginFromPlayer(player);
        
        // Cast client level reference to interface to access injected level cache property.
        var cachingClientLevel = (CachingClientLevel) (Object) clientLevel;
        var clientLevelClone = cachingClientLevel.getCachedClone();
        
        if (clientLevelClone == null) {
            // No cache exists, cache first level clone.

            Loggers.logDebug("Creating new level cache, no existing level clone found in client cache.");
            updateLevelCache(clientLevel, origin, LEVEL_CLONE_MAX_RETAIN_TICKS);
            return;
        }

        var ticksSinceLastClone = currentTick - clientLevelClone.getTick();
        var distanceSinceLastClone = origin.distSqr(clientLevelClone.getOrigin());

        if (ticksSinceLastClone >= LEVEL_CLONE_MAX_RETAIN_TICKS || distanceSinceLastClone >= LEVEL_CLONE_MAX_RETAIN_BLOCK_DISTANCE) {
            // Cache expired or player travelled too far from last clone origin point, update cache.
            
            Loggers.logDebug(
                "Updating level cache, cache expired ({}/{} ticks) or player moved too far ({}/{} block(s)) from last clone origin.", 
                ticksSinceLastClone, LEVEL_CLONE_MAX_RETAIN_TICKS, distanceSinceLastClone, LEVEL_CLONE_MAX_RETAIN_BLOCK_DISTANCE
            );
            
            updateLevelCache(clientLevel, origin, currentTick);
        }
    }

    private static void updateLevelCache(ClientLevel clientLevel, BlockPos origin, long tick) {
        Loggers.logDebug("Updating level cache, creating new level clone with origin {} on tick {}.", origin.toShortString(), tick);

        var profile = profiler.profile();
        var cachingClientLevel = (CachingClientLevel) (Object) clientLevel;
        var clientLevelClone = new ClonedClientLevel(clientLevel, origin, tick, LEVEL_CLONE_RANGE);

        cachingClientLevel.setCachedClone(clientLevelClone);

        profile.finish();

        Loggers.logProfiling("Updated client level clone in cache in {} ms", profile.getDuration());
        profiler.onTally(() -> profiler.logResults());
    }

    // Cache Read

    public static ClientLevelProxy getClientLevelProxy(Minecraft client) {
        var clientLevel = client.level;

        if (clientLevel == null) {
            Loggers.warn("Can not return client level proxy, client level does not exist.");
            return null;
        }

        if (USE_UNSAFE_LEVEL_ACCESS) {
            return (ClientLevelProxy) clientLevel;
        }

        var cachingClientLevel = (CachingClientLevel) (Object) clientLevel;
        var clientLevelClone = cachingClientLevel.getCachedClone();

        if (clientLevelClone == null) {
            Loggers.warn("Can not return client level proxy, client level clone has not been cached. This might only occur once on load.");
            return null;
        }

        return clientLevelClone;
    }

    // Utilities

    private static BlockPos levelOriginFromPlayer(Player player) {
        var playerPos = player.position();
        return new BlockPos((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);
    }

}
