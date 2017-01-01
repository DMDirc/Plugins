/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.AboutDialogModel;
import com.dmdirc.ui.core.util.URLHandler;
import java.awt.Window;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;

/**
 * About dialog.
 */
public class AboutDialog extends StandardDialog {

    private static final long serialVersionUID = 5;
    private final URLHandler urlHandler;
    private final AboutDialogModel model;
    private final AggregateConfigProvider config;

    @Inject
    public AboutDialog(
            @GlobalConfig final AggregateConfigProvider config,
            @MainWindow final Window parentWindow,
            final AboutDialogModel model,
            final URLHandler urlHandler) {
        super(parentWindow, ModalityType.MODELESS);
        this.urlHandler = urlHandler;
        this.model = model;
        this.config = config;
        model.load();
        initComponents();
    }

    /** Initialises the main UI components. */
    private void initComponents() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setResizable(false);

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(e -> dispose());
        getCancelButton().addActionListener(e -> dispose());

        tabbedPane.add("About", new AboutPanel(urlHandler, model));
        tabbedPane.add("Credits", new CreditsPanel(urlHandler, model));
        tabbedPane.add("Licences", new LicencesPanel(model, config));
        tabbedPane.add("Information", new InfoPanel(model));

        getContentPane().setLayout(new MigLayout("ins rel, wrap 1, fill, "
                + "wmin 600, wmax 600, hmin 400, hmax 400"));
        getContentPane().add(tabbedPane, "grow, push");
        getContentPane().add(getOkButton(), "right");
    }
}
