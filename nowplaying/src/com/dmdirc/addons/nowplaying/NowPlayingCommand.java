/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleterUtils;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The now playing command retrieves the currently playing song from a variety of media players.
 */
public class NowPlayingCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("nowplaying",
            "nowplaying [--sources|--source <source>] [format] - "
            + "tells the channel the song you're currently playing",
            CommandType.TYPE_CHAT);
    /** Now playing manager to get and handle sources. */
    private final NowPlayingManager manager;
    /** Global configuration to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** This plugin's settings domain. */
    private final String domain;

    /**
     * Creates a new instance of this command.
     *
     * @param controller   The controller to use for command information.
     * @param manager      Now playing manager to get and handle sources.
     * @param globalConfig Global config to read from
     * @param domain       This plugin's settings domain
     */
    @Inject
    public NowPlayingCommand(
            final CommandController controller,
            final NowPlayingManager manager,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(NowPlayingPlugin.class) final String domain) {
        super(controller);
        this.manager = manager;
        this.globalConfig = globalConfig;
        this.domain = domain;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final FrameContainer target = ((ChatCommandContext) context).getChat();
        if (args.getArguments().length > 0
                && args.getArguments()[0].equalsIgnoreCase("--sources")) {
            doSourceList(origin, args.isSilent(), args.getArgumentsAsString(1));
        } else if (args.getArguments().length > 0
                && args.getArguments()[0].equalsIgnoreCase("--source")) {
            if (args.getArguments().length > 1) {
                final String sourceName = args.getArguments()[1];
                final MediaSource source = manager.getSource(sourceName);

                if (source == null) {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR, "Source not found.");
                } else {
                    if (source.getState() == MediaSourceState.CLOSED) {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, "Source is not running.");
                    } else {
                        target.getCommandParser().parseCommand(origin,
                                getInformation(source, args.getArgumentsAsString(2)));
                    }
                }
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You must specify a source when using --source.");
            }
        } else {
            if (manager.hasRunningSource()) {
                target.getCommandParser().parseCommand(origin,
                        getInformation(manager.getBestSource(), args.
                                getArgumentsAsString(0)));
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "No running media sources available.");
            }
        }
    }

    /**
     * Outputs a list of sources for the nowplaying command.
     *
     * @param origin   The input window where the command was entered
     * @param isSilent Whether this command is being silenced
     * @param format   Format to be passed to getInformation
     */
    private void doSourceList(final FrameContainer origin, final boolean isSilent,
            final String format) {
        final List<MediaSource> sources = manager.getSources();

        if (sources.isEmpty()) {
            sendLine(origin, isSilent, FORMAT_ERROR, "No media sources available.");
        } else {
            final String[] headers = {"Source", "Status", "Information"};
            final String[][] data = new String[sources.size()][3];
            int i = 0;

            for (MediaSource source : sources) {
                data[i][0] = source.getAppName();

                if (source.getState() == MediaSourceState.CLOSED) {
                    data[i][1] = "not running";
                    data[i][2] = "-";
                } else {
                    data[i][1] = source.getState().getNiceName().toLowerCase();
                    data[i][2] = getInformation(source, format);
                }

                i++;
            }

            sendLine(origin, isSilent, FORMAT_OUTPUT, doTable(headers, data));
        }
    }

    /**
     * Returns a formatted information string from the requested source.
     *
     * @param source MediaSource to query
     * @param format Format to use
     *
     * @return Formatted information string
     */
    private String getInformation(final MediaSource source, final String format) {
        if (format.isEmpty()) {
            return manager.doSubstitution(globalConfig.getOption(domain, "format"), source);
        } else {
            return manager.doSubstitution(format, source);
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {

        final List<String> subsList = Arrays.asList("$artist", "$title", "$album", "$app",
                "$bitrate", "$format", "$length", "$state", "$time");

        if (arg == 0) {
            final AdditionalTabTargets res = TabCompleterUtils.
                    getIntelligentResults(arg, context, 0);
            res.add("--sources");
            res.add("--source");
            res.addAll(subsList);
            return res;
        } else if (arg == 1 && context.getPreviousArgs().get(0).equalsIgnoreCase("--source")) {
            final AdditionalTabTargets res = new AdditionalTabTargets();
            res.excludeAll();
            for (MediaSource source : manager.getSources()) {
                if (source.getState() != MediaSourceState.CLOSED) {
                    res.add(source.getAppName());
                }
            }
            return res;
        } else if (arg > 1 && context.getPreviousArgs().get(0).equalsIgnoreCase("--source")) {
            final AdditionalTabTargets res = TabCompleterUtils
                    .getIntelligentResults(arg, context, 2);
            res.addAll(subsList);
            return res;
        } else {
            final AdditionalTabTargets res = TabCompleterUtils
                    .getIntelligentResults(arg, context,
                            context.getPreviousArgs().get(0).equalsIgnoreCase("--sources") ? 1 : 0);
            res.addAll(subsList);
            return res;
        }
    }

}
