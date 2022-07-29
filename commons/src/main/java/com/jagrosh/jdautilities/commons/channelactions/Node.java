package com.jagrosh.jdautilities.commons.channelactions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * an interface to determine nodes.
 *
 * @param <S> type of the self implementation.
 * @param <F> type of the queue object.
 */
public interface Node<S, F> extends CompletionStageEnvelope<F>, FutureEnvelope<F> {

    @NotNull
    @Override
    default Future<F> future() {
        return this.queue();
    }

    /**
     * obtains the queue.
     *
     * @return queue.
     */
    @NotNull
    CompletableFuture<F> queue();

    @NotNull
    @Override
    default CompletionStage<F> stage() {
        return this.queue();
    }

    /**
     * creates a new node with the future.
     *
     * @param future the future to create.
     * @return newly created node.
     */
    @NotNull
    @Contract("_ -> new")
    S with(@NotNull CompletableFuture<F> future);
}
