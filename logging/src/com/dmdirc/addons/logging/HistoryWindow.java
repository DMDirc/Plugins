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

package com.dmdirc.addons.logging;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.util.io.ReverseFileReader;

import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Displays an extended history of a window.
 */
public class HistoryWindow extends FrameContainer {

    private final Path logFile;
    private final DMDircMBassador eventBus;
    private final int numLines;

    /**
     * Creates a new HistoryWindow.
     */
    public HistoryWindow(
            final String title,
            final Path logFile,
            final FrameContainer parent,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory,
            final int numLines) {
        super(parent, "raw", title, title, parent.getConfigManager(), backBufferFactory,
                eventBus,
                Collections.singletonList(WindowComponent.TEXTAREA.getIdentifier()));
        this.logFile = logFile;
        this.eventBus = eventBus;
        this.numLines = numLines;

        initBackBuffer();
        outputLoggingBackBuffer(parent.getConfigManager().getOptionInt("ui", "frameBufferSize"));
    }

    @Override
    public Optional<Connection> getConnection() {
        return getParent().flatMap(FrameContainer::getConnection);
    }

    @VisibleForTesting
    void outputLoggingBackBuffer(final int limit) {
        try (final ReverseFileReader reader = new ReverseFileReader(logFile)) {
            final List<String> lines = reader.getLines(Math.min(limit, numLines));
            Collections.reverse(lines);
            lines.forEach(l -> {
                final ParsePosition pos = new ParsePosition(0);
                final Date date = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]").parse(l, pos);
                final String text = l.substring(pos.getIndex()+1);
                addLine(text, date);
            });
        } catch (IOException | SecurityException ex) {
            eventBus.publishAsync(
                    new UserErrorEvent(ErrorLevel.MEDIUM, ex, "Unable to read log file.", ""));
        }
    }

}
