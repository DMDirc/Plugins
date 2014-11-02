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

import com.dmdirc.ClientModule;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JMenuItem;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

/**
 * A Menu item that closes the active window when triggered.
 */
public class CloseActiveWindowMenuItem extends JMenuItem {

    private final ActiveFrameManager activeFrameManager;
    private final SwingEventBus eventBus;
    private final WindowManager windowManager;

    @Inject
    public CloseActiveWindowMenuItem(final ActiveFrameManager activeFrameManager,
            @ClientModule.GlobalConfig final IconManager iconManager,
            final SwingEventBus eventBus,
            final WindowManager windowManager) {
        super(iconManager.getIcon("close"));
        this.activeFrameManager = activeFrameManager;
        this.eventBus = eventBus;
        this.windowManager = windowManager;
        setMnemonic('c');
        setText("Close");
        setActionCommand("Close");
    }

    /**
     * Initialises the menu item adding listeners as required.
     */
    public void init() {
        addActionListener(l -> activeFrameManager.getActiveFrame()
                .ifPresent(c -> c.getContainer().close()));
        eventBus.subscribe(this);
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void windowAdded(final SwingWindowAddedEvent event) {
        setEnabled(true);
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void windowDeleted(final SwingWindowDeletedEvent event) {
        setEnabled(!windowManager.getRootWindows().isEmpty());
    }
}
