package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.AttachmentOption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

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

    /**
     * Returns the {@link JDA} instance of this interaction
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    public JDA getJDA()
    {
        return getEvent().getJDA();
    }

    /**
     * Responds with a String message.
     *
     * <p>The {@link ReplyCallbackAction} returned by sending the response as a {@link Message} automatically does
     * {@link ReplyCallbackAction#queue() RestAction#queue()}.
     *
     * @param message A String message to reply with
     */
    public void respond(String message)
    {
        getEvent().reply(message).queue();
    }

    /**
     * Replies with a String message.
     *
     * <p>Unlike {@link #respond(String)}, this will not queue the ReplyCallbackAction, it returns it instead.
     *
     * @param message A String message to reply with
     */
    public ReplyCallbackAction reply(String message)
    {
        return getEvent().reply(message);
    }

    /**
     * Responds with a {@link MessageEmbed}.
     *
     * <p>The {@link ReplyCallbackAction} returned by sending the response as a {@link Message} automatically does
     * {@link ReplyCallbackAction#queue() RestAction#queue()}.
     *
     * @param embed The MessageEmbed to reply with
     */
    public void respond(MessageEmbed embed)
    {
        getEvent().replyEmbeds(embed).queue();
    }

    /**
     * Replies with a {@link MessageEmbed} and returns a {@link ReplyCallbackAction}.
     *
     * <p>Unlike {@link #respond(MessageEmbed)}, this will not queue the ReplyCallbackAction, it returns it instead.
     *
     * @param embed The MessageEmbed to reply with
     * @return A {@link ReplyCallbackAction}
     */
    public ReplyCallbackAction reply(MessageEmbed embed)
    {
        return getEvent().replyEmbeds(embed);
    }

    /**
     * Replies with a {@link Message}.
     *
     * <p>The {@link ReplyCallbackAction} returned by sending the response as a {@link Message} automatically does
     * {@link ReplyCallbackAction#queue() RestAction#queue()}.
     *
     * @param message The Message to reply with
     */
    public void respond(Message message)
    {
        getEvent().reply(message).queue();
    }

    /**
     * Replies with a {@link Message} and returns a {@link ReplyCallbackAction}.
     *
     * <p>Unlike {@link #respond(Message)}, this will not queue the ReplyCallbackAction, it returns it instead.
     *
     * @param message The Message to reply with
     * @return A {@link ReplyCallbackAction}
     */
    public ReplyCallbackAction reply(Message message)
    {
        return getEvent().reply(message);
    }

    /**
     * Replies with a {@link File} with the provided name, or a default name if left null.
     *
     * <p>The {@link ReplyCallbackAction} returned by sending the response as a {@link Message} automatically does
     * {@link ReplyCallbackAction#queue() RestAction#queue()}.
     *
     * <p>This method uses {@link GenericCommandInteractionEvent#replyFile(File, String, net.dv8tion.jda.api.utils.AttachmentOption...)}
     * to send the File. For more information on what a bot may send using this, you may find the info in that method.
     *
     * @param file The File to reply with
     * @param filename The filename that Discord should display (null for default).
     * @param options The {@link AttachmentOption}s to apply to the File.
     */
    public void respond(File file, String filename, AttachmentOption... options)
    {
        getEvent().replyFile(file, filename, options).queue();
    }

    /**
     * Replies with a {@link File} and returns a {@link ReplyCallbackAction}.
     *
     * <p>Unlike {@link #respond(File, String, AttachmentOption...)}, this will not queue the ReplyCallbackAction, it returns it instead.
     *
     * @param file The File to reply with
     * @param filename The filename that Discord should display (null for default).
     * @param options The {@link AttachmentOption}s to apply to the File.
     * @return A {@link ReplyCallbackAction}
     */
    public ReplyCallbackAction reply(File file, String filename, AttachmentOption... options)
    {
        return getEvent().replyFile(file, filename, options);
    }

    /**
     * Tests whether the {@link User} who triggered this
     * event is an owner of the bot.
     *
     * @return {@code true} if the User is the Owner, else {@code false}
     */
    public boolean isOwner()
    {
        if(getUser().getId().equals(this.getClient().getOwnerId()))
            return true;
        if(this.getClient().getCoOwnerIds()==null)
            return false;
        for(String id : this.getClient().getCoOwnerIds())
            if(id.equals(getUser().getId()))
                return true;
        return false;
    }
}
