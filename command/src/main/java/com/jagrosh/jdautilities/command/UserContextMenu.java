package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

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
            for(Permission p: userPermissions)
            {
                // Member will never be null because this is only ran in a server
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
}
