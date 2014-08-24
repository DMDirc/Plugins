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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.CustomWindow;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.ClientLineAddedEvent;
import com.dmdirc.events.DMDircEvent;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLBuilder;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Provider;

import net.engio.mbassy.listener.Handler;

/**
 * Displays events passed on an event bus.
 */
public class EventBusViewer extends DebugCommand {

    private final URLBuilder urlBuilder;
    private final AggregateConfigProvider globalConfig;
    private final WindowManager windowManager;
    private final DMDircMBassador globalEventBus;

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     * @param globalConfig    The global configuration to use for new windows.
     * @param urlBuilder      The URL builder to use in new windows.
     * @param windowManager   The manager to register new windows with.
     * @param globalEventBus  The bus to use when the user specifies the --global flag.
     */
    @Inject
    public EventBusViewer(
            final Provider<Debug> commandProvider,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final URLBuilder urlBuilder,
            final WindowManager windowManager,
            final DMDircMBassador globalEventBus) {
        super(commandProvider);
        this.globalConfig = globalConfig;
        this.urlBuilder = urlBuilder;
        this.windowManager = windowManager;
        this.globalEventBus = globalEventBus;
    }

    @Override
    public String getName() {
        return "eventbus";
    }

    @Override
    public String getUsage() {
        return "[--global] - Shows events being sent on an event bus";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final boolean isGlobal = args.getArguments().length > 0
                && "--global".equals(args.getArguments()[0]);

        final CustomWindow window;
        if (isGlobal) {
            window = new CustomWindow("Event bus", "Event bus", globalConfig,
                    urlBuilder, globalEventBus);
            windowManager.addWindow(window);
        } else {
            window = new CustomWindow("Event bus", "Event bus", origin,
                    urlBuilder);
            windowManager.addWindow(origin, window);
        }

        final DMDircMBassador eventBus = isGlobal ? globalEventBus : origin.getEventBus();
        final WindowUpdater updater = new WindowUpdater(eventBus, window);
        eventBus.subscribe(updater);
    }

    /**
     * Updates a custom window with details of each event received on an event bus.
     */
    private static class WindowUpdater {

        private final DMDircMBassador eventBus;
        private final FrameContainer target;

        WindowUpdater(final DMDircMBassador eventBus, final FrameContainer target) {
            this.eventBus = eventBus;
            this.target = target;
        }

        @Handler
        public void handleEvent(final DMDircEvent event) {
            if (event instanceof FrameClosingEvent) {
                eventBus.unsubscribe(this);
                return;
            }
            if (event instanceof ClientLineAddedEvent
                    && ((ClientLineAddedEvent) event).getFrameContainer() == target) {
                // Don't add a line every time we add a line to our output window.
                // Things will explode otherwise.
                return;
            }

            final StringBuilder output = new StringBuilder();
            output.append(Styliser.CODE_BOLD)
                    .append(event.getClass().getSimpleName())
                    .append(Styliser.CODE_BOLD);

            for (Method method : event.getClass().getMethods()) {
                if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                    try {
                        output.append(' ')
                                .append(Styliser.CODE_UNDERLINE)
                                .append(method.getName().substring(3))
                                .append(Styliser.CODE_UNDERLINE)
                                .append('=')
                                .append(method.invoke(event).toString());
                    } catch (ReflectiveOperationException ex) {
                        // Ignore.
                    }
                }
            }

            target.addLine(FORMAT_OUTPUT, output.toString());
        }

    }
}
