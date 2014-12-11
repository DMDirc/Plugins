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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.interfaces.ui.AboutDialogModel;
import com.dmdirc.ui.core.util.URLHandler;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import net.miginfocom.swing.MigLayout;

/**
 * About DMDirc panel.
 */
public final class AboutPanel extends JPanel {

    private static final long serialVersionUID = 1;
    private final URLHandler urlHandler;
    private final AboutDialogModel model;

    public AboutPanel(final URLHandler urlHandler, final AboutDialogModel model) {
        this.urlHandler = urlHandler;
        this.model = model;
        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    private void initComponents() {
        final TextLabel about = new TextLabel(model.getAbout(), false);
        about.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                urlHandler.launchApp(e.getURL());
            }
        });
        setLayout(new MigLayout("ins rel, fill"));
        add(about, "align center");
    }
}
