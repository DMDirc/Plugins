/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.addons.debug.commands.*; //NOPMD
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * Debug plugin providing commands to aid in debugging the client.
 */
@SuppressWarnings("unused")
public class DebugPlugin extends BaseCommandPlugin {

    /** List of build in debug commands to load. */
    private static final Class[] CLASSES = {
        Benchmark.class, ColourSpam.class, ConfigInfo.class, ConfigStats.class,
        com.dmdirc.addons.debug.commands.Error.class, FakeUpdates.class,
        FirstRun.class, ForceUpdate.class, GlobalConfigInfo.class,
        Identities.class, MemInfo.class, Notify.class, RunGC.class,
        ServerInfo.class, ServerState.class, Services.class, ShowRaw.class,
        Threads.class, Time.class, StatusbarMessage.class,
    };

    /** List of registered debug commands. */
    private final Map<String, DebugCommand> commands;
    /** Debug command. */
    private final Debug debugCommand;
    /** Plugin manager instance. */
    @Getter
    private final PluginManager pluginManager;
    /** Identity controller instance. */
    @Getter
    private final IdentityController identityController;

    /**
     * Creates a new debug plugin.
     *
     * @param identityController Identity manager instance
     * @param pluginManager Plugin manager instance.
     * @param commandController Command controller to register commands
     */
    public DebugPlugin(final IdentityController identityController,
            final PluginManager pluginManager,
            final CommandController commandController) {
        super(commandController);
        this.identityController = identityController;
        this.pluginManager = pluginManager;
        commands = new HashMap<>();
        debugCommand = new Debug(this, commandController);
        registerCommand(debugCommand, Debug.INFO);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void onLoad() {
        for (Class<DebugCommand> type : CLASSES) {
            try {
                addCommand(type.getConstructor(DebugPlugin.class, Debug.class)
                        .newInstance(this, debugCommand));
            } catch (LinkageError | Exception e) {
                Logger.appError(ErrorLevel.HIGH,
                        "Unable to load debug command", e);
            }
        }
        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        commands.clear();
        super.onUnload();
    }

    /**
     * Adds a command to the list of commands.
     *
     * @param command Command to add
     */
    public void addCommand(final DebugCommand command) {
        commands.put(command.getName(), command);
    }

    /**
     * Removes a command from the list of commands.
     *
     * @param command Command to remove
     */
    public void removeCommand(final DebugCommand command) {
        commands.remove(command.getName());
    }

    /**
     * Returns a list of command names.
     *
     * @return List of command names
     */
    public List<String> getCommandNames() {
        final List<String> names = new ArrayList<>(commands.size());

        for (DebugCommand command : commands.values()) {
            names.add(command.getName());
        }

        return names;
    }

    /**
     * Returns the debug command with the specified name.
     *
     * @param name Name of command to return
     *
     * @return Command with specified name or null if no matches
     */
    public DebugCommand getCommand(final String name) {
        return commands.get(name);
    }

}
