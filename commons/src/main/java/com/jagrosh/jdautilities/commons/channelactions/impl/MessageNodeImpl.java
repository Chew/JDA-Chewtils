package com.jagrosh.jdautilities.commons.channelactions.impl;

import com.jagrosh.jdautilities.commons.channelactions.MessageChannelNode;
import com.jagrosh.jdautilities.commons.channelactions.MessageNode;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class MessageNodeImpl implements MessageNode {

    @NotNull
    private final MessageChannelNode parent;

    @NotNull
    private final CompletableFuture<Message> queue;

    public MessageNodeImpl(@NotNull final MessageChannelNode parent, @NotNull final CompletableFuture<Message> queue) {
        this.parent = parent;
        this.queue = queue;
    }

    @NotNull
    @Override
    public MessageNode with(@NotNull final CompletableFuture<Message> future) {
        return new MessageNodeImpl(this.parent, future);
    }

    @NotNull
    @Override
    public MessageChannelNode parent() {
        return this.parent;
    }

    @NotNull
    @Override
    public CompletableFuture<Message> queue() {
        return this.queue;
    }
}
