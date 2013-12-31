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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

/**
 * Addon info label describing an addon.
 */
public class AddonInfoLabel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Addon info to be represented. */
    private final AddonInfo addonInfo;
    /** Parent window. */
    private final BrowserWindow parentWindow;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Creates a new addon info label to describe the specified addon info.
     *
     * @param controller Swing controller
     * @param addonInfo Addon to describe
     * @param parentWindow Parent window
     */
    public AddonInfoLabel(final SwingController controller,
            final AddonInfo addonInfo, final BrowserWindow parentWindow) {
        super();

        this.controller = controller;
        this.addonInfo = addonInfo;
        this.parentWindow = parentWindow;

        init();
    }

    /**
     * Initialises the components used in this addon info label.
     */
    private void init() {
        setLayout(new MigLayout("fillx, ins 0"));

        JLabel title = new JLabel(addonInfo.getTitle());
        title.setFont(title.getFont().deriveFont(16f).deriveFont(Font.BOLD));
        add(title, "wmin 165, wmax 165, gaptop 5, gapleft 5");

        title = new JLabel(addonInfo.getScreenshot());
        title.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(title, "wmin 150, wmax 150, hmin 150, hmax 150, wrap, spany 5, "
                + "gapright 5, gaptop 5, gapbottom 5");

        title = new JLabel(addonInfo.getType().toString() + ", rated "
                + addonInfo.getRating() + "/10");
        add(title, "wrap, gapleft 5");

        add(new TextLabel(addonInfo.getDescription()),
                "wmax 100%-170, hmax 150, growy, wrap, pushy, gapleft 5");

        final JButton button = new JButton("Install");
        button.addActionListener(new InstallListener(controller, addonInfo,
                parentWindow));
        final boolean installed = addonInfo.isInstalled();
        add(button, "split, gapleft 5");

        if (installed || !addonInfo.isDownloadable()) {
            button.setEnabled(false);
            title = new JLabel(installed ? "Already installed"
                    : "No download available");
            title.setForeground(Color.GRAY);
            add(title);
        }

        add(new JSeparator(), "newline, span, growx, pushx, gaptop 5");
    }

    /**
     * Returns the addon info for this label.
     *
     * @return Addon info
     */
    public AddonInfo getAddonInfo() {
        return addonInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void repaint() {
        //NOOP for performance reasons
    }

    /** {@inheritDoc} */
    @Override
    public void firePropertyChange(final String propertyName,
            final Object oldValue, final Object newValue) {
        //NOOP for performance reasons
    }

    /** {@inheritDoc} */
    @Override
    public void firePropertyChange(final String propertyName,
            final boolean oldValue, final boolean newValue) {
        //NOOP for performance reasons
    }

    /** {@inheritDoc} */
    @Override
    public void firePropertyChange(final String propertyName,
            final int oldValue, final int newValue) {
        //NOOP for performance reasons
    }
}
