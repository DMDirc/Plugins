/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.util.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * File browser component.
 *
 * @author Simon Mott
 * @since 0.6.3
 */
public class FileBrowser extends JPanel implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** Browse button. */
    private JButton browseButton;
    /** Text field to show path of chosen file. */
    private JTextField pathField;
    /** File browsing window. */
    private JFileChooser fileChooser = new JFileChooser();
    /** Our listeners. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new File Browser.
     *
     * @param filePath Initial path to display
     */
    public FileBrowser(final String filePath) {
        browseButton = new JButton("Browse");
        browseButton.addActionListener(this);

        pathField = new JTextField(filePath);

        setLayout(new MigLayout("ins 0, fill"));
        add(pathField, "growx, pushx, sgy all");
        add(browseButton, "sgy all");
    }

    /**
     * {@inheritDoc}.
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        fileChooser.showOpenDialog(null);
        try {
            pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        } catch (NullPointerException ex) {
            //Do nothing as Cancel was selected
        }
        fireActionEvent();
    }

    /**
     * Adds an action listener to this file browser. Action
     * listeners are notified whenever the path changes.
     *
     * @param l The listener to be added
     */
    public void addActionListener(final ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    /**
     * Returns the current path selected by this file browser.
     *
     * @return Path selected by this filebrowser
     */
    public String getPath() {
        return pathField.getText();
    }

    /**
     * Informs all action listeners that an action has occured.
     */
    protected void fireActionEvent() {
        for (ActionListener listener : listeners.get(ActionListener.class)) {
            listener.actionPerformed(new ActionEvent(this, 1, "stuffChanged"));
        }
    }
}
