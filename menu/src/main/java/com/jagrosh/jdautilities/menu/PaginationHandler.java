package com.jagrosh.jdautilities.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.HashSet;
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
    private final int time;
    private final TimeUnit unit;
    private final Consumer<Message> finalAction;
    
    private final String bulkSkipLeftEmoji;
    private final String leftEmoji;
    private final String stopEmoji;
    private final String rightEmoji;
    private final String bulkSkipRightEmoji;
    
    
    public PaginationHandler(Menu menu, int totalPages, int bulkSkip, boolean allowSinglePage,
                             boolean allowPageWrap, int time, TimeUnit unit, Consumer<Message> finalAction)
    {
        this.menu = menu;
        this.totalPages = totalPages;
        this.bulkSkip = bulkSkip;
        this.allowSinglePage = allowSinglePage;
        this.allowPageWrap = allowPageWrap;
        this.time = time;
        this.unit = unit;
        this.finalAction = finalAction;
        
        this.bulkSkipLeftEmoji = EMOJI_BULK_SKIP_LEFT;
        this.leftEmoji = EMOJI_LEFT;
        this.stopEmoji = EMOJI_STOP;
        this.rightEmoji = EMOJI_RIGHT;
        this.bulkSkipRightEmoji = EMOJI_BULK_SKIP_RIGHT;
    }
    
    public PaginationHandler(Menu menu, int totalPages, int bulkSkip, boolean allowSinglePage,
                             boolean allowPageWrap, int time, TimeUnit unit, Consumer<Message> finalAction,
                             String bulkSkipLeftEmoji, String leftEmoji, String stopEmoji, String rightEmoji,
                             String bulkSkipRightEmoji)
    {
        this.menu = menu;
        this.totalPages = totalPages;
        this.bulkSkip = bulkSkip;
        this.allowSinglePage = allowSinglePage;
        this.allowPageWrap = allowPageWrap;
        this.time = time;
        this.unit = unit;
        this.finalAction = finalAction;
    
        this.bulkSkipLeftEmoji = bulkSkipLeftEmoji;
        this.leftEmoji = leftEmoji;
        this.stopEmoji = stopEmoji;
        this.rightEmoji = rightEmoji;
        this.bulkSkipRightEmoji = bulkSkipRightEmoji;
    }
    
    public void displayWithButtons(MessageChannel channel, int pageNumber)
    {
        pageNumber = setPageNumberLimit(pageNumber);
        
        initButtons(channel, pageNumber);
    }
    
    private int setPageNumberLimit(int pageNumber)
    {
        if(pageNumber < 1)
            return 1;
        
        return Math.min(pageNumber, totalPages);
    
    }
    
    private void initButtons(MessageChannel channel, int pageNumber)
    {
        MessageBuilder builder = new MessageBuilder(menu.getMessage(pageNumber));
        
        if(totalPages > 1)
        {
            Set<Button> buttons = new HashSet<>();
            if(bulkSkip > 1)
                buttons.add(getButton(BUTTON_ID_BULK_SKIP_LEFT, bulkSkipLeftEmoji, pageNumber));
            
            buttons.add(getButton(BUTTON_ID_LEFT, leftEmoji, pageNumber));
            buttons.add(getButton(BUTTON_ID_STOP, stopEmoji, pageNumber));
            buttons.add(getButton(BUTTON_ID_RIGHT, rightEmoji, pageNumber));
            
            if(bulkSkip > 1)
                buttons.add(getButton(BUTTON_ID_BULK_SKIP_RIGHT, bulkSkipRightEmoji, pageNumber));
            
            builder.setActionRows(ActionRow.of(buttons));
        }
        else if(allowSinglePage)
        {
            builder.setActionRows(ActionRow.of(getButton(BUTTON_ID_STOP, stopEmoji, pageNumber)));
        }else{
            finalAction.accept(builder.build());
            return;
        }
        
        channel.sendMessage(builder.build()).queue(m -> handleButtonPagination(m, menu.waiter, pageNumber));
    }
    
    private Button getButton(String id, String emoji, int pageNumber)
    {
        switch(id)
        {
            case BUTTON_ID_BULK_SKIP_LEFT:
            case BUTTON_ID_LEFT:
                return Button.primary(id, Emoji.fromUnicode(emoji)).withDisabled(!allowPageWrap && pageNumber == 1);
            
            case BUTTON_ID_STOP:
                return Button.danger(id, Emoji.fromUnicode(emoji));
            
            case BUTTON_ID_RIGHT:
            case BUTTON_ID_BULK_SKIP_RIGHT:
                return Button.primary(id, Emoji.fromUnicode(emoji)).withDisabled(!allowPageWrap && pageNumber == totalPages);
            
            default:
                throw new IllegalArgumentException("Unknown Button ID " + id);
        }
    }
    
    private void handleButtonPagination(Message message, EventWaiter waiter, int pageNumber)
    {
        waiter.waitForEvent(
            ButtonClickEvent.class,
            event -> isValidButton(event, message.getIdLong()), 
            event -> handleButtonEvent(event, message, pageNumber), 
            time, unit, () -> finalAction.accept(message));
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
        message.editMessage(menu.getMessage(newPageNumber)).queue(m -> handleButtonPagination(m, menu.waiter, finalNewPage));
    }
}
