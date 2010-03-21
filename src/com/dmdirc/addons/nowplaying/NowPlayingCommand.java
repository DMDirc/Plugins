/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.ChatCommand;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;

import java.util.Arrays;
import java.util.List;

/**
 * The now playing command retrieves the currently playing song from a
 * variety of media players.
 * @author chris
 */
public final class NowPlayingCommand extends ChatCommand implements IntelligentCommand {
    
    /** The plugin that's using this command. */
    final NowPlayingPlugin parent;
    
    /**
     * Creates a new instance of NowPlayingCommand.
     *
     * @param parent The plugin that's instansiating this command
     */
    public NowPlayingCommand(final NowPlayingPlugin parent) {
        super();
        
        this.parent = parent;
        
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin, final Server server,
            final MessageTarget<?> target, final boolean isSilent, final CommandArguments args) {
        if (args.getArguments().length > 0 && args.getArguments()[0]
                .equalsIgnoreCase("--sources")) {
            doSourceList(origin, isSilent, args.getArgumentsAsString(1));
        } else if (args.getArguments().length > 0 && args.getArguments()[0]
                .equalsIgnoreCase("--source")) {
            if (args.getArguments().length > 1) {
                final String sourceName = args.getArguments()[1];
                final MediaSource source = parent.getSource(sourceName);
                
                if (source == null) {
                    sendLine(origin, isSilent, FORMAT_ERROR, "Source not found.");
                } else {
                    if (source.getState() != MediaSourceState.CLOSED) {
                        target.getFrame().getCommandParser().parseCommand(origin,
                                getInformation(source, args.getArgumentsAsString(2)));
                    } else {
                        sendLine(origin, isSilent, FORMAT_ERROR, "Source is not running.");
                    }
                }
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR,
                        "You must specify a source when using --source.");
            }
        } else {
            if (parent.hasRunningSource()) {
                target.getFrame().getCommandParser().parseCommand(origin,
                        getInformation(parent.getBestSource(), args.
                        getArgumentsAsString(0)));
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "No running media sources available.");
            }
        }
    }
    
    /**
     * Outputs a list of sources for the nowplaying command.
     *
     * @param origin The input window where the command was entered
     * @param isSilent Whether this command is being silenced
     * @param format Format to be passed to getInformation
     */
    private void doSourceList(final FrameContainer<?> origin, final boolean isSilent,
            final String format) {
        final List<MediaSource> sources = parent.getSources();
        
        if (sources.isEmpty()) {
            sendLine(origin, isSilent, FORMAT_ERROR, "No media sources available.");
        } else {
            final String[] headers = {"Source", "Status", "Information"};
            final String[][] data = new String[sources.size()][3];
            int i = 0;
            
            for (MediaSource source : sources) {
                data[i][0] = source.getAppName();
                
                if (source.getState() != MediaSourceState.CLOSED) {
                    data[i][1] = source.getState().getNiceName().toLowerCase();
                    data[i][2] = getInformation(source, format);
                } else {
                    data[i][1] = "not running";
                    data[i][2] = "-";
                }
                
                i++;
            }
            
            sendLine(origin, isSilent, FORMAT_OUTPUT, doTable(headers, data));
        }
    }
       
    /**
     * Returns a formatted information string from the requested soruce.
     *
     * @param source MediaSource to query
     * @param format Format to use
     *
     * @return Formatted information string
     * @since 0.6.3
     */
    private String getInformation(final MediaSource source, final String format) {
        if (format.isEmpty()) {
            return parent.doSubstitution(IdentityManager.getGlobalConfig()
                    .getOption(parent.getDomain(), "format"), source);
        } else {
            return parent.doSubstitution(format, source);
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public String getName() {
        return "nowplaying";
    }
    
    /** {@inheritDoc}. */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc}. */
    @Override
    public String getHelp() {
        return "nowplaying [--sources|--source <source>] [format] - " +
                "tells the channel the song you're currently playing";
    }
    
    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {

        final List<String> subsList = Arrays.asList(new String[] {
            "$artist", "$title", "$album", "$app", "$bitrate", "$format",
            "$length", "$state", "$time"
        });

        if (arg == 0) {
            final AdditionalTabTargets res = TabCompleter.
                    getIntelligentResults(arg, context, 0);
            res.add("--sources");
            res.add("--source");
            res.addAll(subsList);
            return res;
        } else if (arg == 1 && context.getPreviousArgs().get(0).equalsIgnoreCase("--source")) {
            final AdditionalTabTargets res = new AdditionalTabTargets();
            res.excludeAll();
            for (MediaSource source : parent.getSources()) {
                if (source.getState() != MediaSourceState.CLOSED) {
                    res.add(source.getAppName());
                }
            }
            return res;
        } else if (arg > 1 && context.getPreviousArgs().get(0).equalsIgnoreCase("--source")) {
            final AdditionalTabTargets res = TabCompleter
                    .getIntelligentResults(arg, context, 2);
            res.addAll(subsList);
            return res;
        } else {
            final AdditionalTabTargets res =  TabCompleter
                    .getIntelligentResults(arg, context, context
                    .getPreviousArgs().get(0).equalsIgnoreCase("--sources") ? 1 : 0);
            res.addAll(subsList);
            return res;
        }
    }
}
