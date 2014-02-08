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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.collections.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * File browser component.
 *
 * @since 0.6.3
 */
public class FileBrowser extends JPanel implements ActionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Text field to show path of chosen file. */
    private final ValidatingJTextField pathField;
    /** File browsing window. */
    private final JFileChooser fileChooser = new JFileChooser();
    /** Our listeners. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new File Browser.
     *
     * @param iconManager Icon Manager to get icons from
     * @param setting The setting to create the component for
     * @param type The type of filechooser we want (Files/Directories/Both)
     */
    public FileBrowser(final IconManager iconManager,
            final PreferencesSetting setting, final int type) {
        super();

        fileChooser.setFileSelectionMode(type);

        final JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(this);

        pathField = new ValidatingJTextField(iconManager, setting.getValue(), setting.getValidator());

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
    public void actionPerformed(final ActionEvent e) {
        fileChooser.showOpenDialog(this);

        if (fileChooser.getSelectedFile() != null) {
            pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
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
     * {@inheritDoc}.
     *
     * @param l The listener to be added
     */
    @Override
    public void addKeyListener(final KeyListener l) {
        pathField.addKeyListener(l);
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
            listener.actionPerformed(new ActionEvent(this, 1, getPath()));
        }
    }
}
