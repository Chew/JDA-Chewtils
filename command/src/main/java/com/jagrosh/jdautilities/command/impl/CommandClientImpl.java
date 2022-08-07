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
package com.jagrosh.jdautilities.command.impl;

import com.jagrosh.jdautilities.command.AnnotatedModuleCompiler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.Command.Category;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.command.ContextMenu;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import com.jagrosh.jdautilities.commons.utils.FixedSizeCache;
import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of {@link com.jagrosh.jdautilities.command.CommandClient CommandClient} to be used by a bot.
 *
 * <p>This is a listener usable with {@link net.dv8tion.jda.api.JDA JDA}, as it implements
 * {@link net.dv8tion.jda.api.hooks.EventListener EventListener} in order to catch and use different kinds of
 * {@link net.dv8tion.jda.api.events.Event Event}s. The primary usage of this is where the CommandClient implementation
 * takes {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent}s, and automatically
 * processes arguments, and provide them to a {@link com.jagrosh.jdautilities.command.Command Command} for
 * running and execution.
 *
 * @author John Grosh (jagrosh)
 */
public class CommandClientImpl implements CommandClient, EventListener
{
    private static final Logger LOG = LoggerFactory.getLogger(CommandClient.class);
    private static final String DEFAULT_PREFIX = "@mention";

    private final OffsetDateTime start;
    private final Activity activity;
    private final OnlineStatus status;
    private final String ownerId;
    private final String[] coOwnerIds;
    private final String prefix;
    private final String altprefix;
    private final String[] prefixes;
    private final Function<MessageReceivedEvent, String> prefixFunction;
    private final Function<MessageReceivedEvent, Boolean> commandPreProcessFunction;
    private final BiFunction<MessageReceivedEvent, Command, Boolean> commandPreProcessBiFunction;
    private final String serverInvite;
    private final HashMap<String, Integer> commandIndex;
    private final HashMap<String, Integer> slashCommandIndex;
    private final ArrayList<Command> commands;
    private final ArrayList<SlashCommand> slashCommands;
    private final ArrayList<String> slashCommandIds;
    private final ArrayList<ContextMenu> contextMenus;
    private final HashMap<String, Integer> contextMenuIndex;
    private final String forcedGuildId;
    private final boolean manualUpsert;
    private final String success;
    private final String warning;
    private final String error;
    private final String botsKey, carbonKey;
    private final HashMap<String,OffsetDateTime> cooldowns;
    private final HashMap<String,Integer> uses;
    private final FixedSizeCache<Long, Set<Message>> linkMap;
    private final boolean useHelp;
    private final boolean shutdownAutomatically;
    private final Consumer<CommandEvent> helpConsumer;
    private final String helpWord;
    private final ScheduledExecutorService executor;
    private final AnnotatedModuleCompiler compiler;
    private final GuildSettingsManager manager;

    private String textPrefix;
    private CommandListener listener = null;
    private int totalGuilds;

