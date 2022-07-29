package com.jagrosh.jdautilities.commons.channelactions;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine delete nodes.
 *
 * @param <D> type of the after delete node.
 * @param <N> type of the node itself.
 * @param <F> type of the queue.
 */
public interface DeleteNode<D, N, F> extends Node<N, F> {

    /**
     * deletes the context.
     */
    @NotNull
    D delete();

    /**
     * deletes the context.
     *
     * @param time the time to delete.
     * @param unit the unit to delete.
     */
    @NotNull
    D delete(long time, @NotNull TimeUnit unit);

    /**
     * deletes the context.
     *
     * @param duration the duration to delete.
     */
    @NotNull
    default D delete(@NotNull final Duration duration) {
        return this.delete(duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
