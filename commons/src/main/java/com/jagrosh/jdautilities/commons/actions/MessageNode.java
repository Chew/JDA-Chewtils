package com.jagrosh.jdautilities.commons.actions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine message nodes.
 */
public interface MessageNode extends DeleteNode<ChannelNode, Message> {

    /**
     * creates a new message node.
     *
     * @param message the message to create.
     * @return a newly created message node.
     */
    @NotNull
    @Contract("_ -> new")
    static MessageNode node(@NotNull final CompletableFuture<Message> message) {
        return () -> message;
    }

    /**
     * creates a new message node.
     *
     * @param stage the stage to create.
     * @return a newly created message node.
     */
    @NotNull
    @Contract("_ -> new")
    static MessageNode node(@NotNull final CompletionStage<Message> stage) {
        return MessageNode.node(stage.toCompletableFuture());
    }

    /**
     * creates a new message node.
     *
     * @param message the message to create.
     * @return a newly created message node.
     */
    @NotNull
    @Contract("_ -> new")
    static MessageNode node(@NotNull final Message message) {
        return MessageNode.node(CompletableFuture.completedFuture(message));
    }

    @NotNull
    @Override
    default ChannelNode delete() {
        return ChannelNode.node(
            this.thenCompose(message ->
                message.delete().submit().thenApply(unused -> message.getChannel())
            )
        );
    }

    @NotNull
    @Override
    default ChannelNode delete(final long time, @NotNull final TimeUnit unit) {
        return ChannelNode.node(
            this.thenCompose(message ->
                message
                    .delete()
                    .submitAfter(time, unit)
                    .thenApply(unused -> message.getChannel())
            )
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
        return MessageNode.node(
            this.thenApply(message -> message.editMessage(newContent))
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
        return MessageNode.node(
            this.thenApply(message -> message.editMessage(newContent))
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
        return MessageNode.node(
            this.thenApply(message -> message.editMessage(newContent))
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
        return MessageNode.node(
            this.thenApply(message -> message.editMessage(newContent))
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
}
