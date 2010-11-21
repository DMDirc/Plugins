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

package com.dmdirc.addons.ui_web;

import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.UIController;

import org.mortbay.jetty.Handler;

/**
 * The main web interface plugin.
 * 
 * @author chris
 */
public class WebInterfacePlugin extends Plugin {
    
    /** The UI that we're using. */
    private WebInterfaceUI ui;

    /** {@inheritDoc} */
    @Override
    public void onLoad() {   
        if (ui == null) {
             ui = new WebInterfaceUI(this);
        }
    }

    /**
     * Returns the UI Controller for the web interface.
     *
     * @return The web interface's UI controller
     */
    public UIController getController() {
        return ui;
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        // Do nothing
    }
    
    /**
     * Adds the specified handler to the WebInterface's web server.
     * 
     * @param newHandler The handler to be added
     */
    public void addWebHandler(final Handler newHandler) {
        if (ui == null) {
             ui = new WebInterfaceUI(this);
        }
        
        ui.addWebHandler(newHandler);
    }
}
