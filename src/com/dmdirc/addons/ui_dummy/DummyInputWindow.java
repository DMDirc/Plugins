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

package com.dmdirc.addons.ui_dummy;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.util.StringTranscoder;

import java.nio.charset.Charset;

/**
 * Dummy input window, used for testing.
 */
public class DummyInputWindow implements InputWindow {

    /** Window title. */
    private String title;
    /** Are we visible? */
    private boolean visible;
    /** are we maximised? */
    private boolean maximised;
    /** Our container. */
    private final WritableFrameContainer<? extends InputWindow> container;

    /**
     * Instantiates a new DummyInputWindow.
     *
     * @param owner Parent window
     * @param commandParser Parent command parser
     */
    public DummyInputWindow(final WritableFrameContainer<? extends InputWindow> owner,
            final CommandParser commandParser) {
        this.container = owner;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public CommandParser getCommandParser() {
        return getContainer().getCommandParser();
    }

    /** {@inheritDoc} */
    @Override
    public InputHandler getInputHandler() {
        return new DummyInputHandler(new DummyInputField(), null, this);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void setAwayIndicator(final boolean isAway) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void addLine(final String messageType, final Object... args) {
        getContainer().addLine(messageType, args);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void addLine(final StringBuffer messageType, final Object... args) {
        getContainer().addLine(messageType, args);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void addLine(final String line, final boolean timestamp) {
        getContainer().addLine(line, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public ConfigManager getConfigManager() {
        return IdentityManager.getGlobalConfig();
    }

    /** {@inheritDoc} */
    @Override
    public WritableFrameContainer<? extends InputWindow> getContainer() {
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return visible;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void setVisible(final boolean isVisible) {
        visible = isVisible;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMaximum() {
        return maximised;
    }

    /**
     * {@inheritDoc}
     *
     * @param b maximised or not
     */
    public void setMaximum(final boolean b) {
        maximised = b;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void setTitle(final String title) {
        this.title = title;
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public StringTranscoder getTranscoder() {
        return new StringTranscoder(Charset.defaultCharset());
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        container.handleWindowClosing();
    }

    /** {@inheritDoc} */
    @Override
    public void restore() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void maximise() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void toggleMaximise() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void minimise() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void activateFrame() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public UIController getController() {
        return new DummyController();
    }

}
