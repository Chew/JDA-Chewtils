package com.jagrosh.jdautilities.commons.channelactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine message nodes.
 */
public interface MessageNode extends DeleteNode<MessageChannelNode, MessageNode, Message> {

    @NotNull
    @Override
    default MessageChannelNode delete() {
        return this.parent()
            .with(
                this.queue()
                    .thenApply(Message::delete)
                    .thenCompose(RestAction::submit)
                    .thenApply(unused -> this.parent().channel())
            );
    }

    @NotNull
    @Override
    default MessageChannelNode delete(
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.parent()
            .with(
                this.queue()
                    .thenApply(Message::delete)
                    .thenCompose(action -> action.submitAfter(time, unit))
                    .thenApply(unused -> this.parent().channel())
            );
    }

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_ -> new")
    default MessageNode editMessage(@NotNull final Message newContent) {
        return this.with(
            this.queue()
                .thenApply(message -> message.editMessage(newContent))
                .thenCompose(RestAction::submit)
        );
    }

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
    default MessageNode editMessage(
        @NotNull final Message newContent,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.with(
            this.queue()
                .thenApply(message -> message.editMessage(newContent))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

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
        return this.editMessage(
            newContent,
            duration.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * edits the message.
     *
     * @param newContent the new content to edit.
     * @return message channel node.
     */
    @NotNull
    @Contract("_ -> new")
    default MessageNode editMessage(@NotNull final String newContent) {
        return this.with(
            this.queue()
                .thenApply(message -> message.editMessage(newContent))
                .thenCompose(RestAction::submit)
        );
    }

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
    default MessageNode editMessage(
        @NotNull final String newContent,
        final long time,
        @NotNull final TimeUnit unit
    ) {
        return this.with(
            this.queue()
                .thenApply(message -> message.editMessage(newContent))
                .thenCompose(action -> action.submitAfter(time, unit))
        );
    }

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
        return this.editMessage(
            newContent,
            duration.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * obtains the parent.
     *
     * @return parent.
     */
    @NotNull
    MessageChannelNode parent();
}
