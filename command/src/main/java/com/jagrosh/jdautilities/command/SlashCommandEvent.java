package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper class for a {@link SlashCommandInteractionEvent SlashCommandInteractionEvent} and
 * {@link com.jagrosh.jdautilities.command.CommandClient CommandClient}.
 *
 * <p>From here, developers can invoke several useful and specialized methods to assist in Command function and
 * development. Because this extends SlashCommandInteractionEvent, all methods from it work fine.
 *
 * @author Olivia (Chew)
 */
public class SlashCommandEvent extends SlashCommandInteractionEvent {
    private CommandClient client = null;

    /**
     * Required by law.
     */
    private SlashCommandEvent(@NotNull JDA api, long responseNumber, @NotNull SlashCommandInteraction interaction)
    {
        super(api, responseNumber, interaction);
    }

    public SlashCommandEvent(SlashCommandInteractionEvent event, CommandClient client)
    {
        super(event.getJDA(), event.getResponseNumber(), event);
        this.client = client;
    }

    /**
     * The {@link com.jagrosh.jdautilities.command.CommandClient CommandClient} that this event was triggered from.
     *
     * @return The CommandClient that this event was triggered from
     */
    public CommandClient getClient()
    {
        return client;
    }
}
