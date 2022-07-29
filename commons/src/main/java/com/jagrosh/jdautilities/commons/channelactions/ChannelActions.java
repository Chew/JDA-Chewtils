package com.jagrosh.jdautilities.commons.channelactions;

import com.jagrosh.jdautilities.commons.channelactions.impl.ChannelNodeImpl;
import com.jagrosh.jdautilities.commons.channelactions.impl.MessageChannelNodeImpl;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * an interface that contains utility methods for channel actions.
 * <pre>
 * {@code
 * ChannelActions
 *   .node(messageChannel)
 *   .sendMessage("Guns are ready!", 1L, TimeUnit.SECONDS)
 *   .delete(3L, TimeUnit.SECONDS)
 *   .sendMessage("3")
 *   .editMessage("2", 1L, TimeUnit.SECONDS)
 *   .editMessage("1", 1L, TimeUnit.SECONDS)
 *   .editMessage("OMG WHAT'S HAPPENING!!!", 1L, TimeUnit.SECONDS)
 *   .delete(3L, TimeUnit.SECONDS)
 *   .sendMessage("Player XXX won the fight!")
 *   .delete(Duration.ofSeconds(3L))
 *   .thenAccept(channel -> {
 *   });
 * }</pre>
 */
public interface ChannelActions {

    /**
     * creates a new channel node.
     *
     * @param channel the channel to create.
     * @return a newly created channel node.
     */
    @NotNull
    @Contract("_ -> new")
    static ChannelNode node(@NotNull final Channel channel) {
        return new ChannelNodeImpl(channel);
    }

    /**
     * creates a new message channel node.
     *
     * @param channel the channel to create.
     * @return a newly created message channel node.
     */
    @NotNull
    @Contract("_ -> new")
    static MessageChannelNode node(@NotNull final MessageChannel channel) {
        return new MessageChannelNodeImpl(channel);
    }
}
