package com.jagrosh.jdautilities.command;

/**
 * A series of {@link Enum}s used for defining the scope size for a
 * {@link Interaction}'s cooldown.
 *
 * <p>The purpose for these values is to allow easy, refined, and generally convenient keys
 * for cooldown scopes, allowing a command to remain on cooldown for more than just the user
 * calling it, with no unnecessary abstraction or developer input.
 *
 * Cooldown keys are generated via {@link com.jagrosh.jdautilities.command.Command#getCooldownKey(CommandEvent)
 * Command#getCooldownKey(CommandEvent)} using 1-2 Snowflake ID's corresponding to the name
 * (IE: {@code USER_CHANNEL} uses the ID's of the User and the Channel from the CommandEvent).
 *
 * <p>However, the issue with generalizing and generating like this is that the command may
 * be called in a non-guild environment, causing errors internally.
 * <br>To prevent this, all of the values that contain "{@code GUILD}" in their name default
 * to their "{@code CHANNEL}" counterparts when commands using them are called outside of a
 * {@link net.dv8tion.jda.api.entities.Guild Guild} environment.
 * <ul>
 *     <li>{@link CooldownScope#GUILD GUILD} defaults to
 *     {@link CooldownScope#CHANNEL CHANNEL}.</li>
 *     <li>{@link CooldownScope#USER_GUILD USER_GUILD} defaults to
 *     {@link CooldownScope#USER_CHANNEL USER_CHANNEL}.</li>
 * </ul>
 *
 * These are effective across a single instance of JDA, and not multiple
 * ones, save when multiple shards run on a single JVM and under a
 * {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager}.
 * <br>There is no shard magic, and no guarantees for a 100% "global"
 * cooldown, unless all shards of the bot run under the same ShardManager,
 * and/or via some external system unrelated to JDA-Utilities.
 *
 * @since  1.3
 * @author Kaidan Gustave
 *
 * @see    com.jagrosh.jdautilities.command.Command#cooldownScope Command.cooldownScope
 */
public enum CooldownScope
{
    /**
     * Applies the cooldown to the calling {@link net.dv8tion.jda.api.entities.User User} across all
     * locations on this instance (IE: TextChannels, PrivateChannels, etc).
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|U:<userID>}
     * </ul>
     */
    USER("U:%d",""),

    /**
     * Applies the cooldown to the {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} the
     * command is called in.
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|C:<channelID>}
     * </ul>
     */
    CHANNEL("C:%d","in this channel"),

    /**
     * Applies the cooldown to the calling {@link net.dv8tion.jda.api.entities.User User} local to the
     * {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} the command is called in.
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|U:<userID>|C:<channelID>}
     * </ul>
     */
    USER_CHANNEL("U:%d|C:%d", "in this channel"),

    /**
     * Applies the cooldown to the {@link net.dv8tion.jda.api.entities.Guild Guild} the command is called in.
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|G:<guildID>}
     * </ul>
     *
     * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#CHANNEL CooldownScope.CHANNEL}
     * when called in a private channel.  This is done in order to prevent internal
     * {@link java.lang.NullPointerException NullPointerException}s from being thrown while generating cooldown keys!
     */
    GUILD("G:%d", "in this server"),

    /**
     * Applies the cooldown to the calling {@link net.dv8tion.jda.api.entities.User User} local to the
     * {@link net.dv8tion.jda.api.entities.Guild Guild} the command is called in.
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|U:<userID>|G:<guildID>}
     * </ul>
     *
     * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#CHANNEL CooldownScope.CHANNEL}
     * when called in a private channel. This is done in order to prevent internal
     * {@link java.lang.NullPointerException NullPointerException}s from being thrown while generating cooldown keys!
     */
    USER_GUILD("U:%d|G:%d", "in this server"),

    /**
     * Applies the cooldown to the calling Shard the command is called on.
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|S:<shardID>}
     * </ul>
     *
     * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#GLOBAL CooldownScope.GLOBAL}
     * when {@link net.dv8tion.jda.api.JDA#getShardInfo() JDA#getShardInfo()} returns {@code null}.
     * This is done in order to prevent internal {@link java.lang.NullPointerException NullPointerException}s
     * from being thrown while generating cooldown keys!
     */
    SHARD("S:%d", "on this shard"),

    /**
     * Applies the cooldown to the calling {@link net.dv8tion.jda.api.entities.User User} on the Shard
     * the command is called on.
     *
     * <p>The key for this is generated in the format
     * <ul>
     *     {@code <command-name>|U:<userID>|S:<shardID>}
     * </ul>
     *
     * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#USER CooldownScope.USER}
     * when {@link net.dv8tion.jda.api.JDA#getShardInfo() JDA#getShardInfo()} returns {@code null}.
     * This is done in order to prevent internal {@link java.lang.NullPointerException NullPointerException}s
     * from being thrown while generating cooldown keys!
     */
    USER_SHARD("U:%d|S:%d", "on this shard"),

    /**
     * Applies this cooldown globally.
     *
     * <p>As this implies: the command will be unusable on the instance of JDA in all types of
     * {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}s until the cooldown has ended.
     *
     * <p>The key for this is {@code <command-name>|globally}
     */
    GLOBAL("Global", "globally");

    private final String format;
    final String errorSpecification;

    CooldownScope(String format, String errorSpecification)
    {
        this.format = format;
        this.errorSpecification = errorSpecification;
    }

    String genKey(String name, long id)
    {
        return genKey(name, id, -1);
    }

    String genKey(String name, long idOne, long idTwo)
    {
        if(this.equals(GLOBAL))
            return name+"|"+format;
        else if(idTwo==-1)
            return name+"|"+String.format(format,idOne);
        else return name+"|"+String.format(format,idOne,idTwo);
    }
}
