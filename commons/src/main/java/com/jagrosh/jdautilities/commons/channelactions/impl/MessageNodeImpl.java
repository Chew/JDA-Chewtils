package com.jagrosh.jdautilities.commons.channelactions.impl;

import com.jagrosh.jdautilities.commons.channelactions.MessageChannelNode;
import com.jagrosh.jdautilities.commons.channelactions.MessageNode;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class MessageNodeImpl implements MessageNode {

    @NotNull
    private final MessageChannelNodeImpl parent;

    @NotNull
    private final CompletableFuture<Message> queue;

    public MessageNodeImpl(@NotNull MessageChannelNodeImpl parent, @NotNull CompletableFuture<Message> queue) {
        this.parent = parent;
        this.queue = queue;
    }

    @Override
    @NotNull
    @Contract("-> new")
    public MessageChannelNode delete() {
        return this.parent.with(
            this.queue.thenApply(Message::delete).thenCompose(RestAction::submit)
        );
    }

    @Override
    @NotNull
    @Contract("_, _ -> new")
    public MessageChannelNode delete(
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.parent.with(
            this.queue.thenApply(Message::delete)
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    @Override
    @NotNull
    @Contract("_ -> new")
    public MessageNode editMessage(@NotNull final Message newContent) {
        return this.with(
            this.queue.thenApply(message -> message.editMessage(newContent))
                .thenCompose(RestAction::submit)
        );
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public MessageNode editMessage(
        @NotNull final Message newContent,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.with(
            this.queue.thenApply(message -> message.editMessage(newContent))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    @Override
    @NotNull
    @Contract("_ -> new")
    public MessageNode editMessage(@NotNull final String newContent) {
        return this.with(
            this.queue.thenApply(message -> message.editMessage(newContent))
                .thenCompose(RestAction::submit)
        );
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public MessageNode editMessage(
        @NotNull final String newContent,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.with(
            this.queue.thenApply(message -> message.editMessage(newContent))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    @NotNull
    @Contract("_ -> new")
    private MessageNode with(@NotNull final CompletableFuture<Message> future) {
        return new MessageNodeImpl(this.parent, future);
    }
}
