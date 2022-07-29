package com.jagrosh.jdautilities.commons.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine delete nodes.
 *
 * @param <D> type of the after delete node.
 * @param <Q> type of the queue object.
 */
public interface DeleteNode<D, Q> extends Node<Q> {

    /**
     * deletes the context.
     */
    @NotNull
    @Contract("-> new")
    D delete();

    /**
     * deletes the context.
     *
     * @param time the time to delete.
     * @param unit the unit to delete.
     */
    @NotNull
    @Contract("_, _ -> new")
    D delete(long time, @NotNull TimeUnit unit);

    /**
     * deletes the context.
     *
     * @param duration the duration to delete.
     */
    @NotNull
    @Contract("_ -> new")
    default D delete(@NotNull final Duration duration) {
        return this.delete(duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