    public CommandClientImpl(String ownerId, String[] coOwnerIds, String prefix, String altprefix, String[] prefixes, Function<MessageReceivedEvent, String> prefixFunction, Function<MessageReceivedEvent, Boolean> commandPreProcessFunction, BiFunction<MessageReceivedEvent, Command, Boolean> commandPreProcessBiFunction, Activity activity, OnlineStatus status, String serverInvite,
                             String success, String warning, String error, String carbonKey, String botsKey, ArrayList<Command> commands, ArrayList<SlashCommand> slashCommands, ArrayList<ContextMenu> contextMenus, String forcedGuildId, boolean manualUpsert,
                             boolean useHelp, boolean shutdownAutomatically, Consumer<CommandEvent> helpConsumer, String helpWord, ScheduledExecutorService executor,
                             int linkedCacheSize, AnnotatedModuleCompiler compiler, GuildSettingsManager manager)
    {
        Checks.check(ownerId != null, "Owner ID was set null or not set! Please provide an User ID to register as the owner!");

        if(!SafeIdUtil.checkId(ownerId))
            LOG.warn(String.format("The provided Owner ID (%s) was found unsafe! Make sure ID is a non-negative long!", ownerId));

        if(coOwnerIds!=null)
        {
            for(String coOwnerId : coOwnerIds)
            {
                if(!SafeIdUtil.checkId(coOwnerId))
                    LOG.warn(String.format("The provided CoOwner ID (%s) was found unsafe! Make sure ID is a non-negative long!", coOwnerId));
            }
        }

        this.start = OffsetDateTime.now();

        this.ownerId = ownerId;
        this.coOwnerIds = coOwnerIds;
        this.prefix = prefix==null || prefix.isEmpty() ? DEFAULT_PREFIX : prefix;
        this.altprefix = altprefix==null || altprefix.isEmpty() ? null : altprefix;

        this.prefixes = prefixes==null || prefixes.length == 0 ? null : prefixes;
        if (this.prefixes != null) {
            Arrays.sort(this.prefixes, Comparator.reverseOrder());
        }

        this.prefixFunction = prefixFunction;
        this.commandPreProcessFunction = commandPreProcessFunction;
        this.commandPreProcessBiFunction = commandPreProcessBiFunction;
        this.textPrefix = prefix;
        this.activity = activity;
        this.status = status;
        this.serverInvite = serverInvite;
        this.success = success==null ? "": success;
        this.warning = warning==null ? "": warning;
        this.error = error==null ? "": error;
        this.carbonKey = carbonKey;
        this.botsKey = botsKey;
        this.commandIndex = new HashMap<>();
        this.slashCommandIndex = new HashMap<>();
        this.commands = new ArrayList<>();
        this.slashCommands = new ArrayList<>();
        this.slashCommandIds = new ArrayList<>();
        this.contextMenus = new ArrayList<>();
        this.contextMenuIndex = new HashMap<>();
        this.forcedGuildId = forcedGuildId;
        this.manualUpsert = manualUpsert;
        this.cooldowns = new HashMap<>();
        this.uses = new HashMap<>();
        this.linkMap = linkedCacheSize>0 ? new FixedSizeCache<>(linkedCacheSize) : null;
        this.useHelp = useHelp;
        this.shutdownAutomatically = shutdownAutomatically;
        this.helpWord = helpWord==null ? "help" : helpWord;
        this.executor = executor==null ? Executors.newSingleThreadScheduledExecutor() : executor;
        this.compiler = compiler;
        this.manager = manager;
        this.helpConsumer = helpConsumer==null ? (event) -> {
                StringBuilder builder = new StringBuilder("**"+event.getSelfUser().getName()+"** commands:\n");
                Category category = null;
                for(Command command : commands)
                {
                    if(!command.isHidden() && (!command.isOwnerCommand() || event.isOwner()))
                    {
                        if(!Objects.equals(category, command.getCategory()))
                        {
                            category = command.getCategory();
                            builder.append("\n\n  __").append(category==null ? "No Category" : category.getName()).append("__:\n");
                        }
                        builder.append("\n`").append(textPrefix).append(prefix==null?" ":"").append(command.getName())
                               .append(command.getArguments()==null ? "`" : " "+command.getArguments()+"`")
                               .append(" - ").append(command.getHelp());
                    }
                }
                User owner = event.getJDA().getUserById(ownerId);
                if(owner!=null)
                {
                    builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#").append(owner.getDiscriminator());
                    if(serverInvite!=null)
                        builder.append(" or join ").append(serverInvite);
                }
                event.replyInDm(builder.toString(), unused ->
                {
                    if(event.isFromType(ChannelType.TEXT))
                        event.reactSuccess();
                }, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
        } : helpConsumer;

        // Load commands
        for(Command command : commands)
        {
            addCommand(command);
        }

        // Load slash commands
        for(SlashCommand command : slashCommands)
        {
            addSlashCommand(command);
        }

        // Load context menus
        for(ContextMenu menu : contextMenus)
        {
            addContextMenu(menu);
        }
    }

    @Override
    public void setListener(CommandListener listener)
    {
        this.listener = listener;
    }

    @Override
    public CommandListener getListener()
    {
        return listener;
    }

    @Override
    public List<Command> getCommands()
    {
        return commands;
    }

    @Override
    public List<SlashCommand> getSlashCommands()
    {
        return slashCommands;
    }

    @Override
    public List<ContextMenu> getContextMenus()
    {
        return contextMenus;
    }

    @Override
    public boolean isManualUpsert()
    {
        return manualUpsert;
    }

    @Override
    public String forcedGuildId()
    {
        return forcedGuildId;
    }

    @Override
    public OffsetDateTime getStartTime()
    {
        return start;
    }

    @Override
    public OffsetDateTime getCooldown(String name)
    {
        return cooldowns.get(name);
    }

    @Override
    public int getRemainingCooldown(String name)
    {
        if(cooldowns.containsKey(name))
        {
            int time = (int) Math.ceil(OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.MILLIS) / 1000D);
            if(time<=0)
            {
                cooldowns.remove(name);
                return 0;
            }
            return time;
        }
        return 0;
    }

