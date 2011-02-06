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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowListener;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Provides an MDI style bar for closing frames.
 */
public class MDIBar extends JPanel implements SwingWindowListener,
        SelectionListener, ActionListener, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = -8028057596226636245L;
    /** Icon size for the close button. */
    private static final int ICON_SIZE = 12;
    /** Button to close frames. */
    private final NoFocusButton closeButton;
    /** Main frame component bieng shown in. */
    private final MainFrame mainFrame;
    /** Config manager to get settings from. */
    private final ConfigManager config;
    /** Option domain. */
    private final String configDomain;
    /** Are we meant to be visible? */
    private boolean visibility;

    /**
     * Instantiates a new MDI bar.
     *
     * @param controller The controller that owns this MDI bar
     * @param mainFrame Main frame instance
     */
    public MDIBar(final SwingController controller, final MainFrame mainFrame) {
        super();

        this.mainFrame = mainFrame;
        this.config = IdentityManager.getGlobalConfig();
        this.configDomain = controller.getDomain();
        visibility = config.getOptionBool(configDomain, "mdiBarVisibility");

        closeButton = new NoFocusButton(IconManager.getIconManager().
                getScaledIcon("close-12", ICON_SIZE, ICON_SIZE));

        setOpaque(false);
        setLayout(new MigLayout("hmax 17, ins 1 0 0 0, fill"));
        add(closeButton, "w 17!, h 17!, right");

        controller.getWindowFactory().addWindowListener(this);

        mainFrame.addSelectionListener(this);
        closeButton.addActionListener(this);
        config.addChangeListener(configDomain, "mdiBarVisibility", this);

        check();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        closeButton.setEnabled(enabled);
    }

    /**
     * Checks whether this MDI bar should be visible and active and sets itself
     * accordingly.
     */
    private void check() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setVisible(visibility);
                setEnabled(mainFrame.getActiveFrame() != null);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void windowAdded(final TextFrame parent, final TextFrame window) {
        check();
    }

    /** {@inheritDoc} */
    @Override
    public void windowDeleted(final TextFrame parent, final TextFrame window) {
        check();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (mainFrame.getActiveFrame() == null) {
            return;
        }
        if (closeButton.equals(e.getSource())) {
            mainFrame.getActiveFrame().getContainer().close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        visibility = config.getOptionBool(configDomain, "mdiBarVisibility");
        check();
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final TextFrame window) {
        check();
    }
}
