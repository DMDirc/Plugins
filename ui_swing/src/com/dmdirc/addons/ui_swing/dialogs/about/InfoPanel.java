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
import com.dmdirc.interfaces.ui.AboutDialogModel;

import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

/** Info panel. */
public final class InfoPanel extends JPanel {

    private static final long serialVersionUID = 1;
    private final AboutDialogModel model;

    public InfoPanel(final AboutDialogModel model) {
        this.model = model;
        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    private void initComponents() {
        final JScrollPane scrollPane = new JScrollPane();
        final JEditorPane infoPane = new JEditorPane();
        infoPane.setEditorKit(new HTMLEditorKit());
        final Font font = UIManager.getFont("Label.font");
        ((HTMLDocument) infoPane.getDocument()).getStyleSheet().addRule("body "
                + "{ font-family: " + font.getFamily() + "; " + "font-size: "
                + font.getSize() + "pt; }");

        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        model.getInfo().forEach(i -> {
            sb.append("<b>").append(i.getDescription()).append(": ").append("</b>");
            sb.append(i.getInformation()).append("<br>");
        });
        sb.append("</html>");
        infoPane.setText(sb.toString());
        infoPane.setEditable(false);
        scrollPane.setViewportView(infoPane);

        UIUtilities.resetScrollPane(scrollPane);

        setLayout(new MigLayout("ins rel, fill"));
        add(scrollPane, "grow, push, wrap");
    }

}
