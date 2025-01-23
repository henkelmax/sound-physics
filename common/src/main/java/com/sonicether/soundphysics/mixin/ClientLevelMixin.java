package com.sonicether.soundphysics.mixin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import com.sonicether.soundphysics.utils.SoundsPerTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sonicether.soundphysics.utils.LevelAccessUtils;
import com.sonicether.soundphysics.world.CachingClientLevel;
import com.sonicether.soundphysics.world.ClonedClientLevel;

import net.minecraft.client.multiplayer.ClientLevel;

import javax.annotation.Nullable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements CachingClientLevel {

    @Unique
    private final AtomicReference<ClonedClientLevel> cachedClone = new AtomicReference<>();

    @Unique
    @Nullable
    public ClonedClientLevel sound_physics_remastered$getCachedClone() {
        return cachedClone.get();
    }

    @Unique
    public void sound_physics_remastered$setCachedClone(@Nullable ClonedClientLevel clonedClientLevel) {
        cachedClone.set(clonedClientLevel);
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    private void tick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        // Note: Mods may use mixins to inject logic that runs after this level clone operation,
        // any changes made on tick would not be included. Sound and level caching mixins could be
        // split and assigned different priorities to address this.
        LevelAccessUtils.tickLevelCache((ClientLevel) (Object) this);
        SoundsPerTickCounter.resetAllCounts();
    }

}