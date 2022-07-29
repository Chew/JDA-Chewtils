package com.jagrosh.jdautilities.commons.actions;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * an interface to determine completion stage envelopes.
 *
 * @param <T> type of the object.
 */
public interface CompletionStageEnvelope<T> extends CompletionStage<T> {

    /**
     * obtains the delegate.
     *
     * @return the delegate.
     */
    @NotNull
    CompletionStage<T> stage();

    @Override
    default <U> CompletionStage<U> thenApply(
        final Function<? super T, ? extends U> fn
    ) {
        return this.stage().thenApply(fn);
    }

    @Override
    default <U> CompletionStage<U> thenApplyAsync(
        final Function<? super T, ? extends U> fn
    ) {
        return this.stage().thenApplyAsync(fn);
    }

    @Override
    default <U> CompletionStage<U> thenApplyAsync(
        final Function<? super T, ? extends U> fn,
        final Executor executor
    ) {
        return this.stage().thenApplyAsync(fn, executor);
    }

    @Override
    default CompletionStage<Void> thenAccept(final Consumer<? super T> action) {
        return this.stage().thenAccept(action);
    }

    @Override
    default CompletionStage<Void> thenAcceptAsync(
        final Consumer<? super T> action
    ) {
        return this.stage().thenAcceptAsync(action);
    }

    @Override
    default CompletionStage<Void> thenAcceptAsync(
        final Consumer<? super T> action,
        final Executor executor
    ) {
        return this.stage().thenAcceptAsync(action, executor);
    }

    @Override
    default CompletionStage<Void> thenRun(final Runnable action) {
        return this.stage().thenRun(action);
    }

    @Override
    default CompletionStage<Void> thenRunAsync(final Runnable action) {
        return this.stage().thenRunAsync(action);
    }

    @Override
    default CompletionStage<Void> thenRunAsync(
        final Runnable action,
        final Executor executor
    ) {
        return this.stage().thenRunAsync(action, executor);
    }

    @Override
    default <U, V> CompletionStage<V> thenCombine(
        final CompletionStage<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return this.stage().thenCombine(other, fn);
    }

    @Override
    default <U, V> CompletionStage<V> thenCombineAsync(
        final CompletionStage<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return this.stage().thenCombineAsync(other, fn);
    }

    @Override
    default <U, V> CompletionStage<V> thenCombineAsync(
        final CompletionStage<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn,
        final Executor executor
    ) {
        return this.stage().thenCombineAsync(other, fn, executor);
    }

    @Override
    default <U> CompletionStage<Void> thenAcceptBoth(
        final CompletionStage<? extends U> other,
        final BiConsumer<? super T, ? super U> action
    ) {
        return this.stage().thenAcceptBoth(other, action);
    }

    @Override
    default <U> CompletionStage<Void> thenAcceptBothAsync(
        final CompletionStage<? extends U> other,
        final BiConsumer<? super T, ? super U> action
    ) {
        return this.stage().thenAcceptBothAsync(other, action);
    }

    @Override
    default <U> CompletionStage<Void> thenAcceptBothAsync(
        final CompletionStage<? extends U> other,
        final BiConsumer<? super T, ? super U> action,
        final Executor executor
    ) {
        return this.stage().thenAcceptBothAsync(other, action);
    }

    @Override
    default CompletionStage<Void> runAfterBoth(
        final CompletionStage<?> other,
        final Runnable action
    ) {
        return this.stage().runAfterBoth(other, action);
    }

    @Override
    default CompletionStage<Void> runAfterBothAsync(
        final CompletionStage<?> other,
        final Runnable action
    ) {
        return this.stage().runAfterBoth(other, action);
    }

    @Override
    default CompletionStage<Void> runAfterBothAsync(
        final CompletionStage<?> other,
        final Runnable action,
        final Executor executor
    ) {
        return this.stage().runAfterBoth(other, action);
    }

    @Override
    default <U> CompletionStage<U> applyToEither(
        final CompletionStage<? extends T> other,
        final Function<? super T, U> fn
    ) {
        return this.stage().applyToEither(other, fn);
    }

