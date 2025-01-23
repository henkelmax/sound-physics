package com.sonicether.soundphysics;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.sonicether.soundphysics.utils.RaycastUtils;
import com.sonicether.soundphysics.utils.SoundsPerTickCounter;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

import com.sonicether.soundphysics.config.ReverbParams;
import com.sonicether.soundphysics.debug.RaycastRenderer;
import com.sonicether.soundphysics.profiling.TaskProfiler;
import com.sonicether.soundphysics.profiling.TaskProfiler.TaskProfilerHandle;
import com.sonicether.soundphysics.utils.LevelAccessUtils;
import com.sonicether.soundphysics.world.ClientLevelProxy;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SoundPhysics {

    private static final float PHI = 1.618033988F;

    private static final Pattern AMBIENT_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.]+:ambient\\..*$");
    private static final Pattern BLOCK_PATTERN = Pattern.compile(".*block..*");
    private static final Pattern VOICECHAT_PATTERN = Pattern.compile("^voicechat:.*$");

    private static int auxFXSlot0;
    private static int auxFXSlot1;
    private static int auxFXSlot2;
    private static int auxFXSlot3;
    private static int reverb0;
    private static int reverb1;
    private static int reverb2;
    private static int reverb3;
    private static int directFilter0;
    private static int sendFilter0;
    private static int sendFilter1;
    private static int sendFilter2;
    private static int sendFilter3;

    private static Minecraft minecraft;
    private static TaskProfiler profiler;

    private static SoundSource lastSoundCategory;
    private static String lastSoundName;
    private static int maxAuxSends;

    public static void init() {
        Loggers.log("Initializing Sound Physics");
        setupEFX();
        Loggers.log("EFX ready");

        minecraft = Minecraft.getInstance();
        profiler = new TaskProfiler("Sound Physics");
    }

    public static void syncReverbParams() {
        if (auxFXSlot0 != 0) {
            //Set the global reverb parameters and apply them to the effect and effectslot
            setReverbParams(ReverbParams.getReverb0(), auxFXSlot0, reverb0);
            setReverbParams(ReverbParams.getReverb1(), auxFXSlot1, reverb1);
            setReverbParams(ReverbParams.getReverb2(), auxFXSlot2, reverb2);
            setReverbParams(ReverbParams.getReverb3(), auxFXSlot3, reverb3);
        }
    }

    static void setupEFX() {
        //Get current context and device
        long currentContext = ALC10.alcGetCurrentContext();
        long currentDevice = ALC10.alcGetContextsDevice(currentContext);

        if (ALC10.alcIsExtensionPresent(currentDevice, "ALC_EXT_EFX")) {
            Loggers.log("EFX Extension recognized");
        } else {
            Loggers.error("EFX Extension not found on current device. Aborting.");
            return;
        }

        maxAuxSends = ALC10.alcGetInteger(currentDevice, EXTEfx.ALC_MAX_AUXILIARY_SENDS);
        Loggers.log("Max auxiliary sends: {}", maxAuxSends);

        // Create auxiliary effect slots
        auxFXSlot0 = EXTEfx.alGenAuxiliaryEffectSlots();
        Loggers.log("Aux slot {} created", auxFXSlot0);
        EXTEfx.alAuxiliaryEffectSloti(auxFXSlot0, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL11.AL_TRUE);

        auxFXSlot1 = EXTEfx.alGenAuxiliaryEffectSlots();
        Loggers.log("Aux slot {} created", auxFXSlot1);
        EXTEfx.alAuxiliaryEffectSloti(auxFXSlot1, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL11.AL_TRUE);

        auxFXSlot2 = EXTEfx.alGenAuxiliaryEffectSlots();
        Loggers.log("Aux slot {} created", auxFXSlot2);
        EXTEfx.alAuxiliaryEffectSloti(auxFXSlot2, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL11.AL_TRUE);

        auxFXSlot3 = EXTEfx.alGenAuxiliaryEffectSlots();
        Loggers.log("Aux slot {} created", auxFXSlot3);
        EXTEfx.alAuxiliaryEffectSloti(auxFXSlot3, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL11.AL_TRUE);
        Loggers.logALError("Failed creating auxiliary effect slots");

        reverb0 = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(reverb0, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
        Loggers.logALError("Failed creating reverb effect slot 0");
        reverb1 = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(reverb1, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
        Loggers.logALError("Failed creating reverb effect slot 1");
        reverb2 = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(reverb2, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
        Loggers.logALError("Failed creating reverb effect slot 2");
        reverb3 = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(reverb3, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
        Loggers.logALError("Failed creating reverb effect slot 3");

        directFilter0 = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(directFilter0, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        Loggers.logDebug("directFilter0: {}", directFilter0);

        sendFilter0 = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(sendFilter0, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        Loggers.logDebug("filter0: {}", sendFilter0);

        sendFilter1 = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(sendFilter1, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        Loggers.logDebug("filter1: {}", sendFilter1);

        sendFilter2 = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(sendFilter2, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        Loggers.logDebug("filter2: {}", sendFilter2);

        sendFilter3 = EXTEfx.alGenFilters();
        EXTEfx.alFilteri(sendFilter3, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        Loggers.logDebug("filter3: {}", sendFilter3);
        Loggers.logALError("Error creating lowpass filters");

        syncReverbParams();
    }

    public static void setLastSoundCategoryAndName(SoundSource sc, String name) {
        lastSoundCategory = sc;
        lastSoundName = name;
    }

    /**
     * The old method signature of soundphysics to stay compatible
     */
    public static void onPlaySound(double posX, double posY, double posZ, int sourceID) {
        processSound(sourceID, posX, posY, posZ, lastSoundCategory, lastSoundName, false);
    }

    /**
     * The old method signature of soundphysics to stay compatible
     */
    public static void onPlayReverb(double posX, double posY, double posZ, int sourceID) {
        processSound(sourceID, posX, posY, posZ, lastSoundCategory, lastSoundName, true);
    }

    /**
     * Processes the current sound
     *
     * @return The new sound origin or null if it didn't change
     */
    public static Vec3 processSound(int source, double posX, double posY, double posZ, SoundSource category, String sound) {
        return processSound(source, posX, posY, posZ, category, sound, false);
    }

    /**
     * Processes the current sound
     *
     * @return The new sound origin or null if it didn't change
     */
    @Nullable
    public static Vec3 processSound(int source, double posX, double posY, double posZ, SoundSource category, String sound, boolean auxOnly) {
        if (!SoundPhysicsMod.CONFIG.enabled.get()) {
            return null;
        }

        Loggers.logDebug("Playing sound with source id '{}', position x:{}, y:{}, z:{}, \tcategory: '{}' \tname: '{}'", source, posX, posY, posZ, category.toString(), sound);

        TaskProfilerHandle profile = profiler.profile();
        @Nullable Vec3 newPos = evaluateEnvironment(source, posX, posY, posZ, category, sound, auxOnly);
        profile.finish();

        Loggers.logProfiling("Evaluated environment for sound {} in {} ms", sound, profile.getDuration());
        profiler.onTally(() -> profiler.logResults());

        return newPos;
    }

    @Nullable
    private static Vec3 evaluateEnvironment(int sourceID, double posX, double posY, double posZ, SoundSource category, String sound, boolean auxOnly) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (player == null || level == null || (posX == 0D && posY == 0D && posZ == 0D)) {
            setDefaultEnvironment(sourceID, auxOnly);
            return null;
        }
        Vec3 soundPos = new Vec3(posX, posY, posZ);
        double distance = player.position().distanceTo(soundPos);
        if (distance > SoundPhysicsMod.CONFIG.maxSoundProcessingDistance.get()) {
            Loggers.logDebug("Sound {} is too far away from player ({} blocks)", sound, distance);
            setDefaultEnvironment(sourceID, auxOnly);
            return null;
        }

        if (!SoundPhysicsMod.CONFIG.updateMovingSounds.get()) {
            if (category == SoundSource.RECORDS) {
                setDefaultEnvironment(sourceID, auxOnly);
                return null;
            }
        }

        if (SoundsPerTickCounter.getCountAndIncrement(sound) >= SoundPhysicsMod.MAX_SOUNDS_PER_TICK_CONFIG.getMaxCount(sound)) {
            setDefaultEnvironment(sourceID, auxOnly);
            return null;
        }

        if (!SoundPhysicsMod.CONFIG.evaluateAmbientSounds.get() && isAmbientSound(sound)) {
            setDefaultEnvironment(sourceID, auxOnly);
            return null;
        }

        float directCutoff;
        float absorptionCoeff = (float) (SoundPhysicsMod.CONFIG.blockAbsorption.get() * 3D);

        // Direct sound occlusion

        Vec3 playerPos = minecraft.gameRenderer.getMainCamera().getPosition();
        Vec3 normalToPlayer = playerPos.subtract(soundPos).normalize();

        BlockPos soundBlockPos = BlockPos.containing(soundPos);

        Loggers.logDebug("Player pos: {}, {}, {} \tSound Pos: {}, {}, {} \tTo player vector: {}, {}, {}", playerPos.x, playerPos.y, playerPos.z, soundPos.x, soundPos.y, soundPos.z, normalToPlayer.x, normalToPlayer.y, normalToPlayer.z);

        double occlusionAccumulation = calculateOcclusion(soundPos, playerPos, category, sound);

        directCutoff = (float) Math.exp(-occlusionAccumulation * absorptionCoeff);
        float directGain = auxOnly ? 0F : (float) Math.pow(directCutoff, 0.1D);

        Loggers.logOcclusion("Direct cutoff: {}, direct gain: {}", directCutoff, directGain);

        // Calculate reverb parameters

        float sendGain0 = 0F;
        float sendGain1 = 0F;
        float sendGain2 = 0F;
        float sendGain3 = 0F;

        float sendCutoff0 = 1F;
        float sendCutoff1 = 1F;
        float sendCutoff2 = 1F;
        float sendCutoff3 = 1F;

        if (minecraft.player.isUnderWater()) {
            directCutoff *= 1F - SoundPhysicsMod.CONFIG.underwaterFilter.get();
        }

        // Shoot rays around sound

        float maxDistance = 256F;

        int numRays = SoundPhysicsMod.CONFIG.environmentEvaluationRayCount.get();
        int rayBounces = SoundPhysicsMod.CONFIG.environmentEvaluationRayBounces.get();

        ReflectedAudio audioDirection = new ReflectedAudio(occlusionAccumulation, sound);

        float[] bounceReflectivityRatio = new float[rayBounces];

        float rcpTotalRays = 1F / (numRays * rayBounces);

        float gAngle = PHI * (float) Math.PI * 2F;

        Vec3 directSharedAirspaceVector = getSharedAirspace(soundPos, playerPos);

        if (directSharedAirspaceVector != null) {
            audioDirection.addDirectAirspace(directSharedAirspaceVector);
        }

        for (int i = 0; i < numRays; i++) {
            float fiN = (float) i / numRays;
            float longitude = gAngle * (float) i * 1F;
            float latitude = (float) Math.asin(fiN * 2F - 1F);

            Vec3 rayDir = new Vec3(Math.cos(latitude) * Math.cos(longitude), Math.cos(latitude) * Math.sin(longitude), Math.sin(latitude));

            Vec3 rayEnd = new Vec3(soundPos.x + rayDir.x * maxDistance, soundPos.y + rayDir.y * maxDistance, soundPos.z + rayDir.z * maxDistance);

            BlockHitResult rayHit = RaycastUtils.rayCast(getLevelProxy(), soundPos, rayEnd, soundBlockPos);

            if (rayHit.getType() == HitResult.Type.BLOCK) {
                double rayLength = soundPos.distanceTo(rayHit.getLocation());

                // Additional bounces
                BlockPos lastHitBlock = rayHit.getBlockPos();
                Vec3 lastHitPos = rayHit.getLocation();
                Vec3 lastHitNormal = new Vec3(rayHit.getDirection().step());
                Vec3 lastRayDir = rayDir;

                float totalRayDistance = (float) rayLength;

                RaycastRenderer.addSoundBounceRay(soundPos, rayHit.getLocation(), ChatFormatting.GREEN.getColor());

                Vec3 firstSharedAirspaceVector = getSharedAirspace(rayHit, playerPos);
                if (firstSharedAirspaceVector != null) {
                    audioDirection.addSharedAirspace(firstSharedAirspaceVector, totalRayDistance);
                }

                // Secondary ray bounces
                for (int j = 0; j < rayBounces; j++) {
                    Vec3 newRayDir = reflect(lastRayDir, lastHitNormal);
                    Vec3 newRayStart = lastHitPos;
                    Vec3 newRayEnd = new Vec3(newRayStart.x + newRayDir.x * maxDistance, newRayStart.y + newRayDir.y * maxDistance, newRayStart.z + newRayDir.z * maxDistance);

                    BlockHitResult newRayHit = RaycastUtils.rayCast(getLevelProxy(), newRayStart, newRayEnd, lastHitBlock);

                    float blockReflectivity = getBlockReflectivity(lastHitBlock);
                    float energyTowardsPlayer = 0.25F * (blockReflectivity * 0.75F + 0.25F);

                    if (newRayHit.getType() == HitResult.Type.MISS) {
                        totalRayDistance += lastHitPos.distanceTo(playerPos);

                        RaycastRenderer.addSoundBounceRay(newRayStart, newRayEnd, ChatFormatting.RED.getColor());
                    } else {
                        Vec3 newRayHitPos = newRayHit.getLocation();

                        RaycastRenderer.addSoundBounceRay(newRayStart, newRayHitPos, ChatFormatting.BLUE.getColor());

                        double newRayLength = lastHitPos.distanceTo(newRayHitPos);

                        bounceReflectivityRatio[j] += blockReflectivity;

                        totalRayDistance += newRayLength;

                        lastHitPos = newRayHitPos;
                        lastHitNormal = new Vec3(newRayHit.getDirection().step());
                        lastRayDir = newRayDir;
                        lastHitBlock = newRayHit.getBlockPos();

                        Vec3 sharedAirspaceVector = getSharedAirspace(newRayHit, playerPos);
                        if (sharedAirspaceVector != null) {
                            audioDirection.addSharedAirspace(sharedAirspaceVector, totalRayDistance);
                        }
                    }
                    // Bandaid solution for distance based attenuation
                    if (totalRayDistance < SoundPhysicsMod.CONFIG.reverbAttenuationDistance.get()) {
                        continue;
                    }

                    float reflectionDelay = (float) Math.max(totalRayDistance, 0D) * 0.12F * blockReflectivity;

                    float cross0 = 1F - Mth.clamp(Math.abs(reflectionDelay - 0F), 0F, 1F);
                    float cross1 = 1F - Mth.clamp(Math.abs(reflectionDelay - 1F), 0F, 1F);
                    float cross2 = 1F - Mth.clamp(Math.abs(reflectionDelay - 2F), 0F, 1F);
                    float cross3 = Mth.clamp(reflectionDelay - 2F, 0F, 1F);

                    sendGain0 += cross0 * energyTowardsPlayer * 6.4F * rcpTotalRays;
                    sendGain1 += cross1 * energyTowardsPlayer * 12.8F * rcpTotalRays;
                    sendGain2 += cross2 * energyTowardsPlayer * 12.8F * rcpTotalRays;
                    sendGain3 += cross3 * energyTowardsPlayer * 12.8F * rcpTotalRays;

                    // Nowhere to bounce off of, stop bouncing!
                    if (newRayHit.getType() == HitResult.Type.MISS) {
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < bounceReflectivityRatio.length; i++) {
            bounceReflectivityRatio[i] = bounceReflectivityRatio[i] / numRays;
            Loggers.logEnvironment("Bounce reflectivity {}: {}", i, bounceReflectivityRatio[i]);
        }

        @Nullable Vec3 newSoundPos = audioDirection.evaluateSoundPosition(soundPos, playerPos);

        if (newSoundPos != null) {
            setSoundPos(sourceID, newSoundPos);
        }

        float sharedAirspace = audioDirection.getSharedAirspaces() * 64F * rcpTotalRays;

        Loggers.logEnvironment("Shared airspace: {} ({})", sharedAirspace, audioDirection.getSharedAirspaces());

        float sharedAirspaceWeight0 = Mth.clamp(sharedAirspace / 20F, 0F, 1F);
        float sharedAirspaceWeight1 = Mth.clamp(sharedAirspace / 15F, 0F, 1F);
        float sharedAirspaceWeight2 = Mth.clamp(sharedAirspace / 10F, 0F, 1F);
        float sharedAirspaceWeight3 = Mth.clamp(sharedAirspace / 10F, 0F, 1F);

        sendCutoff0 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1F) * (1F - sharedAirspaceWeight0) + sharedAirspaceWeight0;
        sendCutoff1 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1F) * (1F - sharedAirspaceWeight1) + sharedAirspaceWeight1;
        sendCutoff2 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1F) * (1F - sharedAirspaceWeight2) + sharedAirspaceWeight2;
        sendCutoff3 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1F) * (1F - sharedAirspaceWeight3) + sharedAirspaceWeight3;

        // Attempt to preserve directionality when airspace is shared by allowing some of the dry signal through but filtered
        float averageSharedAirspace = (sharedAirspaceWeight0 + sharedAirspaceWeight1 + sharedAirspaceWeight2 + sharedAirspaceWeight3) * 0.25F;
        directCutoff = Math.max((float) Math.pow(averageSharedAirspace, 0.5D) * 0.2F, directCutoff);
        directGain = auxOnly ? 0F : (float) Math.pow(directCutoff, 0.1D);

        sendGain1 *= bounceReflectivityRatio[1];

        if (bounceReflectivityRatio.length > 2) {
            sendGain2 *= (float) Math.pow(bounceReflectivityRatio[2], 3D);
        }
        if (bounceReflectivityRatio.length > 3) {
            sendGain3 *= (float) Math.pow(bounceReflectivityRatio[3], 4D);
        }

        sendGain0 = Mth.clamp(sendGain0, 0F, 1F);
        sendGain1 = Mth.clamp(sendGain1, 0F, 1F);
        sendGain2 = Mth.clamp(sendGain2 * 1.05F - 0.05F, 0F, 1F);
        sendGain3 = Mth.clamp(sendGain3 * 1.05F - 0.05F, 0F, 1F);

        sendGain0 *= (float) Math.pow(sendCutoff0, 0.1D);
        sendGain1 *= (float) Math.pow(sendCutoff1, 0.1D);
        sendGain2 *= (float) Math.pow(sendCutoff2, 0.1D);
        sendGain3 *= (float) Math.pow(sendCutoff3, 0.1D);

        // I don't know how else to fix reverb not being attenuated by distance
        // We should look into this
        float soundDistance = (float) playerPos.distanceTo(soundPos);
        float maxSoundDistance = AL10.alGetSourcef(sourceID, AL10.AL_MAX_DISTANCE);
        float sendGainMultiplier = 1F - Math.min(soundDistance / (maxSoundDistance * SoundPhysicsMod.CONFIG.reverbDistance.get()), 1F);
        sendGain0 = sendGainMultiplier * sendGain0;
        sendGain1 = sendGainMultiplier * sendGain1;
        sendGain2 = sendGainMultiplier * sendGain2;
        sendGain3 = sendGainMultiplier * sendGain3;

        Loggers.logEnvironment("Final environment settings: {}, {}, {}, {}", sendGain0, sendGain1, sendGain2, sendGain3);

        assert minecraft.player != null;
        if (minecraft.player.isUnderWater()) {
            sendCutoff0 *= 0.4F;
            sendCutoff1 *= 0.4F;
            sendCutoff2 *= 0.4F;
            sendCutoff3 *= 0.4F;
        }

        setEnvironment(sourceID, sendGain0, sendGain1, sendGain2, sendGain3, sendCutoff0, sendCutoff1, sendCutoff2, sendCutoff3, directCutoff, directGain);

        return newSoundPos;
    }

    public static boolean isVoicechatSound(String sound) {
        return VOICECHAT_PATTERN.matcher(sound).matches();
    }

    public static boolean isAmbientSound(String sound) {
        return AMBIENT_PATTERN.matcher(sound).matches();
    }

    private static float getBlockReflectivity(BlockPos blockPos) {
        var levelProxy = getLevelProxy();

        if (levelProxy == null) {
            return SoundPhysicsMod.CONFIG.defaultBlockReflectivity.get();
        }

        BlockState blockState = levelProxy.getBlockState(blockPos);
        return SoundPhysicsMod.REFLECTIVITY_CONFIG.getBlockDefinitionValue(blockState);
    }

    private static Vec3 reflect(Vec3 dir, Vec3 normal) {
        //dir - 2.0 * dot(normal, dir) * normal
        double dot = dir.dot(normal) * 2D;

        double x = dir.x - dot * normal.x;
        double y = dir.y - dot * normal.y;
        double z = dir.z - dot * normal.z;

        return new Vec3(x, y, z);
    }

    private static double calculateOcclusion(Vec3 soundPos, Vec3 playerPos, SoundSource category, String sound) {
        if (SoundPhysicsMod.CONFIG.strictOcclusion.get()) {
            return Math.min(runOcclusion(soundPos, playerPos), SoundPhysicsMod.CONFIG.maxOcclusion.get());
        }
        boolean isBlock = category == SoundSource.BLOCKS || BLOCK_PATTERN.matcher(sound).matches();
        double variationFactor = SoundPhysicsMod.CONFIG.occlusionVariation.get();

        if (isBlock) {
            variationFactor = Math.max(variationFactor, 0.49D);
        }

        double occlusionAccMin = Double.MAX_VALUE;
        occlusionAccMin = Math.min(occlusionAccMin, runOcclusion(soundPos, playerPos));

        if (variationFactor > 0D) {
            for (int x = -1; x <= 1; x += 2) {
                for (int y = -1; y <= 1; y += 2) {
                    for (int z = -1; z <= 1; z += 2) {
                        Vec3 offset = new Vec3(x, y, z).scale(variationFactor);
                        occlusionAccMin = Math.min(occlusionAccMin, runOcclusion(soundPos.add(offset), playerPos.add(offset)));
                    }
                }
            }
        }

        return Math.min(occlusionAccMin, SoundPhysicsMod.CONFIG.maxOcclusion.get());
    }

    private static double runOcclusion(Vec3 soundPos, Vec3 playerPos) {
        ClientLevelProxy levelProxy = getLevelProxy();

        if (levelProxy == null) {
            return 0D;
        }

        double occlusionAccumulation = 0D;
        Vec3 rayOrigin = soundPos;

        BlockPos lastBlockPos = BlockPos.containing(soundPos);

        for (int i = 0; i < SoundPhysicsMod.CONFIG.maxOcclusionRays.get(); i++) {
            BlockHitResult rayHit = RaycastUtils.rayCast(getLevelProxy(), rayOrigin, playerPos, lastBlockPos);

            lastBlockPos = rayHit.getBlockPos();

            if (rayHit.getType() == HitResult.Type.MISS) {
                RaycastRenderer.addOcclusionRay(rayOrigin, playerPos.add(0D, -0.1D, 0D), Mth.hsvToRgb(1F / 3F * (1F - Math.min(1F, (float) occlusionAccumulation / 12F)), 1F, 1F));
                break;
            }

            RaycastRenderer.addOcclusionRay(rayOrigin, rayHit.getLocation(), Mth.hsvToRgb(1F / 3F * (1F - Math.min(1F, (float) occlusionAccumulation / 12F)), 1F, 1F));

            BlockPos blockHitPos = rayHit.getBlockPos();
            rayOrigin = rayHit.getLocation();

            BlockState blockHit = levelProxy.getBlockState(blockHitPos);
            float blockOcclusion = SoundPhysicsMod.OCCLUSION_CONFIG.getBlockDefinitionValue(blockHit);

            // Regardless to whether we hit from inside or outside
            Vec3 dirVec = rayOrigin.subtract(blockHitPos.getX() + 0.5D, blockHitPos.getY() + 0.5D, blockHitPos.getZ() + 0.5D);
            Direction sideHit = Direction.getNearest(dirVec.x, dirVec.y, dirVec.z);

            if (!blockHit.isFaceSturdy(levelProxy, rayHit.getBlockPos(), sideHit)) {
                blockOcclusion *= SoundPhysicsMod.CONFIG.nonFullBlockOcclusionFactor.get();
            }

            Loggers.logOcclusion("{} \t{},{},{}", blockHit.getBlock().getDescriptionId(), rayOrigin.x, rayOrigin.y, rayOrigin.z);

            // Accumulate density
            occlusionAccumulation += blockOcclusion;

            if (occlusionAccumulation > SoundPhysicsMod.CONFIG.maxOcclusion.get()) {
                Loggers.logOcclusion("Max occlusion reached after {} steps", i + 1);
                break;
            }
        }

        return occlusionAccumulation;
    }

    /**
     * Returns a proxy to access the client level with a thread-safe level clone if configured.
     * May return null if caching is enabled but no cache has been created yet on the main thread.
     * May return an unsafe client level cast to a level proxy if caching is disabled.
     */
    private static ClientLevelProxy getLevelProxy() {
        return LevelAccessUtils.getClientLevelProxy(minecraft);
    }

    /**
     * Checks if the hit shares the same airspace with the listener
     *
     * @param hit              the hit position
     * @param listenerPosition the position of the listener
     * @return the vector between the hit and the listener or null if there is no shared airspace
     */
    @Nullable
    private static Vec3 getSharedAirspace(BlockHitResult hit, Vec3 listenerPosition) {
        Vector3f hitNormal = hit.getDirection().step();
        Vec3 rayStart = new Vec3(hit.getLocation().x + hitNormal.x() * 0.001D, hit.getLocation().y + hitNormal.y() * 0.001D, hit.getLocation().z + hitNormal.z() * 0.001D);
        return getSharedAirspace(rayStart, listenerPosition);
    }

    /**
     * Checks if the hit shares the same airspace with the listener
     *
     * @param soundPosition    the sound position
     * @param listenerPosition the position of the listener
     * @return the vector between the hit and the listener or null if there is no shared airspace
     */
    @Nullable
    private static Vec3 getSharedAirspace(Vec3 soundPosition, Vec3 listenerPosition) {
        BlockHitResult finalRayHit = RaycastUtils.rayCast(getLevelProxy(), soundPosition, listenerPosition, null);
        if (finalRayHit.getType() == HitResult.Type.MISS) {
            RaycastRenderer.addSoundBounceRay(soundPosition, listenerPosition.add(0D, -0.1D, 0D), ChatFormatting.WHITE.getColor());
            return soundPosition.subtract(listenerPosition);
        }
        return null;
    }

    public static void setDefaultEnvironment(int sourceID) {
        setDefaultEnvironment(sourceID, false);
    }

    public static void setDefaultEnvironment(int sourceID, boolean auxOnly) {
        setEnvironment(sourceID, 0F, 0F, 0F, 0F, 1F, 1F, 1F, 1F, 1F, auxOnly ? 0F : 1F);
    }

    public static void setEnvironment(int sourceID, float sendGain0, float sendGain1, float sendGain2, float sendGain3, float sendCutoff0, float sendCutoff1, float sendCutoff2, float sendCutoff3, float directCutoff, float directGain) {
        if (!SoundPhysicsMod.CONFIG.enabled.get()) {
            return;
        }
        // Set reverb send filter values and set source to send to all reverb fx slots

        if (maxAuxSends >= 4) {
            EXTEfx.alFilterf(sendFilter0, EXTEfx.AL_LOWPASS_GAIN, sendGain0);
            EXTEfx.alFilterf(sendFilter0, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff0);
            AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot0, 3, sendFilter0);
            Loggers.logALError("Set environment filter0:");
        }

        if (maxAuxSends >= 3) {
            EXTEfx.alFilterf(sendFilter1, EXTEfx.AL_LOWPASS_GAIN, sendGain1);
            EXTEfx.alFilterf(sendFilter1, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff1);
            AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot1, 2, sendFilter1);
            Loggers.logALError("Set environment filter1:");
        }

        if (maxAuxSends >= 2) {
            EXTEfx.alFilterf(sendFilter2, EXTEfx.AL_LOWPASS_GAIN, sendGain2);
            EXTEfx.alFilterf(sendFilter2, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff2);
            AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot2, 1, sendFilter2);
            Loggers.logALError("Set environment filter2:");
        }

        if (maxAuxSends >= 1) {
            EXTEfx.alFilterf(sendFilter3, EXTEfx.AL_LOWPASS_GAIN, sendGain3);
            EXTEfx.alFilterf(sendFilter3, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff3);
            AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot3, 0, sendFilter3);
            Loggers.logALError("Set environment filter3:");
        }

        EXTEfx.alFilterf(directFilter0, EXTEfx.AL_LOWPASS_GAIN, directGain);
        EXTEfx.alFilterf(directFilter0, EXTEfx.AL_LOWPASS_GAINHF, directCutoff);
        AL11.alSourcei(sourceID, EXTEfx.AL_DIRECT_FILTER, directFilter0);
        Loggers.logALError("Set environment directFilter0:");

        AL11.alSourcef(sourceID, EXTEfx.AL_AIR_ABSORPTION_FACTOR, SoundPhysicsMod.CONFIG.airAbsorption.get());
        Loggers.logALError("Set environment airAbsorption:");
    }

    private static void setSoundPos(int sourceID, Vec3 pos) {
        AL11.alSource3f(sourceID, AL11.AL_POSITION, (float) pos.x, (float) pos.y, (float) pos.z);
    }

    /*
     * Applies the parameters in the enum ReverbParams to the main reverb effect.
     */
    protected static void setReverbParams(ReverbParams r, int auxFXSlot, int reverbSlot) {
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DENSITY, r.density);
        Loggers.logALError("Error while assigning reverb density: " + r.density);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DIFFUSION, r.diffusion);
        Loggers.logALError("Error while assigning reverb diffusion: " + r.diffusion);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_GAIN, r.gain);
        Loggers.logALError("Error while assigning reverb gain: " + r.gain);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_GAINHF, r.gainHF);
        Loggers.logALError("Error while assigning reverb gainHF: " + r.gainHF);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DECAY_TIME, r.decayTime);
        Loggers.logALError("Error while assigning reverb decayTime: " + r.decayTime);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, r.decayHFRatio);
        Loggers.logALError("Error while assigning reverb decayHFRatio: " + r.decayHFRatio);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, r.reflectionsGain);
        Loggers.logALError("Error while assigning reverb reflectionsGain: " + r.reflectionsGain);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, r.lateReverbGain);
        Loggers.logALError("Error while assigning reverb lateReverbGain: " + r.lateReverbGain);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, r.lateReverbDelay);
        Loggers.logALError("Error while assigning reverb lateReverbDelay: " + r.lateReverbDelay);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, r.airAbsorptionGainHF);
        Loggers.logALError("Error while assigning reverb airAbsorptionGainHF: " + r.airAbsorptionGainHF);
        EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, r.roomRolloffFactor);
        Loggers.logALError("Error while assigning reverb roomRolloffFactor: " + r.roomRolloffFactor);

        // Attach updated effect object
        EXTEfx.alAuxiliaryEffectSloti(auxFXSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbSlot);
    }

}
