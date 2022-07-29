package com.jagrosh.jdautilities.commons.actions;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * an interface to determine nodes.
 *
 * @param <Q> type of the queue object.
 */
@FunctionalInterface
public interface Node<Q> extends CompletionStageEnvelope<Q>, FutureEnvelope<Q> {

    @NotNull
    @Override
    default Future<Q> future() {
        return this.queue();
    }

    /**
     * obtains the queue.
     *
     * @return queue.
     */
    @NotNull
    CompletableFuture<Q> queue();

    @NotNull
    @Override
    default CompletionStage<Q> stage() {
        return this.queue();
    }
}
