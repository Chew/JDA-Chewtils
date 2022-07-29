package com.jagrosh.jdautilities.commons.actions;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * an interface to determine jda nodes.
 */
public interface JdaNode extends Node<JDA> {

    /**
     * creates a new jda node.
     *
     * @param jda the jda to create.
     * @return a newly created jda node.
     */
    @NotNull
    @Contract("_ -> new")
    static JdaNode node(@NotNull final CompletableFuture<JDA> jda) {
        return () -> jda;
    }

    /**
     * creates a new stage node.
     *
     * @param stage the stage to create.
     * @return a newly created stage node.
     */
    @NotNull
    @Contract("_ -> new")
    static JdaNode node(@NotNull final CompletionStage<JDA> stage) {
        return JdaNode.node(stage.toCompletableFuture());
    }

    /**
     * creates a new jda node.
     *
     * @param jda the jda to create.
     * @return a newly created jda node.
     */
    @NotNull
    @Contract("_ -> new")
    static JdaNode node(@NotNull final JDA jda) {
        return JdaNode.node(CompletableFuture.completedFuture(jda));
    }
}