    @Override
    public void applyCooldown(String name, int seconds)
    {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    @Override
    public void cleanCooldowns()
    {
        OffsetDateTime now = OffsetDateTime.now();
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now)))
                .collect(Collectors.toList()).forEach(cooldowns::remove);
    }

    @Override
    public int getCommandUses(Command command)
    {
    	return getCommandUses(command.getName());
    }

    @Override
    public int getCommandUses(String name)
    {
    	return uses.getOrDefault(name, 0);
    }

    @Override
    public void addCommand(Command command)
    {
        addCommand(command, commands.size());
    }

    @Override
    public void addCommand(Command command, int index)
    {
        if(index>commands.size() || index<0)
            throw new ArrayIndexOutOfBoundsException("Index specified is invalid: ["+index+"/"+commands.size()+"]");
        synchronized(commandIndex)
        {
            String name = command.getName().toLowerCase(Locale.ROOT);
            //check for collision
            if(commandIndex.containsKey(name))
                throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \""+name+"\"!");
            for(String alias : command.getAliases())
            {
                if(commandIndex.containsKey(alias.toLowerCase(Locale.ROOT)))
                    throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \""+alias+"\"!");
            }
            //shift if not append
            if(index<commands.size())
            {
                commandIndex.entrySet().stream().filter(entry -> entry.getValue()>=index).collect(Collectors.toList())
                    .forEach(entry -> commandIndex.put(entry.getKey(), entry.getValue()+1));
            }
            //add
            commandIndex.put(name, index);
            for(String alias : command.getAliases())
                commandIndex.put(alias.toLowerCase(Locale.ROOT), index);
        }
        commands.add(index,command);
    }

    @Override
    public void addSlashCommand(SlashCommand command)
    {
        addSlashCommand(command, slashCommands.size());
    }

    @Override
    public void addSlashCommand(SlashCommand command, int index)
    {
        if(index>slashCommands.size() || index<0)
            throw new ArrayIndexOutOfBoundsException("Index specified is invalid: ["+index+"/"+slashCommands.size()+"]");
        synchronized(slashCommandIndex)
        {
            String name = command.getName().toLowerCase(Locale.ROOT);
            //check for collision
            if(slashCommandIndex.containsKey(name))
                throw new IllegalArgumentException("Command added has a name that has already been indexed: \""+name+"\"!");
            //shift if not append
            if(index<slashCommands.size())
            {
                slashCommandIndex.entrySet().stream().filter(entry -> entry.getValue()>=index).collect(Collectors.toList())
                    .forEach(entry -> slashCommandIndex.put(entry.getKey(), entry.getValue()+1));
            }
            //add
            slashCommandIndex.put(name, index);
        }
        slashCommands.add(index,command);
    }

    @Override
    public void addContextMenu(ContextMenu menu)
    {
        addContextMenu(menu, contextMenus.size());
    }

    @Override
    public void addContextMenu(ContextMenu menu, int index)
    {
        if(index>contextMenus.size() || index<0)
            throw new ArrayIndexOutOfBoundsException("Index specified is invalid: ["+index+"/"+contextMenus.size()+"]");
        synchronized(contextMenuIndex)
        {
            String name = menu.getName();
            //check for collision
            if(contextMenuIndex.containsKey(name)) {
                // Compare the existing menu's class to the new menu's class
                if (contextMenuIndex.get(name).getClass().getName().equals(menu.getClass().getName())) {
                    throw new IllegalArgumentException("Context Menu added has a name and class that has already been indexed: \"" + name + "\"!");
                }
            }
            //shift if not append
            if(index<contextMenuIndex.size())
            {
                contextMenuIndex.entrySet().stream().filter(entry -> entry.getValue()>=index).collect(Collectors.toList())
                    .forEach(entry -> contextMenuIndex.put(entry.getKey(), entry.getValue()+1));
            }
            //add
            contextMenuIndex.put(name, index);
        }
        contextMenus.add(index,menu);
    }

    @Override
    public void removeCommand(String name)
    {
        synchronized(commandIndex)
        {
            if(!commandIndex.containsKey(name.toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("Name provided is not indexed: \"" + name + "\"!");
            int targetIndex = commandIndex.remove(name.toLowerCase(Locale.ROOT));
            Command removedCommand = commands.remove(targetIndex);
            for(String alias : removedCommand.getAliases())
            {
                commandIndex.remove(alias.toLowerCase(Locale.ROOT));
            }
            commandIndex.entrySet().stream().filter(entry -> entry.getValue()>targetIndex).collect(Collectors.toList())
                .forEach(entry -> commandIndex.put(entry.getKey(), entry.getValue()-1));
        }
    }

    @Override
    public void addAnnotatedModule(Object module)
    {
        compiler.compile(module).forEach(this::addCommand);
    }

    @Override
    public void addAnnotatedModule(Object module, Function<Command, Integer> mapFunction)
    {
        compiler.compile(module).forEach(command -> addCommand(command, mapFunction.apply(command)));
    }

    @Override
    public String getOwnerId()
    {
        return ownerId;
    }

    @Override
    public long getOwnerIdLong()
    {
        return Long.parseLong(ownerId);
    }

    @Override
    public String[] getCoOwnerIds()
    {
    	return coOwnerIds;
    }

    @Override
    public long[] getCoOwnerIdsLong()
    {
        // Thought about using java.util.Arrays#setAll(T[], IntFunction<T>)
        // here, but as it turns out it's actually the same thing as this but
        // it throws an error if null. Go figure.
        if(coOwnerIds==null)
            return null;
        long[] ids = new long[coOwnerIds.length];
        for(int i = 0; i<ids.length; i++)
            ids[i] = Long.parseLong(coOwnerIds[i]);
        return ids;
    }

    @Override
    public String getSuccess()
    {
        return success;
    }

    @Override
    public String getWarning()
    {
        return warning;
    }

    @Override
    public String getError()
    {
        return error;
    }

    @Override
    public ScheduledExecutorService getScheduleExecutor()
    {
        return executor;
    }

    @Override
    public String getServerInvite()
    {
        return serverInvite;
    }

    @Override
    public String getPrefix()
    {
        return prefix;
    }

    @Override
    public String[] getPrefixes() {
        return prefixes;
    }

    @Override
    public Function<MessageReceivedEvent, String> getPrefixFunction()
    {
        return prefixFunction;
    }

    @Override
    public String getAltPrefix()
    {
        return altprefix;
    }

    @Override
    public String getTextualPrefix()
    {
        return textPrefix;
    }

    @Override
    public int getTotalGuilds()
    {
        return totalGuilds;
    }

    @Override
    public String getHelpWord()
    {
        return helpWord;
    }

    @Override
    public boolean usesLinkedDeletion() {
        return linkMap != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getSettingsFor(Guild guild)
    {
        if (manager==null)
            return null;
        return (S) manager.getSettings(guild);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends GuildSettingsManager> M getSettingsManager()
    {
        return (M) manager;
    }

    @Override
    public void shutdown()
    {
        GuildSettingsManager<?> manager = getSettingsManager();
        if(manager != null)
            manager.shutdown();
        executor.shutdown();
    }

    @Override
    public void onEvent(GenericEvent event)
    {
        if(event instanceof MessageReceivedEvent)
            onMessageReceived((MessageReceivedEvent)event);

        else if(event instanceof SlashCommandInteractionEvent)
            onSlashCommand((SlashCommandInteractionEvent)event);

        else if(event instanceof MessageContextInteractionEvent)
            onMessageContextMenu((MessageContextInteractionEvent)event);
        else if(event instanceof UserContextInteractionEvent)
            onUserContextMenu((UserContextInteractionEvent)event);

        else if (event instanceof CommandAutoCompleteInteractionEvent)
            onCommandAutoComplete((CommandAutoCompleteInteractionEvent)event);

        else if(event instanceof MessageDeleteEvent && usesLinkedDeletion())
            onMessageDelete((MessageDeleteEvent) event);

        else if(event instanceof GuildJoinEvent)
        {
            if(((GuildJoinEvent)event).getGuild().getSelfMember().getTimeJoined()
                    .plusMinutes(10).isAfter(OffsetDateTime.now()))
                sendStats(event.getJDA());
        }
        else if(event instanceof GuildLeaveEvent)
            sendStats(event.getJDA());
        else if(event instanceof ReadyEvent)
            onReady((ReadyEvent)event);
        else if(event instanceof ShutdownEvent)
        {
            if(shutdownAutomatically)
                shutdown();
        }
    }

    private void onReady(ReadyEvent event)
    {
        if(!event.getJDA().getSelfUser().isBot())
        {
            LOG.error("JDA-Utilities does not support CLIENT accounts.");
            event.getJDA().shutdown();
            return;
        }
        textPrefix = prefix.equals(DEFAULT_PREFIX) ? "@"+event.getJDA().getSelfUser().getName()+" " : prefix;

        if(activity != null)
            event.getJDA().getPresence().setPresence(status==null ? OnlineStatus.ONLINE : status,
                "default".equals(activity.getName()) ? Activity.playing("Type "+textPrefix+helpWord) : activity);

        // Start SettingsManager if necessary
        GuildSettingsManager<?> manager = getSettingsManager();
        if(manager != null)
            manager.init();

        // Upsert slash commands, if not manual
        if (!manualUpsert)
        {
            upsertInteractions(event.getJDA());
        }

        sendStats(event.getJDA());
    }

    @Override
    public void upsertInteractions(JDA jda)
    {
        upsertInteractions(jda, forcedGuildId);
    }

    @Override
    public void upsertInteractions(JDA jda, String serverId)
    {
        // Get all commands
        List<CommandData> data = new ArrayList<>();
        List<SlashCommand> slashCommands = getSlashCommands();
        Map<String, SlashCommand> slashCommandMap = new HashMap<>();
        List<ContextMenu> contextMenus = getContextMenus();
        Map<String, ContextMenu> contextMenuMap = new HashMap<>();

        // Build the command and privilege data
        for (SlashCommand command : slashCommands)
        {
            data.add(command.buildCommandData());
            slashCommandMap.put(command.getName(), command);
        }

        for (ContextMenu menu : contextMenus) {
            data.add(menu.buildCommandData());
            contextMenuMap.put(menu.getName(), menu);
        }

        // Upsert the commands
        if (serverId != null)
        {
            // Attempt to retrieve the provided guild
            Guild server = jda.getGuildById(serverId);
            if (server == null)
            {
                LOG.error("Specified forced guild is null! Slash Commands will NOT be added! Is the bot added?");
                return;
            }
            // Upsert the commands + their privileges
            server.updateCommands().addCommands(data)
                .queue(
                    priv -> LOG.debug("Successfully added " + commands.size() + " slash commands and " + contextMenus.size() + " menus to server " + server.getName()),
                    error -> LOG.error("Could not upsert commands! Does the bot have the applications.commands scope?" + error)
                );
        }
        else
            jda.updateCommands().addCommands(data)
                .queue(commands -> LOG.debug("Successfully added " + commands.size() + " slash commands!"));
    }

    private void onMessageReceived(MessageReceivedEvent event)
    {
        // Return if it's a bot
        if(event.getAuthor().isBot())
            return;

        final MessageParts parts = getParts(event);

        if(parts!=null) //starts with valid prefix
        {
            if(useHelp && parts.command.equalsIgnoreCase(helpWord))
            {
                CommandEvent cevent = new CommandEvent(event, parts.prefixUsed, parts.args, this);
                if(listener!=null)
                    listener.onCommand(cevent, null);
                helpConsumer.accept(cevent); // Fire help consumer
                if(listener!=null)
                    listener.onCompletedCommand(cevent, null);
                return; // Help Consumer is done
            }
            else if(event.isFromType(ChannelType.PRIVATE) || event.getChannel().canTalk())
            {
                String name = parts.command;
                String args = parts.args;
                final Command command; // this will be null if it's not a command
                synchronized(commandIndex)
                {
                    int i = commandIndex.getOrDefault(name.toLowerCase(Locale.ROOT), -1);
                    command = i != -1? commands.get(i) : null;
                }

                if(command != null)
                {
                    CommandEvent cevent = new CommandEvent(event, parts.prefixUsed, args, this);

                    if(listener != null)
                        listener.onCommand(cevent, command);
                    uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
                    if (commandPreProcessFunction != null || commandPreProcessBiFunction != null)
                    {
                        // Apply both pre-process functions
                        if (commandPreProcessFunction != null && commandPreProcessFunction.apply(event))
                        {
                            command.run(cevent);
                            return;
                        }

                        if (commandPreProcessBiFunction != null && commandPreProcessBiFunction.apply(event, command))
                        {
                            command.run(cevent);
                            return;
                        }

                        // If we are here, neither function returned true, so we can just return
                        return;
                    }
                    else
                    {
                        command.run(cevent);
                    }
                    return; // Command is done
                }
            }
        }

        if(listener != null)
            listener.onNonCommandMessage(event);
    }

    @Nullable
    private MessageParts getParts(MessageReceivedEvent event) {
        String rawContent = event.getMessage().getContentRaw();

        GuildSettingsProvider settings = event.isFromType(ChannelType.TEXT)? provideSettings(event.getGuild()) : null;

        // Check for prefix or alternate prefix (@mention cases)
        if(prefix.equals(DEFAULT_PREFIX) || (altprefix != null && altprefix.equals(DEFAULT_PREFIX))) {
            if(rawContent.startsWith("<@"+ event.getJDA().getSelfUser().getId()+">") ||
                    rawContent.startsWith("<@!"+ event.getJDA().getSelfUser().getId()+">")) {
                // Since we now use substring into makeMessageParts function and a indexOf here, we need to do a +1 to get the good substring
                // On top of that we need to do another +1 because the default @mention prefix will always be followed by a space
                // So we need to add 2 characters to get the correct substring
                final int prefixLength = rawContent.indexOf('>') + 2;
                return makeMessageParts(rawContent, prefixLength);
            }
        }

        // Check for prefix
        // Run Function check if there is one, then fallback to normal prefixes
        if (prefixFunction != null) {
            String prefix = prefixFunction.apply(event);
            // Don't lowercase, up to Function to handle this
            if (prefix != null && rawContent.startsWith(prefix)) {
                final int prefixLength = prefix.length();
                return makeMessageParts(rawContent, prefixLength);
            }
        }

        final String lowerCaseContent = rawContent.toLowerCase(Locale.ROOT);
        // Check for default prefix
        if (lowerCaseContent.startsWith(prefix.toLowerCase(Locale.ROOT))) {
            final int prefixLength = prefix.length();
            return makeMessageParts(rawContent, prefixLength);
        }

        // Check for alternate prefix
        if(altprefix != null && lowerCaseContent.startsWith(altprefix.toLowerCase(Locale.ROOT))) {
            final int prefixLength = altprefix.length();
            return makeMessageParts(rawContent, prefixLength);
        }

        // Check for prefixes
        if (prefixes != null) {
            for (String pre : prefixes) {
                if (lowerCaseContent.startsWith(pre.toLowerCase(Locale.ROOT))) {
                    final int prefixLength = pre.length();
                    return makeMessageParts(rawContent, prefixLength);
                }
            }
        }

        // Check for guild specific prefixes
        if(settings != null) {
            Collection<String> prefixes = settings.getPrefixes();
            if(prefixes != null) {
                for(String prefix : prefixes) {
                    if(lowerCaseContent.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                        final int prefixLength = prefix.length();
                        return makeMessageParts(rawContent, prefixLength);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Processes the message raw content and returns the "parts" of the message
     * <br>These parts include:
     * <ul>
     *     <li>Used prefix</li>
     *     <li>Command name</li>
     *     <li>Arguments</li>
     * </ul>
     *
     * @param rawContent
     *        The raw content of the incoming message
     * @param prefixLength
     *        The length of the prefix that has been successfully detected before calling this method
     * @return A MessageParts objects with all the parts cited above
     */
    @NotNull
    private CommandClientImpl.MessageParts makeMessageParts(String rawContent, int prefixLength) {
        //Replacement method below
//        final String[] split = rawContent.substring(prefixLength).trim().split("\\s+", 2);

        //What we do is search for the first whitespace after the prefix, this gets us the command name
        // To then get the arguments, we find the first occurrence of a character other than a whitespace, after the command index, and take the string from that index
        String cmd = null;
        for (int i = prefixLength; i < rawContent.length(); i++) {
            if (Character.isWhitespace(rawContent.charAt(i))) { //If a whitespace assume we found the end of the command name
                cmd = rawContent.substring(prefixLength, i);
                break;
            }
        }

        String args = "";
        if (cmd == null) { //Assume there are no args since there were absolutely no whitespace
            cmd = rawContent.substring(prefixLength);
        } else {
            for (int i = prefixLength + cmd.length(); i < rawContent.length(); i++) {
                if (!Character.isWhitespace(rawContent.charAt(i))) { //If not a whitespace assume we found the start of the arguments
                    args = rawContent.substring(i);
                    break;
                }
            }
        }

        //Just in case something fucked up
        LOG.trace("Received command named '{}' with args '{}'", cmd, args);

        return new MessageParts(
            rawContent.substring(0, prefixLength),
            cmd,
            args
        );
    }

    private void onSlashCommand(SlashCommandInteractionEvent event)
    {
        // this will be null if it's not a command
        final SlashCommand command = findSlashCommand(event.getCommandPath());

        // Wrap the event in a SlashCommandEvent
        final SlashCommandEvent commandEvent = new SlashCommandEvent(event, this);

        if(command != null)
        {
            if(listener != null)
                listener.onSlashCommand(commandEvent, command);
            uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
            command.run(commandEvent);
            // Command is done
        }
    }

    private void onCommandAutoComplete(CommandAutoCompleteInteractionEvent event)
    {
        // this will be null if it's not a command
        final SlashCommand command = findSlashCommand(event.getCommandPath());

        if(command != null)
        {
            command.onAutoComplete(event);
        }
    }

    private SlashCommand findSlashCommand(String path)
    {
        String[] parts = path.split("/");

        final SlashCommand command; // this will be null if it's not a command
        synchronized(slashCommandIndex)
        {
            int i = slashCommandIndex.getOrDefault(parts[0].toLowerCase(Locale.ROOT), -1);
            command = i != -1? slashCommands.get(i) : null;
        }

        if (command == null)
            return null;

        switch (parts.length) {
            case 1: // Slash command with no children
                return command;
            case 2: // Slash command with children
                // child check
                for(SlashCommand cmd: command.getChildren())
                    if(cmd.isCommandFor(parts[1]))
                        return cmd;

                return null;
            case 3: // Slash command with a group and a child
                for(SlashCommand cmd: command.getChildren())
                    if(cmd.isCommandFor(parts[2]) && cmd.getSubcommandGroup().getName().equals(parts[1]))
                        return cmd;

                return null;
        }

        // How did we get here?
        return null;
    }

    private void onUserContextMenu(UserContextInteractionEvent event)
    {
        final UserContextMenu menu; // this will be null if it's not a command
        synchronized(contextMenuIndex)
        {
            ContextMenu c;
            int i = contextMenuIndex.getOrDefault(event.getName(), -1);
            c = i != -1 ? contextMenus.get(i) : null;

            if (c instanceof UserContextMenu)
                menu = (UserContextMenu) c;
            else
                menu = null;
        }

        final UserContextMenuEvent menuEvent = new UserContextMenuEvent(event.getJDA(), event.getResponseNumber(), event,this);

        if(menu != null)
        {
            if(listener != null)
                listener.onUserContextMenu(menuEvent, menu);
            uses.put(menu.getName(), uses.getOrDefault(menu.getName(), 0) + 1);
            menu.run(menuEvent);
            // Command is done
        }
    }

    private void onMessageContextMenu(MessageContextInteractionEvent event)
    {
        final MessageContextMenu menu; // this will be null if it's not a command
        synchronized(contextMenuIndex)
        {
            ContextMenu c;
            // Do not lowercase, as there could be 2 menus with the same name, but different letter cases
            int i = contextMenuIndex.getOrDefault(event.getName(), -1);
            c = i != -1 ? contextMenus.get(i) : null;

            if (c instanceof MessageContextMenu)
                menu = (MessageContextMenu) c;
            else
                menu = null;
        }

        final MessageContextMenuEvent menuEvent = new MessageContextMenuEvent(event.getJDA(), event.getResponseNumber(), event,this);

        if(menu != null)
        {
            if(listener != null)
                listener.onMessageContextMenu(menuEvent, menu);
            uses.put(menu.getName(), uses.getOrDefault(menu.getName(), 0) + 1);
            menu.run(menuEvent);
            // Command is done
        }
    }

    private void sendStats(JDA jda)
    {
        OkHttpClient client = jda.getHttpClient();

        if(carbonKey != null)
        {
            FormBody.Builder bodyBuilder = new FormBody.Builder()
                    .add("key", carbonKey)
                    .add("servercount", Integer.toString(jda.getGuilds().size()));

            if(jda.getShardInfo() != JDA.ShardInfo.SINGLE)
            {
                bodyBuilder.add("shard_id", Integer.toString(jda.getShardInfo().getShardId()))
                           .add("shard_count", Integer.toString(jda.getShardInfo().getShardTotal()));
            }

            Request.Builder builder = new Request.Builder()
                    .post(bodyBuilder.build())
                    .url("https://www.carbonitex.net/discord/data/botdata.php");

            client.newCall(builder.build()).enqueue(new Callback()
            {
                @Override
                public void onResponse(Call call, Response response)
                {
                    LOG.info("Successfully send information to carbonitex.net");
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e)
                {
                    LOG.error("Failed to send information to carbonitex.net ", e);
                }
            });
        }

        if(botsKey != null)
        {
            JSONObject body = new JSONObject().put("guildCount", jda.getGuilds().size());
            if(jda.getShardInfo() != JDA.ShardInfo.SINGLE)
            {
                body.put("shardId", jda.getShardInfo().getShardId())
                    .put("shardCount", jda.getShardInfo().getShardTotal());
            }

            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    .url("https://discord.bots.gg/api/v1/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsKey)
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback()
            {
                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    if(response.isSuccessful())
                    {
                        LOG.info("Successfully sent information to discord.bots.gg");
                        try(Reader reader = response.body().charStream())
                        {
                            totalGuilds = new JSONObject(new JSONTokener(reader)).getInt("guildCount");
                        }
                        catch(Exception ex)
                        {
                            LOG.error("Failed to retrieve bot shard information from discord.bots.gg ", ex);
                        }
                    }
                    else
                        LOG.error("Failed to send information to discord.bots.gg: "+response.body().string());
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e)
                {
                    LOG.error("Failed to send information to discord.bots.gg ", e);
                }
            });
        }
        else if (jda.getShardManager() != null)
        {
            totalGuilds = (int) jda.getShardManager().getGuildCache().size();
        }
        else
        {
            totalGuilds = (int) jda.getGuildCache().size();
        }
    }

    private void onMessageDelete(MessageDeleteEvent event)
    {
        // Check we are in a guild since there is no guild specific event now
        if (!event.isFromGuild()) return;

        // We don't need to cover whether or not this client usesLinkedDeletion() because
        // that is checked in onEvent(Event) before this is even called.
        synchronized(linkMap)
        {
            if(linkMap.contains(event.getMessageIdLong()))
            {
                Set<Message> messages = linkMap.get(event.getMessageIdLong());
                if(messages.size()>1 && event.getGuild().getSelfMember()
                        .hasPermission(event.getChannel().asTextChannel(), Permission.MESSAGE_MANAGE))
                    event.getChannel().asTextChannel().deleteMessages(messages).queue(unused -> {}, ignored -> {});
                else if(messages.size()>0)
                    messages.forEach(m -> m.delete().queue(unused -> {}, ignored -> {}));
            }
        }
    }

    private GuildSettingsProvider provideSettings(Guild guild)
    {
        Object settings = getSettingsFor(guild);
        if(settings instanceof GuildSettingsProvider) //implicit null check
            return (GuildSettingsProvider)settings;
        else
            return null;
    }

    /**
     * <b>DO NOT USE THIS!</b>
     *
     * <p>This is a method necessary for linking a bot's response messages
     * to their corresponding call message ID.
     * <br><b>Using this anywhere in your code can and will break your bot.</b>
     *
     * @param  callId
     *         The ID of the call Message
     * @param  message
     *         The Message to link to the ID
     */
    public void linkIds(long callId, Message message)
    {
        // We don't use linked deletion, so we don't do anything.
        if(!usesLinkedDeletion())
            return;

        synchronized(linkMap)
        {
            Set<Message> stored = linkMap.get(callId);
            if(stored != null)
                stored.add(message);
            else
            {
                stored = new HashSet<>();
                stored.add(message);
                linkMap.add(callId, stored);
            }
        }
    }

    private static class MessageParts {
        private final String prefixUsed;
        private final String command;
        private final String args;

        private MessageParts(String prefixUsed, String command, String args) {
            this.prefixUsed = prefixUsed;
            this.command = command;
            this.args = args;
        }
    }
}
