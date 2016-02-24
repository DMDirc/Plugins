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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.events.ServerAwayEvent;
import com.dmdirc.events.ServerBackEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;

import javax.swing.JLabel;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple panel to show when a user is away or not.
 */
public class AwayLabel extends JLabel {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** Away indicator. */
    private boolean useAwayIndicator;
    /** Parent frame container. */
    private final WindowModel container;

    /**
     * Creates a new away label for the specified container.
     *
     * @param container Parent frame container
     */
    public AwayLabel(final WindowModel container) {
        super("(away)");

        this.container = checkNotNull(container);

        container.getConfigManager().getBinder().bind(this, AwayLabel.class);
        container.getEventBus().subscribe(this);

        setVisible(false);
        container.getConnection().map(Connection::isAway).ifPresent(this::updateVisibility);
    }

    @ConfigBinding(domain = "ui", key = "awayindicator", invocation = EDTInvocation.class)
    public void handleAwayIndicator(final String value) {
        useAwayIndicator = Boolean.valueOf(value);
        container.getConnection().map(Connection::isAway).ifPresent(this::updateVisibility);
    }

    @Handler(delivery = Invoke.Asynchronously, invocation = EdtHandlerInvocation.class)
    public void handleAway(final ServerAwayEvent event) {
        container.getConnection().map(Connection::isAway).ifPresent(this::updateVisibility);
    }

    @Handler(delivery = Invoke.Asynchronously, invocation = EdtHandlerInvocation.class)
    public void handleBack(final ServerBackEvent event) {
        container.getConnection().filter(c -> c.equals(event.getConnection()))
                .map(Connection::isAway).ifPresent(this::updateVisibility);
    }

    private void updateVisibility(final boolean away) {
        setVisible(useAwayIndicator && away);
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void windowClosing(final FrameClosingEvent event) {
        if (event.getSource().equals(container)) {
            container.getConfigManager().getBinder().unbind(this);
            container.getEventBus().unsubscribe(this);
        }
    }

}
