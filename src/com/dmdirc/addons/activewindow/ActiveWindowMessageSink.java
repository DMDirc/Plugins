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

package com.dmdirc.addons.activewindow;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.messages.MessageSink;
import com.dmdirc.messages.MessageSinkManager;

import java.util.Date;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * A message sink which passes messages onto the active swing window.
 */
public class ActiveWindowMessageSink implements MessageSink {

    /** The pattern to use to match this sink. */
    private static final Pattern PATTERN = Pattern.compile("active");
    /** The main frame to use to get the currently active frame. */
    private final MainFrame mainFrame;

    /**
     * Creates a new ActiveWindowMessageSink for the specified mainframe.
     *
     * @param mainFrame The mainframe to use to retrieve active windows
     */
    @Inject
    public ActiveWindowMessageSink(final MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /** {@inheritDoc} */
    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    /** {@inheritDoc} */
    @Override
    public void handleMessage(final MessageSinkManager despatcher,
            final WritableFrameContainer source, final String[] patternMatches,
            final Date date, final String messageType, final Object... args) {
        final TextFrame frame = mainFrame.getActiveFrame();
        if (frame.getContainer() instanceof WritableFrameContainer) {
            frame.getContainer().addLine(messageType, date, args);
        }
    }

}
