package com.jagrosh.jdautilities.commons.channelactions;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine channel nodes.
 */
public interface ChannelNode {

    /**
     * deletes the channel.
     */
    void delete();

    /**
     * deletes the channel.
     *
     * @param time the time to delete.
     * @param unit the unit to delete.
     */
    void delete(long time, @NotNull TimeUnit unit);

    /**
     * deletes the channel.
     *
     * @param duration the duration to delete.
     */
    default void delete(@NotNull final Duration duration) {
        this.delete(duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
