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
package com.jagrosh.jdautilities.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A {@link Menu} implementation, nearly identical to {@link EmbedPaginator}, that displays an individual
 * {@link MessageEmbed}s on each page instead of a list of text items.
 *
 * <p>Unlike Paginator, native Discord buttons are used and allow the user to traverse to the last page
 * using the left arrow, the next page using the right arrow, and to stop the EmbedPaginator prematurely
 * using the stop button.
 */
public class ButtonEmbedPaginator extends Menu {
    private static final List<String> paginators = new ArrayList<>();

    private final BiFunction<Integer, Integer, String> text;
    private final Consumer<Message> finalAction;
    private final boolean waitOnSinglePage;
    private final List<MessageEmbed> embeds;
    private final int bulkSkipNumber;
    private final boolean wrapPageEnds;
    private final ButtonStyle style;

    public static final Emoji BIG_LEFT = Emoji.fromUnicode("\u23EA");
    public static final Emoji LEFT = Emoji.fromUnicode("\u25C0");
    public static final Emoji STOP = Emoji.fromUnicode("\u23F9");
    public static final Emoji RIGHT = Emoji.fromUnicode("\u25B6");
    public static final Emoji BIG_RIGHT = Emoji.fromUnicode("\u23E9");

    protected ButtonEmbedPaginator(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                                   BiFunction<Integer, Integer, String> text, Consumer<Message> finalAction,
                                   boolean waitOnSinglePage, List<MessageEmbed> embeds, int bulkSkipNumber,
                                   boolean wrapPageEnds, ButtonStyle style) {
        super(waiter, users, roles, timeout, unit);
        this.text = text;
        this.finalAction = finalAction;
        this.waitOnSinglePage = waitOnSinglePage;
        this.embeds = embeds;
        this.bulkSkipNumber = bulkSkipNumber;
        this.wrapPageEnds = wrapPageEnds;
        this.style = style;
    }

    /**
     * Begins pagination on page 1 as a new {@link Message} in the provided {@link MessageChannel}.
     *
     * @param channel The MessageChannel to send the new Message to
     */
    @Override
    public void display(MessageChannel channel) {
        paginate(channel, 1);
    }

    /**
     * Begins pagination on page 1 displaying this by editing the provided {@link Message}.
     *
     * @param message The Message to display the Menu in
     */
    @Override
    public void display(Message message) {
        paginate(message, 1);
    }

    /**
     * Begins pagination as a new {@link Message} in the provided {@link MessageChannel}, starting on whatever
     * page number is provided.
     *
     * @param channel The MessageChannel to send the new Message to
     * @param pageNum The page number to begin on
     */
    public void paginate(MessageChannel channel, int pageNum) {
        if (pageNum < 1)
            pageNum = 1;
        else if (pageNum > embeds.size())
            pageNum = embeds.size();
        MessageCreateData msg = MessageCreateData.fromEditData(renderPage(pageNum));
        initialize(channel.sendMessage(msg), pageNum);
    }

    /**
     * Begins pagination displaying this by editing the provided {@link Message}, starting on whatever page number
     * is provided.
     *
     * @param message The message to edit
     * @param pageNum The page number to begin on
     */
    public void paginate(Message message, int pageNum) {
        if (pageNum < 1)
            pageNum = 1;
        else if (pageNum > embeds.size())
            pageNum = embeds.size();
        MessageEditData msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }

    private void initialize(RestAction<Message> action, int pageNum) {
        action.queue(m -> {
            if (embeds.size() > 1) {
                List<Button> actions = buildButtons();

                m.editMessage(renderPage(pageNum)).setActionRow(actions).queue(v -> pagination(m, pageNum));
            } else if (waitOnSinglePage) {
                String id = System.currentTimeMillis() + ":STOP";
                m.editMessage(renderPage(pageNum)).setActionRow(Button.of(style, id, STOP)).queue(v -> pagination(m, pageNum));
            } else {
                finalAction.accept(m);
            }
        });
    }

    private List<Button> buildButtons() {
        // bep = button embed paginator
        String id = "bep:" + System.currentTimeMillis();
        List<Button> actions = new ArrayList<>();

        actions.add(Button.of(style, id + ":LEFT", LEFT));
        actions.add(Button.of(style, id + ":STOP", STOP));
        actions.add(Button.of(style, id + ":RIGHT", RIGHT));
        if (bulkSkipNumber > 1) {
            actions.add(0, Button.primary(id + ":BIG_LEFT", BIG_LEFT));
            actions.add(Button.of(style, id + ":BIG_RIGHT", BIG_RIGHT));
        }

        return actions;
    }

    private void pagination(Message message, int pageNum) {
        // store this for later
        paginators.add(message.getId());

        waiter.waitForEvent(ButtonInteractionEvent.class,
            event -> checkButton(event, message.getIdLong()),
            event -> handleButtonInteraction(event, message, pageNum),
            timeout, unit, () -> finalAction.accept(message));
    }

