/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

/**
 * A window to show the progress whilst installing an addon.
 */
public class InstallerWindow extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Downloader progress bar. */
    private final JProgressBar jpb = new JProgressBar(0, 100);
    /** Parent window. */
    private BrowserWindow parentWindow;
    /** Message label. */
    private TextLabel label;
    /** Addon info. */
    private AddonInfo info;

    /**
     * Instantiates a new installer window.
     *
     * @param parentWindow Parent window
     * @param info Assocaited addon info
     */
    public InstallerWindow(final BrowserWindow parentWindow, final AddonInfo info) {
        super(parentWindow, ModalityType.DOCUMENT_MODAL);
        this.info = info;
        this.parentWindow = parentWindow;
        setTitle("Installing addon...");
        jpb.setIndeterminate(true);
        label = new TextLabel("Installing: " + info.getTitle());
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        setLayout(new MigLayout("fill, wrap 1"));
        add(label);
        add(jpb, "grow");
        add(getOkButton(), "split, right");
        getOkButton().setEnabled(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setIconImages(parentWindow.getIconImages());
    }

    /**
     * {@inheritDoc}
     *
     * @param ae Action event
     */
    @Override
    public void actionPerformed(final ActionEvent ae) {
        dispose();
    }

    /**
     * Tells the window the installation has finished and displays the
     * appropriate message to the user.
     *
     * @param message Message to display (or an empty sting for success)
     */
    public void finished(final String message) {
        if (message.isEmpty()) {
            label.setText("Finished installing addon: " + info.getTitle());
        } else {
            label.setText("Error installing addon: " + info.getTitle() + "<br>"
                    + message);
        }
        jpb.setIndeterminate(false);
        jpb.setValue(100);
        getOkButton().setEnabled(true);
        pack();
        parentWindow.loadData();
    }
}
