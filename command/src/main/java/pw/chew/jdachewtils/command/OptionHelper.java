package pw.chew.jdachewtils.command;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A collection of useful methods for working with Options.
 */
public final class OptionHelper {
    private OptionHelper() {}

    /**
     * Guarantees a String option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static String optString(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable String defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsString();
    }

    /**
     * Guarantees a boolean option value by providing a default value.
     *
     * @param event        the slash command event to get options from
     * @param option       the option we want
     * @param defaultValue if the option doesn't exist, what should we use instead?
     * @return the never-null option
     */
    public static boolean optBoolean(@NotNull SlashCommandEvent event, String option, boolean defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsBoolean();
    }

    /**
     * Guarantees a long option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    public static long optLong(@NotNull SlashCommandEvent event, @NotNull String option, long defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsLong();
    }

    /**
     * Guarantees a double option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    public static double optDouble(@NotNull SlashCommandEvent event, @NotNull String option, double defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsDouble();
    }

    /**
     * Guarantees a Guild Channel option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static GuildChannel optGuildChannel(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable GuildChannel defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsGuildChannel();
    }

    /**
     * Guarantees a Member option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static Member optMember(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable Member defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsMember();
    }

    /**
     * Guarantees a IMentionable option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static IMentionable optMentionable(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable IMentionable defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsMentionable();
    }

    /**
     * Guarantees a Role option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static Role optRole(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable Role defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsRole();
    }

    /**
     * Guarantees a User option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static User optUser(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable User defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsUser();
    }

    /**
     * Guarantees a MessageChannel option value by providing a default value.
     *
     * @param event        The slash command event to get options from
     * @param option       The option we want
     * @param defaultValue The fallback option in case of the absence of the option value
     * @return The provided option, or the default value if the option is not present
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    public static MessageChannel optMessageChannel(@NotNull SlashCommandEvent event, @NotNull String option, @Nullable MessageChannel defaultValue) {
        List<OptionMapping> options = event.getOptionsByName(option);

        return options.isEmpty() ? defaultValue : options.get(0).getAsMessageChannel();
    }

    /**
     * Checks to see if the event has an option.
     *
     * @param event  the slash command event to get options from
     * @param option the option we want
     * @return true if the option exists, false otherwise
     */
    public static boolean hasOption(@NotNull SlashCommandEvent event, @NotNull String option) {
        return !event.getOptionsByName(option).isEmpty();
    }
}
