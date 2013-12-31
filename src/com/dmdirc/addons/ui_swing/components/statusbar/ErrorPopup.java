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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ErrorReportStatus;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.collections.MapList;

import java.awt.Font;
import java.awt.Window;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Shows a breakdown of errors that have occurred.
 *
 * @since 0.6.3m1
 */
public class ErrorPopup extends StatusbarPopupWindow {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Icon manager. */
    private final IconManager iconManager;

    /**
     * Creates a new error popup.
     *
     * @param controller Swing controller
     * @param parent Parent panel
     * @param parentWindow Parent window
     */
    public ErrorPopup(final SwingController controller, final JPanel parent,
            final Window parentWindow) {
        super(controller, parent, parentWindow);
        iconManager = controller.getIconManager();
    }

    /** {@inheritDoc} */
    @Override
    protected void initContent(final JPanel panel) {
        final List<ProgramError> errors = ErrorManager.getErrorManager().getErrors();
        final MapList<ErrorLevel, ProgramError> buckets = new MapList<>();
        final MapList<ErrorReportStatus, ProgramError> statuses = new MapList<>();

        for (final ProgramError error : errors) {
            buckets.add(error.getLevel(), error);
            statuses.add(error.getReportStatus(), error);
        }

        JLabel header = new JLabel("Severity");
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        panel.add(header);

        header = new JLabel("#", JLabel.RIGHT);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        panel.add(header, "growx, pushx, wrap");

        for (final ErrorLevel level : ErrorLevel.values()) {
            if (buckets.containsKey(level)) {
                final int count = buckets.values(level).size();

                panel.add(new JLabel(level.toString(), iconManager.getIcon(
                        level.getIcon()), JLabel.LEFT));
                panel.add(new JLabel(String.valueOf(count), JLabel.RIGHT),
                        "growx, pushx, wrap");
            }
        }

        panel.add(new JSeparator(), "span, growx, pushx, wrap");

        header = new JLabel("Report status");
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        panel.add(header);

        header = new JLabel("#", JLabel.RIGHT);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        panel.add(header, "growx, pushx, wrap");

        for (final ErrorReportStatus status : ErrorReportStatus.values()) {
            if (statuses.containsKey(status)) {
                final int count = statuses.values(status).size();

                panel.add(new JLabel(status.toString(), JLabel.LEFT));
                panel.add(new JLabel(String.valueOf(count), JLabel.RIGHT),
                        "growx, pushx, wrap");
            }
        }
    }
}
