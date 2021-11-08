package com.jagrosh.jdautilities.menu;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PaginationHandler
{
    public static final String EMOJI_BULK_SKIP_LEFT  = "\u23EA"; // ⏪
    public static final String EMOJI_LEFT            = "\u25C0"; // ◀
    public static final String EMOJI_STOP            = "\u23F9"; // ⏹
    public static final String EMOJI_RIGHT           = "\u25B6"; // ▶
    public static final String EMOJI_BULK_SKIP_RIGHT = "\u23E9"; // ⏩
    
    public static final String BUTTON_ID_BULK_SKIP_LEFT  = "bulk_skip_left";
    public static final String BUTTON_ID_LEFT            = "left";
    public static final String BUTTON_ID_STOP            = "stop";
    public static final String BUTTON_ID_RIGHT           = "right";
    public static final String BUTTON_ID_BULK_SKIP_RIGHT = "bulk_skip_right";
    
    private final Menu menu;
    private final int totalPages;
    private final int bulkSkip;
    private final boolean allowSinglePage;
    private final boolean allowPageWrap;
    private final long time;
    private final TimeUnit unit;
    private final Consumer<Message> finalAction;
    
    private final PaginatorButton bulkSkipLeftButton;
    private final PaginatorButton leftButton;
    private final PaginatorButton stopButton;
    private final PaginatorButton rightButton;
    private final PaginatorButton bulkSkipRightButton;
    
    private final String bulkSkipLeftName;
    private final String leftName;
    private final String stopName;
    private final String rightName;
    private final String bulkSkipRightName;
    
    private final List<MessageEmbed> embeds;
    
    public PaginationHandler() throws IllegalAccessException{
        throw new IllegalAccessException("Cannot initialize PaginationHandler directly! Use the Builder instead!");
    }
    
    private PaginationHandler(Menu menu, int totalPages, int bulkSkip, boolean allowSinglePage,
                              boolean allowPageWrap, long time, TimeUnit unit, Consumer<Message> finalAction,
                              PaginatorButton bulkSkipLeftButton, PaginatorButton leftButton, PaginatorButton stopButton,
                              PaginatorButton rightButton, PaginatorButton bulkSkipRightButton, List<MessageEmbed> embeds)
    {
        this.menu = menu;
        this.totalPages = totalPages;
        this.bulkSkip = bulkSkip;
        this.allowSinglePage = allowSinglePage;
        this.allowPageWrap = allowPageWrap;
        this.time = time;
        this.unit = unit;
        this.finalAction = finalAction;
    
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
    
    public void displayWithReactions(MessageChannel channel, int pageNumber)
    {
        channel.sendMessage(renderMessage(pageNumber)).queue(m -> displayWithReactions(m, pageNumber));
    }
    
    public void displayWithReactions(Message message, int pageNumber)
    {
        Checks.notEmpty(embeds, "List of embeds cannot be null or empty.");
        pageNumber = setPageNumberLimit(pageNumber);
        
        initReactions(message, pageNumber);
    }
    
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
    
    private void initReactions(Message message, int pageNumber)
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
        
        channel.sendMessage(builder.build()).queue(m -> handleButtonPagination(m, menu.waiter, pageNumber));
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
    
    private boolean isValidReaction(MessageReactionAddEvent event, long messageId)
    {
        if(event.getMessageIdLong() != messageId)
            return false;
    
        String name = event.getReactionEmote().getName();
        if(name.equals(bulkSkipLeftName) || name.equals(bulkSkipRightName))
        {
            return bulkSkip > 1 && menu.isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        }
        else if(name.equals(leftName) || name.equals(stopName) || name.equals(rightName))
        {
            return menu.isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
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
                return bulkSkip > 1 && menu.isValidUser(event.getUser(), event.getGuild() != null ? event.getGuild() : null);
            
            case BUTTON_ID_LEFT:
            case BUTTON_ID_STOP:
            case BUTTON_ID_RIGHT:
                return menu.isValidUser(event.getUser(), event.getGuild() != null ? event.getGuild() : null);
            
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
        message.editMessage(renderMessage(finalPageNumber)).queue(m -> handleReactionPagination(m, menu.waiter, finalPageNumber));
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
        message.editMessage(renderMessage(finalNewPage)).queue(m -> handleButtonPagination(m, menu.waiter, finalNewPage));
    }
    
    private Message renderMessage(int page)
    {
        MessageBuilder builder = new MessageBuilder();
        MessageEmbed embed = embeds.get(page - 1);
        builder.setEmbeds(embed);
        
        return builder.build();
    }
    
    public static class Builder
    {
        private final Menu menu;
        
        private int totalPages = 0;
        private int bulkSkip = 1;
        
        private boolean singlePage = false;
        private boolean pageWrap = false;
        
        private long time;
        private TimeUnit timeUnit;
        
        private Consumer<Message> finalAction = m -> m.delete().queue();
        
        private PaginatorButton bulkSkipLeftEmoji = null;
        private PaginatorButton leftEmoji = PaginatorButton.fromUnicode(EMOJI_LEFT);
        private PaginatorButton stopEmoji = PaginatorButton.fromUnicode(EMOJI_STOP);
        private PaginatorButton rightEmoji = PaginatorButton.fromUnicode(EMOJI_RIGHT);
        private PaginatorButton bulkSkipRightEmoji = null;
        
        private List<MessageEmbed> embeds;
        
        public Builder(Menu menu)
        {
            Checks.notNull(menu, "Menu may not be null.");
            
            this.menu = menu;
            this.time = menu.timeout;
            this.timeUnit = menu.unit;
        }
        
        public PaginationHandler build()
        {
            Checks.notNull(menu.waiter, "EventWaiter cannot be null.");
            
            return new PaginationHandler(
                menu, totalPages, bulkSkip, singlePage, pageWrap, time, timeUnit, finalAction,
                bulkSkipLeftEmoji, leftEmoji, stopEmoji, rightEmoji, bulkSkipRightEmoji, embeds
            );
        }
        
        public Builder setTotalPages(int totalPages)
        {
            Checks.check(totalPages > 0, "Amount of pages cannot be less than 1.");
            
            this.totalPages = totalPages;
            return this;
        }
        
        public Builder setBulkSkipNumber(int bulkSkip)
        {
            Checks.check(bulkSkip >= 0, "BulkSkip number cannot be less than 0.");
            
            this.bulkSkip = bulkSkip;
            return this;
        }
        
        public Builder allowSinglePage(boolean singlePage)
        {
            this.singlePage = singlePage;
            return this;
        }
        
        public Builder allowPageWrap(boolean pageWrap)
        {
            this.pageWrap = pageWrap;
            return this;
        }
        
        public Builder setTime(long time)
        {
            Checks.check(time > 0, "Time cannot be less than 1.");
            
            this.time = time;
            return this;
        }
        
        public Builder setTimeUnit(TimeUnit timeUnit)
        {
            Checks.notNull(timeUnit, "TimeUnit may not be null.");
            
            this.timeUnit = timeUnit;
            return this;
        }
        
        public Builder setFinalAction(Consumer<Message> finalAction)
        {
            Checks.notNull(finalAction, "FinalAction cannot be null.");
            
            this.finalAction = finalAction;
            return this;
        }
        
        public Builder setBulkSkipLeft(String unicode)
        {
            this.bulkSkipLeftEmoji = PaginatorButton.fromUnicode(unicode);
            return this;
        }
        
        public Builder setBulkSkipLeft(String name, long id, boolean animated)
        {
            this.bulkSkipLeftEmoji = PaginatorButton.fromEmote(name, id, animated);
            return this;
        }
        
        public Builder setBulkSkipLeft(Emote emote)
        {
            this.bulkSkipLeftEmoji = PaginatorButton.fromEmote(emote);
            return this;
        }
    
        public Builder setLeft(String unicode)
        {
            this.leftEmoji = PaginatorButton.fromUnicode(unicode);
            return this;
        }
    
        public Builder setLeft(String name, long id, boolean animated)
        {
            this.leftEmoji = PaginatorButton.fromEmote(name, id, animated);
            return this;
        }
    
        public Builder setLeft(Emote emote)
        {
            this.leftEmoji = PaginatorButton.fromEmote(emote);
            return this;
        }
    
        public Builder setStop(String unicode)
        {
            this.stopEmoji = PaginatorButton.fromUnicode(unicode);
            return this;
        }
    
        public Builder setStop(String name, long id, boolean animated)
        {
            this.stopEmoji = PaginatorButton.fromEmote(name, id, animated);
            return this;
        }
    
        public Builder setStop(Emote emote)
        {
            this.stopEmoji = PaginatorButton.fromEmote(emote);
            return this;
        }
    
        public Builder setRight(String unicode)
        {
            this.rightEmoji = PaginatorButton.fromUnicode(unicode);
            return this;
        }
    
        public Builder setRight(String name, long id, boolean animated)
        {
            this.rightEmoji = PaginatorButton.fromEmote(name, id, animated);
            return this;
        }
    
        public Builder setRight(Emote emote)
        {
            this.rightEmoji = PaginatorButton.fromEmote(emote);
            return this;
        }
    
        public Builder setBulkSkipRight(String unicode)
        {
            this.bulkSkipRightEmoji = PaginatorButton.fromUnicode(unicode);
            return this;
        }
    
        public Builder setBulkSkipRight(String name, long id, boolean animated)
        {
            this.bulkSkipRightEmoji = PaginatorButton.fromEmote(name, id, animated);
            return this;
        }
    
        public Builder setBulkSkipRight(Emote emote)
        {
            this.bulkSkipRightEmoji = PaginatorButton.fromEmote(emote);
            return this;
        }
        
        public Builder setEmbeds(List<MessageEmbed> embeds)
        {
            Checks.notEmpty(embeds, "Embeds cannot be null nor empty.");
            
            this.embeds = embeds;
            return this;
        }
    }
    
    private static class PaginatorButton
    {
        private final String name;
        private final long id;
        private final boolean animated;
        
        private PaginatorButton(String name)
        {
            this(name, 0L, false);
        }
        
        private PaginatorButton(String name, long id, boolean animated)
        {
            this.name = name;
            this.id = id;
            this.animated = animated;
        }
        
        public static PaginatorButton fromUnicode(String unicode)
        {
            return new PaginatorButton(unicode);
        }
        
        public static PaginatorButton fromEmote(String name, long id, boolean animated)
        {
            return new PaginatorButton(name, id, animated);
        }
        
        public static PaginatorButton fromEmote(Emote emote)
        {
            return new PaginatorButton(emote.getName(), emote.getIdLong(), emote.isAnimated());
        }
        
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
        
        @Override
        public String toString()
        {
            if(id == 0L)
                return name;
            
            return String.format("%s:%s:%d", animated ? "a" : "", name, id);
        }
    }
}
