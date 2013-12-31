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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.config.prefs.PreferencesSetting;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

/**
 * Add option combo box cell renderer.
 */
public final class AddOptionCellRenderer extends DMDircListCellRenderer {

    /**
     * A version number for this class.
     */
    private static final long serialVersionUID = 1;
    /** Parent settings panel. */
    private final SettingsPanel settingsPanel;

    /**
     * Creates a new add option cell renderer.
     *
     * @param settingsPanel Parent settings panel
     * @param renderer Parent renderer
     */
    public AddOptionCellRenderer(final SettingsPanel settingsPanel,
            final ListCellRenderer renderer) {
        super(renderer);

        this.settingsPanel = settingsPanel;
    }

    /** {@inheritDoc} */
    @Override
    protected void renderValue(final JLabel label, final Object value,
            final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final PreferencesSetting setting;
        if (value instanceof JComponent) {
            setting = settingsPanel.getSettingForComponent((JComponent) value);
        } else {
            setting = null;
        }
        if (setting == null) {
           label.setText((String) value);
        } else {
            label.setText(setting.getTitle());
        }
    }
}
