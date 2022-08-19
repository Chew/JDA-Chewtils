/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * <h2><b>Message Context Menus In JDA-Chewtils</b></h2>
 *
 * <p>The internal inheritance for Message Context Menus used in JDA-Chewtils is that of the object.
 *
 * <p>Classes created inheriting this class gain the unique traits of commands operated using the menu Extension.
 * <br>Using several fields, a command can define properties that make it unique and complex while maintaining
 * a low level of development.
 * <br>All classes extending this class can define any number of these fields in a object constructor and then
 * create the menu action/response in the abstract {@link MessageContextMenu#execute(MessageContextMenuEvent)} body:
 *
 * <pre><code> public class ExampleCmd extends MessageContextMenu {
 *
 *      public ExampleCmd() {
 *          this.name = "Example";
 *      }
 *
 *      {@literal @Override}
 *      protected void execute(MessageContextMenuEvent event) {
 *          event.reply("Hey look! This would be the bot's reply if this was a command!");
 *      }
 *
 * }</code></pre>
 *
 * Execution is with the provision of a MessageContextInteractionEvent-CommandClient wrapper called a
 * {@link MessageContextMenu} and is performed in two steps:
 * <ul>
 *     <li>{@link MessageContextMenu#run(MessageContextMenuEvent) run} - The menu runs
 *     through a series of conditionals, automatically terminating the command instance if one is not met,
 *     and possibly providing an error response.</li>
 *
 *     <li>{@link MessageContextMenu#execute(MessageContextMenuEvent) execute} - The menu,
 *     now being cleared to run, executes and performs whatever lies in the abstract body method.</li>
 * </ul>
 *
 * @author Olivia (Chew)
 */
public class MessageContextMenuEvent extends MessageContextInteractionEvent
{
    private final CommandClient client;

    public MessageContextMenuEvent(@NotNull JDA api, long responseNumber, @NotNull MessageContextInteraction interaction, CommandClient client)
    {
        super(api, responseNumber, interaction);
        this.client = client;
    }

    /**
     * Returns the {@link CommandClient} that initiated this event.
     *
     * @return The initiating CommandClient
     */
    public CommandClient getClient()
    {
        return client;
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
        reply(message).queue();
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
        replyEmbeds(embed).queue();
    }

    /**
     * Replies with a {@link Message}.
     *
     * <p>The {@link ReplyCallbackAction} returned by sending the response as a {@link Message} automatically does
     * {@link ReplyCallbackAction#queue() RestAction#queue()}.
     *
     * @param message The Message to reply with
     */
    public void respond(MessageCreateData message)
    {
        reply(message).queue();
    }

    /**
     * Replies with a {@link File} with the provided name, or a default name if left null.
     *
     * <p>The {@link ReplyCallbackAction} returned by sending the response as a {@link Message} automatically does
     * {@link ReplyCallbackAction#queue() RestAction#queue()}.
     *
     * <p>This method uses {@link GenericCommandInteractionEvent#replyFiles(net.dv8tion.jda.api.utils.FileUpload...)}
     * to send the File. For more information on what a bot may send using this, you may find the info in that method.
     *
     * @param file The File to reply with
     * @param filename The filename that Discord should display (null for default).
     * @param description The description to set (null for no description).
     * @param spoiler whether the file should be marked as spoiler.
     */
    public void respond(File file, String filename, String description, boolean spoiler)
    {
        FileUpload fileUpload = FileUpload.fromData(file, filename);
        if(description != null && !description.isEmpty())
            fileUpload.setDescription(description);
        
        if(spoiler)
            fileUpload.asSpoiler();
        
        replyFiles(fileUpload).queue();
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
