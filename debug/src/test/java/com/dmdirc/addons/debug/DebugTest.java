/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.WindowModel;

import com.google.common.collect.Sets;

import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DebugTest {

    @Mock private CommandArguments arguments;
    @Mock private WindowModel container;
    @Mock private CommandController controller;
    @Mock private DebugCommand debugCommand;
    @Mock private CommandContext commandContext;
    @Mock private EventBus eventbus;
    private Debug debug;

    @Before
    public void setup() {
        when(controller.getCommandChar()).thenReturn('/');
        when(debugCommand.getName()).thenReturn("test");
        when(container.getEventBus()).thenReturn(eventbus);
    }

    /** Checks the debug command with no arguments shows usage. */
    @Test
    public void testNoArgs() {
        debug = new Debug(controller, Sets.<DebugCommand>newHashSet());
        when(arguments.isCommand()).thenReturn(true);
        when(arguments.getArguments()).thenReturn(new String[0]);
        debug.execute(container, arguments, null);
        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

    /** Checks the debug command with an invalid subcommand shows an error. */
    @Test
    public void testInvalidArg() {
        debug = new Debug(controller, Sets.<DebugCommand>newHashSet());
        when(arguments.isCommand()).thenReturn(true);
        when(arguments.getArguments()).thenReturn(new String[]{"test"});

        debug.execute(container, arguments, null);
        verify(eventbus).publishAsync(isA(CommandErrorEvent.class));
    }

    /** Checks the debug command executes a subcommand with no args. */
    @Test
    public void testCommandNoArgs() {
        debug = new Debug(controller, Sets.newHashSet(debugCommand));
        when(arguments.isCommand()).thenReturn(true);
        when(arguments.getArguments()).thenReturn(new String[]{"test"});
        when(arguments.getArgumentsAsString(1)).thenReturn("");
        when(debugCommand.getName()).thenReturn("test");

        debug.execute(container, arguments, commandContext);

        verify(eventbus, never()).publishAsync(any());
        verify(debugCommand).execute(same(container), eqLine("/test"),
                same(commandContext));
    }

    /** Checks the debug command executes a subcommand with args. */
    @Test
    public void testCommandWithArgs() {
        debug = new Debug(controller, Sets.newHashSet(debugCommand));
        when(arguments.isCommand()).thenReturn(true);
        when(arguments.getArguments()).thenReturn(new String[]{"test", "1", "2", "3"});
        when(arguments.getArgumentsAsString(1)).thenReturn("1 2 3");
        when(debugCommand.getName()).thenReturn("test");

        debug.execute(container, arguments, commandContext);

        verify(eventbus, never()).publishAsync(any());
        verify(debugCommand).execute(same(container), eqLine("/test 1 2 3"),
                same(commandContext));
    }

    private static class CommandArgsMatcher extends ArgumentMatcher<CommandArguments> {

        private final String text;

        public CommandArgsMatcher(final String text) {
            this.text = text;
        }

        @Override
        public boolean matches(final Object item) {
            return text.equals(((CommandArguments) item).getLine());
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("eqLine(\"" + text + "\")");
        }

    }

    public static CommandArguments eqLine(final String line) {
        return argThat(new CommandArgsMatcher(line));
    }

}
