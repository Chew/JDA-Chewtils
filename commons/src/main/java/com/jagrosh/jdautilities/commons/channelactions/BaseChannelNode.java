package com.jagrosh.jdautilities.commons.channelactions;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * an interface to determine channel nodes.
 */
public interface BaseChannelNode<C extends Channel, S extends BaseChannelNode<C, S>> extends DeleteNode<S, S, C> {

    /**
     * obtains the channel.
     *
     * @return channel.
     */
    @NotNull
    C channel();

    @NotNull
    @Override
    default S delete() {
        return this.with(
            this.queue()
                .thenApply(Channel::delete)
                .thenCompose(RestAction::submit)
                .thenApply(unused -> this.channel())
        );
    }

    @NotNull
    @Override
    default S delete(final long time, @NotNull final TimeUnit unit) {
        return this.with(
            this.queue()
                .thenApply(Channel::delete)
                .thenCompose(action -> action.submitAfter(time, unit))
                .thenApply(unused -> this.channel())
        );
    }
}