    @Override
    default <U> CompletionStage<U> applyToEitherAsync(
        final CompletionStage<? extends T> other,
        final Function<? super T, U> fn
    ) {
        return this.stage().applyToEitherAsync(other, fn);
    }

    @Override
    default <U> CompletionStage<U> applyToEitherAsync(
        final CompletionStage<? extends T> other,
        final Function<? super T, U> fn,
        final Executor executor
    ) {
        return this.stage().applyToEitherAsync(other, fn, executor);
    }

    @Override
    default CompletionStage<Void> acceptEither(
        final CompletionStage<? extends T> other,
        final Consumer<? super T> action
    ) {
        return this.stage().acceptEither(other, action);
    }

    @Override
    default CompletionStage<Void> acceptEitherAsync(
        final CompletionStage<? extends T> other,
        final Consumer<? super T> action
    ) {
        return this.stage().acceptEitherAsync(other, action);
    }

    @Override
    default CompletionStage<Void> acceptEitherAsync(
        final CompletionStage<? extends T> other,
        final Consumer<? super T> action,
        final Executor executor
    ) {
        return this.stage().acceptEitherAsync(other, action, executor);
    }

    @Override
    default CompletionStage<Void> runAfterEither(
        final CompletionStage<?> other,
        final Runnable action
    ) {
        return this.stage().runAfterEither(other, action);
    }

    @Override
    default CompletionStage<Void> runAfterEitherAsync(
        final CompletionStage<?> other,
        final Runnable action
    ) {
        return this.stage().runAfterEitherAsync(other, action);
    }

    @Override
    default CompletionStage<Void> runAfterEitherAsync(
        final CompletionStage<?> other,
        final Runnable action,
        final Executor executor
    ) {
        return this.stage().runAfterEitherAsync(other, action, executor);
    }

    @Override
    default <U> CompletionStage<U> thenCompose(
        final Function<? super T, ? extends CompletionStage<U>> fn
    ) {
        return this.stage().thenCompose(fn);
    }

    @Override
    default <U> CompletionStage<U> thenComposeAsync(
        final Function<? super T, ? extends CompletionStage<U>> fn
    ) {
        return this.stage().thenComposeAsync(fn);
    }

    @Override
    default <U> CompletionStage<U> thenComposeAsync(
        final Function<? super T, ? extends CompletionStage<U>> fn,
        final Executor executor
    ) {
        return this.stage().thenComposeAsync(fn, executor);
    }

    @Override
    default <U> CompletionStage<U> handle(
        final BiFunction<? super T, Throwable, ? extends U> fn
    ) {
        return this.stage().handle(fn);
    }

    @Override
    default <U> CompletionStage<U> handleAsync(
        final BiFunction<? super T, Throwable, ? extends U> fn
    ) {
        return this.stage().handleAsync(fn);
    }

    @Override
    default <U> CompletionStage<U> handleAsync(
        final BiFunction<? super T, Throwable, ? extends U> fn,
        final Executor executor
    ) {
        return this.stage().handleAsync(fn, executor);
    }

    @Override
    default CompletionStage<T> whenComplete(
        final BiConsumer<? super T, ? super Throwable> action
    ) {
        return this.stage().whenComplete(action);
    }

    @Override
    default CompletionStage<T> whenCompleteAsync(
        final BiConsumer<? super T, ? super Throwable> action
    ) {
        return this.stage().whenCompleteAsync(action);
    }

    @Override
    default CompletionStage<T> whenCompleteAsync(
        final BiConsumer<? super T, ? super Throwable> action,
        final Executor executor
    ) {
        return this.stage().whenCompleteAsync(action, executor);
    }

    @Override
    default CompletionStage<T> exceptionally(
        final Function<Throwable, ? extends T> fn
    ) {
        return this.stage().exceptionally(fn);
    }

    @Override
    default CompletableFuture<T> toCompletableFuture() {
        return this.stage().toCompletableFuture();
    }
}
