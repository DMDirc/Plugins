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

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * About dialog.
 */
public class AboutDialog extends StandardDialog implements ActionListener, ChangeListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 5;
    /** Tabbed pane to use. */
    private JTabbedPane tabbedPane;
    /** Credits panel. */
    private CreditsPanel cp;
    /** Tab history. */
    private int history = 0;

    /**
     * Creates a new instance of AboutDialog.
     *
     * @param parentWindow  Parent window
     * @param infoPanel     The info panel to display.
     * @param creditsPanel  The credits panel to display.
     * @param licensesPanel The licenses panel to display.
     * @param aboutPanel    The about panel to display.
     */
    @Inject
    public AboutDialog(
            final MainFrame parentWindow,
            final InfoPanel infoPanel,
            final CreditsPanel creditsPanel,
            final LicencesPanel licensesPanel,
            final AboutPanel aboutPanel) {
        super(parentWindow, ModalityType.MODELESS);

        initComponents(infoPanel, creditsPanel, licensesPanel, aboutPanel);
    }

    /** Initialises the main UI components. */
    private void initComponents(
            final InfoPanel infoPanel,
            final CreditsPanel creditsPanel,
            final LicencesPanel licensesPanel,
            final AboutPanel aboutPanel) {
        tabbedPane = new JTabbedPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setResizable(false);

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        cp = creditsPanel;

        tabbedPane.add("About", aboutPanel);
        tabbedPane.add("Credits", cp);
        tabbedPane.add("Licences", licensesPanel);
        tabbedPane.add("Information", infoPanel);
        tabbedPane.addChangeListener(this);

        getContentPane().setLayout(new MigLayout("ins rel, wrap 1, fill, "
                + "wmin 600, wmax 600, hmin 400, hmax 400"));
        getContentPane().add(tabbedPane, "grow, push");
        getContentPane().add(getOkButton(), "right");
    }

    /**
     * {@inheritDoc}.
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        dispose();
    }

    
    @Override
    public boolean enterPressed() {
        executeAction(getOkButton());
        return true;
    }

    
    @Override
    public void stateChanged(final ChangeEvent e) {
        history = 10 * (history % 10000) + tabbedPane.getSelectedIndex();

        if (history / 10 % 100 == 32 && (history & 1) == 1
                && history >> 8 == 118) {
            cp.showEE();
        }
    }

}
