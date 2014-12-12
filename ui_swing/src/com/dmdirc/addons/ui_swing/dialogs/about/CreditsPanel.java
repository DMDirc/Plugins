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
import com.dmdirc.ui.core.about.Developer;
import com.dmdirc.ui.core.util.URLHandler;

import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent.EventType;

import net.miginfocom.swing.MigLayout;

/**
 * Authors Panel.
 */
public final class CreditsPanel extends JPanel {

    private static final long serialVersionUID = 2;
    private final URLHandler urlHandler;
    private final AboutDialogModel model;

    public CreditsPanel(final URLHandler urlHandler, final AboutDialogModel model) {
        this.urlHandler = urlHandler;
        this.model = model;

        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Main Developers:</h3>");
        sb.append("<ul style='margin-top: 0px;'>");
        final Consumer<Developer> developerConsumer = i -> {
                sb.append("<li><a href=\"").append(i.getWebsite()).append("\">");
                sb.append(i.getName()).append("</a>").append("</li>");
        };
        model.getMainDevelopers().forEach(developerConsumer);
        sb.append("</ul>");
        sb.append("<h3 style='margin: 3px; padding: 0px 0px 5px 0px;'>Other Developers: </h3>");
        sb.append("<ul style='margin-top: 0px;'>");
        model.getOtherDevelopers().forEach(developerConsumer);
        sb.append("</ul></html>");
        final TextLabel about = new TextLabel(sb.toString());
        about.addHyperlinkListener(e -> {
            if (e.getEventType() == EventType.ACTIVATED) {
                urlHandler.launchApp(e.getURL());
            }
        });

        setLayout(new MigLayout("ins rel, fill"));
        final JScrollPane scrollPane = new JScrollPane(about);
        scrollPane.setOpaque(UIUtilities.getTabbedPaneOpaque());
        scrollPane.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        add(scrollPane, "grow, push");
    }

}
