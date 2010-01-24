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

package com.dmdirc.addons.ui_web.uicomponents;

import com.dmdirc.FrameContainer;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;
import com.dmdirc.addons.ui_web.WebInterfaceUI;
import javax.swing.JComponent;

/**
 *
 * @author chris
 */
public class WebFrameManager implements FrameManager {

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionVertically() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionHorizontally() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        DynamicRequestHandler.addEvent(new Event("newwindow", window.getFrame()));
        WebInterfaceUI.active = (WebWindow) window.getFrame();
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent, final FrameContainer window) {
        DynamicRequestHandler.addEvent(new Event("newchildwindow", new Object[]{
            parent.getFrame(), window.getFrame()
        }));
        WebInterfaceUI.active = (WebWindow) window.getFrame();
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
