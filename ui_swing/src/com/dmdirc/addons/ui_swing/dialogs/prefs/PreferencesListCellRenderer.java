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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.ui.IconManager;

import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Prefs dialog list cell renderer.
 */
public class PreferencesListCellRenderer extends JLabel implements
        ListCellRenderer<PreferencesCategory> {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Number of categories shown. */
    private final int numCats;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Label map. */
    private final Map<PreferencesCategory, JLabel> labelMap;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;

    /**
     * Instantiates a new prefs list cell renderer.
     *
     * @param iconManager Icon manager to load icons
     * @param eventBus    The event bus to post errors to
     * @param numCats     Number of categories in the list
     */
    public PreferencesListCellRenderer(final IconManager iconManager, final DMDircMBassador eventBus,
            final int numCats) {
        labelMap = new HashMap<>();
        this.numCats = numCats;
        this.iconManager = iconManager;
        this.eventBus = eventBus;
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends PreferencesCategory> list,
            final PreferencesCategory value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        if (!labelMap.containsKey(value)) {
            labelMap.put(value, new CategoryLabel(iconManager, eventBus,
                    list, value, numCats, index));
        }
        final JLabel label = labelMap.get(value);

        if (isSelected) {
            label.setFont(getFont().deriveFont(Font.BOLD));
        } else {
            label.setFont(getFont().deriveFont(Font.PLAIN));
        }

        return label;
    }

}
