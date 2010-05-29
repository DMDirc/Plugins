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

package com.dmdirc.addons.parserdebug;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;

/**
 * The ParserDebug Command allows controlling of which parsers spam debug info.
 *
 * @author Shane "Dataforce" Mc Cormack
 */
@CommandOptions(allowOffline=false)
public final class ParserDebugCommand extends Command implements CommandInfo {
    /** My Plugin */
    final DebugPlugin myPlugin;

    /**
     * Creates a new instance of ParserDebugCommand.
     *
     * @param plugin Plugin that owns this command
     */
    public ParserDebugCommand(final DebugPlugin plugin) {
        super();
        myPlugin = plugin;
        CommandManager.registerCommand(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_SERVER;
    }

    /**
     * Executes this command.
     *
     * @param origin The framecontainer in which this command was issued
     * @param commandArgs The user supplied arguments
     * @param context The Context of this command execution
     */
    @Override
    public void execute(final FrameContainer<?> origin, final CommandArguments commandArgs, final CommandContext context) {
        final boolean isSilent = commandArgs.isSilent();
  
        Parser parser = ((ServerCommandContext) context).getServer().getParser();
        
        if (parser == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Unable to get a parser for this window.");
            return;
        }
        if (myPlugin.registeredParsers.containsKey(parser)) {
            try {
                parser.getCallbackManager().delCallback(DebugInfoListener.class, myPlugin);
                DebugWindow window = myPlugin.registeredParsers.get(parser);
                window.addLine("======================", true);
                window.addLine("No Longer Monitoring: " + parser + " (User Requested)", true);
                window.addLine("======================", true);
                myPlugin.registeredParsers.remove(parser);
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Removing callback ok");
            } catch (Exception e) {
                sendLine(origin, isSilent, FORMAT_ERROR, "Removing callback failed");
            }
        } else {
            try {
                parser.getCallbackManager().addCallback(DebugInfoListener.class, myPlugin);
                DebugWindow window = new DebugWindow(myPlugin, "Parser Debug", parser, origin.getServer());
                myPlugin.registeredParsers.put(parser, window);
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Adding callback ok");
                window.addLine("======================", true);
                window.addLine("Started Monitoring: " + parser, true);
                window.addLine("======================", true);
            } catch (Exception e) {
                sendLine(origin, isSilent, FORMAT_ERROR, "Adding callback failed");
            }
        }
    }

    /**
     * Returns this command's name.
     *
     * @return The name of this command
     */
    @Override
    public String getName() { return "parserdebug"; }
    
    /**
     * Returns whether or not this command should be shown in help messages.
     *
     * @return True iff the command should be shown, false otherwise
     */
    @Override
    public boolean showInHelp() { return true; }
    
    /**
     * Returns a string representing the help message for this command.
     *
     * @return the help message for this command
     */
    @Override
    public String getHelp() { return "parserdebug - Enables/Disables hooks for onDebugInfo for the parser that owns this window"; }
}

