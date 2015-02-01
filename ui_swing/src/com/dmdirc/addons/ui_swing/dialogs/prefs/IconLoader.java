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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;

import java.util.concurrent.ExecutionException;

import javax.swing.Icon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Loads an icon in the background and uses it for a category label once it has been loaded.
 */
public class IconLoader extends LoggingSwingWorker<Icon, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(IconLoader.class);
    /** Category this icon will be used for. */
    private final CategoryLabel label;
    /** Icon to load. */
    private final String icon;
    /** Icon manager. */
    private final IconManager iconManager;

    /**
     * Creates a new icon loader adding the specified icon to the specified icon after it has been
     * loaded in the background.
     *
     * @param iconManager Icon manager
     * @param label       Label to load category for
     * @param icon        Icon to load
     */
    public IconLoader(final IconManager iconManager, final CategoryLabel label, final String icon) {
        this.iconManager = iconManager;
        this.label = label;
        this.icon = icon;
    }

    @Override
    protected Icon doInBackground() {
        return iconManager.getIcon(icon);
    }

    @Override
    protected void done() {
        try {
            label.setIcon(get());
        } catch (InterruptedException ex) {
            //Ignore
        } catch (ExecutionException ex) {
            LOG.info(USER_ERROR, ex.getMessage(), ex);
        }

    }

}
