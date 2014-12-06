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

import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.UIManager;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

/**
 * Changes the font for a JComponent on a selection change.
 */
@Listener(references = References.Strong)
public class WindowSelectionFontChanger {

    private final JComponent component;
    private final TextFrame window;

    public WindowSelectionFontChanger(final JComponent component, final TextFrame window) {
        this.component = component;
        this.window = window;
    }

    public void init(final ActiveFrameManager activeFrameMaanger, final SwingEventBus eventBus) {
        eventBus.subscribe(this);
        selectionChanged(new SwingWindowSelectedEvent(activeFrameMaanger.getActiveFrame()));
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void selectionChanged(final SwingWindowSelectedEvent event) {
        // TODO: Check children and set italic
        if (event.getWindow().isPresent() && window.equals(event.getWindow().get())) {
            component.setFont(UIManager.getFont("MenuItem.font").deriveFont(Font.BOLD));
        } else {
            component.setFont(UIManager.getFont("MenuItem.font").deriveFont(Font.PLAIN));
        }
    }
}
