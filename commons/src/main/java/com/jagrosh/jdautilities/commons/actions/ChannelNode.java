package com.jagrosh.jdautilities.commons.actions;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine base channel nodes.
 */
public interface ChannelNode extends DeleteNode<JdaNode, Channel> {

    /**
     * creates a new channel node.
     *
     * @param channel the channel to create.
     * @return a newly created channel node.
     */
    @NotNull
    @Contract("_ -> new")
    static ChannelNode node(@NotNull final CompletableFuture<Channel> channel) {
        return () -> channel;
    }

    /**
     * creates a new channel node.
     *
     * @param channel the channel to create.
     * @return a newly created channel node.
     */
    @NotNull
    @Contract("_ -> new")
    static ChannelNode node(@NotNull final CompletionStage<Channel> channel) {
        return ChannelNode.node(channel.toCompletableFuture());
    }

    /**
     * creates a new channel node.
     *
     * @param channel the channel to create.
     * @return a newly created channel node.
     */
    @NotNull
    @Contract("_ -> new")
    static ChannelNode node(@NotNull final Channel channel) {
        return ChannelNode.node(CompletableFuture.completedFuture(channel));
    }

    @NotNull
    @Override
    default JdaNode delete() {
        return JdaNode.node(
            this.thenCompose(channel -> {
                final var jda = channel.getJDA();
                return channel.delete().submit().thenApply(unused -> jda);
            })
        );
    }

    @NotNull
    @Override
    default JdaNode delete(final long time, @NotNull final TimeUnit unit) {
        return JdaNode.node(
            this.thenCompose(channel -> {
                final var jda = channel.getJDA();
                return channel
                    .delete()
                    .submitAfter(time, unit)
                    .thenApply(unused -> jda);
            })
        );
    }

    /**
     * deletes the channel as a guild.
     */
    @NotNull
    default GuildNode deleteAsGuild() {
        return GuildNode.node(
            this.thenApply(GuildChannel.class::cast)
                .thenCompose(channel -> {
                    final var guild = channel.getGuild();
                    return channel.delete().submit().thenApply(unused -> guild);
                })
        );
    }

    /**
     * deletes the channel as a guild.
     *
     * @param time the time to delete.
     * @param unit the unit to delete.
     */
    @NotNull
    default GuildNode deleteAsGuild(
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return GuildNode.node(
            this.thenApply(GuildChannel.class::cast)
                .thenCompose(channel -> {
                    final var guild = channel.getGuild();
                    return channel
                        .delete()
                        .submitAfter(time, unit)
                        .thenApply(unused -> guild);
                })
        );
    }

    /**
     * deletes the channel as a guild.
     *
     * @param duration the duration to delete.
     */
    @NotNull
    @Contract("_ -> new")
    default GuildNode deleteAsGuild(@NotNull final Duration duration) {
        return this.deleteAsGuild(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

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
        return MessageNode.node(
            this.thenApply(MessageChannel.class::cast)
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
        return MessageNode.node(
            this.thenApply(MessageChannel.class::cast)
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
        return MessageNode.node(
            this.thenApply(MessageChannel.class::cast)
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
        return MessageNode.node(
            this.thenApply(MessageChannel.class::cast)
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
     * @param args the args to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode sendMessage(
        @NotNull final String text,
        @NotNull final Object... args
    ) {
        return MessageNode.node(
            this.thenApply(MessageChannel.class::cast)
                .thenApply(channel ->
                    channel.sendMessage(MessageFormat.format(text, args))
                )
                .thenCompose(RestAction::submit)
        );
    }

    /**
     * sends the message to the channel.
     *
     * @param time the time to send.
     * @param unit the unit to send.
     * @param text the text to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _, _, _ -> new")
    default MessageNode sendMessage(
        final long time,
        @NotNull final TimeUnit unit,
        @NotNull final String text,
        @NotNull final Object... args
    ) {
        return MessageNode.node(
            this.thenApply(MessageChannel.class::cast)
                .thenApply(channel ->
                    channel.sendMessage(MessageFormat.format(text, args))
                )
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
    @Contract("_, _, _ -> new")
    default MessageNode sendMessage(
        @NotNull final Duration duration,
        @NotNull final String text,
        @NotNull final Object... args
    ) {
        return this.sendMessage(
            duration.toMillis(),
            TimeUnit.MILLISECONDS,
            text,
            args
        );
    }
}
