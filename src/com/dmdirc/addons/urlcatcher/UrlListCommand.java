/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.urlcatcher;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.Map;

/**
 *
 * @author chris
 */
public class UrlListCommand extends Command implements IntelligentCommand,
        CommandInfo {
    
    private final UrlCatcherPlugin plugin;

    /**
     * Creates a new URL List command, outputting all known URLs seen by the URL
     * catcher plugin to the active window.
     *
     * @param plugin Parent plugin
     */
    public UrlListCommand(final UrlCatcherPlugin plugin) {
        this.plugin = plugin;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final String[] headers = {"URL", "Count"};
        final Map<String, Integer> map = plugin.getURLS();
        final String[][] data = new String[map.size()][];
        
        int i = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            data[i++] = new String[]{entry.getKey(), entry.getValue().toString()};
        }
        
        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, doTable(headers, data));
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "urllist";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_GLOBAL;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "urllist - shows a list of URLs that have been seen";
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return new AdditionalTabTargets().excludeAll();
    } 

}
