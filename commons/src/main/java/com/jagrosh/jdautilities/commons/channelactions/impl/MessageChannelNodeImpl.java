package com.jagrosh.jdautilities.commons.channelactions.impl;

import com.jagrosh.jdautilities.commons.channelactions.MessageChannelNode;
import com.jagrosh.jdautilities.commons.channelactions.MessageNode;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class MessageChannelNodeImpl extends ChannelNodeImpl implements MessageChannelNode {

    public MessageChannelNodeImpl(
        @NotNull final MessageChannel channel,
        @NotNull final CompletableFuture<MessageChannel> queue
    ) {
        super(channel, queue);
    }

    public MessageChannelNodeImpl(@NotNull final MessageChannel channel) {
        this(channel, CompletableFuture.completedFuture(channel));
    }

    @NotNull
    @Contract("_ -> param1")
    private static MessageChannel messageChannel(@NotNull final Channel channel) {
        if (!(channel instanceof MessageChannel messageChannel)) {
            throw new IllegalStateException("This is not a message channel!");
        }
        return messageChannel;
    }

    @Override
    @NotNull
    @Contract("_ -> new")
    public MessageNode send(@NotNull final String message) {
        return new MessageNodeImpl(
            this,
            this.queue.thenApply(MessageChannelNodeImpl::messageChannel)
                .thenApply(channel -> channel.sendMessage(message))
                .thenCompose(RestAction::submit)
        );
    }

    @Override
    @NotNull
    @Contract("_, _, _ -> new")
    public MessageNode send(
        @NotNull final String message,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return new MessageNodeImpl(
            this,
            this.queue.thenApply(MessageChannelNodeImpl::messageChannel)
                .thenApply(channel -> channel.sendMessage(message))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    @NotNull
    @Override
    @Contract("_ -> new")
    public MessageChannelNode with(@NotNull final CompletableFuture<?> future) {
        final var channel = MessageChannelNodeImpl.messageChannel(this.channel);
        return new MessageChannelNodeImpl(channel, future.thenApply(o -> channel));
    }
}
