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

package com.dmdirc.addons.conditional_execute;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConditionalExecuteCommandTest {

    @Mock private CommandController commandController;
    @Mock private CommandParser commandParser;
    @Mock private FrameContainer container;
    @Mock private CommandContext context;
    private ConditionalExecuteCommand command;

    @Before
    public void setup() {
        when(commandController.getCommandChar()).thenReturn('/');
        when(commandController.getSilenceChar()).thenReturn('/');
        when(container.getCommandParser()).thenReturn(commandParser);

        command = new ConditionalExecuteCommand(commandController);
    }

    /** Tests that {@code --namespace} with no args shows an error. */
    @Test
    public void testWithNoNamespace() {
        execute("/command --namespace");
        verifyErrorOutput("must specify a namespace");
    }

    /** Tests that passing a command without a namespace shows an error. */
    @Test
    public void testExecutingWithNoNamespace() {
        execute("/command /echo test");
        verifyErrorOutput("must specify a namespace");
    }

    /** Tests that passing a command with a previously unused namespace executes it. */
    @Test
    public void testExecutingWithNewNamespace() {
        execute("/command --namespace foo /echo test");
        verifyCommandExecuted("/echo test");
    }

    /** Tests that passing a command with an inhibited namespace does not execute it. */
    @Test
    public void testExecutingWithInhibitedNamespace() {
        execute("/command --namespace foo --inhibit");
        execute("/command --namespace foo /echo test");
        verifyNoCommandExecuted();
    }

    /** Tests that passing a command with a forced namespace executes it. */
    @Test
    public void testExecutingWithForcedNamespace() {
        execute("/command --namespace foo --force");
        execute("/command --namespace foo /echo test");
        verifyCommandExecuted("/echo test");
    }

    /** Tests that passing a command with an inhibited then forced namespace executes it. */
    @Test
    public void testExecutingWithInhibitedThenForcedNamespace() {
        execute("/command --namespace foo --inhibit");
        execute("/command --namespace foo --force");
        execute("/command --namespace foo /echo test");
        verifyCommandExecuted("/echo test");
    }

    /** Tests that passing a command with an inhibited then allowed namespace executes it. */
    @Test
    public void testExecutingWithInhibitedThenAllowedNamespace() {
        execute("/command --namespace foo --inhibit");
        execute("/command --namespace foo --allow");
        execute("/command --namespace foo /echo test");
        verifyCommandExecuted("/echo test");
    }

    /** Tests that passing a command with an inhibited then removed namespace executes it. */
    @Test
    public void testExecutingWithInhibitedThenRemovedNamespace() {
        execute("/command --namespace foo --inhibit");
        execute("/command --namespace foo --remove");
        execute("/command --namespace foo /echo test");
        verifyCommandExecuted("/echo test");
    }

    /** Tests that passing a command with a forced then inhibited namespace does not execute it. */
    @Test
    public void testExecutingWithForcedThenInhibitedNamespace() {
        execute("/command --namespace foo --force");
        execute("/command --namespace foo --inhibit");
        execute("/command --namespace foo /echo test");
        verifyNoCommandExecuted();
    }

    /**
     * Tests that passing a command with a new namespace and {@link --inverse} does not execute it.
     */
    @Test
    public void testInverseExecutingWithNewNamespace() {
        execute("/command --namespace foo --inverse /echo test");
        verifyNoCommandExecuted();
    }

    /**
     * Tests that passing a command with an inhibited namespace and {@link --inverse} does not
     * execute it. Inverse doesn't apply to inhibited namespaces.
     */
    @Test
    public void testInverseExecutingWithInhibitedNamespace() {
        execute("/command --namespace foo --inhibit");
        execute("/command --namespace foo --inverse /echo test");
        verifyNoCommandExecuted();
    }

    private void execute(String line) {
        command.execute(container, new CommandArguments(commandController, line), context);
    }

    private void verifyCommandExecuted(String command) {
        verify(commandParser).parseCommand(same(container), eq(command));
    }

    private void verifyNoCommandExecuted() {
        verify(commandParser, never()).parseCommand(same(container), anyString());
    }

    private void verifyErrorOutput(String substring) {
        verify(container).addLine(eq("commandError"), contains(substring));
    }

}
