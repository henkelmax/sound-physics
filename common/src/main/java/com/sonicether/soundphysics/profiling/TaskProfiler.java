package com.sonicether.soundphysics.profiling;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;

import com.sonicether.soundphysics.Loggers;

public class TaskProfiler {

    private static final int TASK_RING_BUFFER_SIZE = 100;

    private final String identifier;                // Identifier of the profiler for logging
    private final Deque<Long> durations;            // Durations stored in milliseconds for each task
    private final AtomicLong tally;                 // Total number of profiling tasks finished

    public TaskProfiler(String identifier) {
        this.identifier = identifier;
        this.durations = new ArrayDeque<>(TASK_RING_BUFFER_SIZE);
        this.tally = new AtomicLong(0);
    }

    public TaskHandle profile() {
        return new TaskHandle();
    }

    public synchronized void addDuration(long duration) {
        if (durations.size() == TASK_RING_BUFFER_SIZE) {
            durations.poll();
        }

        durations.offer(duration);
        this.tally.getAndIncrement();
    }

    public long getTally() {
        return tally.get();
    }

    public synchronized double getTotalDuration() {
        return durations.stream().mapToLong(Long::longValue).sum();
    }

    public synchronized double getAverageDuration() {
        return durations.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    public synchronized long getMinDuration() {
        return durations.stream().max(Long::compareTo).orElse(Long.MIN_VALUE);
    }

    public synchronized long getMaxDuration() {
        return durations.stream().min(Long::compareTo).orElse(Long.MAX_VALUE);
    }

    public void logResults() {
        Loggers.logProfiling("Profile for task '{}', total: {} ms, average: {} ms, min: {} ms, max: {} ms", 
            identifier, getTotalDuration(), getAverageDuration(), getMinDuration(), getMaxDuration());
    }

    // Handle

    public class TaskHandle {
        private final long startTime;
        private long duration;
        private WeakReference<TaskProfiler> owner;

        private TaskHandle() {
            this.startTime = System.nanoTime();
        }

        public void finish() {
            TaskProfiler aggregator = owner.get();

            if (aggregator == null) {
                return;
            }

            long endTime = System.nanoTime();
            this.duration = (endTime - startTime) / 1_000_000;

            aggregator.addDuration(this.duration);
        }

        public long getDuration() {
            return duration;
        }
    }

}