    private boolean checkButton(ButtonInteractionEvent event, long messageId) {
        if (event.getComponentId().startsWith("bep:") && !paginators.contains(event.getMessageId())) {
            event.reply("This paginator is no longer active. The buttons will be removed. Please make a new one!").setEphemeral(true).queue();
            event.getMessage().editMessageComponents().queue();
            return false;
        }

        if (event.getMessageIdLong() != messageId)
            return false;

        if (Arrays.asList(STOP, LEFT, RIGHT).contains(event.getButton().getEmoji())) {
            return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        } else if (Arrays.asList(BIG_LEFT, BIG_RIGHT).contains(event.getButton().getEmoji())) {
            return bulkSkipNumber > 1 && isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        } else {
            return false;
        }
    }

    private void handleButtonInteraction(ButtonInteractionEvent event, Message message, int pageNum) {
        int newPageNum = pageNum;
        int pages = embeds.size();

        Emoji emoji = event.getButton().getEmoji();
        if (emoji == null) {
            LoggerFactory.getLogger(this.getClass()).warn("Received null emoji in ButtonInteractionEvent!");
            return;
        }

        if (emoji.equals(LEFT)) {
            if (newPageNum == 1 && wrapPageEnds)
                newPageNum = pages + 1;
            if (newPageNum > 1)
                newPageNum--;
        } else if (emoji.equals(RIGHT)) {
            if (newPageNum == pages && wrapPageEnds)
                newPageNum = 0;
            if (newPageNum < pages)
                newPageNum++;
        } else if (emoji.equals(BIG_LEFT)) {
            if (newPageNum > 1 || wrapPageEnds) {
                for (int i = 1; (newPageNum > 1 || wrapPageEnds) && i < bulkSkipNumber; i++) {
                    if (newPageNum == 1)
                        newPageNum = pages + 1;
                    newPageNum--;
                }
            }
        } else if (emoji.equals(BIG_RIGHT)) {
            if (newPageNum < pages || wrapPageEnds) {
                for (int i = 1; (newPageNum < pages || wrapPageEnds) && i < bulkSkipNumber; i++) {
                    if (newPageNum == pages)
                        newPageNum = 0;
                    newPageNum++;
                }
            }
        } else if (emoji.equals(STOP)) {
            finalAction.accept(message);
            return;
        }

        int n = newPageNum;
        event.deferEdit().queue(
            interactionHook -> message.editMessage(renderPage(n)).setActionRow(buildButtons()).queue(m -> pagination(m, n))
        );
    }

    private MessageEditData renderPage(int pageNum) {
        MessageEditBuilder mbuilder = new MessageEditBuilder();
        MessageEmbed membed = this.embeds.get(pageNum - 1);
        mbuilder.setEmbeds(membed);
        if (text != null)
            mbuilder.setContent(text.apply(pageNum, embeds.size()));
        return mbuilder.build();
    }

    /**
     * The {@link Menu.Builder} for a {@link ButtonEmbedPaginator}.
     */
    public static class Builder extends Menu.Builder<Builder, ButtonEmbedPaginator> {
        private BiFunction<Integer, Integer, String> text = (page, pages) -> null;
        private Consumer<Message> finalAction = m -> m.delete().queue();
        private boolean waitOnSinglePage = false;
        private int bulkSkipNumber = 1;
        private boolean wrapPageEnds = false;
        private ButtonStyle style = ButtonStyle.SECONDARY;

        private final List<MessageEmbed> embeds = new LinkedList<>();

        /**
         * Builds the {@link ButtonEmbedPaginator} with this Builder.
         *
         * @return The Paginator built from this Builder.
         * @throws IllegalArgumentException If one of the following is violated:
         *                                            <ul>
         *                                                <li>No {@link EventWaiter} was set.</li>
         *                                                <li>No items were set to paginate.</li>
         *                                            </ul>
         */
        @Override
        public ButtonEmbedPaginator build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!embeds.isEmpty(), "Must include at least one item to paginate");

