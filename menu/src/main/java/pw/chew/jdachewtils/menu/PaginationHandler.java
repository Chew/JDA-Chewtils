package pw.chew.jdachewtils.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Central class used for handling Pagination of the various Menus.
 * <br>Own menus can be added to this handler by using the {@link PaginationHandler.Builder Builder class}
 * for creating a new instance and then calling one of the various "displayX" methods available:
 * <ul>
 *     <li>{@link #displayWithButtons(MessageChannel, int)} will send a message with Buttons attached to it
 *     in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} and start the Menu.</li>
 *
 *     <li>{@link #displayWithReactions(MessageChannel, int)} will send a message in the provided
 *     {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}, add Reactions to it and start the Menu.</li>
 *
 *     <li>{@link #displayWithReactions(Message, int)} will edit the provided Message, add Reactions to it
 *     and start the Menu.</li>
 * </ul>
 */
public class PaginationHandler
{
    /**
     * The ⏪ emoji used for moving X pages to the left.
     */
    public static final String EMOJI_BULK_SKIP_LEFT  = "\u23EA";
    /**
     * The ◀ emoji used for moving one page to the left.
     */
    public static final String EMOJI_LEFT            = "\u25C0";
    /**
     * The ⏹ emoji used for stopping/closing the menu.
     */
    public static final String EMOJI_STOP            = "\u23F9";
    /**
     * The ▶ emoji used for moving one page to the right.
     */
    public static final String EMOJI_RIGHT           = "\u25B6";
    /**
     * The ⏩ emoji used for moving X pages to the right.
     */
    public static final String EMOJI_BULK_SKIP_RIGHT = "\u23E9";
    
    private final String BUTTON_ID_BULK_SKIP_LEFT  = "bulk_skip_left";
    private final String BUTTON_ID_LEFT            = "left";
    private final String BUTTON_ID_STOP            = "stop";
    private final String BUTTON_ID_RIGHT           = "right";
    private final String BUTTON_ID_BULK_SKIP_RIGHT = "bulk_skip_right";
    
    private final EventWaiter waiter;
    
    private final int totalPages;
    private final int bulkSkip;
    
    private final boolean allowSinglePage;
    private final boolean allowPageWrap;
    
    private final long time;
    private final TimeUnit unit;
    
    private final Consumer<Message> finalAction;
    
    private final Set<Long> allowedUserIds;
    private final Set<Long> allowedRoleIds;
    
    private final PaginationIcon bulkSkipLeftButton;
    private final PaginationIcon leftButton;
    private final PaginationIcon stopButton;
    private final PaginationIcon rightButton;
    private final PaginationIcon bulkSkipRightButton;
    
    private final String bulkSkipLeftName;
    private final String leftName;
    private final String stopName;
    private final String rightName;
    private final String bulkSkipRightName;
    
    private final List<MessageEmbed> embeds;
    
    public PaginationHandler() throws IllegalAccessException{
        throw new IllegalAccessException("Cannot initialize PaginationHandler directly! Use the Builder instead!");
    }
    
    private PaginationHandler(EventWaiter waiter, int totalPages, int bulkSkip, boolean allowSinglePage,
                              boolean allowPageWrap, long time, TimeUnit unit, Consumer<Message> finalAction,
                              Set<Long> allowedUserIds, Set<Long> allowedRoleIds, PaginationIcon bulkSkipLeftButton,
                              PaginationIcon leftButton, PaginationIcon stopButton, PaginationIcon rightButton,
                              PaginationIcon bulkSkipRightButton, List<MessageEmbed> embeds)
    {
        this.waiter = waiter;
        
        this.totalPages = totalPages;
        this.bulkSkip = bulkSkip;
        
        this.allowSinglePage = allowSinglePage;
        this.allowPageWrap = allowPageWrap;
        
        this.time = time;
        this.unit = unit;
        
        this.finalAction = finalAction;
        
        this.allowedUserIds = allowedUserIds;
        this.allowedRoleIds = allowedRoleIds;
        
        this.bulkSkipLeftButton = bulkSkipLeftButton;
        this.leftButton = leftButton;
        this.stopButton = stopButton;
        this.rightButton = rightButton;
        this.bulkSkipRightButton = bulkSkipRightButton;
        
        this.bulkSkipLeftName = bulkSkipLeftButton.getName();
        this.leftName = leftButton.getName();
        this.stopName = stopButton.getName();
        this.rightName = rightButton.getName();
        this.bulkSkipRightName = bulkSkipRightButton.getName();
        
        this.embeds = embeds;
    }
    
    /**
     * Creates a new instance of the PaginationHandler class with values taken from the {@link Builder Builder interface}.
     * 
     * @param  builder
     *         Class implementing the {@link Builder Builder interface} to use values from.
     *         
     * @return New instance of the PaginationHandler class to use.
     */
    public static PaginationHandler fromBuilder(Builder builder)
    {
        return new PaginationHandler(builder.getEventWaiter(), builder.getEmbeds().size(), builder.getBulkSkipNumber(),
            builder.allowSinglePage(), builder.allowPageWrap(), builder.getTime(), builder.getTimeUnit(),
            builder.getFinalAction(), builder.getAllowedUserIds(), builder.getAllowedRoleIds(),
            builder.getBulkSkipLeftIcon(), builder.getLeftIcon(), builder.getStopIcon(), builder.getRightIcon(),
            builder.getBulkSkipRightIcon(), builder.getEmbeds());
    }
    
    /**
     * Will send a Message in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} and
     * add Reactions to it, before handling the pagination.
     * <br>The provided page number will be set to 1 if it is lower than 1, or set to the total page count if going
     * above it.
     *
     * @param channel
     *        The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} to display the initial message in.
     * @param pageNumber
     *        The page at which the menu should start.
     */
    public void displayWithReactions(MessageChannel channel, int pageNumber)
    {
        Checks.notEmpty(embeds, "List of embeds cannot be null or empty.");
        
        pageNumber = setPageNumberLimit(pageNumber);
        
        channel.sendMessage(renderMessage(pageNumber)).queue(this::initReactions);
    }
    
    /**
     * Will edit the provided {@link net.dv8tion.jda.api.entities.Message Message} and add Reactions to it,
     * before handling the pagination.
     * <br>The provided page number will be set to 1 if it is lower than 1, or set to the total page count if going
     * above it.
     *
     * @param message
     *        The {@link net.dv8tion.jda.api.entities.Message Message} to edit and display the page on.
     * @param pageNumber
     *        The page at which the menu should start.
     */
    public void displayWithReactions(Message message, int pageNumber)
    {
        Checks.notEmpty(embeds, "List of embeds cannot be null or empty.");
        
        pageNumber = setPageNumberLimit(pageNumber);
        
        message.editMessage(renderMessage(pageNumber)).queue(this::initReactions);
    }
    
    /**
     * Will send a Message in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} and
     * add Reactions to it, before handling the pagination.
     * <br>The provided page number will be set to 1 if it is lower than 1, or set to the total page count if going
     * above it.
     *
     * @param channel
     *        The {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} to display the initial message in.
     * @param pageNumber
     *        The page at which the menu should start.
     */
    public void displayWithButtons(MessageChannel channel, int pageNumber)
    {
        Checks.notEmpty(embeds, "List of embeds cannot be null or empty.");
        
        pageNumber = setPageNumberLimit(pageNumber);
        
        initButtons(channel, pageNumber);
    }
    
    private int setPageNumberLimit(int pageNumber)
    {
        if(pageNumber < 1)
            return 1;
        
        return Math.min(pageNumber, totalPages);
        
    }
    
    private void initReactions(Message message)
    {
        if(totalPages > 1)
        {
            Set<RestAction<Void>> reactions = new HashSet<>();
            if(bulkSkip > 1 && bulkSkipLeftButton != null)
                reactions.add(message.addReaction(bulkSkipLeftButton.toString()));
            
            reactions.add(message.addReaction(leftButton.toString()));
            reactions.add(message.addReaction(stopButton.toString()));
            reactions.add(message.addReaction(rightButton.toString()));
            
            if(bulkSkip > 1 && bulkSkipRightButton != null)
                reactions.add(message.addReaction(bulkSkipRightButton.toString()));
            
            RestAction.allOf(reactions).queue();
        }
        else if(allowSinglePage)
        {
            message.addReaction(stopButton.toString()).queue();
        }
        else
        {
            finalAction.accept(message);
        }
    }
    
    private void initButtons(MessageChannel channel, int pageNumber)
    {
        MessageBuilder builder = new MessageBuilder(renderMessage(pageNumber));
        
        if(totalPages > 1)
        {
            Set<Button> buttons = new HashSet<>();
            if(bulkSkip > 1 && bulkSkipLeftButton != null)
                buttons.add(getButton(BUTTON_ID_BULK_SKIP_LEFT, bulkSkipLeftButton.getAsEmoji(), pageNumber));
            
            buttons.add(getButton(BUTTON_ID_LEFT, leftButton.getAsEmoji(), pageNumber));
            buttons.add(getButton(BUTTON_ID_STOP, stopButton.getAsEmoji(), pageNumber));
            buttons.add(getButton(BUTTON_ID_RIGHT, rightButton.getAsEmoji(), pageNumber));
            
            if(bulkSkip > 1 && bulkSkipRightButton != null)
                buttons.add(getButton(BUTTON_ID_BULK_SKIP_RIGHT, bulkSkipRightButton.getAsEmoji(), pageNumber));
            
            builder.setActionRows(ActionRow.of(buttons));
        }
        else if(allowSinglePage)
        {
            builder.setActionRows(ActionRow.of(getButton(BUTTON_ID_STOP, stopButton.getAsEmoji(), pageNumber)));
        }else{
            finalAction.accept(builder.build());
            return;
        }
        
        channel.sendMessage(renderMessage(pageNumber)).queue(m -> handleButtonPagination(m, waiter, pageNumber));
    }
    
    private Button getButton(String id, Emoji emoji, int pageNumber)
    {
        switch(id)
        {
            case BUTTON_ID_BULK_SKIP_LEFT:
            case BUTTON_ID_LEFT:
                return Button.primary(id, emoji).withDisabled(!allowPageWrap && pageNumber == 1);
            
            case BUTTON_ID_STOP:
                return Button.danger(id, emoji);
            
            case BUTTON_ID_RIGHT:
            case BUTTON_ID_BULK_SKIP_RIGHT:
                return Button.primary(id, emoji).withDisabled(!allowPageWrap && pageNumber == totalPages);
            
            default:
                throw new IllegalArgumentException("Unknown Button ID " + id);
        }
    }
    
    private void handleReactionPagination(Message message, EventWaiter waiter, int pageNumber)
    {
        waiter.waitForEvent(
            MessageReactionAddEvent.class,
            event -> isValidReaction(event, message.getIdLong()),
            event -> handleReactionEvent(event, message, pageNumber),
            time, unit, () -> finalAction.accept(message)
        );
    }
    
    private void handleButtonPagination(Message message, EventWaiter waiter, int pageNumber)
    {
        waiter.waitForEvent(
            ButtonClickEvent.class,
            event -> isValidButton(event, message.getIdLong()),
            event -> handleButtonEvent(event, message, pageNumber),
            time, unit, () -> finalAction.accept(message)
        );
    }
    
    private boolean isValidUser(@Nullable User user, @Nullable Guild guild)
    {
        if(user == null)
            return false;
        if(user.isBot())
            return false;
        if(allowedUserIds.isEmpty() && allowedRoleIds.isEmpty())
            return true;
        if(allowedUserIds.contains(user.getIdLong()))
            return true;
        if(guild == null || !guild.isMember(user))
            return false;
        
        Member member = guild.getMember(user);
        if(member == null)
            return false;
        
        return member.getRoles().stream().map(Role::getIdLong).anyMatch(allowedRoleIds::contains);
    }
    
    private boolean isValidReaction(MessageReactionAddEvent event, long messageId)
    {
        if(event.getMessageIdLong() != messageId)
            return false;
        
        String name = event.getReactionEmote().getName();
        if(name.equals(bulkSkipLeftName) || name.equals(bulkSkipRightName))
        {
            return bulkSkip > 1 && isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        }
        else if(name.equals(leftName) || name.equals(stopName) || name.equals(rightName))
        {
            return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        }
        else
        {
            return false;
        }
    }
    
    private boolean isValidButton(ButtonClickEvent event, long messageId)
    {
        if(event.getMessageIdLong() != messageId)
            return false;
        
        switch(event.getComponentId())
        {
            case BUTTON_ID_BULK_SKIP_LEFT:
            case BUTTON_ID_BULK_SKIP_RIGHT:
                return bulkSkip > 1 && isValidUser(event.getUser(), event.getGuild() != null ? event.getGuild() : null);
            
            case BUTTON_ID_LEFT:
            case BUTTON_ID_STOP:
            case BUTTON_ID_RIGHT:
                return isValidUser(event.getUser(), event.getGuild() != null ? event.getGuild() : null);
            
            default:
                return false;
        }
    }
    
    private void handleReactionEvent(MessageReactionAddEvent event, Message message, int pageNumber)
    {
        int newPageNumber = pageNumber;
        String name = event.getReactionEmote().getName();
        if(name.equals(bulkSkipLeftName))
        {
            if(newPageNumber > 1)
            {
                for(int i = 1; newPageNumber > 1 && i < bulkSkip; i++)
                {
                    newPageNumber--;
                }
            }
            else if(allowPageWrap)
            {
                newPageNumber = totalPages;
            }
        }
        else if(name.equals(leftName))
        {
            if(newPageNumber == 1 && allowPageWrap)
                newPageNumber = totalPages + 1;
            if(newPageNumber > 1)
                newPageNumber--;
        }
        else if(name.equals(stopName))
        {
            finalAction.accept(message);
            return;
        }
        else if(name.equals(rightName))
        {
            if(newPageNumber == totalPages && allowPageWrap)
                newPageNumber = 0;
            if(newPageNumber < totalPages)
                newPageNumber++;
        }
        else if(name.equals(bulkSkipRightName))
        {
            if(newPageNumber < totalPages)
            {
                for(int i = 1; newPageNumber < totalPages && i < bulkSkip; i++)
                {
                    newPageNumber++;
                }
            }
            else if(allowPageWrap)
            {
                newPageNumber = 1;
            }
        }
        
        if(event.isFromGuild() && event.getUser() != null)
        {
            if(event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
            {
                event.getReaction().removeReaction(event.getUser()).queue();
            }
        }
        
        int finalPageNumber = newPageNumber;
        message.editMessage(renderMessage(finalPageNumber)).queue(m -> handleReactionPagination(m, waiter, finalPageNumber));
    }
    
    private void handleButtonEvent(ButtonClickEvent event, Message message, int pageNumber)
    {
        int newPageNumber = pageNumber;
        switch(event.getComponentId())
        {
            case BUTTON_ID_BULK_SKIP_LEFT:
                if(newPageNumber > 1)
                {
                    for(int i = 1; newPageNumber > 1 && i < bulkSkip; i++)
                    {
                        newPageNumber--;
                    }
                }
                else if(allowPageWrap)
                {
                    newPageNumber = totalPages;
                }
                break;
            
            case BUTTON_ID_LEFT:
                if(newPageNumber == 1 && allowPageWrap)
                    newPageNumber = totalPages + 1;
                if(newPageNumber > 1)
                    newPageNumber--;
                break;
            
            case BUTTON_ID_STOP:
                finalAction.accept(message);
                return;
            
            case BUTTON_ID_RIGHT:
                if(newPageNumber == totalPages && allowPageWrap)
                    newPageNumber = 0;
                if(newPageNumber < totalPages)
                    newPageNumber++;
                break;
            
            case BUTTON_ID_BULK_SKIP_RIGHT:
                if(newPageNumber < totalPages)
                {
                    for(int i = 1; newPageNumber < totalPages && i < bulkSkip; i++)
                    {
                        newPageNumber++;
                    }
                }
                else if(allowPageWrap)
                {
                    newPageNumber = 1;
                }
                break;
        }
        
        if(!event.isAcknowledged())
            event.deferEdit().queue();
        
        int finalNewPage = newPageNumber;
        message.editMessage(renderMessage(finalNewPage)).queue(m -> handleButtonPagination(m, waiter, finalNewPage));
    }
    
    private Message renderMessage(int page)
    {
        MessageBuilder builder = new MessageBuilder();
        MessageEmbed embed = embeds.get(page - 1);
        builder.setEmbeds(embed);
        
        return builder.build();
    }
    
    /**
     * Builder class that can be used to create an instance of the {@link PaginationHandler PaginationHandler}.
     * <br>The goal of this class is to have a more central place for setting commonly used values such as
     * the emojis/emotes used for the Buttons/Reactions.
     *
     * <p>You can add your own Menu class by using this Builder in its creation process and then run the
     * {@link PaginationHandler#displayWithReactions(Message, int) displayWithReactions} or
     * {@link PaginationHandler#displayWithButtons(MessageChannel, int) displayWithButtons} methods.
     */
    public interface Builder
    {
        /**
         * Returns the currently set {@link EventWaiter EventWaiter} to use.
         * 
         * @return Currently set EventWaiter instance.
         */
        EventWaiter getEventWaiter();
        
        /**
         * Returns the number used to determine how many pages should be skipped when moving in bulk.
         * 
         * @return Integer returning the bulk skip number.
         */
        int getBulkSkipNumber();
        
        /**
         * Returns whether the Paginator should still handle a single page.
         * 
         * @return {@code true} if the Paginator should wait on a single page, otherwise {@code false}.
         */
        boolean allowSinglePage();
        
        /**
         * Returns whether the Paginator should "wrap around" (Go from last page to first or vice-versa).
         * 
         * @return {@code true} if the Paginator should go back to the start when reaching the end or vice-versa.
         */
        boolean allowPageWrap();
        
        /**
         * The amount of {@link #getTimeUnit() time units} of inactivity after which the Paginator should call the
         * {@link #getFinalAction() final action} to execute.
         * 
         * @return Long representing the time to wait before executing the final action.
         */
        long getTime();
        
        /**
         * The time unit to wait X of before executing the {@link #getFinalAction() final action}.
         * 
         * @return Type of TimeUnit to wait.
         */
        TimeUnit getTimeUnit();
        
        /**
         * Consumer of type {@link Message Message} that should be executed once the Paginator times out.
         * 
         * @return Currently set Consumer&lt;{@link Message Message}&gt; containing what the Paginator should do after
         *         a timeout.
         */
        Consumer<Message> getFinalAction();
        
        /**
         * Set of longs containing all User ids that should be allowed to interact with the current Menu.
         * 
         * @return Possibly-empty Set of User id longs.
         */
        Set<Long> getAllowedUserIds();
        
        /**
         * Set of longs containing all Role ids that should be allowed to interact with the current Menu.
         *
         * @return Possibly-empty Set of Role id longs.
         */
        Set<Long> getAllowedRoleIds();
        
        /**
         * Gives the currently set {@link PaginationIcon PaginationIcon} used for the "Bulk Skip Left" Button/Reaction.
         * 
         * @return {@link PaginationIcon PaginationIcon} of the "Bulk Skip Left" Button/Reaction.
         */
        PaginationIcon getBulkSkipLeftIcon();
    
        /**
         * Gives the currently set {@link PaginationIcon PaginationIcon} used for the "Left" Button/Reaction.
         *
         * @return {@link PaginationIcon PaginationIcon} of the "Left" Button/Reaction.
         */
        PaginationIcon getLeftIcon();
    
        /**
         * Gives the currently set {@link PaginationIcon PaginationIcon} used for the "Stop" Button/Reaction.
         *
         * @return {@link PaginationIcon PaginationIcon} of the "Stop" Button/Reaction.
         */
        PaginationIcon getStopIcon();
    
        /**
         * Gives the currently set {@link PaginationIcon PaginationIcon} used for the "Right" Button/Reaction.
         *
         * @return {@link PaginationIcon PaginationIcon} of the "Right" Button/Reaction.
         */
        PaginationIcon getRightIcon();
    
        /**
         * Gives the currently set {@link PaginationIcon PaginationIcon} used for the "Bulk Skip Right" Button/Reaction.
         *
         * @return {@link PaginationIcon PaginationIcon} of the "Bulk Skip Right" Button/Reaction.
         */
        PaginationIcon getBulkSkipRightIcon();
    
        /**
         * Gives a List of all currently set {@link MessageEmbed MessageEmbeds} to use in the Paginator.
         * 
         * @return List containing all currently set MessageEmbeds.
         */
        List<MessageEmbed> getEmbeds();
    
        /**
         * Creates a new Instance of the {@link PaginationHandler PaginationHandler} to use.
         * <br>This is equal to using {@code PaginationHandler handler = PaginationHandler.fromBuilder(this);} in a
         * class implementing this interface.
         * 
         * @return New instance of the {@link PaginationHandler PaginationHandler}.
         */
        PaginationHandler build();
        
        /**
         * Sets the {@link EventWaiter EventWaiter} to use for the Pagination.
         *
         * @param  waiter
         *         The EventWaiter instance to use
         *
         * @return This Builder for chaining convenience.
         */
        Builder setEventWaiter(EventWaiter waiter);
        
        /**
         * Sets the number of pages to skip when pressing the Bulk Skip buttons/reactions.
         * <br>Default is 1.
         *
         * @param  bulkSkipNumber
         *         The number to set for bulk skip.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipNumber(int bulkSkipNumber);
        
        /**
         * Set if the Paginator should allow single pages for menus. If set to {@code false} (default) will
         * the menu's {@link #setFinalAction(Consumer) final Action} get executed immediately!
         * <br>When set to {@code true} will the menu only contain a single Stop Button/Reaction to interact with.
         *
         * @param  singlePage
         *         Whether the Paginator should wait on a single page.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setAllowSinglePage(boolean singlePage);
        
        /**
         * Set if the Paginator should wrap around. By default, will the Paginator not advance when reaching the last
         * or first page of the menu.
         * <br>Setting this to true however, will the following actions happen:
         * <ul>
         *     <li>Pressing the right Button/Reaction while on the last page will move you to the first page</li>
         *
         *     <li>Pressing the left Button/Reaction while on the first page will move you to the last page</li>
         *
         *     <li>Using Bulk skip to the right while having less remaining pages than Bulk skip is skipping
         *     will move you to the first page.</li>
         *
         *     <li>Using Bulk skip to the left while having less remaining pages than Bulk skip is skipping
         *     will move you to the last page.</li>
         * </ul>
         *
         * @param  pageWrap
         *         Whether the Paginator should wrap around when reaching an end.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setAllowPageWrap(boolean pageWrap);
        
        /**
         * Sets the amount of time units to wait for the Pagination.
         * <br>If no interaction happens within the specified time will the Paginator call the 
         * {@link #setFinalAction(Consumer) final action} of the menu.
         *
         * @param  time
         *         The amount of TimeUnits to wait for interactions before cancelling.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setTime(long time);
        
        /**
         * Sets the TimeUnit to use for the {@link #setTime(long) specified time} to wait for an interaction.
         * <br>If no interaction happens within the specified time will the Paginator call the
         * {@link #setFinalAction(Consumer) final action} of the menu.
         * @param  timeUnit
         *         The TimeUnit to use for the specified time.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setTimeUnit(TimeUnit timeUnit);
        
        /**
         * Sets the final action that should be performed once the menu times out.
         * <br>Default is deleting the message.
         *
         * @param  finalAction
         *         A Consumer of type Message to set the final action.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setFinalAction(Consumer<Message> finalAction);
        
        /**
         * Adds the provided Array of Longs as allowed User ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User's id matches any of the allowed ones in the set.
         *
         * @param  userIds
         *         Array of Longs that should be added as allowed User ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder addAllowedUserIds(Long... userIds);
        
        /**
         * Adds the provided Collection of Longs as allowed User ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User's id matches any of the allowed ones in the set.
         *
         * @param  userIds
         *         Collection of Longs that should be added as allowed User ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder addAllowedUserIds(Collection<Long> userIds);
        
        /**
         * Clears the current Set and adds the provided Array of Longs as allowed User ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User's id matches any of the allowed ones in the set.
         *
         * @param  userIds
         *         Array of Longs that should be set as allowed User ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setAllowedUserIds(Long... userIds);
        
        /**
         * Clears the current Set and adds the provided Collection of Longs as allowed User ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User's id matches any of the allowed ones in the set.
         *
         * @param  userIds
         *         Collection of Longs that should be set as allowed User ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setAllowedUserIds(Collection<Long> userIds);
        
        /**
         * Adds the provided Array of Longs as allowed Role Ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User has a role with a matching id.
         *
         * @param  roleIds
         *         Array of Longs that should be added as allowed Role ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder addAllowedRoleIds(Long... roleIds);
        
        /**
         * Adds the provided Collection of Longs as allowed Role Ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User has a role with a matching id.
         *
         * @param  roleIds
         *         Collection of Longs that should be added as allowed Role ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder addAllowedRoleIds(Collection<Long> roleIds);
        
        /**
         * Clears the current Set and adds the provided Array of Longs as allowed Role Ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User has a role with a matching id.
         *
         * @param  roleIds
         *         Array of Longs that should be set as allowed Role ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setAllowedRoleIds(Long... roleIds);
        
        /**
         * Clears the current Set and adds the provided Collection of Longs as allowed Role Ids.
         * <br>If <i>any</i> amount of Longs has been set will the PaginationHandler only update the page if the
         * User has a role with a matching id.
         *
         * @param  roleIds
         *         Collection of Longs that should be set as allowed Role ids.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setAllowedRoleIds(Collection<Long> roleIds);
        
        /**
         * Sets the Emoji used for the Bulk Skip Left Button/Reaction.
         * <br>The provided String is treated as Unicode. Use {@link #setBulkSkipLeft(String, long, boolean)}
         * or {@link #setBulkSkipLeft(Emote)} for custom Discord Emotes.
         *
         * @param  unicode
         *         The unicode Emoji to use.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipLeft(String unicode);
        
        /**
         * Sets the emote used for the Bulk Skip Left Button/Reaction.
         * <br>Use {@link #setBulkSkipLeft(String)} for unicode emojis.
         *
         * @param  name
         *         The name of the emote
         * @param  id
         *         The id of the emote
         * @param  animated
         *         If the emote is actually animated or not
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipLeft(String name, long id, boolean animated);
        
        /**
         * Sets the emote used for the Bulk Skip Left Button/Reaction.
         * <br>Use {@link #setBulkSkipLeft(String)} for unicode emojis.
         *
         * @param  emote
         *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipLeft(Emote emote);
        
        /**
         * Sets the emoji to use for the Left Button/Reaction.
         * <br>The provided String is treated as Unicode. Use {@link #setLeft(String, long, boolean)}
         * or {@link #setLeft(Emote)} for custom Discord emotes.
         *
         * @param  unicode
         *         The unicode Emoji to use.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setLeft(String unicode);
        
        /**
         * Sets the emote used for the Left Button/Reaction.
         * <br>Use {@link #setLeft(String)} for unicode emojis.
         *
         * @param  name
         *         The name of the emote
         * @param  id
         *         The id of the emote
         * @param  animated
         *         If the emote is actually animated or not
         *
         * @return This Builder for chaining convenience.
         */
        Builder setLeft(String name, long id, boolean animated);
        
        /**
         * Sets the emote used for the Left Button/Reaction.
         * <br>Use {@link #setLeft(String)} for unicode emojis.
         *
         * @param  emote
         *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
         *
         * @return This Builder for chaining convenience.
         */
        Builder setLeft(Emote emote);
        
        /**
         * Sets the emoji to use for the Stop Button/Reaction.
         * <br>The provided String is treated as Unicode. Use {@link #setStop(String, long, boolean)}
         * or {@link #setStop(Emote)} for custom Discord emotes.
         *
         * @param  unicode
         *         The unicode Emoji to use.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setStop(String unicode);
        
        /**
         * Sets the emote used for the Stop Button/Reaction.
         * <br>Use {@link #setStop(String)} for unicode emojis.
         *
         * @param  name
         *         The name of the emote
         * @param  id
         *         The id of the emote
         * @param  animated
         *         If the emote is actually animated or not
         *
         * @return This Builder for chaining convenience.
         */
        Builder setStop(String name, long id, boolean animated);
        
        /**
         * Sets the emote used for the Stop Button/Reaction.
         * <br>Use {@link #setStop(String)} for unicode emojis.
         *
         * @param  emote
         *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
         *
         * @return This Builder for chaining convenience.
         */
        Builder setStop(Emote emote);
        
        /**
         * Sets the emoji to use for the Right Button/Reaction.
         * <br>The provided String is treated as Unicode. Use {@link #setRight(String, long, boolean)}
         * or {@link #setRight(Emote)} for custom Discord emotes.
         *
         * @param  unicode
         *         The unicode Emoji to use.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setRight(String unicode);
        
        /**
         * Sets the emote used for the Right Button/Reaction.
         * <br>Use {@link #setRight(String)} for unicode emojis.
         *
         * @param  name
         *         The name of the emote
         * @param  id
         *         The id of the emote
         * @param  animated
         *         If the emote is actually animated or not
         *
         * @return This Builder for chaining convenience.
         */
        Builder setRight(String name, long id, boolean animated);
        
        /**
         * Sets the emote used for the Right Button/Reaction.
         * <br>Use {@link #setRight(String)} for unicode emojis.
         *
         * @param  emote
         *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
         *
         * @return This Builder for chaining convenience.
         */
        Builder setRight(Emote emote);
        
        /**
         * Sets the emoji to use for the Bulk Skip Right Button/Reaction.
         * <br>The provided String is treated as Unicode. Use {@link #setBulkSkipRight(String, long, boolean)}
         * or {@link #setBulkSkipRight(Emote)} for custom Discord emotes.
         *
         * @param  unicode
         *         The unicode Emoji to use.
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipRight(String unicode);
        
        /**
         * Sets the emote used for the Bulk Skip Right Button/Reaction.
         * <br>Use {@link #setBulkSkipRight(String)} for unicode emojis.
         *
         * @param  name
         *         The name of the emote
         * @param  id
         *         The id of the emote
         * @param  animated
         *         If the emote is actually animated or not
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipRight(String name, long id, boolean animated);
        
        /**
         * Sets the emote used for the Bulk Skip Right Button/Reaction.
         * <br>Use {@link #setBulkSkipRight(String)} for unicode emojis.
         *
         * @param  emote
         *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
         *
         * @return This Builder for chaining convenience.
         */
        Builder setBulkSkipRight(Emote emote);
    
        /**
         * Adds the provided Array of embeds to the current List of set Embeds.
         * 
         * @param  embeds
         *         The MessageEmbeds to add to the list.
         *
         * @return This Builder for chaining convenience.
         */
        Builder addEmbeds(MessageEmbed... embeds);
    
        /**
         * Adds the provided Collection of embeds to the current List of set Embeds.
         *
         * @param  embeds
         *         The MessageEmbeds to add to the list.
         *
         * @return This Builder for chaining convenience.
         */
        Builder addEmbeds(Collection<MessageEmbed> embeds);
        
        /**
         * Sets the {@link MessageEmbed embeds} that the paginator should use for the menu.
         * <br>The provided list cannot be null nor empty!
         *
         * @param  embeds
         *         List of {@link MessageEmbed embeds} to use for the Paginator
         *
         * @return This Builder for chaining convenience.
         */
        Builder setEmbeds(Collection<MessageEmbed> embeds);
    }
    
    /**
     * Convenience class to create and handle Emojis and Emotes for both Buttons and Reactions.
     * <br>Note that in case of unicode Emojis, only actual unicode (\u1234) or Codepoint (U+1234) are supported!
     * 
     * <p>To create a new instance, use one of the available {@code fromX} methods:
     * <ul>
     *     <li>{@link #fromUnicode(String) fromUnicode(String)} for normal Unicode Emojis</li>
     *     
     *     <li>{@link #fromEmote(String, long, boolean) fromEmote(String, long, boolean)} or
     *     {@link #fromEmote(Emote) fromEmote(Emote)} for Discord's custom Emojis</li>
     * </ul>
     */
    public static class PaginationIcon
    {
        private final String name;
        private final long id;
        private final boolean animated;
        
        private PaginationIcon(String name)
        {
            this(name, 0L, false);
        }
        
        private PaginationIcon(String name, long id, boolean animated)
        {
            this.name = name;
            this.id = id;
            this.animated = animated;
        }
    
        /**
         * Creates a new instance of this class with the provided String being treated as unicode.
         * <br>Supported are the common/default unicode format (\u1234) and Codepoint (U+1234).
         * 
         * <p>This will set the id to {@code 0L} and the animated boolean to {@code false}.
         * 
         * @param  unicode
         *         The unicode to use.
         * 
         * @return New instance of the PaginationIcon class with the provided String.
         */
        public static PaginationIcon fromUnicode(String unicode)
        {
            return new PaginationIcon(unicode);
        }
    
        /**
         * Creates a new instance of this class with the provided String, long and boolean.
         * <br>The values will be seen as part of a {@link Emote Discord custom Emote} with the String being
         * the name, the long being the id and the boolean determining if it is animated.
         * 
         * <p>For your convenience is there also a {@link #fromEmote(Emote) fromEmote(Emote)} variant available.
         * 
         * @param  name
         *         The name of the Emote.
         * @param  id
         *         The id of the Emote.
         * @param  animated
         *         Whether the Emote is animated.
         * 
         * @return New instance of the PaginationIcon class with the provided values set.
         */
        public static PaginationIcon fromEmote(String name, long id, boolean animated)
        {
            return new PaginationIcon(name, id, animated);
        }
    
        /**
         * Creates a new instance of this class with the provided {@link Emote Emote}.
         * <br>This is equal to using
         * {@link #fromEmote(String, long, boolean) fromEmote(emote.getName(), emote.getId(), emote.isAnimated())}.
         * 
         * @param  emote
         *         The Emote to use values of.
         * 
         * @return New instance of the PaginationIcon class with the provided Emote values set.
         */
        public static PaginationIcon fromEmote(Emote emote)
        {
            return new PaginationIcon(emote.getName(), emote.getIdLong(), emote.isAnimated());
        }
    
        /**
         * Returns an {@link Emoji Emoji instance} which can be used in Buttons.
         * <br><b>Emojis can NOT be used for Reactions! Use {@link #toString()} instead.</b>
         * 
         * @return new {@link Emoji Emoji instance} that can be used for Buttons.
         */
        public Emoji getAsEmoji()
        {
            if(id == 0L)
                return Emoji.fromUnicode(name);
            
            return Emoji.fromEmote(name, id, animated);
        }
        
        public String getName()
        {
            return name;
        }
    
        /**
         * Returns either the Unicode of the Emoji, if id is 0, or the custom Emote in the format "a?:name:id".
         * <br>This String can be used for Reactions, but not for Buttons.
         * 
         * @return String representing either the unicode emoji or a custom one.
         */
        @Override
        public String toString()
        {
            if(id == 0L)
                return name;
            
            return String.format("%s:%s:%d", animated ? "a" : "", name, id);
        }
    }
}
