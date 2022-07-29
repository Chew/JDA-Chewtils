package com.jagrosh.jdautilities.commons.actions;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * an interface to determine guild nodes.
 */
public interface GuildNode extends DeleteNode<JdaNode, Guild> {

    /**
     * creates a new guild node.
     *
     * @param guild the guild to create.
     * @return a newly created guild node.
     */
    @NotNull
    @Contract("_ -> new")
    static GuildNode node(@NotNull final CompletableFuture<Guild> guild) {
        return () -> guild;
    }

    /**
     * creates a new stage node.
     *
     * @param stage the stage to create.
     * @return a newly created stage node.
     */
    @NotNull
    @Contract("_ -> new")
    static GuildNode node(@NotNull final CompletionStage<Guild> stage) {
        return GuildNode.node(stage.toCompletableFuture());
    }

    /**
     * creates a new guild node.
     *
     * @param guild the guild to create.
     * @return a newly created guild node.
     */
    @NotNull
    @Contract("_ -> new")
    static GuildNode node(@NotNull final Guild guild) {
        return GuildNode.node(CompletableFuture.completedFuture(guild));
    }

    @Override
    @NotNull
    default JdaNode delete() {
        return JdaNode.node(
            this.thenApply(Guild::delete)
                .thenCompose(action ->
                    action.submit().thenApply(unused -> action.getJDA())
                )
        );
    }

    @Override
    @NotNull
    default JdaNode delete(final long time, @NotNull final TimeUnit unit) {
        return JdaNode.node(
            this.thenApply(Guild::delete)
                .thenCompose(action ->
                    action.submitAfter(time, unit).thenApply(unused -> action.getJDA())
                )
        );
    }
}
