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

package com.dmdirc.addons.debug;

import com.dmdirc.ClientModule;
import com.dmdirc.addons.debug.commands.Benchmark;
import com.dmdirc.addons.debug.commands.ColourSpam;
import com.dmdirc.addons.debug.commands.ConfigInfo;
import com.dmdirc.addons.debug.commands.EventBusViewer;
import com.dmdirc.addons.debug.commands.FakeError;
import com.dmdirc.addons.debug.commands.FakeUpdates;
import com.dmdirc.addons.debug.commands.FirstRun;
import com.dmdirc.addons.debug.commands.ForceUpdate;
import com.dmdirc.addons.debug.commands.GlobalConfigInfo;
import com.dmdirc.addons.debug.commands.Identities;
import com.dmdirc.addons.debug.commands.MemInfo;
import com.dmdirc.addons.debug.commands.Notify;
import com.dmdirc.addons.debug.commands.RunGC;
import com.dmdirc.addons.debug.commands.ServerInfo;
import com.dmdirc.addons.debug.commands.ServerState;
import com.dmdirc.addons.debug.commands.Services;
import com.dmdirc.addons.debug.commands.ShowRaw;
import com.dmdirc.addons.debug.commands.StatusbarMessage;
import com.dmdirc.addons.debug.commands.Threads;
import com.dmdirc.addons.debug.commands.Time;

import dagger.Module;
import dagger.Provides;

/**
 * Dependency injection module for the debug plugin.
 */
@Module(injects = Debug.class, addsTo = ClientModule.class)
public class DebugModule {

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final Benchmark command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final ColourSpam command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final ConfigInfo command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final FakeError command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final FakeUpdates command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final FirstRun command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final ForceUpdate command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final GlobalConfigInfo command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final Identities command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final MemInfo command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final Notify command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final RunGC command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final ServerInfo command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final ServerState command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final Services command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final ShowRaw command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final StatusbarMessage command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final Threads command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final Time command) {
        return command;
    }

    @Provides(type = Provides.Type.SET)
    public DebugCommand getCommand(final EventBusViewer command) {
        return command;
    }

}
