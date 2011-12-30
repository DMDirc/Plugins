/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.debug;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;

import lombok.Getter;

/**
 * Debug command, serves as a proxy between debug commands and normal commands.
 */
@SuppressWarnings("unused")
public abstract class DebugCommand {
    /** The format name used for command output. */
    public static final String FORMAT_OUTPUT = "commandOutput";
    /** The format name used for command errors. */
    public static final String FORMAT_ERROR = "commandError";
    /** Parent debug command. */
    private final Debug command;
    /** Parent debug plugin. */
    @Getter
    private final DebugPlugin plugin;

    /**
     * Returns this command's name.
     *
     * @return The name of this command
     */
    public abstract String getName();

    /**
     * Returns a string representing the help message for this command.
     * <p>
     * The help text should generally be one line, and must start with
     * the name of the command. It should then summarise the arguments of
     * that command, using <code>&lt;angled&gt;</code> brackets for required
     * arguments, and <code>[square]</code> brackets for optional arguments.
     * Where multiple possibilities exist, they are typically separated by a
     * pipe (<code>|</code>), for example: <code>command [--arg1|--arg2]</code>.
     * The usage summary should then be followed by a dash and a brief
     * summary of the purpose of the command.
     * <p>
     * A typical help message would look similar to:
     * <p>
     * <code>command [--arg &lt;param_for_arg&gt;] [someparam] - does x, y and z</code>
     *
     * @return the help message for this command
     */
    public abstract String getUsage();


    /**
     * Executes this command.
     *
     * @param origin The container which received the command
     * @param args Arguments passed to this command
     * @param context The context the command was executed in
     */
    public abstract void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context);

    /**
     * Creates a new debug command.
     *
     * @param plugin Parent plugin
     * @param command Parent debug command
     */
    public DebugCommand(final DebugPlugin plugin, final Debug command) {
        this.command = command;
        this.plugin = plugin;
    }


    /**
     * Sends a line, if appropriate, to the specified target.
     *
     * @param target The command window to send the line to
     * @param isSilent Whether this command is being silenced or not
     * @param type The type of message to send
     * @param args The arguments of the message
     */
    public void sendLine(final FrameContainer target,
            final boolean isSilent, final String type, final Object ... args) {
        if (command != null) {
            command.proxySendLine(target, isSilent, type, args);
        }
    }

    /**
     * Sends a usage line, if appropriate, to the specified target.
     *
     * @param target The command window to send the line to
     * @param isSilent Whether this command is being silenced or not
     * @param name The name of the command that's raising the error
     * @param args The arguments that the command accepts or expects
     */
    public void showUsage(final FrameContainer target,
            final boolean isSilent, final String name, final String args) {
        if (command != null) {
            command.proxyShowUsage(target, isSilent, name, args);
        }
    }

    /**
     * Formats the specified data into a table suitable for output in the
     * textpane. It is expected that each String[] in data has the same number
     * of elements as the headers array.
     *
     * @param headers The headers of the table.
     * @param data The contents of the table.
     * @return A string containing an ASCII table
     */
    public String doTable(final String[] headers, final String[][] data) {
        if (command != null) {
            return command.proxyDoTable(headers, data);
        }
        return "";
    }
}
