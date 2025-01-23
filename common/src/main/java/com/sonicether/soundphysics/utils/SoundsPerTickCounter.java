package com.sonicether.soundphysics.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoundsPerTickCounter {

    // Static map to store sound counts, shared across the application
    private static final Map<String, Integer> soundCounts = new ConcurrentHashMap<>();

    private SoundsPerTickCounter() {}

    /**
     * Returns the current count of how many times the specified sound has been encountered, then increments it.
     * If the sound is new, returns 0 and starts counting from 1.
     *
     * @param sound the identifier of the sound to track
     * @return the count of how many times the sound has been encountered so far
     */
    public static int getCountAndIncrement(String sound) {
        return soundCounts.merge(sound, 1, Integer::sum) - 1;
    }


    /**
     * Resets all sound counts to 0 by clearing the map.
     */
    public static void resetAllCounts() {
        soundCounts.clear();
    }
}
