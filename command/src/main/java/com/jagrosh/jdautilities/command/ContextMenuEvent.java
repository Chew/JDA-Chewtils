package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A wrapper class for both {@link UserContextInteractionEvent UserContextInteractionEvent} and
 * {@link MessageContextInteractionEvent MessageContextInteractionEvent}. This class is used to provide a common
 * interface for both types of events.
 *
 * <p>From here, developers can invoke several useful and specialized methods to assist in Context Menu function and
 * development. There are also "extension" methods for all methods found in the generic interaction events.
 *
 * @author Olivia (Chew)
 */
public class ContextMenuEvent
{
    private final UserContextInteractionEvent uEvent;
    private final MessageContextInteractionEvent mEvent;
    private final CommandClient client;

    /**
     * Constructs this event as a UserContextInteractionEvent.
     * This means you should use any User related methods, or you may run into IllegalStateExceptions.
     *
     * @param event The UserContextInteractionEvent to construct this event from
     * @param client The CommandClient that this event is from
     */
    public ContextMenuEvent(UserContextInteractionEvent event, CommandClient client)
    {
        uEvent = event;
        mEvent = null;
        this.client = client;
    }

    /**
     * Constructs this event as a MessageContextInteractionEvent.
     * This means you should use any Message related methods, or you may run into IllegalStateExceptions.
     *
     * @param event The MessageContextInteractionEvent to construct this event from
     * @param client The CommandClient that this event is from
     */
    public ContextMenuEvent(MessageContextInteractionEvent event, CommandClient client)
    {
        mEvent = event;
        uEvent = null;
        this.client = client;
    }

    /**
     * Gets specifically the {@link UserContextInteractionEvent UserContextInteractionEvent} from this event.
     *
     * @return The UserContextInteractionEvent from this event
     * @throws IllegalStateException If this is a {@link MessageContextInteractionEvent MessageContextInteractionEvent}
     */
    public UserContextInteractionEvent getUserEvent()
    {
        if (uEvent == null)
            throw new IllegalStateException("This is not a User context menu event!");

        return uEvent;
    }

    /**
     * Gets specifically the {@link MessageContextInteractionEvent MessageContextInteractionEvent} from this event.
     *
     * @return The MessageContextInteractionEvent from this event
     * @throws IllegalStateException If this is a {@link UserContextInteractionEvent UserContextInteractionEvent}
     */
    public MessageContextInteractionEvent getMessageEvent()
    {
        if (mEvent == null)
            throw new IllegalStateException("This is not a Message context menu event!");

        return mEvent;
    }

    /**
     * Gets the {@link GenericCommandInteractionEvent GenericCommandInteractionEvent} from this event.
     * Both {@link UserContextInteractionEvent UserContextInteractionEvent} and
     * {@link MessageContextInteractionEvent MessageContextInteractionEvent} extend this class, so this never returns null.
     * It may be convenient to use this method to get the correct type of event.
     *
     * @return The GenericCommandInteractionEvent from this event
     */
    public GenericCommandInteractionEvent getEvent() {
        if (uEvent != null)
            return uEvent;
        else
            return mEvent;
    }

    /**
     * Returns the {@link com.jagrosh.jdautilities.command.CommandClient CommandClient}
     * that initiated this CommandEvent.
     *
     * @return The initiating CommandClient
     */
    public CommandClient getClient()
    {
        return client;
    }

    /**
     * Checks to see if this is a {@link UserContextInteractionEvent UserContextInteractionEvent}.
     * @return True if this is a UserContextInteractionEvent, false otherwise
     */
    public boolean isUserEvent()
    {
        return uEvent != null;
    }

    /**
     * Checks to see if this is a {@link MessageContextInteractionEvent MessageContextInteractionEvent}.
     * @return True if this is a MessageContextInteractionEvent, false otherwise
     */
    public boolean isMessageEvent()
    {
        return mEvent != null;
    }

    /**
     * Assuming this is a {@link UserContextInteractionEvent UserContextInteractionEvent}, you will get the target user.
     * @return The target user
     * @see UserContextInteractionEvent#getTarget()
     * @throws IllegalStateException If this is not a UserContextInteractionEvent
     */
    public User getTargetUser()
    {
        if (uEvent == null)
            throw new IllegalStateException("This event is not from a user!");

        return uEvent.getTarget();
    }

    /**
     * Assuming this is a {@link UserContextInteractionEvent UserContextInteractionEvent}, you will get the target member.
     * @return The target member
     * @see UserContextInteractionEvent#getTargetMember()
     * @throws IllegalStateException If this is not a UserContextInteractionEvent
     */
    public Member getTargetMember()
    {
        if (uEvent == null)
            throw new IllegalStateException("This event is not from a user!");

        return uEvent.getTargetMember();
    }

    /**
     * If this is a {@link UserContextInteractionEvent UserContextInteractionEvent},
     * you will get the message this interaction came from.
     * If this is a {@link MessageContextInteractionEvent MessageContextInteractionEvent},
     * you will get the channel the message that caused this interaction was sent from.
     * <br>Because both events have a method, this method will never return null.
     *
     * @return The channel the message that caused this interaction was sent from
     */
    public MessageChannel getChannel()
    {
        if (mEvent == null && uEvent != null)
            return uEvent.getMessageChannel();
        else if (mEvent != null && uEvent == null)
            return mEvent.getChannel();


        return getEvent().getMessageChannel();
    }

    /**
     * Assuming this is a {@link MessageContextInteractionEvent MessageContextInteractionEvent},
     * you will get the message this interaction is referring to.
     * @return The message this interaction is referring to
     * @throws IllegalStateException If this is not a MessageContextInteractionEvent
     */
    public Message getTargetMessage()
    {
        if (mEvent == null)
            throw new IllegalStateException("This event is not from a message!");

        return mEvent.getTarget();
    }

    /**
     * The {@link User} who caused this interaction.
     *
     * @return The {@link User}
     */
    @Nonnull
    public User getUser()
    {
        return getEvent().getUser();
    }

    /**
     * The {@link Member} who caused this interaction.
     * <br>This is null if the interaction is not from a guild.
     *
     * @return The {@link Member}
     */
    @Nullable
    public Member getMember()
    {
        return getEvent().getMember();
    }

    /**
     * The {@link net.dv8tion.jda.api.entities.ChannelType ChannelType} for this channel
     *
     * @return The channel type
     */
    @Nonnull
    public ChannelType getChannelType()
    {
        return getChannel().getType();
    }

    /**
     * The {@link Guild} this interaction happened in.
     * <br>This is null in direct messages.
     *
     * @return The {@link Guild} or null
     */
    @Nullable
    public Guild getGuild()
    {
        return getEvent().getGuild();
    }
}
