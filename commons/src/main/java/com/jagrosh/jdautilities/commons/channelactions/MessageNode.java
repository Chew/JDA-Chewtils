package com.jagrosh.jdautilities.commons.channelactions;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine message nodes.
 */
public interface MessageNode {

    /**
     * deletes the message.
     *
     * @return message channel node.
     */
    @NotNull
    @Contract("-> new")
    MessageChannelNode delete();

    /**
     * deletes the message.
     *
     * @param time the time to delete.
     * @param unit the unit to delete.
     * @return message channel node.
     */
    @NotNull
    @Contract("_, _ -> new")
    MessageChannelNode delete(long time, @NotNull TimeUnit unit);

    /**
     * deletes the message.
     *
     * @param duration the duration to delete.
     * @return message channel node.
     */
    @NotNull
    @Contract("_ -> new")
    default MessageChannelNode delete(@NotNull final Duration duration) {
        return this.delete(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_ -> new")
    MessageNode editMessage(@NotNull Message newContent);

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @param time       the time to edit.
     * @param unit       the unit to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    MessageNode editMessage(
        @NotNull Message newContent,
        long time,
        @NotNull TimeUnit unit
    );

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @param duration   the duration to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode editMessage(
        @NotNull final Message newContent,
        @NotNull final Duration duration
    ) {
        return this.editMessage(newContent, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_ -> new")
    MessageNode editMessage(@NotNull String newContent);

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @param time       the time to edit.
     * @param unit       the unit to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_, _, _ -> new")
    MessageNode editMessage(
        @NotNull String newContent,
        long time,
        @NotNull TimeUnit unit
    );

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @param duration   the duration to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_, _ -> new")
    default MessageNode editMessage(
        @NotNull final String newContent,
        @NotNull final Duration duration
    ) {
        return this.editMessage(newContent, duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
