package com.jagrosh.jdautilities.commons.channelactions;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * an interface to determine future envelopes.
 *
 * @param <T> type of the object.
 */
public interface FutureEnvelope<T> extends Future<T> {

    @Override
    default boolean cancel(final boolean mayInterruptIfRunning) {
        return this.future().cancel(mayInterruptIfRunning);
    }

    @Override
    default boolean isCancelled() {
        return this.future().isCancelled();
    }

    @Override
    default boolean isDone() {
        return this.future().isDone();
    }

    @Override
    default T get() throws InterruptedException, ExecutionException {
        return this.future().get();
    }

    @Override
    default T get(final long timeout, @NotNull final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return this.future().get(timeout, unit);
    }

    /**
     * obtains the delegate.
     *
     * @return the delegate.
     */
    @NotNull
    Future<T> future();
}
