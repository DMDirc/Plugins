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

import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.sslcertificate.SSLCertificateDialog;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.events.ServerCertificateProblemEncounteredEvent;
import com.dmdirc.events.ServerCertificateProblemResolvedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;

import java.awt.Window;

import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

import net.engio.mbassy.listener.Handler;

/**
 * The ServerFrame is the MDI window that shows server messages to the user.
 */
public final class ServerFrame extends InputTextFrame {

    /** Serial version UID. */
    private static final long serialVersionUID = 9;
    /** Main window provider. */
    private final Provider<Window> mainWindow;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Dialog provider to close SSD. */
    private final KeyedDialogProvider<Connection, ServerSettingsDialog> dialogProvider;
    /** popup menu item. */
    private JMenuItem settingsMI;
    /** The SSL certificate dialog we're displaying for this server, if any. */
    private SSLCertificateDialog sslDialog;
    /** Server instance. */
    private final Connection connection;

    /**
     * Creates a new ServerFrame.
     *
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param dialogProvider     Dialog provider to close SSD with
     * @param owner              Parent Frame container
     */
    public ServerFrame(
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            final InputTextFramePasteActionFactory inputTextFramePasteActionFactory,
            final KeyedDialogProvider<Connection, ServerSettingsDialog> dialogProvider,
            final Connection owner) {
        super(deps, inputFieldProvider, inputTextFramePasteActionFactory, owner.getWindowModel());
        this.mainWindow = deps.mainWindow;
        this.iconManager = deps.iconManager;
        this.dialogProvider = dialogProvider;
        this.connection = owner;
        initComponents();
    }

    /**
     * Initialises the instance, adding any required listeners.
     */
    @Override
    public void init() {
        connection.getWindowModel().getEventBus().subscribe(this);
        super.init();
    }

    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(l -> dialogProvider.displayOrRequestFocus(connection));

        setLayout(new MigLayout("ins 0, fill, hidemode 3, wrap 1"));
        add(getTextPane(), "grow, push");
        add(getSearchBar(), "growx, pushx");
        add(inputPanel, "growx, pushx");
    }

    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_NICK;
    }

    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_HYPERLINK;
    }

    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        if (getContainer().getConnection().get().getState() == ServerState.CONNECTED) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }

        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }

        popupMenu.add(settingsMI);
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleCertProblem(final ServerCertificateProblemEncounteredEvent event) {
        if (sslDialog != null) {
            sslDialog.dispose();
        }

        sslDialog = new SSLCertificateDialog(iconManager, mainWindow.get(),
                new SSLCertificateDialogModel(event.getCertificateChain(), event.getProblems(),
                        event.getCertificateManager()));
        sslDialog.display();
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleCertProblemResolved(final ServerCertificateProblemResolvedEvent event) {
        if (sslDialog != null) {
            sslDialog.dispose();
        }
    }

    @Override
    @Handler(invocation = EdtHandlerInvocation.class)
    public void windowClosing(final FrameClosingEvent event) {
        connection.getWindowModel().getEventBus().unsubscribe(this);
        dialogProvider.dispose(connection);
        super.windowClosing(event);
    }

    @Override
    public void dispose() {
        connection.getWindowModel().getEventBus().unsubscribe(this);
        super.dispose();
    }

}
