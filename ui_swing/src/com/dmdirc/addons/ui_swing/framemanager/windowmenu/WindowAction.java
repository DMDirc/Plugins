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

package com.dmdirc.addons.ui_swing.framemanager.windowmenu;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingActiveWindowChangeRequestEvent;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.FrameNameChangedEvent;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.Styliser;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.engio.mbassy.listener.Handler;

/**
 * An action representing a window.
 */
public class WindowAction extends AbstractAction {

    private final SwingEventBus eventBus;
    private final IconManager iconManager;
    private final TextFrame window;

    public WindowAction(final SwingEventBus eventBus, final IconManager iconManager,
            final TextFrame window) {
        this.eventBus = eventBus;
        this.iconManager = iconManager;
        this.window = window;
    }

    public void init(final DMDircMBassador eventBus) {
        eventBus.subscribe(this);
        putValue(Action.SMALL_ICON, iconManager.getIcon(window.getContainer().getIcon()));
        putValue(Action.NAME, Styliser.stipControlCodes(window.getContainer().getName()));
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void iconChanged(final FrameIconChangedEvent event) {
        if (event.getContainer().equals(window.getContainer())) {
            putValue(Action.SMALL_ICON, iconManager.getIcon(event.getIcon()));
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void nameChanged(final FrameNameChangedEvent event) {
        if (event.getContainer().equals(window.getContainer())) {
            putValue(Action.NAME, Styliser.stipControlCodes(event.getName()));
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        eventBus.publishAsync(new SwingActiveWindowChangeRequestEvent(Optional.ofNullable(window)));
    }
}
