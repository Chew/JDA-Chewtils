package com.jagrosh.jdautilities.commons.channelactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determiner message channel nodes.
 */
public interface MessageChannelNode extends BaseChannelNode<MessageChannel, MessageChannelNode> {

    /**
     * sends the embed messages to the channel.
     *
     * @param embeds   the embeds to send.
     * @param duration the duration to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode sendEmbeds(
        @NotNull final Collection<MessageEmbed> embeds,
        @NotNull final Duration duration
    ) {
        return this.sendEmbeds(embeds, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * sends the embed messages to the channel.
     *
     * @param embeds the embeds to send.
     * @return message node.
     */
    @NotNull
    @Contract("_ -> new")
    default MessageNode sendEmbeds(
        @NotNull final Collection<MessageEmbed> embeds
    ) {
        return this.withMessage(
            this.queue()
                .thenApply(channel -> channel.sendMessageEmbeds(embeds))
                .thenCompose(RestAction::submit)
        );
    }

    /**
     * sends the embed messages to the channel.
     *
     * @param embeds the embeds to send.
     * @param time   the time to send.
     * @param unit   the unit to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    default MessageNode sendEmbeds(
        @NotNull final Collection<MessageEmbed> embeds,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.withMessage(
            this.queue()
                .thenApply(channel -> channel.sendMessageEmbeds(embeds))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    /**
     * sends the message to the channel.
     *
     * @param msg the msg to send.
     * @return message node.
     */
    @NotNull
    @Contract("_ -> new")
    default MessageNode sendMessage(@NotNull final Message msg) {
        return this.withMessage(
            this.queue()
                .thenApply(channel -> channel.sendMessage(msg))
                .thenCompose(RestAction::submit)
        );
    }

    /**
     * sends the message to the channel.
     *
     * @param msg  the msg to send.
     * @param time the time to send.
     * @param unit the unit to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    default MessageNode sendMessage(
        @NotNull final Message msg,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.withMessage(
            this.queue()
                .thenApply(channel -> channel.sendMessage(msg))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    /**
     * sends the message to the channel.
     *
     * @param msg      the msg to send.
     * @param duration the duration to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode sendMessage(
        @NotNull final Message msg,
        @NotNull final Duration duration
    ) {
        return this.sendMessage(msg, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * sends the message to the channel.
     *
     * @param text the text to send.
     * @return message node.
     */
    @NotNull
    @Contract("_ -> new")
    default MessageNode sendMessage(@NotNull final String text) {
        return this.withMessage(
            this.queue()
                .thenApply(channel -> channel.sendMessage(text))
                .thenCompose(RestAction::submit)
        );
    }

    /**
     * sends the message to the channel.
     *
     * @param text the text to send.
     * @param time the time to send.
     * @param unit the unit to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    default MessageNode sendMessage(
        @NotNull final String text,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.withMessage(
            this.queue()
                .thenApply(channel -> channel.sendMessage(text))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

    /**
     * sends the message to the channel.
     *
     * @param text     the text to send.
     * @param duration the duration to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode sendMessage(
        @NotNull final String text,
        @NotNull final Duration duration
    ) {
        return this.sendMessage(text, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * creates a new message node with the future.
     *
     * @param future the future to create.
     * @return a newly created message node.
     */
    @NotNull
    @Contract("_ -> new")
    MessageNode withMessage(@NotNull CompletableFuture<Message> future);
}
