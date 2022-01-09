package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ContextMenu extends Interaction
{
    /**
     * The name of the command. This appears in the context menu.
     * Can be 1-32 characters long. Spaces are allowed.
     * @see net.dv8tion.jda.api.interactions.commands.build.Commands#context(Command.Type, String) Commands#context string argument.
     */
    protected String name = "null";

    /**
     * The type of context menu you want.
     * Can be of type User or Message.
     * @see Command.Type
     */
    protected Command.Type type = null;

    /**
     * Whether this command is disabled by default.
     * If disabled, you must give yourself permission to use it.<br>
     * @see net.dv8tion.jda.api.interactions.commands.build.CommandData#setDefaultEnabled
     */
    protected boolean defaultEnabled = true;

    /**
     * The list of role IDs who can use this Slash Command.
     * Because command privileges are restricted to a Guild, these will not take effect for Global commands.<br>
     * This is useless if {@link #defaultEnabled} isn't false.
     */
    protected String[] enabledRoles = new String[]{};

    /**
     * The list of user IDs who can use this Slash Command.
     * Because command privileges are restricted to a Guild, these will not take effect for Global commands.<br>
     * This is useless if {@link #defaultEnabled} isn't false.
     */
    protected String[] enabledUsers = new String[]{};

    /**
     * The list of role IDs who cannot use this Slash Command.
     * Because command privileges are restricted to a Guild, these will not take effect for Global commands.<br>
     * This is useless if {@link #defaultEnabled} isn't true.
     */
    protected String[] disabledRoles = new String[]{};

    /**
     * The list of user IDs who cannot use this Slash Command.
     * Because command privileges are restricted to a Guild, these will not take effect for Global commands.<br>
     * This is useless if {@link #defaultEnabled} isn't true.
     */
    protected String[] disabledUsers = new String[]{};

    /**
     * Runs checks for the {@link SlashCommand SlashCommand} with the
     * given {@link SlashCommandEvent SlashCommandEvent} that called it.
     * <br>Will terminate, and possibly respond with a failure message, if any checks fail.
     *
     * @param  event
     *         The SlashCommandEvent that triggered this Command
     */
    public final void run(ContextMenuEvent event)
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
                terminate(event, getCooldownError(event, remaining));
                return;
            }
            else event.getClient().applyCooldown(key, cooldown);
        }

        // availability check
        if(event.getChannelType()==ChannelType.TEXT)
        {
            //user perms
            for(Permission p: userPermissions)
            {
                // Member will never be null because this is only ran in a server (text channel)
                if(event.getMember() == null)
                    continue;

                if(p.isChannel())
                {
                    if(!event.getMember().hasPermission(event.getEvent().getGuildChannel(), p))
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
                        if(!selfMember.hasPermission(event.getEvent().getGuildChannel(), p))
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
                event.getClient().getListener().onContextMenuException(event, this, t);
                return;
            }
            // otherwise we rethrow
            throw t;
        }

        if(event.getClient().getListener() != null)
            event.getClient().getListener().onCompletedContextMenu(event, this);
    }

    /**
     * The main body method of a {@link com.jagrosh.jdautilities.command.Command Command}.
     * <br>This is the "response" for a successful
     * {@link com.jagrosh.jdautilities.command.Command#run(CommandEvent) #run(CommandEvent)}.
     *
     * @param  event
     *         The {@link com.jagrosh.jdautilities.command.CommandEvent CommandEvent} that
     *         triggered this Command
     */
    protected abstract void execute(ContextMenuEvent event);

    /**
     * Gets the {@link ContextMenu ContextMenu.name} for the Context Menu.
     *
     * @return The name for the Context Menu.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Whether this context menu is enabled by default.
     *
     * @return if this is default enabled
     */
    public boolean isDefaultEnabled()
    {
        return defaultEnabled;
    }

    /**
     * Gets the enabled roles for this Slash Command.
     * A user MUST have a role for a command to be ran.
     *
     * @return a list of String role IDs
     */
    public String[] getEnabledRoles()
    {
        return enabledRoles;
    }

    /**
     * Gets the enabled users for this Slash Command.
     * A user with an ID in this list is required for the command to be ran.
     *
     * @return a list of String user IDs
     */
    public String[] getEnabledUsers()
    {
        return enabledUsers;
    }

    /**
     * Gets the disabled roles for this Slash Command.
     * A user with this role may not run this command.
     *
     * @return a list of String role IDs
     */
    public String[] getDisabledRoles()
    {
        return disabledRoles;
    }

    /**
     * Gets the disabled users for this Slash Command.
     * Uses in this list may not run this command.
     *
     * @return a list of String user IDs
     */
    public String[] getDisabledUsers()
    {
        return disabledUsers;
    }

    private void terminate(ContextMenuEvent event, String message)
    {
        if(message!=null)
            event.reply(message).setEphemeral(true).queue();
        if(event.getClient().getListener()!=null)
            event.getClient().getListener().onTerminatedContextMenu(event, this);
    }

    /**
     * Gets the proper cooldown key for this Command under the provided {@link ContextMenuEvent}.
     *
     * @param event The ContextMenuEvent to generate the cooldown for.
     *
     * @return A String key to use when applying a cooldown.
     */
    public String getCooldownKey(ContextMenuEvent event)
    {
        switch (cooldownScope)
        {
            case USER:         return cooldownScope.genKey(name,event.getUser().getIdLong());
            case USER_GUILD:   return event.getGuild()!=null ? cooldownScope.genKey(name,event.getUser().getIdLong(),event.getGuild().getIdLong()) :
                CooldownScope.USER_CHANNEL.genKey(name,event.getUser().getIdLong(), event.getChannel().getIdLong());
            case USER_CHANNEL: return cooldownScope.genKey(name,event.getUser().getIdLong(),event.getChannel().getIdLong());
            case GUILD:        return event.getGuild()!=null ? cooldownScope.genKey(name,event.getGuild().getIdLong()) :
                CooldownScope.CHANNEL.genKey(name,event.getChannel().getIdLong());
            case CHANNEL:      return cooldownScope.genKey(name,event.getChannel().getIdLong());
            case SHARD:        return event.getJDA().getShardInfo() != JDA.ShardInfo.SINGLE ? cooldownScope.genKey(name, event.getJDA().getShardInfo().getShardId()) :
                CooldownScope.GLOBAL.genKey(name, 0);
            case USER_SHARD:   return event.getJDA().getShardInfo() != JDA.ShardInfo.SINGLE ? cooldownScope.genKey(name,event.getUser().getIdLong(),event.getJDA().getShardInfo().getShardId()) :
                CooldownScope.USER.genKey(name, event.getUser().getIdLong());
            case GLOBAL:       return cooldownScope.genKey(name, 0);
            default:           return "";
        }
    }

    /**
     * Gets an error message for this Context Menu under the provided {@link ContextMenuEvent}.
     *
     * @param  event
     *         The CommandEvent to generate the error message for.
     * @param  remaining
     *         The remaining number of seconds a command is on cooldown for.
     *
     * @return A String error message for this command if {@code remaining > 0},
     *         else {@code null}.
     */
    public String getCooldownError(ContextMenuEvent event, int remaining)
    {
        if(remaining<=0)
            return null;
        String front = event.getClient().getWarning()+" That command is on cooldown for "+remaining+" more seconds";
        if(cooldownScope.equals(CooldownScope.USER))
            return front+"!";
        else if(cooldownScope.equals(CooldownScope.USER_GUILD) && event.getGuild()==null)
            return front+" "+CooldownScope.USER_CHANNEL.errorSpecification+"!";
        else if(cooldownScope.equals(CooldownScope.GUILD) && event.getGuild()==null)
            return front+" "+CooldownScope.CHANNEL.errorSpecification+"!";
        else
            return front+" "+cooldownScope.errorSpecification+"!";
    }

    /**
     * Builds CommandData for the ContextMenu upsert.
     * This code is executed when we need to upsert the command.
     *
     * Useful for manual upserting.
     *
     * @return the built command data
     */
    public CommandData buildCommandData()
    {
        // Make the command data
        CommandData data = Commands.context(type, name);

        // Default enabled is synonymous with hidden now.
        data.setDefaultEnabled(isDefaultEnabled());

        return data;
    }

    /**
     * Builds CommandPrivilege for the SlashCommand permissions.
     * This code is executed after upsertion to update the permissions.
     * <br>
     * <b>The max amount of privilege is 10, keep this in mind.</b>
     *
     * Useful for manual upserting.
     *
     * @param client the command client for owner checking.
     *               if null, owner checks won't be performed
     * @return the built privilege data
     */
    public List<CommandPrivilege> buildPrivileges(@Nullable CommandClient client)
    {
        List<CommandPrivilege> privileges = new ArrayList<>();
        // Privilege Checks
        for (String role : getEnabledRoles())
            privileges.add(CommandPrivilege.enableRole(role));
        for (String user : getEnabledUsers())
            privileges.add(CommandPrivilege.enableUser(user));
        for (String role : getDisabledRoles())
            privileges.add(CommandPrivilege.disableRole(role));
        for (String user : getDisabledUsers())
            privileges.add(CommandPrivilege.disableUser(user));
        // Co/Owner checks
        if (isOwnerCommand() && client != null)
        {
            // Clear array, we have the priority here.
            privileges.clear();
            // Add owner
            privileges.add(CommandPrivilege.enableUser(client.getOwnerId()));
            // Add co-owners
            if (client.getCoOwnerIds() != null)
                for (String user : client.getCoOwnerIds())
                    privileges.add(CommandPrivilege.enableUser(user));
        }

        // can only have up to 10 privileges
        if (privileges.size() > 10)
            privileges = privileges.subList(0, 10);

        return privileges;
    }
}
