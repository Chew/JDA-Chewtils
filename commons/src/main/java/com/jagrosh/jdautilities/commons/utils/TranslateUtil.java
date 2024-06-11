package com.jagrosh.jdautilities.commons.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Very basic translation lib that accepts properties of translations for different locales.
 * <br>
 * If you would like to use this for interactions, the locale MUST Be loaded before the CommandClient is built.
 *
 * @author Olivia
 */
public class TranslateUtil {
    public static final Map<DiscordLocale, Properties> LOCALES = new HashMap<>();
    private static DiscordLocale DEFAULT = DiscordLocale.ENGLISH_US;

    // Prevent instantiation
    private TranslateUtil() {}

    /**
     * Sets the default locale for the bot. By default, this is {@link DiscordLocale#ENGLISH_US}.
     *
     * @param locale The default locale
     */
    public static void setDefaultLocale(DiscordLocale locale) {
        DEFAULT = locale;
    }

    /**
     * Loads a locale file from the given path and adds it to the locale map.
     * The properties file should be loaded already.
     *
     * @param locale The locale to load
     * @param prop The loaded properties file
     */
    public static void addLocale(DiscordLocale locale, Properties prop) {
        LOCALES.put(locale, prop);
    }

    /**
     * Translates a string into the given server's locale.
     * Recommended for non-ephemeral responses. For ephemeral responses, use {@link #t(DiscordLocale, String)}.
     *
     * @param key The key to translate
     * @param server The server to translate for
     * @return The translated string
     */
    public static String t(Guild server, String key) {
        DiscordLocale locale = server.getLocale();

        String def = LOCALES.get(DEFAULT).getProperty(key, "No translation provided");

        String translated = LOCALES.getOrDefault(locale, LOCALES.get(DEFAULT)).getProperty(key, def);

        if (translated.equals(def)) {
            LoggerFactory.getLogger(TranslateUtil.class).warn("No translation provided for key {} in locale {}", key, locale);
        }

        return translated;
    }

    /**
     * Converts the following key to the provided locale. Will default to {@code DEFAULT} if no locale.<br>
     * Recommended use: {@code t("MY_KEY", event.getUserLocale())} on interactions.
     *
     * @param locale The locale to convert to
     * @param key The key to convert
     * @return The translated string
     */
    public static String t(DiscordLocale locale, String key) {
        return LOCALES.getOrDefault(locale, LOCALES.get(DEFAULT)).getProperty(key, LOCALES.get(DEFAULT).getProperty(key, "No translation provided"));
    }

    /**
     * Builds a locale map for the given key, used for command descriptions and options.
     * If there is no provided translation for a locale, it will not be included in the map.
     *
     * @param key The key to use for the command description
     * @return A map of locales to their respective descriptions
     */
    public static Map<DiscordLocale, String> buildLocaleMap(String key) {
        HashMap<DiscordLocale, String> locales = new HashMap<>();
        // default
        locales.put(DEFAULT, LOCALES.get(DEFAULT).getProperty(key, "No translation provided"));

        for (DiscordLocale locale : LOCALES.keySet()) {
            String translation = LOCALES.get(locale).getProperty(key);
            if (translation != null) {
                locales.put(locale, translation);
            }
        }

        return locales;
    }
}
