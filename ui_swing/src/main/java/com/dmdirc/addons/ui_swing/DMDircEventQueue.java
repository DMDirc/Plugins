/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing;

import com.dmdirc.addons.ui_swing.actions.CopyAction;
import com.dmdirc.addons.ui_swing.actions.CutAction;
import com.dmdirc.addons.ui_swing.events.SwingWindowEvent;
import com.dmdirc.addons.ui_swing.events.ClientKeyPressedEvent;
import com.dmdirc.events.eventbus.EventBus;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Custom event queue to add common functionality to certain components.
 */
@Singleton
public class DMDircEventQueue extends EventQueue {

    /** Event bus to dispatch events to. */
    private final EventBus eventBus;
    /** Clipboard to copy and paste from. */
    private final Clipboard clipboard;

    @Inject
    public DMDircEventQueue(final EventBus eventBus, final Clipboard clipboard) {
        this.eventBus = eventBus;
        this.clipboard = clipboard;
    }

    @Override
    protected void dispatchEvent(final AWTEvent event) {
        preDispatchEvent(event);
        super.dispatchEvent(event);
        postDispatchEvent(event);
        if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        } else if (event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
        } else if (event instanceof WindowEvent) {
            eventBus.publish(new SwingWindowEvent((WindowEvent) event));
        }
    }

    /**
     * Triggered before the event is dispatched to AWT.
     *
     * @param event Event about to be dispatched
     */
    protected void preDispatchEvent(final AWTEvent event) {
        //Do nothing
    }

    /**
     * Called just after an event is dispatched to AWT.
     *
     * @param event Event that has been dispatched
     */
    protected void postDispatchEvent(final AWTEvent event) {
        //Do nothing
    }

    /**
     * Handles key events.
     *
     * @param ke Key event
     */
    private void handleKeyEvent(final KeyEvent ke) {
        eventBus.publishAsync(new ClientKeyPressedEvent(
                KeyStroke.getKeyStroke(ke.getKeyChar(), ke.getModifiers())));
    }

    /**
     * Handles mouse events.
     *
     * @param me Mouse event
     */
    private void handleMouseEvent(final MouseEvent me) {
        if (!me.isPopupTrigger() || me.getComponent() == null) {
            return;
        }

        final Component comp = SwingUtilities.getDeepestComponentAt(
                me.getComponent(), me.getX(), me.getY());

        if (!(comp instanceof JTextComponent) || MenuSelectionManager
                .defaultManager().getSelectedPath().length > 0) {
            return;
        }

        final JTextComponent tc = (JTextComponent) comp;
        final JPopupMenu menu = new JPopupMenu();
        menu.add(new CutAction(tc));
        menu.add(new CopyAction(tc));
        menu.add(new PasteAction(clipboard, tc));

        final Point pt = SwingUtilities.convertPoint(me.getComponent(),
                me.getPoint(), tc);
        menu.show(tc, pt.x, pt.y);
    }

    @Override
    public void pop() { //NOPMD
        super.pop();
    }

}
