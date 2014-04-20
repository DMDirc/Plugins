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

package com.dmdirc.addons.ui_swing.dialogs.url;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.addons.ui_swing.components.URLProtocolPanel;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.ui.core.util.URLHandler;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

/** URL Protocol dialog. */
@Factory(inject = true, singleton = true)
public class URLDialog extends StandardDialog implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** URL protocol config panel. */
    private URLProtocolPanel panel;
    /** URL. */
    private final URI url;
    /** Blurb label. */
    private TextLabel blurb;
    /** Swing controller. */
    private final Window parentWindow;
    /** The URL Handler to use to handle clicked links. */
    private final URLHandler urlHandler;

    /**
     * Instantiates the URLDialog.
     *
     * @param url          URL to open once added
     * @param global       Global Configuration
     * @param config       User settings
     * @param parentWindow Parent window
     * @param urlHandler   The URL Handler to use to handle clicked links
     */
    public URLDialog(
            @Unbound final URI url,
            @SuppressWarnings("qualifiers") @GlobalConfig final AggregateConfigProvider global,
            @SuppressWarnings("qualifiers") @UserConfig final ConfigProvider config,
            @SuppressWarnings("qualifiers") @MainWindow final Window parentWindow,
            final URLHandler urlHandler) {
        super(parentWindow, ModalityType.MODELESS);

        this.url = url;
        this.parentWindow = parentWindow;
        this.urlHandler = urlHandler;

        initComponents(global, config);
        layoutComponents();
        addListeners();

        setTitle("Unknown URL Protocol");
    }

    /** Initialises the components. */
    private void initComponents(final AggregateConfigProvider global, final ConfigProvider config) {
        orderButtons(new JButton(), new JButton());
        blurb = new TextLabel("Please select the appropriate action to " + "handle " + url.
                getScheme() + ":// URLs from the list " + "below.");
        panel = new URLProtocolPanel(global, config, url, false);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, pack"));

        add(blurb, "");
        add(panel, "grow, push");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            panel.save();
            dispose();
            urlHandler.launchApp(url);
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }

    @Override
    public boolean enterPressed() {
        if (panel.getSelection().isEmpty()) {
            return false;
        } else {
            executeAction(getOkButton());
            return true;
        }
    }

    @Override
    public void validate() {
        super.validate();

        setLocationRelativeTo(parentWindow);
    }

}
