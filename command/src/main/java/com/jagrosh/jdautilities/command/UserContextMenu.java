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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.HashSet;
import java.util.Set;

/**
 * <h2><b>User Context Menus In JDA-Chewtils</b></h2>
 *
 * <p>The internal inheritance for User Context Menus used in JDA-Chewtils is that of the object.
 *
 * <p>Classes created inheriting this class gain the unique traits of commands operated using the menu Extension.
 * <br>Using several fields, a menu can define properties that make it unique and complex while maintaining
 * a low level of development.
 * <br>All classes extending this class can define any number of these fields in a object constructor and then
 * create the menu action/response in the abstract {@link UserContextMenu#execute(UserContextMenuEvent)} body:
 *
 * <pre><code> public class ExampleCmd extends UserContextMenu {
 *
 *      public ExampleCmd() {
 *          this.name = "Example";
 *      }
 *
 *      {@literal @Override}
 *      protected void execute(UserContextMenu event) {
 *          event.reply("Hey look! This would be the bot's reply if this was a command!");
 *      }
 *
 * }</code></pre>
 *
 * Execution is with the provision of a UserContextInteractionEvent-CommandClient wrapper called a
 * {@link UserContextMenuEvent} and is performed in two steps:
 * <ul>
 *     <li>{@link UserContextMenu#run(UserContextMenuEvent) run} - The menu runs
 *     through a series of conditionals, automatically terminating the command instance if one is not met,
 *     and possibly providing an error response.</li>
 *
 *     <li>{@link UserContextMenu#execute(UserContextMenuEvent) execute} - The menu,
 *     now being cleared to run, executes and performs whatever lies in the abstract body method.</li>
 * </ul>
 *
 * @author Olivia (Chew)
 */
public abstract class UserContextMenu extends ContextMenu
{
    /**
     * Runs checks for the {@link UserContextMenu} with the given {@link MessageContextMenuEvent} that called it.
     * <br>Will terminate, and possibly respond with a failure message, if any checks fail.
     *
     * @param  event
     *         The UserContextMenuEvent that triggered this Context Menu
     */
    public final void run(UserContextMenuEvent event)
    {
        // owner check
        if(ownerCommand && !(event.isOwner()))
        {
            terminate(event,null);
            return;
        }

        // cooldown check, ignoring owner
        if(cooldown>0 && !(event.isOwner()))
        {
            String key = getCooldownKey(event);
            int remaining = event.getClient().getRemainingCooldown(key);
            if(remaining>0)
            {
                terminate(event, getCooldownError(event, remaining, event.getClient()));
                return;
            }
            else event.getClient().applyCooldown(key, cooldown);
        }

        // availability check
        if(event.isFromGuild())
        {
            //user perms
            if (forceUserPermissions)
            {
                for(Permission p: userPermissions)
                {
                    // Member will never be null because this is only ran in a server
                    if(event.getMember() == null)
                        continue;

                    if(p.isChannel())
                    {
                        if(!event.getMember().hasPermission(event.getGuildChannel(), p))
                        {
                            terminate(event, String.format(userMissingPermMessage, event.getClient().getError(), p.getName(), "channel"));
                            return;
                        }
                    }
                    else
                    {
                        if(!event.getMember().hasPermission(p))
                        {
                            terminate(event, String.format(userMissingPermMessage, event.getClient().getError(), p.getName(), "server"));
                            return;
                        }
                    }
                }
            }

            // bot perms
            for(Permission p: botPermissions)
            {
                // We can ignore this permission because bots can reply with embeds even without either of these perms.
                // The only thing stopping them is the user's ability to use Application Commands.
                // It's extremely dumb, but what more can you do.
                if (p == Permission.VIEW_CHANNEL || p == Permission.MESSAGE_EMBED_LINKS)
                    continue;

                Member selfMember = event.getGuild() == null ? null : event.getGuild().getSelfMember();
                if(p.isChannel())
                {
                    if(p.name().startsWith("VOICE"))
                    {
                        GuildVoiceState gvc = event.getMember().getVoiceState();
                        AudioChannel vc = gvc == null ? null : gvc.getChannel();
                        if(vc==null)
                        {
                            terminate(event, event.getClient().getError()+" You must be in a voice channel to use that!");
                            return;
                        }
                        else if(!selfMember.hasPermission(vc, p))
                        {
                            terminate(event, String.format(botMissingPermMessage, event.getClient().getError(), p.getName(), "voice channel"));
                            return;
                        }
                    }
                    else
                    {
                        if(!selfMember.hasPermission(event.getGuildChannel(), p))
                        {
                            terminate(event, String.format(botMissingPermMessage, event.getClient().getError(), p.getName(), "channel"));
                            return;
                        }
                    }
                }
                else
                {
                    if(!selfMember.hasPermission(p))
                    {
                        terminate(event, String.format(botMissingPermMessage, event.getClient().getError(), p.getName(), "server"));
                        return;
                    }
                }
            }
        }

        // run
        try {
            execute(event);
        } catch(Throwable t) {
            if(event.getClient().getListener() != null)
            {
                event.getClient().getListener().onUserContextMenuException(event, this, t);
                return;
            }
            // otherwise we rethrow
            throw t;
        }

        if(event.getClient().getListener() != null)
            event.getClient().getListener().onCompletedUserContextMenu(event, this);
    }

    /**
     * The main body method of a {@link UserContextMenu}.
     * <br>This is the "response" for a successful
     * {@link UserContextMenu#run(UserContextMenuEvent)}
     *
     * @param  event
     *         The {@link UserContextMenuEvent} that triggered this menu.
     */
    protected abstract void execute(UserContextMenuEvent event);

    private void terminate(UserContextMenuEvent event, String message)
    {
        if(message!=null)
            event.reply(message).setEphemeral(true).queue();
        if(event.getClient().getListener()!=null)
            event.getClient().getListener().onTerminatedUserContextMenu(event, this);
    }

    @Override
    public CommandData buildCommandData()
    {
        // Make the command data
        CommandData data = Commands.user(getName());

        if (this.userPermissions == null)
            data.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        else
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(this.userPermissions));

        Set<InteractionContextType> contexts = getContexts();

        // manually set to true
        if (this.guildOnly == null) {
            // do nothing!!! nothing!!!!
        } else if (this.guildOnly) {
            // remove bot dm from contexts
            contexts.remove(InteractionContextType.BOT_DM);
        } else {
            contexts.add(InteractionContextType.BOT_DM);
        }
        data.setNSFW(this.nsfwOnly);

        // Check for guildOnly state.
        if (this.guildOnly == null) {
            // don't do anything
        } else if (this.guildOnly) {
            contexts.remove(InteractionContextType.BOT_DM);
        } else {
            contexts.add(InteractionContextType.BOT_DM);
        }

        Set<IntegrationType> types = new HashSet<>();
        // Mark as a user install if it's a private channel. Only users can access private channels.
        if (contexts.contains(InteractionContextType.PRIVATE_CHANNEL)) {
            types.add(IntegrationType.USER_INSTALL);
        }
        // Mark as a guild install if it's a guild or bot dm. Default behavior.
        if (contexts.contains(InteractionContextType.BOT_DM) || contexts.contains(InteractionContextType.GUILD)) {
            types.add(IntegrationType.GUILD_INSTALL);
        }

        data.setIntegrationTypes(types);
        data.setContexts(contexts);

        return data;
    }
}
