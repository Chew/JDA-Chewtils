package com.jagrosh.jdautilities.commons.channelactions.impl;

import com.jagrosh.jdautilities.commons.channelactions.MessageChannelNode;
import com.jagrosh.jdautilities.commons.channelactions.MessageNode;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class MessageChannelNodeImpl implements MessageChannelNode {

    @NotNull
    private final MessageChannel channel;

    @NotNull
    private final CompletableFuture<MessageChannel> queue;

    public MessageChannelNodeImpl(@NotNull final MessageChannel channel, @NotNull final CompletableFuture<MessageChannel> queue) {
        this.channel = channel;
        this.queue = queue;
    }

    public MessageChannelNodeImpl(@NotNull final MessageChannel channel) {
        this(channel, CompletableFuture.completedFuture(channel));
    }

    @NotNull
    @Override
    public MessageChannelNode with(
        @NotNull final CompletableFuture<MessageChannel> future
    ) {
        return new MessageChannelNodeImpl(
            this.channel,
            future.thenApply(o -> this.channel)
        );
    }

    @NotNull
    @Override
    public MessageNode withMessage(
        @NotNull final CompletableFuture<Message> future
    ) {
        return new MessageNodeImpl(this, future);
    }

    @NotNull
    @Override
    public MessageChannel channel() {
        return this.channel;
    }

    @NotNull
    @Override
    public CompletableFuture<MessageChannel> queue() {
        return this.queue;
    }
}
