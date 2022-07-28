package com.jagrosh.jdautilities.commons.channelactions.impl;

import com.jagrosh.jdautilities.commons.channelactions.ChannelNode;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ChannelNodeImpl implements ChannelNode {

    @NotNull
    protected final Channel channel;

    @NotNull
    protected final CompletableFuture<? extends Channel> queue;

    public ChannelNodeImpl(@NotNull Channel channel, @NotNull CompletableFuture<? extends Channel> queue) {
        this.channel = channel;
        this.queue = queue;
    }

    public ChannelNodeImpl(@NotNull final Channel channel) {
        this(channel, CompletableFuture.completedFuture(channel));
    }

    @Override
    public final void delete() {
        this.with(
            this.queue.thenApply(Channel::delete).thenCompose(RestAction::submit)
        );
    }

    @Override
    public final void delete(final long time, @NotNull final TimeUnit unit) {
        this.with(
            this.queue.thenApply(Channel::delete)
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    @NotNull
    @Contract("_ -> new")
    public ChannelNode with(@NotNull final CompletableFuture<?> future) {
        return new ChannelNodeImpl(
            this.channel,
            future.thenApply(o -> this.channel)
        );
    }
}
