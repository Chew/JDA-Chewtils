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

public abstract class MessageContextMenu extends ContextMenu
{
    /**
     * Runs checks for the {@link MessageContextMenu} with the given {@link MessageContextMenuEvent} that called it.
     * <br>Will terminate, and possibly respond with a failure message, if any checks fail.
     *
     * @param  event
     *         The MessageContextMenuEvent that triggered this menu
     */
    public final void run(MessageContextMenuEvent event)
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
                    // Member will never be null because this is only ran in a server (text channel)
                    if(event.getMember() == null)
                        continue;

                    if(p.isChannel())
                    {
                        if(!event.getMember().hasPermission(event.getGuildChannel(), p))
                        {
                            terminate(event, String.format("%s%s%s", event.getClient().getError(), p.getName(), "channel"));
                            return;
                        }
                    }
                    else
                    {
                        if(!event.getMember().hasPermission(p))
                        {
                            terminate(event, String.format("%s%s%s", event.getClient().getError(), p.getName(), "server"));
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
                            terminate(event, String.format("%s%s%s", event.getClient().getError(), p.getName(), "voice channel"));
                            return;
                        }
                    }
                    else
                    {
                        if(!selfMember.hasPermission(event.getGuildChannel(), p))
                        {
                            terminate(event, String.format("%s%s%s", event.getClient().getError(), p.getName(), "channel"));
                            return;
                        }
                    }
                }
                else
                {
                    if(!selfMember.hasPermission(p))
                    {
                        terminate(event, String.format("%s%s%s", event.getClient().getError(), p.getName(), "server"));
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
                event.getClient().getListener().onMessageContextMenuException(event, this, t);
                return;
            }
            // otherwise we rethrow
            throw t;
        }

        if(event.getClient().getListener() != null)
            event.getClient().getListener().onCompletedMessageContextMenu(event, this);
    }

    /**
     * The main body method of a {@link MessageContextMenu}.
     * <br>This is the "response" for a successful
     * {@link MessageContextMenu#run(MessageContextMenuEvent)}
     *
     * @param  event
     *         The {@link MessageContextMenuEvent} that triggered this menu.
     */
    protected abstract void execute(MessageContextMenuEvent event);

    private void terminate(MessageContextMenuEvent event, String message)
    {
        if(message!=null)
            event.reply(message).setEphemeral(true).queue();
        if(event.getClient().getListener()!=null)
            event.getClient().getListener().onTerminatedMessageContextMenu(event, this);
    }

    @Override
    public CommandData buildCommandData()
    {
        // Make the command data
        CommandData data = Commands.message(getName());
        if (this.userPermissions == null)
            data.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        else
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(this.userPermissions));

        Set<InteractionContextType> contexts = getContexts();

        // Check for guildOnly state.
        if (this.guildOnly == null) {
            // don't do anything
        } else if (this.guildOnly) {
            contexts.remove(InteractionContextType.BOT_DM);
        } else if (!this.guildOnly) {
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

        data.setNSFW(this.nsfwOnly);

        return data;
    }
}
