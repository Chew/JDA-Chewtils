package com.jagrosh.jdautilities.commons.channelactions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determiner message channel nodes.
 */
public interface MessageChannelNode extends ChannelNode {

    /**
     * sends the message to the channel.
     *
     * @param message the message to send.
     * @return message node.
     */
    @NotNull
    @Contract("_ -> new")
    MessageNode send(@NotNull String message);

    /**
     * sends the message to the channel.
     *
     * @param message the message to send.
     * @param time    the time to send.
     * @param unit    the unit to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    MessageNode send(@NotNull String message, long time, @NotNull TimeUnit unit);

    /**
     * sends the message to the channel.
     *
     * @param message  the message to send.
     * @param duration the duration to send.
     * @return message node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode send(@NotNull final String message, @NotNull final Duration duration) {
        return this.send(message, duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
