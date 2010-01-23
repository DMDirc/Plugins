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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.GenericListModel;
import com.dmdirc.addons.ui_swing.components.ListScroller;

import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

/**
 * Licences panel.
 */
public final class LicencesPanel extends JPanel implements ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Licence scroll pane. */
    private JScrollPane scrollPane;
    /** Licence list model */
    private GenericListModel<Licence> listModel;
    /** Licence textpane. */
    private JEditorPane licence;
    /** Licence list. */
    private JList list;
    /** Selected index. */
    private int selectedIndex;

    /** Creates a new instance of LicencesPanel. */
    public LicencesPanel() {
        super();

        initComponents();
        addListeners();
        layoutComponents();

        list.setSelectedIndex(0);
    }

    /**
     * Adds the listeners to the components.
     */
    private void addListeners() {
        list.addListSelectionListener(this);
    }

    /**
     *  Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("ins rel, fill"));
        add(new JScrollPane(list), "growy, pushy, w 150!");
        add(scrollPane, "grow, push");
    }

    /** Initialises the components. */
    private void initComponents() {
        setOpaque(UIUtilities.getTabbedPaneOpaque());
        listModel = new GenericListModel<Licence>();
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new ListScroller(list);
        new LicenceLoader(listModel).execute();
        licence = new JEditorPane();
        licence.setEditorKit(new HTMLEditorKit());
        final Font font = UIManager.getFont("Label.font");
        ((HTMLDocument) licence.getDocument()).getStyleSheet().addRule("body " +
                "{ font-family: " + font.getFamily() + "; " + "font-size: " +
                font.getSize() + "pt; }");
        licence.setEditable(false);
        scrollPane = new JScrollPane(licence);
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (list.getSelectedIndex() == -1) {
                list.setSelectedIndex(selectedIndex);
            } else {
                licence.setText(listModel.get(list.getSelectedIndex()).getBody());
                UIUtilities.resetScrollPane(scrollPane);
            }
            selectedIndex = list.getSelectedIndex();
        }
    }
}
