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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.util.ClientInfo;
import com.dmdirc.util.DateUtils;

import java.awt.Font;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.layout.LayoutUtil;
import net.miginfocom.swing.MigLayout;

/** Info panel. */
public final class InfoPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Parent controller. */
    private final SwingController controller;
    /** The config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** The base directory used for settings. */
    private final String baseDirectory;

    /**
     * Creates a new instance of InfoPanel.
     *
     * @param controller    Parent swing controller
     * @param globalConfig  The config to read settings from.
     * @param baseDirectory The base directory that DMDirc is using for settings.
     */
    @Inject
    public InfoPanel(
            final SwingController controller,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @Directory(DirectoryType.BASE) final String baseDirectory) {
        this.controller = controller;
        this.globalConfig = globalConfig;
        this.baseDirectory = baseDirectory;
        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        final JScrollPane scrollPane = new JScrollPane();
        final JEditorPane infoPane = new JEditorPane();
        infoPane.setEditorKit(new HTMLEditorKit());
        final Font font = UIManager.getFont("Label.font");
        ((HTMLDocument) infoPane.getDocument()).getStyleSheet().addRule("body "
                + "{ font-family: " + font.getFamily() + "; " + "font-size: "
                + font.getSize() + "pt; }");

        infoPane.setText("<html>"
                + "<b>DMDirc version: </b>" + ClientInfo.getVersionInformation() + "<br>"
                + "<b>Mode Aliases version: </b>"
                + globalConfig.getOption("identity", "modealiasversion")
                + "<br>"
                + "<b>Swing UI version: </b>" + controller.getVersion() + "<br>"
                + "<b>OS Version: </b>" + ClientInfo.getOperatingSystemInformation() + "<br>"
                + "<b>Profile directory: </b>" + baseDirectory + "<br>"
                + "<b>Java version: </b>" + ClientInfo.getJavaInformation() + "<br>"
                + "<b>Look and Feel: </b>" + SwingController.getLookAndFeel()
                + "<br>"
                + "<b>MiG Layout version: </b>" + LayoutUtil.getVersion()
                + "<br>"
                + "<b>Java Default charset: </b>" + Charset.defaultCharset().displayName() + "<br>"
                + "<b>Client Uptime: </b>"
                + DateUtils.formatDuration((int) ClientInfo.getUptime() / 1000) + "<br>"
                + "</html>");
        infoPane.setEditable(false);
        scrollPane.setViewportView(infoPane);

        UIUtilities.resetScrollPane(scrollPane);

        setLayout(new MigLayout("ins rel, fill"));
        add(scrollPane, "grow, push, wrap");
    }

}
