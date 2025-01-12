/*
 * Copyright (c) 2020-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserDiscordBot
 */

package org.geysermc.discordbot.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.geysermc.discordbot.GeyserBot;

import java.util.List;

/**
 * This class helps us with error messages when handling commands of varying types
 */
public class MessageHelper {
    /**
     * Parse and reply an error response if the user needs to be told about their incorrect ways.
     * Handles both CommandEvent and SlashCommandEvent to ease support for both.
     *
     * @param event the event to handle
     * @param title the title of the embed
     * @param message the content of the embed
     * @throws IllegalArgumentException if event isn't CommandEvent or SlashCommandEvent
     * @return a MessageEmbed, or null if you want to pass it up
     */
    public static MessageEmbed errorResponse(Object event, String title, String message) {
        MessageEmbed embed = new EmbedBuilder()
            .setTitle(title) // Set our title
            .setDescription(message) // Set the description
            .setColor(BotColors.FAILURE.getColor()) // Set the color
            .build(); // Finalize it

        if (event == null) {
            return embed;
        }

        if (event instanceof CommandEvent) { // If this is a normal !command
            ((CommandEvent) event)
                .getMessage()
                .replyEmbeds(embed)
                .queue();
        } else if (event instanceof MessageReceivedEvent) { // If this is a /command
            ((MessageReceivedEvent) event)
                    .getMessage()
                    .replyEmbeds(embed)
                    .queue();
        } else if (event instanceof SlashCommandEvent) { // If this is a /command
            ((SlashCommandEvent) event)
                .replyEmbeds(embed) // Have to do this nonsense...
                .setEphemeral(true) // Only show error to the user
                .queue();
        } else {
            throw new IllegalArgumentException("Event must be one of CommandEvent, SlashCommandEvent");
        }

        return null;
    }

    /**
     * Checks if a List of {@link MessageEmbed.Field}s has a Field whose name is similar to a given String
     *
     * @param fields The List of {@link MessageEmbed.Field}s to check
     * @param string The string to check
     * @return True if the List has a {@link MessageEmbed.Field} whose name contains the given String, or the given string contains the field's name
     */
    public static boolean similarFieldExists(List<MessageEmbed.Field> fields, String string) {
        String lowerCaseString = string.toLowerCase();
        for (MessageEmbed.Field field : fields) {
            String fieldName = field.getName();
            if (fieldName == null) {
                continue;
            }

            String lowerCaseFieldName = fieldName.toLowerCase();
            if (lowerCaseFieldName.contains(lowerCaseString) || lowerCaseString.contains(lowerCaseFieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes {@link MessageEmbed.Field}s from the end of a given {@link EmbedBuilder} until there are no more fields, or the following returns true: {@link EmbedBuilder#isValidLength()} <br/>
     * May fail to make the EmbedBuilder have a valid length if it exceeds the valid length for other reasons.
     * @see <a href="https://discord.com/developers/docs/resources/channel#embed-limits">https://discord.com/developers/docs/resources/channel#embed-limits</a>
     *
     * @param embedBuilder The {@link EmbedBuilder} to truncate if necessary.
     */
    public static void truncateFields(EmbedBuilder embedBuilder) {
        if (embedBuilder.isValidLength()) {
            return;
        }

        // todo: remove this once we figure out ErrorAnalyzer
        GeyserBot.LOGGER.debug("EmbedBuilder with description: " + embedBuilder.getDescriptionBuilder() + " is being truncated to valid length.");

        // remove entries from the bottom of the list until the length is valid, or we completely empty the list
        for (int i = embedBuilder.getFields().size() - 1; i > 0 && !embedBuilder.isValidLength(); i--) {
            embedBuilder.getFields().remove(i);
        }
    }
}
