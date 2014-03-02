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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.addons.ui_swing.dialogs.sslcertificate.SSLCertificateDialog;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.tls.CertificateManager;
import com.dmdirc.tls.CertificateProblemListener;
import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * The ServerFrame is the MDI window that shows server messages to the user.
 */
@Factory(inject = true, singleton = true, providers = true)
public final class ServerFrame extends InputTextFrame implements
        ActionListener, CertificateProblemListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 9;
    /** Swing controller. */
    private final SwingController controller;
    /** popup menu item. */
    private JMenuItem settingsMI;
    /** The SSL certificate dialog we're displaying for this server, if any. */
    private SSLCertificateDialog sslDialog = null;

    /**
     * Creates a new ServerFrame.
     *
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param owner              Parent Frame container
     */
    public ServerFrame(
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            @Unbound final Server owner) {
        super(deps, inputFieldProvider, owner);

        this.controller = deps.controller;
        initComponents();

        owner.addCertificateProblemListener(this);
    }

    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);

        setLayout(new MigLayout("ins 0, fill, hidemode 3, wrap 1"));
        add(getTextPane(), "grow, push");
        add(getSearchBar(), "growx, pushx");
        add(inputPanel, "growx, pushx");
    }

    /**
     * {@inheritDoc}.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == settingsMI) {
            controller.showServerSettingsDialog((Server) getContainer().getConnection());
        }
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_NICK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_HYPERLINK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        if (getContainer().getConnection().getState()
                .equals(ServerState.CONNECTED)) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }

        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }

        popupMenu.add(settingsMI);
    }

    /** {@inheritDoc} */
    @Override
    public void certificateProblemEncountered(final X509Certificate[] chain,
            final Collection<CertificateException> problems,
            final CertificateManager certificateManager) {
        sslDialog = new SSLCertificateDialog(controller.getIconManager(), controller.getMainFrame(),
                new SSLCertificateDialogModel(chain, problems, certificateManager));
        sslDialog.display();
    }

    /** {@inheritDoc} */
    @Override
    public void certificateProblemResolved(final CertificateManager manager) {
        if (sslDialog != null) {
            sslDialog.dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer window) {
        controller.closeServerSettingsDialog((Server) window.getConnection());
        super.windowClosing(window);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        ((Connection) frameParent).removeCertificateProblemListener(this);
        super.dispose();
    }

}
