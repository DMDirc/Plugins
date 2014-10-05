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
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.injection.SwingEventBus;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.ui.WindowManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JPopupMenu;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

/**
 * Separator that only appears when there are top level windows.
 */
public class WindowMenuSeparator extends JPopupMenu.Separator implements ActionListener {

    private final ActiveFrameManager activeFrameManager;
    private final DMDircMBassador eventBus;
    private final WindowManager windowManager;

    @Inject
    public WindowMenuSeparator(final ActiveFrameManager activeFrameManager,
            @SwingEventBus final DMDircMBassador eventBus,
            final WindowManager windowManager) {
        this.activeFrameManager = activeFrameManager;
        this.eventBus = eventBus;
        this.windowManager = windowManager;
    }
    /**
     * Initialises the menu item adding listeners as required.
     */
    public void init() {
        eventBus.subscribe(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        activeFrameManager.getActiveFrame().getContainer().close();
    }

    @Handler(invocation = EdtHandlerInvocation.class, priority = Integer.MIN_VALUE,
            delivery = Invoke.Asynchronously)
    public void windowAdded(final SwingWindowAddedEvent event) {
        setVisible(true);
    }

    @Handler(invocation = EdtHandlerInvocation.class, priority = Integer.MIN_VALUE,
            delivery = Invoke.Asynchronously)
    public void windowDeleted(final SwingWindowDeletedEvent event) {
        setVisible(!windowManager.getRootWindows().isEmpty());
    }
}
