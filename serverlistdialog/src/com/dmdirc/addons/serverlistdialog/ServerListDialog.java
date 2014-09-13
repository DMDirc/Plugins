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

package com.dmdirc.addons.serverlistdialog;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.addons.serverlists.ServerGroupItem;
import com.dmdirc.addons.ui_swing.components.LockedLayer;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.util.URLHandler;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Window;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorConvertOp;

import javax.inject.Inject;
import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.effect.BufferedImageOpEffect;

/**
 * Dialog to show and edit server lists.
 */
public class ServerListDialog extends StandardDialog implements
        ActionListener, ServerListListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Server list model. */
    private final ServerListModel model;
    /** Connect button. */
    private final JButton connectButton;
    /** Info lock. */
    private final LockedLayer<Info> infoLock;
    /** Perform lock. */
    private final LockedLayer<Perform> performLock;
    /** Profile lock. */
    private final LockedLayer<Profiles> profileLock;
    /** Settings lock. */
    private final LockedLayer<Settings> settingsLock;
    /** Info layer. */
    private final JXLayer<Info> infoLayer;
    /** Perform layer. */
    private final JXLayer<Perform> performLayer;
    /** Profile layer. */
    private final JXLayer<Profiles> profileLayer;
    /** Settings layer. */
    private final JXLayer<Settings> settingsLayer;
    /** Help panel. */
    private final Help help;

    /**
     * Creates a new server list dialog.
     *
     * @param iconManager        Icon manager to get icons
     * @param globalConfig       Global config to read settings from
     * @param urlHandler         The URL Handler to use to handle clicked links
     * @param performWrapper     The wrapper to use for the perform tab
     * @param serverListModel    The model to use for the dialog.
     * @param parentWindow       The window that owns this dialog.
     * @param settingsPanel      The panel to use for settings.
     * @param identityController Controller to use to get profiles.
     */
    @Inject
    public ServerListDialog(
            @GlobalConfig final IconManager iconManager,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final URLHandler urlHandler,
            final PerformWrapper performWrapper,
            final ServerListModel serverListModel,
            @MainWindow final Window parentWindow,
            final Settings settingsPanel,
            final IdentityController identityController,
            final ColourManagerFactory colourManagerFactory) {
        super(parentWindow, ModalityType.MODELESS);

        setTitle("Server List");
        model = serverListModel;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        connectButton = new JButton("Connect");

        profileLock = new LockedLayer<>(new BufferedImageOpEffect(
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        null)));
        performLock = new LockedLayer<>(new BufferedImageOpEffect(
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        null)));
        settingsLock = new LockedLayer<>(new BufferedImageOpEffect(
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        null)));
        infoLock = new LockedLayer<>(new BufferedImageOpEffect(
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        null)));
        profileLayer = new JXLayer<>(new Profiles(model, identityController), profileLock);
        performLayer = new JXLayer<>(new Perform(iconManager, globalConfig, performWrapper, model,
                colourManagerFactory),
                performLock);
        settingsLayer = new JXLayer<>(settingsPanel, settingsLock);
        infoLayer = new JXLayer<>(new Info(model, urlHandler), infoLock);
        help = new Help();
        lockLayers();

        setLayout(new MigLayout("fill, wrap 2, wmin 600, wmax 600"));

        add(new Tree(iconManager, model, this),
                "grow, spany 4, wmax 150, wmin 150");
        add(help, "pos 160 0.5al");
        add(infoLayer, "growx, pushx");
        add(settingsLayer, "grow, push, gaptop unrel, gapbottom unrel");
        add(performLayer, "grow, push");
        add(profileLayer, "growx, pushx");
        add(connectButton, "skip 1, split 3, right, gapright unrel*2, "
                + "sgx button");
        add(getLeftButton(), "right, sgx button");
        add(getRightButton(), "right, sgx button");

        addListeners();
    }

    /**
     * Adds requires listeners to objects.
     */
    private void addListeners() {
        model.addServerListListener(this);
        connectButton.addActionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            model.dialogClosed(true);
            dispose();
        } else if (e.getSource() == getCancelButton()) {
            model.dialogClosed(false);
            dispose();
        } else if (e.getSource() == connectButton) {
            model.getSelectedItem().connect();
        }
    }

    /**
     * Lock or unlock layers.
     */
    private void lockLayers() {
        final boolean lock = !model.hasItems()
                || model.getSelectedItem() == null;
        performLock.setLocked(lock);
        settingsLock.setLocked(lock);
        infoLock.setLocked(lock);
        profileLock.setLocked(lock);
        connectButton.setEnabled(!lock);
        performLayer.setVisible(!lock);
        settingsLayer.setVisible(!lock);
        infoLayer.setVisible(!lock);
        profileLayer.setVisible(!lock);
        help.setVisible(lock);
    }

    @Override
    public void serverGroupChanged(final ServerGroupItem item) {
        lockLayers();
    }

    @Override
    public void dialogClosed(final boolean save) {
        if (save) {
            model.save();
        }
    }

    @Override
    public void serverGroupAdded(final ServerGroupItem parent,
            final ServerGroupItem group) {
        lockLayers();
    }

    @Override
    public void serverGroupRemoved(final ServerGroupItem parent,
            final ServerGroupItem group) {
        lockLayers();
    }

}