            return new ButtonEmbedPaginator(
                waiter, users, roles, timeout, unit, text, finalAction, waitOnSinglePage, embeds, bulkSkipNumber,
                wrapPageEnds, style
            );
        }

        /**
         * Sets the text of the {@link Message} to be displayed when the {@link ButtonEmbedPaginator} is built.
         *
         * @param text The Message content to be displayed above the embed when the EmbedPaginator is built.
         * @return This builder
         */
        public Builder setText(String text) {
            this.text = (i0, i1) -> text;
            return this;
        }

        /**
         * Sets the text of the {@link Message} to be displayed relative to the total page number and the current page
         * as determined by the provided {@link BiFunction}.
         * <br>As the page changes, the BiFunction will re-process the current page number and the total page number,
         * allowing for the displayed text of the Message to change depending on the page number.
         *
         * @param textBiFunction The BiFunction that uses both current and total page numbers, to get text for the Message
         * @return This builder
         */
        public Builder setText(BiFunction<Integer, Integer, String> textBiFunction) {
            this.text = textBiFunction;
            return this;
        }

        /**
         * Sets the {@link Consumer} to perform if the {@link ButtonEmbedPaginator} times out.
         *
         * @param finalAction The Consumer action to perform if the EmbedPaginator times out
         * @return This builder
         */
        public Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }

        /**
         * Sets whether the {@link ButtonEmbedPaginator} will instantly timeout, and possibly run a provided
         * {@link Runnable}, if only a single slide is available to display.
         *
         * @param waitOnSinglePage {@code true} if the EmbedPaginator will still generate
         * @return This builder
         */
        public Builder waitOnSinglePage(boolean waitOnSinglePage) {
            this.waitOnSinglePage = waitOnSinglePage;
            return this;
        }

        /**
         * Clears all previously set items.
         *
         * @return This builder
         */
        public Builder clearItems() {
            this.embeds.clear();
            return this;
        }

        /**
         * Adds {@link MessageEmbed} to the list of items to paginate.
         *
         * @param embeds The list of MessageEmbeds to add
         * @return This builder
         */
        public Builder addItems(MessageEmbed... embeds) {
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }

        /**
         * Adds the collection of provided {@link MessageEmbed} to the list of items to paginate.
         *
         * @param embeds The collection of MessageEmbeds to add
         * @return This builder
         */
        public Builder addItems(Collection<MessageEmbed> embeds) {
            this.embeds.addAll(embeds);
            return this;
        }

        /**
         * Adds {@link MessageEmbed MessageEmbeds} to the list of items to paginate.
         * <br>This method creates a new, basic MessageEmbed containing only the provided String as description.
         * <br>Use the {@link Paginator} for more Embed customization, without providing your own MessageEmbed instances.
         *
         * @param items The String list of items to add as MessageEmbeds
         * @return This builder
         * @throws IllegalArgumentException When one of the provided Strings is longer than 2048 characters.
         */
        public Builder addItems(String... items) {
            for (String item : items) {
                Checks.check(item.length() <= MessageEmbed.TEXT_MAX_LENGTH, "Text may not be longer than " + MessageEmbed.TEXT_MAX_LENGTH + " characters.");
                this.embeds.add(new EmbedBuilder().setDescription(item).build());
            }
            return this;
        }

        /**
         * Sets the {@link MessageEmbed} to paginate.
         * <br>This method clears all previously set items before setting.
         *
         * @param embeds The MessageEmbed list of items to add
         * @return This builder
         */
        public Builder setItems(MessageEmbed... embeds) {
            this.embeds.clear();
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }

        /**
         * Sets the {@link MessageEmbed MessageEmbeds} to paginate.
         * <br>This method clears all previously set items before adding the provided collection of MessageEmbeds.
         *
         * @param embeds The collection of MessageEmbeds to set.
         * @return This builder
         */
        public Builder setItems(Collection<MessageEmbed> embeds) {
            this.embeds.clear();
            addItems(embeds);
            return this;
        }

        /**
         * Sets the {@link MessageEmbed MessageEmbeds} to paginate.
         * <br>This method clears all previously set items before setting each String as a new MessageEmbed.
         * <br>Use the {@link Paginator} for more Embed customization, without providing your own MessageEmbed instances.
         *
         * @param items The String list of items to add
         * @return This builder
         * @throws IllegalArgumentException When one of the provided Strings is longer than 2048 characters.
         */
        public Builder setItems(String... items) {
            this.embeds.clear();
            addItems(items);
            return this;
        }

        /**
         * Sets the {@link ButtonEmbedPaginator}'s bulk-skip function to
         * skip multiple pages using alternate forward and backwards reactions.
         *
         * @param bulkSkipNumber The number of pages to skip when the bulk-skip reactions are used.
         * @return This builder
         */
        public Builder setBulkSkipNumber(int bulkSkipNumber) {
            this.bulkSkipNumber = Math.max(bulkSkipNumber, 1);
            return this;
        }

        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} to wrap from the last page
         * to the first when traversing right and vice versa from the left.
         *
         * @param wrapPageEnds {@code true} to enable wrapping.
         * @return This builder
         */
        public Builder wrapPageEnds(boolean wrapPageEnds) {
            this.wrapPageEnds = wrapPageEnds;
            return this;
        }

        /**
         * Sets the {@link ButtonStyle} to use for the buttons.
         * By default, this is {@link ButtonStyle#SECONDARY SECONARY}.
         *
         * @param style The new style
         * @return This builder
         */
        public Builder setButtonStyle(ButtonStyle style) {
            this.style = style;
            return this;
        }
    }
}
