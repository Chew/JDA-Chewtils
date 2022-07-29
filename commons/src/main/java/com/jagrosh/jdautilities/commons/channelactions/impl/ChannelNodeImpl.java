package com.jagrosh.jdautilities.commons.channelactions.impl;

import com.jagrosh.jdautilities.commons.channelactions.ChannelNode;
import net.dv8tion.jda.api.entities.Channel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class ChannelNodeImpl implements ChannelNode {

    @NotNull
    private final Channel channel;

    @NotNull
    private final CompletableFuture<Channel> queue;

    public ChannelNodeImpl(@NotNull final Channel channel, @NotNull final CompletableFuture<Channel> queue) {
        this.channel = channel;
        this.queue = queue;
    }

    public ChannelNodeImpl(@NotNull final Channel channel) {
        this(channel, CompletableFuture.completedFuture(channel));
    }

    @NotNull
    @Override
    public ChannelNodeImpl with(
        @NotNull final CompletableFuture<Channel> future
    ) {
        return new ChannelNodeImpl(this.channel(), future);
    }

    @NotNull
    @Override
    public Channel channel() {
        return this.channel;
    }

    @NotNull
    @Override
    public CompletableFuture<Channel> queue() {
        return this.queue;
    }
}
