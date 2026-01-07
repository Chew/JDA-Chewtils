package pw.chew.jdachewtils.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EmbedPaginator implements PaginationHandler.Builder
{
    private EventWaiter waiter = null;
    
    private int bulkSkipNumber = 1;
    
    private boolean singlePage = false;
    private boolean pageWrap = false;
    
    private long time = 1;
    private TimeUnit timeUnit = TimeUnit.MINUTES;
    
    private Consumer<Message> finalAction = m -> m.delete().queue();
    
    private final Set<Long> allowedUserIds = new HashSet<>();
    private final Set<Long> allowedRoleIds = new HashSet<>();
    
    private PaginationHandler.PaginationIcon bulkSkipLeftIcon = PaginationHandler.PaginationIcon.fromUnicode(PaginationHandler.EMOJI_BULK_SKIP_LEFT);
    private PaginationHandler.PaginationIcon leftIcon = PaginationHandler.PaginationIcon.fromUnicode(PaginationHandler.EMOJI_LEFT);
    private PaginationHandler.PaginationIcon stopIcon = PaginationHandler.PaginationIcon.fromUnicode(PaginationHandler.EMOJI_STOP);
    private PaginationHandler.PaginationIcon rightIcon = PaginationHandler.PaginationIcon.fromUnicode(PaginationHandler.EMOJI_RIGHT);
    private PaginationHandler.PaginationIcon bulkSkipRightIcon = PaginationHandler.PaginationIcon.fromUnicode(PaginationHandler.EMOJI_BULK_SKIP_RIGHT);
    
    private final List<MessageEmbed> embeds = new ArrayList<>();
    
    @Override
    public EventWaiter getEventWaiter(){
        return waiter;
    }
    
    @Override
    public int getBulkSkipNumber(){
        return bulkSkipNumber;
    }
    
    @Override
    public boolean allowSinglePage(){
        return singlePage;
    }
    
    @Override
    public boolean allowPageWrap(){
        return pageWrap;
    }
    
    @Override
    public long getTime(){
        return time;
    }
    
    @Override
    public TimeUnit getTimeUnit(){
        return timeUnit;
    }
    
    @Override
    public Consumer<Message> getFinalAction(){
        return finalAction;
    }
    
    @Override
    public Set<Long> getAllowedUserIds(){
        return allowedUserIds;
    }
    
    @Override
    public Set<Long> getAllowedRoleIds(){
        return allowedRoleIds;
    }
    
    @Override
    public PaginationHandler.PaginationIcon getBulkSkipLeftIcon(){
        return bulkSkipLeftIcon;
    }
    
    @Override
    public PaginationHandler.PaginationIcon getLeftIcon(){
        return leftIcon;
    }
    
    @Override
    public PaginationHandler.PaginationIcon getStopIcon(){
        return stopIcon;
    }
    
    @Override
    public PaginationHandler.PaginationIcon getRightIcon(){
        return rightIcon;
    }
    
    @Override
    public PaginationHandler.PaginationIcon getBulkSkipRightIcon(){
        return bulkSkipRightIcon;
    }
    
    @Override
    public List<MessageEmbed> getEmbeds(){
        return embeds;
    }
    
    // ===========================================
    
    /**
     * {@inheritDoc}
     * 
     * @return New instance of the {@link PaginationHandler PaginationHandler} to use.
     */
    @Override
    public PaginationHandler build(){
        return PaginationHandler.fromBuilder(this);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  waiter
     *         The EventWaiter instance to use
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setEventWaiter(EventWaiter waiter){
        Checks.notNull(waiter, "EventWaiter");
        
        this.waiter = waiter;
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  bulkSkipNumber
     *         The number to set for bulk skip.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipNumber(int bulkSkipNumber){
        Checks.positive(bulkSkipNumber, "BulkSkipNumber");
        
        this.bulkSkipNumber = bulkSkipNumber;
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  singlePage
     *         Whether the Paginator should wait on a single page.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setAllowSinglePage(boolean singlePage){
        this.singlePage = singlePage;
        return this;
    }
    
    /**
     * 
     * @param  pageWrap
     *         Whether the Paginator should wrap around when reaching an end.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setAllowPageWrap(boolean pageWrap){
        this.pageWrap = pageWrap;
        return this;
    }
    
    /**
     * 
     * @param  time
     *         The amount of TimeUnits to wait for interactions before cancelling.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setTime(long time){
        Checks.positive(time, "Time");
        
        this.time = time;
        return this;
    }
    
    /**
     * 
     * @param  timeUnit
     *         The TimeUnit to use for the specified time.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setTimeUnit(TimeUnit timeUnit){
        Checks.notNull(timeUnit, "TimeUnit");
        
        this.timeUnit = timeUnit;
        return this;
    }
    
    /**
     * 
     * @param  finalAction
     *         A Consumer of type Message to set the final action.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setFinalAction(Consumer<Message> finalAction){
        Checks.notNull(finalAction, "FinalAction");
        
        this.finalAction = finalAction;
        return this;
    }
    
    /**
     * 
     * @param  userIds
     *         Array of Longs that should be added as allowed User ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator addAllowedUserIds(Long... userIds){
        Checks.noneNull(userIds, "UserIds");
        
        this.allowedUserIds.addAll(Arrays.asList(userIds));
        return this;
    }
    
    /**
     * 
     * @param  userIds
     *         Collection of Longs that should be added as allowed User ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator addAllowedUserIds(Collection<Long> userIds){
        Checks.noneNull(userIds, "UserIds");
        
        this.allowedUserIds.addAll(userIds);
        return this;
    }
    
    /**
     * 
     * @param  userIds
     *         Array of Longs that should be set as allowed User ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setAllowedUserIds(Long... userIds){
        Checks.noneNull(userIds, "UserIds");
    
        this.allowedUserIds.clear();
        this.allowedUserIds.addAll(Arrays.asList(userIds));
        return this;
    }
    
    /**
     * 
     * @param  userIds
     *         Collection of Longs that should be set as allowed User ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setAllowedUserIds(Collection<Long> userIds){
        Checks.noneNull(userIds, "UserIds");
    
        this.allowedUserIds.clear();
        this.allowedUserIds.addAll(userIds);
        return this;
    }
    
    /**
     * 
     * @param  roleIds
     *         Array of Longs that should be added as allowed Role ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator addAllowedRoleIds(Long... roleIds){
        Checks.noneNull(roleIds, "RoleIds");
    
        this.allowedUserIds.addAll(Arrays.asList(roleIds));
        return this;
    }
    
    /**
     * 
     * @param  roleIds
     *         Collection of Longs that should be added as allowed Role ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator addAllowedRoleIds(Collection<Long> roleIds){
        Checks.noneNull(roleIds, "RoleIds");
    
        this.allowedUserIds.addAll(roleIds);
        return this;
    }
    
    /**
     * 
     * @param  roleIds
     *         Array of Longs that should be set as allowed Role ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setAllowedRoleIds(Long... roleIds){
        Checks.noneNull(roleIds, "RoleIds");
    
        this.allowedUserIds.clear();
        this.allowedUserIds.addAll(Arrays.asList(roleIds));
        return this;
    }
    
    /**
     * 
     * @param  roleIds
     *         Collection of Longs that should be set as allowed Role ids.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setAllowedRoleIds(Collection<Long> roleIds){
        Checks.noneNull(roleIds, "RoleIds");
    
        this.allowedUserIds.clear();
        this.allowedUserIds.addAll(roleIds);
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  unicode
     *         The unicode Emoji to use.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipLeft(String unicode){
        Checks.notNull(unicode, "Unicode");
        
        this.bulkSkipLeftIcon = PaginationHandler.PaginationIcon.fromUnicode(unicode);
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  name
     *         The name of the emote
     * @param  id
     *         The id of the emote
     * @param  animated
     *         If the emote is actually animated or not
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipLeft(String name, long id, boolean animated){
        Checks.notNull(name, "Name");
        
        this.bulkSkipLeftIcon = PaginationHandler.PaginationIcon.fromEmote(name, id, animated);
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipLeft(Emote emote){
        Checks.notNull(emote, "Emote");
        
        this.bulkSkipLeftIcon = PaginationHandler.PaginationIcon.fromEmote(emote);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  unicode
     *         The unicode Emoji to use.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setLeft(String unicode){
        Checks.notNull(unicode, "Unicode");
    
        this.leftIcon = PaginationHandler.PaginationIcon.fromUnicode(unicode);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  name
     *         The name of the emote
     * @param  id
     *         The id of the emote
     * @param  animated
     *         If the emote is actually animated or not
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setLeft(String name, long id, boolean animated){
        Checks.notNull(name, "Name");
    
        this.leftIcon = PaginationHandler.PaginationIcon.fromEmote(name, id, animated);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setLeft(Emote emote){
        Checks.notNull(emote, "Emote");
    
        this.leftIcon = PaginationHandler.PaginationIcon.fromEmote(emote);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  unicode
     *         The unicode Emoji to use.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setStop(String unicode){
        Checks.notNull(unicode, "Unicode");
    
        this.stopIcon = PaginationHandler.PaginationIcon.fromUnicode(unicode);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  name
     *         The name of the emote
     * @param  id
     *         The id of the emote
     * @param  animated
     *         If the emote is actually animated or not
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setStop(String name, long id, boolean animated){
        Checks.notNull(name, "Name");
    
        this.stopIcon = PaginationHandler.PaginationIcon.fromEmote(name, id, animated);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setStop(Emote emote){
        Checks.notNull(emote, "Emote");
    
        this.stopIcon = PaginationHandler.PaginationIcon.fromEmote(emote);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  unicode
     *         The unicode Emoji to use.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setRight(String unicode){
        Checks.notNull(unicode, "Unicode");
    
        this.rightIcon = PaginationHandler.PaginationIcon.fromUnicode(unicode);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  name
     *         The name of the emote
     * @param  id
     *         The id of the emote
     * @param  animated
     *         If the emote is actually animated or not
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setRight(String name, long id, boolean animated){
        Checks.notNull(name, "Name");
    
        this.rightIcon = PaginationHandler.PaginationIcon.fromEmote(name, id, animated);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setRight(Emote emote){
        Checks.notNull(emote, "Emote");
    
        this.rightIcon = PaginationHandler.PaginationIcon.fromEmote(emote);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  unicode
     *         The unicode Emoji to use.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipRight(String unicode){
        Checks.notNull(unicode, "Unicode");
    
        this.bulkSkipRightIcon = PaginationHandler.PaginationIcon.fromUnicode(unicode);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  name
     *         The name of the emote
     * @param  id
     *         The id of the emote
     * @param  animated
     *         If the emote is actually animated or not
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipRight(String name, long id, boolean animated){
        Checks.notNull(name, "Name");
    
        this.bulkSkipRightIcon = PaginationHandler.PaginationIcon.fromEmote(name, id, animated);
        return this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @param  emote
     *         The {@link net.dv8tion.jda.api.entities.Emote Emote} to use
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setBulkSkipRight(Emote emote){
        Checks.notNull(emote, "Emote");
    
        this.bulkSkipRightIcon = PaginationHandler.PaginationIcon.fromEmote(emote);
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  embeds
     *         The MessageEmbeds to add to the list.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator addEmbeds(MessageEmbed... embeds){
        Checks.noneNull(embeds, "Embeds");
        
        this.embeds.addAll(Arrays.asList(embeds));
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  embeds
     *         The MessageEmbeds to add to the list.
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator addEmbeds(Collection<MessageEmbed> embeds){
        Checks.noneNull(embeds, "Embeds");
        
        this.embeds.addAll(embeds);
        return this;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param  embeds
     *         List of {@link MessageEmbed embeds} to use for the Paginator
     *
     * @return EmbedPaginator class for chaining convenience.
     */
    @Override
    public EmbedPaginator setEmbeds(Collection<MessageEmbed> embeds){
        Checks.noneNull(embeds, "Embeds");
        
        this.embeds.clear();
        this.embeds.addAll(embeds);
        return this;
    }
}
