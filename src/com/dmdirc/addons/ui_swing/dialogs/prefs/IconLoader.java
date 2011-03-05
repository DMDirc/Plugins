/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.IconManager;
import java.util.concurrent.ExecutionException;
import javax.swing.Icon;

/**
 * Loads an icon in the background and uses it for a category label once it
 * has been loaded.
 */
public class IconLoader extends LoggingSwingWorker<Icon, Void> {

    /** Category this icon will be used for. */
    private final CategoryLabel label;
    /** Icon to load. */
    private final String icon;

    /**
     * Creates a new icon loader adding the specified icon to the specified
     * icon after it has been loaded in the background.
     *
     * @param label Label to load category for
     * @param icon Icon to load
     */
    public IconLoader(final CategoryLabel label, final String icon) {
        super();

        this.label = label;
        this.icon = icon;
    }

    /** {@inheritDoc} */
    @Override
    protected Icon doInBackground() {
        return IconManager.getIconManager().getIcon(icon);
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        try {
            label.setIcon(get());
        } catch (InterruptedException ex) {
            //Ignore
        } catch (ExecutionException ex) {
            Logger.appError(ErrorLevel.LOW, ex.getMessage(), ex);
        }

    }
}
